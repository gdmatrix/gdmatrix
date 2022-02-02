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
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.kernel.KernelConstants;
import org.matrix.kernel.KernelList;
import org.matrix.kernel.Person;
import org.matrix.kernel.Sex;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.TypedHelper;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.matrix.pf.web.helper.TypedPage;
import org.matrix.web.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named("personMainBacking")
public class PersonMainBacking extends PageBacking 
  implements TypedPage
{
  private Person person;
  private TypedHelper typedHelper;
  private List<SelectItem> personParticleSelectItems;
  
  public PersonMainBacking()
  {
  }
  
  @PostConstruct
  public void init()
  {
    objectBacking = WebUtils.getInstance(PersonBacking.class);
    typedHelper = new TypedHelper(this);      
    load();
  }

  @Override
  public String getRootTypeId()
  {
    return objectBacking.getRootTypeId();
  }

  @Override
  public String getTypeId()
  {
    return getObjectTypeId();
  }
  
  @Override
  public String getAdminRole()
  {
    return KernelConstants.KERNEL_ADMIN_ROLE;
  }

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

  public Person getPerson()
  {
    return person;
  }

  public void setPerson(Person person)
  {
    this.person = person;
  }
  
  @Override
  public String show(String pageId)
  {
    objectBacking.setObjectId(pageId);
    return show();
  }  
  
  @Override
  public String show()
  {
    load();
    return "pf_person_main";
  }
  
  public String store()
  {
    try
    {
      KernelConfigBean.getPort().storePerson(person);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }
  
  public String remove()
  {
    error("Not implemented yet");
    return null;
  }
    
  public List<SelectItem> getPersonParticleSelectItems()
  {
    if (personParticleSelectItems == null)
    {
      try
      {
        personParticleSelectItems = FacesUtils.getListSelectItems(
          KernelConfigBean.getPort().listKernelListItems(
          KernelList.PERSON_PARTICLE),
          "itemId", "label", true);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return personParticleSelectItems;
  }  
  
  public SelectItem[] getSexSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
     "org.santfeliu.kernel.web.resources.KernelBundle", getLocale());    
    return FacesUtils.getEnumSelectItems(Sex.class, bundle);
  }

  @Override
  public String getPageId()
  {
    return objectBacking.getObjectId();
  }
      
  private void load()
  {
    String personId = getPageId();
    if (personId != null)
    {
      person = KernelConfigBean.getPort().loadPerson(personId);
    }
  }


}
