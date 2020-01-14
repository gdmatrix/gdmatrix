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

import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;
import org.apache.tools.ant.types.LogLevel;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.classif.ClassificationManagerPort;
import org.matrix.classif.ClassificationManagerService;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
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
public class FindDocumentPoliciesTask extends WSTask
{
  protected String state;
  protected String startDateVar;
  protected String docPolViewVar;
  protected boolean excludeCaseDocuments;

  private Sequential forEachDocumentPolicy;

  public String getDocPolViewVar()
  {
    return docPolViewVar;
  }

  public void setDocPolViewVar(String docPolViewVar)
  {
    this.docPolViewVar = docPolViewVar;
  }

  public boolean isExcludeCaseDocuments()
  {
    return excludeCaseDocuments;
  }

  public void setExcludeCaseDocuments(boolean excludeCaseDocuments)
  {
    this.excludeCaseDocuments = excludeCaseDocuments;
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

  public void addForEachDocumentPolicy(Sequential forEachDocumentPolicy)
  {
    this.forEachDocumentPolicy = forEachDocumentPolicy;
  }

  @Override
  public void execute()
  {
    if (forEachDocumentPolicy == null)
      throw new BuildException("Nested element 'forEachDocumentPolicy' is required");

    PolicyManagerPort port = getPolicyPort();
    String activationDate = (String)getVariable(startDateVar);
    PolicyState policyState = PolicyState.valueOf(state);
    List<DocumentPolicyView> pvList =
      findDocumentPolicyViews(port, activationDate, policyState);

    for (DocumentPolicyView pv : pvList)
    {
      Document document = pv.getDocument();
      if (document != null) //Document no longer exists
      {
        //Exclude this document if it is contained within a case
        boolean isExcluded = false;
        if (excludeCaseDocuments)
        {
          CaseDocumentFilter filter = new CaseDocumentFilter();
          filter.setDocId(document.getDocId());
          int caseDocumentsCount = getCasePort().countCaseDocuments(filter);
          isExcluded = caseDocumentsCount > 0;
          if (isExcluded) log("Excluded because found in " + 
            caseDocumentsCount + " cases ", LogLevel.WARN.getLevel());
        }

        if (!isExcluded)
        {
          document =
            getDocPort().loadDocument(document.getDocId(), document.getVersion(),
              ContentInfo.METADATA);

          //Set classTitle property to document
          if (document.getClassId() != null && document.getClassId().size() > 0)
          {
            String classId = document.getClassId().get(0);
            DocumentUtils.setProperty(document, "classTitle",
              getClassTitle(classId));
          }
          pv.setDocument(document);

          setVariable(docPolViewVar, pv);

          forEachDocumentPolicy.perform();

          //Policies states could be change during task execution. It stores
          //policies to update states.
          port.storeDocumentPolicy(pv.getDocPolicy());
        }
      }
    }
  }

  private PolicyManagerPort getPolicyPort()
  {
    WSEndpoint enpoint = getEndpoint(PolicyManagerService.class);
    PolicyManagerPort port =
      enpoint.getPort(PolicyManagerPort.class, getUsername(), getPassword());

    return port;
  }

  private DocumentManagerPort getDocPort()
  {
    WSEndpoint endpoint = getEndpoint(DocumentManagerService.class);
    DocumentManagerPort port =
      endpoint.getPort(DocumentManagerPort.class, getUsername(), getPassword());

    return port;
  }

  private CaseManagerPort getCasePort()
  {
    WSEndpoint endpoint = getEndpoint(CaseManagerService.class);
    CaseManagerPort port =
      endpoint.getPort(CaseManagerPort.class, getUsername(), getPassword());

    return port;
  }

  private List<DocumentPolicyView> findDocumentPolicyViews(PolicyManagerPort port,
    String activationDate, PolicyState policyState)
  {
    DocumentPolicyFilter filter = new DocumentPolicyFilter();
    filter.setState(policyState);
    filter.setActivationDate(activationDate);
    filter.setFirstResult(0);
    filter.setMaxResults(0);

    List<DocumentPolicyView> result = port.findDocumentPolicyViews(filter);

    return result;
  }

  private String getClassTitle(String classId)
  {
    String dsc = classId;
    WSEndpoint classEndpoint = getEndpoint(ClassificationManagerService.class);
    ClassificationManagerPort classPort = classEndpoint.getPort(
      ClassificationManagerPort.class, getUsername(), getPassword());
    org.matrix.classif.Class docClass = classPort.loadClass(classId, null);
    if (docClass != null)
    {
      dsc = docClass.getTitle() != null ?
        docClass.getTitle() : docClass.getClassId();
    }

    return dsc;
  }

}
