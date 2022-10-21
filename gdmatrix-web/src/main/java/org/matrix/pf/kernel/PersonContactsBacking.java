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
package org.matrix.pf.kernel;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.matrix.kernel.Contact;
import org.matrix.kernel.ContactFilter;
import org.matrix.kernel.ContactView;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.santfeliu.kernel.web.KernelConfigBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
public class PersonContactsBacking extends PageBacking 
  implements TypedTabPage, ResultListPage
{
  private static final String PERSON_BACKING = "personBacking";
  private static final String ROOT_TYPE_ID = "Contact";
  private static final String OUTCOME = "pf_person_contacts";  
  
  private PersonBacking personBacking;
  
  //Helpers
  private TypedHelper typedHelper;
  private ResultListHelper<ContactView> resultListHelper;
  private TabHelper tabHelper;
  
  private ContactView editing;
  
  public PersonContactsBacking()
  {
    //Let to super class constructor.   
  }
  
  @PostConstruct
  public void init()
  {
    personBacking = WebUtils.getBacking(PERSON_BACKING);   
    typedHelper = new TypedHelper(this);
    resultListHelper = new ResultListHelper(this);
    tabHelper = new TabHelper(this);
    populate();
  }
  
  public ContactView getEditing()
  {
    return editing;
  }

  public void setEditing(ContactView editing)
  {
    this.editing = editing;
  } 
  
  public boolean isNew()
  {
    return isNew(editing);
  }
  
  @Override
  public String getPageObjectId()
  {
    if (editing != null)
      return editing.getContactId();
    else
      return null;
  }

  @Override
  public String getRootTypeId()
  {
    return ROOT_TYPE_ID;
  }
  
  public String getPageObjectDescription()
  {
    if (editing != null)
    {
      //AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
      //return addressBacking.getDescription(editing.getAddressId());
      return editing.getContactId();
    }
    return null;
  }

  @Override
  public PersonBacking getObjectBacking()
  {
    return personBacking;
  }
  
  @Override
  public String getTypeId()
  {
    return personBacking.getTabTypeId();
  }
  
  @Override
  public ResultListHelper<ContactView> getResultListHelper()
  {
    return resultListHelper;
  }  
  
  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }  

  @Override
  public TabHelper getTabHelper()
  {
    return tabHelper;
  }
  
  public List<ContactView> getRows()
  {
    return resultListHelper.getRows();
  }
    
  public String getContactIcon(ContactView contact)
  {
    return KernelUtils.getContactTypeIconName(contact.getContactTypeId());
  }
  
  @Override
  public String show(String pageObjectId)
  {
    editContact(pageObjectId);
    showDialog();
    return isEditing(pageObjectId) ? OUTCOME : show();
  }  
  
  @Override
  public String show()
  {    
    populate();
    return OUTCOME;
  }
  
  public String editContact(ContactView row)
  {
    String contactId = null;
    if (row != null)
      contactId = row.getContactId();

    return editContact(contactId);
  } 
  
  public String createContact()
  {
    editing = new ContactView();
    return null;
  }  
  
  public String removeContact(ContactView row)
  {
    try
    {
      if (row == null)
        throw new Exception("CONTACT_MUST_BE_SELECTED");
      
      String rowContactId = row.getContactId();
      
      if (editing != null && 
        rowContactId.equals(editing.getContactId()))
        editing = null;
      
      KernelManagerPort port = KernelConfigBean.getPort();
      port.removeContact(rowContactId);
      
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  

  public String storeContact()
  {
    try
    {
      if (editing == null)
        return null;
      
      if (editing.getContactTypeId() == null)
      {
        editing.setContactTypeId(typedHelper.getTypeId());
      }      
      Contact contact = new Contact();
      contact.setContactId(editing.getContactId());
      contact.setPersonId(personBacking.getObjectId());
      contact.setContactTypeId(editing.getContactTypeId());
      contact.setValue(editing.getValue());
      contact.setComments(editing.getComments());
      KernelConfigBean.getPort().storeContact(contact);      
      cancel();
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public List<ContactView> getResults(int firstResult, int maxResults)
  {
    try
    {
      ContactFilter filter = new ContactFilter();
      filter.setPersonId(personBacking.getObjectId());        
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return KernelConfigBean.getPort().findContactViews(filter);
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
    return storeContact();
  }
  
  @Override
  public void load()
  {
    resultListHelper.search();
  }

  @Override
  public void create()
  {
    editing = new ContactView();
  }
  
  @Override
  public String cancel()
  {
    editing = null;
    return null;
  }  
  
  @Override
  public void reset()
  {
    cancel();
    resultListHelper.reset();
  }
    
  private boolean isNew(ContactView contactView)
  {
    return (contactView != null && contactView.getContactId() == null);
  }  
    
  private String editContact(String contactId)
  {
    try
    {
      if (contactId != null && !isEditing(contactId))
      {
        Contact contact = KernelConfigBean.getPort().loadContact(contactId);
        editing = new ContactView();        
        editing.setContactId(contactId);
        editing.setContactTypeId(contact.getContactTypeId());
        editing.setValue(contact.getValue());
        editing.setComments(contact.getComments());                
      }
      else
      {
        editing = new ContactView();
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }  

}