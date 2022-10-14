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
package org.santfeliu.policy.ant;

import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;
import org.matrix.cases.Case;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.classif.ClassificationManagerPort;
import org.matrix.classif.ClassificationManagerService;
import org.matrix.dic.Property;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.policy.CasePolicyFilter;
import org.matrix.policy.CasePolicyView;
import org.matrix.policy.DocumentPolicyFilter;
import org.matrix.policy.DocumentPolicyView;
import org.matrix.policy.PolicyManagerPort;
import org.matrix.policy.PolicyManagerService;
import org.matrix.policy.PolicyState;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ant.ws.WSTask;
import org.santfeliu.doc.util.DocumentUtils;

/**
 *
 * @author blanquepa
 */
public class FindCasePoliciesTask extends WSTask
{
  //input
  protected String state;
  protected String policyTypeId;
  protected String startDateVar;
  //output
  protected String casePolViewVar;
  protected String docPolViewListVar;

  private Sequential forEachCasePolicy;

  public String getCasePolViewVar()
  {
    return casePolViewVar;
  }

  public void setCasePolViewVar(String casePolViewVar)
  {
    this.casePolViewVar = casePolViewVar;
  }

  public String getDocPolViewListVar()
  {
    return docPolViewListVar;
  }

  public void setDocPolViewListVar(String docPolViewListVar)
  {
    this.docPolViewListVar = docPolViewListVar;
  }

  public String getStartDateVar()
  {
    return startDateVar;
  }

  public void setStartDateVar(String startDateVar)
  {
    this.startDateVar = startDateVar;
  }

  public String getState()
  {
    return state;
  }

  public void setState(String state)
  {
    this.state = state;
  }

  public String getPolicyTypeId()
  {
    return policyTypeId;
  }

  public void setPolicyTypeId(String policyTypeId)
  {
    this.policyTypeId = policyTypeId;
  }

  public void addForEachCasePolicy(Sequential forEachCasePolicy)
  {
    this.forEachCasePolicy = forEachCasePolicy;
  }

  @Override
  public void execute()
  {
    if (forEachCasePolicy == null)
      throw new BuildException("Nested element 'forEachCasePolicy' is required");

    PolicyManagerPort port = getPolicyPort();
    PolicyState policyState = PolicyState.valueOf(state);
    String startDate = (String)getVariable(startDateVar);

    List<CasePolicyView> cpvList =
      findCasePolicyViews(port, startDate, policyState, policyTypeId);
    for (CasePolicyView cpv : cpvList)
    {
      List<DocumentPolicyView> docPolViewList =
        new ArrayList<DocumentPolicyView>();

      CaseManagerPort casePort = getCasePort();
      if (cpv.getCase() != null) //Case no longer exists
      {
        populateCase(casePort, cpv);
        setVariable(casePolViewVar, cpv);

        List<CaseDocumentView> dvList =
          findCaseDocumentViews(casePort, cpv.getCase().getCaseId());

        for (CaseDocumentView cdv : dvList)
        {
          List<DocumentPolicyView> auxDocPolViewList =
            findDocumentPolicyViews(port, cdv.getDocument().getDocId(),
            startDate, policyState, policyTypeId);

          DocumentManagerPort docPort = getDocumentPort();
          populateDocument(docPort, cdv);
          for (DocumentPolicyView dpv : auxDocPolViewList)
          {
            dpv.setDocument(cdv.getDocument());
          }
          docPolViewList.addAll(auxDocPolViewList);
        }

        setVariable(docPolViewListVar, docPolViewList);

        forEachCasePolicy.perform();

        //Policies states could be change during task execution. It stores
        //policies to update states.
        port.storeCasePolicy(cpv.getCasePolicy());
        for (DocumentPolicyView dv : docPolViewList)
        {
          port.storeDocumentPolicy(dv.getDocPolicy());
        }
      }
    }
  }

  private DocumentManagerPort getDocumentPort()
  {
    WSEndpoint endpoint = getEndpoint(DocumentManagerService.class);
    DocumentManagerPort port =
      endpoint.getPort(DocumentManagerPort.class, getUsername(), getPassword());

    return port;
  }

  private CaseManagerPort getCasePort()
  {
    WSEndpoint enpoint = getEndpoint(CaseManagerService.class);
    CaseManagerPort port =
      enpoint.getPort(CaseManagerPort.class, getUsername(), getPassword());

    return port;
  }

  private PolicyManagerPort getPolicyPort()
  {
    WSEndpoint enpoint = getEndpoint(PolicyManagerService.class);
    PolicyManagerPort port =
      enpoint.getPort(PolicyManagerPort.class, getUsername(), getPassword());

    return port;
  }

  private List<CasePolicyView> findCasePolicyViews(PolicyManagerPort port,
    String activationDate, PolicyState policyState, String policyTypeId)
  {
    CasePolicyFilter filter = new CasePolicyFilter();
    filter.setState(policyState);
    filter.setPolicyTypeId(policyTypeId);
    filter.setActivationDate(activationDate);
    filter.setFirstResult(0);
    filter.setMaxResults(0);

    return port.findCasePolicyViews(filter);
  }

  private List<DocumentPolicyView> findDocumentPolicyViews(PolicyManagerPort port,
    String docId, String activationDate, PolicyState policyState,
    String policyTypeId)
  {
    DocumentPolicyFilter docPolFilter = new DocumentPolicyFilter();
    docPolFilter.setState(policyState);
    docPolFilter.setPolicyTypeId(policyTypeId);
    docPolFilter.setActivationDate(activationDate);
    docPolFilter.setDocId(docId);
    return port.findDocumentPolicyViews(docPolFilter);
  }

  private List<CaseDocumentView> findCaseDocumentViews(CaseManagerPort port,
    String caseId)
  {
    CaseDocumentFilter docFilter = new CaseDocumentFilter();
    docFilter.setCaseId(caseId);

    return port.findCaseDocumentViews(docFilter);
  }

  private void populateCase(CaseManagerPort port, CasePolicyView cpv)
  {
    Case cas = cpv.getCase();
    cas = port.loadCase(cas.getCaseId());
    if (cas.getClassId() != null && cas.getClassId().size() > 0)
    {
      String classId = cas.getClassId().get(0);
      String title = getClassTitle(classId);
      Property p = new Property();
      p.setName("classTitle");
      p.getValue().add(title);
      cas.getProperty().add(p);
    }
    cpv.setCase(cas);
  }

  private void populateDocument(DocumentManagerPort port, CaseDocumentView cdv)
  {
    Document document = cdv.getDocument();
    document =
      port.loadDocument(document.getDocId(), document.getVersion(),
        ContentInfo.METADATA);

    if (document.getClassId() != null && document.getClassId().size() > 0)
    {
      String classId = document.getClassId().get(0);
      DocumentUtils.setProperty(document, "classTitle",
        getClassTitle(classId));
    }
    cdv.setDocument(document);
  }

  private String getClassTitle(String classId)
  {
    String title = classId;
    WSEndpoint classEndpoint = getEndpoint(ClassificationManagerService.class);
    ClassificationManagerPort classPort = classEndpoint.getPort(
      ClassificationManagerPort.class, getUsername(), getPassword());
    org.matrix.classif.Class docClass = classPort.loadClass(classId, null);
    if (docClass != null)
    {
      title = docClass.getTitle() != null ?
        docClass.getTitle() : docClass.getClassId();
    }

    return title;
  }
}
