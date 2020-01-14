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
package org.santfeliu.util.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;


/**
 *
 * @author real
 */
public class HttpClient
{
  public static final String USER_AGENT_IE6 = 
    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;" +
    " .NET CLR 1.1.4322; InfoPath.1; HtmlBrowser)";

  public static final String USER_AGENT_IE7 = 
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1;" +
    " .NET CLR 1.1.4322; InfoPath.1; HtmlBrowser)";
  
  public static final String USER_AGENT_FIREFOX2 = 
    "Mozilla/5.0 (Windows; U; Windows NT 5.1; es-ES; HtmlBrowser) " +
    "Gecko/20071127 Firefox/2.0.0.11";
  
  private String url;
  private String lastUrl;
  private boolean forceHttp = true;
  private int httpPort = 80;
  private int connectTimeout = 10000;
  private int readTimeout = 30000;
  private int maxRedirects = 5;
  private String downloadContentType = "text/";
  private long maxContentLength = 524288; // 512 kb
  private Map<String, String> requestProperties = new HashMap();
  private Map<String, List<String>> headerProperties = new HashMap();
  private byte[] content;
  
  public void setURL(String url)
  {
    this.url = url;
  }
  
  public String getURL()
  {
    return url;
  }
  
  public String getLastURL()
  {
    return lastUrl;
  }

  public int getMaxRedirects()
  {
    return maxRedirects;
  }

  public void setMaxRedirects(int maxRedirects)
  {
    this.maxRedirects = maxRedirects;
  }

  public boolean isForceHttp()
  {
    return forceHttp;
  }

  public void setForceHttp(boolean forceHttp)
  {
    this.forceHttp = forceHttp;
  }

  public int getHttpPort()
  {
    return httpPort;
  }

  public void setHttpPort(int httpPort)
  {
    this.httpPort = httpPort;
  }

  public int getConnectTimeout()
  {
    return connectTimeout;
  }

  public void setConnectTimeout(int connectTimeout)
  {
    this.connectTimeout = connectTimeout;
  }

  public int getReadTimeout()
  {
    return readTimeout;
  }

  public void setReadTimeout(int readTimeout)
  {
    this.readTimeout = readTimeout;
  }

  public long getMaxContentLength()
  {
    return maxContentLength;
  }

  public void setMaxContentLength(long maxContentLength)
  {
    this.maxContentLength = maxContentLength;
  }
  
  public String getDownloadContentType()
  {
    return downloadContentType;
  }

  public void setDownloadContentType(String downloadContentType)
  {
    this.downloadContentType = downloadContentType;
  }
  
  public Map getRequestProperties()
  {
    return requestProperties;
  }
  
  public void setRequestProperty(String property, String value)
  {
    requestProperties.put(property, value);
  }

  public String getRequestProperty(String property)
  {
    return requestProperties.get(property);
  }

  public Map<String, List<String>> getHeaderProperties()
  {
    return headerProperties;
  }
  
  public String getHeaderProperty(String property)
  {
    List<String> list = headerProperties.get(property);
    if (list != null && list.size() > 0) return list.get(0);
    return null;
  }
  
  public List<String> getHeaderPropertyValues(String property)
  {
    return headerProperties.get(property);
  }
  
  public void doGet() throws Exception
  {
    URL connURL; // url to connect
    URLConnection conn; // connection
    String location = url; // redirect uri
    int numRedirects = 0;
    do
    {
      lastUrl = location;
      connURL = new URL(location);

      if (isForceHttp() && !"http".equals(connURL.getProtocol()))
      {
        // if protocol not http, connect with http protocol and port 80
        // to avoid other protocols like https, FTP, etc.
        connURL = new URL("http", connURL.getHost(),
                          httpPort, connURL.getFile());
      }
      conn = connURL.openConnection();
      conn.setConnectTimeout(connectTimeout);
      conn.setReadTimeout(readTimeout);

      if (conn instanceof HttpURLConnection)
      {
        ((HttpURLConnection)conn).setInstanceFollowRedirects(false);
      }
      setRequestProperties(conn);
      location = conn.getHeaderField("Location"); 
      if (location != null) numRedirects++; // It's a redirect.
    } while (location != null && numRedirects < maxRedirects);

    if (location != null)
      throw new IOException("Can't redirect to: " + location);
    // read response
    readResponse(conn);
  }
  
  public void doPost(byte[] dataToPost) throws Exception
  {
    URL connURL = new URL(url);
    URLConnection conn = connURL.openConnection();
    conn.setConnectTimeout(connectTimeout);
    conn.setReadTimeout(readTimeout);   
    setRequestProperties(conn);
    conn.setDoInput(true);
    conn.setDoOutput(true);
    
    // post request data
    OutputStream os = conn.getOutputStream();
    os.write(dataToPost);
    os.flush();
    
    // read response
    readResponse(conn);
  }
  
  public void connect() throws Exception
  {
    doGet();
  }
  
  public long getContentLength()
  {
    return content == null ? 0 : content.length;
  }
  
  public String getContentType()
  {
    return getHeaderProperty("Content-Type");
  }

  public String getContentLanguage()
  {
    return getHeaderProperty("Content-Language");
  }

  public String getContentEncoding()
  {
    return getHeaderProperty("Content-Encoding");
  }
  
  public byte[] getContent()
  {
    return content;
  }
  
  public InputStream getContentInputStream()
  {
    if (content == null) return null;
    
    return new ByteArrayInputStream(content);
  }
  
  public String getContentAsString()
  {
    if (content == null) return null;

    String contentType = getContentType();
    if (contentType != null && !contentType.startsWith("text/")) return null;
    
    String contentEncoding = getContentEncoding();
    if (contentEncoding == null)
    {
      return new String(content); // user default encoding
    }
    else
    {
      try
      {
        return new String(content, contentEncoding);
      }
      catch (UnsupportedEncodingException ex)
      {
        return new String(content); // user default encoding
      }
    }
  }
  
  public Document getContentAsXML() throws Exception
  {
    return getContentAsXML(false);
  }
  
  public Document getContentAsXML(boolean removeInvalidChars) throws Exception
  {
    if (content == null) return null;

    String contentType = getContentType();
    if (contentType != null && !contentType.startsWith("text/xml")) return null;

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    
    InputStream is = null;
    String contentEncoding = getContentEncoding();
    if (removeInvalidChars && contentEncoding != null)
    {
      String text = new String(content, contentEncoding);
      text = stripNonValidXMLCharacters(text);
      is = new ByteArrayInputStream(text.getBytes(contentEncoding));
    }
    else
    {
      is = new ByteArrayInputStream(content);
    }
    return dbf.newDocumentBuilder().parse(is);
  }

  /* private methods */  
  private void readResponse(URLConnection conn) throws Exception
  {
    // clear content
    content = null;

    // set header properties
    headerProperties.putAll(conn.getHeaderFields());

    // read content
    if (canDownloadContent())
    {
      if (conn instanceof HttpURLConnection)
      {
        HttpURLConnection httpConn = (HttpURLConnection)conn;
        try
        {
          content = readInputStream(conn.getInputStream());
        }
        catch (IOException ex)
        {
          InputStream errorStream = httpConn.getErrorStream();
          if (errorStream != null)
          {
            content = readInputStream(errorStream);
          }
          else throw ex;
        }
        httpConn.disconnect();
      }
      else
      {
        content = readInputStream(conn.getInputStream());
      }
    }
    setHeaderProperties();
  }

  private byte[] readInputStream(InputStream is) throws IOException
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int count = is.read(buffer);
    while (count != -1)
    {
      os.write(buffer, 0, count);
      count = is.read(buffer);
    }
    is.close();    
    return os.toByteArray();
  }

  private boolean canDownloadContent()
  {
    String contentLength = getHeaderProperty("Content-Length");
    if (contentLength != null)
    {
      long length = Long.parseLong(contentLength);
      if (length > maxContentLength) return false;
    }
    
    String contentType = getHeaderProperty("Content-Type");
    if (contentType == null || downloadContentType == null) return true;
    
    if (!contentType.startsWith(downloadContentType)) return false;
    
    return true;
  }
  
  private void setRequestProperties(URLConnection conn)
  {
    conn.setConnectTimeout(getConnectTimeout());
    conn.setReadTimeout(getReadTimeout());
    Set<Map.Entry<String, String>> entries = requestProperties.entrySet();
    for (Map.Entry<String, String> entry : entries)
    {
      conn.setRequestProperty(entry.getKey(), entry.getValue());
    }
  }

  private void setHeaderProperties()
  {
    String contentType = getContentType();
    if (contentType != null && contentType.startsWith("text/"))
    {
      String contentEncoding = getContentEncoding();
      if (contentEncoding == null)
      {
        int index = contentType.indexOf("charset=");
        if (index != -1)
        {
          contentEncoding = contentType.substring(index + 8);
        }
        else if (content != null)
        {
          if (contentType.equals("text/html") || 
             contentType.equals("text/xhtml"))
          {
            // look inside HEAD
            contentEncoding = extractSequence(
              "charset=".getBytes(), (byte)'"', 200);
          }
          else if (contentType.equals("text/xml"))
          {
            // look inside encoding attribute
            contentEncoding = extractSequence(
              "encoding=\"".getBytes(), (byte)'"', 200);
          }
        }
        if (contentEncoding != null)
        {
          ArrayList list = new ArrayList();
          list.add(contentEncoding);
          headerProperties.put("Content-Encoding", list);
        }
      }
    }
  }
  
  public String stripNonValidXMLCharacters(String in)
  {
    StringBuilder out = new StringBuilder();
    char current;
    
    if (in == null || ("".equals(in))) return "";
    int i = 0;
    while (i < in.length())
    {
      current = in.charAt(i);
      if ((current == 0x9) ||
          (current == 0xA) ||
          (current == 0xD) ||
          ((current >= 0x20) && (current <= 0xD7FF)) ||
          ((current >= 0xE000) && (current <= 0xFFFD)) ||
          ((current >= 0x10000) && (current <= 0x10FFFF)))
        out.append(current);
      i++;
    }
    String text = out.toString();
    text = text.replaceAll("&#0;", "");
    System.out.println(text);
    return text;
  }
  
  private String extractSequence(byte[] startToken, byte endByte, int length)
  {
    int i = 0;
    int j = 0;
    while (i < length && i < content.length && j < startToken.length)
    {
      if (content[i] == startToken[j]) j++;
      else j = 0;
      i++;
    }
    if (j == startToken.length)
    {
      StringBuilder buffer = new StringBuilder();
      while (i < length && i < content.length && content[i] != endByte)
      {
        buffer.append((char)content[i]);
        i++;
      }
      if (content[i] == endByte) return buffer.toString();
    }
    return null;
  }
  
  public static void main(String[] args)
  {
    try
    {
      HttpClient client = new HttpClient();

      client.setURL("http://sms.lleida.net/xmlapi/smsgw.cgi");
      String message =
         "xml=<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
         "<sms>" +
         "<user>********</user>" +
         "<password>*******</password>" +
         "<dst>" +
         "<num>999777888</num>" +
         "</dst>" +
         "<txt>Això és una prova</txt>" +
         "</sms>";

      client.setRequestProperty("Content-Type",
        "text/xml;charset=\"utf-8\"");

      client.doPost(message.getBytes());

      System.out.println("Last url:" + client.getLastURL());
      System.out.println("Content-Type:" + client.getContentType());
      System.out.println("Content-Length:" + client.getContentLength());
      System.out.println("Content-Language:" + client.getContentLanguage());
      System.out.println("Content-Encoding:" + client.getContentEncoding());
      System.out.println("------");
      System.out.println("Content as string:" + client.getContentAsString());
      System.out.println("Content as xml:" + client.getContentAsXML(true));
      System.out.println("Content properties:" + client.getHeaderProperties());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
