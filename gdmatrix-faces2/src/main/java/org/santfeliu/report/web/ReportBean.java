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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.dic.Property;
import org.matrix.doc.State;
import org.matrix.report.ParameterDefinition;
import org.matrix.report.Report;
import org.matrix.report.ReportManagerPort;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.security.util.URLCredentialsCipher;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author unknown
 */
@CMSManagedBean
public class ReportBean extends WebBean implements Serializable
{
  @CMSProperty(mandatory=true)
  public static final String REPORT_NAME_PROPERTY = "reportName";
  @CMSProperty
  public static final String FORM_NAME_PROPERTY = "formName";
  @CMSProperty
  public static final String FORM_TYPE_PROPERTY = "formType";
  @CMSProperty
  public static final String CONNECTION_NAME_PROPERTY = "connectionName";
  @CMSProperty
  public static final String OUTPUT_FORMAT_PROPERTY = "outputFormat";
  @CMSProperty
  public static final String EXECUTE_BUTTON_LABEL_PROPERTY =
    "executeButtonLabel";
  @CMSProperty
  public static final String PRINT_REPORT_NAME_PROPERTY = "printReportName";
  @CMSProperty
  public static final String PRINT_BUTTON_LABEL_PROPERTY = "printButtonLabel";
  @CMSProperty
  public static final String PRINT_BUTTON_RENDERED_PROPERTY =
    "printButtonRendered";
  @CMSProperty
  public static final String RUN_AS_ADMIN_PROPERTY = "runAsAdmin";
  @CMSProperty
  public static final String SHOW_IN_IFRAME_PROPERTY = "showInIFrame";
  @CMSProperty
  public static final String HEADER_DOCID_PROPERTY = "header.docId";
  @CMSProperty
  public static final String FOOTER_DOCID_PROPERTY = "footer.docId";
  @CMSProperty
  public static final String SPREAD_REQUEST_PARAMETERS_PROPERTY = "spreadRequestParameters"; 
  @CMSProperty
  public static final String RENDER_BUTTON_PROPERTY = "renderExecuteButton";  
  @CMSProperty
  public static final String ALLOWED_TAGS_PROPERTY = "allowedHtmlTags";    
  @CMSProperty
  public static final String READ_TIMEOUT_PROPERTY = "readTimeout";   

  private static final String STORED_PARAMS = "storedParams";
  
  private boolean reportRendered = false;
  private Map parameters;

  public ReportBean()
  {
  }

  /* bean properties */

  public String getReportName()
  {    
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    MenuItemCursor cursor = menuModel.getSelectedMenuItem();
    return cursor.getBrowserSensitiveProperty(REPORT_NAME_PROPERTY);
  }

  public boolean isReportRendered()
  {
    return reportRendered;
  }

  public boolean isFormRendered()
  {
    return getFormName() != null;
  }
  
  public boolean isExecuteButtonRendered()
  {
    return (getProperty(RENDER_BUTTON_PROPERTY) == null || 
      "true".equalsIgnoreCase(getProperty(RENDER_BUTTON_PROPERTY)));
  }

  public String getFormURL()
  {
    String url = null;
    String formName = getFormName();
    if (formName != null)
    {
      try
      {
        String formType = getFormType();
        if (formType == null) formType = "workflow.form";
        DocumentManagerClient client = getDocumentManagerClient();
        DocumentFilter documentFilter = new DocumentFilter();
        documentFilter.setDocTypeId("FORM");
        Property property = new Property();
        property.setName(formType);
        property.getValue().add(formName);
        documentFilter.getProperty().add(property);
        documentFilter.getStates().add(State.DRAFT);
        documentFilter.getStates().add(State.COMPLETE);
        documentFilter.getStates().add(State.RECORD);
        List<Document> documents = client.findDocuments(documentFilter);
        if (documents.size() > 0)
        {
          String docId = documents.get(0).getDocId();
          url = getConnectionBase() + "/documents/" + docId;
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return url;
  }

  public String getHeaderURL()
  {
    String url = null;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getMenuModel().getSelectedMenuItem();
    String docId = cursor.getProperty(HEADER_DOCID_PROPERTY);
    if (docId != null)
    {
      url = getConnectionBase() + "/documents/" + docId;
    }
    return url;
  }

  public String getFooterURL()
  {
    String url = null;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getMenuModel().getSelectedMenuItem();
    String docId = cursor.getProperty(FOOTER_DOCID_PROPERTY);
    if (docId != null)
    {
      url = getConnectionBase() + "/documents/" + docId;
    }
    return url;
  }

  public String getReportURL()
  {
    String url = null;
    String reportName = getReportName();
    if (reportName != null)
    {
      url = getContextURL() + "/reports/" + reportName + "." +
        getOutputFormat() + getParametersString();
      Credentials credentials = ReportConfigBean.getExecutionCredentials();
      URLCredentialsCipher cipher = SecurityUtils.getURLCredentialsCipher();
      url = cipher.putCredentials(url, credentials);
    }
    return url;
  }

  public String getExecuteButtonLabel()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.translateProperty(EXECUTE_BUTTON_LABEL_PROPERTY);
  }

  public String getOutputFormat()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getMenuModel().getSelectedMenuItem();
    String outputFormat = cursor.getProperty(OUTPUT_FORMAT_PROPERTY);
    return outputFormat == null ? "html" : outputFormat;
  }

  public boolean isShowInIFrame()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getMenuModel().getSelectedMenuItem();
    return "true".equals(cursor.getProperty(SHOW_IN_IFRAME_PROPERTY));
  }

  public String getPrintReportName()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getMenuModel().getSelectedMenuItem();
    String printReportName = cursor.getProperty(PRINT_REPORT_NAME_PROPERTY);
    if (printReportName == null)
    {
      printReportName = cursor.getProperty(REPORT_NAME_PROPERTY);
    }
    return printReportName;
  }

  public String getPrintURL()
  {
    String url = null;
    String printReportName = getPrintReportName();
    if (printReportName != null)
    {
      url = getContextURL() + "/reports/" + printReportName + ".pdf" +
        getParametersString();
      Credentials credentials = ReportConfigBean.getExecutionCredentials();
      URLCredentialsCipher cipher = SecurityUtils.getURLCredentialsCipher();
      url = cipher.putCredentials(url, credentials);
    }
    return url;
  }

  public String getPrintButtonLabel()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getMenuModel().getSelectedMenuItem();
    return cursor.getProperty(PRINT_BUTTON_LABEL_PROPERTY);
  }
  
  public boolean isPrintButtonRendered()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getMenuModel().getSelectedMenuItem();
    return "true".equals(cursor.getProperty(PRINT_BUTTON_RENDERED_PROPERTY));
  }
  
  public String getAllowedHtmlTags()
  {  
    return getProperty(ALLOWED_TAGS_PROPERTY);
  }
  
  public Integer getReadTimeout()
  {
    String readTimeout = getProperty(READ_TIMEOUT_PROPERTY);
    return readTimeout != null ? Integer.valueOf(readTimeout) : null;  
  }
  
  public Map getParameters()
  {
    if (parameters == null)
    {
      try
      {
        String reportName = getReportName();
        if (reportName == null) throw new Exception("UNDEFINED_REPORT_NAME");
        parameters = getReportDefaultParameters(reportName);
        putRequestParameters(parameters);
      }
      catch (Exception ex)
      {
        parameters = new HashMap();
        error(ex);
      }
    }
    return parameters;
  }
  
  public void setParameters(Map parameters)
  {
    this.parameters = parameters;
  }
  
  /* actions */
  @CMSAction
  public String showForm()
  {
    reportRendered = false;
    return "report";
  }

  @CMSAction
  public String executeReport()
  {
    reportRendered = true;
    return "report";
  }

  /* private methods */

  private String getConnectionBase()
  {
    return "http://localhost:" +
      MatrixConfig.getProperty("org.santfeliu.web.defaultPort") +
      getContextPath();
  }

  private String getParametersString()
  {
    try
    {
      StringBuilder buffer = new StringBuilder();
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String NIF = userSessionBean.getNIF();
      if (NIF != null)
      {
        buffer.append(buffer.length() == 0 ? "?" : "&");
        buffer.append("NIF=").append(NIF);
      }
      String CIF = userSessionBean.getCIF();
      if (CIF != null)
      {
        buffer.append(buffer.length() == 0 ? "?" : "&");
        buffer.append("CIF=").append(CIF);
      }
      String username = userSessionBean.getUsername();
      buffer.append(buffer.length() == 0 ? "?" : "&");
      buffer.append("username=").append(URLEncoder.encode(username, "UTF-8"));

      for (Object e : getParameters().entrySet())
      {
        Map.Entry<String, String> entry = (Map.Entry<String, String>)e;
        String parameter = entry.getKey();
        String value = entry.getValue();
        if (value != null)
        {
          buffer.append(buffer.length() == 0 ? "?" : "&");
          buffer.append(parameter).append("=");
          buffer.append(URLEncoder.encode(value, "UTF-8"));
        }
      }
      MenuItemCursor cursor = 
        userSessionBean.getMenuModel().getSelectedMenuItem();
      String connectionName = cursor.getProperty(CONNECTION_NAME_PROPERTY);
      if (connectionName != null)
      {
        buffer.append(buffer.length() == 0 ? "?" : "&");
        buffer.append(ReportServlet.CONNECTION_NAME_PARAMETER + "=");
        buffer.append(connectionName);
      }
      
      //spread url parameters
      List<String> spreadParameters = 
        cursor.getMultiValuedProperty(SPREAD_REQUEST_PARAMETERS_PROPERTY);
      if (spreadParameters != null)
      {
        Map requestParams = getExternalContext().getRequestParameterMap();
        Map<String, String> storedParams = 
          (Map<String, String>)userSessionBean.getAttribute(STORED_PARAMS);
        if (storedParams == null)
        {
          storedParams = new HashMap<String, String>();
          userSessionBean.setAttribute(STORED_PARAMS, storedParams);
        }
        
        for (String spParam : spreadParameters)
        {
          if (!spParam.equalsIgnoreCase("username")
           && !spParam.equalsIgnoreCase("CIF")
           && !spParam.equalsIgnoreCase("NIF"))  //avoid override of internal params
          {
            String value = cursor.getProperty("parameter_" + spParam);
            if (value == null)
            {
              value = (String)requestParams.get(spParam);
              if (value == null)
              {
                value = storedParams.get(spParam);
              }
            }
            if (value != null)
            {
              storedParams.put(spParam, value);
              buffer.append(buffer.length() == 0 ? "?" : "&");
              buffer.append(spParam).append("=");
              buffer.append(value);
            }
          }
        }
      }
      System.out.println("REPORT URL>>>> " + buffer.toString());
      return buffer.toString();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private Map getReportDefaultParameters(String reportName) throws Exception
  {
    Map defaultParams = new HashMap();
    ReportManagerPort port = ReportConfigBean.getReportManagerPort(
      ReportConfigBean.getReportAdminCredentials());
    Report report = port.loadReport(reportName, false);
    List<ParameterDefinition> paramDefs = report.getParameterDefinition();
    for (ParameterDefinition paramDef : paramDefs)
    {
      defaultParams.put(paramDef.getName(), paramDef.getDefaultValue());
    }
    return defaultParams;
  }
  
  private void putRequestParameters(Map parameters)
  {
    Map requestParameters = getExternalContext().getRequestParameterMap();        
    for (Object paramName : parameters.keySet())
    {
      String paramValue = (String) requestParameters.get(paramName);
      if (paramValue != null)
        parameters.put(paramName, paramValue);
    }    
  }  
  
  private String getFormName()
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    MenuItemCursor cursor = menuModel.getSelectedMenuItem();
    return cursor.getBrowserSensitiveProperty(FORM_NAME_PROPERTY);
  }

  private String getFormType()
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    MenuItemCursor cursor = menuModel.getSelectedMenuItem();
    return cursor.getBrowserSensitiveProperty(FORM_TYPE_PROPERTY);
  }  

  private DocumentManagerClient getDocumentManagerClient() throws Exception
  {
    DocumentManagerClient client = new DocumentManagerClient();
    return client;
  }
}
