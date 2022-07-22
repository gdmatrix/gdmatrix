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

import java.util.List;
import org.matrix.pf.web.helper.BigListHelper;
import org.matrix.pf.web.helper.BigListPage;

/**
 *
 * @author blanquepa
 */
public abstract class SearchBacking extends PageBacking
  implements BigListPage
{    
  protected final BigListHelper bigListHelper;
  protected String smartValue;
  
  protected SearchBacking()
  {
    bigListHelper = new BigListHelper(this);   
  }
  
  public String getSmartValue()
  {
    return smartValue;
  }

  public void setSmartValue(String smartValue)
  {
    this.smartValue = smartValue;
  }  
  
  public abstract String smartSearch();

  public abstract String clear();

  @Override
  public BigListHelper getResultListHelper()
  {
    return bigListHelper;
  }
  
  public abstract String getFilterTypeId();
  
  public abstract void refresh();
  
  public abstract String getOutcome();
  
  @Override
  public String show()
  {
    String currentTypeId = getFilterTypeId();
    String menuItemTypeId = getMenuItemTypeId();    
    if (currentTypeId != null && !currentTypeId.equals(menuItemTypeId))
    {
      refresh();
      bigListHelper.reset();
      bigListHelper.refresh();
    }

    return getOutcome();
  }  
  
  public String search()
  {
    populate();
    return null;
  }  

  @Override
  public String getPageObjectId()
  {
    return null;
  }
  
  @Override
  public abstract int countResults();

  @Override
  public abstract List<?> getResults(int firstResult, int maxResults);  
  
  public boolean isRenderBasicResults()
  {
    return (objectBacking.getObjectId() != null);
  }
 
  public String getObjectId(Object row)
  {
    return objectBacking.getObjectId(row);
  }
  
  public String getDescription(Object row)
  {
    return objectBacking.getDescription(row);
  }
    
  public String show(Object row)
  {
    Object obj = bigListHelper.getObject(row);
    return show(getObjectId(obj));
  }
  
  @Override
  public String show(String objectId)
  {
    return ControllerBacking.getCurrentInstance().show(objectId);
  }
  
  public String select(Object row)
  {
    Object obj = bigListHelper.getObject(row);    
    return select(getObjectId(obj));
  }  
  
  public String select(String selectedObjectId)
  {
    return ControllerBacking.getCurrentInstance().select(selectedObjectId);
  }
  
  public boolean isSelectableObject()
  {
    return ControllerBacking.getCurrentInstance().isSelectableNode();
  }

  protected void populate()
  {
    bigListHelper.search();    
  }
   
}
