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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.matrix.news.New;
import org.matrix.news.NewSection;
import org.matrix.news.NewView;
import org.matrix.news.NewsFilter;
import org.santfeliu.cms.web.CMSConfigBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.BasicSearchBean;
import org.santfeliu.web.obj.util.SetObjectManager;

/**
 *
 * @author unknown
 */
@CMSManagedBean
public class NewSearchBean extends BasicSearchBean
{
  @CMSProperty
  public static final String MAX_NEWS_PER_PAGE_PROPERTY = "body.maxNewsPerPage";
  @CMSProperty
  public static final String SECTION_ID_PROPERTY = "sectionId";
  @CMSProperty(mandatory=true)
  public static final String READ_ONLY_MODE_PROPERTY = "body.readOnlyMode";
  @CMSProperty
  public static final String FILTER_RENDER_PROPERTY = "filter.render";
  @CMSProperty
  public static final String FILTER_ID_RENDER_PROPERTY =
    "filter.id.render";  
  @CMSProperty
  public static final String FILTER_USER_RENDER_PROPERTY =
    "filter.user.render";  
  @CMSProperty
  public static final String RESULTBAR_RENDER_PROPERTY =
    "body.resultBar.render";
  @CMSProperty
  public static final String COLHEADERS_RENDER_PROPERTY =
    "body.colHeaders.render";
  @CMSProperty
  public static final String PAGINATOR_RENDER_PROPERTY =
    "body.paginator.render";
  @CMSProperty
  public static final String HEADER_RENDER_PROPERTY = "header.render";
  @CMSProperty
  public static final String HEADER_DOCID_PROPERTY = "header.docId";
  @CMSProperty
  public static final String FOOTER_RENDER_PROPERTY = "footer.render";
  @CMSProperty
  public static final String FOOTER_DOCID_PROPERTY = "footer.docId";
  @CMSProperty
  public static final String NEWS_RESTRICTED_AGENTS = "newsRestrictedAgents";
  public static final String NEWS_RESTRICT_ALL_AGENTS = "ALL";
  @CMSProperty
  public static final String DEFAULT_START_DATE_TIME_PROPERTY =
    "newSearch.defaultStartDateTime";
  @CMSProperty
  public static final String DEFAULT_END_DATE_TIME_PROPERTY =
    "newSearch.defaultEndDateTime";
  @CMSProperty
  public static final String URL_SEPARATOR_PROPERTY = "newsURLSeparator";
  
  private static final String DOC_SERVLET_PATH = "/documents/";
  

  private SetObjectManager setObjectManager;  
  private NewsFilter filter;  
  
  private Set<String> visibleSections = new HashSet<String>();
  private Set<String> editableSections = new HashSet<String>();

  private String lastMid = null;
  private String searchNewId;
  private String searchContent;

  public NewSearchBean()
  {
    filter = new NewsFilter();
    setObjectManager = new SetObjectManager(filter);
  }

  public NewsFilter getFilter()
  {
    return filter;
  }

  public void setFilter(NewsFilter filter)
  {
    this.filter = filter;
  }

  public String getSearchNewId() 
  {
    return searchNewId;
  }

  public void setSearchNewId(String searchNewId) 
  {
    this.searchNewId = searchNewId;
  }

  public String getSearchContent()
  {
    return searchContent;
  }

  public void setSearchContent(String searchContent)
  {
    this.searchContent = searchContent;
  }

  public int countResults()
  {
    try
    {
      filter.getSectionId().clear();
      if (isEditMode())
      {
        filter.setExcludeDrafts(false);
        filter.setExcludeNotPublished(false);
        filter.getSectionId().addAll(editableSections);
      }
      else
      {
        filter.setExcludeDrafts(true);
        filter.setExcludeNotPublished(true);        
        MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
          getMenuModel().getSelectedMenuItem();
        List<String> sectionIds = menuItem.getMultiValuedProperty(
          SECTION_ID_PROPERTY);
        if (sectionIds.isEmpty())
        {
          filter.getSectionId().addAll(visibleSections);
        }        
        else 
        {
          for (String sectionId : sectionIds)
          {            
            if (visibleSections.contains(sectionId))
            {
              filter.getSectionId().add(sectionId);              
            }
          }          
        }        
      }
      return NewsConfigBean.getPort().countNewsFromCache(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      filter.getSectionId().clear();
      if (isEditMode())
      {
        filter.setExcludeDrafts(false);
        filter.setExcludeNotPublished(false);        
        filter.getSectionId().addAll(editableSections);
      }
      else
      {
        filter.setExcludeDrafts(true);
        filter.setExcludeNotPublished(true);        
        MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
          getMenuModel().getSelectedMenuItem();
        List<String> sectionIds = menuItem.getMultiValuedProperty(
          SECTION_ID_PROPERTY);
        if (sectionIds.isEmpty())
        {
          filter.getSectionId().addAll(visibleSections);
        }        
        else 
        {
          for (String sectionId : sectionIds)
          {            
            if (visibleSections.contains(sectionId))
            {
              filter.getSectionId().add(sectionId);              
            }
          }          
        }
      }
      List<NewView> list = NewsConfigBean.getPort().findNewViewsFromCache(filter);      
      return list;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;    
  }

  public String showNew()
  {
    return getControllerBean().showObject("New", 
      (String)getValue("#{row.newId}"));
  }

  public String getNewDate()
  {
    NewView row = (NewView)getValue("#{row}");
    String startDay = row.getStartDate();
    if ((startDay != null) && (startDay.length() > 0))
    {
      try
      {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat dayHumanFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dayHumanFormat.format(dayFormat.parse(startDay));
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return "";
  }

  @CMSAction
  public String show()
  { 
    Map requestParameters = getExternalContext().getRequestParameterMap();
    String newId = (String)requestParameters.get("newid");
    if (newId != null) //jump to new
    {
      try
      {
        return goToNew(newId);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        getExternalContext().log(ex.getMessage());
      }
      return null;
    }
    else
    {
      ((NewBean)getObjectBean()).setAutoSectionId(null);
      boolean nodeChange = 
        !UserSessionBean.getCurrentInstance().getSelectedMid().equals(lastMid);
      boolean reloadDefaults = 
        (nodeChange || (!nodeChange && !isFilterRender()));
      if (reloadDefaults)
      {
        searchNewId = null;
        filter = new NewsFilter();
        Date now = new Date();
        filter.setStartDateTime(getDefaultStartDateTime(now));
        filter.setEndDateTime(getDefaultEndDateTime(now));
        filter.setContent("");
        filter.setUserId(null);
        setObjectManager.setObject(filter);
      }
      setObjectManager.execute(getRequestParameters());
      if (filter.getContent() != null)
      {
        setSearchContent(filter.getContent().replace("%", " "));
      }
      if (!filter.getNewId().isEmpty())
      {
        searchNewId = filter.getNewId().get(0);        
      }
      lastMid = UserSessionBean.getCurrentInstance().getSelectedMid();
      search();
      return "new_search";
    }
  }

  public String search()
  {
    try
    {      
      putNewsFilterData(filter);
      if (isEditMode())
      {
        this.editableSections = getEditableSections();        
      }
      else
      {
        this.visibleSections = getVisibleSections();        
      }
      return super.search();      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public int getPageSize()
  {
    int superPageSize = super.getPageSize();
    if (superPageSize != PAGE_SIZE)
      return superPageSize;
    
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String rowsPerPage = mic.getProperty(MAX_NEWS_PER_PAGE_PROPERTY);
    if (rowsPerPage != null) return Integer.parseInt(rowsPerPage);
    else return 10;
  }

  public boolean isEditMode()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(READ_ONLY_MODE_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("false");
  }
  
  public boolean isFilterRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(FILTER_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");    
  }

  public boolean isFilterUserRender()
  {
    boolean defaultValue = true;
    if (!isEditMode()) defaultValue = false;
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(FILTER_USER_RENDER_PROPERTY);
    boolean enabledInNode = 
      (value == null ? defaultValue : value.equalsIgnoreCase("true")); 
    return enabledInNode;
  }
  
  public boolean isFilterIdRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(FILTER_ID_RENDER_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");    
  }
  
  public boolean isResultBarRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(RESULTBAR_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");    
  }
  
  public boolean isColHeadersRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(COLHEADERS_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");    
  }
  
  public boolean isPaginatorRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(PAGINATOR_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");    
  }

  public boolean isHeaderRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(HEADER_RENDER_PROPERTY);
    if (value == null) return false;
    else return "true".equalsIgnoreCase(value);
  }
  
  public boolean isFooterRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(FOOTER_RENDER_PROPERTY);
    if (value == null) return false;
    else return "true".equalsIgnoreCase(value);    
  }

  public String getHeaderUrl()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String headerDocId = menuItem.getProperty(HEADER_DOCID_PROPERTY);
    return getDocumentServletURL() + headerDocId;
  }

  public String getFooterUrl()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String footerDocId = menuItem.getProperty(FOOTER_DOCID_PROPERTY);
    return getDocumentServletURL() + footerDocId;
  }

  public String getUrlSeparator()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String urlSeparator = menuItem.getProperty(URL_SEPARATOR_PROPERTY);
    return (urlSeparator == null ? "###" : urlSeparator);
  }

  public String getTranslationGroup()
  {
    String newId = (String)getValue("#{row.newId}");
    return "new:" + newId;
  }

  public String goToNew()
  {
    return goToNew(null);
  }
  
  public String goToNew(String newId)
  {
    try
    {
      NewsManagerClient client = NewsConfigBean.getPort();      
      New newObject = client.loadNewFromCache(newId);
      if (newId == null)
      {
        NewView row = (NewView)getFacesContext().getExternalContext().
          getRequestMap().get("row");
        newId = row.getNewId();
      }
      else
      {
        boolean[] check = checkNewSections(client, newId);
        boolean userCanRead = check[0];
        boolean userCanEdit = check[1];
        if (!userCanRead)
          return "new_inaccessible";
        if (newObject.isDraft() && !userCanEdit)
          return "new_inaccessible";
      }
      NewDetailsBean newDetailsBean = (NewDetailsBean)getBean("newDetailsBean");
      if (newDetailsBean == null) newDetailsBean = new NewDetailsBean();
      newDetailsBean.setNewObject(newObject);
      newDetailsBean.prepareView(client);
      newDetailsBean.setReadingCountRender(true);
      int readingCount = newObject.getTotalReadingCount();
      if (isReadingCountIncrementAllowed())
      {
        readingCount = client.incrementNewCounter(newId, null);
      }
      newDetailsBean.setReadingCount(readingCount);
      return "new_details";
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public String getNewLink()
  {
    NewView row = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    if (isCustomUrlHeadline(row))
    {
      String headline = row.getHeadline();
      int idx = headline.lastIndexOf(getUrlSeparator());
      return headline.substring(idx + getUrlSeparator().length());      
    }
    else
    {
      if (row.getCustomUrl() != null)
      {
        return row.getCustomUrl();
      }
      else
      {      
        MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
          getMenuModel().getSelectedMenuItem();
        return getContextPath() + "/go.faces?xmid=" + menuItem.getMid() +
          "&newid=" + row.getNewId();
      }
    }
  }
  
  public String getNewHeadline()
  {
    NewView row = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    if (isCustomUrlHeadline(row))
    {
      String headline = row.getHeadline();
      int idx = headline.lastIndexOf(getUrlSeparator());
      return headline.substring(0, idx);
    }
    else
    {
      return row.getHeadline();
    }
  }

  private Set<String> getEditableSections() throws Exception
  {
    Set<String> result = new HashSet<String>();
    String workspaceId = UserSessionBean.getCurrentInstance().getWorkspaceId();
    CMSManagerPort port = CMSConfigBean.getPort();
    NodeFilter nodeFilter = new NodeFilter();
    Property property = new Property();
    property.setName("action");
    property.getValue().add("%newSearchBySection%");
    nodeFilter.getProperty().add(property);
    nodeFilter.getWorkspaceId().add(workspaceId);
    List<Node> nodeList = port.findNodes(nodeFilter);
    List<String> nodeIdList = new ArrayList<String>();
    for (Node node : nodeList)
    {
      nodeIdList.add(node.getNodeId());
    }
    List<MenuItemCursor> menuItemList = UserSessionBean.getCurrentInstance().
      getMenuModel().getMenuItemsByMid(nodeIdList);
    for (MenuItemCursor menuItem : menuItemList)
    {    
      for (String editRole : menuItem.getEditRoles())
      {
        if (UserSessionBean.getCurrentInstance().isUserInRole(editRole))
        {
          result.add(menuItem.getMid());
        }
      }
    }
    return result;
  }

  private Set<String> getVisibleSections()
  {
    Set<String> result = new HashSet<String>();
    try
    {
      String workspaceId =
        UserSessionBean.getCurrentInstance().getWorkspaceId();
      CMSManagerPort port = CMSConfigBean.getPort();
      NodeFilter nodeFilter = new NodeFilter();
      Property property = new Property();
      property.setName("action");
      property.getValue().add("%newSearchBySection%");
      nodeFilter.getProperty().add(property);
      nodeFilter.getWorkspaceId().add(workspaceId);
      List<Node> nodeList = port.findNodes(nodeFilter);
      List<String> nodeIdList = new ArrayList<String>();
      for (Node node : nodeList)
      {
        nodeIdList.add(node.getNodeId());
      }
      List<MenuItemCursor> menuItemList = UserSessionBean.getCurrentInstance().
        getMenuModel().getMenuItemsByMid(nodeIdList);
      for (MenuItemCursor menuItem : menuItemList)
      {
        result.add(menuItem.getMid());
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return result;
  }

  private boolean isReadingCountIncrementAllowed()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    List<String> restrictedAgentList =
      menuItem.getMultiValuedProperty(NEWS_RESTRICTED_AGENTS);
    if (!restrictedAgentList.isEmpty())
    {
      if (restrictedAgentList.contains(NEWS_RESTRICT_ALL_AGENTS))
        return false;
      HttpServletRequest request =
        (HttpServletRequest)getExternalContext().getRequest();
      String userAgent = request.getHeader("User-Agent");
      if (userAgent != null)
      {
        for (String restrictedAgent : restrictedAgentList)
        {
          if (userAgent.contains(restrictedAgent)) return false;
        }
      }
    }
    return true;
  }

  private void putNewsFilterData(NewsFilter filter) throws Exception
  {
    filter.getNewId().clear();
    if (searchNewId != null && !searchNewId.trim().isEmpty())
    {
      for (String newId : searchNewId.split(";"))
      {
        if (!newId.trim().isEmpty()) filter.getNewId().add(newId.trim());
      }
    }
    if ((filter.getStartDateTime() == null) ||
      (filter.getStartDateTime().trim().isEmpty()))
    {
      filter.setStartDateTime(null);
    }
    if ((filter.getEndDateTime() == null) ||
      (filter.getEndDateTime().trim().isEmpty()))
    {
      filter.setEndDateTime(null);
    }
    if ((filter.getContent() == null) ||
      (filter.getContent().trim().isEmpty()))
    {
      filter.setContent("");
    }
    filter.setContent(NewsConfigBean.formatInputText(searchContent));
    //UserId    
    if (filter.getUserId() != null)
    {
      filter.setUserId(filter.getUserId().trim());
    }    
    if (filter.getUserId() == null || filter.getUserId().isEmpty())
    {
      filter.setUserId(null);
    }
  }

  private String getDocumentServletURL()
  {
    return getContextURL() + DOC_SERVLET_PATH;
  }
  
  private String getDefaultStartDateTime(Date now)
  {
    return getDefaultDateTime(now, DEFAULT_START_DATE_TIME_PROPERTY);
  }  

  private String getDefaultEndDateTime(Date now)
  {
    return getDefaultDateTime(now, DEFAULT_END_DATE_TIME_PROPERTY);
  }
  
  private String getDefaultDateTime(Date now, String dateTimePropertyName)
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(dateTimePropertyName);
    if (value == null || value.equals("now"))
    {
      SimpleDateFormat bigFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      return bigFormat.format(now);
    }      
    else if (value.equals("blank"))
      return "";
    else 
      return value;    
  }

  private boolean[] checkNewSections(NewsManagerClient client, String newId)
  {
    boolean userCanRead = false;
    boolean userCanEdit = false;
    List<NewSection> newSections = client.findNewSectionsFromCache(newId);    
    for (NewSection newSection : newSections)
    {
      try
      {
        MenuItemCursor mic = UserSessionBean.getCurrentInstance().
          getMenuModel().getMenuItemByMid(newSection.getSectionId());
        userCanRead = true;
        if (isEditorUser(mic))
          userCanEdit = true;
        if (userCanRead && userCanEdit) break;
      }
      catch (Exception ex)
      {
        //Non visible node
      }
    }
    return new boolean[]{userCanRead, userCanEdit};
  }
  
  private boolean isEditorUser(MenuItemCursor mic)
  {
    List<String> editRoles =
      mic.getMultiValuedProperty(MenuModel.EDIT_ROLES);
    if (editRoles == null || editRoles.isEmpty()) return true;
    return UserSessionBean.getCurrentInstance().isUserInRole(editRoles);
  }

  private boolean isCustomUrlHeadline(NewView newView)
  {
    String headline = newView.getHeadline();
    return (headline != null && headline.contains(getUrlSeparator()));
  }
}
