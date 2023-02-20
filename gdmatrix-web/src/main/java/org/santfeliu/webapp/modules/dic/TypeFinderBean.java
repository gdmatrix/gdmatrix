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
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
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
public class TypeFinderBean extends FinderBean
{
  private TypeFilter filter = new TypeFilter();
  private List<Type> rows;
  
  private String smartFilter;
  private int firstRow;
  private boolean finding;
  private boolean outdated;  
  
  private String rootTypeId;
  
  @Inject
  NavigatorBean navigatorBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @Inject
  TypeObjectBean typeObjectBean;  
  
  @Override
  public ObjectBean getObjectBean()
  {
    return typeObjectBean;
  }

  public String getRootTypeId()
  {
    return rootTypeId;
  }

  public void setRootTypeId(String rootTypeId)
  {
    this.rootTypeId = rootTypeId;
  }

  public TypeFilter getFilter()
  {
    return filter;
  }

  public void setFilter(TypeFilter filter)
  {
    this.filter = filter;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }
  


  public List<Type> getRows()
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
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getTypeId();
  } 
  
  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }  
  
  @Override
  public void smartFind()
  {
    finding = true;
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    if (typeTypeBean.getRootTypeId().equals(baseTypeId))
      baseTypeId = null;
    filter = typeTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    finding = true;
    setFilterTabSelector(1);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    if (typeTypeBean.getRootTypeId().equals(baseTypeId))
      baseTypeId = null;
    if (!StringUtils.isBlank(rootTypeId))
    {
      String typePath = (rootTypeId != null && rootTypeId.trim().length() > 0) ?
       DictionaryConstants.TYPE_PATH_SEPARATOR + rootTypeId +
       DictionaryConstants.TYPE_PATH_SEPARATOR + "%" : null;
      filter.setTypePath(typePath);
    }
    smartFilter = typeTypeBean.filterToQuery(filter);
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

  public void clear()
  {
    filter = new TypeFilter();
    smartFilter = null;
    rows = null;
    finding = false;
    rootTypeId = null;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ finding, getFilterTabSelector(), filter, firstRow,
      getObjectPosition(), rootTypeId };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      finding = (Boolean)stateArray[0];
      setFilterTabSelector((Integer)stateArray[1]);
      filter = (TypeFilter)stateArray[2];
      smartFilter = typeTypeBean.filterToQuery(filter);

      doFind(false);

      firstRow = (Integer)stateArray[3];
      setObjectPosition((Integer)stateArray[4]);
      rootTypeId = ((String)stateArray[5]);
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
      if (!finding)
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
              return DicModuleBean.getPort(false).countTypes(filter);
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
              return DicModuleBean.getPort(false).findTypes(filter);
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
            navigatorBean.view(rows.get(0).getTypeId());
            typeObjectBean.setSearchTabSelector(
              typeObjectBean.getEditModeSelector());
          }
          else
          {
            typeObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
}
