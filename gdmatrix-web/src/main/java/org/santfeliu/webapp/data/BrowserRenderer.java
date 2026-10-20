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
package org.santfeliu.webapp.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.santfeliu.faces.Translator;
import org.santfeliu.util.net.HttpClient;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;

public class BrowserRenderer
{

  public static final Logger LOGGER = Logger.getLogger("BrowserRenderer");
  private static final int DEFAULT_READ_TIMEOUT = 60000;

  public String render(Map<String, Object> params, Locale locale)
  {
    try
    {
      String url = (String) params.get("url");
      if (url == null || url.isEmpty())
      {
        return "<div class=\"error\">INVALID_BROWSER_URL</div>";
      }

      String port = getStringParam(params, "port", "80");
      boolean iframe = getBooleanParam(params, "iframe", false);
      String width = (String) params.get("width");
      String height = (String) params.get("height");
      Integer readTimeout = getIntParam(params, "readTimeout");
      Translator translator = (Translator) params.get("translator");
      String translationGroup = (String) params.get("translationGroup");
      String allowedHtmlTags = (String) params.get("allowedHtmlTags");

      StringBuilder html = new StringBuilder();

      if (iframe)
      {
        LOGGER.log(Level.INFO, "IFRAME URL: {0}", url);
        html.append("<iframe src=\"").append(url).append("\"");
        if (width != null)
        {
          html.append(" width=\"").append(width).append("\"");
        }
        if (height != null)
        {
          html.append(" height=\"").append(height).append("\"");
        }
        html.append(">");
        html.append("<a href=\"").append(url).append("\" target=\"_blank\">")
          .append(url).append("</a>");
        html.append("</iframe>");
      }
      else
      {
        if (url.startsWith("/"))
        {
          url = "http://localhost" + url;
        }
        String userLanguage = locale != null ? locale.getLanguage() : "es";
        LOGGER.log(Level.INFO, "EMBED URL: {0}, Language: {1}", new Object[]
        {
          url, userLanguage
        });

        HttpClient httpClient = new HttpClient();
        httpClient.setURL(url);
        httpClient.setForceHttp(true);
        httpClient.setHttpPort(Integer.parseInt(port));
        httpClient.setDownloadContentType("text/");
        httpClient.setMaxContentLength(524288);
        httpClient.setRequestProperty("User-Agent", HttpClient.USER_AGENT_IE6);
        httpClient.setRequestProperty("Accept-Charset", "utf-8");
        httpClient.setRequestProperty("Accept-Language", userLanguage);
        httpClient.setReadTimeout(readTimeout != null ? readTimeout : DEFAULT_READ_TIMEOUT);
        httpClient.connect();

        String charset = httpClient.getContentEncoding();
        if (charset == null)
        {
          charset = Charset.defaultCharset().name();
        }

        InputStream input;
        if (translator != null)
        {
          Reader in = new InputStreamReader(httpClient.getContentInputStream(), charset);
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          Writer out = new OutputStreamWriter(bos, charset);
          translator.translate(in, out, "text/html", userLanguage, translationGroup);
          input = new ByteArrayInputStream(bos.toByteArray());
        }
        else
        {
          input = httpClient.getContentInputStream();
        }

        Tidy tidy = new Tidy();
        boolean hasAllowedTags = allowedHtmlTags != null;
        boolean isHeadAllowed = hasAllowedTags && allowedHtmlTags.contains("head");

        if (hasAllowedTags)
        {
          if (isHeadAllowed)
          {
            tidy.setPrintBodyOnly(true);
          }
          Properties props = new Properties();
          props.put("new-blocklevel-tags", allowedHtmlTags);
          tidy.setConfigurationFromProps(props);
        }

        tidy.setInputEncoding(charset);
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PrintWriter errorWriter = new PrintWriter(errorStream, true);
        tidy.setErrout(errorWriter);
        errorWriter.flush();

        Document domDocument = tidy.parseDOM(input, null);

        StringWriter outputWriter = new StringWriter();
        writeDocumentBody(domDocument, outputWriter, isHeadAllowed, url);
        html.append(outputWriter.toString());
      }

      return html.toString();

    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, ex.toString());
      return "<div class=\"error\">CAN_NOT_SHOW_DOCUMENT</div>";
    }
  }

  private void writeDocumentBody(Document document, Writer writer, 
    boolean isHeadAllowed, String baseUrl) throws IOException
  {
    Node htmlNode = findNode(document.getFirstChild(), "html");
    if (htmlNode == null)
    {
      throw new IOException("Invalid html document.");
    }

    if (isHeadAllowed)
    {
      Node head = findNode(htmlNode.getFirstChild(), "head");
      if (head != null)
      {
        Node node = head.getFirstChild();
        while (node != null)
        {
          writeNode(node, writer, null, baseUrl);
          node = node.getNextSibling();
        }
      }
    }

    Node body = findNode(htmlNode.getFirstChild(), "body");
    if (body == null)
    {
      throw new IOException("Invalid html document.");
    }

    Node node = body.getFirstChild();
    while (node != null)
    {
      writeNode(node, writer, null, baseUrl);
      node = node.getNextSibling();
    }
  }

  private Node findNode(Node node, String nodeName)
  {
    Node foundNode = null;
    Node currentNode = node;
    while (foundNode == null && currentNode != null)
    {
      String name = currentNode.getNodeName();
      if (nodeName.equalsIgnoreCase(name))
      {
        foundNode = currentNode;
      }
      else
      {
        currentNode = currentNode.getNextSibling();
      }
    }
    return foundNode;
  }

  private void writeNode(Node node, Writer writer, String actionURL, 
    String baseUrl) throws IOException
  {
    if (node instanceof Text)
    {
      writer.write(node.getNodeValue());
    }
    else if (node instanceof Comment)
    {
      writer.write("<!-- " + node.getNodeValue() + " -->");
    }
    else if (node instanceof Element)
    {
      Element element = (Element) node;
      String tag = element.getNodeName().toLowerCase();

      if (tag.equals("form"))
      {
        actionURL = element.getAttribute("action");
        writeChildren(node, writer, actionURL, baseUrl);
        return;
      }
      else if (tag.equals("img"))
      {
        String src = element.getAttribute("src");
        element.setAttribute("src", getAbsoluteHRef(src, baseUrl));
      }
      else if (tag.equals("a"))
      {
        String href = element.getAttribute("href");
        if (href != null && !href.startsWith("javascript:") && 
          !href.startsWith("mailto:") && !href.equals("#"))
        {
          element.setAttribute("href", getAbsoluteHRef(href, baseUrl));
          element.setAttribute("target", "_blank");
        }
      }

      writer.write("<" + tag);
      writeAttributes(element, writer);
      if (isSelfClosing(tag))
      {
        writer.write(" />");
      }
      else
      {
        writer.write(">");
        writeChildren(node, writer, actionURL, baseUrl);
        writer.write("</" + tag + ">");
      }
    }
  }

  private void writeChildren(Node node, Writer writer, String actionURL, 
    String baseUrl) throws IOException
  {
    Node child = node.getFirstChild();
    while (child != null)
    {
      writeNode(child, writer, actionURL, baseUrl);
      child = child.getNextSibling();
    }
  }

  private void writeAttributes(Element element, Writer writer) 
    throws IOException
  {
    NamedNodeMap map = element.getAttributes();
    for (int i = 0; i < map.getLength(); i++)
    {
      Node attribute = map.item(i);
      String name = attribute.getNodeName();
      String value = attribute.getNodeValue();
      writer.write(" " + name + "=\"" + (value != null ? 
        value.replace("\"", "&quot;") : "") + "\"");
    }
  }

  private boolean isSelfClosing(String tag)
  {
    return tag.equals("img") || tag.equals("br") || tag.equals("hr") 
      || tag.equals("input");
  }

  private String getAbsoluteHRef(String href, String baseUrl)
  {
    if (href == null)
    {
      return "";
    }
    if (href.startsWith("#") || href.startsWith("/"))
    {
      return href;
    }
    try
    {
      URL u = new URL(href);
      return u.toString();
    }
    catch (MalformedURLException e)
    {
      try
      {
        URL base = new URL(baseUrl);
        URL absolute = new URL(base, href);
        return absolute.toString();
      }
      catch (MalformedURLException ex)
      {
        return href;
      }
    }
  }

  private String getStringParam(Map<String, Object> params, String key, String defaultValue)
  {
    Object val = params.get(key);
    return val != null ? val.toString() : defaultValue;
  }

  private Integer getIntParam(Map<String, Object> params, String key)
  {
    Object val = params.get(key);
    if (val instanceof Number)
    {
      return ((Number) val).intValue();
    }
    try
    {
      if (val != null)
      {
        return Integer.parseInt(val.toString());
      }
    }
    catch (Exception ignored)
    {
    }
    return null;
  }

  private boolean getBooleanParam(Map<String, Object> params, String key, boolean defaultValue)
  {
    Object val = params.get(key);
    if (val instanceof Boolean)
    {
      return (Boolean) val;
    }
    if (val != null)
    {
      return Boolean.parseBoolean(val.toString());
    }
    return defaultValue;
  }
}
