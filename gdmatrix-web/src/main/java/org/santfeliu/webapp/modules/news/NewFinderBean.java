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
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.news.NewView;
import org.matrix.news.NewsFilter;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.BigList;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.enc.Unicode;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
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
          private final List<String> sections = getEditSections();

          @Override
          public int getElementCount()
          {
            try
            {
              String content = filter.getContent();
              filter.setContent(setWildcards(content));
              filter.getSectionId().clear();
              filter.getSectionId().addAll(sections);
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
              filter.getSectionId().addAll(sections);
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

  private List<String> getEditSections() throws Exception
  {
    List<String> sections = new ArrayList();
    List<MenuItemCursor> menuItemList = newObjectBean.getSectionNodes();

    for (MenuItemCursor menuItem : menuItemList)
    {
      for (String editRole : menuItem.getEditRoles())
      {
        if (UserSessionBean.getCurrentInstance().isUserInRole(editRole))
          sections.add(menuItem.getMid());
      }
    }
    return sections;
  }

}
