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
package org.santfeliu.cases.proxy;

import java.util.List;
import javax.jws.WebService;
import org.matrix.cases.Case;
import org.matrix.cases.CaseAddress;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.cases.CaseCase;
import org.matrix.cases.CaseCaseFilter;
import org.matrix.cases.CaseCaseView;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseEvent;
import org.matrix.cases.CaseEventFilter;
import org.matrix.cases.CaseEventView;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.cases.CaseMetaData;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.cases.Demand;
import org.matrix.cases.DemandFilter;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionFilter;
import org.matrix.cases.InterventionProblem;
import org.matrix.cases.InterventionProblemFilter;
import org.matrix.cases.InterventionProblemView;
import org.matrix.cases.InterventionView;
import org.matrix.cases.Problem;
import org.matrix.cases.ProblemFilter;
import org.matrix.cases.ProblemView;
import org.matrix.util.WSEndpoint;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.ws.WSProxy;

/**
 *
 * @author realor
 */
@WebService(endpointInterface = "org.matrix.cases.CaseManagerPort")
public class CaseManager extends WSProxy implements CaseManagerPort
{
  public CaseManager()
  {
    super(CaseManagerService.class, CaseManagerPort.class);
  }

  public CaseMetaData getCaseMetaData()
  {
    return new CaseMetaData();
  }

  // **** Case ****

  public Case loadCase(String caseId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(Case.class, caseId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Case.class, caseId);
      Case _case = port.loadCase(id);
      return endpoint.toGlobal(Case.class, _case);
    }
    else return port.loadCase(caseId);
  }

  public Case storeCase(Case _case)
  {
    // find endpoint
    String caseId = _case.getCaseId();
    WSEndpoint endpoint;
    if (caseId != null)
    {
      // update case
      endpoint = getDestinationEndpoint(Case.class, caseId);
    }
    else
    {
      // new case
      String caseTypeId = _case.getCaseTypeId();
      endpoint = getDestinationEnpointByTypeId(caseTypeId);
    }
    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      _case = endpoint.toLocal(Case.class, _case);
      _case = port.storeCase(_case);
      return endpoint.toGlobal(Case.class, _case);
    }
    else return port.storeCase(_case);
  }

  public boolean removeCase(String caseId)
  {
    // find endpoint
    WSEndpoint endpoint =
      getDestinationEndpoint(Case.class, caseId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Case.class, caseId);
      return port.removeCase(id);
    }
    else return port.removeCase(caseId);
  }

  public int countCases(CaseFilter filter)
  {
    return count("countCases", filter);
  }

  public List<Case> findCases(CaseFilter filter)
  {
    return find("countCases", "findCases", filter, Case.class);
  }

  // **** CasePerson ****

  public CasePerson loadCasePerson(String casePersonId)
  {
    // find endpoint
    WSEndpoint endpoint =
      getDestinationEndpoint(CasePerson.class, casePersonId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(CasePerson.class, casePersonId);
      CasePerson casePerson = port.loadCasePerson(id);
      return endpoint.toGlobal(CasePerson.class, casePerson);
    }
    else return port.loadCasePerson(casePersonId);
  }

  public CasePerson storeCasePerson(CasePerson casePerson)
  {
    // find endpoint
    String casePersonId = casePerson.getCasePersonId();
    WSEndpoint endpoint;
    if (casePersonId != null)
    {
      endpoint = getDestinationEndpoint(CasePerson.class, casePersonId);
    }
    else
    {
      // new casePerson
      String caseId = casePerson.getCaseId();
      endpoint = getDestinationEndpoint(Case.class, caseId);
    }
    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      casePerson = endpoint.toLocal(CasePerson.class, casePerson);
      casePerson = port.storeCasePerson(casePerson);
      return endpoint.toGlobal(CasePerson.class, casePerson);
    }
    else return port.storeCasePerson(casePerson);
  }

  public boolean removeCasePerson(String casePersonId)
  {
    // find endpoint
    WSEndpoint endpoint =
      getDestinationEndpoint(CasePerson.class, casePersonId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(CasePerson.class, casePersonId);
      return port.removeCasePerson(id);
    }
    else return port.removeCasePerson(casePersonId);
  }

  public int countCasePersons(CasePersonFilter filter)
  {
    return count("countCasePersons", filter);
  }

  public List<CasePersonView> findCasePersonViews(CasePersonFilter filter)
  {
    return find("countCasePersons", "findCasePersonViews",
      filter, CasePersonView.class);
  }

  // **** CaseAddress ****

  public CaseAddress loadCaseAddress(String caseAddressId)
  {
    // find endpoint
    WSEndpoint endpoint =
      getDestinationEndpoint(CaseAddress.class, caseAddressId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(CaseAddress.class, caseAddressId);
      CaseAddress caseAddress = port.loadCaseAddress(id);
      return endpoint.toGlobal(CaseAddress.class, caseAddress);
    }
    else return port.loadCaseAddress(caseAddressId);
  }

  public CaseAddress storeCaseAddress(CaseAddress caseAddress)
  {
    // find endpoint
    String caseAddressId = caseAddress.getCaseAddressId();
    WSEndpoint endpoint;
    if (caseAddressId != null)
    {
      endpoint = getDestinationEndpoint(CaseAddress.class, caseAddressId);
    }
    else
    {
      // new caseAddress
      String caseId = caseAddress.getCaseId();
      endpoint = getDestinationEndpoint(Case.class, caseId);
    }
    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      caseAddress = endpoint.toLocal(CaseAddress.class, caseAddress);
      caseAddress = port.storeCaseAddress(caseAddress);
      return endpoint.toGlobal(CaseAddress.class, caseAddress);
    }
    else return port.storeCaseAddress(caseAddress);
  }

  public boolean removeCaseAddress(String caseAddressId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(
      CaseAddress.class, caseAddressId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(CaseAddress.class, caseAddressId);
      return port.removeCaseAddress(id);
    }
    else return port.removeCaseAddress(caseAddressId);
  }

  public int countCaseAddresses(CaseAddressFilter filter)
  {
    return count("countCaseAddresses", filter);
  }

  public List<CaseAddressView> findCaseAddressViews(CaseAddressFilter filter)
  {
    return find("countCaseAddresses", "findCaseAddressViews",
      filter, CaseAddressView.class);
  }

  // **** CaseDocument ****

  public CaseDocument loadCaseDocument(String caseDocId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(CaseDocument.class, caseDocId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(CaseDocument.class, caseDocId);
      CaseDocument caseDocument = port.loadCaseDocument(id);
      return endpoint.toGlobal(CaseDocument.class, caseDocument);
    }
    else return port.loadCaseDocument(caseDocId);
  }

  public CaseDocument storeCaseDocument(CaseDocument caseDocument)
  {
    // find endpoint
    String caseDocId = caseDocument.getCaseDocId();
    WSEndpoint endpoint;
    if (caseDocId != null)
    {
      endpoint = getDestinationEndpoint(CaseDocument.class, caseDocId);
    }
    else
    {
      // new caseDocument
      String caseId = caseDocument.getCaseId();
      endpoint = getDestinationEndpoint(Case.class, caseId);
    }
    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      caseDocument = endpoint.toLocal(CaseDocument.class, caseDocument);
      caseDocument = port.storeCaseDocument(caseDocument);
      return endpoint.toGlobal(CaseDocument.class, caseDocument);
    }
    else return port.storeCaseDocument(caseDocument);
  }

  public boolean removeCaseDocument(String caseDocId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(CaseDocument.class, caseDocId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(CaseDocument.class, caseDocId);
      return port.removeCaseDocument(id);
    }
    else return port.removeCaseDocument(caseDocId);
  }

  public int countCaseDocuments(CaseDocumentFilter filter)
  {
    return count("countCaseDocuments", filter);
  }

  public List<CaseDocumentView> findCaseDocumentViews(CaseDocumentFilter filter)
  {
    return find("countCaseDocuments", "findCaseDocumentViews",
      filter, CaseDocumentView.class);
  }

  public List<String> findCaseVolumes(String caseId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(Case.class, caseId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Case.class, caseId);
      return port.findCaseVolumes(id);
    }
    else
      return port.findCaseVolumes(caseId);
  }

  // **** CaseCase ****

  public CaseCase loadCaseCase(String caseCaseId)
  {
    // find endpoint
    WSEndpoint endpoint =
      getDestinationEndpoint(CaseCase.class, caseCaseId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(CaseCase.class, caseCaseId);
      CaseCase caseCase = port.loadCaseCase(id);
      return endpoint.toGlobal(CaseCase.class, caseCase);
    }
    else return port.loadCaseCase(caseCaseId);
  }

  public CaseCase storeCaseCase(CaseCase caseCase)
  {
    // find endpoint
    String caseCaseId = caseCase.getCaseCaseId();
    WSEndpoint endpoint;

    if (caseCaseId != null)
    {
      endpoint = getDestinationEndpoint(CaseCase.class, caseCaseId);
    }
    else
    {
      // new caseDocument
      String caseId = caseCase.getCaseId();
      endpoint = getDestinationEndpoint(Case.class, caseId);
    }
    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      caseCase = endpoint.toLocal(CaseCase.class, caseCase);
      caseCase = port.storeCaseCase(caseCase);
      return endpoint.toGlobal(CaseCase.class, caseCase);
    }
    else return port.storeCaseCase(caseCase);
  }

  public boolean removeCaseCase(String caseCaseId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(CaseCase.class, caseCaseId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(CaseCase.class, caseCaseId);
      return port.removeCaseCase(id);
    }
    else return port.removeCaseCase(caseCaseId);
  }

  public int countCaseCases(CaseCaseFilter filter)
  {
    return count("countCaseCases", filter);
  }

  public List<CaseCaseView> findCaseCaseViews(CaseCaseFilter filter)
  {
    return find("countCaseCases", "findCaseCaseViews",
      filter, CaseCaseView.class);
  }

  // **** Demand ****

  public Demand loadDemand(String demandId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(Demand.class, demandId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Demand.class, demandId);
      Demand demand = port.loadDemand(id);
      return endpoint.toGlobal(Demand.class, demand);
    }
    else return port.loadDemand(demandId);
  }

  public Demand storeDemand(Demand demand)
  {
   // find endpoint
    String demandId = demand.getDemandId();
    WSEndpoint endpoint;
    if (demandId != null)
    {
      endpoint = getDestinationEndpoint(Demand.class, demandId);
    }
    else
    {
      // new intervention
      String caseId = demand.getCaseId();
      endpoint = getDestinationEndpoint(Case.class, caseId);
    }
    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      demand = endpoint.toLocal(Demand.class, demand);
      demand = port.storeDemand(demand);
      return endpoint.toGlobal(Demand.class, demand);
    }
    else return port.storeDemand(demand);
  }

  public boolean removeDemand(String demandId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(Demand.class, demandId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Demand.class, demandId);
      return port.removeDemand(id);
    }
    else return port.removeDemand(demandId);
  }

  public int countDemands(DemandFilter filter)
  {
    return count("countDemands", filter);
  }

  public List<Demand> findDemands(DemandFilter filter)
  {
    return find("countDemands", "findDemands", filter, Demand.class);
  }

  // **** Problem ****

  public Problem loadProblem(String probId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(Problem.class, probId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Problem.class, probId);
      Problem problem = port.loadProblem(id);
      return endpoint.toGlobal(Problem.class, problem);
    }
    else return port.loadProblem(probId);
  }

  public Problem storeProblem(Problem problem)
  {
   // find endpoint
    String probId = problem.getProbId();
    WSEndpoint endpoint;
    if (probId != null)
    {
      endpoint = getDestinationEndpoint(Problem.class, probId);
    }
    else
    {
      // new intervention
      String caseId = problem.getCaseId();
      endpoint = getDestinationEndpoint(Case.class, caseId);
    }
    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      problem = endpoint.toLocal(Problem.class, problem);
      problem = port.storeProblem(problem);
      return endpoint.toGlobal(Problem.class, problem);
    }
    else return port.storeProblem(problem);
  }

  public boolean removeProblem(String probId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(Problem.class, probId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Problem.class, probId);
      return port.removeProblem(id);
    }
    else return port.removeProblem(probId);
  }

  public int countProblems(ProblemFilter filter)
  {
    return count("countProblems", filter);
  }

  public List<ProblemView> findProblemViews(ProblemFilter filter)
  {
    return find("countProblems", "findProblemViews",
      filter, ProblemView.class);
  }

  // **** Intervention ****

  public Intervention loadIntervention(String intId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(Intervention.class, intId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Intervention.class, intId);
      Intervention intervention = port.loadIntervention(id);
      return endpoint.toGlobal(Intervention.class, intervention);
    }
    else return port.loadIntervention(intId);
  }

  public Intervention storeIntervention(Intervention intervention)
  {
    // find endpoint
    String intId = intervention.getIntId();
    WSEndpoint endpoint;
    if (intId != null)
    {
      endpoint = getDestinationEndpoint(Intervention.class, intId);
    }
    else
    {
      // new intervention
      String caseId = intervention.getCaseId();
      endpoint = getDestinationEndpoint(Case.class, caseId);
    }
    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      intervention = endpoint.toLocal(Intervention.class, intervention);
      intervention = port.storeIntervention(intervention);
      return endpoint.toGlobal(Intervention.class, intervention);
    }
    else return port.storeIntervention(intervention);
  }

  public boolean removeIntervention(String intId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(Intervention.class, intId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Intervention.class, intId);
      return port.removeIntervention(id);
    }
    else return port.removeIntervention(intId);
  }

  public int countInterventions(InterventionFilter filter)
  {
    return count("countInterventions", filter);
  }

  public List<InterventionView> findInterventionViews(InterventionFilter filter)
  {
    return find("countInterventions", "findInterventionViews",
      filter, InterventionView.class);
  }
  
 // **** InterventionProblem ****

  public InterventionProblem loadInterventionProblem(String intProbId)
  {
    // find endpoint
    WSEndpoint endpoint =
      getDestinationEndpoint(InterventionProblem.class, intProbId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(InterventionProblem.class, intProbId);
      InterventionProblem interventionProblem = port.loadInterventionProblem(id);
      return endpoint.toGlobal(InterventionProblem.class, interventionProblem);
    }
    else return port.loadInterventionProblem(intProbId);
  }

  public InterventionProblem storeInterventionProblem(InterventionProblem interventionProblem)
  {
    // find endpoint
    String intProbId = interventionProblem.getIntProbId();
    WSEndpoint endpoint;

    if (intProbId != null)
    {
      endpoint = getDestinationEndpoint(InterventionProblem.class, intProbId);
    }
    else
    {
      // new interventionProblem
      String intId = interventionProblem.getIntId();
      endpoint = getDestinationEndpoint(Intervention.class, intId);
    }
    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      interventionProblem = endpoint.toLocal(InterventionProblem.class, interventionProblem);
      interventionProblem = port.storeInterventionProblem(interventionProblem);
      return endpoint.toGlobal(InterventionProblem.class, interventionProblem);
    }
    else return port.storeInterventionProblem(interventionProblem);
  }

  public boolean removeInterventionProblem(String intProbId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(InterventionProblem.class, intProbId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(InterventionProblem.class, intProbId);
      return port.removeInterventionProblem(id);
    }
    else return port.removeInterventionProblem(intProbId);
  }

  public int countInterventionProblems(InterventionProblemFilter filter)
  {
    return count("countInterventionProblems", filter);
  }

  public List<InterventionProblemView> findInterventionProblemViews(InterventionProblemFilter filter)
  {
    return find("countInterventionProblems", "findInterventionProblemViews",
      filter, InterventionProblemView.class);
  }  

  public CaseEvent loadCaseEvent(String caseEventId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(CaseEvent.class, caseEventId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(CaseEvent.class, caseEventId);
      CaseEvent caseEvent = port.loadCaseEvent(id);
      return endpoint.toGlobal(CaseEvent.class, caseEvent);
    }
    else return port.loadCaseEvent(caseEventId);
  }

  public CaseEvent storeCaseEvent(CaseEvent caseEvent)
  {
    // find endpoint
    String caseEventId = caseEvent.getCaseEventId();
    WSEndpoint endpoint;
    if (caseEventId != null)
    {
      endpoint = getDestinationEndpoint(CaseEvent.class, caseEventId);
    }
    else
    {
      // new caseEvent
      String caseId = caseEvent.getCaseId();
      endpoint = getDestinationEndpoint(Case.class, caseId);
    }
    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      caseEvent = endpoint.toLocal(CaseEvent.class, caseEvent);
      caseEvent = port.storeCaseEvent(caseEvent);
      return endpoint.toGlobal(CaseEvent.class, caseEvent);
    }
    else return port.storeCaseEvent(caseEvent);    
  }

  public boolean removeCaseEvent(String caseEventId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(CaseEvent.class, caseEventId);

    // get port
    CaseManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(CaseEvent.class, caseEventId);
      return port.removeCaseEvent(id);
    }
    else return port.removeCaseEvent(caseEventId);
  }

  public int countCaseEvents(CaseEventFilter filter)
  {
    return count("countCaseEvents", filter);    
  }

  public List<CaseEventView> findCaseEventViews(CaseEventFilter filter)
  {
    return find("countCaseEvents", "findCaseEventViews", filter, 
      CaseEventView.class);
  }
  
  /* private methods */

  private CaseManagerPort getPort(WSEndpoint endpoint)
  {
    return getPort(endpoint, SecurityUtils.getCredentials(wsContext));
  }

  private CaseManagerPort getPort(WSEndpoint endpoint, Credentials credentials)
  {
    return endpoint.getPort(CaseManagerPort.class,
      credentials.getUserId(), credentials.getPassword());
  }
}
