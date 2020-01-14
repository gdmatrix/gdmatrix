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
package org.santfeliu.news.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.CMSManagerService;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.matrix.news.NewView;
import org.matrix.news.SectionFilter;
import org.matrix.news.SectionView;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.translation.TranslatorFactory;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.ApplicationBean.WebTranslator;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj
 */
public class NewsRSSServlet extends HttpServlet
{
  private static final String PARAM_RSS_ENABLED =
    "rss.enabled";
  private static final String PARAM_RSS_TITLE =
    "rss.title";
  private static final String PARAM_RSS_DESCRIPTION =
    "rss.description";
  private static final String PARAM_RSS_ICON_DOCID =
    "rss.iconDocId";
  private static final String PARAM_RSS_DEEP =
    "rss.deep";
  private static final String PARAM_RSS_MAX_RESULTS =
    "rss.maxResults";  
  private static final String PARAM_RSS_INCLUDE_IMAGES =
    "rss.includeImages";  
  private static final String PARAM_RSS_DEFAULT_TITLE_PREFIX =
    "rss.defaultTitlePrefix";
  private static final String PARAM_RSS_DEFAULT_TITLE_SUFFIX =
    "rss.defaultTitleSuffix";
  private static final String PARAM_RSS_TOKEN_CODE =
    "rss.tokenCode";

  private static final String NO_CHANNEL_MESSAGE =
    "No hi ha cap canal associat a aquesta URL";
  private static final String ERROR_MESSAGE =
    "Error al generar contingut RSS";

  private static final String LANGUAGE_PARAM = "language";
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    try
    {      
      CMSManagerPort cmsPort = getCMSManagerPort(request);      
      Node node = getNewsNode(request, cmsPort);
      InheritedProperties inheritedProperties = new InheritedProperties();
      collectInheritedProperties(node, cmsPort, inheritedProperties);
      
      if (inheritedProperties.getEnabled())
      {
        NewsManagerClient newsClient = getNewsManagerClient(request);
        writeRSS(request, response, node, inheritedProperties, newsClient, cmsPort);
      }
      else
      {
        writeErrorRSS(request, response, NO_CHANNEL_MESSAGE);
      }          
    }
    catch (Exception ex)
    {
      if (NO_CHANNEL_MESSAGE.equals(ex.getMessage()))
      {
        writeErrorRSS(request, response, NO_CHANNEL_MESSAGE);
      }
      else
      {
        writeErrorRSS(request, response, ERROR_MESSAGE);
      }
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    doGet(request, response);
  }

  private void writeRSS(HttpServletRequest request,
    HttpServletResponse response, Node node, InheritedProperties inheritedProperties, 
    NewsManagerClient newsClient, CMSManagerPort cmsPort) throws IOException
  {    
    String serverURL = getServerURL(request);
    String dateTime = getDateTime();
    String language = getLanguage(request);
    response.setContentType("text/xml");
    response.setCharacterEncoding("UTF-8");
    PrintWriter writer = response.getWriter();
    writer.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
    writer.println("<rss version=\"2.0\">");
    List<SectionView> sectionViewList = new ArrayList<SectionView>();
    try
    {
      SectionFilter filter = new SectionFilter();
      if (inheritedProperties.getDeep())
      {
        filter.getSectionId().addAll(getAllNodeIdList(node, cmsPort));
        filter.setMaxResults(Integer.MAX_VALUE);
      }
      else
      {
        filter.getSectionId().add(getTargetNodeId(node));
        filter.setMaxResults(inheritedProperties.getMaxResults());
      }
      filter.getExcludeDrafts().add(true);
      filter.setStartDateTime(dateTime);
      filter.setEndDateTime(dateTime);      
      sectionViewList = newsClient.findNewsBySectionFromCache(filter);
      writer.println("<channel>");
      writeChannelDescriptionRSS(writer, language, serverURL, node, inheritedProperties);
      writeChannelImageRSS(writer, serverURL, inheritedProperties.getIconDocId());
      List<NewView> newViewList;
      if (inheritedProperties.getDeep())
      {
        newViewList = new ArrayList<NewView>();
        for (SectionView sectionView : sectionViewList)
        {
          newViewList.addAll(sectionView.getNewView());
        }
        Collections.sort(newViewList, new Comparator() {
          public int compare(Object o1, Object o2)
          {
            NewView nv1 = (NewView)o1;
            NewView nv2 = (NewView)o2;
            String nv1StartDateTime = nv1.getStartDate() + nv1.getStartTime();
            String nv2StartDateTime = nv2.getStartDate() + nv2.getStartTime();
            return (nv2StartDateTime.compareTo(nv1StartDateTime));
          }
        });
        newViewList = newViewList.subList(0, 
          Math.min(newViewList.size(), inheritedProperties.getMaxResults()));
      }
      else
      {
        newViewList = sectionViewList.get(0).getNewView();
      }
      for (NewView newView : newViewList)
      {
        writeItemRSS(writer, newView, serverURL, language, node.getNodeId(), 
          inheritedProperties.getIncludeImages());
      }
      writer.print("</channel>");
      writer.print("</rss>");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void writeChannelDescriptionRSS(PrintWriter writer, String language,
    String serverURL, Node node, InheritedProperties inheritedProperties)
  {
    String rssTitle = getPropertyValue(node, PARAM_RSS_TITLE);
    if (rssTitle == null) rssTitle = 
      getDefaultRSSTitle(node, inheritedProperties);
    String rssDescription = getPropertyValue(node, PARAM_RSS_DESCRIPTION);
    if (rssDescription == null) rssDescription = 
      getDefaultRSSTitle(node, inheritedProperties);
    writer.println("<title>" +
      getNonParsedText
      (
        translate(rssTitle, "rss", language, false)
      ) +
      "</title>");
    writer.println("<link>" +
      getNonParsedText(serverURL) +
      "</link>");
    writer.println("<description>" + 
      getNonParsedText
      (
        translate(rssDescription, "rss", language, false)
      ) +
      "</description>");
  }

  private void writeChannelImageRSS(PrintWriter writer, String serverURL,
    String rssIconDocId)
  {
    if (rssIconDocId != null)
    {
      writer.println("<image>");
      writer.println("<url>" +
        getNonParsedText(serverURL + "/documents/" + rssIconDocId) +
        "</url>");
      writer.println("<title>" +
        getNonParsedText("RSS") +
        "</title>");
      writer.println("<link>" +
        getNonParsedText(serverURL) +
        "</link>");
      writer.println("</image>");
    }
  }

  private void writeItemRSS(PrintWriter writer, NewView newView,
    String serverURL, String language, String newsNodeId, boolean includeImages)
  {
    writer.println("<item>");
    writer.println("<title>" +
      getNonParsedText
      (
        translate(newView.getHeadline(), newView.getNewId(), language, false)
      ) +
      "</title>");
    writer.println("<link>" +
      getNonParsedText
      (
        serverURL + "/go.faces?xmid=" + newsNodeId +
          "&newid=" + newView.getNewId() +
          "&language=" + language
      ) +
      "</link>");
    String imageTag = "";
    if (includeImages)
    {
      String docId = getNewImageDocId(newView);
      if (docId != null && !docId.isEmpty())
      {            
        String imageURL = serverURL + "/documents/" + docId;
        imageTag = "<img alt=\"\" src=\"" + imageURL + "\" />";
      }    
    }
    writer.println("<description>" +
      getNonParsedText
      (
        imageTag + translate(newView.getSummary(), newView.getNewId(), language)
      ) +
      "</description>");
    writer.println("<pubDate>" +
      getNonParsedText(getNewViewDateTime(newView)) +
      "</pubDate>");
    writer.println("</item>");
  }

  private void writeErrorRSS(HttpServletRequest request,
    HttpServletResponse response, String message) throws IOException
  {
    String language = getLanguage(request);
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    PrintWriter writer = response.getWriter();
    writer.println("<html>");
    writer.println("<head>");
    writer.println("<meta http-equiv=\"Content-Type\" " +
      "content=\"text/html; charset=UTF-8\" />");
    writer.println("<title>RSS</title>");
    writer.println("</head>");
    writer.println("<body>");
    writer.println(translate(message, "rss", language, false));
    writer.println("</body>");
    writer.println("</html>");
  }

  private String getServerURL(HttpServletRequest request)
  {
    return HttpUtils.getContextURL(request);
  }

  private String getNewsNodeId(HttpServletRequest request) throws Exception
  {
    // TODO: CHECK URL
    String url = request.getRequestURL().toString();
    String servletPath = request.getServletPath();
    String serverURL = url.substring(0, url.indexOf(servletPath));
    String params = url.substring((serverURL + servletPath).length());
    if (params.startsWith("/")) //rss channel specified
    {
      int i = params.indexOf(";");
      String auxNodeId = (i > 0 ? params.substring(1, i) : params.substring(1));
      if ("".equals(auxNodeId)) return getDefaultNewsNodeId();      
      try
      {
        Integer.parseInt(auxNodeId);
        return auxNodeId;
      }
      catch (NumberFormatException ex)
      {
        //rss token specified
        NodeFilter nodeFilter = new NodeFilter();        
        nodeFilter.getWorkspaceId().add(getWorkspaceId(request));
        Property tokenProperty = new Property();
        tokenProperty.setName(PARAM_RSS_TOKEN_CODE);
        tokenProperty.getValue().add(auxNodeId);
        nodeFilter.getProperty().add(tokenProperty);
        List<Node> newsNodeList = getCMSManagerPort(request).findNodes(nodeFilter);
        if (newsNodeList.size() > 0) 
        {
          return newsNodeList.get(0).getNodeId();
        }
        else
        {
          throw new Exception(NO_CHANNEL_MESSAGE);
        }
      }
    }
    else //default rss channel
    {
      return getDefaultNewsNodeId();
    }
  }

  private String getDefaultNewsNodeId()
  {
    return MatrixConfig.getClassProperty(
      NewsRSSServlet.class, "defaultNewsNodeId");
  }

  private String getWorkspaceId(HttpServletRequest request)
  {
    String workspaceId;
    UserSessionBean userSessionBean = getUserSessionBean(request);
    if (userSessionBean != null &&
      userSessionBean.getWorkspaceId() != null)
    {
      workspaceId = userSessionBean.getWorkspaceId();
    }
    else
    {
      workspaceId = 
        MatrixConfig.getProperty("org.santfeliu.web.defaultWorkspaceId");
    }
    return workspaceId;
  }

  private String getDateTime()
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    return df.format(new Date());
  }

  private String getLanguage(HttpServletRequest request)
  {
    String language = request.getParameter(LANGUAGE_PARAM);
    if (language == null || !TranslatorFactory.isSupportedLanguage(language))
    {
      UserSessionBean userSessionBean = getUserSessionBean(request);
      if (userSessionBean != null)
      {
        language = userSessionBean.getLastPageLanguage();
        if (language == null)
        {
          language = TranslatorFactory.getDefaultLanguage();   
        }
      }
      else
      {
        language = TranslatorFactory.getDefaultLanguage();
      }
    }
    return language;
  }

  private String getNewViewDateTime(NewView newView)
  {
    try
    {
      SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMddHHmmss");
      Date date = df1.parse(newView.getStartDate() + newView.getStartTime());
      SimpleDateFormat df2 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",
        Locale.ENGLISH);
      return df2.format(date);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return "";
  }
  
  private String getNewImageDocId(NewView newView)
  {
    String docId = NewsConfigBean.getListImageDocId(newView);
    if (docId == null)
    {
      docId = NewsConfigBean.getDetailsImageDocId(newView);
    }
    if (docId == null)
    {
      docId = NewsConfigBean.getCarouselImageDocId(newView);
    }
    return docId;
  }  
  
  private String getNonParsedText(String text)
  {
    return("<![CDATA[" + text + "]]>");
  }

  private String translate(String text, String newId, String language)
  {
    return translate(text, newId, language, true);
  }
  
  private String translate(String text, String newId, String language,
    boolean htmlTranslation)
  {
    if (htmlTranslation)
    {
      return translateHTMLText(text, newId, language);
    }
    else
    {
      return translatePlainText(text, newId, language);
    }
  }

  private String translateHTMLText(String text, String newId, String language)
  {
    try
    {
      if (text != null && text.trim().length() > 0)
      {
        ApplicationBean applicationBean =
          (ApplicationBean)getServletContext().getAttribute("applicationBean");
        WebTranslator tr = (WebTranslator)applicationBean.getTranslator();
        StringWriter sw = new StringWriter();
        String group = "new:" + newId;
        tr.translate(new StringReader(text), sw, "text/html", language, group);
        return sw.toString();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return "";
  }

  private String translatePlainText(String text, String newId, String language)
  {
    try
    {
      if (text != null && text.trim().length() > 0)
      {
        ApplicationBean applicationBean =
          (ApplicationBean)getServletContext().getAttribute("applicationBean");
        WebTranslator tr = (WebTranslator)applicationBean.getTranslator();
        StringWriter sw = new StringWriter();
        String group = "new:" + newId;
        tr.translate(new StringReader(text), sw, "text/plain", language, group);
        return sw.toString();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return "";
  }

  private Node getNewsNode(HttpServletRequest request, CMSManagerPort port)
    throws Exception
  {
    return port.loadNode(getWorkspaceId(request), getNewsNodeId(request));
  }
  
  private String getTargetNodeId(Node node)
  {
    String copyNodeId = getPropertyValue(node, "copySectionId");
    return (copyNodeId == null ? node.getNodeId() : copyNodeId);
  }

  private String getDefaultRSSTitle(Node node, 
    InheritedProperties inheritedProperties)
  {
    String auxTitle = getPropertyValue(node, "description");
    if (auxTitle == null)
    {
      auxTitle = getPropertyValue(node, "label");
      if (auxTitle == null)
      {
        auxTitle = node.getNodeId();
      }
    }
    String prefix = inheritedProperties.getDefaultTitlePrefix();
    String suffix = inheritedProperties.getDefaultTitleSuffix();
    return ((prefix == null ? "" : prefix) + 
      auxTitle + 
      (suffix == null ? "" : suffix));
  }

  private String getPropertyValue(Node node, String propertyName)
  {
    List<Property> propertyList = node.getProperty();
    for (Property property : propertyList)
    {
      if (property.getName().equals(propertyName))
      {
        return property.getValue().get(0);
      }
    }
    return null;
  }

  private void collectInheritedProperties(Node node, CMSManagerPort port, 
    InheritedProperties inheritedProperties)    
  {
    if (node == null) return;
    
    if (inheritedProperties.getIconDocId() == null)
    {
      String iconDocIdValue = getPropertyValue(node, PARAM_RSS_ICON_DOCID);
      inheritedProperties.setIconDocId(iconDocIdValue);
    }
    
    if (inheritedProperties.getEnabled() == null)
    {
      String enabledValue = getPropertyValue(node, PARAM_RSS_ENABLED);
      if (enabledValue != null) inheritedProperties.setEnabled(Boolean.valueOf(enabledValue));      
    }
    
    if (inheritedProperties.getDeep() == null)
    {  
      String deepValue = getPropertyValue(node, PARAM_RSS_DEEP);
      if (deepValue != null) inheritedProperties.setDeep(Boolean.valueOf(deepValue));
    }

    if (inheritedProperties.getMaxResults() == null)
    {
      String maxResultsValue = getPropertyValue(node, PARAM_RSS_MAX_RESULTS);
      if (maxResultsValue != null) inheritedProperties.setMaxResults(Integer.valueOf(maxResultsValue));
    }

    if (inheritedProperties.getIncludeImages() == null)
    {
      String includeImagesValue = getPropertyValue(node, PARAM_RSS_INCLUDE_IMAGES);
      if (includeImagesValue != null) inheritedProperties.setIncludeImages(Boolean.valueOf(includeImagesValue));
    }    

    if (inheritedProperties.getDefaultTitlePrefix() == null)
    {
      String defaultTitlePrefix = getPropertyValue(node, PARAM_RSS_DEFAULT_TITLE_PREFIX);
      inheritedProperties.setDefaultTitlePrefix(defaultTitlePrefix);
    }

    if (inheritedProperties.getDefaultTitleSuffix() == null)
    {
      String defaultTitleSuffix = getPropertyValue(node, PARAM_RSS_DEFAULT_TITLE_SUFFIX);
      inheritedProperties.setDefaultTitleSuffix(defaultTitleSuffix);
    }

    if (!inheritedProperties.isDone() && node.getParentNodeId() != null)
    {
      Node parentNode = port.loadNode(node.getWorkspaceId(), node.getParentNodeId());
      collectInheritedProperties(parentNode, port, inheritedProperties);
    }
    else
    {
      if (inheritedProperties.getEnabled() == null) inheritedProperties.setEnabled(false);
      if (inheritedProperties.getDeep() == null) inheritedProperties.setDeep(false);      
      if (inheritedProperties.getMaxResults() == null) inheritedProperties.setMaxResults(Integer.MAX_VALUE); 
      if (inheritedProperties.getIncludeImages() == null) inheritedProperties.setIncludeImages(true);       
    }
  }

  private List<String> getAllNodeIdList(Node node, CMSManagerPort port)
  {
    List<String> result = new ArrayList<String>();
    result.add(getTargetNodeId(node));
    
    NodeFilter filter = new NodeFilter();
    filter.getWorkspaceId().add(node.getWorkspaceId());
    filter.getParentNodeId().add(node.getNodeId());
    List<Node> nodeList = port.findNodes(filter);

    for (Node child : nodeList)
    {
      result.addAll(getAllNodeIdList(child, port));
    }
    
    return result;
  }
  
  private NewsManagerClient getNewsManagerClient(HttpServletRequest request)
  {
    Credentials credentials = SecurityUtils.getCredentials(request, false);
    if (credentials == null)
    {
      credentials = UserSessionBean.getCredentials(request);
    }
    String username = credentials.getUserId();
    String password = credentials.getPassword();
    return new NewsManagerClient(username, password);
  }

  private CMSManagerPort getCMSManagerPort(HttpServletRequest request)
  {
    Credentials credentials = SecurityUtils.getCredentials(request, false);
    if (credentials == null)
    {
      credentials = UserSessionBean.getCredentials(request);
    }
    String username = credentials.getUserId();
    String password = credentials.getPassword();
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(CMSManagerService.class);
    return endpoint.getPort(CMSManagerPort.class, username, password);
  }
  
  private UserSessionBean getUserSessionBean(HttpServletRequest request)
  {
    try
    {
      HttpSession session = request.getSession(false);
      if (session != null)
      {
        return (UserSessionBean)session.getAttribute("userSessionBean");
      }      
    }
    catch (Exception ex) 
    { 
    }
    return null;
  }

  class InheritedProperties
  {
    private String iconDocId = null;
    private Boolean enabled = null;
    private Boolean deep = null;
    private Integer maxResults = null;
    private Boolean includeImages = null;
    private String defaultTitlePrefix = null;
    private String defaultTitleSuffix = null;

    public String getIconDocId()
    {
      return iconDocId;
    }

    public void setIconDocId(String iconDocId)
    {
      this.iconDocId = iconDocId;
    }

    public Boolean getEnabled()
    {
      return enabled;
    }

    public void setEnabled(Boolean enabled)
    {
      this.enabled = enabled;
    }

    public Boolean getDeep()
    {
      return deep;
    }

    public void setDeep(Boolean deep)
    {
      this.deep = deep;
    }

    public Integer getMaxResults()
    {
      return maxResults;
    }

    public void setMaxResults(Integer maxResults)
    {
      this.maxResults = maxResults;
    }

    public Boolean getIncludeImages()
    {
      return includeImages;
    }

    public void setIncludeImages(Boolean includeImages)
    {
      this.includeImages = includeImages;
    }

    public String getDefaultTitlePrefix()
    {
      return defaultTitlePrefix;
    }

    public void setDefaultTitlePrefix(String defaultTitlePrefix)
    {
      this.defaultTitlePrefix = defaultTitlePrefix;
    }

    public String getDefaultTitleSuffix()
    {
      return defaultTitleSuffix;
    }

    public void setDefaultTitleSuffix(String defaultTitleSuffix)
    {
      this.defaultTitleSuffix = defaultTitleSuffix;
    }

    public boolean isDone()
    {
      return (iconDocId != null && enabled != null && deep != null && 
        maxResults != null && includeImages != null && 
        defaultTitlePrefix != null && defaultTitleSuffix != null);
    }    
  }
  
}
