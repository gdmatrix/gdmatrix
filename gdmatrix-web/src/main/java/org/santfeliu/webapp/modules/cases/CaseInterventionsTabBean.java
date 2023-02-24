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
import org.matrix.dic.Property;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.web.UserSessionBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.ColumnsHelper;
import org.santfeliu.webapp.setup.PropertyMap;
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
  @Inject
  InterventionTypeBean interventionTypeBean;
  
  private ColumnsHelper columnsHelper;
  
  Map<String, TabInstance> tabInstances = new HashMap<>();
  

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<InterventionView> rows;
    int firstRow = 0;
    boolean groupedView = true;
  }

  private Intervention editing;
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
    
    columnsHelper = new ColumnsHelper<InterventionView>(interventionTypeBean)
    {
      @Override
      public List<InterventionView> getRows()
      {
        TabInstance tabInstance = getCurrentTabInstance();
        return tabInstance.rows;
      }

      @Override
      public InterventionView getRowData(InterventionView row)
      {
        try
        {
          Intervention intervention = 
            CasesModuleBean.getPort(false).loadIntervention(row.getIntId());
          row.getProperty().addAll(intervention.getProperty());
          return row;
        }
        catch (Exception ex)
        {
          return null;
        }        
      }  
    };
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

  public boolean isGroupedView()
  {
    return getCurrentTabInstance().groupedView;
  }

  public void setGroupedView(boolean groupedView)
  {
    getCurrentTabInstance().groupedView = groupedView;
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
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<InterventionView> rows)
  {
    this.getCurrentTabInstance().rows = rows;
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }

  public ColumnsHelper getColumnsHelper()
  {
    return columnsHelper;
  }

  public void setColumnsHelper(ColumnsHelper columnsHelper)
  {
    this.columnsHelper = columnsHelper;
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
    editing.setCaseId(getObjectId());
    String baseTypeId = getTabBaseTypeId();
    if (baseTypeId != null)
      editing.setIntTypeId(baseTypeId);
  }

  public void switchView()
  {
    getCurrentTabInstance().groupedView = !getCurrentTabInstance().groupedView;
  }

  private String getTabBaseTypeId()
  {    
    EditTab editTab = caseObjectBean.getActiveEditTab();
    return editTab.getProperties().getString("typeId");
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

        getCurrentTabInstance().rows =
          CasesModuleBean.getPort(false).findInterventionViews(filter);
        
        
        EditTab editTab = caseObjectBean.getActiveEditTab();
        if (editTab != null)
        {
          columnsHelper.setColumns(editTab.getColumns());
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
          ComponentUtils.findComponent(":mainform:search_tabs:int_dyn_form");
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
    return new Object[]{ editing, columnsHelper };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (Intervention)stateArray[0];
      columnsHelper = (ColumnsHelper)stateArray[1];

      load();
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
        ComponentUtils.findComponent(":mainform:search_tabs:int_dyn_form");
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
    PropertyMap properties = caseObjectBean.getActiveEditTab().getProperties();
    String formName = properties.getString("formName");
    if (formName == null && editing != null)
      formName = findForm(editing.getIntTypeId());

    if (!StringUtils.isBlank(formName))
    {
      ComponentUtils.includeFormComponents(parent, formName,
        "caseInterventionsTabBean.propertyHelper.value",
        Collections.emptyMap()); // TODO: take map from intervention
    }
    else
    {
      String scriptName = properties.getString("scriptName");
      if (!StringUtils.isBlank(scriptName))
      {
        ComponentUtils.includeScriptComponents(parent, scriptName);
      }
    }
  }

  private String findForm(String typeId)
  {
    String selector = null;
    
    if (typeId != null)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String selectorBase
        = TypeFormBuilder.PREFIX + ":" + typeId
        + TypeFormBuilder.USERID + userSessionBean.getUserId()
        + TypeFormBuilder.PASSWORD + userSessionBean.getPassword();
      FormFactory formFactory = FormFactory.getInstance();
      List<FormDescriptor> descriptors = formFactory.findForms(selectorBase);
      for (FormDescriptor descriptor : descriptors)
      {
        //TODO: List of selectors
        selector = descriptor.getSelector();
      }
    }

    return selector;
  }
  

}
