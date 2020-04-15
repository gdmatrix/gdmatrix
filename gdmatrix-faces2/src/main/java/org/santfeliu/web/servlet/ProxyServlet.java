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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.IOUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.servlet.proxy.ProxyAction;
import org.santfeliu.web.servlet.proxy.ProxyConfig;
import org.santfeliu.web.servlet.proxy.ProxyRule;

/**
 *
 * @author realor
 */
public class ProxyServlet extends HttpServlet
{
  public static final String URL_PARAM = "url";
  public static final String CACHE_GROUP_PARAM = "_CHG";
  public static final String CACHE_REFRESH_PARAM = "_CHR";
  public static final String CACHE_FORMAT_PARAM = "_CHF";
  public static final String MIME_SUFFIX = ".mime";
  public static final String DATA_SUFFIX = ".data";
  public static final String CACHE_DIR = ".proxycache";
  public static File cacheDir;
  private static final int BUFFER_SIZE = 4096;
  private int cacheRequestCount;
  private int cacheHitCount;
  private final ProxyConfig proxyConfig = new ProxyConfig();
  private static final Logger LOGGER = Logger.getLogger("ProxyServlet");

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    String surl = req.getParameter(URL_PARAM);
    if (surl != null)
    {
      surl = proxyConfig.getActualURL(surl);
      String cacheGroup = req.getParameter(CACHE_GROUP_PARAM);
      String cacheRefresh = req.getParameter(CACHE_REFRESH_PARAM);
      String cacheFormat = req.getParameter(CACHE_FORMAT_PARAM);
      Enumeration names = req.getParameterNames();
      ArrayList<String> parameterList = new ArrayList<String>();
      while (names.hasMoreElements())
      {
        String name = (String)names.nextElement();
        if (!name.equals(URL_PARAM) && 
            !name.equals(CACHE_GROUP_PARAM) &&
            !name.equals(CACHE_REFRESH_PARAM) &&
            !name.equals(CACHE_FORMAT_PARAM) &&
            !name.equals(cacheRefresh))
        {
          String value = req.getParameter(name);
          name = URLEncoder.encode(name, "UTF-8");
          value = URLEncoder.encode(value, "UTF-8");
          parameterList.add(name + "=" + value);
        }
      }
      Collections.sort(parameterList);
      StringBuilder parameters = new StringBuilder();
      Iterator<String> iter = parameterList.iterator();
      if (iter.hasNext())
      {
        parameters.append("?");
        parameters.append(iter.next());
        while (iter.hasNext())
        {
          parameters.append("&");
          parameters.append(iter.next());
        }
      }
      // URL encode only whitespaces
      surl = surl.replace(" ", "+");
      surl = surl + parameters.toString();
      LOGGER.log(Level.FINE, "GET {0}", surl);

      if (cacheGroup == null)
      {
        sendResponse(surl, req, resp);
      }
      else
      {
        String refreshValue = req.getParameter(cacheRefresh);
        String refreshParam = refreshValue == null ?
          null : "&" + cacheRefresh + "=" + refreshValue;
        sendResponseFromCache(surl, 
          cacheFormat, cacheGroup, refreshParam, req, resp);
      }
    }
    else
    {
      printInfo(resp);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    String surl = req.getParameter(URL_PARAM);
    if (surl != null)
    {
      surl = proxyConfig.getActualURL(surl);
      URL url = new URL(surl);
      LOGGER.log(Level.FINE, "POST {0}", surl);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      prepareConnection(conn, req);
      conn.setDoInput(true);
      conn.setDoOutput(true);
      // set request properties
      Enumeration headerNames = req.getHeaderNames();
      while (headerNames.hasMoreElements())
      {
        String name = (String)headerNames.nextElement();
        String value = req.getHeader(name);
        conn.setRequestProperty(name, value);
      }
      // write request message
      byte[] buffer = new byte[BUFFER_SIZE];
      InputStream ris = req.getInputStream();
      try
      {
        OutputStream cos = conn.getOutputStream();
        try
        {
          int nr = ris.read(buffer);
          while (nr > 0)
          {
            cos.write(buffer, 0, nr);
            nr = ris.read(buffer);
          }
        }
        finally
        {
          cos.close();
        }
      }
      finally
      {
        ris.close();
      }
      if (!connect(conn, resp)) return;

      // read response
      String contentType = conn.getContentType();
      int contentLength = conn.getContentLength();
      String contentEncoding = conn.getContentEncoding();

      resp.setContentType(contentType);
      resp.setContentLength(contentLength);
      if (contentEncoding != null)
      {
        resp.setHeader("Content-Encoding", contentEncoding);
      }

      InputStream cis = conn.getInputStream();
      try
      {
        OutputStream ros = resp.getOutputStream();
        try
        {
          int nr = cis.read(buffer);
          while (nr > 0)
          {
            ros.write(buffer, 0, nr);
            nr = cis.read(buffer);
          }
        }
        finally
        {
          ros.close();
        }
      }
      finally
      {
        cis.close();
      }
    }
    else
    {
      printInfo(resp);
    }
  }

  private void sendResponse(String surl, 
    HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    URL url = new URL(surl);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    prepareConnection(conn, req);
    if (!connect(conn, resp)) return;

    // read response
    String contentDisposition = conn.getHeaderField("Content-Disposition");
    if (contentDisposition != null)
      resp.setHeader("Content-Disposition", contentDisposition);
    int contentLength = conn.getContentLength();
    resp.setContentLength(contentLength);
    String contentType = updateContentType(conn.getContentType());
    resp.setContentType(contentType);
    long expiration = conn.getExpiration();
    resp.setDateHeader("Expires", expiration);
    IOUtils.writeToStream(conn.getInputStream(), resp.getOutputStream());
  }

  private void sendResponseFromCache(String surl, String cacheFormat,
    String cacheGroup, String refreshParam, 
    HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    String hashValue = calcHash(surl);
    int contentLength;
    String contentType;
    cacheRequestCount++;

    long lastModified = 0;
    
    InputStream dataStream;
    File cacheGroupDir = getCacheGroupDir(cacheGroup);
    File mimeFile = new File(cacheGroupDir, hashValue + MIME_SUFFIX);
    File dataFile = new File(cacheGroupDir, hashValue + DATA_SUFFIX);
    if (mimeFile.exists() && dataFile.exists() && refreshParam == null)
    {
      cacheHitCount++;
      long since = req.getDateHeader("If-Modified-Since");
      if (dataFile.lastModified() / 1000 <= since / 1000)
      {
        // client has a valid version      
        resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return;
      }
      // get dataStream from cache
      BufferedReader reader = new BufferedReader(new FileReader(mimeFile));
      try { contentType = reader.readLine();} finally { reader.close(); }
      contentLength = (int)dataFile.length();
      dataStream = new FileInputStream(dataFile);
      lastModified = dataFile.lastModified();
    }
    else
    {
      // read dataStream from connection
      URL url = refreshParam == null ?
        new URL(surl) : new URL(surl + refreshParam);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      prepareConnection(conn, req);
      if (!connect(conn, resp)) return;

      // read response
      contentLength = conn.getContentLength();
      contentType = updateContentType(conn.getContentType());
      dataStream = conn.getInputStream();

      if (acceptedFormat(contentType, cacheFormat))
      {
        // Create or update cache files

        // write mime file
        File tempMimeFile = File.createTempFile("temp", MIME_SUFFIX);
        FileWriter writer = new FileWriter(tempMimeFile);
        try { writer.write(contentType); } finally { writer.close(); }
        if (mimeFile.exists()) mimeFile.delete();
        tempMimeFile.renameTo(mimeFile);

        // white data file
        File tempDataFile = File.createTempFile("temp", DATA_SUFFIX);
        IOUtils.writeToStream(dataStream, new FileOutputStream(tempDataFile));
        if (dataFile.exists()) dataFile.delete();
        tempDataFile.renameTo(dataFile);

        dataStream = new FileInputStream(dataFile);
        lastModified = dataFile.lastModified();
      }
    }
    // write to response
    resp.setContentLength(contentLength);
    resp.setContentType(contentType);
    resp.setDateHeader("Last-Modified", lastModified);
    resp.setDateHeader("Expires", System.currentTimeMillis() + 5000);
    IOUtils.writeToStream(dataStream, resp.getOutputStream());
  }

  private void printInfo(HttpServletResponse resp) throws IOException
  {
    resp.setContentType("text/html");
    PrintWriter writer = resp.getWriter();
    try
    {
      writer.println("<?xml version='1.0' encoding='UTF-8'?>");
      writer.println("<html><head><title>ProxyServlet</title>");
      writer.println("<style>body {font-family:Arial; font-size:14px;}</style>");
      writer.println("</head><body>");
      writer.println("<h1>ProxyServlet</h1>");
      writer.println("<p>Config file: " + 
        proxyConfig.getProxyFile().getAbsolutePath() + "</p>");
      writer.println("<p>Cache groups:</p>");
      writer.println("<ul>");
      File baseDir = getCacheDir();
      File[] dirs = baseDir.listFiles();
      for (int i = 0; i < dirs.length; i++)
      {
        File dir = dirs[i];
        String name = dir.getName();
        int size = dir.listFiles(new FileFilter()
        {
          @Override
          public boolean accept(File file)
          {
            return file.getName().endsWith(DATA_SUFFIX);
          }
        }).length;
        writer.println("<li>" + name + " (" + size + ")</li>");
      }
      writer.println("</ul>");
      int cacheFailCount = cacheRequestCount - cacheHitCount;
      double cacheHitPerc = (100.0 * cacheHitCount) / cacheRequestCount;
      double cacheFailPerc = (100.0 * cacheFailCount) / cacheRequestCount;
      DecimalFormat df = new DecimalFormat("#0.00");
      writer.println("<p>Cache statistics:</p>");
      writer.println("<ul>");
      writer.println("<li>Request count: " + cacheRequestCount + "</li>");
      writer.println("<li>Hit count: " + cacheHitCount +
        " (" + df.format(cacheHitPerc) + "%)</li>");
      writer.println("<li>Cache fail count: " + cacheFailCount +
        " (" + df.format(cacheFailPerc) + "%)</li>");
      writer.println("</ul>");
      writer.println("</body>");
      writer.println("</html>");
    }
    finally
    {
      writer.close();
    }
  }

  private String calcHash(String surl)
  {
    try
    {
      MessageDigest digester = MessageDigest.getInstance("SHA-1");
      digester.reset();
      digester.update(surl.getBytes());
      StringBuilder sb = new StringBuilder();
      byte[] value = digester.digest();
      for (byte b : value)
      {
        sb.append(String.format("%02X", b));
      }
      return sb.toString();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private File getCacheDir()
  {
    if (cacheDir == null)
    {
      String userHomeDir = System.getProperty("user.home");
      cacheDir = new File(userHomeDir + "/" + CACHE_DIR);
    }
    return cacheDir;
  }

  private File getCacheGroupDir(String cacheGroup)
  {
    cacheGroup = cacheGroup.toUpperCase();
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < cacheGroup.length() && buffer.length() < 64; i++)
    {
      char ch = cacheGroup.charAt(i);
      if ((ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9'))
        buffer.append(ch);
    }
    if (buffer.length() == 0) buffer.append("DEFAULT");
    File cacheGroupDir = new File(getCacheDir(), buffer.toString());
    if (!cacheGroupDir.exists()) cacheGroupDir.mkdirs();
    return cacheGroupDir;
  }

  private String updateContentType(String contentType)
  {
    if (contentType == null) contentType = "application/octet-stream";
    else if (contentType.startsWith("application/vnd.ogc.se_xml"))
      contentType = "text/xml";
    return contentType;
  }

  private boolean acceptedFormat(String contentType, String cacheFormat)
  {
    return cacheFormat == null || contentType.startsWith(cacheFormat);
  }

  private void prepareConnection(HttpURLConnection conn, 
    HttpServletRequest req) throws IOException
  {
    URL url = conn.getURL();
    String host = url.getHost();
    List<ProxyRule> rules = proxyConfig.getRules(host);
    if (rules != null)
    {
      Credentials credentials = SecurityUtils.getCredentials(req, false);
      if (credentials == null)
      {
        credentials = UserSessionBean.getCredentials(req);
      }
      User user = UserCache.getUser(credentials);
      boolean done = false;
      Iterator<ProxyRule> iter = rules.iterator();
      while (!done && iter.hasNext())
      {
        ProxyRule rule = iter.next();
        if (rule.evaluate(conn, req, user))
        {
          List<ProxyAction> actions = rule.getActions();
          for (ProxyAction action : actions)
          {
            action.execute(conn, req, user);
          }
          if (rule.isStop()) done = true;
        }
      }
    }
  }

  private boolean connect(HttpURLConnection conn, HttpServletResponse resp)
    throws IOException
  {
    conn.connect();
    int status = conn.getResponseCode();
    if (status != -1)
    {
      resp.setStatus(status);
    }
    return status == HttpServletResponse.SC_OK;
  }
}
