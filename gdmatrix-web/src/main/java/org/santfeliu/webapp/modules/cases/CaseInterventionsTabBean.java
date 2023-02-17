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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionFilter;
import org.matrix.cases.InterventionView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.PropertyHelper;
import org.santfeliu.webapp.util.ComponentUtils;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class CaseInterventionsTabBean extends TabBean
{
  private List<InterventionView> rows;
  private Intervention editing;  
  private int firstRow;
  private boolean groupedView = true;
  private PropertyHelper propertyHelper;
  
  @Inject
  CaseObjectBean caseObjectBean;

  @PostConstruct
  public void init()
  {
    propertyHelper = new PropertyHelper()
    {
      @Override
      public List<Property> getProperties()
      {
        return editing != null ? editing.getProperty() :
          Collections.emptyList();
      }
    };
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
  }

  public Intervention getEditing()
  {
    return editing;
  }

  public void setEditing(Intervention editing)
  {
    this.editing = editing;
  }

  public boolean isGroupedView()
  {
    return groupedView;
  }

  public void setGroupedView(boolean groupedView)
  {
    this.groupedView = groupedView;
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
    showDialog();    
  }

  public PropertyHelper getPropertyHelper()
  {
    return propertyHelper;
  }

  public List<InterventionView> getRows()
  {
    return rows;
  }

  public void setRows(List<InterventionView> rows)
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
    
    showDialog();
  }
  
  public String getIntTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getIntTypeId();    
  }
  
  public String getBaseIntTypeId()
  {
    //TODO: Get from JSON tabs
    return getProperty("intTypeId");
  }
  
  public void onPersonClear()
  {
    editing.setPersonId(null);
  } 
  
  public void create()
  {
    editing = new Intervention();
    editing.setCaseId(objectId);
    String baseTypeId = getTabBaseTypeId();
    if (baseTypeId != null)
      editing.setIntTypeId(baseTypeId);    
  }
  
  public void switchView()
  {
    groupedView = !groupedView;
  } 
  
  //TODO: get property from JSON  
  private String getTabBaseTypeId()
  {
    String typeId;
        
    String tabPrefix = String.valueOf(caseObjectBean.getDetailSelector());
    typeId = getProperty("tabs::" + tabPrefix + "::typeId");
    if (typeId == null)
      typeId = DictionaryConstants.INTERVENTION_TYPE; 
    
    return typeId;
  }  

  @Override
  public void load() throws Exception
  {
    System.out.println("load interventions:" + objectId);
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      try
      {
        InterventionFilter filter = new InterventionFilter();
        filter.setCaseId(objectId);
        
        String typeId = getTabBaseTypeId();
        if (typeId != null)
          filter.setIntTypeId(typeId);
        
        rows = CasesModuleBean.getPort(false).findInterventionViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      rows = Collections.emptyList();
    }      
  }

  public void edit(InterventionView intView)
  {
    String intId = null;
    if (intView != null)
    {
      intId = intView.getIntId();
    }

    try
    {
      if (intId != null)
      {
        editing = CasesModuleBean.getPort(false).loadIntervention(intId);

        UIComponent panel =
          ComponentUtils.findComponent(":mainform:search_tabs:tabs:int_dyn_form");
        if (panel != null)
        {
          panel.getChildren().clear();
          includeDynamicComponents(panel);        
        }

      }
      else
      {
        create();
      }
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
    load();
    editing = null;    
  }

  public void cancel()
  {
    info("CANCEL_OBJECT");    
    editing = null;
  }

  public void remove(InterventionView row)
  {
    try
    {
      if (row != null)
      {
        String intId = row.getIntId();
        CasesModuleBean.getPort(false).removeIntervention(intId);
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
    return new Object[]{ editing, groupedView };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (Intervention)stateArray[0];
      groupedView = (boolean)stateArray[1];

      if (!isNew()) load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  public void onTypeSelect(SelectEvent<SelectItem> event)
  {
    changeForm();
  }
  
  public void changeForm()
  {
    try
    {
      UIComponent panel =
        ComponentUtils.findComponent(":mainform:search_tabs:tabs:int_dyn_form");
      panel.getChildren().clear();
      includeDynamicComponents(panel);    
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  public void loadDynamicComponents(ComponentSystemEvent event)
  {
    try
    {
      UIComponent panel = event.getComponent();
      if (panel.getChildCount() == 0)
      {
        includeDynamicComponents(panel);
      }
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
      current.executeScript("PF('interventionDataDialog').show();");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  private void includeDynamicComponents(UIComponent parent) throws Exception
  {
    // load dynamic fields

    String formName = getProperty("formName");
    if (!StringUtils.isBlank(formName))
    {
      ComponentUtils.includeFormComponents(parent, formName,
        "caseInterventionsTabBean.propertyHelper.value",
        Collections.emptyMap()); // TODO: take map from intervention
    }
    else
    {
      String scriptName = getProperty("scriptName");
      if (!StringUtils.isBlank(scriptName))
      {
        ComponentUtils.includeScriptComponents(parent, scriptName);
      }
    }
  }  
}
