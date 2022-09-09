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
package org.matrix.pf.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuItem;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.ObjectDescriptionCache;

/**
 *
 * @author blanquepa
 */
@Named("templateBacking")
public class TemplateBacking extends WebBacking
{
  private static final String OBJECT_BUNDLE = 
    "org.santfeliu.web.obj.resources.ObjectBundle";
  private static final String SEARCH_PAGE_LABEL = "search";
  
  private Integer searchPanelIndex = 0;
  
  public TemplateBacking()
  {
    //Let to super class constructor. 
  }

  //Search panels
  public Integer getSearchPanelIndex()
  {
    return searchPanelIndex;
  }

  public void setSearchPanelIndex(Integer searchPanelIndex)
  {
    this.searchPanelIndex = searchPanelIndex;
  }
  
  public SearchBacking getSearchBacking()
  {
    return getObjectBacking().getSearchBacking();
  } 
  
  public List getBasicResults()
  {
    SearchBacking searchBacking = getSearchBacking();
    String filterTypeId = searchBacking.getFilterTypeId();
    String menuItemTypeId = getMenuItemTypeId();
    if (filterTypeId.equals(menuItemTypeId))
      return getSearchBacking().bigListHelper.getRows();
    else
      return null;
  }
    
  //Object tabs
  public List getTabs()
  {
    return getObjectBacking().getTabs();
  }

  public Integer getTabIndex()
  {
    return getObjectBacking().getTabIndex();
  }

  public void doTabAction(ActionEvent event)
  {
    getObjectBacking().doTabAction(event);
  }
  
  //Menús
  public MatrixMenuModel getPFMenuModel()
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    return new MatrixMenuModel(menuModel);
  } 
  
  public List<MenuItem> getLeftMenuItems()
  {
    List<MenuItem> results = new ArrayList<>();
    MatrixMenuModel mMenuModel = getPFMenuModel();
    List<MenuElement> elements = mMenuModel.getElements();
    for (MenuElement element : elements)
    {
      if (element instanceof MatrixMenuItem)
      {
        MatrixMenuItem menuItem = (MatrixMenuItem)element;
        if (menuItem.getIcon() != null)
        {
          String lmenu = menuItem.getProperty("lmenu");
          if (lmenu != null && lmenu.equalsIgnoreCase("true"))
            results.add(menuItem);
        }
      }
    }
    return results;
  }
  
  public String executeItemAction(String action)
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    return facesContext.getApplication().evaluateExpressionGet(
      facesContext, action, String.class);   
  }
  
  //Favorites panel
  public List<SelectItem> getFavorites()
  {
    return getObjectBacking().getFavorites();
  }
  
  public boolean isFavorite()
  {
    ObjectBacking objectBacking = getObjectBacking();
    String objectTypeId = objectBacking.getObjectTypeId();
    String objectId = objectBacking.getObjectId();
    return isObjectFavorite(objectTypeId, objectId);
  }
  
  public String markFavorite()
  {   
    ObjectBacking objectBacking = getObjectBacking();    
    String objectTypeId = objectBacking.getObjectTypeId();
    String objectId = objectBacking.getObjectId();    
    return markObjectAsFavorite(objectTypeId, objectId);
  }

  public String unmarkFavorite()
  {  
    ObjectBacking objectBacking = getObjectBacking();    
    String objectTypeId = objectBacking.getObjectTypeId();
    String objectId = objectBacking.getObjectId();        
    return unmarkObjectAsFavorite(objectTypeId, objectId);
  } 
  
  //Recent history panel
  public PageHistory getRecentHistory()
  {
    return ControllerBacking.getCurrentInstance().getPageHistory();
  }
  
  public TreeNode getRecentHistoryTree()
  {
    TreeNode root = new DefaultTreeNode("", null);
    
    PageHistory pageHistory = getRecentHistory();
    Collections.sort(pageHistory, new PageHistoryComparator());
    
    String prevTypeId = null;
    TreeNode group = null;
    for (Object item : pageHistory)
    {
      PageHistory.Entry entry = (PageHistory.Entry) item;
      String entryTypeId = entry.getTypeId();
      if (group == null || (entryTypeId != null && !entryTypeId.equals(prevTypeId)))
      {
        group = new DefaultTreeNode("group", entry, root);
        group.setExpanded(true);
        prevTypeId = entry.getTypeId();
      }
      group.getChildren().add(new DefaultTreeNode(entry));
    }
    
    return root;
  }
  
  //Object header
  public String getDescription(String typeId, String objectId)
  {
    String label = "";
    ResourceBundle bundle = 
      ResourceBundle.getBundle(OBJECT_BUNDLE, getLocale());     
    if (StringUtils.isBlank(objectId))
      label = bundle.getString(SEARCH_PAGE_LABEL);
    else
    {
      ObjectBacking targetBacking = 
        ControllerBacking.getCurrentInstance().getObjectBacking(typeId);
      if (targetBacking != null)
        label = targetBacking.getDescription(objectId);
    }
    
    return encodeDescription(label, objectId);
  }
  
  public String getDescription(SelectItem row)
  {
    String label = row.getLabel();
    String objectId = (String) row.getValue();
    return encodeDescription(label, objectId);  
  }
  
  public String getDescription(Object row)
  {
    String label = getSearchBacking().getDescription(row);
    String objectId = getSearchBacking().getObjectId(row);
    return encodeDescription(label, objectId);
  }  
    
  public String getDescription()
  {
    ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
    return cache.getDescription(getObjectBacking(), getObjectId());
  }
  
  public String getPageHistoryDescription(PageHistory.Entry entry)
  {
    String label = null;
    String objectId = entry.getObjectId();
    if (StringUtils.isBlank(objectId))
      label = entry.getTitle();
    else
      label = entry.getDescription();
    
    if (StringUtils.isBlank(label))
      label = getDescription(entry.getTypeId(), objectId);
    
    return label;
  }    
  
  public String getObjectId()
  {
    return getObjectBacking().getObjectId();
  }
  
  public String getTypeDescription()
  {
    String typeId = getObjectBacking().getObjectTypeId();
    return getTypeDescription(typeId);
  }
  
  public String getTypeDescription(String typeId)
  {
    String description = typeId;
    if (typeId != null)
    {
      Type type = TypeCache.getInstance().getType(typeId);      
      if (type != null)
        description = type.getDescription();
    }
    return description;    
  }
    
  //Operations
  public String show(Object row)
  {
    String objectId = getObjectBacking().getObjectId(row);
    return ControllerBacking.getCurrentInstance().show(objectId);
  }
  
  public String create()
  {
    return getObjectBacking().create();
  }
  
  public String store()
  {
    return getObjectBacking().store();
  }
  
  public String cancel()
  {
    return getObjectBacking().cancel();
  }
  
  public String remove()
  { 
    return getObjectBacking().remove();
  }
  
  public ObjectBacking getObjectBacking()
  {
    return ControllerBacking.getCurrentInstance().getObjectBacking();
  }  
       
  //Private methods
  private String encodeDescription(String label, String objectId)
  {
    return label + 
      (!StringUtils.isBlank(objectId) ? " (" + objectId + ")" : "");    
  }
  
  private String markObjectAsFavorite(String objectTypeId, String objectId)
  {
    UserSessionBean.getCurrentInstance().getUserPreferences().
      storePreference(objectTypeId, objectId);
    return null;
  }

  private String unmarkObjectAsFavorite(String objectTypeId, String objectId)
  {
    UserSessionBean.getCurrentInstance().getUserPreferences().
      removePreference(objectTypeId, objectId);
    return null;
  }

  private boolean isObjectFavorite(String objectTypeId, String objectId)
  {
    return UserSessionBean.getCurrentInstance().getUserPreferences().
      existsPreference(objectTypeId, objectId);
  }  
    
  private class PageHistoryComparator implements Comparator<PageHistory.Entry>
  {
    @Override
    public int compare(PageHistory.Entry o1, PageHistory.Entry o2)
    { 
      if (o1 == null || o1.getTypeId() == null)
        return -1;
      else if (o2 == null || o2.getTypeId() == null)
        return 1;

      int result = o1.getTypeId().compareTo(o2.getTypeId());
      if (result == 0)
      {
        String description1 = o1.getDescription();
        String description2 = o2.getDescription();
        if (description1 != null && description2 != null)
          return o1.getDescription().compareTo(o2.getDescription());
        else if (description1 == null)
          return -1;
        else
          return 1;
      }
      return result;
    } 
  }
 
}
