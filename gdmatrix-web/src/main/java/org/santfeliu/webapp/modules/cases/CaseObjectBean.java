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
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.primefaces.PrimeFaces;
import org.santfeliu.util.TextUtils;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.Tab;
import org.santfeliu.webapp.helpers.PropertyHelper;
import org.santfeliu.webapp.helpers.TypedHelper;
import org.santfeliu.webapp.util.ComponentUtils;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class CaseObjectBean extends ObjectBean
{
  private Case cas = new Case();
  private PropertyHelper propertyHelper;
  private TypedHelper typedHelper;

  @Inject
  CaseTypeBean caseTypeBean;

  @Inject
  CaseFinderBean caseFinderBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);

    propertyHelper = new PropertyHelper()
    {
      @Override
      public List<Property> getProperties()
      {
        return cas.getProperty();
      }
    };

    typedHelper = new TypedHelper()
    {
      @Override
      public String getTypeId()
      {
        return isNew() ? getBaseTypeInfo().getBaseTypeId() : 
          cas.getCaseTypeId();
      }
    };
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CASE_TYPE;
  }

  @Override
  public CaseTypeBean getTypeBean()
  {
    return caseTypeBean;
  }

  @Override
  public CaseFinderBean getFinderBean()
  {
    return caseFinderBean;
  }

  public PropertyHelper getPropertyHelper()
  {
    return propertyHelper;
  }

  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : cas.getTitle();
  }

  @Override
  public Case getObject()
  {
    return isNew() ? null : cas;
  }

  public Case getCase()
  {
    return cas;
  }

  public void setCase(Case cas)
  {
    this.cas = cas;
  }

  @Override
  public String show()
  {
    return "/pages/cases/case.xhtml";
  }

  public Date getStartDateTime()
  {
    if (cas != null && cas.getStartDate() != null)
    {
      return getDate(cas.getStartDate(), cas.getStartTime());
    }
    else
    {
      return null;
    }
  }

  public Date getEndDateTime()
  {
    if (cas != null && cas.getEndDate() != null)
    {
      return getDate(cas.getEndDate(), cas.getEndTime());
    }
    else
    {
      return null;
    }
  }

  public void setStartDateTime(Date date)
  {
    if (cas != null)
    {
      if (date == null)
      {
        date = new Date();
      }
      cas.setStartDate(TextUtils.formatDate(date, "yyyyMMdd"));
      cas.setStartTime(TextUtils.formatDate(date, "HHmmss"));
    }
  }

  public void setEndDateTime(Date date)
  {
    if (date != null && cas != null)
    {
      cas.setEndDate(TextUtils.formatDate(date, "yyyyMMdd"));
      cas.setEndTime(TextUtils.formatDate(date, "HHmmss"));
    }
  }

  private Date getDate(String date, String time)
  {
    String dateTime = TextUtils.concatDateAndTime(date, time);
    return TextUtils.parseInternalDate(dateTime);
  }

  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
      cas = CasesModuleBean.getPort(false).loadCase(objectId);
    else
      cas = new Case();

    if (PrimeFaces.current().isAjaxRequest())
    {
      UIComponent panel
        = ComponentUtils.findComponent(":mainform:search_tabs:tabs:dyn_form");

      panel.getChildren().clear();
      includeDynamicComponents(panel);
    }
  }

  @Override
  public void loadTabs()
  {
    super.loadTabs();

    if (tabs.isEmpty())
    {
      tabs = new ArrayList<>(); // empty list may be read only
      tabs.add(new Tab("Main", "/pages/cases/case_main.xhtml"));
      tabs.add(new Tab("Persons", "/pages/cases/case_persons.xhtml", "casePersonsTabBean"));
      tabs.add(new Tab("Documents", "/pages/cases/case_documents.xhtml", "caseDocumentsTabBean"));
      tabs.add(new Tab("Actuacions", "/pages/cases/case_interventions.xhtml", "caseInterventionsTabBean"));
      tabs.add(new Tab("Cases", "/pages/cases/case_cases.xhtml", "caseCasesTabBean"));
      tabs.add(new Tab("Cases2", "/pages/cases/case_cases.xhtml", "caseCasesTabBean"));
      tabs.add(new Tab("Actuacions2", "/pages/cases/case_interventions.xhtml", "caseInterventionsTabBean"));      
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    cas = CasesModuleBean.getPort(false).storeCase(cas);
    setObjectId(cas.getCaseId());

    caseFinderBean.outdate();
  }

  @Override
  public Serializable saveState()
  {
    return cas;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.cas = (Case) state;
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

  private void includeDynamicComponents(UIComponent parent) throws Exception
  {
    // load dynamic fields

    String formName = getProperty("formName");
    if (!StringUtils.isBlank(formName))
    {
      ComponentUtils.includeFormComponents(parent, formName,
        "caseObjectBean.propertyHelper.value",
        Collections.emptyMap()); // TODO: take map from cas
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
