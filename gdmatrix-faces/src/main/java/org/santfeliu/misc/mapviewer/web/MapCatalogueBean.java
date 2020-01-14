package org.santfeliu.misc.mapviewer.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.misc.mapviewer.MapCategory;
import org.santfeliu.misc.mapviewer.MapStore;
import org.santfeliu.misc.mapviewer.MapView;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;
import org.santfeliu.web.bean.CMSProperty;
/**
 *
 * @author realor
 */
@CMSManagedBean
public class MapCatalogueBean extends BasicSearchBean implements Serializable
{
  @CMSProperty
  public static final String MAP_VIEW_MID_PROPERTY = "mapViewerMid";
  @CMSProperty
  public static final String CATEGORIES_ENABLED_PROPERTY = "categoriesEnabled";

  private String propertyName;
  private String searchText;
  private MapCategory category;
  private boolean showMaps;

  private transient List<MapCategory> mapCategories;

  public String getPropertyName()
  {
    return propertyName;
  }

  public void setPropertyName(String propertyName)
  {
    this.propertyName = propertyName;
  }
  
  public String getSearchText()
  {
    return searchText;
  }

  public void setSearchText(String searchText)
  {
    this.searchText = searchText;
  }

  public MapCategory getCategory()
  {
    return category;
  }

  public void setCategory(MapCategory category)
  {
    this.category = category;
  }

  public boolean isPropertyNameRendered()
  {
    String role = getProperty("propertyFilterRole");
    if (role == null) return true;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole(role);
  }
  
  public List<SelectItem> getPropertyNameSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>();
    
    TypeCache typeCache = TypeCache.getInstance();
    Type mapType = typeCache.getType(MapStore.MAP_TYPEID);
    List<PropertyDefinition> propDefs = mapType.getPropertyDefinition();
    for (PropertyDefinition propDef : propDefs)
    {
      if (!propDef.isHidden() && 
           propDef.getType().equals(PropertyType.TEXT) &&
          !propDef.getName().equals(MapStore.CATEGORY_NAME_PROPERTY))
      {
        SelectItem selectItem = new SelectItem();
        selectItem.setValue(propDef.getName());
        selectItem.setLabel(propDef.getDescription());
        selectItems.add(selectItem);
      }
    }
    return selectItems;
  }
  
  public boolean isShowMaps()
  {
    return showMaps || !isCategoriesEnabled();
  }

  public boolean isCategoriesEnabled()
  {
    return "true".equals(getProperty(CATEGORIES_ENABLED_PROPERTY));
  }
  
  @Override
  public int countResults()
  {
    try
    {
      MapBean mapBean = MapBean.getInstance();
      return mapBean.countMapViews(propertyName, searchText, 
        category == null ? null : category.getName());
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
      MapBean mapBean = MapBean.getInstance();
      return mapBean.findMapViews(propertyName, searchText, 
        category == null ? null : category.getName(), 
        firstResult, maxResults);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String getThumbnailUrl()
  {
    MapView mapView = (MapView)getValue("#{row}");
    String contentId = mapView.getThumbnailContentId();
    if (contentId == null) return "/common/misc/images/thumbnail";
    else return "/documents/" + contentId + "/thumbnail";
  }

  public String getCreationDate()
  {
    MapView mapView = (MapView)getValue("#{row}");
    String creationDate = mapView.getCreationDate();
    if (creationDate == null) return null;
    Date date = TextUtils.parseInternalDate(creationDate);
    return TextUtils.formatDate(date, "dd/MM/yyyy");
  }

  public boolean isFormattedDescription()
  {
    MapView mapView = (MapView)getValue("#{row}");
    String description = mapView.getDescription();
    if (description == null) return false;
    return description.contains("<") && description.contains(">");
  }
  
  public List<MapCategory> getMapCategories() throws Exception
  {
    if (mapCategories == null)
    {
      mapCategories = MapBean.getInstance().findMapCategories();
    }
    return mapCategories;
  }
  
  
  /* actions */
  
  @Override
  @CMSAction
  public String show()
  {
    mapCategories = null;
    if (isShowMaps())
    {
      super.search();
    }
    return "map_catalogue";
  }

  @Override
  public String search()
  {
    showMaps = true;
    category = null;
    super.search();
    return "map_catalogue";
  }
  
  @Override
  public String refresh()
  {
    super.refresh();
    return "map_catalogue";
  }

  public String showMap()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String mid =
      userSessionBean.getSelectedMenuItem().getProperty(MAP_VIEW_MID_PROPERTY);
    if (mid != null)
    {
      MenuItemCursor menuItem =
        userSessionBean.getMenuModel().getMenuItem(mid);
      if (!menuItem.isNull()) menuItem.select(); // move to mapViewerMid
    }
    MapView mapView = (MapView)getValue("#{row}");
    String mapName = mapView.getName();
    MapViewerBean mapViewerBean = (MapViewerBean)getBean("mapViewerBean");
    return mapViewerBean.showMap(mapName);
  }
  
  public String showCategoryMaps()
  {
    this.category = (MapCategory)getValue("#{row}");
    this.searchText = null;
    this.showMaps = true;
    super.search();
    return "map_catalogue";
  }

  public String showCategories()
  {
    this.category = null;
    this.searchText = null;
    this.showMaps = false;
    super.refresh();
    return "map_catalogue";
  }
}
