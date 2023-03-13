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
import java.util.Locale;
import java.util.ResourceBundle;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.dic.DictionaryConstants;
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
    
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.cases.web.resources.CaseBundle", Locale.getDefault()); 
    
    List<SearchTab> searchTabs = new ArrayList();
    SearchTab searchTab = 
      new SearchTab("Llistat", "/pages/cases/case_list.xhtml");
    searchTab.getColumns().add(new Column("caseId", 
      bundle.getString("case_caseId")));
    searchTab.getColumns().add(new Column("caseTypeId", 
       bundle.getString("case_type")));   
    searchTab.getColumns().add(new Column("title", 
       bundle.getString("case_title")));  
    searchTabs.add(searchTab);
    
    objectSetup.setSearchTabs(searchTabs);

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab("Main", "/pages/cases/case_main.xhtml"));
    editTabs.add(new EditTab("Persons", "/pages/cases/case_persons.xhtml", 
      "casePersonsTabBean"));
    editTabs.add(new EditTab("Documents", "/pages/cases/case_documents.xhtml", 
      "caseDocumentsTabBean", "docs1", 
      "/pages/cases/case_documents_dialog.xhtml"));
    
    EditTab intEditTab = new EditTab("Actuacions", 
      "/pages/cases/case_interventions.xhtml", "caseInterventionsTabBean", 
      "act1", "/pages/cases/case_interventions_dialog.xhtml");
    intEditTab.getColumns().add(new Column("intId", 
      bundle.getString("caseInterventions_id")));
    intEditTab.getColumns().add(new Column("intTypeId", 
      bundle.getString("caseInterventions_type")));
    intEditTab.getColumns().add(new Column("startDate", 
      bundle.getString("caseInterventions_startDate"))); 
    intEditTab.getColumns().add(new Column("endDate", 
      bundle.getString("caseInterventions_endDate")));  
    editTabs.add(intEditTab);
    
    EditTab casesEditTab = new EditTab("Cases", "/pages/cases/case_cases.xhtml", 
      "caseCasesTabBean", "cases1", "/pages/cases/case_cases_dialog.xhtml");   
    casesEditTab.getColumns().add(new Column("caseCaseId", 
      bundle.getString("caseCases_id")));
    casesEditTab.getColumns().add(new Column("caseCaseTypeId", 
      bundle.getString("caseCases_type")));
    casesEditTab.getColumns().add(new Column("startDate", 
      bundle.getString("caseCases_startDate")));    
    casesEditTab.getColumns().add(new Column("endDate", 
      bundle.getString("caseCases_endDate")));      
    editTabs.add(casesEditTab);
    
    editTabs.add(new EditTab("ACL", "/pages/cases/case_acl.xhtml", 
      "caseACLTabBean"));
    editTabs.add(new EditTab("Events", "/pages/cases/case_events.xhtml", 
      "caseEventsTabBean", "events1", 
      "/pages/cases/case_events_dialog.xhtml"));
    
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
