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
package org.santfeliu.webapp.modules.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.KernelList;
import org.matrix.kernel.Person;
import org.matrix.kernel.Sex;
import org.matrix.pf.kernel.CountryBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.kernel.web.KernelConfigBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.Tab;

/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class PersonObjectBean extends ObjectBean
{   
  private Person person = new Person();
  
  private List<SelectItem> personParticleSelectItems;
  private List<SelectItem> countrySelectItems;
  private SelectItem[] sexSelectItems;
  
  @Inject
  PersonFinderBean personFinderBean;

  public PersonObjectBean()
  {
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
  public String getRootTypeId()
  {
    return DictionaryConstants.PERSON_TYPE;
  }
  
  @Override
  public String getDescription()
  {
    return isNew() ? "" : person.getName() + " " 
      + person.getFirstParticle() != null ? person.getFirstParticle() : ""
      + person.getFirstSurname()  
      + person.getSecondParticle() != null ? person.getSecondParticle() : ""
      + person.getSecondSurname();
  }  
  
  //TODO: PersonDescriptor
  public String getDescription(String personId)
  {
    //TODO:
    return personId;
  }

  @Override
  public String show()
  {
    return "/pages/kernel/person.xhtml";
  }

  @Override
  public PersonFinderBean getFinderBean()
  {
    return personFinderBean;
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
    if (sexSelectItems == null)
    {
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.kernel.web.resources.KernelBundle", getLocale());    
      sexSelectItems = FacesUtils.getEnumSelectItems(Sex.class, bundle);
    }
    return sexSelectItems;
  } 
  
  public List<SelectItem> getCountrySelectItems()
  {
    if (countrySelectItems == null)
    {
      CountryBacking countryBacking = WebUtils.getBacking("countryBacking");
      countrySelectItems = countryBacking.getCountrySelectItems();
    }
    return countrySelectItems;
  }
  
  public String getCountryLabel(String value)
  {
    List<SelectItem> items = getCountrySelectItems();
    if (items != null)
    {
      for (SelectItem item : items)
      {
        if (value.equals(item.getValue()))
        {
          return item.getLabel();
        }
      }
    }
    return "";
  }  
    
  @Override
  public void loadObject()
  {
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      try
      {
        person = KernelConfigBean.getPort().loadPerson(objectId);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else person = new Person();
  }

  @Override
  public void loadTabs()
  {
    super.loadTabs();

    if (tabs.isEmpty())
    {
      tabs = new ArrayList<>(); // empty list may be read only
      tabs.add(new Tab("Main", "/pages/kernel/person_main.xhtml"));
      tabs.add(new Tab("Addresses", "/pages/kernel/person_addresses.xhtml", "personAddressesTabBean"));
    }
  }

  @Override
  public void storeObject()
  {
    try
    {
      person = KernelConfigBean.getPort().storePerson(person);
      setObjectId(person.getPersonId());
      info("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  @Override
  public Serializable saveState()
  {
    return person;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.person = (Person)state;
  }  
    
}
