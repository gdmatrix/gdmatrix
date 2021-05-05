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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.kernel.ContactFilter;
import org.matrix.kernel.ContactView;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.util.ResultsManager;

/**
 *
 * @author blanquepa
 */
public class ContactsDetailPanel extends TabulatedDetailPanel
{
  public static final String ALLOWED_CASEPERSON_TYPEIDS_PROPERTY = "allowedCasePersonTypeIds";
  public static final String FORBIDDEN_CASEPERSON_TYPEIDS_PROPERTY = "forbiddenCasePersonTypeIds";
  public static final String ALLOWED_CONTACT_TYPEIDS_PROPERTY = "allowedContactTypeIds";
  public static final String FORBIDDEN_CONTACT_TYPEIDS_PROPERTY = "forbiddenContactTypeIds";
  public static final String GROUPBY_PERSON = "groupByPerson";
  private static final String FILTER_COMMENTS = "filterComments";

  private List<CasePersonView> casePersons;
  private HashMap<CasePersonView, List<ContactView>> contactsMap;

  private ResultsManager resultsManager;

  public ContactsDetailPanel()
  {
    resultsManager =
      new ResultsManager(
        "org.santfeliu.cases.web.resources.CaseBundle", "casePersons_");
    resultsManager.addDefaultColumn("contactTypeLabel");
    resultsManager.addDefaultColumn("value");
  }

  @Override
  public void loadData(DetailBean detailBean)
  {
    resultsManager.setColumns(getMid());
    try
    {
      casePersons = new ArrayList();
      contactsMap = new HashMap();
      String caseId = ((CaseDetailBean) detailBean).getCaseId();
      CasePersonFilter filter = new CasePersonFilter();
      filter.setCaseId(caseId);
      List<CasePersonView> persons =
        CaseConfigBean.getPort().findCasePersonViews(filter);
      for (CasePersonView pers : persons)
      {
        if (isAllowedCasePersonTypeId(pers.getCasePersonTypeId()) &&
          (!isFormerPerson(pers)))
        {
          casePersons.add(pers);

          CasePerson casePerson = 
            CaseConfigBean.getPort().loadCasePerson(pers.getCasePersonId());
          List<ContactView> contacts =
            findContacts(casePerson.getPersonId(), casePerson.getContactId());
          contactsMap.put(pers, contacts);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public List<ContactView> getContacts()
  {
    if (isGroupByPerson())
    {
      CasePersonView row = (CasePersonView)getValue("#{row}");
      return getContacts(row);
    }
    else
    {
      List<ContactView> result = new ArrayList();
      for (List<ContactView> contacts : contactsMap.values())
      {
        result.addAll(contacts);
      }

      List<String> orderBy = getMultivaluedProperty(ResultsManager.ORDERBY);
      if (orderBy != null && !orderBy.isEmpty())
        resultsManager.sort(result, orderBy);
      else  //Default sorting
      {
        Collections.sort(result, new Comparator() {
          public int compare(Object o1, Object o2)
          {
            ContactView p1 = (ContactView)o1;
            ContactView p2 = (ContactView)o2;
            return p1.getContactTypeLabel().compareTo(p2.getContactTypeLabel());
          }
        });
      }

      return result;
    }
  }

  public List<ContactView> getContacts(CasePersonView casePersonView)
  {
    if (contactsMap != null)
      return contactsMap.get(casePersonView);
    else
      return null;
  }

  @Override
  public boolean isRenderContent()
  {
    return (casePersons != null && !casePersons.isEmpty());
  }

  @Override
  public String getType()
  {
    return "contacts";
  }

  public boolean isRenderPersonNames()
  {
    return (isGroupByPerson() && casePersons != null && casePersons.size() > 1);
  }

  public boolean isGroupByPerson()
  {
    return getProperty(GROUPBY_PERSON, "false").equalsIgnoreCase("true");
  }

  private boolean isFormerPerson(CasePersonView casePerson)
  {
    String startDate = casePerson.getStartDate();
    String endDate = casePerson.getEndDate();
    String now = TextUtils.formatDate(new Date(), "yyyyMMdd");
    if (startDate != null && now.compareTo(startDate) < 0) return true;
    if (endDate != null && now.compareTo(endDate) > 0) return true;
    return false;
  }

  private List<String> getAllowedCasePersonTypeIds()
  {
    return getMultivaluedProperty(ALLOWED_CASEPERSON_TYPEIDS_PROPERTY);
  }

  private List<String> getForbiddenCasePersonTypeIds()
  {
    return getMultivaluedProperty(FORBIDDEN_CASEPERSON_TYPEIDS_PROPERTY);
  }

  private List<String> getAllowedContactTypeIds()
  {
    return getMultivaluedProperty(ALLOWED_CONTACT_TYPEIDS_PROPERTY);
  }

  private List<String> getForbiddenContactTypeIds()
  {
    return getMultivaluedProperty(FORBIDDEN_CONTACT_TYPEIDS_PROPERTY);
  }

  private boolean isAllowedCasePersonTypeId(String typeId)
  {
    return (getAllowedCasePersonTypeIds().isEmpty() || isDerivedFrom(getAllowedCasePersonTypeIds(), typeId)) &&
      (getForbiddenCasePersonTypeIds().isEmpty() || !isDerivedFrom(getForbiddenCasePersonTypeIds(), typeId));
  }

  private boolean isAllowedContactTypeId(String typeId)
  {
    return (getAllowedContactTypeIds().isEmpty() || isDerivedFrom(getAllowedContactTypeIds(), typeId)) &&
      (getForbiddenContactTypeIds().isEmpty() || !isDerivedFrom(getForbiddenContactTypeIds(), typeId));
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

  private List<ContactView> findContacts(String personId,
    List<String> contactIds)
  {
    if (personId == null) return Collections.EMPTY_LIST;

    List<ContactView> result = new ArrayList();
    ContactFilter filter = new ContactFilter();
    filter.setPersonId(personId);

    List<ContactView> contacts =
      KernelConfigBean.getPortAsAdmin().findContactViews(filter);

    // look for contactIds in contacts list
    for (String contactId : contactIds)
    {
      ContactView contactView = findContactById(contactId, contacts);
      if (contactView != null && isAllowedContactTypeId(contactView.getContactTypeId()))
      {
        result.add(contactView);
      }
    }
    return result;
  }

  private ContactView findContactById(String contactId,
    List<ContactView> contacts)
  {
    boolean found = false;
    ContactView contact = null;
    Iterator<ContactView> iter = contacts.iterator();
    while (!found && iter.hasNext())
    {
      contact = iter.next();
      if (contact.getContactId().equals(contactId)) found = true;
    }
    return found ? contact : null;
  }

  public List<CasePersonView> getCasePersons()
  {
    return casePersons;
  }

  public void setCasePersons(List<CasePersonView> casePersons)
  {
    this.casePersons = casePersons;
  }

  public ResultsManager getResultsManager()
  {
    return resultsManager;
  }

  public void setResultsManager(ResultsManager resultsManager)
  {
    this.resultsManager = resultsManager;
  }

}
