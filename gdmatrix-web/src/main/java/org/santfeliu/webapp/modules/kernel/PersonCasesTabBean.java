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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.cases.CaseObjectBean;
import org.santfeliu.webapp.modules.cases.CasesModuleBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class PersonCasesTabBean extends TabBean
{
  private final Map<String, TabInstance> tabInstances = new HashMap();
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();  
  
  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CasePersonView> rows;
    int firstRow = 0;
  }  
  
  @Inject
  private PersonObjectBean personObjectBean;

  @Inject
  private CaseObjectBean caseObjectBean;

  public PersonCasesTabBean()
  {
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return personObjectBean;
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
    else
      return EMPTY_TAB_INSTANCE;
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

  @Override
  public boolean isNew()
  {
    return NEW_OBJECT_ID.equals(getCurrentTabInstance().objectId);
  }  
  
  public List<CasePersonView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CasePersonView> rows)
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
  public void load()
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {       
      try
      {
        CasePersonFilter filter = new CasePersonFilter();
        filter.setPersonId(personObjectBean.getObjectId());
        
        String typeId = getTabBaseTypeId();
        if (typeId != null)
          filter.setCasePersonTypeId(typeId);        
        
        List<CasePersonView> auxList = 
          CasesModuleBean.getPort(false).findCasePersonViews(filter);
        setRows(auxList);
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
