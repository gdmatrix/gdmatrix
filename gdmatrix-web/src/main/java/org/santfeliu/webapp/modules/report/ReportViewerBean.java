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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.State;
import org.matrix.report.ParameterDefinition;
import org.matrix.report.Report;
import org.matrix.report.ReportManagerPort;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.report.web.ReportServlet;
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
 * @author blanquepa
 */
@CMSManagedBean
@Named
@RequestScoped
public class ReportViewerBean extends WebBean implements Serializable
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
  public static final String SPREAD_REQUEST_PARAMETERS_PROPERTY = 
    "spreadRequestParameters"; 
  @CMSProperty
  public static final String RENDER_BUTTON_PROPERTY = "renderExecuteButton";  
  @CMSProperty
  public static final String ALLOWED_TAGS_PROPERTY = "allowedHtmlTags";    
  @CMSProperty
  public static final String READ_TIMEOUT_PROPERTY = "readTimeout";   

  private static final String STORED_PARAMS = "storedParams";
  
  private boolean reportRendered = false;
  private Map parameters;
  private Map formValues;
  
  public ReportViewerBean()
  {
  }

  public String getContent()
  {
    return "/pages/report/report.xhtml";
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
          Content content = documents.get(0).getContent();
          if (content != null)
            url = getConnectionBase() + "/documents/" + content.getContentId();
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
    MenuItemCursor cursor = 
      userSessionBean.getMenuModel().getSelectedMenuItem();
    String docId = cursor.getProperty(HEADER_DOCID_PROPERTY);
    if (docId != null && !"none".equals(docId))
    {
      url = getConnectionBase() + "/documents/" + docId;
    }
    return url;
  }

  public String getFooterURL()
  {
    String url = null;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = 
      userSessionBean.getMenuModel().getSelectedMenuItem();
    String docId = cursor.getProperty(FOOTER_DOCID_PROPERTY);
    if (docId != null && !"none".equals(docId))
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
      Credentials credentials = ReportModuleBean.getExecutionCredentials();
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
    MenuItemCursor cursor = 
      userSessionBean.getMenuModel().getSelectedMenuItem();
    String outputFormat = cursor.getProperty(OUTPUT_FORMAT_PROPERTY);
    return outputFormat == null ? "html" : outputFormat;
  }

  public boolean isShowInIFrame()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = 
      userSessionBean.getMenuModel().getSelectedMenuItem();
    return "true".equals(cursor.getProperty(SHOW_IN_IFRAME_PROPERTY));
  }

  public String getPrintReportName()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = 
      userSessionBean.getMenuModel().getSelectedMenuItem();
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
      Credentials credentials = ReportModuleBean.getExecutionCredentials();
      URLCredentialsCipher cipher = SecurityUtils.getURLCredentialsCipher();
      url = cipher.putCredentials(url, credentials);
    }
    return url;
  }

  public String getPrintButtonLabel()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = 
      userSessionBean.getMenuModel().getSelectedMenuItem();
    return cursor.getProperty(PRINT_BUTTON_LABEL_PROPERTY);
  }
  
  public boolean isPrintButtonRendered()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = 
      userSessionBean.getMenuModel().getSelectedMenuItem();
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

  public Map getFormValues()
  {
    if (formValues == null)
    {
      formValues = new HashMap();
      formValues = parameters;
      putRequestParameters(formValues); //Set fresh request parameters
      putUserParameters(formValues); //Put user related parameters
    }

    return formValues;
  }

  public Map getParameters()
  {
    return parameters;
  }
  
  public void setParameters(Map parameters)
  {
    this.parameters = parameters;
  }
  
  public void setParameters() throws Exception
  {
    String reportName = getReportName();
    if (reportName == null) throw new Exception("UNDEFINED_REPORT_NAME");
    parameters = getReportDefaultParameters(reportName);       
    putRequestParameters(parameters);
    putFormValues(parameters);
    putUserParameters(parameters);
  }    
  
  /* actions */
  
  /** Report loading action.
   * 
   * @return outcome
   */
  @CMSAction
  public String showForm()
  {
    reportRendered = false;
    return "report";
  }

  /** Report execution action. 
   *  If current node configures oc properties (oc.pageBean and oc.objectBean) 
   *  then calls ControllerBean.showObject to benefit from pageHistory and be 
   *  able to navigate through objects, otherwise self show() method is called
   *  directly for compatibility reasons. 
   * 
   *  @return outcome
   */
  @CMSAction
  public String executeReport()
  {
    String outcome = null;
    
    try    
    {     
      setParameters();
      //TODO: Report as an Object (execution with NavigatorBean)
      outcome = show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    
    return outcome;
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
    ReportManagerPort port = ReportModuleBean.getPort(
      ReportModuleBean.getReportAdminCredentials());
    Report report = port.loadReport(reportName, false);
    List<ParameterDefinition> paramDefs = report.getParameterDefinition();
    for (ParameterDefinition paramDef : paramDefs)
    {
      defaultParams.put(paramDef.getName(), paramDef.getDefaultValue());
    }
    return defaultParams;
  }
  
  private void putUserParameters(Map parameters) 
  {
    //Injected from UserSessionBean 
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    
    String NIF = userSessionBean.getNIF();
    if (NIF != null)
      parameters.put("NIF", NIF);
    String CIF = userSessionBean.getCIF();
    if (CIF != null)
      parameters.put("CIF", CIF);
    Boolean representant = userSessionBean.isRepresentant();
    if (representant != null)
      parameters.put("CIF_REPRESENTANT", String.valueOf(representant));
    String username = userSessionBean.getUsername();
    parameters.put("username", username);
    
    //Injected from CMS Node
    MenuItemCursor cursor = 
      userSessionBean.getMenuModel().getSelectedMenuItem();
    
    String connectionName = 
      cursor.getProperty(ReportServlet.CONNECTION_NAME_PARAMETER );
    if (connectionName != null)
      parameters.put(ReportServlet.CONNECTION_NAME_PARAMETER , connectionName);          
  }
  
  private void putRequestParameters(Map parameters)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = 
      userSessionBean.getMenuModel().getSelectedMenuItem(); 

    //spread request parameters
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
        //1. Node parameter
        String value = cursor.getProperty("parameter_" + spParam);
        if (value == null)
        {
          //2. URL parameter
          value = (String)requestParams.get(spParam);
          if (value == null)
          {
            //3. In Session parameter
            value = storedParams.get(spParam);
          }
        }
        parameters.put(spParam, value);
        if (value != null)
          storedParams.put(spParam, value);            
      }
    }
  } 
  
  private void putFormValues(Map parameters)
  {
    if (formValues != null)
    {
      parameters.putAll(formValues); 
      formValues = null; //Reset form values to force refresh
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
    Credentials credentials = ReportModuleBean.getReportAdminCredentials();
    String userId = credentials.getUserId();
    String password = credentials.getPassword();
    DocumentManagerClient client = new DocumentManagerClient(userId, password);
    return client;
  }

  public String show() {
    reportRendered = true;
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  public String getObjectTypeId() {
    return DictionaryConstants.REPORT_TYPE;
  }
}
