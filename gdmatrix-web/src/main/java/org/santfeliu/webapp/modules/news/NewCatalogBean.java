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
package org.santfeliu.webapp.modules.news;

import com.google.gson.Gson;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.matrix.news.NewDocument;
import org.matrix.news.NewSection;
import org.matrix.news.NewView;
import org.matrix.news.NewsFilter;
import org.matrix.news.SectionFilter;
import org.santfeliu.cms.web.CMSConfigBean;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.util.HTMLCharTranslator;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.util.RequestParameters;
import org.santfeliu.web.obj.util.SetObjectManager;
import org.santfeliu.webapp.helpers.BasicSearchHelper;

/**
 *
 * @author lopezrj-sf
 */
@CMSManagedBean
@Named
@RequestScoped
public class NewCatalogBean extends WebBean implements Serializable
{
  @CMSProperty
  private static final String FILTER_RENDER_PROPERTY =
    "filter.render";
  @CMSProperty
  private static final String FILTER_ID_RENDER_PROPERTY =
    "filter.id.render";
  @CMSProperty
  private static final String FILTER_CONTENT_RENDER_PROPERTY =
    "filter.content.render";
  @CMSProperty
  private static final String FILTER_DATE_RENDER_PROPERTY =
    "filter.date.render";
  @CMSProperty
  private static final String FILTER_USER_RENDER_PROPERTY =
    "filter.user.render";
  @CMSProperty
  private static final String ROOT_HEADLINE_RENDER_PROPERTY =
    "body.root.hline.render";
  @CMSProperty
  private static final String ROOT_SUMMARY_RENDER_PROPERTY =
    "body.root.summary.render";
  @CMSProperty
  private static final String ROOT_DATE_RENDER_PROPERTY =
    "body.root.date.render";
  @CMSProperty
  private static final String ROOT_IMAGE_RENDER_PROPERTY =
    "body.root.img.render";
  @CMSProperty
  private static final String ROOT_IMAGE_POSITION_PROPERTY =
    "body.root.img.position";
  @CMSProperty
  private static final String ROOT_IMAGE_WIDTH_PROPERTY =
    "body.root.img.width";
  @CMSProperty
  private static final String ROOT_IMAGE_HEIGHT_PROPERTY =
    "body.root.img.height";
  @CMSProperty
  private static final String ROOT_DOCUMENTS_RENDER_PROPERTY =
    "body.root.docs.render";
  @CMSProperty
  private static final String MAX_NEWS_PER_PAGE_PROPERTY =
    "body.maxNewsPerPage";
  @CMSProperty
  private static final String SECTION_ID_PROPERTY =
    "sectionId";
  @CMSProperty
  private static final String COPY_SECTION_ID_PROPERTY =
    "copySectionId";
  @CMSProperty
  private static final String SECTION_DESC_PROPERTY =
    "sectionDesc";
/*
  @CMSProperty(mandatory=true)
  private static final String EDIT_NODE_PROPERTY =
    "newSearchMid";
*/
  @CMSProperty
  private static final String HEADER_RENDER_PROPERTY =
    "header.render";
  @CMSProperty
  private static final String HEADER_DOCID_PROPERTY =
    "header.docId";
  @CMSProperty
  private static final String FOOTER_RENDER_PROPERTY =
    "footer.render";
  @CMSProperty
  private static final String FOOTER_DOCID_PROPERTY =
    "footer.docId";
/*
  @CMSProperty
  private static final String ROOT_RESULTBAR_RENDER_PROPERTY =
    "body.root.resultBar.render";
*/
  @CMSProperty
  private static final String ROOT_SECTION_HEADER_RENDER_PROPERTY =
    "body.root.sectionHeader.render";
  @CMSProperty
  private static final String ROOT_READ_LINK_RENDER_PROPERTY =
    "body.root.readLink.render";
  @CMSProperty
  private static final String ROOT_READ_LINK_TEXT_PROPERTY =
    "body.root.readLink.text";
  @CMSProperty
  private static final String ROOT_LINKS_POSITION_PROPERTY =
    "body.root.links.position";
  @CMSProperty
  private static final String RSS_ENABLED_PROPERTY =
    "rss.enabled";
  @CMSProperty
  private static final String NEWS_RESTRICTED_AGENTS = "newsRestrictedAgents";
  @CMSProperty
  private static final String DEFAULT_START_DATE_TIME_PROPERTY =
    "newSearch.defaultStartDateTime";
  @CMSProperty
  private static final String DEFAULT_END_DATE_TIME_PROPERTY =
    "newSearch.defaultEndDateTime";
  @CMSProperty
  private static final String URL_SEPARATOR_PROPERTY = "newsURLSeparator";
  @CMSProperty
  private static final String AUTO_SECTION_ENABLED_PROPERTY =
    "newsAutoSectionEnabled";

  //New property
  @CMSProperty
  private static final String VIEW_MODE_PROPERTY =
    "viewMode";

  private static final String DOC_SERVLET_PATH = "/documents/";
  private static final int DEFAULT_PAGE_SIZE = 10;

  private static final String OUTCOME_SEARCH = "/pages/news/new_catalog.xhtml";
  private static final String OUTCOME_VIEW = "/pages/news/new_viewer.xhtml";

  private SetObjectManager setObjectManager;
  private String lastMid = null;

  //NewSearchBySectionBean mode
  private SectionFilter sectionFilter;

  //NewSearchBean mode
  private NewsFilter newsFilter;
  private Set<String> visibleSections;
  private Set<String> editableSections;

  //Filter box fields
  private String searchNewId;
  private String searchContent;
  private Date searchStartDate;
  private Date searchEndDate;
  private String searchUserId;

  private String content;

  private BasicSearchHelper basicSearchHelper;

  private enum ViewMode
  {
    BYSECTION,
    EDITABLE,
    VISIBLE
  }

  @Inject
  NewObjectBean newObjectBean;

  @Inject
  NewViewerBean newViewerBean;

  @PostConstruct
  public void init()
  {
    sectionFilter = new SectionFilter();
    newsFilter = new NewsFilter();
    assignObjectManager();
    basicSearchHelper = new BasicSearchHelper()
    {
      @Override
      public int countResults()
      {
        try
        {
          if (getViewMode() == ViewMode.BYSECTION)
          {
            MenuItemCursor menuItem = getSelectedMenuItem();
            sectionFilter.getSectionId().clear();
            sectionFilter.getSectionId().add(getSectionId(menuItem));
            sectionFilter.getExcludeDrafts().clear();
            sectionFilter.getExcludeDrafts().add(isRemoveDraftNews(menuItem));
            return NewsModuleBean.getClient().countNewsBySectionFromCache(sectionFilter);
          }
          else if (getViewMode() == ViewMode.EDITABLE)
          {
            newsFilter.getSectionId().clear();
            newsFilter.getSectionId().addAll(getEditableSections());
            newsFilter.setExcludeDrafts(false);
            newsFilter.setExcludeNotPublished(false);
            return NewsModuleBean.getClient().countNewsFromCache(newsFilter);
          }
          else if (getViewMode() == ViewMode.VISIBLE)
          {
            newsFilter.getSectionId().clear();
            newsFilter.getSectionId().addAll(getFilteredVisibleSections());
            newsFilter.setExcludeDrafts(true);
            newsFilter.setExcludeNotPublished(true);
            return NewsModuleBean.getClient().countNewsFromCache(newsFilter);
          }
        }
        catch (Exception ex)
        {
          error(ex);
        }
        return 0;
      }

      @Override
      public List getResults(int firstResult, int maxResults)
      {
        try
        {
          List<NewView> list = new ArrayList<>();
          if (getViewMode() == ViewMode.BYSECTION)
          {
            MenuItemCursor menuItem = getSelectedMenuItem();
            sectionFilter.getSectionId().clear();
            sectionFilter.getSectionId().add(getSectionId(menuItem));
            sectionFilter.getExcludeDrafts().clear();
            sectionFilter.getExcludeDrafts().add(isRemoveDraftNews(menuItem));
            sectionFilter.setFirstResult(firstResult);
            sectionFilter.setMaxResults(maxResults);
            list = NewsModuleBean.getClient().
              findNewsBySectionFromCache(sectionFilter).get(0).getNewView();
          }
          else if (getViewMode() == ViewMode.EDITABLE)
          {
            newsFilter.getSectionId().clear();
            newsFilter.getSectionId().addAll(getEditableSections());
            newsFilter.setExcludeDrafts(false);
            newsFilter.setExcludeNotPublished(false);
            newsFilter.setFirstResult(firstResult);
            newsFilter.setMaxResults(maxResults);
            list = NewsModuleBean.getClient().findNewViewsFromCache(newsFilter);
          }
          else if (getViewMode() == ViewMode.VISIBLE)
          {
            newsFilter.getSectionId().clear();
            newsFilter.getSectionId().addAll(getFilteredVisibleSections());
            newsFilter.setExcludeDrafts(true);
            newsFilter.setExcludeNotPublished(true);
            newsFilter.setFirstResult(firstResult);
            newsFilter.setMaxResults(maxResults);
            list = NewsModuleBean.getClient().findNewViewsFromCache(newsFilter);
          }
          for (NewView nv : list)
          {
            String summary = nv.getSummary();
            if (summary != null)
            {
              nv.setSummary(HTMLCharTranslator.toHTMLText(summary,
                NewsModuleBean.HTML_IGNORED_CHARS));
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
    };
  }

  public SectionFilter getSectionFilter()
  {
    return sectionFilter;
  }

  public void setSectionFilter(SectionFilter sectionFilter)
  {
    this.sectionFilter = sectionFilter;
  }

  public NewsFilter getNewsFilter()
  {
    return newsFilter;
  }

  public void setNewsFilter(NewsFilter newsFilter)
  {
    this.newsFilter = newsFilter;
  }

  public Set<String> getVisibleSections()
  {
    if (visibleSections == null)
    {
      visibleSections = calcVisibleSections();
    }
    return visibleSections;
  }

  public void setVisibleSections(Set<String> visibleSections)
  {
    this.visibleSections = visibleSections;
  }

  public Set<String> getEditableSections()
  {
    if (editableSections == null)
    {
      editableSections = calcEditableSections();
    }
    return editableSections;
  }

  public void setEditableSections(Set<String> editableSections)
  {
    this.editableSections = editableSections;
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

  public Date getSearchStartDate()
  {
    return searchStartDate;
  }

  public void setSearchStartDate(Date searchStartDate)
  {
    this.searchStartDate = searchStartDate;
  }

  public Date getSearchEndDate()
  {
    return searchEndDate;
  }

  public void setSearchEndDate(Date searchEndDate)
  {
    this.searchEndDate = searchEndDate;
  }

  public String getSearchUserId()
  {
    return searchUserId;
  }

  public void setSearchUserId(String searchUserId)
  {
    this.searchUserId = searchUserId;
  }

  public String getContent()
  {
    return content;
  }

  public void setContent(String content)
  {
    this.content = content;
  }

  public BasicSearchHelper getBasicSearchHelper()
  {
    return basicSearchHelper;
  }

  public void setBasicSearchHelper(BasicSearchHelper basicSearchHelper)
  {
    this.basicSearchHelper = basicSearchHelper;
  }

  @Override
  public Map getProperties()
  {
    return super.getProperties();
  }

  // action methods
  @CMSAction
  public String show()
  {
    setContent(OUTCOME_SEARCH);
    if (isAllowedNode())
    {
      Map requestParameters = getExternalContext().getRequestParameterMap();
      String newId = (String)requestParameters.get("newid");
      if (newId != null) //jump to new
      {
        try
        {
          MenuItemCursor menuItem = getSelectedMenuItem();
          String sectionId = null;
          if (getViewMode() == ViewMode.BYSECTION)
          {
            sectionId = getSectionId(menuItem);
          }
          goToNew(sectionId, newId, null, true);
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
      else //search news
      {
        newObjectBean.setAutoSectionId(null);
        boolean nodeChange =
          !UserSessionBean.getCurrentInstance().getSelectedMid().equals(lastMid);
        boolean reloadDefaults =
          (nodeChange || (!nodeChange && !isFilterRender()));
        if (reloadDefaults)
        {
          resetFilter();
        }
        readRequestParameters();
        lastMid = UserSessionBean.getCurrentInstance().getSelectedMid();
        search();
      }
    }
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  public boolean isAllowedNode()
  {
    return !(getViewMode() == ViewMode.BYSECTION && !isSectionReadable());
  }

  public void clearFilters()
  {
    resetFilter(); // Reset base filter
    search();
  }

  public String search()
  {
    try
    {
      if (getViewMode() == ViewMode.BYSECTION)
      {
        putSectionFilterData(sectionFilter);
      }
      else
      {
        putNewsFilterData(newsFilter);
      }
      return basicSearchHelper.search();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String getCreateNewOnclick()
  {
    if (isAutoSectionEnabled())
    {
      MenuItemCursor menuItem = getSelectedMenuItem();
      String sectionId = getSectionId(menuItem);
      return "return showObject('New', '', {autoSectionId:'" + sectionId + "'})";
    }
    else
    {
      return "return showObject('New', '')";
    }
  }

  public String goToNew()
  {
    try
    {
      MenuItemCursor mic = getSelectedMenuItem();
      String sectionId = null;
      List<NewDocument> newDocumentList = null;
      NewView newRow = (NewView)getFacesContext().getExternalContext().
        getRequestMap().get("row");
      if (getViewMode() == ViewMode.BYSECTION)
      {
        sectionId = getSectionId(mic);
        newDocumentList = newRow.getNewDocument();
      }
      String newId = newRow.getNewId();
      goToNew(sectionId, newId, newDocumentList, false);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  public String moveUp()
  {
    try
    {
      MenuItemCursor mic = getSelectedMenuItem();
      String sectionId = getSectionId(mic);
      NewView newRow = (NewView)getFacesContext().getExternalContext().
        getRequestMap().get("row");
      NewView previousNewRow = getPreviousNewView();
      swapPriority(NewsModuleBean.getClient(), previousNewRow, newRow, sectionId);
      return basicSearchHelper.refresh();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String moveDown()
  {
    try
    {
      MenuItemCursor mic = getSelectedMenuItem();
      String sectionId = getSectionId(mic);
      NewView newRow = (NewView)getFacesContext().getExternalContext().
        getRequestMap().get("row");
      NewView nextNewRow = getNextNewView();
      swapPriority(NewsModuleBean.getClient(), newRow, nextNewRow, sectionId);
      return basicSearchHelper.refresh();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public boolean isMoveUpButtonRender() throws Exception
  {
    if (getViewMode() != ViewMode.BYSECTION) return false;

    int index = (Integer)getFacesContext().getExternalContext().
      getRequestMap().get("rowIndex");
    if (index == 0 || !isRootEditLinkRender()) return false;
    else
    {
      NewView previousNewRow = (NewView)basicSearchHelper.getRows().get(index - 1);
      NewView newRow = (NewView)getFacesContext().getExternalContext().
        getRequestMap().get("row");
      return !(!newRow.isSticky() && previousNewRow.isSticky());
    }
  }

  public boolean isMoveDownButtonRender() throws Exception
  {
    if (getViewMode() != ViewMode.BYSECTION) return false;

    int index = (Integer)getFacesContext().getExternalContext().
      getRequestMap().get("rowIndex");
    if (index == basicSearchHelper.getRows().size() - 1 || !isRootEditLinkRender()) return false;
    else
    {
      NewView nextNewRow = (NewView)basicSearchHelper.getRows().get(index + 1);
      NewView newRow = (NewView)getFacesContext().getExternalContext().
        getRequestMap().get("row");
      return !(newRow.isSticky() && !nextNewRow.isSticky());
    }
  }

  public boolean isRootEditLinkRender() throws Exception
  {
    MenuItemCursor mic = getSelectedMenuItem();
    if (getViewMode() == ViewMode.BYSECTION)
    {
      return NewsModuleBean.isEditorUser(mic);
    }
    else if (getViewMode() == ViewMode.EDITABLE)
    {
      return true;
    }
    else if (getViewMode() == ViewMode.VISIBLE)
    {
      return false;
    }
    return false;
  }

  public boolean isHeaderRender()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(HEADER_RENDER_PROPERTY);
    if (value == null) return false;
    else return "true".equalsIgnoreCase(value);
  }

  public boolean isFooterRender()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(FOOTER_RENDER_PROPERTY);
    if (value == null) return false;
    else return "true".equalsIgnoreCase(value);
  }

  public String getHeaderUrl()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String headerDocId = menuItem.getProperty(HEADER_DOCID_PROPERTY);
    return getDocumentServletURL() + headerDocId;
  }

  public String getFooterUrl()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String footerDocId = menuItem.getProperty(FOOTER_DOCID_PROPERTY);
    return getDocumentServletURL() + footerDocId;
  }

  public boolean isFilterRender()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(FILTER_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isFilterIdRender()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(FILTER_ID_RENDER_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isFilterContentRender()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(FILTER_CONTENT_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isFilterUserRender()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
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
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(FILTER_DATE_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isRssEnabled()
  {
    if (getViewMode() != ViewMode.BYSECTION) return false;

    MenuItemCursor menuItem = getSelectedMenuItem();
    String propertyValue = menuItem.getProperty(RSS_ENABLED_PROPERTY);
    return "true".equals(propertyValue);
  }

  public String getRssURL()
  {
    MenuItemCursor mic = getSelectedMenuItem();
    String language = FacesContext.getCurrentInstance().getViewRoot().
      getLocale().getLanguage();
    return getContextPath() + "/rss/" + getSectionId(mic) + "?language=" + language;
  }

  public String getSectionDesc()
  {
    MenuItemCursor currentMenuItem = getSelectedMenuItem();
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

  public int getPageSize()
  {
    int superPageSize = basicSearchHelper.getPageSize();
    if (superPageSize != DEFAULT_PAGE_SIZE)
      return superPageSize;

    MenuItemCursor mic = getSelectedMenuItem();
    String rowsPerPage = mic.getProperty(MAX_NEWS_PER_PAGE_PROPERTY);
    if (rowsPerPage != null) return Integer.parseInt(rowsPerPage);
    else return DEFAULT_PAGE_SIZE;
  }

  public boolean isFutureNew() throws Exception
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    NewView newRow = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    Date newStartDate = df.parse(getNewViewStartDateTime(newRow));
    return newStartDate.after(new Date());
  }

  public boolean isExpiredNew() throws Exception
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    NewView newRow = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    Date newEndDate = df.parse(getNewViewEndDateTime(newRow));
    return newEndDate.before(new Date());
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
      catch (ParseException ex)
      {
        error(ex);
      }
    }
    return "";
  }

  public String getNewLink()
  {
    NewView row = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String customUrl = getCustomUrl(row);
    return (customUrl != null ? customUrl : "#");
  }

  public String getNewOnclick()
  {
    NewView row = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    if (getCustomUrl(row) == null)
    {
      return "return showPage('New', " + getJsonParameters(row) + ")";
    }
    else return "";
  }

  public boolean isNewCustomUrl()
  {
    NewView row = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    return (getCustomUrl(row) != null);
  }

  public String getNewLinkTarget()
  {
    NewView row = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    if ((isCustomUrlHeadline(row) || row.getCustomUrl() != null) &&
      row.getCustomUrlTarget() != null)
    {
      return row.getCustomUrlTarget();
    }
    else
    {
      return "_self";
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

  public String getNewImageURL()
  {
    NewView newRow = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String docId = NewsModuleBean.getListImageDocId(newRow);
    if ((docId == null) || (docId.length() == 0)) return null;
    else return getContextPath() + DOC_SERVLET_PATH + docId;
  }

  public String getRootNewImageHeight()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    return menuItem.getProperty(ROOT_IMAGE_HEIGHT_PROPERTY);
  }

  public String getRootNewImageWidth()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    return menuItem.getProperty(ROOT_IMAGE_WIDTH_PROPERTY);
  }

  public List<NewDocument> getExtendedInfoDocList()
  {
    NewView row = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    return NewsModuleBean.getExtendedInfoDocList(row);
  }

  public boolean isRootStickyIconRender() throws Exception
  {
    NewView newRow = (NewView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    return (newRow.isSticky() && isRootEditLinkRender());
  }

  public boolean isRootHeadlineRender()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_HEADLINE_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isRootSummaryRender()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_SUMMARY_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isRootDateRender()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_DATE_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  public boolean isRootImageRender()
  {
    if (getViewMode() != ViewMode.BYSECTION) return false;

    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_IMAGE_RENDER_PROPERTY);
    if (value == null) return true;
    else return value.equalsIgnoreCase("true");
  }

  //TODO Use this property to set image position in new_catalog
  public String getRootImagePosition()
  {
    if (isRootImageRender())
    {
      MenuItemCursor menuItem = getSelectedMenuItem();
      String value = menuItem.getProperty(ROOT_IMAGE_POSITION_PROPERTY);
      if (value != null) return value;
    }
    return "";
  }

  public boolean isRootDocumentsRender()
  {
    if (getViewMode() != ViewMode.BYSECTION) return false;

    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_DOCUMENTS_RENDER_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");
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

  public String getRootLinksPosition()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String position = menuItem.getProperty(ROOT_LINKS_POSITION_PROPERTY);
    if (position == null) return "right";
    else return position;
  }

  public boolean isRootBottomPanelRender() throws Exception
  {
    return (isRootReadLinkRender() || isRootEditLinkRender());
  }

  public boolean isRootReadLinkRender()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_READ_LINK_RENDER_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");
  }

  public String getRootReadLinkText()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String text = menuItem.getProperty(ROOT_READ_LINK_TEXT_PROPERTY);
    if (text != null)
    {
      return translate(text);
    }
    else
    {
      return translate("$$newsBundle.new_search_expand");
    }
  }

  public boolean isRootSectionHeaderRender()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(ROOT_SECTION_HEADER_RENDER_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");
  }

  public String getRssButtonTitle()
  {
    String text = "Accedir al canal RSS de " + getSectionDesc();
    return translate(text);
  }

  public boolean isSectionReadable()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String sectionId = getSectionId(menuItem);
    try
    {
      UserSessionBean.getCurrentInstance().getMenuModel().
        getMenuItemByMid(sectionId);
      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public String getUrlSeparator()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String urlSeparator = menuItem.getProperty(URL_SEPARATOR_PROPERTY);
    return (urlSeparator == null ? "###" : urlSeparator);
  }

  public String getJsonParameters(NewView newView)
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    Map<String, String> params = new HashMap();
    params.put("newid", newView.getNewId());
    params.put("sectionid", getSectionId(menuItem));
    return new Gson().toJson(params);
  }

  public boolean isAutoSectionEnabled()
  {
    if (getViewMode() != ViewMode.BYSECTION) return false;

    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(AUTO_SECTION_ENABLED_PROPERTY);
    if (value == null) return false;
    else return value.equalsIgnoreCase("true");
  }

  public String getArticleAriaLabel()
  {
    return translate(getNewHeadline()) + ", " + getNewDate();
  }

  public String getNewAriaLabel()
  {
    if ("_blank".equals(getNewLinkTarget()))
    {
      String openNewWindowLabel =
        MatrixConfig.getProperty("org.santfeliu.web.OpenNewWindowLabel");
      String trOpenNewWindowLabel = translate(openNewWindowLabel);
      return translate(getNewHeadline()) + " (" + trOpenNewWindowLabel + ")";
    }
    return null;
  }

  public String getDocumentAriaLabel()
  {
    NewDocument doc = (NewDocument)getFacesContext().getExternalContext().
      getRequestMap().get("doc");
    String openNewWindowLabel =
      MatrixConfig.getProperty("org.santfeliu.web.OpenNewWindowLabel");
    String trOpenNewWindowLabel = translate(openNewWindowLabel);
    return doc.getTitle() + " (" + trOpenNewWindowLabel + ")";
  }

  public String getRootReadLinkAriaLabel()
  {
    return getRootReadLinkText() + ": " + translate(getNewHeadline());
  }

  //PRIVATE METHODS

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

  private Date getDefaultStartDate(Date now)
  {
    return getDefaultDate(now, DEFAULT_START_DATE_TIME_PROPERTY);
  }

  private Date getDefaultEndDate(Date now)
  {
    return getDefaultDate(now, DEFAULT_END_DATE_TIME_PROPERTY);
  }

  private Date getDefaultDate(Date now, String dateTimePropertyName)
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(dateTimePropertyName);
    if (value == null || value.equals("now"))
    {
      return now;
    }
    else if (value.equals("blank"))
    {
      return null;
    }
    else
    {
      try
      {
        SimpleDateFormat bigFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return bigFormat.parse(value);
      }
      catch (ParseException ex)
      {
        return null;
      }
    }
  }

  private String getNewViewStartDateTime(NewView newView)
  {
    return newView.getStartDate() +
      (newView.getStartTime() != null ? newView.getStartTime() : "000000");
  }

  private String getNewViewEndDateTime(NewView newView)
  {
    return newView.getEndDate() +
      (newView.getEndTime() != null ? newView.getEndTime() : "000000");
  }

  private String getDocumentServletURL()
  {
    return getContextURL() + DOC_SERVLET_PATH;
  }

  private boolean isCustomUrlHeadline(NewView newView)
  {
    String headline = newView.getHeadline();
    return (headline != null && headline.contains(getUrlSeparator()));
  }

  private void putSectionFilterData(SectionFilter filter)
  {
    //New Id
    filter.getNewId().clear();
    if (searchNewId != null && !searchNewId.trim().isEmpty())
    {
      for (String newId : searchNewId.split(";"))
      {
        if (!newId.trim().isEmpty()) filter.getNewId().add(newId.trim());
      }
    }

    //Start date
    if (searchStartDate != null)
    {
      filter.setStartDateTime(
        TextUtils.formatDate(searchStartDate, "yyyyMMddHHmmss"));
    }
    else
      filter.setStartDateTime(null);

    //End date
    if (searchEndDate != null)
    {
      filter.setEndDateTime(
        TextUtils.formatDate(searchEndDate, "yyyyMMddHHmmss"));
    }
    else
      filter.setEndDateTime(null);

    //Content
    if (searchContent != null)
    {
      filter.setContent(NewsModuleBean.formatInputText(searchContent));
    }
    else
      filter.setContent("");

    //UserId
    if (searchUserId != null)
    {
      filter.setUserId(searchUserId.trim());
    }
    else
    {
      filter.setUserId(null);
    }
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
    if (searchStartDate != null)
    {
      filter.setStartDateTime(
        TextUtils.formatDate(searchStartDate, "yyyyMMddHHmmss"));
    }
    else
      filter.setStartDateTime(null);

    if (searchEndDate != null)
    {
      filter.setEndDateTime(
        TextUtils.formatDate(searchEndDate, "yyyyMMddHHmmss"));
    }
    else
      filter.setEndDateTime(null);

    if (searchContent != null)
    {
      filter.setContent(NewsModuleBean.formatInputText(searchContent));
    }
    else
      filter.setContent("");

    //UserId
    if (searchUserId != null)
    {
      filter.setUserId(searchUserId.trim());
    }
    else
    {
      filter.setUserId(null);
    }

  }

  private RequestParameters getRequestParameters()
  {
    RequestParameters parameters = new RequestParameters();

    Map requestMap = getExternalContext().getRequestParameterMap();
    HttpServletRequest request =
      (HttpServletRequest)getExternalContext().getRequest();
    String qs = request.getQueryString();

    for (Object key : requestMap.keySet())
    {
      String skey = String.valueOf(key);
      String svalue = String.valueOf(requestMap.get(key));
      parameters.add(skey, svalue, qs);
    }

    return parameters;
  }

  private boolean isRemoveDraftNews(MenuItemCursor mic)
  {
    return !NewsModuleBean.isEditorUser(mic);
  }

  private void resetFilter()
  {
    searchNewId = null;
    searchContent = null;
    Date now = new Date();
    searchStartDate = getDefaultStartDate(now);
    searchEndDate = getDefaultEndDate(now);
    searchUserId = null;

    //Reset newsFilter
    newsFilter.getNewId().clear();
    newsFilter.setContent(null);
    newsFilter.setStartDateTime(null);
    newsFilter.setEndDateTime(null);
    newsFilter.setUserId(null);

    //Reset sectionFilter
    sectionFilter.getNewId().clear();
    sectionFilter.setContent(null);
    sectionFilter.setStartDateTime(null);
    sectionFilter.setEndDateTime(null);
    sectionFilter.setUserId(null);
  }

  private String translate(String text)
  {
    return ApplicationBean.getCurrentInstance().translate(text);
  }

  private ViewMode getViewMode()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String value = menuItem.getProperty(VIEW_MODE_PROPERTY);
    if (value == null)
      return ViewMode.BYSECTION;
    else return
      ViewMode.valueOf(value);
  }

  private void assignObjectManager()
  {
    if (getViewMode() == ViewMode.BYSECTION)
    {
      setObjectManager = new SetObjectManager(sectionFilter);
    }
    else
    {
      setObjectManager = new SetObjectManager(newsFilter);
    }
  }

  private Set<String> calcEditableSections()
  {
    Set<String> result = new HashSet<>();
    List<String> nodeIdList = getNewSectionNodes();
    //Filter nodes
    List<MenuItemCursor> menuItemList = UserSessionBean.getCurrentInstance().
      getMenuModel().getMenuItemsByMid(nodeIdList);
    for (MenuItemCursor menuItem : menuItemList)
    {
      if (NewsModuleBean.isEditorUser(menuItem))
      {
        result.add(menuItem.getMid());
      }
    }
    return result;
  }

  private Set<String> calcVisibleSections()
  {
    Set<String> result = new HashSet<>();
    List<String> nodeIdList = getNewSectionNodes();
    //Filter nodes
    List<MenuItemCursor> menuItemList = UserSessionBean.getCurrentInstance().
      getMenuModel().getMenuItemsByMid(nodeIdList);
    for (MenuItemCursor menuItem : menuItemList)
    {
      result.add(menuItem.getMid());
    }
    return result;
  }

  private List<String> getNewSectionNodes()
  {
    List<String> nodeIdList = new ArrayList<>();
    String workspaceId =
      UserSessionBean.getCurrentInstance().getWorkspaceId();
    CMSManagerPort port = CMSConfigBean.getPort();
    //Old property
    NodeFilter nodeFilter = new NodeFilter();
    Property property = new Property();
    property.setName("action");
    property.getValue().add("%newSearchBySection%");
    nodeFilter.getProperty().add(property);
    nodeFilter.getWorkspaceId().add(workspaceId);
    List<Node> nodeList = port.findNodes(nodeFilter);
    for (Node node : nodeList)
    {
      nodeIdList.add(node.getNodeId());
    }
    //New property
    nodeFilter = new NodeFilter();
    property = new Property();
    property.setName("action");
    property.getValue().add("%newCatalogBean%");
    nodeFilter.getProperty().add(property);
    nodeFilter.getWorkspaceId().add(workspaceId);
    nodeList = port.findNodes(nodeFilter);
    for (Node node : nodeList)
    {
      nodeIdList.add(node.getNodeId());
    }
    return nodeIdList;
  }

  private List<String> getFilteredVisibleSections()
  {
    List<String> result = new ArrayList<>();
    MenuItemCursor menuItem = getSelectedMenuItem();
    List<String> sectionIds = menuItem.getMultiValuedProperty(
      SECTION_ID_PROPERTY);
    if (sectionIds.isEmpty())
    {
      result.addAll(getVisibleSections());
    }
    else
    {
      for (String sectionId : sectionIds)
      {
        if (getVisibleSections().contains(sectionId))
        {
          result.add(sectionId);
        }
      }
    }
    return result;
  }

  private void readRequestParameters()
  {
    setObjectManager.execute(getRequestParameters());
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    if (getViewMode() == ViewMode.BYSECTION)
    {
      if (!sectionFilter.getNewId().isEmpty())
      {
        searchNewId = sectionFilter.getNewId().get(0);
      }
      if (sectionFilter.getContent() != null)
      {
        searchContent = sectionFilter.getContent().replace("%", " ");
      }
      if (sectionFilter.getStartDateTime() != null)
      {
        try
        {
          searchStartDate = df.parse(sectionFilter.getStartDateTime());
        }
        catch (Exception ex) { }
      }
      if (sectionFilter.getEndDateTime() != null)
      {
        try
        {
          searchEndDate = df.parse(sectionFilter.getEndDateTime());
        }
        catch (Exception ex) { }
      }
      if (sectionFilter.getUserId() != null)
      {
        searchUserId = sectionFilter.getUserId();
      }
    }
    else
    {
      if (!newsFilter.getNewId().isEmpty())
      {
        searchNewId = newsFilter.getNewId().get(0);
      }
      if (newsFilter.getContent() != null)
      {
        searchContent = newsFilter.getContent().replace("%", " ");
      }
      if (newsFilter.getStartDateTime() != null)
      {
        try
        {
          searchStartDate = df.parse(newsFilter.getStartDateTime());
        }
        catch (ParseException ex) { }
      }
      if (newsFilter.getEndDateTime() != null)
      {
        try
        {
          searchEndDate = df.parse(newsFilter.getEndDateTime());
        }
        catch (ParseException ex) { }
      }
      if (newsFilter.getUserId() != null)
      {
        searchUserId = newsFilter.getUserId();
      }
    }
  }

  private void goToNew(String sectionId, String newId,
    List<NewDocument> newDocumentList, boolean checkVisibility) throws Exception
  {
    newViewerBean.loadNew(sectionId, newId, newDocumentList, checkVisibility,
      isReadingCountIncrementAllowed());
    setContent(OUTCOME_VIEW);
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
    else return (NewView)basicSearchHelper.getRows().get(index - 1);
  }

  private NewView getNextNewView() throws Exception
  {
    int index = (Integer)getFacesContext().getExternalContext().
      getRequestMap().get("rowIndex");
    if (index == basicSearchHelper.getRows().size() - 1)
      throw new Exception("INVALID_MOVE_OPERATION");
    else return (NewView)basicSearchHelper.getRows().get(index + 1);
  }

  private boolean isReadingCountIncrementAllowed()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
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

  //URL in headline with headline###URL as format, or in 'customUrl' New prop.
  private String getCustomUrl(NewView row)
  {
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
        return null; //no custom URL
      }
    }
  }

}
