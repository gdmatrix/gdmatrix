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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.matrix.news.New;
import org.matrix.news.NewDocument;
import org.matrix.news.NewSection;
import org.matrix.news.NewView;
import org.matrix.news.SectionFilter;
import org.matrix.news.SectionView;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.util.HTMLCharTranslator;
import org.santfeliu.util.MimeTypeMap;
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
public class NewSearchBySectionBean extends BasicSearchBean
{
  @CMSProperty
  public static final String CHILDREN_RENDER_PROPERTY =
    "body.children.render";
  @CMSProperty
  public static final String FILTER_RENDER_PROPERTY =
    "filter.render";
  @CMSProperty
  public static final String FILTER_CONTENT_RENDER_PROPERTY =
    "filter.content.render";
  @CMSProperty
  public static final String FILTER_DATE_RENDER_PROPERTY =
    "filter.date.render";
  @CMSProperty
  public static final String FILTER_USER_RENDER_PROPERTY =
    "filter.user.render";
  @CMSProperty
  public static final String ROOT_HEADLINE_RENDER_PROPERTY =
    "body.root.hline.render";
  @CMSProperty
  public static final String CHILDREN_HEADLINE_RENDER_PROPERTY =
    "body.children.hline.render";
  @CMSProperty
  public static final String ROOT_SUMMARY_RENDER_PROPERTY =
    "body.root.summary.render";
  @CMSProperty
  public static final String CHILDREN_SUMMARY_RENDER_PROPERTY =
    "body.children.summary.render";
  @CMSProperty
  public static final String ROOT_DATE_RENDER_PROPERTY =
    "body.root.date.render";
  @CMSProperty
  public static final String CHILDREN_DATE_RENDER_PROPERTY =
    "body.children.date.render";
  @CMSProperty
  public static final String ROOT_IMAGE_RENDER_PROPERTY =
    "body.root.img.render";
  @CMSProperty
  public static final String CHILDREN_IMAGE_RENDER_PROPERTY =
    "body.children.img.render";
  @CMSProperty
  public static final String ROOT_IMAGE_POSITION_PROPERTY =
    "body.root.img.position";
  @CMSProperty
  public static final String CHILDREN_IMAGE_POSITION_PROPERTY =
    "body.children.img.position";
  @CMSProperty
  public static final String ROOT_IMAGE_WIDTH_PROPERTY =
    "body.root.img.width";
  @CMSProperty
  public static final String CHILDREN_IMAGE_WIDTH_PROPERTY =
    "body.children.img.width";
  @CMSProperty
  public static final String ROOT_IMAGE_HEIGHT_PROPERTY =
    "body.root.img.height";
  @CMSProperty
  public static final String CHILDREN_IMAGE_HEIGHT_PROPERTY =
    "body.children.img.height";
  @CMSProperty
  public static final String ROOT_DOCUMENTS_RENDER_PROPERTY =
    "body.root.docs.render";
  @CMSProperty
  public static final String CHILDREN_DOCUMENTS_RENDER_PROPERTY =
    "body.children.docs.render";
  @CMSProperty
  public static final String COL_NUMBER_PROPERTY =
    "body.colNumber";
  @CMSProperty
  public static final String MAX_NEWS_PER_SECTION_PROPERTY =
    "body.maxNewsPerSection";
  @CMSProperty
  public static final String MAX_NEWS_PER_PAGE_PROPERTY =
    "body.maxNewsPerPage";
  @CMSProperty
  public static final String ORIENTATION_PROPERTY =
    "body.orientation";
  @CMSProperty
  public static final String COPY_SECTION_ID_PROPERTY =
    "copySectionId";
  @CMSProperty
  public static final String SECTION_DESC_PROPERTY =
    "sectionDesc";
  @CMSProperty(mandatory=true)
  public static final String EDIT_NODE_PROPERTY =
    "newSearchMid";
  @CMSProperty
  public static final String HEADER_RENDER_PROPERTY =
    "header.render";
  @CMSProperty
  public static final String HEADER_DOCID_PROPERTY =
    "header.docId";
  @CMSProperty
  public static final String FOOTER_RENDER_PROPERTY =
    "footer.render";
  @CMSProperty
  public static final String FOOTER_DOCID_PROPERTY =
    "footer.docId";
  @CMSProperty
  public static final String ROOT_RESULTBAR_RENDER_PROPERTY =
    "body.root.resultBar.render";
  @CMSProperty
  public static final String ROOT_SECTION_HEADER_RENDER_PROPERTY =
    "body.root.sectionHeader.render";
  @CMSProperty
  public static final String ROOT_READ_LINK_RENDER_PROPERTY =
    "body.root.readLink.render";
  @CMSProperty
  public static final String ROOT_READ_LINK_TEXT_PROPERTY =
    "body.root.readLink.text";
  @CMSProperty
  public static final String ROOT_LINKS_POSITION_PROPERTY =
    "body.root.links.position";
  @CMSProperty
  public static final String CHILDREN_READ_LINK_RENDER_PROPERTY =
    "body.children.readLink.render";
  @CMSProperty
  public static final String CHILDREN_READ_LINK_TEXT_PROPERTY =
    "body.children.readLink.text";
  @CMSProperty
  public static final String CHILDREN_LINKS_POSITION_PROPERTY =
    "body.children.links.position";
  @CMSProperty
  public static final String RSS_ENABLED_PROPERTY =
    "rss.enabled";
  @CMSProperty
  public static final String NEWS_RESTRICTED_AGENTS = "newsRestrictedAgents";
  @CMSProperty
  public static final String DEFAULT_START_DATE_TIME_PROPERTY =
    "newSearch.defaultStartDateTime";
  @CMSProperty
  public static final String DEFAULT_END_DATE_TIME_PROPERTY =
    "newSearch.defaultEndDateTime";
  @CMSProperty
  public static final String URL_SEPARATOR_PROPERTY = "newsURLSeparator";

  @Deprecated
  private static final String SECTION_ID_PROPERTY =
    "sectionId";
  
  private static final String DOC_SERVLET_PATH = "/documents/";
  
  private List<SectionView> childrenSectionViewList;  
  private SetObjectManager setObjectManager; 
  private SectionFilter filter;
  private String lastMid = null;
  
  private String searchContent;
  
  public NewSearchBySectionBean()
  {
    filter = new SectionFilter();
    setObjectManager = new SetObjectManager(filter);
  }
  
  public SectionFilter getFilter()
  {
    return filter;
  }

  public void setFilter(SectionFilter filter)
  {
    this.filter = filter;
  }    

  public String getSearchContent()
  {
    return searchContent;
  }
  
  public void setSearchContent(String searchContent)
  {
    this.searchContent = searchContent;
  }

  public void setChildrenSectionViewList(List<SectionView> 
    childrenSectionViewList)
  {
    this.childrenSectionViewList = childrenSectionViewList;
  }

  public List<SectionView> getChildrenSectionViewList()
  {
    return childrenSectionViewList;
  }

  public int countResults()
  {
    try
    {
      MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
        getMenuModel().getSelectedMenuItem();
      filter.getSectionId().clear();      
      filter.getSectionId().add(getSectionId(menuItem));      
      filter.getExcludeDrafts().clear();
      filter.getExcludeDrafts().add(isRemoveDraftNews(menuItem));      
      return NewsConfigBean.getPort().countNewsBySectionFromCache(filter);
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
      MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
        getMenuModel().getSelectedMenuItem();
      filter.getSectionId().clear();      
      filter.getSectionId().add(getSectionId(menuItem));      
      filter.getExcludeDrafts().clear();
      filter.getExcludeDrafts().add(isRemoveDraftNews(menuItem));      
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      List<NewView> list = NewsConfigBean.getPort().findNewsBySectionFromCache(filter).
        get(0).getNewView(); 
      
      for (NewView nv : list)
      {
        String summary = nv.getSummary();
        if (summary != null)
        {
          nv.setSummary(
            HTMLCharTranslator.toHTMLText(summary, 
            NewsConfigBean.HTML_IGNORED_CHARS));                          
        }
      }
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
  
  public String goToNew()
  {
    try
    {
      MenuItemCursor mic;
      SectionView sectionRow = (SectionView)getFacesContext().
        getExternalContext().getRequestMap().get("sectionRow");
      if (sectionRow == null) //Root new
      {
        mic = UserSessionBean.getCurrentInstance().getMenuModel().
          getSelectedMenuItem();
      }
      else //Child section new
      {
        mic = UserSessionBean.getCurrentInstance().getMenuModel().
          getMenuItemByMid(sectionRow.getSectionId());
      }
      String sectionId = getSectionId(mic);      
      NewView newRow = (NewView)getFacesContext().getExternalContext().
        getRequestMap().get("row");
      String newId = newRow.getNewId();      
      return goToNew(sectionId, newId, newRow.getNewDocument(), false);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String moveUp()
  {
    try
    {
      MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
        getSelectedMenuItem();
      String sectionId = getSectionId(mic);
      NewView newRow = (NewView)getFacesContext().getExternalContext().
        getRequestMap().get("row");
      NewView previousNewRow = getPreviousNewView();
      swapPriority(NewsConfigBean.getPort(), previousNewRow, newRow, sectionId);
      return refresh();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public boolean isMoveUpButtonRender() throws Exception
  {
    int index = (Integer)getFacesContext().getExternalContext().
      getRequestMap().get("rowIndex");
    if (index == 0 || !isRootEditLinkRender()) return false;
    else
    {
      NewView previousNewRow = (NewView)getRows().get(index - 1);
      NewView newRow = (NewView)getFacesContext().getExternalContext().
        getRequestMap().get("row");
      return !(!newRow.isSticky() && previousNewRow.isSticky());
    }
  }
  
  public String moveDown()
  {
    try
    {
      MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
        getSelectedMenuItem();
      String sectionId = getSectionId(mic);
      NewView newRow = (NewView)getFacesContext().getExternalContext().
        getRequestMap().get("row");
      NewView nextNewRow = getNextNewView();
      swapPriority(NewsConfigBean.getPort(), newRow, nextNewRow, sectionId);
      return refresh();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public boolean isMoveDownButtonRender() throws Exception
  {
    int index = (Integer)getFacesContext().getExternalContext().
      getRequestMap().get("rowIndex");
    if (index == getRows().size() - 1 || !isRootEditLinkRender()) return false;
    else
    {
      NewView nextNewRow = (NewView)getRows().get(index + 1);
      NewView newRow = (NewView)getFacesContext().getExternalContext().
        getRequestMap().get("row");
      return !(newRow.isSticky() && !nextNewRow.isSticky());
    }    
  }
  
  public boolean isRootStickyIconRender() throws Exception
  {
    NewView newRow = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    return (newRow.isSticky() && isRootEditLinkRender());
  }  
  
  public boolean isSectionStickyIconRender() throws Exception
  {
    NewView newRow = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    return (newRow.isSticky() && isSectionEditLinkRender());
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
        MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
          getMenuModel().getSelectedMenuItem();
        String sectionId = getSectionId(menuItem);      
        return goToNew(sectionId, newId, true);        
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        getExternalContext().log(ex.getMessage());
      }
      return null;
    }
    else //search news
    {
      boolean nodeChange = 
        !UserSessionBean.getCurrentInstance().getSelectedMid().equals(lastMid);
      boolean reloadDefaults = 
        (nodeChange || (!nodeChange && !isFilterRender()));      
      if (reloadDefaults)
      {
        filter = new SectionFilter();
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
      lastMid = UserSessionBean.getCurrentInstance().getSelectedMid();
      search();
      return "new_search_by_section";
    }
  }
  
  public int getMaxNewsPerSection()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String maxNews = menuItem.getProperty(MAX_NEWS_PER_SECTION_PROPERTY);
    if (maxNews == null) return 3;
    else return Integer.valueOf(maxNews).intValue();    
  }

  public String getNewLink()
  {
    NewView row = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    if (isExternalUrlMode(row))
    {
      String headline = row.getHeadline();
      int idx = headline.lastIndexOf(getUrlSeparator());
      return headline.substring(idx + getUrlSeparator().length());      
    }
    else
    {
      MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
        getMenuModel().getSelectedMenuItem();      
      return getContextPath() + "/go.faces?xmid=" + menuItem.getMid() +
        "&newid=" + row.getNewId();
    }
  }
  
  public String getNewHeadline()
  {
    NewView row = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    if (isExternalUrlMode(row))
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
  
  @Override
  public String search()
  {
    try
    {
      putSectionFilterData(filter);
      if (isChildrenRender()) loadChildrenSections();
      return super.search();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  //Public format methods

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

  public String getNewDate()
  {
    NewView row = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
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

  public List<NewDocument> getExtendedInfoDocList()
  {
    NewView row = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    return NewsConfigBean.getExtendedInfoDocList(row);
  }
  
  public boolean isExtendedInfoDocListEmpty()
  {
    return getExtendedInfoDocList().isEmpty();
  }

  public int getColWidth()
  {
    int colMax = getColNumber();
    int colNumber = childrenSectionViewList.size();
    if (colNumber >= colMax)
    {
      return Math.round(100/colMax);
    }
    else
    {
      return Math.round(100/colNumber);
    }
  }

  public int getColNumber()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String colNumber = menuItem.getProperty(COL_NUMBER_PROPERTY);
    if (colNumber == null) return 1;
    else return Integer.parseInt(colNumber);
  }

  public String getColOrientation()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String colOrientation = menuItem.getProperty(ORIENTATION_PROPERTY);
    if (colOrientation == null) return "horizontal";
    else return colOrientation;
  }

  public boolean isRootHeadlineRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_HEADLINE_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isChildrenHeadlineRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(CHILDREN_HEADLINE_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }
  
  public boolean isRootDateRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_DATE_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isChildrenDateRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(CHILDREN_DATE_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isRootReadLinkRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_READ_LINK_RENDER_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isChildrenReadLinkRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(CHILDREN_READ_LINK_RENDER_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isRootDocumentsRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_DOCUMENTS_RENDER_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isChildrenDocumentsRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(CHILDREN_DOCUMENTS_RENDER_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isRootOnlyImageRender()
  {
    return isRootImageRender() && !isRootSummaryRender(); 
  }
  
  public boolean isRootOnlySummaryRender()
  {
    return !isRootImageRender() && isRootSummaryRender(); 
  }
  
  public boolean isRootImageAndSummaryRenderLeft()
  {
    return isRootImageRender() && isRootSummaryRender() && 
      isRootNewImageLeftPosition(); 
  }

  public boolean isRootImageAndSummaryRenderRight()
  {
    return isRootImageRender() && isRootSummaryRender() && 
      !isRootNewImageLeftPosition(); 
  }

  public boolean isChildrenOnlyImageRender()
  {
    return isChildrenImageRender() && !isChildrenSummaryRender(); 
  }
  
  public boolean isChildrenOnlySummaryRender()
  {
    return !isChildrenImageRender() && isChildrenSummaryRender(); 
  }
  
  public boolean isChildrenImageAndSummaryRenderLeft()
  {
    return isChildrenImageRender() && isChildrenSummaryRender() && 
      isChildrenNewImageLeftPosition(); 
  }

  public boolean isChildrenImageAndSummaryRenderRight()
  {
    return isChildrenImageRender() && isChildrenSummaryRender() && 
      !isChildrenNewImageLeftPosition(); 
  }

  public boolean isFilterRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(FILTER_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");    
  }

  public boolean isFilterContentRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(FILTER_CONTENT_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");    
  }

  public boolean isFilterUserRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(FILTER_USER_RENDER_PROPERTY);
    boolean enabledInNode = 
      (value == null ? true : value.equalsIgnoreCase("true")); 
    try
    {
      return enabledInNode && isRootEditLinkRender();
    }
    catch (Exception ex)
    {
      return false;
    }        
  }
  
  public boolean isFilterDateRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(FILTER_DATE_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");    
  }
  
  public String getNewImageURL()
  {
    NewView newRow = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String docId = NewsConfigBean.getListImageDocId(newRow);
    if ((docId == null) || (docId.length() == 0)) return null;
    else return getContextPath() + DOC_SERVLET_PATH + docId;
  }
  
  public boolean isFutureNew() throws Exception
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    NewView newRow = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    Date newStartDate = df.parse(newRow.getStartDate() + newRow.getStartTime());
    return newStartDate.after(new Date());
  }

  public boolean isExpiredNew() throws Exception
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    NewView newRow = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    Date newEndDate = df.parse(newRow.getEndDate() + newRow.getEndTime());
    return newEndDate.before(new Date());
  }

  public String getRootNewStyleClass() throws Exception
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    NewView newRow = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    Date newStartDate = df.parse(newRow.getStartDate() + newRow.getStartTime());
    Date newEndDate = df.parse(newRow.getEndDate() + newRow.getEndTime());
    if (newStartDate.after(new Date())) //Future
      return "futureNew";
    else if(newEndDate.before(new Date())) //Expired
      return "expiredNew";
    else
      return "rootNew";
  }

  public String getRootNewImageHeight()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(ROOT_IMAGE_HEIGHT_PROPERTY);
  }

  public String getChildrenNewImageHeight()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(CHILDREN_IMAGE_HEIGHT_PROPERTY);
  }
  
  public String getRootNewImageWidth()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(ROOT_IMAGE_WIDTH_PROPERTY);
  }

  public String getChildrenNewImageWidth()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(CHILDREN_IMAGE_WIDTH_PROPERTY);
  }

  public String getRootReadLinkText()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String text = menuItem.getProperty(ROOT_READ_LINK_TEXT_PROPERTY);
    if (text != null) return text;
    else
    {
      Locale locale = getFacesContext().getViewRoot().getLocale();
      ResourceBundle bundle = 
        ResourceBundle.getBundle("org.santfeliu.news.web.resources.NewsBundle", 
        locale);
      return bundle.getString("new_search_expand");
    }
  }

  public String getRootLinksPosition()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String position = menuItem.getProperty(ROOT_LINKS_POSITION_PROPERTY);
    if (position == null) return "right";
    else return position;
  }

  public String getChildrenReadLinkText()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String text = menuItem.getProperty(CHILDREN_READ_LINK_TEXT_PROPERTY);
    if (text != null) return text;
    else
    {
      Locale locale = getFacesContext().getViewRoot().getLocale();
      ResourceBundle bundle = 
        ResourceBundle.getBundle("org.santfeliu.news.web.resources.NewsBundle", 
        locale);
      return bundle.getString("new_search_expand");
    }
  }

  public String getChildrenLinksPosition()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String position = menuItem.getProperty(CHILDREN_LINKS_POSITION_PROPERTY);
    if (position == null) return "right";
    else return position;
  }

  public boolean isRootOnlyDateRender()
  {
    return isRootDateRender() && !isRootHeadlineRender();
  }
  
  public boolean isRootOnlyHeadlineRender()
  {
    return !isRootDateRender() && isRootHeadlineRender();    
  }
  
  public boolean isRootDateAndHeadlineRender()
  {
    return isRootDateRender() && isRootHeadlineRender();    
  }  

  public boolean isRootEditLinkRender() throws Exception
  {
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();  
    if (mic.getProperty(EDIT_NODE_PROPERTY) != null)
    {
      try
      {
        UserSessionBean.getCurrentInstance().getMenuModel().
          getMenuItemByMid(mic.getProperty(EDIT_NODE_PROPERTY));
      }
      catch (Exception ex)
      {
        return false;
      }
      return isEditorUser(mic);
    }
    else return false;
  }

  public boolean isSectionEditLinkRender() throws Exception
  {
    SectionView sectionRow = (SectionView)getFacesContext().
      getExternalContext().getRequestMap().get("sectionRow");
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getMenuItemByMid(sectionRow.getSectionId());
    if (mic.getProperty(EDIT_NODE_PROPERTY) != null)
    {
      try
      {
        UserSessionBean.getCurrentInstance().getMenuModel().
          getMenuItemByMid(mic.getProperty(EDIT_NODE_PROPERTY));
      }
      catch (Exception ex)
      {
        return false;
      }    
      return isEditorUser(mic);
    }
    else return false;    
  }

  public boolean isRootBottomPanelRender() throws Exception
  {
    return (isRootReadLinkRender() || isRootEditLinkRender());
  }

  public boolean isChildrenBottomPanelRender() throws Exception
  {
    return (isChildrenReadLinkRender() || isSectionEditLinkRender());
  }

  public String getDocumentUrl()
  { 
    String url = "";
    NewDocument doc = (NewDocument)getFacesContext().getExternalContext().
      getRequestMap().get("doc");
    if (doc != null)  
    {
      String title = doc.getTitle();
      String mimeType = doc.getMimeType();
      String extension = MimeTypeMap.getMimeTypeMap().getExtension(mimeType);
      String filename = DocumentUtils.getFilename(title) + "." + extension;
      url = getContextPath() + DOC_SERVLET_PATH + doc.getDocumentId() + "/" +
        filename;
    }
    return url;
  }  
  
  public String getMimeTypePath() 
  {
    NewDocument doc = (NewDocument)getFacesContext().getExternalContext().
      getRequestMap().get("doc");
    return DocumentBean.getContentTypeIcon(doc.getMimeType());
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
  
  public boolean isRootResultBarRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_RESULTBAR_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }
  
  public boolean isRootSectionHeaderRender()
  {
    List rows = getRows();
    if ((rows == null) || (rows.isEmpty())) return false;
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_SECTION_HEADER_RENDER_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isChildrenRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(CHILDREN_RENDER_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");
  }

  public String getSectionDesc()
  {
    MenuItemCursor currentMenuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    return getSectionDesc(currentMenuItem);
  }
  
  public String getSectionDesc(MenuItemCursor mic)
  {
    Map nodeProperties = (Map)mic.getDirectProperties();
    if (nodeProperties.containsKey(SECTION_DESC_PROPERTY))
    {
      return (String)nodeProperties.get(SECTION_DESC_PROPERTY);
    }
    else
    {
      return (String)nodeProperties.get("label");
    }
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

  public boolean isRssEnabled()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String propertyValue = menuItem.getProperty(RSS_ENABLED_PROPERTY);
    if ("true".equals(propertyValue))
    {
      return true;
    }
    return false;
  }
  
  public String getRssURL()
  {
    String mid = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem().getMid();
    String language = FacesContext.getCurrentInstance().getViewRoot().
      getLocale().getLanguage();
    return getContextPath() + "/rss/" + mid + "?language=" + language;
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

  private void loadChildrenSections()
  {
    try
    {    
      filter.getSectionId().clear();
      filter.getExcludeDrafts().clear();        
      MenuItemCursor currentMenuItem = UserSessionBean.getCurrentInstance().
        getMenuModel().getSelectedMenuItem();
      if (childrenSectionViewList != null) childrenSectionViewList.clear();
      else childrenSectionViewList = new ArrayList<SectionView>();
      MenuItemCursor firstChild = currentMenuItem.getFirstChild();
      if (firstChild.getMid() != null)
      {        
        MenuItemCursor menuItem = currentMenuItem.getFirstChild();
        while ((menuItem != null) && (menuItem.getMid() != null))
        {
          if (menuItem.getProperty(ACTION_PROPERTY).
            contains("newSearchBySection"))
          {            
            filter.getSectionId().add(getSectionId(menuItem));
            filter.getExcludeDrafts().add(!isEditorUser(menuItem));
          }
          menuItem = menuItem.getNext();                     
        }
        filter.setFirstResult(0);
        filter.setMaxResults(getMaxNewsPerSection());
        
        // Here we get the unsorted list of sections
        List<SectionView> unsortedList = NewsConfigBean.getPort().
          findNewsBySectionFromCache(filter);        
        
        // Now we convert the special chars to HTML
        for (SectionView sv : unsortedList)
        {
          for (NewView nv : sv.getNewView())
          {
            String summary = nv.getSummary();
            if (summary != null)
            {
              nv.setSummary(
                HTMLCharTranslator.toHTMLText(summary, 
                NewsConfigBean.HTML_IGNORED_CHARS));                          
            }
          }
        }
        
        //Now we build the sections-newView Map
        Map sectionMap = new HashMap();
        for (SectionView sv : unsortedList)
        {
          sectionMap.put(sv.getSectionId(), sv.getNewView());
        }
        // And now, we sort it
        menuItem = currentMenuItem.getFirstChild();
        while ((menuItem != null) && (menuItem.getMid() != null))
        {
          if (menuItem.getProperty(ACTION_PROPERTY).
            contains("newSearchBySection"))
          {
            List newViewList = (List<NewView>)(sectionMap.get(
              getSectionId(menuItem)));        
            if ((newViewList != null) && (newViewList.size() > 0))
            {
              SectionView sv = new SectionView();
              sv.setSectionId(getNodeId(menuItem));
              sv.setDesc(getSectionDesc(menuItem));
              sv.getNewView().addAll(newViewList);            
              childrenSectionViewList.add(sv);          
            }
          }
          menuItem = menuItem.getNext();                     
        }              
      }        
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  

  private String getSectionId(MenuItemCursor mic)
  {
    Map nodeProperties = (Map)mic.getDirectProperties();
    if (nodeProperties.containsKey(SECTION_ID_PROPERTY))
    {
      return (String)nodeProperties.get(SECTION_ID_PROPERTY);
    }
    else
    {
      if (nodeProperties.containsKey(COPY_SECTION_ID_PROPERTY))
      {
        return (String)nodeProperties.get(COPY_SECTION_ID_PROPERTY);
      }
      else return mic.getMid();
    }
  }
  
  private String getNodeId(MenuItemCursor mic)
  {
    return mic.getMid();
  }

  private String getDocumentServletURL()
  {
    return getContextURL() + DOC_SERVLET_PATH;
  }

  private boolean isRootSummaryRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_SUMMARY_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  private boolean isChildrenSummaryRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(CHILDREN_SUMMARY_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  private boolean isRootImageRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_IMAGE_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  private boolean isChildrenImageRender()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(CHILDREN_IMAGE_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }
    
  private boolean isRootNewImageLeftPosition()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_IMAGE_POSITION_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("left");    
  }

  private boolean isChildrenNewImageLeftPosition()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(CHILDREN_IMAGE_POSITION_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("left");    
  }
  
  private boolean isEditorUser(MenuItemCursor mic)
  {
    List<String> editRoles =
      mic.getMultiValuedProperty(MenuModel.EDIT_ROLES);
    if (editRoles == null || editRoles.isEmpty()) return true;
    return UserSessionBean.getCurrentInstance().isUserInRole(editRoles);
  }

  private boolean isRemoveDraftNews(MenuItemCursor mic)
  {
    return !isEditorUser(mic);
  }

  private boolean isReadingCountIncrementAllowed()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    List<String> restrictedAgentList =
      menuItem.getMultiValuedProperty(NEWS_RESTRICTED_AGENTS);
    if (!restrictedAgentList.isEmpty())
    {
      if (restrictedAgentList.contains("ALL"))
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

  private void putSectionFilterData(SectionFilter filter)
  {
    //Start date
    if ((filter.getStartDateTime() == null) ||
      (filter.getStartDateTime().trim().isEmpty()))
    {
      filter.setStartDateTime(null);
    }
    //End date
    if ((filter.getEndDateTime() == null) ||
      (filter.getEndDateTime().trim().isEmpty()))
    {
      filter.setEndDateTime(null);
    }
    //Content
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
  
  private String goToNew(String sectionId, String newId, 
    boolean checkVisibility) throws Exception
  {
    return goToNew(sectionId, newId, null, checkVisibility);
  }
  
  private String goToNew(String sectionId, String newId, 
    List<NewDocument> newDocumentList, boolean checkVisibility) throws Exception
  {    
    NewsManagerClient client = NewsConfigBean.getPort();
    New newObject = client.loadNewFromCache(newId);
    
    if (checkVisibility)
    {
      boolean[] check = checkNewSections(client, newId);
      boolean userCanRead = check[0];
      boolean userCanEdit = check[1];
      if (!userCanRead)
        return "new_inaccessible";
      if (newObject.isDraft() && !userCanEdit)
        return "new_inaccessible";
    }

    NewDetailsBean newDetailsBean =
      (NewDetailsBean)getBean("newDetailsBean");
    if (newDetailsBean == null) newDetailsBean = new NewDetailsBean();
    newDetailsBean.setNewObject(newObject);
    newDetailsBean.setReadingCountRender(true);
    int readingCount = newObject.getTotalReadingCount();
    if (isReadingCountIncrementAllowed())
    {
      readingCount = client.incrementNewCounter(newId, sectionId);
    }
    newDetailsBean.setReadingCount(readingCount);
    newDetailsBean.prepareView(client, newDocumentList);
    return "new_details";
  }  
  
  private void swapPriority(NewsManagerClient client, NewView nv1, NewView nv2,
    String sectionId)
  {
    int prio1 = nv1.getPriority();
    int prio2 = nv2.getPriority();

    if (prio1 == prio2) prio1++;

    String newSectionId1 = nv1.getNewId() + ";" + sectionId;
    NewSection ns1 = client.loadNewSectionFromCache(newSectionId1);
    ns1.setPriority(prio2);
    
    String newSectionId2 = nv2.getNewId() + ";" + sectionId;
    NewSection ns2 = client.loadNewSectionFromCache(newSectionId2);
    ns2.setPriority(prio1);
    
    client.storeNewSection(ns1);
    client.storeNewSection(ns2);
  }

  private NewView getPreviousNewView() throws Exception
  {
    int index = (Integer)getFacesContext().getExternalContext().
      getRequestMap().get("rowIndex");
    if (index == 0)
      throw new Exception("INVALID_MOVE_OPERATION");
    else return (NewView)getRows().get(index - 1);
  }

  private NewView getNextNewView() throws Exception
  {
    int index = (Integer)getFacesContext().getExternalContext().
      getRequestMap().get("rowIndex");
    if (index == getRows().size() - 1)
      throw new Exception("INVALID_MOVE_OPERATION");
    else return (NewView)getRows().get(index + 1);
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
  
  private boolean isExternalUrlMode(NewView newView)
  {
    String headline = newView.getHeadline();
    return (headline != null && headline.contains(getUrlSeparator()));
  }
}
