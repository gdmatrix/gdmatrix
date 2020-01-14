package org.santfeliu.search.web;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.matrix.search.AgendaFilter;
import org.matrix.search.Item;
import org.matrix.search.NewsFilter;
import org.matrix.search.DocFilter;
import org.matrix.search.WebFilter;
import org.matrix.search.GlobalSearchFilter;
import org.matrix.search.SearchManagerPort;
import org.matrix.search.SearchManagerService;
import org.matrix.search.GlobalSearchResults;
import org.matrix.search.ItemType;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.doc.web.DocumentBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.template.Template;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

@CMSManagedBean
public class SearchBean extends WebBean implements Serializable
{
  //Config node properties
  @CMSProperty
  public static final String AGENDA_SEARCH_DAYS_PROPERTY =
    "globalSearch.agenda.searchDays";
  @CMSProperty
  public static final String AGENDA_MAX_ROWS_PROPERTY =
    "globalSearch.agenda.maxRows";
  @CMSProperty
  public static final String AGENDA_THEME_PROPERTY =
    "globalSearch.agenda.theme";
  @CMSProperty(mandatory=true)
  public static final String AGENDA_VIEW_NODE_PROPERTY =
    "globalSearch.agenda.viewNode";
  @CMSProperty
  public static final String NEWS_SEARCH_DAYS_PROPERTY =
    "globalSearch.news.searchDays";
  @CMSProperty
  public static final String NEWS_MAX_ROWS_PROPERTY =
    "globalSearch.news.maxRows";
  @CMSProperty
  public static final String NEWS_SECTION_PROPERTY =
    "globalSearch.news.section";
  @CMSProperty(mandatory=true)
  public static final String NEWS_VIEW_NODE_PROPERTY =
    "globalSearch.news.viewNode";
  @CMSProperty
  public static final String DOC_SEARCH_DAYS_PROPERTY =
    "globalSearch.doc.searchDays";
  @CMSProperty
  public static final String DOC_MAX_ROWS_PROPERTY =
    "globalSearch.doc.maxRows";
  @CMSProperty
  public static final String WEB_SEARCH_DAYS_PROPERTY =
    "globalSearch.web.searchDays";
  @CMSProperty
  public static final String WEB_MAX_ROWS_PROPERTY =
    "globalSearch.web.maxRows";
  @CMSProperty
  public static final String WEB_ROOT_PROPERTY =
    "globalSearch.web.root";
  @CMSProperty
  public static final String WEB_EXCLUDE_ROOT_PROPERTY =
    "globalSearch.web.excludeRoot";
  @CMSProperty
  public static final String WEB_INCLUDE_URL_NODES_PROPERTY =
    "globalSearch.web.includeURLNodes";
  @CMSProperty
  public static final String DATE_FORMAT_PROPERTY =
    "globalSearch.dateFormat";
  @CMSProperty
  public static final String MAX_PAGES_PROPERTY =
    "globalSearch.maxPages";
  @CMSProperty
  public static final String MAX_ROWS_PER_PAGE_PROPERTY =
    "globalSearch.maxRowsPerPage";

  //Render node properties
  @CMSProperty
  public static final String AGENDA_RENDER_ID_PROPERTY =
    "globalSearch.agenda.render.id";
  @CMSProperty
  public static final String AGENDA_RENDER_DATE_PROPERTY =
    "globalSearch.agenda.render.date";
  @CMSProperty
  public static final String AGENDA_RENDER_NAME_PROPERTY =
    "globalSearch.agenda.render.name";
  @CMSProperty
  public static final String AGENDA_RENDER_OBSERV_PROPERTY =
    "globalSearch.agenda.render.observ";
  @CMSProperty
  public static final String AGENDA_RENDER_SCORE_PROPERTY =
    "globalSearch.agenda.render.score";
  @CMSProperty
  public static final String NEWS_RENDER_ID_PROPERTY =
    "globalSearch.news.render.id";
  @CMSProperty
  public static final String NEWS_RENDER_DATE_PROPERTY =
    "globalSearch.news.render.date";
  @CMSProperty
  public static final String NEWS_RENDER_HEADLINE_PROPERTY =
    "globalSearch.news.render.headline";
  @CMSProperty
  public static final String NEWS_RENDER_SUMMARY_PROPERTY =
    "globalSearch.news.render.summary";
  @CMSProperty
  public static final String NEWS_RENDER_SCORE_PROPERTY =
    "globalSearch.news.render.score";
  @CMSProperty
  public static final String DOC_RENDER_ID_PROPERTY =
    "globalSearch.doc.render.id";
  @CMSProperty
  public static final String DOC_RENDER_DATE_PROPERTY =
    "globalSearch.doc.render.date";
  @CMSProperty
  public static final String DOC_RENDER_NAME_PROPERTY =
    "globalSearch.doc.render.name";
  @CMSProperty
  public static final String DOC_RENDER_MIMETYPE_PROPERTY =
    "globalSearch.doc.render.mimeType";
  @CMSProperty
  public static final String DOC_RENDER_SCORE_PROPERTY =
    "globalSearch.doc.render.score";
  @CMSProperty
  public static final String WEB_RENDER_ID_PROPERTY =
    "globalSearch.web.render.id";
  @CMSProperty
  public static final String WEB_RENDER_DATE_PROPERTY =
    "globalSearch.web.render.date";
  @CMSProperty
  public static final String WEB_RENDER_LABEL_PROPERTY =
    "globalSearch.web.render.label";
  @CMSProperty
  public static final String WEB_RENDER_SCORE_PROPERTY =
    "globalSearch.web.render.score";

  private static final String WEB_URL_PATTERN_PROPERTY =
    "globalSearch.web.urlPattern";
  private static final String NODEID_PROPERTY =
    "globalSearch.nodeId";
  
  private static final String DOC_SERVLET_PATH = "/documents/";

  public static final String MODULE_AGENDA = "AGENDA";
  public static final String MODULE_NEWS = "NEWS";
  public static final String MODULE_DOC = "DOC";
  public static final String MODULE_WEB = "WEB";

  private String remoteInputText;

  private String inputText;
  private String selectedModule;
  private boolean searchDone;
  private GlobalSearchFilter filter;
  private GlobalSearchResults result;
  private int firstRowIndex;

  public SearchBean()
  {    
  }

  //Set & get methods

  public String getInputText()
  {
    return inputText;
  }

  public void setInputText(String inputText)
  {
    this.inputText = inputText;
  }

  public String getRemoteInputText()
  {
    return remoteInputText;
  }

  public void setRemoteInputText(String remoteInputText)
  {
    this.remoteInputText = remoteInputText;
  }

  public String getSelectedModule()
  {
    return selectedModule;
  }

  public void setSelectedModule(String selectedModule)
  {
    this.selectedModule = selectedModule;
  }

  public boolean isSearchDone()
  {
    return searchDone;
  }

  public void setSearchDone(boolean searchDone)
  {
    this.searchDone = searchDone;
  }

  public GlobalSearchFilter getFilter()
  {
    return filter;
  }

  public void setFilter(GlobalSearchFilter filter)
  {
    this.filter = filter;
  }

  public GlobalSearchResults getResult()
  {
    return result;
  }

  public void setResult(GlobalSearchResults result)
  {
    this.result = result;
  }

  public int getFirstRowIndex()
  {
    return firstRowIndex;
  }

  public void setFirstRowIndex(int firstRowIndex)
  {
    this.firstRowIndex = firstRowIndex;
  }

  //Actions

  public void remoteSearch()
  {
    if (remoteInputText != null && remoteInputText.trim().length() > 0)
    {
      selectedModule = MODULE_WEB;
      inputText = remoteInputText;
      String nodeId = getSearchNodeId();
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      userSessionBean.setSelectedMid(nodeId);
      userSessionBean.executeSelectedMenuItem();
    }
  }

  @CMSAction
  public String search()
  {
    try
    {
      firstRowIndex = 0;
      remoteInputText = "";
      if (inputText != null && inputText.trim().length() > 0)
      {
        setFilterValues();
        result = getPort().search(filter);
//        if (filter.getWebFilter() != null)
//        {
//          filterWebSearch(result);
//        }
        searchDone = true;
      }
      else searchDone = false;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "search";
  }

  @CMSAction
  public String show()
  {
    return search();
  }

  //Public web methods

  public boolean isAgendaSearchSelected()
  {
    return MODULE_AGENDA.equals(selectedModule);
  }

  public boolean isNewsSearchSelected()
  {
    return MODULE_NEWS.equals(selectedModule);
  }

  public boolean isDocSearchSelected()
  {
    return MODULE_DOC.equals(selectedModule);
  }

  public boolean isWebSearchSelected()
  {
    return MODULE_WEB.equals(selectedModule);
  }

  public String getAgendaTranslationGroup()
  {
    Item row = (Item)getValue("#{item}");
    return "event:" + row.getId();
  }

  public String getNewsTranslationGroup()
  {
    Item row = (Item)getValue("#{item}");
    return "new:" + row.getId();
  }

  public String getWebTranslationGroup()
  {
    Item row = (Item)getValue("#{item}");
    return "jsp:" + row.getId();
  }

  public String getItemDate() throws Exception
  {
    try
    {
      Item row = (Item)getValue("#{item}");
      String sysDate = row.getDate();
      if (sysDate != null)
      {
        SimpleDateFormat userFormat = new SimpleDateFormat(getDateFormat());
        SimpleDateFormat sysFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return userFormat.format(sysFormat.parse(sysDate));
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "";
  }

  public boolean isAgendaRenderId()
  {
    return getPropertyBooleanValue(AGENDA_RENDER_ID_PROPERTY, false);
  }

  public boolean isAgendaRenderDate()
  {
    return getPropertyBooleanValue(AGENDA_RENDER_DATE_PROPERTY, true);
  }

  public boolean isAgendaRenderName()
  {
    return getPropertyBooleanValue(AGENDA_RENDER_NAME_PROPERTY, true);
  }

  public boolean isAgendaRenderObserv()
  {
    return getPropertyBooleanValue(AGENDA_RENDER_OBSERV_PROPERTY, true);
  }

  public boolean isAgendaRenderScore()
  {
    return getPropertyBooleanValue(AGENDA_RENDER_SCORE_PROPERTY, false);
  }

  public boolean isEventRenderAsLink()
  {
    return getAgendaViewNode() != null;
  }

  public boolean isNewsRenderId()
  {
    return getPropertyBooleanValue(NEWS_RENDER_ID_PROPERTY, false);
  }

  public boolean isNewsRenderDate()
  {
    return getPropertyBooleanValue(NEWS_RENDER_DATE_PROPERTY, true);
  }

  public boolean isNewsRenderHeadline()
  {
    return getPropertyBooleanValue(NEWS_RENDER_HEADLINE_PROPERTY, true);
  }

  public boolean isNewsRenderSummary()
  {
    return getPropertyBooleanValue(NEWS_RENDER_SUMMARY_PROPERTY, true);
  }

  public boolean isNewsRenderScore()
  {
    return getPropertyBooleanValue(NEWS_RENDER_SCORE_PROPERTY, false);
  }

  public boolean isNewRenderAsLink()
  {
    return getNewsViewNode() != null;
  }

  public boolean isDocRenderId()
  {
    return getPropertyBooleanValue(DOC_RENDER_ID_PROPERTY, false);
  }

  public boolean isDocRenderDate()
  {
    return getPropertyBooleanValue(DOC_RENDER_DATE_PROPERTY, true);
  }

  public boolean isDocRenderName()
  {
    return getPropertyBooleanValue(DOC_RENDER_NAME_PROPERTY, true);
  }

  public boolean isDocRenderMimeType()
  {
    return getPropertyBooleanValue(DOC_RENDER_MIMETYPE_PROPERTY, true);
  }

  public boolean isDocRenderScore()
  {
    return getPropertyBooleanValue(DOC_RENDER_SCORE_PROPERTY, false);
  }

  public boolean isWebRenderId()
  {
    return getPropertyBooleanValue(WEB_RENDER_ID_PROPERTY, false);
  }

  public boolean isWebRenderDate()
  {
    return getPropertyBooleanValue(WEB_RENDER_DATE_PROPERTY, true);
  }

  public boolean isWebRenderLabel()
  {
    return getPropertyBooleanValue(WEB_RENDER_LABEL_PROPERTY, true);
  }

  public boolean isWebRenderScore()
  {
    return getPropertyBooleanValue(WEB_RENDER_SCORE_PROPERTY, false);
  }

  public int getMaxPages()
  {
    return getPropertyIntValue(MAX_PAGES_PROPERTY, 5);
  }

  public int getMaxRowsPerPage()
  {
    return getPropertyIntValue(MAX_ROWS_PER_PAGE_PROPERTY, 20);
  }

  public String getAgendaLink()
  {
    Item row = (Item)getValue("#{item}");
    return getContextPath() + "/go.faces?xmid=" + getAgendaViewNode() +
      "&eventid=" + row.getId();
  }

  public String getNewLink()
  {
    Item row = (Item)getValue("#{item}");
    return getContextPath() + "/go.faces?xmid=" + getNewsViewNode() +
      "&newid=" + row.getId();
  }

  public String getDocLink()
  {
    Item row = (Item)getValue("#{item}");
    return getContextPath() + DOC_SERVLET_PATH + row.getId();
  }

  public String getWebLink()
  {
    Item row = (Item)getValue("#{item}");
    if (row.getInfo2() == null) //non-URL node
    {
      Properties properties = new Properties();
      properties.setProperty("nodeId", row.getId());
      return Template.create(getWebURLPattern(row)).merge(properties);
    }
    else //URL node
    {
      return row.getInfo2();
    }
  }

  public String getDocFileTypeImage()
  {
    Item item = (Item)getValue("#{item}");
    return DocumentBean.getContentTypeIcon(item.getInfo1());
  }

  public String getDataTableId()
  {
    String prefix = "";
    if (isAgendaSearchSelected()) prefix = "agenda";
    else if (isNewsSearchSelected()) prefix = "news";
    else if (isDocSearchSelected()) prefix = "doc";
    else if (isWebSearchSelected()) prefix = "web";
    return prefix + "DataTable";
  }

  public boolean isNoResultsFound()
  {
    return searchDone &&
      (isAgendaSearchSelected() || isNewsSearchSelected() ||
        isDocSearchSelected() || isWebSearchSelected()) &&
      result.getItemList().isEmpty();
  }

  public List<Item> getAgendaItemList()
  {
    return getItemListByType(ItemType.AGENDA);
  }

  public List<Item> getNewsItemList()
  {
    return getItemListByType(ItemType.NEWS);
  }

  public List<Item> getDocItemList()
  {
    return getItemListByType(ItemType.DOC);
  }

  public List<Item> getWebItemList()
  {
    List<Item> webItemList = getItemListByType(ItemType.WEB);
    return filterWebSearch(webItemList);
  }  

  //Private methods

  private void setFilterValues()
  {
    filter = new GlobalSearchFilter();
    if (isAgendaSearchSelected())
    {
      filter.setAgendaFilter(new AgendaFilter());
      filter.getAgendaFilter().setMaxRows(getAgendaMaxRows());
      filter.getAgendaFilter().setSearchDays(getAgendaSearchDays());
      filter.getAgendaFilter().getThemeList().addAll(getAgendaThemeList());
    }
    else if (isNewsSearchSelected())
    {
      filter.setNewsFilter(new NewsFilter());
      filter.getNewsFilter().setMaxRows(getNewsMaxRows());
      filter.getNewsFilter().setSearchDays(getNewsSearchDays());
      filter.getNewsFilter().getSectionList().addAll(getNewsSectionList());
    }
    else if (isDocSearchSelected())
    {
      filter.setDocFilter(new DocFilter());
      filter.getDocFilter().setMaxRows(getDocMaxRows());
      filter.getDocFilter().setSearchDays(getDocSearchDays());
    }
    else if (isWebSearchSelected())
    {
      filter.setWebFilter(new WebFilter());
      filter.getWebFilter().setMaxRows(getWebMaxRows());
      filter.getWebFilter().setSearchDays(getWebSearchDays());
      filter.getWebFilter().setWorkspaceId(getWorkspaceId());
    }
    filter.setText(inputText);
  }

  private String getSearchNodeId()
  {
    return getPropertyStringValue(NODEID_PROPERTY);
  }

  private int getAgendaSearchDays()
  {
    return getPropertyIntValue(AGENDA_SEARCH_DAYS_PROPERTY, 30);
  }

  private int getAgendaMaxRows()
  {
    return getPropertyIntValue(AGENDA_MAX_ROWS_PROPERTY, 10);
  }

  private List<String> getAgendaThemeList()
  {
   List<String> list = new ArrayList<String>();
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
    List<String> map = mic.getMultiValuedProperty(AGENDA_THEME_PROPERTY);
    for (Object value : map)
    {
      list.add((String)value);
    }
    if (list.isEmpty()) list.addAll(getDefaultAgendaThemeList());
    return list;
  }

  private String getAgendaViewNode()
  {
    return getPropertyStringValue(AGENDA_VIEW_NODE_PROPERTY);
  }

  private int getNewsSearchDays()
  {
    return getPropertyIntValue(NEWS_SEARCH_DAYS_PROPERTY, 365);
  }

  private int getNewsMaxRows()
  {
    return getPropertyIntValue(NEWS_MAX_ROWS_PROPERTY, 10);
  }

  private List<String> getNewsSectionList()
  {
    List<String> list = new ArrayList<String>();
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
    List<String> map = mic.getMultiValuedProperty(NEWS_SECTION_PROPERTY);
    for (Object value : map)
    {
      list.add((String)value);
    }
    if (list.isEmpty()) list.addAll(getDefaultNewsSectionList());
    return getVisibleSections(list);
  }

  private String getNewsViewNode()
  {
    return getPropertyStringValue(NEWS_VIEW_NODE_PROPERTY);
  }

  private int getDocSearchDays()
  {
    return getPropertyIntValue(DOC_SEARCH_DAYS_PROPERTY, 36500);
  }

  private int getDocMaxRows()
  {
    return getPropertyIntValue(DOC_MAX_ROWS_PROPERTY, 10);
  }

  private int getWebSearchDays()
  {
    return getPropertyIntValue(WEB_SEARCH_DAYS_PROPERTY, 36500);
  }

  private int getWebMaxRows()
  {
    return getPropertyIntValue(WEB_MAX_ROWS_PROPERTY, 10);
  }

  private boolean isWebIncludeURLNodes()
  {
    return getPropertyBooleanValue(WEB_INCLUDE_URL_NODES_PROPERTY, true);
  }

  private String getWebURLPattern(Item row)
  {
    try
    {
      MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
        getMenuItemByMid(row.getId());
      String value = mic.getProperty(WEB_URL_PATTERN_PROPERTY);
      if (value != null) return value;
    }
    catch (Exception ex)
    {      
    }
    return getContextPath() + "/go.faces?xmid=${nodeId}";
  }

  private String getWorkspaceId()
  {
    return UserSessionBean.getCurrentInstance().getWorkspaceId();
  }

  private List<String> getWebRootList()
  {
    List<String> list = new ArrayList<String>();
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
    List<String> map = mic.getMultiValuedProperty(WEB_ROOT_PROPERTY);
    for (Object value : map)
    {
      list.add((String)value);
    }
    if (list.isEmpty()) list.addAll(getDefaultWebRootList());
    return list;
  }

  private List<String> getWebExcludeRootList()
  {
    List<String> list = new ArrayList<String>();
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
    List<String> map = mic.getMultiValuedProperty(WEB_EXCLUDE_ROOT_PROPERTY);
    for (Object value : map)
    {
      list.add((String)value);
    }
    return list;
  }

  private String getDateFormat()
  {
    return getPropertyStringValue(DATE_FORMAT_PROPERTY, "dd/MM/yyyy HH:mm");
  }

  private List<Item> getItemListByType(ItemType itemType)
  {
    List<Item> itemList = new ArrayList<Item>();
    if (result != null)
    {      
      for (Item item : result.getItemList())
      {
        if (itemType.value().equals(item.getType().value()))
        {
          itemList.add(item);
        }
      }
    }
    return itemList;
  }

  private List<Item> filterWebSearch(List<Item> webItemList)
  {    
    List<String> webRootList = getWebRootList();
    List<String> webExcludeRootList = getWebExcludeRootList();
    List<Item> newWebItemList = new ArrayList<Item>();
    for (Item webItem : webItemList)
    {
      if (isWebIncludeURLNodes() || webItem.getInfo2() == null)
      {
        try
        {
          MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
            getMenuModel().getMenuItemByMid(webItem.getId());
          String[] path = menuItem.getPath();
          boolean include = false;
          boolean end = false;
          for (int i = 0; i < path.length && !end; i++)
          {
            if (webRootList.contains(path[i]) || webRootList.isEmpty())
            {
              include = true;
            }
            if (webExcludeRootList.contains(path[i]))
            {
              include = false;
              end = true;
            }
          }
          if (include)
          {
            newWebItemList.add(webItem);
          }
        }
        catch (Exception ex)
        {
          // Node is not visible
        }
      }
    }
    return newWebItemList;
  }

  private List<String> getVisibleSections(List<String> nodeIdList)
  {
    List<String> visibleNodeIdList = new ArrayList<String>();
    List<MenuItemCursor> menuItemList = UserSessionBean.getCurrentInstance().
      getMenuModel().getMenuItemsByMid(nodeIdList);
    for (MenuItemCursor menuItem : menuItemList)
    {
      visibleNodeIdList.add(menuItem.getMid());
    }
    return visibleNodeIdList;
  }  

  private List<String> getDefaultAgendaThemeList()
  {
    List<String> valueList = new ArrayList<String>();
    String[] propertyValueSplit = MatrixConfig.getProperty(
      "org.santfeliu.search.defaultAgendaThemeList").split(",");
    for (String propertyValue : propertyValueSplit)
    {
      valueList.add(propertyValue);
    }
    return valueList;
  }

  private List<String> getDefaultNewsSectionList()
  {
    List<String> valueList = new ArrayList<String>();
    String[] propertyValueSplit = MatrixConfig.getProperty(
      "org.santfeliu.search.defaultNewsSectionList").split(",");
    for (String propertyValue : propertyValueSplit)
    {
      valueList.add(propertyValue);
    }
    return valueList;
  }

  private List<String> getDefaultWebRootList()
  {
    List<String> valueList = new ArrayList<String>();
    String propertyValue =
      MatrixConfig.getProperty("org.santfeliu.search.defaultWebRootList");
    if (propertyValue != null)
    {
      String[] propertyValueSplit = propertyValue.split(",");
      for (String item : propertyValueSplit)
      {
        valueList.add(item);
      }      
    }
    return valueList;
  }

  private boolean getPropertyBooleanValue(String propertyName,
    boolean defaultValue)
  {
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
    String value = mic.getProperty(propertyName);
    if ("true".equals(value))
    {
      return true;
    }
    else if ("false".equals(value))
    {
      return false;
    }
    else return defaultValue;
  }
  
  private String getPropertyStringValue(String propertyName)
  {
    return getPropertyStringValue(propertyName, null);
  }
  
  private String getPropertyStringValue(String propertyName,
    String defaultValue)
  {
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
    String value = mic.getProperty(propertyName);
    return value != null ? value : defaultValue;
  }

  private int getPropertyIntValue(String propertyName, int defaultValue)
  {
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
    String value = mic.getProperty(propertyName);
    try
    {
      if (value != null)
      {
        return Integer.parseInt(value);
      }
    }
    catch (Exception ex)
    {

    }
    return defaultValue;
  }

  private SearchManagerPort getPort()
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(SearchManagerService.class);
    return endpoint.getPort(SearchManagerPort.class,
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

}
