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
package org.santfeliu.cases.web;

import org.matrix.cases.Case;

import org.matrix.dic.PropertyDefinition;
import org.santfeliu.cases.CaseCaseCache;
import org.santfeliu.web.obj.ObjectBean;

/**
 *
 * @author unknown
 */
public class CaseBean extends ObjectBean
{
  public static final String TYPE_NODE_PROPERTY = "name";
  public static final String SCOPE_MID = "scopeMid";
  public static final String SEARCH_MID = "caseSearchMid";

  private static final String RENDER_ACL_PROPERTY = "_showACL";
  private static final String RENDER_ADDRESSES_PROPERTY = "_showAddresses";
  private static final String RENDER_CASES_PROPERTY = "_showCases";
  private static final String RENDER_DEMANDS_PROPERTY = "_showDemands";
  private static final String RENDER_DISPOSITIONS_PROPERTY = "_showDispositions";
  private static final String RENDER_DOCUMENTS_PROPERTY = "_showDocuments";
  private static final String RENDER_EVENTS_PROPERTY = "_showEvents";
  private static final String RENDER_INTERVENTIONS_PROPERTY = "_showInterventions";
  private static final String RENDER_PERSONS_PROPERTY = "_showPersons";
  private static final String RENDER_PROBLEMS_PROPERTY = "_showProblems";

  private static final String ACL_LABEL_PROPERTY = "_ACLLabel";
  private static final String ADDRESSES_LABEL_PROPERTY = "_addressesLabel";
  private static final String CASES_LABEL_PROPERTY = "_casesLabel";
  private static final String DEMANDS_LABEL_PROPERTY = "_demandsLabel";
  private static final String DISPOSITIONS_LABEL_PROPERTY = "_dispositionsLabel";
  private static final String DOCUMENTS_LABEL_PROPERTY = "_documentsLabel";  
  private static final String EVENTS_LABEL_PROPERTY = "_eventsLabel";
  private static final String INTERVENTIONS_LABEL_PROPERTY = "_interventionsLabel";
  private static final String PERSONS_LABEL_PROPERTY = "_personsLabel";
  private static final String PROBLEMS_LABEL_PROPERTY = "_problemsLabel";

  public CaseBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Case";
  }

  @Override
  public String getActualTypeId()
  {
    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    if (isNew())
      return caseMainBean.getCurrentTypeId();
    else
      return caseMainBean.getCase().getCaseTypeId();
  }
  
  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        preRemove();
        CaseConfigBean.getPort().removeCase(getObjectId());
        CaseCaseCache.getInstance().clear(getObjectId());
        postRemove();
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    
    return getControllerBean().show();
  }
  
  @Override
  public String getDescription()
  {
    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    Case cas = caseMainBean.getCase();
    return getCaseDescription(cas);
  }   
  
  @Override
  public String getDescription(String oid)
  {
    String description = "";
    try
    {
      Case cas = CaseConfigBean.getPort().loadCase(oid);
      if (cas != null)
        description = getCaseDescription(cas);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;
  }
  
  private String getCaseDescription(Case cas)
  {
    StringBuffer buffer = new StringBuffer();  
    if (cas.getTitle() != null)
    {
      buffer.append(cas.getTitle());
      buffer.append(" : ");
    }
    CaseConfigBean caseConfigBean = (CaseConfigBean)getBean("caseConfigBean");
    buffer.append(caseConfigBean.getCaseTypeDescription(cas.getCaseTypeId()));
    buffer.append(" (");
    buffer.append(cas.getCaseId());
    buffer.append(")");

    return buffer.toString();
  }

  @Override
  public boolean isEditable()
  {
    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    try
    {
      return caseMainBean.isEditable();
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public boolean isProblemsTabRendered()
  {
    return isTabRendered(RENDER_PROBLEMS_PROPERTY);
  }

  public boolean isDemandsTabRendered()
  {
    return isTabRendered(RENDER_DEMANDS_PROPERTY);
  }

  public boolean isInterventionsTabRendered()
  {
    return isIndexedTabRendered(RENDER_INTERVENTIONS_PROPERTY, 1);
  }
  
  public boolean isInterventionsTabRendered2()
  {
    return isIndexedTabRendered(RENDER_INTERVENTIONS_PROPERTY, 2);
  }

  public boolean isInterventionsTabRendered3()
  {
    return isIndexedTabRendered(RENDER_INTERVENTIONS_PROPERTY, 3);
  }

  public boolean isInterventionsTabRendered4()
  {
    return isIndexedTabRendered(RENDER_INTERVENTIONS_PROPERTY, 4);
  }  

  public boolean isInterventionsTabRendered5()
  {
    return isIndexedTabRendered(RENDER_INTERVENTIONS_PROPERTY, 5);
  }  

  public boolean isInterventionsTabRendered6()
  {
    return isIndexedTabRendered(RENDER_INTERVENTIONS_PROPERTY, 6);
  }  

  public boolean isPersonsTabRendered()
  {
    return isIndexedTabRendered(RENDER_PERSONS_PROPERTY, 1);
  }

  public boolean isPersonsTabRendered2()
  {
    return isIndexedTabRendered(RENDER_PERSONS_PROPERTY, 2);
  }

  public boolean isPersonsTabRendered3()
  {
    return isIndexedTabRendered(RENDER_PERSONS_PROPERTY, 3);
  }

  public boolean isPersonsTabRendered4()
  {
    return isIndexedTabRendered(RENDER_PERSONS_PROPERTY, 4);
  }  

  public boolean isPersonsTabRendered5()
  {
    return isIndexedTabRendered(RENDER_PERSONS_PROPERTY, 5);
  }  
  
  public boolean isPersonsTabRendered6()
  {
    return isIndexedTabRendered(RENDER_PERSONS_PROPERTY, 6);
  }
  
  public boolean isAddressesTabRendered()
  {
    return isTabRendered(RENDER_ADDRESSES_PROPERTY);
  }

  public boolean isCasesTabRendered()
  {
    return isIndexedTabRendered(RENDER_CASES_PROPERTY, 1);
  }
  
  public boolean isCasesTabRendered2()
  {
    return isIndexedTabRendered(RENDER_CASES_PROPERTY, 2);
  }

  public boolean isCasesTabRendered3()
  {
    return isIndexedTabRendered(RENDER_CASES_PROPERTY, 3);
  }
  
  public boolean isCasesTabRendered4()
  {
    return isIndexedTabRendered(RENDER_CASES_PROPERTY, 4);
  }

  public boolean isCasesTabRendered5()
  {
    return isIndexedTabRendered(RENDER_CASES_PROPERTY, 5);
  }
  
  public boolean isCasesTabRendered6()
  {
    return isIndexedTabRendered(RENDER_CASES_PROPERTY, 6);
  }

  public boolean isEventsTabRendered()
  {
    return isIndexedTabRendered(RENDER_EVENTS_PROPERTY, 1);
  }
  
  public boolean isEventsTabRendered2()
  {
    return isIndexedTabRendered(RENDER_EVENTS_PROPERTY, 2);
  }

  public boolean isEventsTabRendered3()
  {
    return isIndexedTabRendered(RENDER_EVENTS_PROPERTY, 3);
  }
  
  public boolean isEventsTabRendered4()
  {
    return isIndexedTabRendered(RENDER_EVENTS_PROPERTY, 4);
  }

  public boolean isEventsTabRendered5()
  {
    return isIndexedTabRendered(RENDER_EVENTS_PROPERTY, 5);
  }
  
  public boolean isEventsTabRendered6()
  {
    return isIndexedTabRendered(RENDER_EVENTS_PROPERTY, 6);
  }
  
  public boolean isDocumentsTabRendered()
  {
    return isTabRendered(RENDER_DOCUMENTS_PROPERTY);
  }

  public boolean isDispositionsTabRendered()
  {
    return isTabRendered(RENDER_DISPOSITIONS_PROPERTY);
  }

  public boolean isACLTabRendered()
  {
    return isTabRendered(RENDER_ACL_PROPERTY);
  }

  public String getACLTabLabel()
  {
    return getTabLabel(ACL_LABEL_PROPERTY);
  }

  public String getAddressesTabLabel()
  {
    return getTabLabel(ADDRESSES_LABEL_PROPERTY);
  }

  public String getCasesTabLabel()
  {
    return getIndexedTabLabel(CASES_LABEL_PROPERTY, 1);
  }

  public String getCasesTabLabel2()
  {
    return getIndexedTabLabel(CASES_LABEL_PROPERTY, 2);
  }
  
  public String getCasesTabLabel3()
  {
    return getIndexedTabLabel(CASES_LABEL_PROPERTY, 3);
  }

  public String getCasesTabLabel4()
  {
    return getIndexedTabLabel(CASES_LABEL_PROPERTY, 4);
  }
  
  public String getCasesTabLabel5()
  {
    return getIndexedTabLabel(CASES_LABEL_PROPERTY, 5);
  }

  public String getCasesTabLabel6()
  {
    return getIndexedTabLabel(CASES_LABEL_PROPERTY, 6);
  }

  public String getDemandsTabLabel()
  {
    return getTabLabel(DEMANDS_LABEL_PROPERTY);
  }

  public String getInterventionsTabLabel()
  {
    return getIndexedTabLabel(INTERVENTIONS_LABEL_PROPERTY, 1);
  }

  public String getInterventionsTabLabel2()
  {
    return getIndexedTabLabel(INTERVENTIONS_LABEL_PROPERTY, 2);
  }
  
  public String getInterventionsTabLabel3()
  {
    return getIndexedTabLabel(INTERVENTIONS_LABEL_PROPERTY, 3);
  }

  public String getInterventionsTabLabel4()
  {
    return getIndexedTabLabel(INTERVENTIONS_LABEL_PROPERTY, 4);
  }  
  
  public String getInterventionsTabLabel5()
  {
    return getIndexedTabLabel(INTERVENTIONS_LABEL_PROPERTY, 5);
  }  

  public String getInterventionsTabLabel6()
  {
    return getIndexedTabLabel(INTERVENTIONS_LABEL_PROPERTY, 6);
  }

  public String getPersonsTabLabel()
  {
    return getIndexedTabLabel(PERSONS_LABEL_PROPERTY, 1);
  }

  public String getPersonsTabLabel2()
  {
    return getIndexedTabLabel(PERSONS_LABEL_PROPERTY, 2);
  }  
  
  public String getPersonsTabLabel3()
  {
    return getIndexedTabLabel(PERSONS_LABEL_PROPERTY, 3);
  }

  public String getPersonsTabLabel4()
  {
    return getIndexedTabLabel(PERSONS_LABEL_PROPERTY, 4);
  }
  
  public String getPersonsTabLabel5()
  {
    return getIndexedTabLabel(PERSONS_LABEL_PROPERTY, 5);
  }
  
  public String getPersonsTabLabel6()
  {
    return getIndexedTabLabel(PERSONS_LABEL_PROPERTY, 6);
  }
  
  public String getProblemsTabLabel()
  {
    return getTabLabel(PROBLEMS_LABEL_PROPERTY);
  }

  public String getEventsTabLabel()
  {
    return getIndexedTabLabel(EVENTS_LABEL_PROPERTY, 1);
  }

  public String getEventsTabLabel2()
  {
    return getIndexedTabLabel(EVENTS_LABEL_PROPERTY, 2);
  }
  
  public String getEventsTabLabel3()
  {
    return getIndexedTabLabel(EVENTS_LABEL_PROPERTY, 3);
  }

  public String getEventsTabLabel4()
  {
    return getIndexedTabLabel(EVENTS_LABEL_PROPERTY, 4);
  }
  
  public String getEventsTabLabel5()
  {
    return getIndexedTabLabel(EVENTS_LABEL_PROPERTY, 5);
  }

  public String getEventsTabLabel6()
  {
    return getIndexedTabLabel(EVENTS_LABEL_PROPERTY, 6);
  }
  
  public String getDocumentsTabLabel()
  {
    return getTabLabel(DOCUMENTS_LABEL_PROPERTY);
  }

  public String getDispositionsTabLabel()
  {
    return getTabLabel(DISPOSITIONS_LABEL_PROPERTY);
  }
  
  private String getIndexedTabLabel(String labelPropertyName, int tabIndex)
  {
    if (tabIndex == 1)
      return getTabLabel(labelPropertyName);
    else
      return getTabLabel(labelPropertyName + tabIndex);
  }
  
  private boolean isIndexedTabRendered(String renderPropertyName, int tabIndex)
  {
    if (tabIndex == 1)
      return isTabRendered(renderPropertyName);
    else
      return isTabRendered(renderPropertyName + tabIndex, false);
  }  

  private boolean isTabRendered(String renderPropertyName)
  {
    return isTabRendered(renderPropertyName, true);
  }  
  
  private boolean isTabRendered(String renderPropertyName, boolean defaultValue)
  {
    if (isNew()) return false;

    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");

    org.santfeliu.dic.Type type = caseMainBean.getCurrentType();
    if (type == null)
      return defaultValue;

    PropertyDefinition pd = type.getPropertyDefinition(renderPropertyName);
    if (pd == null || pd.getValue().isEmpty()) return defaultValue;
    else return "true".equals(pd.getValue().get(0));
  }

}
