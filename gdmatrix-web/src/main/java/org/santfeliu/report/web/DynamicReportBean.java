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
package org.santfeliu.report.web;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.report.Report;
import org.matrix.report.ReportManagerPort;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormFactory;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author realor
 */
public class DynamicReportBean extends WebBean implements Savable
{
  @CMSProperty
  public static final String REPORT_TITLE_PROPERTY = "reportTitle";
  @CMSProperty
  public static final String REPORT_LABEL_PROPERTY = "reportLabel";
  @CMSProperty
  public static final String REPORTS_PROPERTY = "reports";
  @CMSProperty
  public static final String FORM_SELECTOR_PROPERTY = "formSelector";
  @CMSProperty
  public static final String EXECUTE_BUTTON_LABEL_PROPERTY = "executeButtonLabel";
  
  private HashMap parameters = new HashMap();
  private String reportId;
  private String formSelector;
  private ArrayList<ReportSelectItem> reportSelectItems =
    new ArrayList<ReportSelectItem>();

  public Map getParameters()
  {
    return parameters;
  }

  public void setParameters(Map map)
  {
    parameters.putAll(map);
  }

  public void setReportId(String reportId)
  {
    this.reportId = reportId;
    boolean found = false;
    int i = 0;
    while (!found && i < reportSelectItems.size())
    {
      if (reportSelectItems.get(i).getReportId().equals(reportId))
        found = true;
      else i++;
    }
    if (found)
    {
      formSelector = reportSelectItems.get(i).getFormSelector();
    }
  }

  public String getReportId()
  {
    return reportId;
  }

  public ArrayList<ReportSelectItem> getReportSelectItems()
  {
    return reportSelectItems;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  public boolean isReportSelectorRendered()
  {
    return reportSelectItems.size() > 1;
  }

  public String getExecuteButtonLabel()
  {
    String label = getProperty(EXECUTE_BUTTON_LABEL_PROPERTY);
    if (label == null) label = "Execute";
    return label;
  }

  public Form getForm()
  {
    try
    {
      if (formSelector != null)
      {
        // in post back always use cache
        FormFactory formFactory = FormFactory.getInstance();

        if (getFacesContext().getRenderResponse())
        {
          formFactory.clearForm(formSelector);
        }
        return formFactory.getForm(formSelector, parameters);
      }
    }
    catch (Exception ex)
    {
      error(ex);      
    }
    return null;
  }

  public boolean isPdfRendered()
  {
    return (!StringUtils.isBlank(reportId));
  }

  public String getPdfUrl()
  {
    try
    {
      StringBuilder buffer = new StringBuilder();
      buffer.append(getBaseUrl());
      buffer.append("/reports/");
      buffer.append(reportId);
      buffer.append(".pdf");
      Set<String> keySet = parameters.keySet();
      boolean first = true;
      for (String parameter : keySet)
      {
        Object value = parameters.get(parameter);
        if (value != null)
        {
          if (first)
          {
            buffer.append("?");
            first = false;
          }
          else
          {
            buffer.append("&");
          }
          buffer.append(parameter);
          buffer.append("=");
          buffer.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
      }
      return buffer.toString();
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @CMSAction
  public String show()
  {
    try
    {
      readParameters();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "dynamic_report";
  }

  private String getBaseUrl()
  {
    return getContextURL();
  }

  private void readParameters() throws Exception
  {
    FacesContext facesContext = getFacesContext();
    ExternalContext extContext = facesContext.getExternalContext();
    Object req = extContext.getRequest();
    if (req instanceof HttpServletRequest)
    {
      HttpServletRequest request = (HttpServletRequest)req;
      if ("GET".equals(request.getMethod()))
      {
        parameters.clear();
        parameters.putAll(extContext.getRequestParameterMap());

        loadReportSelectItems();
      }
    }
  }

  private void loadReportSelectItems() throws Exception
  {
    String reports = (String)parameters.get(REPORTS_PROPERTY);
    if (reports == null) getProperty(REPORTS_PROPERTY);
    if (reports != null)
    {
      String defaultFormSelector = getProperty(FORM_SELECTOR_PROPERTY);
      String[] pairs = reports.split(",");
      Credentials credentials = ReportConfigBean.getReportAdminCredentials();
      ReportManagerPort port = 
        ReportConfigBean.getReportManagerPort(credentials);
      for (String pair : pairs)
      {
        int index = pair.indexOf(":");
        String iReportId = (index == -1) ? pair : pair.substring(0, index);
        String iFormSelector = (index == -1) ? 
          defaultFormSelector : pair.substring(index + 1);

        try
        {
          Report report = port.loadReport(iReportId, false);
          if (ReportConfigBean.canUserExecuteReport(report))
          {
            ReportSelectItem item = new ReportSelectItem();
            item.setReportId(iReportId);
            String title = report.getTitle();
            index = title.indexOf(":");
            if (index != -1) title = title.substring(index + 1);
            item.setLabel(title);
            item.setFormSelector(iFormSelector);
            reportSelectItems.add(item);
          }
        }
        catch (Exception ex)
        {
          // report not found
        }
      }
      if (!reportSelectItems.isEmpty())
      {
        setReportId(reportSelectItems.get(0).getReportId());
      }
    }
  }
  
  public class ReportSelectItem extends SelectItem implements Serializable
  {
    private String formSelector;

    public void setReportId(String reportId)
    {
      setValue(reportId);
    }

    public String getReportId()
    {
      return String.valueOf(getValue());
    }

    public String getFormSelector()
    {
      return formSelector;
    }

    public void setFormSelector(String formSelector)
    {
      this.formSelector = formSelector;
    }
  }
}
