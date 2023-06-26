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
package org.santfeliu.webapp.modules.dic;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.EnumType;
import org.matrix.dic.EnumTypeFilter;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;

/**
 * 
 * @author blanquepa
 */
@ViewScoped
@Named
public class EnumTypeFinderBean extends FinderBean
{
  private EnumTypeFilter filter = new EnumTypeFilter();
  private List<EnumType> rows;
  
  private String smartFilter;
  private int firstRow;
  private boolean outdated;  
  
  @Inject
  NavigatorBean navigatorBean;

  @Inject
  EnumTypeObjectBean enumTypeObjectBean;

  @Inject
  EnumTypeTypeBean enumTypeTypeBean;
  
  @Override
  public ObjectBean getObjectBean()
  {
    return enumTypeObjectBean;
  }

  public EnumTypeFilter getFilter()
  {
    return filter;
  }

  public void setFilter(EnumTypeFilter filter)
  {
    this.filter = filter;
  }
  
  public List<String> getEnumTypeIdList()
  {
    return filter.getEnumTypeId();
  }

  public void setEnumTypeIdList(List<String> enumTypeIdList)
  {
    filter.getEnumTypeId().clear();
    if (enumTypeIdList != null)
    {
      filter.getEnumTypeId().addAll(enumTypeIdList);
    }
  }
  
  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }
  
  public List<EnumType> getRows()
  {
    return rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }
  
  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getEnumTypeId();
  } 
  
  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }  
  
  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    filter = enumTypeTypeBean.queryToFilter(smartFilter, 
      DictionaryConstants.ENUM_TYPE_TYPE);
    doFind(true);
    resetWildcards(filter);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    smartFilter = enumTypeTypeBean.filterToQuery(filter);
    doFind(true);
    resetWildcards(filter);    
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

  public void clear()
  {
    filter = new EnumTypeFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(), filter, firstRow,
      getObjectPosition() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      setFinding((Boolean)stateArray[0]);
      setFilterTabSelector((Integer)stateArray[1]);
      filter = (EnumTypeFilter)stateArray[2];
      smartFilter = enumTypeTypeBean.filterToQuery(filter);

      doFind(false);

      firstRow = (Integer)stateArray[3];
      setObjectPosition((Integer)stateArray[4]);
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
        rows = Collections.emptyList();
      }
      else
      {
        rows = new BigList(20, 10)
        {
          @Override
          public int getElementCount()
          {
            try
            {
              String name = filter.getName();
              filter.setName(setWildcards(name));
              return DicModuleBean.getPort(false).countEnumTypes(filter);
            }
            catch (Exception ex)
            {
              error(ex);
              return 0;
            }
          }

          @Override
          public List getElements(int firstResult, int maxResults)
          {
            try
            {
              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              String name = filter.getName();
              filter.setName(setWildcards(name));     
              List<EnumType> result = 
                DicModuleBean.getPort(false).findEnumTypes(filter);
              filter.setName(name);
              return result;
            }
            catch (Exception ex)
            {
              error(ex);
              return null;
            }
          }
        };

        outdated = false;

        if (autoLoad)
        {
          if (rows.size() == 1)
          {
            navigatorBean.view(rows.get(0).getEnumTypeId());
            enumTypeObjectBean.setSearchTabSelector(
              enumTypeObjectBean.getEditModeSelector());
          }
          else
          {
            enumTypeObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch (Exception ex)
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
  
  private void resetWildcards(EnumTypeFilter filter)
  {
    String name = filter.getName();
    if (name != null && !name.startsWith("\"") && !name.endsWith("\""))
      name = name.replaceAll("^%+|%+$", "");
    filter.setName(name);
  }  
}
