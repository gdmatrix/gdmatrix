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
package org.santfeliu.webapp.modules.kernel;

import java.io.Serializable;
import org.santfeliu.webapp.modules.agenda.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.AttendantView;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class PersonEventsTabBean extends TabBean
{
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();
  private GroupableRowsHelper groupableRowsHelper;    

  Map<String, TabInstance> tabInstances = new HashMap<>();

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<AttendantView> rows;
    int firstRow = 0;
  }

  @Inject
  PersonObjectBean personObjectBean;
  
  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);  
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return personObjectBean;
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return Collections.EMPTY_LIST;
      }

      @Override
      public void sortRows()
      {
      }

      @Override
      public String getRowTypeColumnName()
      {
        return null;
      }

      @Override
      public String getFixedColumnValue(Object row, String columnName)
      {        
        return null;
      }
    };
  }

  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = personObjectBean.getActiveEditTab();
    if (WebUtils.getBeanName(this).equals(tab.getBeanName()))
    {
      TabInstance tabInstance = tabInstances.get(tab.getSubviewId());
      if (tabInstance == null)
      {
        tabInstance = new TabInstance();
        tabInstances.put(tab.getSubviewId(), tabInstance);
      }
      return tabInstance;
    }
    else return EMPTY_TAB_INSTANCE;
  }

  @Override
  public String getObjectId()
  {
    return getCurrentTabInstance().objectId;
  }

  @Override
  public void setObjectId(String objectId)
  {
    getCurrentTabInstance().objectId = objectId;
  }

  public GroupableRowsHelper getGroupableRowsHelper()
  {
    return groupableRowsHelper;
  }

  @Override
  public boolean isNew()
  {
    return NEW_OBJECT_ID.equals(getCurrentTabInstance().objectId);
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return personObjectBean;
  }

  public List<AttendantView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<AttendantView> rows)
  {
    getCurrentTabInstance().rows = rows;
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }
   
  @Override
  public void load() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        AttendantFilter filter = new AttendantFilter();
        filter.setPersonId(personObjectBean.getObjectId());
        filter.setMaxResults(100);
        List<AttendantView> auxList = AgendaModuleBean.getClient(false).
          findAttendantViewsFromCache(filter);
        String typeId = getTabBaseTypeId();      
        
        if (typeId == null)
        {
          getCurrentTabInstance().rows = auxList;
        }
        else
        {
          List<AttendantView> result = new ArrayList();
          for (AttendantView item : auxList)
          {
            Type attendantType =
              TypeCache.getInstance().getType(item.getAttendantTypeId());
            if (attendantType != null && attendantType.isDerivedFrom(typeId))
            {
              result.add(item);
            }
          }
          getCurrentTabInstance().rows = result;
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      TabInstance tabInstance = getCurrentTabInstance();
      tabInstance.objectId = NEW_OBJECT_ID;
      tabInstance.rows = Collections.EMPTY_LIST;
      tabInstance.firstRow = 0;
    }
  }
  
  @Override
  public void clear()
  {
    tabInstances.clear();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{};
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      if (!isNew()) load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
    
}
