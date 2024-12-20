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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
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
import org.santfeliu.form.Form;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.builder.DocumentFormBuilder;
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
import org.santfeliu.webapp.util.ComponentUtils;

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
  public static final String FORM_TYPE_FLEX = "flex";
  public static final String FORM_TYPE_DYNAMIC = "dynamic";
  public static final String FORM_TYPE_HTML = "html";
  public static final String DEFAULT_FORM_NAME_PROPERTY = "workflow.form";
  public static final String HTML_FORM_NAME_PROPERTY = "workflow.html";

  @CMSProperty
  public static final String OUTPUT_FORMAT_PROPERTY = "outputFormat";
  public static final String HTML_OUTPUT_FORMAT = "html";
  public static final String PDF_OUTPUT_FORMAT = "pdf";

  @CMSProperty
  public static final String EXECUTE_BUTTON_LABEL_PROPERTY =
    "executeButtonLabel";

  @CMSProperty
  public static final String PRINT_REPORT_NAME_PROPERTY = "printReportName";
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

  private String reportName;
  private Map parameters = new HashMap<>();
  private String reportTemplate;
  private String outputFormat;

  private Map formValues = new HashMap<>();
  private String formSelector;

  public ReportViewerBean()
  {
  }

  public String getContent()
  {
    return "/pages/report/report_viewer.xhtml";
  }

  //Accessors
  public void setFormValues(Map formValues)
  {
    this.formValues = formValues;
  }

  public Map getFormValues()
  {
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

  private String getReportName()
  {
    return reportName;
  }

  public void setReportName(String reportName)
  {
    this.reportName = reportName;
  }

  public void setOutputFormat(String outputFormat)
  {
    this.outputFormat = outputFormat;
  }

  public String getOutputFormat()
  {
    if (outputFormat != null)
      return outputFormat;

    outputFormat = getSelectedMenuItem().getProperty(OUTPUT_FORMAT_PROPERTY);

    return outputFormat == null ? HTML_OUTPUT_FORMAT : outputFormat;
  }

  public String getReportTemplate()
  {
    if (reportTemplate != null)
      return reportTemplate;

    reportTemplate = getSelectedMenuItem().getProperty("reportTemplate");

    return reportTemplate == null ? "default" : reportTemplate;
  }

  public void setReportTemplate(String reportTemplate)
  {
    this.reportTemplate = reportTemplate;
  }

  //Report methods
  private String getReportURL(String reportName, boolean addCredentials)
  {
    String url = null;
    if (reportName != null)
    {
      url = getContextURL() + "/reports/" + reportName + "." +
        getOutputFormat() + getParametersString();
      if (addCredentials)
      {
        String runAsAdmin =
          getSelectedMenuItem().getProperty(RUN_AS_ADMIN_PROPERTY);
        Credentials credentials =
          ReportModuleBean.getExecutionCredentials("true".equals(runAsAdmin));
        URLCredentialsCipher cipher = SecurityUtils.getURLCredentialsCipher();
        url = cipher.putCredentials(url, credentials);
      }
    }
    return url;
  }

  public String getReportURL()
  {
    return getReportURL(reportName, true);
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

  public boolean isShowInIFrame()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor =
      userSessionBean.getMenuModel().getSelectedMenuItem();
    return "true".equals(cursor.getProperty(SHOW_IN_IFRAME_PROPERTY));
  }

  public boolean isExecuteButtonRendered()
  {
    return (getProperty(RENDER_BUTTON_PROPERTY) == null ||
      "true".equalsIgnoreCase(getProperty(RENDER_BUTTON_PROPERTY)));
  }

  public String getExecuteButtonLabel()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.translateProperty(EXECUTE_BUTTON_LABEL_PROPERTY);
  }

  //Header & footer methods
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

  //Form methods
  public boolean isFormRendered()
  {
    return getFormName() != null;
  }

  public Form getForm()
  {
    try
    {
      if (formSelector != null)
      {
        FormFactory formFactory = FormFactory.getInstance();
        return formFactory.getForm(formSelector, getFormValues());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public void loadDynamicComponents(ComponentSystemEvent event)
  {
    if (isFormRendered())
    {
      UIComponent panel = event.getComponent();
      if (panel.getChildren().isEmpty())
      {
        updateComponents(panel);
      }
    }
  }

  public boolean isRenderFlexForm()
  {
    String formType = getFormType();
    return formType == null || formType.equals(FORM_TYPE_FLEX);
  }

  //Print report methods
  public String getPrintReportName()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor =
      userSessionBean.getMenuModel().getSelectedMenuItem();
    String printReportName = cursor.getProperty(PRINT_REPORT_NAME_PROPERTY);
    if (printReportName == null)
    {
      printReportName = getReportName();
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

      String runAsAdmin =
        getSelectedMenuItem().getProperty(RUN_AS_ADMIN_PROPERTY);
      Credentials credentials =
        ReportModuleBean.getExecutionCredentials("true".equals(runAsAdmin));
      URLCredentialsCipher cipher = SecurityUtils.getURLCredentialsCipher();
      url = cipher.putCredentials(url, credentials);
    }
    return url;
  }

  public boolean isPrintButtonRendered()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor =
      userSessionBean.getMenuModel().getSelectedMenuItem();
    return "true".equals(cursor.getProperty(PRINT_BUTTON_RENDERED_PROPERTY));
  }


  //Actions
  @CMSAction
  public String executeReport()
  {
    String outcome = null;

    try
    {
      outcome = executeReport(reportName, parameters);
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return outcome;
  }

  public String executeReport(Report report)
  {
    String outcome = null;

    try
    {
      Map params = getReportDefaultParameters(report);
      if (StringUtils.isBlank(reportTemplate)) //target _blank
      {
        setParameters(params);
        ExternalContext externalContext =
          FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect(getReportURL(report.getReportId(), false));
      }
      else
        outcome = executeReport(report.getReportId(), params);
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return outcome;
  }

  private String executeReport(String reportName, Map parameters)
    throws Exception
  {
    String outcome;

    if (reportName == null)
    {
      MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
      MenuItemCursor cursor = menuModel.getSelectedMenuItem();
      reportName = cursor.getBrowserSensitiveProperty(REPORT_NAME_PROPERTY);
    }
    if (reportName == null)
      throw new Exception("UNDEFINED_REPORT_NAME");
    setReportName(reportName);

    if (parameters == null)
      parameters = getReportDefaultParameters(reportName);
    setParameters(parameters);

    mergeRequestParameters(parameters);
    mergeFormValues(parameters);
    mergeUserParameters(parameters);

    formSelector = getFormSelector();
    outcome = show();

    return outcome;
  }


  /* private methods */
  private String show()
  {
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  private void updateComponents(UIComponent panel)
  {
    try
    {
      panel.getChildren().clear();
      if (formSelector != null)
      {
        ComponentUtils.includeFormComponents(panel, formSelector,
           "reportViewerBean.formValues", "reportViewerBean.formValues",
           formValues, Collections.EMPTY_MAP);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private String getFormSelector()
  {
    String selector = null;
    String formName = getFormName();
    if (formName == null)
      return selector;

    try
    {
      Document formDocument = getFormDocument(formName);
      if (formDocument != null)
      {
        selector = DocumentFormBuilder.PREFIX + ":" + formDocument.getDocId();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return selector;
  }

  private Document getFormDocument(String formName) throws Exception
  {
    Document form = null;
    String formNameProp = DEFAULT_FORM_NAME_PROPERTY;
    String formType = getFormType();
    if (formType != null && formType.equals(FORM_TYPE_HTML))
      formNameProp = HTML_FORM_NAME_PROPERTY;
    DocumentManagerClient client = getDocumentManagerClient();
    DocumentFilter documentFilter = new DocumentFilter();
    documentFilter.setDocTypeId("FORM");
    Property property = new Property();
    property.setName(formNameProp);
    property.getValue().add(formName);
    documentFilter.getProperty().add(property);
    documentFilter.getStates().add(State.DRAFT);
    documentFilter.getStates().add(State.COMPLETE);
    documentFilter.getStates().add(State.RECORD);
    List<Document> documents = client.findDocuments(documentFilter);
    if (!documents.isEmpty())
      form = documents.get(0);

    return form;
  }

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
    if (reportName == null)
      throw new Exception("UNDEFINED_REPORT_NAME");

    ReportManagerPort port = ReportModuleBean.getPort(
      ReportModuleBean.getReportAdminCredentials());
    Report report = port.loadReport(reportName, false);

    return getReportDefaultParameters(report);
  }

  public Map getReportDefaultParameters(Report report) throws Exception
  {
    if (report == null)
      throw new Exception("UNDEFINED_REPORT");

    Map defaultParams = new HashMap();
    List<ParameterDefinition> paramDefs = report.getParameterDefinition();
    for (ParameterDefinition paramDef : paramDefs)
    {
      defaultParams.put(paramDef.getName(), paramDef.getDefaultValue());
    }
    return defaultParams;
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

  private void mergeUserParameters(Map parameters)
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

  private void mergeRequestParameters(Map parameters)
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
        storedParams = new HashMap<>();
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

  private void mergeFormValues(Map parameters)
  {
    if (formValues != null)
    {
      parameters.putAll(formValues);
    }
  }

}
