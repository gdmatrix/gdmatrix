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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.news.NewView;
import org.matrix.news.NewsFilter;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.util.BigList;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.enc.Unicode;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import static org.santfeliu.webapp.modules.news.NewObjectBean.NEWSECTION_PROPERTY;
import static org.santfeliu.webapp.modules.news.NewSectionsTabBean.SECTIONID_PROPERTY;
import org.santfeliu.webapp.util.DateTimeRowStyleClassGenerator;
import org.santfeliu.webapp.util.RowStyleClassGenerator;
/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class NewFinderBean extends FinderBean
{
  private String smartFilter;
  private NewsFilter filter = new NewsFilter();
  private List<NewView> rows;
  private int firstRow;
  private boolean outdated; 
  private String sectionId;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  NewObjectBean newObjectBean;

  @Inject
  NewTypeBean newTypeBean;

  @PostConstruct
  public void init()
  {
    initFilter();
  }

  @Override
  public List<NewView> getRows()
  {
    return rows;
  }

  public void setRows(List<NewView> rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  @Override
  public NewsFilter getFilter()
  {
    return filter;
  }

  public void setFilter(NewsFilter filter)
  {
    this.filter = filter;
  }

  public List<String> getFilterNewId()
  {
    return this.filter.getNewId();
  }

  public void setFilterNewId(List<String> newIds)
  {
    this.filter.getNewId().clear();
    if (newIds != null && !newIds.isEmpty())
      this.filter.getNewId().addAll(newIds);
  }

  public SelectItem getFilterSectionId()
  {
    SelectItem sectionSelectItem = null;
    
    if (sectionId != null)
    {
      MenuItemCursor node = UserSessionBean.getCurrentInstance()
        .getMenuModel().getMenuItem(sectionId);
      sectionSelectItem = getSectionSelectItem(node);      
    }
    
    return sectionSelectItem;
  }

  public void setFilterSectionId(SelectItem sectionSelectItem)
  {
    sectionId = sectionSelectItem != null ? 
      (String) sectionSelectItem.getValue() : null;
  }  
  
  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getNewId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return newObjectBean;
  }
  
  public List<SelectItem> complete(String query)
  {
    List<SelectItem> sections = new ArrayList<>();

    try
    {
      sections = getEditSections(query);

      Collections.sort(sections, (Object o1, Object o2) ->
      {
        SelectItem i1 = (SelectItem)o1;
        SelectItem i2 = (SelectItem)o2;
                
        String d1 = i1.getDescription() != null ? i1.getDescription() : "";
        String d2 = i2.getDescription() != null ? i2.getDescription() : "";
        
        String label1 = d1 + i1.getLabel();
        String label2 = d2 + i2.getLabel();
        
        return label1.compareTo(label2);
      });
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return sections;
  }
      
  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = newTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    smartFilter = newTypeBean.filterToQuery(filter);
    doFind(true);
    firstRow = 0;
  }

  public void outdate()
  {
    this.outdated = true;
  }

  public void update()
  {
    if (outdated)
    {
      doFind(false);
    }
  }

  @Override
  public void clear()
  {
    super.clear();
    initFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(), filter, firstRow,
      getObjectPosition(), rows, outdated, getPageSize() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[]) state;
      setFinding((Boolean)stateArray[0]);
      setFilterTabSelector((Integer) stateArray[1]);
      filter = (NewsFilter) stateArray[2];
      firstRow = (Integer) stateArray[3];
      setObjectPosition((Integer) stateArray[4]);
      rows = (List<NewView>) stateArray[5];
      outdated = (Boolean) stateArray[6];
      setPageSize((Integer)stateArray[7]);      
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public String getRowStyleClass(Object row)
  {
    String styleClass = "";
    if (row != null)
    {
      RowStyleClassGenerator styleClassGenerator = 
        new DateTimeRowStyleClassGenerator("startDate,startTime",
        "endDate,endTime", null);
      styleClass = styleClassGenerator.getStyleClass(row);
    }
    return styleClass;
  }

  private void doFind(boolean autoLoad)
  {
    try
    {
      if (!isFinding())
      {
        rows = Collections.EMPTY_LIST;
      }
      else
      {
        rows = new BigList(2 * getPageSize() + 1, getPageSize())
        {
          private List<SelectItem> sections = null; //getEditSections();

          @Override
          public int getElementCount()
          {
            try
            {
              String content = filter.getContent();
              filter.setContent(setWildcards(content));
              filter.getSectionId().clear();              
              if (sectionId != null)
                filter.getSectionId().add(sectionId);
              else
              {
                sections = getEditSections();
                sections.stream().forEach(section -> 
                  filter.getSectionId().add((String) section.getValue()));                
              }
              int count = NewsModuleBean.getPort(false).countNews(filter);
              resetWildcards(filter);
              return count;
            }
            catch (Exception ex)
            {
              error(ex);
            }
            return 0;
          }

          @Override
          public List getElements(int firstResult, int maxResults)
          {
            try
            {
              String content = filter.getContent();
              filter.setContent(setWildcards(content));
              filter.getSectionId().clear();
              if (sectionId != null)
                filter.getSectionId().add(sectionId);
              else
              {
                sections = getEditSections();
                sections.stream().forEach(section -> 
                  filter.getSectionId().add((String) section.getValue()));                
              }
              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              List<NewView> results =
                NewsModuleBean.getPort(false).findNewViews(filter);
              if (!results.isEmpty())
              {
                results.stream()
                  .forEach(n -> n.setHeadline(Unicode.decode(n.getHeadline())));
              }
              resetWildcards(filter);
              return results;

            }
            catch (Exception ex)
            {
              error(ex);
            }
            return null;
          }
        };

        outdated = false;

        if (autoLoad)
        {
          if (rows.size() == 1)
          {
            NewView newView = (NewView) rows.get(0);
            navigatorBean.view(newView.getNewId());
            newObjectBean.setSearchTabSelector(1);
          }
          else
          {
            newObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
  }

  private String setWildcards(String text)
  {
    if (text != null && !text.startsWith("\"") && !text.endsWith("\""))
      text = "%" + text.replaceAll("^%|%$", "") + "%" ;
    else if (text != null && text.startsWith("\"") && text.endsWith("\""))
      text = text.replaceAll("^\"|\"$", "");
    return text;
  }

  private void resetWildcards(NewsFilter filter)
  {
    String content = filter.getContent();
    if (content != null && !content.startsWith("\"")
      && !content.endsWith("\""))
    {
      content = content.replaceAll("^%+|%+$", "");
      filter.setContent(content);
    }
  }

  private void initFilter()
  {
    filter = new NewsFilter();
    Date now = new Date();
    filter.setStartDateTime(TextUtils.formatDate(now, "yyyyMMddHHmmss"));
    filter.setEndDateTime(TextUtils.formatDate(now, "yyyyMMddHHmmss"));
    filter.setExcludeNotPublished(false);
  }
  
  private SelectItem getSectionSelectItem(MenuItemCursor node)
  {
    SelectItem item = null;
    MenuItemCursor[] path = getSectionPath(node);
    
    if (path != null)
    {
      item = new SelectItem(node.getMid());
      StringBuilder sb = new StringBuilder();
      for (int i = 1; i < path.length; i++) 
      {
        MenuItemCursor p = path[i];
        if (i == 1)
          item.setDescription(getSectionName(p)); 
        else if (i + 1 < path.length)
          sb.append(getSectionName(p)).append(" / ");
        else if (i + 1 == path.length)
          item.setLabel(sb.toString() + getSectionName(p));
      }
    }
    return item;
  }  
  
  private String getSectionName(MenuItemCursor mic)
  {
    String section = mic.getDirectProperty(NEWSECTION_PROPERTY);
    if (section == null)
      section = mic.getDirectProperty("description");
    if (section == null)
      section = mic.getLabel();
 
    return section;
  }
  
  private MenuItemCursor[] getSectionPath(MenuItemCursor node)
  {
    MenuItemCursor[] result = null;
    String sectionId = node.getDirectProperty(SECTIONID_PROPERTY);
    if (sectionId == null)
    {
      List<String> editRoles =
        node.getMultiValuedProperty(MenuModel.EDIT_ROLES);

      if (UserSessionBean.getCurrentInstance().isUserInRole(editRoles))
      {
        result = node.getCursorPath();
      }
    }   
    return result;
  }  
  
  private List<SelectItem> getEditSections() 
    throws Exception
  {
    return getEditSections(null);
  } 
  
  private List<SelectItem> getEditSections(String query) 
    throws Exception
  {
    List<SelectItem> sections = new ArrayList();
    
    List<MenuItemCursor> menuItemList = newObjectBean.getSectionNodes(query);

    for (MenuItemCursor menuItem : menuItemList)
    {
      SelectItem item = getSectionSelectItem(menuItem);
      if (item != null)
        sections.add(item);
    }
    
    return sections;
  }  
  
}
