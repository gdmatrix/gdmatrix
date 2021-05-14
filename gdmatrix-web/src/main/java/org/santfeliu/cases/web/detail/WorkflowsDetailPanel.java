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
package org.santfeliu.cases.web.detail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.matrix.cases.Case;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.dic.DictionaryConstants;
import static org.matrix.dic.DictionaryConstants.EXECUTE_ACTION;
import static org.matrix.dic.DictionaryConstants.READ_ACTION;
import org.matrix.dic.Property;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.security.AccessControl;
import org.matrix.workflow.WorkflowConstants;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.cms.CMSListener;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.security.web.LoginBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.util.ResultsManager;
import org.santfeliu.workflow.web.InstanceBean;

/**
 *
 * @author blanquepa
 * @author realor
 */
public class WorkflowsDetailPanel extends TabulatedDetailPanel
{
  public static final int NO_LEVEL = 0;
  public static final int LOW_LEVEL = 1;
  public static final int MEDIUM_LEVEL = 2;
  public static final int HIGH_LEVEL = 2;

  private static final String WORKFLOW_DOCTYPEID = "sf:WORKFLOW";
  private static final String WORKFLOW_MID = "workflowMid";
  public static final String ALLOWED_TYPEIDS_PROPERTY = "allowedTypeIds";
  public static final String FORBIDDEN_TYPEIDS_PROPERTY = "forbiddenTypeIds";

  private static final String START_DATE_PROPERTY = "dataIniciTramitacio";
  private static final String END_DATE_PROPERTY = "dataFiTramitacio";

  private static final String AUTHENTICATION_LEVEL = "authenticationLevel";
  private static final String SIGNATURE_LEVEL = "signatureLevel";

  private static final String SIMULATE = "simular";
  private static final String MOBILE = "mobil";
  private static final String URL = "url";

  private List<CaseDocumentView> caseDocuments = new ArrayList();
  private Case cas;
  private ResultsManager resultsManager;

  public WorkflowsDetailPanel()
  {
    resultsManager =
      new ResultsManager(
        "org.santfeliu.cases.web.resources.CaseBundle", "caseDocuments_");
  }

  @Override
  public void loadData(DetailBean detailBean)
  {
    try
    {
      resultsManager.setColumns(getMid());
      cas = ((CaseDetailBean) detailBean).getCase();
      String caseId = ((CaseDetailBean) detailBean).getCaseId();
      CaseDocumentFilter filter = new CaseDocumentFilter();
      filter.setCaseId(caseId);
      List<CaseDocumentView> docs =
        CaseConfigBean.getPortAsAdmin().findCaseDocumentViews(filter);
      for (CaseDocumentView doc : docs)
      {
        String caseDocTypeId = doc.getCaseDocTypeId();
        if (WORKFLOW_DOCTYPEID.equals(doc.getDocument().getDocTypeId()) &&
          isAllowedTypeId(caseDocTypeId))
        {
          String docId = doc.getDocument().getDocId();
          Document document = DocumentConfigBean.getPortAsAdmin()
            .loadDocument(docId, 0, ContentInfo.METADATA);
          if (canUserExecuteWorkflow(document))
          {
            doc.setDocument(document);
            caseDocuments.add(doc);
          }
        }
      }

      if (getExternalURL() != null) //Add external url row
      {
        if (caseDocuments == null)
          caseDocuments = new ArrayList();

        CaseDocumentView cdview = new CaseDocumentView();
        Document doc = new Document();
        doc.setTitle(cas.getTitle());

        if (DictionaryUtils.containsProperty(cas, START_DATE_PROPERTY))
          doc.getProperty().add(DictionaryUtils.getProperty(cas, START_DATE_PROPERTY));
        if (DictionaryUtils.containsProperty(cas, END_DATE_PROPERTY))
          doc.getProperty().add(DictionaryUtils.getProperty(cas, END_DATE_PROPERTY));

        cdview.setDocument(doc);
        caseDocuments.add(cdview);
      }

    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public ResultsManager getResultsManager()
  {
    return resultsManager;
  }

  public void setResultsManager(ResultsManager resultsManager)
  {
    this.resultsManager = resultsManager;
  }

  public String startInstance()
  {
    return startInstance(false);
  }

  public String startSimulation()
  {
    return startInstance(true);
  }

  public String certStartInstance()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    try
    {
      userSessionBean.loginCertificate();
    }
    catch (Exception ex)
    {
      userSessionBean.showLoginPage(ex);
      return null;
    }

    try
    {
      return startInstance(false);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String certStartSimulation()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    try
    {
      userSessionBean.loginCertificate();
    }
    catch (Exception ex)
    {
      userSessionBean.showLoginPage(ex);
      return null;
    }

    try
    {
      return startInstance(true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String showDocument()
  {
    CaseDocumentView row = (CaseDocumentView)getValue("#{row}");
    return getControllerBean().showObject(DictionaryConstants.DOCUMENT_TYPE,
      DocumentConfigBean.toObjectId(
        row.getDocument().getDocId(), row.getDocument().getVersion()));
  }

  public String getWorkflowTitle()
  {
    CaseDocumentView caseDocumentView = (CaseDocumentView)getValue("#{row}");
    Document document = caseDocumentView.getDocument();
    String title = document.getTitle();
    if (title != null && title.contains(":"))
      title = title.substring(title.indexOf(":") + 1).trim();

    return title;
  }

  public List<CaseDocumentView> getCaseDocuments()
  {
    return caseDocuments;
  }

  public void setCaseDocuments(List<CaseDocumentView> caseDocuments)
  {
    this.caseDocuments = caseDocuments;
  }

  @Override
  public boolean isRenderContent()
  {
    return (caseDocuments != null && !caseDocuments.isEmpty());
  }

  @Override
  public String getType()
  {
    return "workflows";
  }

  private String getWorkflowMid()
  {
    return getProperty(WORKFLOW_MID);
  }

  private String getWorkflowName()
  {
    CaseDocumentView caseDocumentView = (CaseDocumentView)getValue("#{row}");
    Document document = caseDocumentView.getDocument();
    try
    {
      return DocumentUtils.getPropertyValue(document, "workflow.xml");
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public String sort()
  {
    resultsManager.sort(caseDocuments);
    return null;
  }

  public boolean isRenderStartInstance()
  {
    String startDate = null;
    String endDate = null;
    String dateFormat = "yyyyMMdd";

    CaseDocumentView row = (CaseDocumentView)getValue("#{row}");
    Document document = row.getDocument();

    startDate = DocumentUtils.getPropertyValue(document, START_DATE_PROPERTY);
    endDate = DocumentUtils.getPropertyValue(document, END_DATE_PROPERTY);

    if (startDate != null && endDate != null)
    {
      Date sDate = TextUtils.parseUserDate(startDate, dateFormat);
      Date eDate = TextUtils.parseUserDate(endDate, dateFormat);
      startDate = TextUtils.formatDate(sDate, "yyyyMMdd");
      endDate = TextUtils.formatDate(eDate, "yyyyMMdd");
      String today = TextUtils.formatDate(new Date(), "yyyyMMdd");
      if (startDate != null && startDate.compareTo(today) <= 0 &&
        endDate != null && endDate.compareTo(today) >= 0 ||
        (startDate == null && endDate == null))
        return true;

//      Date today = TextUtils.parseUserDate(
//        TextUtils.formatDate(new Date(), dateFormat), dateFormat);
//      if (sDate != null && sDate.compareTo(today) <= 0 &&
//        eDate != null && eDate.compareTo(today) >= 0 ||
//        (sDate == null && eDate == null))
//        return true;
      else
        return false;
    }
    return true;
  }

  public boolean isRenderShowDocument()
  {
    return isEditorUser();
  }

  public boolean isEditorUser()
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(getMid());

    Set userRoles = UserSessionBean.getCurrentInstance().getRoles();
    if (userRoles.contains(DocumentConstants.DOC_ADMIN_ROLE))
      return true;

    List<String> rolesUpdate = mic.getMultiValuedProperty("roles.update");
    if (rolesUpdate != null && rolesUpdate.size() > 0)
    {
      for (String roleUpdate : rolesUpdate)
      {
        if (userRoles.contains(roleUpdate))
        {
          return true;
        }
      }
      return false;
    }
    else
    {
      return true;
    }
  }

  public String getProcessURL()
  {
    String workflowName = getWorkflowName();
    String mid = getWorkflowMid();
    int authenticationLevel = getRequestedAuthenticationLevel();
    int signatureLevel = getRequestedSignatureLevel();
    String language = getLocale().getLanguage();

    return CMSListener.LOGIN_URI + "?" + CMSListener.XMID_PARAM + "=" + mid +
      "&workflow=" + workflowName + 
      "&" + LoginBean.AUTHENTICATION_LEVEL_PARAM + "=" + authenticationLevel +
      "&" + LoginBean.SIGNATURE_LEVEL_PARAM + "=" + signatureLevel + 
      "&" + CMSListener.LANGUAGE_PARAM + "=" + language;
  }
  
  public String getExternalURL()
  {
    String url = null;

    Property urlProperty =
      DictionaryUtils.getProperty(cas, URL);
    if (urlProperty != null)
      url = urlProperty.getValue().get(0);

    return url;
  }
  
  public int getRequestedAuthenticationLevel()
  {
    String authenticationLevel = null;

    Property authProperty =
      DictionaryUtils.getProperty(cas, AUTHENTICATION_LEVEL);

    if (authProperty != null)
      authenticationLevel = authProperty.getValue().get(0);

    if (authenticationLevel == null)
    {
      CaseDocumentView row = (CaseDocumentView)getValue("#{row}");
      Document workflow = row.getDocument();
      if (workflow != null)
        authenticationLevel = 
          DocumentUtils.getPropertyValue(workflow, AUTHENTICATION_LEVEL);
    }
    if (authenticationLevel != null)
    {
      try
      {
        return Integer.parseInt(authenticationLevel);
      }
      catch (Exception ex)
      {
      }
    }
    return NO_LEVEL;
  }

  public int getRequestedSignatureLevel()
  {
    String signatureLevel = null;

    Property signProperty =
      DictionaryUtils.getProperty(cas, SIGNATURE_LEVEL);

    if (signProperty != null)
      signatureLevel = signProperty.getValue().get(0);

    if (signatureLevel == null)
    {
      CaseDocumentView row = (CaseDocumentView)getValue("#{row}");
      Document workflow = row.getDocument();
      if (workflow != null) 
        signatureLevel = 
          DocumentUtils.getPropertyValue(workflow, SIGNATURE_LEVEL);
    }
    if (signatureLevel != null)
    {
      try
      {
        return Integer.parseInt(signatureLevel);
      }
      catch (Exception ex)
      {
      }
    }
    return NO_LEVEL;
  }

  public int getUserAuthenticationLevel()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String loginMethod = userSessionBean.getLoginMethod();
    if (UserSessionBean.LOGIN_CERTIFICATE.equals(loginMethod))
    {
      return HIGH_LEVEL; // TODO: check type of certificate
    }
    else if ("VALID".equals(loginMethod))
    {
      return MEDIUM_LEVEL;
    }
    else if (UserSessionBean.LOGIN_PASSWORD.equals(loginMethod))
    {
      return LOW_LEVEL;
    }
    return NO_LEVEL;
  }
  
  public int getUserSignatureLevel()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String loginMethod = userSessionBean.getLoginMethod();
    if (UserSessionBean.LOGIN_CERTIFICATE.equals(loginMethod))
    {
      return HIGH_LEVEL; // TODO: check type of certificate
    }
    else if ("VALID".equals(loginMethod))
    {
      return MEDIUM_LEVEL;
    }
    else if (UserSessionBean.LOGIN_PASSWORD.equals(loginMethod))
    {
      return LOW_LEVEL;
    }
    return NO_LEVEL;
  }
  
  public boolean isSimulateEnabled()
  {
    String simulate = null;

    Property certProperty =
      DictionaryUtils.getProperty(cas, SIMULATE);

    if (certProperty != null)
      simulate = certProperty.getValue().get(0);

    if (simulate == null)
    {
      CaseDocumentView row = (CaseDocumentView)getValue("#{row}");
      Document workflow = row.getDocument();
      if (workflow != null)
        simulate = DocumentUtils.getPropertyValue(workflow, SIMULATE);
    }
    return (simulate != null && simulate.equalsIgnoreCase("true"));
  }

  public boolean isMobileEnabled()
  {
    String mobile = null;
    Property mobileProperty = DictionaryUtils.getProperty(cas, MOBILE);
    if (mobileProperty != null) mobile = mobileProperty.getValue().get(0);
    return (mobile != null && mobile.equalsIgnoreCase("true"));
  }

  public String getRowStyleClass()
  {
    return getRowStyleClass(null);
  }

  public String getRowStyleClass(String defaultStyleClass)
  {
    String styleClass = defaultStyleClass;
    if (resultsManager != null && resultsManager.getRowStyleClass() != null)
      styleClass = (styleClass != null ? styleClass + " " : "")
        + resultsManager.getRowStyleClass();

    return styleClass;
  }
  
  private String startInstance(boolean simulate)
  {
    try
    {
      String workflowName = getWorkflowName();
      if (workflowName != null)
      {
        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        String outcome =
          instanceBean.createInstance(workflowName, null, simulate);
        MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
        menuModel.setSelectedMid(getWorkflowMid());
        return outcome;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  private List<String> getAllowedDocumentTypeIds()
  {
    return getMultivaluedProperty(ALLOWED_TYPEIDS_PROPERTY);
  }

  private List<String> getForbiddenDocumentTypeIds()
  {
    return getMultivaluedProperty(FORBIDDEN_TYPEIDS_PROPERTY);
  }

  private boolean isAllowedTypeId(String typeId)
  {
    return (getAllowedDocumentTypeIds().isEmpty() || isDerivedFrom(getAllowedDocumentTypeIds(), typeId)) &&
      (getForbiddenDocumentTypeIds().isEmpty() || !isDerivedFrom(getForbiddenDocumentTypeIds(), typeId));
  }

  private boolean isDerivedFrom(List<String> typeIds, String typeId)
  {
    if (typeId == null)
      return false;

    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
    {
      for (String allowedTypeId : typeIds)
      {
        if (type.isDerivedFrom(allowedTypeId))
          return true;
      }
    }
    return false;
  }
  
  private boolean canUserExecuteWorkflow(Document workflow)
  {
    Set<String> roles  = UserSessionBean.getCurrentInstance().getRoles();
    if (roles == null)
      return false;
    List<AccessControl> acl = workflow.getAccessControl();
    return roles.contains(WorkflowConstants.WORKFLOW_ADMIN_ROLE) ||
      DictionaryUtils.canPerformAction(READ_ACTION, roles, acl) ||
      DictionaryUtils.canPerformAction(EXECUTE_ACTION, roles, acl);
  }
}
