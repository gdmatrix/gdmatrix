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

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.kernel.KernelList;
import org.matrix.kernel.Person;
import org.matrix.kernel.Sex;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.MainPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.kernel.web.CountryToStreetBean;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
@Named("personMainBacking")
public class PersonMainBacking extends PageBacking 
  implements TypedTabPage, MainPage
{
  public static final String OUTCOME = "pf_person_main";
  
  private Person person;
  private List<SelectItem> personParticleSelectItems;
  private transient List<SelectItem> countrySelectItems;  
  
  //Helpers
  private TypedHelper typedHelper;
  private TabHelper tabHelper;
  
  private PersonBacking personBacking;
  
  public PersonMainBacking()
  { 
  }
  
  @PostConstruct
  public void init()
  {
    personBacking = WebUtils.getBacking("personBacking");
    typedHelper = new TypedHelper(this);
    tabHelper = new TabHelper(this);
    populate();
  }

  @Override
  public String getRootTypeId()
  {
    return personBacking.getRootTypeId();
  }

  @Override
  public String getTypeId()
  {
    return getMenuItemTypeId();
  }

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

  public TabHelper getTabHelper()
  {
    return tabHelper;
  }

  @Override
  public PersonBacking getObjectBacking()
  {
    return personBacking;
  }

  public Person getPerson()
  {
    return person;
  }

  public void setPerson(Person person)
  {
    this.person = person;
  }
  
  public Date getBirthDate()
  {
    if (person != null && person.getBirthDate() != null)
      return TextUtils.parseInternalDate(person.getBirthDate());
    else
      return null;
  }
  
  public void setBirthDate(Date date)
  {
    if (date != null && person != null)
      person.setBirthDate(TextUtils.formatDate(date, "yyyyMMdd"));
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
  
  //TODO: Backing
  public SelectItem getCitySelectItem()
  {
    CityBacking cityBacking  = WebUtils.getBacking("cityBacking");
    String id = person.getBirthCityId();
    String description = getDescription(cityBacking, id);
    return new SelectItem(id, description);
  }
  
  public void setCitySelectItem(SelectItem selectItem)
  {
    if (selectItem != null)
      person.setBirthCityId((String) selectItem.getValue());
    else
      person.setBirthCityId(null);
  }  
  
  public void selectCity(String cityId)
  {
    if (person != null)
      person.setBirthCityId(cityId);      
  }  
    
  @Override
  public String show(String pageId)
  {
    personBacking.setObjectId(pageId);
    return show();
  }  
  
  @Override
  public String show()
  {
    populate();
    return OUTCOME;
  }
  
  @Override
  public String store()
  {
    try
    {
      KernelConfigBean.getPort().storePerson(person);
      info("STORE_OBJECT");      
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
  public String getPageObjectId()
  {
    return personBacking.getObjectId();
  }

  @Override
  public void create()
  {
    person = new Person();
  }
          
  @Override
  public void load()
  {
    String personId = getPageObjectId();
    if (personId != null)
    {
      person = KernelConfigBean.getPort().loadPerson(personId);
    }
  }

  @Override
  public void reset()
  {
    if (personBacking.isNew())
      create();
    else
      person = null;
  }
  
  @Override
  public String cancel()
  {
    reset();
    info("CANCEL_OBJECT");          
    return null;
  }  
  
  private CountryToStreetBean getCountryToStreetBean()
  {
    return (CountryToStreetBean) getBean("countryToStreetBean");    
  }


}
