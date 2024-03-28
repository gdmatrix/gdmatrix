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
package org.santfeliu.webapp.modules.cases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.dic.DictionaryConstants;
import static org.matrix.dic.DictionaryConstants.CASE_ADDRESS_TYPE;
import static org.matrix.dic.DictionaryConstants.CASE_CASE_TYPE;
import static org.matrix.dic.DictionaryConstants.CASE_DOCUMENT_TYPE;
import static org.matrix.dic.DictionaryConstants.CASE_EVENT_TYPE;
import static org.matrix.dic.DictionaryConstants.CASE_PERSON_TYPE;
import static org.matrix.dic.DictionaryConstants.INTERVENTION_TYPE;
import org.matrix.policy.PolicyConstants;
import org.matrix.security.SecurityConstants;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;
import static org.santfeliu.webapp.modules.cases.CasesModuleBean.getPort;
import org.santfeliu.webapp.setup.Column;
import org.santfeliu.webapp.setup.SearchTab;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class CaseTypeBean extends TypeBean<Case, CaseFilter>
{
  private static final String BUNDLE_PREFIX = "$$caseBundle.";

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CASE_TYPE;
  }

  @Override
  public String getObjectId(Case cas)
  {
    return cas.getCaseId();
  }

  @Override
  public String describe(Case cas)
  {
    return cas.getTitle();
  }

  @Override
  public Case loadObject(String objectId)
  {
    try
    {
      return getPort(true).loadCase(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(Case cas)
  {
    return cas.getCaseTypeId();
  }

  @Override
  public ObjectSetup createObjectSetup()
  {

    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/cases/case.xhtml");
        
    List<SearchTab> searchTabs = new ArrayList();
    SearchTab searchTab = 
      new SearchTab("Llistat", "/pages/cases/case_list.xhtml");
    searchTab.getColumns().add(new Column("caseId", 
      BUNDLE_PREFIX + "case_caseId", "col-1"));
    searchTab.getColumns().add(new Column("caseTypeId", 
      BUNDLE_PREFIX + "case_type", "col-3"));   
    searchTab.getColumns().add(new Column("title", 
      BUNDLE_PREFIX + "case_title", "col-8")); 
    searchTab.getOrderBy().add("caseId");
    searchTabs.add(searchTab);
    
    objectSetup.setSearchTabs(searchTabs);

    List<EditTab> editTabs = new ArrayList<>();
    
    EditTab mainTab = 
      new EditTab(BUNDLE_PREFIX + "tab_main", "pi pi-folder", "/pages/cases/case_main.xhtml");
    mainTab.getReadRoles().add(SecurityConstants.EVERYONE_ROLE);
    mainTab.getWriteRoles().add(SecurityConstants.EVERYONE_ROLE);
    editTabs.add(mainTab);
    
    EditTab personsTab = 
      new EditTab(BUNDLE_PREFIX + "tab_persons", "fa fa-person", 
        "/pages/cases/case_persons.xhtml", "casePersonsTabBean", "persons1", 
        "/pages/cases/case_persons_dialog.xhtml");
    personsTab.getColumns().add(new Column("personId", 
      BUNDLE_PREFIX + "casePersons_id", "col-1"));
    personsTab.getColumns().add(new Column("personName", 
      BUNDLE_PREFIX + "casePersons_person", "col-6"));    
    personsTab.getColumns().add(new Column("casePersonTypeId", 
      BUNDLE_PREFIX + "casePersons_type", "col-3"));    
    personsTab.getColumns().add(new Column("startDate", 
      BUNDLE_PREFIX + "casePersons_startDate", 
      "col-1 text-center white-space-nowrap")); 
    personsTab.getColumns().add(new Column("endDate", 
      BUNDLE_PREFIX + "casePersons_endDate", 
      "col-1 text-center white-space-nowrap"));
    personsTab.getProperties().put("typeId", CASE_PERSON_TYPE);
    personsTab.getReadRoles().add(SecurityConstants.EVERYONE_ROLE);
    personsTab.getWriteRoles().add(SecurityConstants.EVERYONE_ROLE);    
    editTabs.add(personsTab);
    
    EditTab addressesTab = 
      new EditTab(BUNDLE_PREFIX + "tab_addresses", "pi pi-building", 
        "/pages/cases/case_addresses.xhtml", "caseAddressesTabBean", 
        "addresses1", "/pages/cases/case_addresses_dialog.xhtml");
    addressesTab.getColumns().add(new Column("addressId", 
      BUNDLE_PREFIX + "caseAddresses_id", "col-1"));
    addressesTab.getColumns().add(new Column("addressFullDescription", 
      BUNDLE_PREFIX + "caseAddresses_address", "col-5"));    
    addressesTab.getColumns().add(new Column("caseAddressTypeId", 
      BUNDLE_PREFIX + "caseAddresses_type", "col-2"));    
    addressesTab.getColumns().add(new Column("comments", 
      BUNDLE_PREFIX + "caseAddresses_comments", "col-2")); 
    addressesTab.getColumns().add(new Column("startDate", 
      BUNDLE_PREFIX + "caseAddresses_startDate", 
      "col-1 text-center white-space-nowrap"));    
    addressesTab.getColumns().add(new Column("endDate", 
      BUNDLE_PREFIX + "caseAddresses_endDate", 
      "col-1 text-center white-space-nowrap"));    
    addressesTab.getProperties().put("typeId", CASE_ADDRESS_TYPE);
    addressesTab.getReadRoles().add(SecurityConstants.EVERYONE_ROLE);
    addressesTab.getWriteRoles().add(SecurityConstants.EVERYONE_ROLE);      
    editTabs.add(addressesTab);
    
    EditTab documentsTab = 
      new EditTab(BUNDLE_PREFIX + "tab_documents", "pi pi-file-o", 
        "/pages/cases/case_documents.xhtml", "caseDocumentsTabBean", "docs1", 
        "/pages/cases/case_documents_dialog.xhtml");
    documentsTab.getColumns().add(new Column("docId", 
      BUNDLE_PREFIX + "caseDocuments_id", "col-1"));
    documentsTab.getColumns().add(new Column("docLanguage", 
      BUNDLE_PREFIX + "caseDocuments_language", "col-1"));    
    Column titleColumn = new Column("docTitle", 
      BUNDLE_PREFIX + "caseDocuments_title", "col-5");        
    titleColumn.setIcon("org.santfeliu.webapp.modules.doc.DocumentTypeBean." + 
      "getContentIcon(row.getDocument()) + ' text-xl mr-1'");
    documentsTab.getColumns().add(titleColumn);    
    documentsTab.getColumns().add(new Column("caseDocTypeId", 
      BUNDLE_PREFIX + "caseDocuments_type", "col-3")); 
    documentsTab.getColumns().add(new Column("docCreationDate", 
      "$$documentBundle.document_creationDate", 
      "col-2 text-center"));    
    documentsTab.getProperties().put("typeId", CASE_DOCUMENT_TYPE);
    documentsTab.getReadRoles().add(SecurityConstants.EVERYONE_ROLE);
    documentsTab.getWriteRoles().add(SecurityConstants.EVERYONE_ROLE);      
    editTabs.add(documentsTab);
    
    EditTab intEditTab = 
      new EditTab(BUNDLE_PREFIX + "tab_interventions", "pi pi-clock", 
        "/pages/cases/case_interventions.xhtml", "caseInterventionsTabBean", 
        "act1", "/pages/cases/case_interventions_dialog.xhtml");
    intEditTab.getColumns().add(new Column("intId", 
      BUNDLE_PREFIX + "caseInterventions_id", "col-1")); 
    intEditTab.getColumns().add(new Column("intTypeId", 
      BUNDLE_PREFIX + "caseInterventions_type", "col-9"));
    intEditTab.getColumns().add(new Column("startDate", 
      BUNDLE_PREFIX + "caseInterventions_startDate", "col-1 text-center white-space-nowrap"));
    intEditTab.getColumns().add(new Column("endDate", 
      BUNDLE_PREFIX + "caseInterventions_endDate", "col-1 text-center white-space-nowrap" ));
    intEditTab.getProperties().put("typeId", INTERVENTION_TYPE);
    intEditTab.getReadRoles().add(SecurityConstants.EVERYONE_ROLE);
    intEditTab.getWriteRoles().add(SecurityConstants.EVERYONE_ROLE);       
    editTabs.add(intEditTab);
    
    EditTab casesEditTab = 
      new EditTab(BUNDLE_PREFIX + "tab_cases", "pi pi-folder", "/pages/cases/case_cases.xhtml", 
        "caseCasesTabBean", "cases1", "/pages/cases/case_cases_dialog.xhtml");   
    casesEditTab.getColumns().add(new Column("caseId", 
      BUNDLE_PREFIX + "caseCases_id", "col-1"));
    casesEditTab.getColumns().add(new Column("caseTitle", 
      BUNDLE_PREFIX + "caseCases_title", "col-5"));    
    casesEditTab.getColumns().add(new Column("caseCaseTypeId", 
      BUNDLE_PREFIX + "caseCases_type", "col-3"));    
    casesEditTab.getColumns().add(new Column("startDate", 
      BUNDLE_PREFIX + "caseCases_startDate", "col-1 text-center white-space-nowrap"));    
    casesEditTab.getColumns().add(new Column("endDate", 
      BUNDLE_PREFIX + "caseCases_endDate", "col-1 text-center white-space-nowrap"));
    casesEditTab.getProperties().put("typeId", CASE_CASE_TYPE);
    casesEditTab.getReadRoles().add(SecurityConstants.EVERYONE_ROLE);
    casesEditTab.getWriteRoles().add(SecurityConstants.EVERYONE_ROLE);     
    editTabs.add(casesEditTab);
    
    EditTab eventsEditTab = new EditTab(BUNDLE_PREFIX + "tab_events", "pi pi-calendar", 
      "/pages/cases/case_events.xhtml", "caseEventsTabBean", "events1", 
      "/pages/cases/case_events_dialog.xhtml");
    eventsEditTab.getColumns().add(new Column("eventId", 
      BUNDLE_PREFIX + "caseEvents_id", "col-1"));
    eventsEditTab.getColumns().add(new Column("eventTitle", 
      BUNDLE_PREFIX + "caseEvents_event", "col-7"));    
    eventsEditTab.getColumns().add(new Column("caseEventTypeId", 
      BUNDLE_PREFIX + "caseEvents_type", "col-3"));    
    eventsEditTab.getColumns().add(new Column("eventIniDate", 
      BUNDLE_PREFIX + "caseEvents_date", 
      "col-1 text-center white-space-nowrap"));
    eventsEditTab.getProperties().put("typeId", CASE_EVENT_TYPE);
    eventsEditTab.getReadRoles().add(SecurityConstants.EVERYONE_ROLE);
    eventsEditTab.getWriteRoles().add(SecurityConstants.EVERYONE_ROLE);      
    editTabs.add(eventsEditTab);    
    
    EditTab aclTab = 
      new EditTab(BUNDLE_PREFIX + "tab_acl", "pi pi-key", "/pages/cases/case_acl.xhtml", 
        "caseACLTabBean");
    aclTab.getReadRoles().add(SecurityConstants.EVERYONE_ROLE);
    aclTab.getWriteRoles().add(SecurityConstants.EVERYONE_ROLE);      
    editTabs.add(aclTab);
    
    EditTab policiesEditTab = new EditTab(BUNDLE_PREFIX + "tab_policies", 
      "material-icons-outlined mi-policy text-lg", 
      "/pages/policy/case_policies.xhtml", "casePoliciesTabBean");
    policiesEditTab.getReadRoles().add(PolicyConstants.POLICY_ADMIN_ROLE);
    policiesEditTab.getWriteRoles().add(PolicyConstants.POLICY_ADMIN_ROLE);     
    editTabs.add(policiesEditTab);      
    
    objectSetup.setEditTabs(editTabs);
    
    return objectSetup;
  }

  @Override
  public CaseFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";

    CaseFilter filter = new CaseFilter();
    if (query.matches(".{0,4}[0-9]+"))
    {
      filter.getCaseId().add(query);
    }
    else
    {
      if (!query.startsWith("%")) query = "%" + query;
      if (!query.endsWith("%")) query += "%";
      filter.setTitle(query);
    }
    if (typeId != null)
    {
      filter.setCaseTypeId(typeId);
    }
    filter.setMaxResults(10);

    return filter;
  }

  @Override
  public String filterToQuery(CaseFilter filter)
  {
    if (!filter.getCaseId().isEmpty())
    {
      return filter.getCaseId().get(0);
    }
    else if (filter.getTitle() != null)
    {
      String query = filter.getTitle();
      if (query.startsWith("%")) query = query.substring(1);
      if (query.endsWith("%")) query = query.substring(0, query.length() - 1);
      return query;
    }
    return "";
  }

  @Override
  public List<Case> find(CaseFilter filter)
  {
    try
    {
      return getPort(true).findCases(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }
}
