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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionFilter;
import org.matrix.cases.InterventionView;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.setup.Column;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class CaseInterventionsTabBean extends TabBean
{
  @Inject
  CaseTypeBean caseTypeBean;

  Map<String, TabInstance> tabInstances = new HashMap<>();
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();  

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<DataTableRow> rows;
    int firstRow = 0;
    boolean groupedView = true;
  }

  private Intervention editing;
  private String formSelector;

  @Inject
  CaseObjectBean caseObjectBean;

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
  }

  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = caseObjectBean.getActiveEditTab();
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

  public Intervention getEditing()
  {
    return editing;
  }

  public void setEditing(Intervention editing)
  {
    this.editing = editing;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  public boolean isGroupedView()
  {
    return isGroupedViewEnabled() && getCurrentTabInstance().groupedView;
  }

  public void setGroupedView(boolean groupedView)
  {
    getCurrentTabInstance().groupedView = groupedView;
  }

  public boolean isGroupedViewEnabled()
  {
    return caseObjectBean.getActiveEditTab().getProperties()
      .getBoolean("groupedViewEnabled");
  }

  public String getPersonId()
  {
    return editing != null ? editing.getPersonId() : null;
  }

  public void setPersonId(String personId)
  {
    if (editing == null)
      editing = new Intervention();

    editing.setPersonId(personId);
  }

  public List<DataTableRow> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<DataTableRow> rows)
  {
    this.getCurrentTabInstance().rows = rows;
  }

  public List<Column> getColumns()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getColumns();
    else
      return Collections.EMPTY_LIST;
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }

  public String getStartDateTime()
  {
    if (editing == null)
      return null;

    String startDate = editing.getStartDate() != null ?
      editing.getStartDate() : "";
    String startTime = editing.getStartTime() != null ?
      editing.getStartTime() : "";

    return startDate + startTime;
  }

  public String getEndDateTime()
  {
    if (editing == null)
      return null;

    String endDate = editing.getEndDate() != null ?
      editing.getEndDate() : "";
    String endTime = editing.getEndTime() != null ?
      editing.getEndTime() : "";

    return endDate + endTime;
  }

  public void setStartDateTime(String dateTime)
  {
    String date = null;
    String time = null;

    if (dateTime != null)
    {
      date = dateTime.substring(0, 8);
      time = dateTime.length() > 8 ? dateTime.substring(8) : null;
    }

    if (editing != null)
    {
      editing.setStartDate(date);
      editing.setStartTime(time);
    }
  }

  public void setEndDateTime(String dateTime)
  {
    String date = null;
    String time = null;

    if (dateTime != null)
    {
      date = dateTime.substring(0, 8);
      time = dateTime.length() > 8 ? dateTime.substring(8) : null;
    }

    if (editing != null)
    {
      editing.setEndDate(date);
      editing.setEndTime(time);
    }
  }

  public void setIntTypeId(String intTypeId)
  {
    if (editing != null)
      editing.setIntTypeId(intTypeId);
  }

  public String getIntTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getIntTypeId();
  }

  public void onPersonClear()
  {
    editing.setPersonId(null);
  }

  public void create()
  {
    editing = new Intervention();
    editing.setIntTypeId(getCreationTypeId());
    formSelector = null;
  }

  public void switchView()
  {
    getCurrentTabInstance().groupedView = !getCurrentTabInstance().groupedView;
  }



  @Override
  public void load() throws Exception
  {
    System.out.println("load interventions:" + getObjectId());
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        InterventionFilter filter = new InterventionFilter();
        filter.setCaseId(getObjectId());

        String typeId = getTabBaseTypeId();
        if (typeId != null)
          filter.setIntTypeId(typeId);

        List<InterventionView> interventions =
          CasesModuleBean.getPort(false).findInterventionViews(filter);

        setRows(toDataTableRows(interventions));
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

  public void edit(DataTableRow row)
  {
    String intId = null;
    if (row != null)
      intId = row.getRowId();
       
    try
    {
      if (intId != null)
      {       
        editing = CasesModuleBean.getPort(false).loadIntervention(intId);
      }
      else
      {
        create();
      }
      formSelector = null;        
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public void store() throws Exception
  {
    String caseId = getObjectId();
    editing.setCaseId(caseId);
    CasesModuleBean.getPort(false).storeIntervention(editing);
    refreshHiddenTabInstances();
    load();
    editing = null;
    info("STORE_OBJECT");
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }  
  
  public void remove(DataTableRow row)
  {
    try
    {
      if (row != null)
      {
        String intId = row.getRowId();
        CasesModuleBean.getPort(false).removeIntervention(intId);
        refreshHiddenTabInstances();
        load();
      }
    }
    catch (Exception ex)
    {
      error(ex);
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
    return new Object[]{ editing, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (Intervention)stateArray[0];
      formSelector = (String)stateArray[1];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private List<DataTableRow> toDataTableRows(List<InterventionView>
    interventions) throws Exception
  {
    List<DataTableRow> convertedRows = new ArrayList<>();
    for (InterventionView row : interventions)
    {
      DataTableRow dataTableRow =
        new DataTableRow(row.getIntId(), row.getIntTypeId());
      dataTableRow.setValues(this, row, getColumns());
      convertedRows.add(dataTableRow);
    }
    return convertedRows;
  }

  private void refreshHiddenTabInstances()
  {
    for (TabInstance tabInstance : tabInstances.values())
    {
      if (tabInstance != getCurrentTabInstance())
      {
        tabInstance.objectId = NEW_OBJECT_ID;
      }
    }
  }

}
