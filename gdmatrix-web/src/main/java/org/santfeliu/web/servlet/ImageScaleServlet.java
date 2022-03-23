/*
 * GDMatrix
 *
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.web.servlet;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Utilities;

/**
 *
 * @author blanquepa
 */
public class ImageScaleServlet extends HttpServlet
{
  private static final String DEFAULT_HOSTNAME = "localhost";
  private static final String SERVLET_PATH = "/documents/";
  private static final String IMAGE_MAX_SIZE = "maxSize";
  private static final double MAX_DIFFERENCE_RATIO = 0.05;

  static long size = 0;

  protected static final Logger log = Logger.getLogger("ImageScaleServlet");

  @Override
  public String getServletInfo()
  {
    return "ImageScale Servlet 1.0";
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    try
    {
      long millis = System.currentTimeMillis();
      RequestedImage reqImage = parseRequest(request);

      //Browser cache
      if (reqImage == null)
        writeServletInfo(response);
      else
      {
        if (reqImage.isValidEtag() || reqImage.isNotModified())
        {
          response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
          return;
        }

        //If has no contentId get it from document manager.
        if (!reqImage.hasContentId())
          loadContentId(reqImage);

        if (!readFromCache(response, reqImage))
          transformImage(response, reqImage);
        else
          log.log(Level.INFO, "ImageCache hit! {0} ((Memory usage: {1})",
            new Object[]{reqImage.getIdentifier(), getMemoryUsage()});
      }
      log.log(Level.INFO, "Image {0} processed in {1} ms (Memory usage: {2})",
        new Object[]{reqImage.getIdentifier(), (System.currentTimeMillis() - millis), getMemoryUsage()});
    }
    catch(Exception ex)
    {
      String message = ex.getMessage();
      if (message == null)
        message = ex.getClass().toString();

      log.log(Level.INFO, "Image not properly processed: {0} (Memory usage: {1})",
        new Object[]{message, getMemoryUsage()});
      if (response != null && !response.isCommitted())
        response.sendError(response.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private RequestedImage parseRequest(HttpServletRequest request)
  {
    RequestedImage reqImage = new RequestedImage();

    //Parameters
    int maxSize = 0;
    String imageMaxSize = getServletConfig().getInitParameter(IMAGE_MAX_SIZE);
    if (imageMaxSize != null)
      maxSize = Integer.parseInt(imageMaxSize);

    String width = getParameter(request, "width");
    if (width != null)
      reqImage.setWidth(width, maxSize);

    String height = getParameter(request, "height");
    if (height != null)
      reqImage.setHeight(height, maxSize);

    String crop = getParameter(request, "crop", "middle_center");
    if (crop != null)
    {
      if (!"auto".equals(crop) && !"middle_center".equals(crop))
      {
        crop = "middle_center";
      }
      reqImage.setCropMode(crop);
    }

    String contextPath = request.getContextPath();
    String servletPath = request.getServletPath();
    String uri = request.getRequestURI();
    String ref = uri.substring((contextPath + servletPath).length() + 1);
    if (ref.length() != 0)
    {
      int index = ref.indexOf("/");
      String identifier = null;
      if (index == -1) // no filename specified
        identifier = ref;
      else
      {
        identifier = ref.substring(0, index);
        reqImage.setFilename(ref.substring(index + 1));
      }

      reqImage.setIdentifier(identifier);
    }

    //ETag
    reqImage.setModifiedSince(request.getDateHeader("If-Modified-Since"));
    reqImage.setEtag(request.getHeader("If-None-Match"));

    return reqImage;
  }

  private void loadContentId(RequestedImage reqImage)
  {
    DocumentManagerClient client = new DocumentManagerClient();
    Document doc =
            client.loadDocument(reqImage.getIdentifier(), 0, ContentInfo.ID);
    if (doc != null)
    {
      Content content = doc.getContent();
      if (content != null)
        reqImage.setIdentifier(content.getContentId());
    }
  }

  private boolean readFromCache(
    HttpServletResponse response, RequestedImage reqImage) throws IOException
  {
    if (reqImage.isHeightPercent() || reqImage.isWidthPercent())
      return false;

    String identifier = reqImage.getIdentifier();
    if (identifier == null) return false;

    boolean written = false;
    if (reqImage.hasContentId())
    {
      int width = reqImage.getWidth();
      int height = reqImage.getHeight();
      File imageFile = restoreImageFile(reqImage, width, height);
      if (imageFile != null)
      {
        response.setHeader("ETag", imageFile.getName());
        response.setDateHeader("Last-Modified", imageFile.lastModified());
        IOUtils.writeToStream(new FileInputStream(imageFile),
          response.getOutputStream());
        written = true;
      }
    }
    return written;
  }

  private synchronized boolean transformImage(
    HttpServletResponse response, RequestedImage reqImage) throws IOException
  {
    boolean written = false;

    //Call to DocumentServlet
    URL url = reqImage.getUrl();
    HttpURLConnection httpUrlConnection = null;

    httpUrlConnection = (HttpURLConnection)url.openConnection();
    long lastModified =
      httpUrlConnection.getHeaderFieldDate("Last-Modified", 0);
    response.setDateHeader("Last-Modified", lastModified);
    InputStream is = httpUrlConnection.getInputStream();
    String formatName = getFormatName(httpUrlConnection);
    BufferedImage srcImage = null;
    BufferedImage dstImage = null;
    try
    {
      //Read source image
      srcImage = ImageIO.read(is);
      incSize(srcImage);
      log.log(Level.INFO, "Image {0} loaded (Memory usage: {1})",
        new Object[]{reqImage.getIdentifier(), getMemoryUsage()});

      if (reqImage.isHeightPercent() || reqImage.isWidthPercent() ||
        needsTransformation(srcImage.getHeight(), srcImage.getWidth(),
          reqImage.getHeight(), reqImage.getWidth()))
      {
        //Image transformation
        int dstWidth = reqImage.isWidthPercent() ?
          (srcImage.getWidth() * reqImage.getWidth() / 100) : reqImage.getWidth();
        int dstHeight = reqImage.isHeightPercent() ?
          (srcImage.getHeight() * reqImage.getHeight() / 100) : reqImage.getHeight();

        String crop = reqImage.getCropMode();
        if (crop == null)
          dstImage = getScaledInstance(srcImage, dstWidth, dstHeight);
        else if (!crop.equals("auto"))
          dstImage = getSubimage(srcImage, dstWidth, dstHeight, crop);
        else
        {
          Dimension subImageDim = preserveAspectRatio(srcImage.getWidth(),
            srcImage.getHeight(), dstWidth, dstHeight);
          dstImage = getSubimage(srcImage, subImageDim.width,
            subImageDim.height, "middle_center");
          dstImage = getScaledInstance(dstImage, dstWidth, dstHeight);
        }

        incSize(dstImage);
        log.log(Level.INFO, "Image {0} scaled (Memory usage: {1})", new Object[]{reqImage.getIdentifier(), getMemoryUsage()});
        decSize(srcImage);
        srcImage.flush();
        srcImage = null;

        String contentType = httpUrlConnection.getContentType();
        response.setContentType(contentType);
        response.setDateHeader("Expires", System.currentTimeMillis() + (31536000 * 1000));

        //Save to cache
        if (reqImage.hasContentId())
          saveImage(dstImage, reqImage, formatName);

        //Write transformed image to response output stream
        OutputStream out = new ImageIOOutputStream(response.getOutputStream());
        try
        {
          ImageIO.write(dstImage, formatName, out);
        }
        finally
        {
          if (out != null)
            out.close();
        }
      }
      else //Write input as it is
      {
        httpUrlConnection = (HttpURLConnection)url.openConnection();
        lastModified =
          httpUrlConnection.getHeaderFieldDate("Last-Modified", 0);
        response.setDateHeader("Last-Modified", lastModified);
        is = httpUrlConnection.getInputStream();
        //Save to cache
        saveImage(is, reqImage);
        //Write to response from cache
        readFromCache(response, reqImage);

        log.log(Level.INFO, "Image {0} does not need transformation. Not scaled (Memory usage:{1})",
                new Object[]{reqImage.getIdentifier(), getMemoryUsage()});
      }
    }
    finally
    {
      if (srcImage != null)
      {
        decSize(srcImage);
        srcImage.flush();
      }
      srcImage = null;
      if (dstImage != null)
      {
        decSize(dstImage);
        dstImage.flush();
      }
      dstImage = null;
      is.close();
    }

    return written;
  }

  private BufferedImage getScaledInstance(BufferedImage srcImage, int width,
    int height)
  {
    if (width == 0 && height == 0)
      return srcImage;

    if (width == 0)
    {
      float srcAspRatio = (float)srcImage.getWidth()/(float)srcImage.getHeight();
      width = (int) ((float)height * (float) srcAspRatio);
    }
    if (height == 0)
    {
      float srcAspRatio = (float)srcImage.getWidth()/(float)srcImage.getHeight();
      height = (int) ((float)width / (float) srcAspRatio);
    }

    Image img =
      srcImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

    BufferedImage scaledImage =
      new BufferedImage(width, height, srcImage.getType());
    scaledImage.getGraphics().drawImage(img, 0, 0, width, height, null);

    return scaledImage;
  }

  private BufferedImage getSubimage(BufferedImage srcImage, int dstWidth,
    int dstHeight, String cropMode)
  {
    if (dstWidth == 0 && dstHeight == 0)
      return srcImage;

    int srcWidth = srcImage.getWidth();
    int srcHeight = srcImage.getHeight();
    if (dstWidth > srcWidth || dstWidth == 0) dstWidth = srcWidth;
    if (dstHeight > srcHeight || dstHeight == 0) dstHeight = srcHeight;

    Position pos =
      getUpperLeftCorner(cropMode, srcWidth, srcHeight, dstWidth, dstHeight);
    Image img =
      srcImage.getSubimage(pos.x, pos.y, dstWidth, dstHeight);

    BufferedImage bufferedImage =
      new BufferedImage(dstWidth, dstHeight, srcImage.getType());
    bufferedImage.getGraphics().drawImage(img, 0, 0, dstWidth, dstHeight, null);

    return bufferedImage;
  }

  private Position getUpperLeftCorner(String crop, int srcWidth, int srcHeight,
    int dstWidth, int dstHeight)
  {
    //Resolve uper-left corner position
    Position pos = new Position(0, 0);

    String[] position = crop.split("_");
    if (position.length == 2)
    {
      String yPos = position[0];
      String xPos = position[1];

      //Y positioning
      if ("middle".equals(yPos))
        pos.y = (srcHeight - dstHeight) / 2;
      else if ("bottom".equals(yPos))
        pos.y = srcHeight - dstHeight;

      //X positioning
      if ("center".equals(xPos))
        pos.x = (srcWidth - dstWidth) / 2;
      else if ("right".equals(xPos))
        pos.x = srcWidth - dstWidth;
    }

    return pos;
  }

  private Dimension preserveAspectRatio(int srcWidth,
    int srcHeight, int dstWidth, int dstHeight)
  {
    Dimension result = new Dimension();

    float srcAspRatio = (float)srcWidth/(float)srcHeight;
    float dstAspRatio = (float)dstWidth/(float)dstHeight;
    if (srcAspRatio > dstAspRatio)
      result.setSize((int)(srcHeight * dstAspRatio), srcHeight);
    else
      result.setSize(srcWidth, (int)(srcWidth / dstAspRatio));

    return result;
  }

  private String getParameter(HttpServletRequest request, String param, String def)
  {
    String parameter = null;
    String queryString = request.getQueryString();
    if (queryString == null)
      return def;
    try
    {
      URLDecoder.decode(queryString, "UTF-8");
      parameter = request.getParameter(param);
      if (parameter == null || "".equals(parameter))
      {
       return def;
      }
    }
    catch (IllegalArgumentException ex)
    {
      //Querystring not properly encoded
      if (queryString != null)
      {
        String[] params = queryString.split("\\&");
        for (String p : params)
        {
          if (p.contains("="))
          {
            String key = p.split("=")[0];
            if (param.equals(key))
              return p.split("=")[1];
            else
              parameter = def;
          }
        }
      }
    }
    catch (UnsupportedEncodingException ex)
    {
      parameter = request.getParameter(param);
      if (parameter == null || "".equals(parameter))
      {
       return def;
      }
    }

    return parameter;
  }

  private String getParameter(HttpServletRequest request, String param)
  {
    return getParameter(request, param, null);
  }

  private void writeServletInfo(HttpServletResponse response) throws IOException
  {
    PrintWriter writer = response.getWriter();
    writer.print("<html><body><p>");
    writer.print(getServletInfo());
    writer.print("</p></body></html>");
  }

  private String getFormatName(HttpURLConnection httpUrlConnection)
  {
    String contentType = httpUrlConnection.getContentType();
    String formatName = "gif"; //default
    if (contentType != null && contentType.contains("image/"))
      formatName = contentType.substring("image/".length());
    return formatName;
  }

  //Cache Methods
  private void saveImage(BufferedImage image, RequestedImage reqImage, String formatName)
    throws IOException
  {
    String contentId = getCachedFilename(reqImage.getIdentifier(),
      reqImage.getWidth(), reqImage.getHeight(), reqImage.isWidthPercent(),
      reqImage.isHeightPercent(), reqImage.getCropMode());

    if (contentId != null)
    {
      try
      {
        File dir = getImageCacheDir();
        File imageFile = new File(dir, contentId);
        FileOutputStream os = new FileOutputStream(imageFile);
        try
        {
          FileLock lock = os.getChannel().lock();
          try
          {
            OutputStream out = new ImageIOOutputStream(os);
            try
            {
              ImageIO.write(image, formatName, out);
            }
            finally
            {
              if (out != null)
                out.close();
            }
          }
          finally
          {
            if (lock != null && lock.isValid())
              lock.release();
          }
        }
        finally
        {
          os.close();
        }
      }
      catch (IOException ex)
      {
        log.log(Level.SEVERE, ex.getMessage());
      }
    }
  }

  private void saveImage(InputStream is, RequestedImage reqImage) throws IOException
  {
    String contentId = getCachedFilename(reqImage.getIdentifier(),
      reqImage.getWidth(), reqImage.getHeight(), reqImage.isWidthPercent(),
      reqImage.isHeightPercent(), reqImage.getCropMode());

    if (contentId != null)
    {
      try
      {
        File dir = getImageCacheDir();
        File imageFile = new File(dir, contentId);
        FileOutputStream os = new FileOutputStream(imageFile);
        try
        {
          FileLock lock = os.getChannel().lock();
          try
          {
            IOUtils.writeToStream(is, os);
          }
          finally
          {
            if (lock != null && lock.isValid())
              lock.release();
          }
        }
        finally
        {
          os.close();
        }
      }
      catch (IOException ex)
      {
        log.log(Level.SEVERE, ex.getMessage());
      }
    }
  }

  private boolean needsTransformation(int srcHeight, int srcWidth,
    int dstHeight, int dstWidth)
  {
    int maxHeightDifference = (int)(dstHeight * MAX_DIFFERENCE_RATIO);
    int maxWidthDifference = (int)(dstWidth * MAX_DIFFERENCE_RATIO);

    return !(srcHeight <= dstHeight + maxHeightDifference
      && srcHeight >= dstHeight - maxHeightDifference
      && srcWidth <= dstWidth + maxWidthDifference
      && srcWidth >= dstWidth - maxWidthDifference);
  }

  private File restoreImageFile(RequestedImage reqImage, int width, int height)
  {
    String filename = getCachedFilename(reqImage.getIdentifier(),
      width, height, reqImage.isWidthPercent(), reqImage.isHeightPercent(), reqImage.getCropMode());
    File dir = getImageCacheDir();
    File imageFile = new File(dir, filename);
    if (imageFile.exists())
      return imageFile;

    return null;
  }

  static String getCachedFilename(String identifier, int width, int height,
    boolean isWidthPercent, boolean isHeightPercent, String cropMode)
  {
    String w = width + (isWidthPercent ? "pc" : "px");
    String h = height + (isHeightPercent? "pc" : "px");
    String crop = cropMode != null ? "_" + cropMode : "";
    return identifier + "_" + w + "_" + h + crop;
  }

  private File getImageCacheDir()
  {
    String userDir = System.getProperty("user.home");
    File cacheDir = new File(userDir, ".imgcache");
    if (!cacheDir.exists())
    {
      cacheDir.mkdir();
    }
    return cacheDir;
  }

  class Position
  {
    int x;
    int y;

    Position(int x, int y)
    {
      this.x = x;
      this.y = y;
    }
  }

  class RequestedImage
  {
    private String identifier;
    private String filename;
    private int width;
    private int height;
    private String cropMode;
    private boolean widthPercent;
    private boolean heightPercent;
    private String etag;
    private long modifiedSince;

    public String getCropMode()
    {
      return cropMode;
    }

    public void setCropMode(String cropMode)
    {
      this.cropMode = cropMode;
    }

    public String getFilename()
    {
      return filename;
    }

    public void setFilename(String filename)
    {
      this.filename = filename;
    }

    public String getIdentifier()
    {
      return identifier;
    }

    public void setIdentifier(String identifier)
    {
      this.identifier = identifier;
    }

    public int getHeight()
    {
      return height;
    }

    public void setHeight(int height)
    {
      this.height = height;
    }

    public int getWidth()
    {
      return width;
    }

    public void setWidth(int width)
    {
      this.width = width;
    }

    public void setWidth(String value, int maxSize)
    {
      if (value != null)
      {
        if (value.endsWith("%"))
        {
          value = value.substring(0, value.length() - 1);
          setWidthPercent(true);
        }
        else if (value.endsWith("px"))
        {
          value = value.substring(0, value.length() - 2);
        }

        try
        {
          int w = Integer.parseInt(value);
          if (maxSize == 0 || w < maxSize)
            setWidth(w);
          else
            setWidth(maxSize);
        }
        catch (NumberFormatException ex)
        {
          log.log(Level.INFO, "Invalid width format: {0}", ex.getMessage());
        }
      }
    }

    public void setHeight(String value, int maxSize)
    {
      if (value != null)
      {
        if (value.endsWith("%"))
        {
          value = value.substring(0, value.length() - 1);
          setHeightPercent(true);
        }
        else if (value.endsWith("px"))
        {
          value = value.substring(0, value.length() - 2);
        }

        try
        {
          int w = Integer.parseInt(value);
          if (maxSize == 0 || w < maxSize)
            setHeight(w);
          else
            setHeight(maxSize);
        }
        catch (NumberFormatException ex)
        {
          log.log(Level.INFO, "Invalid height format: {0}", ex.getMessage());
        }
      }
    }

    public boolean isWidthPercent()
    {
      return widthPercent;
    }

    public void setWidthPercent(boolean widthPercent)
    {
      this.widthPercent = widthPercent;
    }

    public boolean isHeightPercent()
    {
      return heightPercent;
    }

    public void setHeightPercent(boolean heightPercent)
    {
      this.heightPercent = heightPercent;
    }

    public String getEtag()
    {
      return etag;
    }

    public void setEtag(String etag)
    {
      this.etag = etag;
    }


    public URL getUrl() throws MalformedURLException
    {
      String uri = MatrixConfig.getProperty("contextPath") +
        SERVLET_PATH + identifier + (filename != null ? "/" + filename : "");

      return new URL("http", DEFAULT_HOSTNAME, uri);
    }

    public boolean hasContentId()
    {
      return identifier != null && Utilities.isUUID(identifier);
    }

    public String getCachedFilename()
    {
      String w = width + (isWidthPercent() ? "pc" : "px");
      String h = height + (isHeightPercent()? "pc" : "px");
      String crop = cropMode != null ? "_" + cropMode : "";
      return identifier + "_" + w + "_" + h + crop;
    }

    public boolean isValidEtag()
    {
      return etag != null && etag.equals(getCachedFilename());
    }

    public boolean isNotModified()
    {
      return getModifiedSince() >= 0;
    }

    private void setModifiedSince(long modifiedSince)
    {
      this.modifiedSince = modifiedSince;
    }

    private long getModifiedSince()
    {
      return modifiedSince;
    }

  }

  //Memory size methods
  private synchronized void incSize(BufferedImage image)
  {
    if (image != null)
      size = size + (image.getHeight() * image.getWidth() * 4);
  }

  private synchronized void decSize(BufferedImage image)
  {
    if (image != null)
    {
      size = size - (image.getHeight() * image.getWidth() * 4);
      if (size < 0) size = 0;
    }
  }

  private String getMemoryUsage()
  {
    return DocumentUtils.getSizeString(size);
  }

  /**
   * An OutputStream which can be used to write Images
   * with the ImageIO in servlets.
   */
  public class ImageIOOutputStream extends OutputStream
  {
    private OutputStream out;
    private volatile boolean isActive = true;

    public ImageIOOutputStream(OutputStream out)
    {
        this.out = out;
    }

    @Override
    public void close() throws IOException
    {
      if (isActive)
      {
        isActive = false; // deactivate
        try
        {
          out.close();
        }
        finally
        {
          out = null;
        }
      }
    }

    @Override
    public void flush() throws IOException
    {
      if(isActive)
      {
        out.flush();
      }
      // otherwise do nothing (prevent polluting the stream)
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
      if (isActive)
        out.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
      if (isActive)
        out.write(b);
    }

    @Override
    public void write(int b) throws IOException
    {
      if (isActive)
        out.write(b);
    }
  }
}
