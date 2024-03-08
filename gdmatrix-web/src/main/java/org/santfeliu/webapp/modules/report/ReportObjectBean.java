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
package org.santfeliu.webapp.modules.report;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.report.ParameterDefinition;
import org.matrix.report.Report;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.util.ComponentUtils;
import static org.santfeliu.webapp.util.FormImporter.ACTION_METHOD_OPTION;
import static org.santfeliu.webapp.util.FormImporter.ACTION_UPDATE_OPTION;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class ReportObjectBean extends ObjectBean
{
  private Report report = new Report();
  private int firstRow;
  private boolean executing = false;
  private String formSelector;
  private Map<String, String> parameters = new HashMap<>();

  @Inject
  ReportFinderBean reportFinderBean;
  
  @Inject
  ReportTypeBean reportTypeBean;
  
  @Inject
  NavigatorBean navigatorBean;  
  
  @Inject
  ReportViewerBean reportViewerBean;    

  @PostConstruct
  public void init()
  {
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public Report getReport()
  {
    return report;
  }

  public void setReport(Report report)
  {
    this.report = report;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  public Map getParameters()
  {
    return parameters;
  }

  public void setParameters(Map parameters)
  {
    this.parameters = parameters;
  }



  @Override
  public Report getObject()
  {
    return isNew() ? null : report;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(report.getReportId());
  }

  public String getDescription(String reportId)
  {
    return getTypeBean().getDescription(reportId);
  }

  @Override
  public FinderBean getFinderBean()
  {
    return reportFinderBean;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.REPORT_TYPE;
  }
  
  public String getContent()
  {
    if (executing)
    {
      executing = false;
      return reportViewerBean.getContent();
    }
    else
    {
      return navigatorBean.getContent();
    }
  }
  
  @Override
  public void loadObject() throws Exception
  {
    formSelector = null;
    parameters.clear();
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      report = ReportModuleBean.getPort().loadReport(objectId, false);
      String formName = 
        DictionaryUtils.getPropertyValue(report.getProperty(), "form");    
      if (formName != null)
      {
        formSelector = "form:" + formName;
        report.getParameterDefinition().stream()
          .forEach(pd -> parameters.put(pd.getName(), pd.getDefaultValue()));     
      }
    }
    else
      report = new Report();
  }
 
  public void executeReport(String outputFormat, boolean targetBlank)
  {
    try
    {
      if (outputFormat != null)
        reportViewerBean.setOutputFormat(outputFormat);
      if (!parameters.isEmpty())
      {
        for (ParameterDefinition pd : report.getParameterDefinition())
        {
          String value = parameters.get(pd.getName());
          if (value != null)
            pd.setDefaultValue(value);
        }
      }
      reportViewerBean.executeReport(report, targetBlank);
      executing = true;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  @Override
  public boolean isEditable()
  {
    return false;
  }
  
  @Override
  public Serializable saveState()
  {
    return report;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.report = (Report)state;
  }

  @Override
  public TypeBean getTypeBean()
  {
    return reportTypeBean;
  }
  
  public void loadDynamicComponents(ComponentSystemEvent event)
  {
    UIComponent panel = event.getComponent();
    updateComponents(panel);
  }  
  
  public void doAction(String name, String value)
  {
    parameters.put(name, value);
  }  
  
  private void updateComponents(UIComponent panel)
  {
    try
    {
      HtmlOutputText hidden =
        (HtmlOutputText)panel.findComponent("form_selector");
      String actualFormSelector = (String)hidden.getStyleClass();

      if (formSelector != null && !formSelector.equals(actualFormSelector))
      {
        hidden.setStyleClass(formSelector);

        panel.getChildren().clear();

        Map<String, Object> options = new HashMap<>();
        options.put(ACTION_METHOD_OPTION, "reportObjectBean.doAction");
        options.put(ACTION_UPDATE_OPTION, ":mainform:cnt");

        ComponentUtils.includeFormComponents(panel, formSelector,
           "reportObjectBean.parameters", "reportObjectBean.parameters", parameters, options);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  

}
