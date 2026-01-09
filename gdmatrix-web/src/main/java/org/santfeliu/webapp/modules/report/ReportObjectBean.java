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

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.DocumentConstants;
import org.matrix.report.ParameterDefinition;
import org.matrix.report.Report;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TypeBean;
import static org.santfeliu.webapp.modules.report.ReportModuleBean.REPORTS_SERVLET;
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
  private boolean targetBlank = false;
  private String formSelector;
  private Map<String, Object> parameters = new HashMap<>();

  @Inject
  ReportFinderBean reportFinderBean;
  
  @Inject
  ReportTypeBean reportTypeBean;
  
  @Inject
  NavigatorBean navigatorBean;  
  
  @Inject
  ReportViewerBean reportViewerBean;   
  
  public enum Technology
  {
    SCRIPT("Script"),
    TEMPLATE("Template"),
    JASPER("Jasper reports");
    
    private final String description;
    
    private Technology (String description)
    {
      this.description = description;
    }
    
    public String getId()
    {
      return this.name().toLowerCase();
    }

    public String getDescription()
    {
      return description;
    }
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
    if (targetBlank)
    {
      targetBlank = false;
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
    reportViewerBean.setReportName(null); //Reset current report in viewer
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
  
  public void executeReport(String outputFormat)
  {
    this.executeReport(outputFormat, false);
  }
   
  public void executeReport(String outputFormat, boolean targetBlank)
  {
    try
    {
      if (outputFormat != null)
        reportViewerBean.setOutputFormat(outputFormat);
      if (DictionaryUtils.containsProperty(report, "form") 
        && !parameters.isEmpty())
      {
        for (ParameterDefinition pd : report.getParameterDefinition())
        {
          Object value = parameters.get(pd.getName());
          if (value != null)
          {
            if (value instanceof Collection)
              value = ((Collection)value).stream().findFirst();
            pd.setDefaultValue(String.valueOf(value));
          }
        }
      }

      reportViewerBean.executeReport(report);
      
      this.targetBlank = targetBlank;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void executeExternal(String outputFormat)
  {
    try
    {
      getExternalContext().redirect(getReportURL(outputFormat));
    }
    catch (IOException ex)
    {
      error(ex);
    }
  }
  
  private String getReportURL(String outputFormat)
  {
    String url = null;
    if (report != null)
    {
      url = getContextURL() + REPORTS_SERVLET + report.getReportId() + "." +
        outputFormat + getParametersString();
    }
    return url;
  }
  
  private String getParametersString()
  {
    if (report.getParameterDefinition().isEmpty())
      return "";
    try
    {
      StringBuilder buffer = new StringBuilder();

      for (ParameterDefinition pd : report.getParameterDefinition())
      {
        String parameter = pd.getName();
        String value = pd.getDefaultValue();
        if (value != null)
        {
          buffer.append(buffer.length() == 0 ? "?" : "&");
          buffer.append(parameter).append("=");
          buffer.append(URLEncoder.encode(value, "UTF-8"));
        }
      }

      return buffer.toString();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }  
  
  @Override
  public boolean isEditable()
  {
    if (UserSessionBean.getCurrentInstance().isUserInRole(
      DocumentConstants.DOC_ADMIN_ROLE))
      return true;

    if (!super.isEditable()) return false; //tab protection

    Type currentType =
      TypeCache.getInstance().getType(report.getDocTypeId());
    if (currentType == null) return true;

    Set<AccessControl> acls = new HashSet();
    acls.addAll(currentType.getAccessControl());
    acls.addAll(report.getAccessControl());
    for (AccessControl acl : acls)
    {
      String action = acl.getAction();
      if (DictionaryConstants.WRITE_ACTION.equals(action))
      {
        String roleId = acl.getRoleId();
        if (UserSessionBean.getCurrentInstance().isUserInRole(roleId))
          return true;
      }
    }
    return false;
  }
  
  @Override
  public Serializable saveState()
  {
    return new Object[] { report, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] array = (Object[])state;
    this.report = (Report)array[0];
    this.formSelector = (String)array[1];
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
      
      if (hidden != null)
      {
        String actualFormSelector = hidden.getStyleClass();

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
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  

}
