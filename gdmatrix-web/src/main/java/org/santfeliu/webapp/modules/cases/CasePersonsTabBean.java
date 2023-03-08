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
package org.santfeliu.webapp.modules.cases;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.dic.DictionaryConstants;
import org.primefaces.PrimeFaces;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.setup.EditTab;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class CasePersonsTabBean extends TabBean
{
  private Map<String, TabInstance> tabInstances = new HashMap<>();
  private CasePerson editing;
  private boolean importAddresses;

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CasePersonView> rows;
    int firstRow = 0;
    boolean groupedView = false;    
  }

  @Inject
  CaseObjectBean caseObjectBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
  }
  
  public TabInstance getCurrentTabInstance()
  {
    EditTab editTab = caseObjectBean.getActiveEditTab();
    TabInstance tabInstance = tabInstances.get(editTab.getSubviewId());
    if (tabInstance == null)
    {
      tabInstance = new TabInstance();
      tabInstances.put(editTab.getSubviewId(), tabInstance);
    }
    return tabInstance;
  }  

  public List<CasePersonView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CasePersonView> casePersonViews)
  {
    getCurrentTabInstance().rows = casePersonViews;
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }
  
  public boolean isGroupedView()
  {
    return getCurrentTabInstance().groupedView;
  }

  public void setGroupedView(boolean groupedView)
  {
    getCurrentTabInstance().groupedView = groupedView;
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
  
  public CasePerson getEditing()
  {
    return editing;
  }

  public void setEditing(CasePerson casePerson)
  {
    editing = casePerson;
  }  
  
  public boolean isImportAddresses()
  {
    return importAddresses;
  }

  public void setImportAddresses(boolean importAddresses)
  {
    this.importAddresses = importAddresses;
  }  
  
  public void switchView()
  {
    getCurrentTabInstance().groupedView = !getCurrentTabInstance().groupedView;
  }  
  
  public void create()
  {
    editing = new CasePerson();
    String typeId = getTabBaseTypeId();
    if (typeId != null)
      editing.setCasePersonTypeId(typeId);
  }

  @Override
  public void load()
  {
    System.out.println("load casePersons:" + getObjectId());
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        CasePersonFilter filter = new CasePersonFilter();
        filter.setCaseId(getObjectId());
        
        String typeId = getTabBaseTypeId();
        if (typeId != null)
          filter.setCasePersonTypeId(typeId);        
        
        getCurrentTabInstance().rows = 
          CasesModuleBean.getPort(false).findCasePersonViews(filter);
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
  
  public void edit(CasePersonView casePersonView)
  {
    if (casePersonView != null)
    {
      try
      {
        editing = CasesModuleBean.getPort(false)
          .loadCasePerson(casePersonView.getCasePersonId());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      create();
    }
  }   

  @Override
  public void store()
  {
    try
    { 
      editing.setCaseId(getObjectId());
      
      if (editing.getCasePersonTypeId() == null)
        editing.setCasePersonTypeId(DictionaryConstants.CASE_PERSON_TYPE);
  
      CasesModuleBean.getPort(false).storeCasePerson(editing);
      load();
      editing = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    info("CANCEL_OBJECT");
    editing = null;
  }

  public void remove(CasePersonView row)
  {
    try
    {
      if (row != null)
      {
        String casePersonId = row.getCasePersonId();
        CasesModuleBean.getPort(false).removeCasePerson(casePersonId);
        load();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (CasePerson)stateArray[0];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void showDialog()
  {
    try
    {
      PrimeFaces current = PrimeFaces.current();
      current.executeScript("PF('casePersonsDialog').show();");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  private String getTabBaseTypeId()
  {    
    EditTab editTab = caseObjectBean.getActiveEditTab();
    return editTab.getProperties().getString("typeId");
  }  
}
