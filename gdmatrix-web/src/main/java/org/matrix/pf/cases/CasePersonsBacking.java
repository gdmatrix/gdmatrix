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
package org.matrix.pf.cases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonFilter;
import org.matrix.pf.kernel.PersonBacking;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.web.WebUtils;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
@Named("casePersonsBacking")
public class CasePersonsBacking extends PageBacking 
  implements TypedTabPage, ResultListPage
{  
  private TypedHelper typedHelper;
  private ResultListHelper<CasePersonView> resultListHelper;
  private CasePerson editing;
  
  public CasePersonsBacking()
  {
    //Let to super class constructor.   
  }
  
  @PostConstruct
  public void init()
  {
    objectBacking = WebUtils.getInstance(CaseBacking.class);   
    typedHelper = new TypedHelper(this);
    resultListHelper = new ResultListHelper(this);
    populate();
  }
  
  public CasePerson getEditing()
  {
    return editing;
  }

  public void setEditing(CasePerson editing)
  {
    this.editing = editing;
  }    

  @Override
  public String getPageObjectId()
  {
    if (editing != null)
      return editing.getCasePersonId();
    else
      return null;
  }

  @Override
  public String getRootTypeId()
  {
    return "CasePerson";
  }
  
  @Override
  public String getTypeId()
  {
    return objectBacking.getPageTypeId();
  }
  
  @Override
  public ResultListHelper<CasePersonView> getResultListHelper()
  {
    return resultListHelper;
  }  
  
  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }  
  
  public List<CasePersonView> getRows()
  {
    return resultListHelper.getRows();
  }
  
  public String getViewStartDate()
  {
    String date = "";
    CasePersonView row = (CasePersonView)getValue("#{row}");
    if (row != null)
    {
      date = row.getStartDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }

  public String getViewEndDate()
  {
    String date = "";
    CasePersonView row = (CasePersonView)getValue("#{row}");
    if (row != null)
    {
      date = row.getEndDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }   
  
  public Date getCreationDateTime()
  {
    if (editing != null && editing.getCreationDateTime() != null)
      return TextUtils.parseInternalDate(editing.getCreationDateTime());
    else
      return null;
  }

  public Date getChangeDateTime()
  {
    if (editing != null && editing.getChangeDateTime() != null)
      return TextUtils.parseInternalDate(editing.getChangeDateTime());
    else
      return null;
  }  
  
  public SelectItem getPersonSelectItem()
  {
    PersonBacking personBacking = (PersonBacking) getBean("personBacking");
    String description = personBacking.getDescription(editing.getPersonId());
    return new SelectItem(editing.getPersonId(), description);
  }
  
  public void setPersonSelectItem(SelectItem item)
  {
    editing.setPersonId((String) item.getValue());
  }
  
  public List<SelectItem> getFavorites()
  {
    PersonBacking personBacking = WebUtils.getInstance(PersonBacking.class);
    return personBacking.getFavorites();     
  }
  
  public List<SelectItem> completePerson(String query)
  {
    ArrayList<SelectItem> items = new ArrayList();
    PersonBacking personBacking = WebUtils.getInstance(PersonBacking.class);;
    
    //Add current item
    String personId = editing.getPersonId();
    String description = personBacking.getDescription(personId);
    items.add(new SelectItem(personId, description));
    
    //Query search
    if (query != null && query.length() >= 3)
    {
      PersonFilter filter = new PersonFilter();
      filter.setFullName(query);
      filter.setMaxResults(10);
      List<Person> persons = KernelConfigBean.getPort().findPersons(filter);
      if (persons != null)
      {       
        for (Person person : persons)
        {
          description = personBacking.getDescription(person);
          SelectItem item = new SelectItem(person.getPersonId(), description);
          items.add(item);
        }
      }
    }
    else
    {
      //Add favorites
      items.addAll(personBacking.getFavorites(personBacking.getPageTypeId())); 
    }
    
    return items;
  } 
  
  @Override
  public String show(String pageId)
  {
    editPerson(pageId);
    return show();
  }  
  
  @Override
  public String show()
  {    
    populate();
    return "pf_case_persons";
  }
  
  public String editPerson(CasePersonView row)
  {
    String casePersonId = null;
    if (row != null)
      casePersonId = row.getCasePersonId();

    return editPerson(casePersonId);
  }  
  
  public String removePerson(CasePersonView row)
  {
    try
    {
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeCasePerson(row.getCasePersonId());
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  

  public String storePerson()
  {
    try
    {
      if (editing == null)
        return null;
      
      if (editing.getPersonId() == null || 
        editing.getPersonId().isEmpty())
      {
        throw new Exception("PERSON_MUST_BE_SELECTED"); 
      }
                      
      String caseId = objectBacking.getObjectId();
      editing.setCaseId(caseId);
      
      if (editing.getCasePersonTypeId() == null)
      {
        editing.setCasePersonTypeId(typedHelper.getTypeId());
      }
      
      CaseManagerPort port = CaseConfigBean.getPort();
      port.storeCasePerson(editing);

      editing = null;          
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelPerson()
  {
    editing = null;
    return null;
  }
    
  private String editPerson(String casePersonId)
  {
    try
    {
      if (casePersonId != null)
      {
        editing = CaseConfigBean.getPort().loadCasePerson(casePersonId);
      }
      else
      {
        editing = new CasePerson();
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }  

  @Override
  public List<CasePersonView> getResults(int firstResult, int maxResults)
  {
    try
    {
      CasePersonFilter filter = new CasePersonFilter();
      filter.setCaseId(objectBacking.getObjectId());        
      filter.setCasePersonTypeId(getTypeId());
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return CaseConfigBean.getPort().findCasePersonViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public String store()
  {
    return storePerson();
  }
  
  @Override
  public void load()
  {
    resultListHelper.search();
  }

  @Override
  public void create()
  {
    editing = new CasePerson();
  }

  @Override
  public void reset()
  {
    editing = null;
    resultListHelper.reset();
  }


}
