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
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionFilter;
import org.matrix.cases.InterventionView;
import org.matrix.dic.Property;
import org.primefaces.PrimeFaces;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.util.TextUtils;
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
@ManualScoped
public class CaseInterventionsTabBean extends TabBean
{
  private List<InterventionView> interventionViews;
  private int firstRow;
  private Intervention intervention = new Intervention();
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
        return intervention.getProperty();
      }
    };
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
  }

  public Intervention getIntervention()
  {
    return intervention;
  }

  public void setIntervention(Intervention intervention)
  {
    this.intervention = intervention;
  }
  
  public String getPersonId()
  {
    return intervention.getPersonId();
  }
  
  public void setPersonId(String personId)
  {
    intervention.setPersonId(personId);
    showDialog();    
  }

  public PropertyHelper getPropertyHelper()
  {
    return propertyHelper;
  }

  public List<InterventionView> getInterventionViews()
  {
    return interventionViews;
  }

  public void setInterventionViews(List<InterventionView> interventionViews)
  {
    this.interventionViews = interventionViews;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public Date getStartDateTime()
  {
    if (intervention.getStartDate() != null)
    {
      return TextUtils.parseInternalDate(
        intervention.getStartDate() + intervention.getStartTime());
    }
    else
    {
      return null;
    }
  }

  public Date getEndDateTime()
  {
    if (intervention.getEndDate() != null)
    {
      return TextUtils.parseInternalDate(
        intervention.getEndDate() + intervention.getEndTime());
    }
    else
    {
      return null;
    }
  }

  public void setStartDateTime(Date date)
  {
    if (date != null)
    {
      intervention.setStartDate(TextUtils.formatDate(date, "yyyyMMdd"));
      intervention.setStartTime(TextUtils.formatDate(date, "HHmmss"));
    }
  }

  public void setEndDateTime(Date date)
  {
    if (date != null)
    {
      intervention.setEndDate(TextUtils.formatDate(date, "yyyyMMdd"));
      intervention.setEndTime(TextUtils.formatDate(date, "HHmmss"));
    }
  }
  
  public String getBaseIntTypeId()
  {
    return getProperty("intTypeId");
  }
  
  public void onPersonClear()
  {
    intervention.setPersonId(null);
  }  
  
  public void create()
  {
    intervention = new Intervention();
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
        interventionViews
          = CasesModuleBean.getPort(false).findInterventionViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      interventionViews = Collections.EMPTY_LIST;
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
        intervention = CasesModuleBean.getPort(false).loadIntervention(intId);
        UIComponent panel =
          ComponentUtils.findComponent(":mainform:search_tabs:tabs:dyn_form");

        panel.getChildren().clear();
        includeDynamicComponents(panel);        
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
    intervention.setCaseId(caseId);
    CasesModuleBean.getPort(false).storeIntervention(intervention);
    intervention = new Intervention();
    load();
  }

  public void cancel()
  {
    intervention = new Intervention();
  }

  public void remove(InterventionView interventionView)
  {
    try
    {
      if (interventionView != null)
      {
        String intId = interventionView.getIntId();
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
    return new Object[]{ intervention };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      intervention = (Intervention)stateArray[0];

      if (!isNew()) load();
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
