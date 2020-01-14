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
package org.santfeliu.feed.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.santfeliu.util.HTMLNormalizer;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.enc.HtmlDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author lopezrj
 */

public class FeedUtils
{
  //Tags for RSS feeds
  private static final String TAG_RSS = "rss";  
  private static final String TAG_CHANNEL = "channel";
  private static final String TAG_ITEM = "item";
  private static final String TAG_DESCRIPTION = "description";
  private static final String TAG_CONTENT_ENCODED = "content:encoded";
  private static final String TAG_PUBDATE = "pubDate";
  private static final String TAG_DC_DATE = "dc:date";
  private static final String TAG_ENCLOSURE = "enclosure";
  private static final String TAG_MEDIA_CONTENT = "media:content";  
  private static final String TAG_IMAGE = "image";
  private static final String TAG_URL = "url";  
  private static final String ATTR_URL = "url";

  //Tags for Atom feeds
  private static final String TAG_FEED = "feed";
  private static final String TAG_ENTRY = "entry";
  private static final String TAG_CONTENT = "content";
  private static final String TAG_SUMMARY = "summary";
  private static final String TAG_UPDATED = "updated";
  private static final String CONST_ALTERNATE = "alternate";
  private static final String CONST_ENCLOSURE = "enclosure";  
  private static final String ATTR_REL = "rel";
  private static final String ATTR_HREF = "href";
  
  //Tags for RSS and Atom feeds
  private static final String TAG_TITLE = "title";
  private static final String TAG_LINK = "link";
  private static final String TAG_SOURCE = "source";  
  private static final String ATTR_TYPE = "type";  

  public static List<Row> getRowList(FeedReading feedReading) throws Exception
  {    
    if (feedReading.isJsonReading())
    {
      JsonFeedReader reader;
      if (feedReading.isJsonFacebookReading())
      {
        reader = new JsonFacebookReader(feedReading);          
      }
      else if (feedReading.isJsonTwitterReading())
      {
        reader = new JsonTwitterReader(feedReading);          
      }
      else
      {
        throw new Exception("INVALID_FEED_TYPE");
      }
      return reader.getRowList();
    }
    else //Xml RSS/Atom reading
    {
      InputStream is = null;
      try
      {
        is = getInputStreamFromUrl(feedReading.getFeedUrl());
        DocumentBuilder docBuilder =
          DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docBuilder.parse(is);
        if (doc.getElementsByTagName(TAG_FEED).getLength() > 0) //Atom feed
        {
          return getAtomRowList(doc, feedReading);
        }
        else if (doc.getElementsByTagName(TAG_RSS).getLength() > 0) //RSS feed
        {
          return getRssRowList(doc, feedReading);
        }
        else
        {
          throw new Exception("INVALID_FEED_TYPE");
        }        
      }
      finally
      {
        try
        {
          if (is != null) is.close();
        }
        catch (Exception ex) { }
      }
    }
  }

  public static List<Row> getAtomRowList(Document doc, FeedReading feedReading)
  {
    List<Row> result = new ArrayList<Row>();
    Node feedNode = doc.getElementsByTagName(TAG_FEED).item(0);
    NodeList itemList = ((Element)feedNode).getElementsByTagName(TAG_ENTRY);
    for (int i = 0; i < itemList.getLength() && 
      result.size() < feedReading.getRowCount(); i++)
    {
      Node itemNode = itemList.item(i);
      if (itemNode.getNodeType() == Node.ELEMENT_NODE)
      {      
        Element element = (Element)itemNode;      
        Row row = new Row();
        row.setHeadLine(normalizeText(getTagValue(element, TAG_TITLE)));
        row.setUrl(getAtomUrlValue(element));

        String summary = getTagValue(element, TAG_CONTENT);
        if (summary.isEmpty())
        {
          summary = getTagValue(element, TAG_SUMMARY);
        }
        row.setSummary(normalizeText(summary));

        row.setDate(getDBDateTime(getTagValue(element, TAG_UPDATED)));
        if (feedReading.isIncludeSource())
        {
          setRowSource(row, element);
        }      
        if (feedReading.isIncludeImages())
        {
          row.setImageUrl(getAtomEntryImageURL(element, feedReading, 
            row.getSourceUrl()));
        }
        result.add(row);
      }
    }      
    return result;
  }

  public static List<Row> getRssRowList(Document doc, FeedReading feedReading)
  {
    List<Row> result = new ArrayList<Row>();
    Node feedNode = doc.getElementsByTagName(TAG_RSS).item(0);
    Node channelNode =
      ((Element)feedNode).getElementsByTagName(TAG_CHANNEL).item(0);
    NodeList itemList = ((Element)channelNode).getElementsByTagName(TAG_ITEM);      
    for (int i = 0; i < itemList.getLength() && 
      result.size() < feedReading.getRowCount(); i++)
    {
      Node itemNode = itemList.item(i);
      if (itemNode.getNodeType() == Node.ELEMENT_NODE)
      {
        Element element = (Element)itemNode;      
        Row row = new Row();
        row.setHeadLine(normalizeText(getTagValue(element, TAG_TITLE)));
        row.setUrl(getTagValue(element, TAG_LINK));

        String summary = getTagValue(element, TAG_DESCRIPTION);
        if (summary.isEmpty())
        {
          summary = getTagValue(element, TAG_CONTENT_ENCODED);
        }        
        row.setSummary(normalizeText(summary));

        String pubDate = getTagValue(element, TAG_PUBDATE);
        if (pubDate.isEmpty())
        {
          pubDate = getTagValue(element, TAG_DC_DATE);
        }
        row.setDate(getDBDateTime(pubDate));
        if (feedReading.isIncludeSource())
        {
          setRowSource(row, element); //Source processing
        }        
        if (feedReading.isIncludeImages())
        {
          row.setImageUrl(getRSSEntryImageURL(element, feedReading, 
            row.getSourceUrl()));
        }
        result.add(row);
      }
    }
    return result;
  }
    
  private static void setRowSource(Row row, Element element)
  {
    try
    {
      Node sourceNode = element.getElementsByTagName(TAG_SOURCE).item(0);
      NamedNodeMap map = sourceNode.getAttributes();
      String sourceUrl = map.getNamedItem("url").getNodeValue();
      String sourceTitle = sourceNode.getFirstChild().getNodeValue();
      row.setSourceUrl(sourceUrl);
      row.setSourceTitle(sourceTitle);                
    }
    catch (Exception ex) //no source element
    {
      row.setSourceUrl("");
      row.setSourceTitle("");
    }    
  }
  
  private static String getTagValue(Element element, String tagName)
  {
    try
    {
      String result = element.getElementsByTagName(tagName).item(0).getTextContent();
      if (result == null)
      {
        Node node = element.getElementsByTagName(tagName).item(0).getFirstChild();
        result = node.getNodeValue();  
      }
      return result;
    }
    catch (Exception ex)
    {
      return "";
    }
  }
  
  private static String getAtomUrlValue(Element element)
  {
    NodeList nodeList = element.getElementsByTagName(TAG_LINK);
    for (int i = 0; i < nodeList.getLength(); i++)
    {
      Node itemNode = nodeList.item(i);
      NamedNodeMap map = itemNode.getAttributes();
      if (map.getNamedItem(ATTR_REL) != null)
      {      
        if (CONST_ALTERNATE.equals(map.getNamedItem(ATTR_REL).getNodeValue()))
        {
          if (map.getNamedItem(ATTR_HREF) != null)
          {
            return map.getNamedItem(ATTR_HREF).getNodeValue();
          }
        }
      }
    }
    for (int i = 0; i < nodeList.getLength(); i++)
    {
      Node itemNode = nodeList.item(i);
      NamedNodeMap map = itemNode.getAttributes();
      if (map.getNamedItem(ATTR_HREF) != null)
      {
        return map.getNamedItem(ATTR_HREF).getNodeValue();
      }
    }    
    return "";
  }  

  private static String getAtomEntryImageURL(Element entryElement, 
    FeedReading feedReading, String sourceUrl)
  {
    try
    {
      //Search in entry metadata
      NodeList nodeList = entryElement.getElementsByTagName(TAG_LINK);
      for (int i = 0; i < nodeList.getLength(); i++)
      {
        Node itemNode = nodeList.item(i);
        NamedNodeMap map = itemNode.getAttributes();
        if (map.getNamedItem(ATTR_REL) != null && 
          CONST_ENCLOSURE.equals(map.getNamedItem(ATTR_REL).getNodeValue()))
        {
          if (map.getNamedItem(ATTR_TYPE) != null && 
            map.getNamedItem(ATTR_TYPE).getNodeValue().startsWith("image/"))
          {
            if (map.getNamedItem(ATTR_HREF) != null)
            {
              String url = map.getNamedItem(ATTR_HREF).getNodeValue();
              if (isValidImage(url, feedReading.getInvalidImagePrefixList()))
                return url;            
            }              
          }
        }
      }
      // Search in content
      String url = null;
      nodeList = entryElement.getElementsByTagName(TAG_CONTENT);
      if (nodeList.getLength() > 0)
      {
        Element contentElement = (Element)nodeList.item(0);
        url = getImageURL(contentElement, 
          feedReading.getInvalidImagePrefixList());
      }  
      if (url == null)
      {
        nodeList = entryElement.getElementsByTagName(TAG_SUMMARY);
        if (nodeList.getLength() > 0)
        {
          Element contentElement = (Element)nodeList.item(0);
          url = getImageURL(contentElement, 
            feedReading.getInvalidImagePrefixList());
        }        
      }
      return url;
    }
    catch (Exception ex)
    {
      return null;
    }
  }    

  private static String getRSSEntryImageURL(Element itemElement, 
    FeedReading feedReading, String sourceUrl)
  {
    try
    {
      //Search in <enclosure> tag
      NodeList nodeList = itemElement.getElementsByTagName(TAG_ENCLOSURE);
      for (int i = 0; i < nodeList.getLength(); i++)
      {
        Node itemNode = nodeList.item(i);
        NamedNodeMap map = itemNode.getAttributes();
        if (map.getNamedItem(ATTR_TYPE) != null && 
          map.getNamedItem(ATTR_TYPE).getNodeValue().startsWith("image/"))
        {
          if (map.getNamedItem(ATTR_URL) != null)
          {
            String url = map.getNamedItem(ATTR_URL).getNodeValue();
            if (isValidImage(url, feedReading.getInvalidImagePrefixList()))
              return url;            
          }          
        }
      }
      // Search in <image> tag
      nodeList = itemElement.getElementsByTagName(TAG_IMAGE);
      for (int i = 0; i < nodeList.getLength(); i++)
      {
        Element imageElement = (Element)nodeList.item(i);
        NodeList urlElements = imageElement.getElementsByTagName(TAG_URL);
        if (urlElements.getLength() > 0)
        {
          String url = urlElements.item(0).getFirstChild().getNodeValue();
          if (isValidImage(url, feedReading.getInvalidImagePrefixList()))
            return url;
        }
      }
      //Search in <media:content> tag
      nodeList = itemElement.getElementsByTagName(TAG_MEDIA_CONTENT);
      for (int i = 0; i < nodeList.getLength(); i++)
      {
        Node itemNode = nodeList.item(i);
        NamedNodeMap map = itemNode.getAttributes();
        if (map.getNamedItem(ATTR_TYPE) != null && 
          map.getNamedItem(ATTR_TYPE).getNodeValue().startsWith("image/"))
        {
          if (map.getNamedItem(ATTR_URL) != null)
          {
            String url = map.getNamedItem(ATTR_URL).getNodeValue();
            if (isValidImage(url, feedReading.getInvalidImagePrefixList()))
              return url;            
          }          
        }
      }      
      // Search in content
      String url = null;
      nodeList = itemElement.getElementsByTagName(TAG_DESCRIPTION);
      if (nodeList.getLength() > 0)
      {
        Element contentElement = (Element)nodeList.item(0);        
        url = getImageURL(contentElement, 
          feedReading.getInvalidImagePrefixList());
      }  
      if (url == null)
      {
        nodeList = itemElement.getElementsByTagName(TAG_CONTENT_ENCODED);
        if (nodeList.getLength() > 0)
        {
          Element contentElement = (Element)nodeList.item(0);
          url = getImageURL(contentElement, 
            feedReading.getInvalidImagePrefixList());
        }        
      }
      return url;            
    }
    catch (Exception ex) 
    { 
      return null;
    }    
  }

  private static String getImageURL(Element contentElement, 
    List<String> invalidImagePrefixList)
  {
    try
    {
      if (contentElement.hasChildNodes())
      {
        String content = contentElement.getFirstChild().getNodeValue();      
        if (content != null)        
        {                
          int img1 = content.indexOf("<img");
          while (img1 >= 0)
          {
            int img2 = content.substring(img1).indexOf("/>");
            if (img2 < 0) img2 = content.substring(img1).indexOf("/img>");          
            if (img2 < 0) img2 = content.substring(img1).indexOf(">");          
            String imgTag = content.substring(img1, img1 + img2);            
            int src2 = -1;
            int src1 = imgTag.indexOf("src=\"");
            if (src1 > 0)
            {
              src1 = src1 + 5;
              src2 = imgTag.substring(src1).indexOf("\"");
            }
            else
            {
              src1 = imgTag.indexOf("src=\'");
              if (src1 > 0)
              {
                src1 = src1 + 5;
                src2 = imgTag.substring(src1).indexOf("\'");
              }              
            }
            if (src1 > 0 && src2 > 0)
            {
              String url = imgTag.substring(src1, src1 + src2);
              if (isValidImage(url, invalidImagePrefixList))
              {
                return HtmlDecoder.decode(url);            
              }              
            }
            content = content.substring(img1 + img2);
            img1 = content.indexOf("<img");
          }
        }
      }            
    }
    catch (Exception ex) 
    { 
    }
    return null;
  }  
  
  static String getDBDateTime(String dateString)
  {
    try
    {      
      Date date = TextUtils.parseUnknownDate(dateString.trim());
      if (date != null)
      {
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dbFormat.format(date);        
      }
    }
    catch (Exception ex) { }
    
    return "";
  } 
  
  static String normalizeText(String s)
  {
    String result = HTMLNormalizer.replaceSpecialChars(s);
    result = result.replace("\u0093", "\"");
    result = result.replace("\u0094", "\"");
    result = result.replace("…", "...");
    return result;
  }
  
  static InputStream getInputStreamFromUrl(String sUrl) throws Exception
  {
    InputStream is;
    URL url = new URL(sUrl);
    try
    {
      is = url.openStream();
    }
    catch (IOException ex)
    {
      URLConnection urlConn = url.openConnection();
      urlConn.setRequestProperty("User-Agent", 
        "Mozilla/5.0 (Windows NT 5.1; rv:22.0) Gecko/20100101 Firefox/22.0");
      is = urlConn.getInputStream();                  
    }
    return is;
  }  
  
  private static boolean isValidImage(String url, 
    List<String> invalidImagePrefixList)
  {    
    if (invalidImagePrefixList != null)
    {
      for (String invalidPrefix : invalidImagePrefixList)
      {              
        if (url.startsWith(invalidPrefix)) return false; 
      }
    }
    return true;
  }
      
  public static class FeedReading
  {
    private String feedUrl;
    private String sourceUrl;
    private String feedTitle;
    private int rowCount = 10;
    private boolean includeImages = false;
    private boolean includeSource = false;
    private List<String> invalidImagePrefixList = new ArrayList<String>();    
      
    public String getFeedUrl()
    {
      return feedUrl;
    }

    public void setFeedUrl(String feedUrl)
    {
      this.feedUrl = feedUrl;
    }

    public String getSourceUrl()
    {
      return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl)
    {
      this.sourceUrl = sourceUrl;
    }

    public String getFeedTitle()
    {
      return feedTitle;
    }

    public void setFeedTitle(String feedTitle)
    {
      this.feedTitle = feedTitle;
    }

    public int getRowCount()
    {
      return rowCount;
    }

    public void setRowCount(int rowCount)
    {
      this.rowCount = rowCount;
    }

    public boolean isIncludeImages()
    {
      return includeImages;
    }

    public void setIncludeImages(boolean includeImages)
    {
      this.includeImages = includeImages;
    }

    public boolean isIncludeSource()
    {
      return includeSource;
    }

    public void setIncludeSource(boolean includeSource)
    {
      this.includeSource = includeSource;
    }

    public List<String> getInvalidImagePrefixList()
    {
      return invalidImagePrefixList;
    }

    public void setInvalidImagePrefixList(List<String> invalidImagePrefixList)
    {
      this.invalidImagePrefixList = invalidImagePrefixList;
    }

    public boolean isJsonReading()
    {
      return isJsonFacebookReading() || isJsonTwitterReading();
    }
    
    public boolean isJsonFacebookReading()
    {
      return feedUrl != null && feedUrl.contains("facebook.com");
    }
    
    public boolean isJsonTwitterReading()
    {
      return feedUrl != null && feedUrl.contains("twitter.com");
    }    
  }
  
  public static class Row
  {
    private String sourceTitle;
    private String sourceUrl;
    private String date;  
    private String headLine;    
    private String summary;
    private String url;
    private String imageUrl;

    public String getSourceTitle() 
    {
      return sourceTitle;
    }

    public void setSourceTitle(String sourceTitle) 
    {
      this.sourceTitle = sourceTitle;
    }

    public String getSourceUrl() 
    {
      return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) 
    {
      this.sourceUrl = sourceUrl;
    }

    public String getDate() 
    {
      return date;
    }

    public void setDate(String date) 
    {
      this.date = date;
    }

    public String getHeadLine() 
    {
      return headLine;
    }

    public void setHeadLine(String headLine) 
    {
      this.headLine = headLine;
    }

    public String getSummary() 
    {
      return summary;
    }

    public void setSummary(String summary) 
    {
      this.summary = summary;
    }

    public String getUrl() 
    {
      return url;
    }

    public void setUrl(String url) 
    {
      this.url = url;
    }

    public String getImageUrl()
    {
      return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
      this.imageUrl = imageUrl;
    }
    
    @Override
    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      String ln = System.getProperty("line.separator");
      sb.append("*** Entry ***").append(ln);
      sb.append("Headline:").append(getHeadLine()).append(ln);    
      sb.append("URL:").append(getUrl()).append(ln);
      sb.append("Date:").append(getDate()).append(ln);  
      sb.append("Summary:").append(getSummary()).append(ln);
      sb.append("Image URL:").append(getImageUrl()).append(ln);    
      sb.append("Source Title:").append(getSourceTitle()).append(ln);
      sb.append("Source URL:").append(getSourceUrl());
      return sb.toString();
    }
  }
    
  public static void main(String[] args) throws Exception
  {
    String url = "file://c:/feed_biblio.json";
    FeedUtils.FeedReading feedReading = new FeedUtils.FeedReading();
    feedReading.setFeedUrl(url);
    feedReading.setSourceUrl("http://www.google.es");
    feedReading.setIncludeSource(false);
    feedReading.setIncludeImages(false);
    feedReading.setRowCount(1000);
    List<Row> rowList = FeedUtils.getRowList(feedReading);
    int i = 0;
    for (Row row : rowList)
    {
      System.out.println("*** ROW " + ++i + " ***");
      System.out.println(row);
    }
  }
  
}
