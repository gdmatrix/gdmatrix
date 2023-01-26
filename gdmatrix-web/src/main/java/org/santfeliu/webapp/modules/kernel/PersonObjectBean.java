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
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.KernelList;
import org.matrix.kernel.Person;
import org.matrix.kernel.Sex;
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
  private SelectItem[] sexSelectItems;

  @Inject
  PersonTypeBean personTypeBean;

  @Inject
  CountryTypeBean countryTypeBean;

  @Inject
  PersonFinderBean personFinderBean;

  @PostConstruct
  public void init()
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
  public PersonTypeBean getTypeBean()
  {
    return personTypeBean;
  }

  @Override
  public Person getObject()
  {
    return isNew() ? null : person;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(person.getPersonId());
  }

  public String getDescription(String personId)
  {
    return getTypeBean().getDescription(personId);
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

  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
      person = KernelConfigBean.getPort().loadPerson(objectId);
    else
      person = new Person();
  }

  @Override
  public void loadTabs()
  {
    super.loadTabs();

    if (tabs.isEmpty())
    {
      tabs = new ArrayList<>(); // empty list may be read only
      tabs.add(new Tab("Main", "/pages/kernel/person_main.xhtml"));
      tabs.add(new Tab("Addresses", "/pages/kernel/person_addresses.xhtml",
        "personAddressesTabBean"));
      tabs.add(new Tab("Contacts", "/pages/kernel/person_contacts.xhtml",
        "personContactsTabBean"));      
      tabs.add(new Tab("Cases", "/pages/kernel/person_cases.xhtml",
        "personCasesTabBean"));      
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    person = KernelModuleBean.getPort(false).storePerson(person);
    setObjectId(person.getPersonId());
    personFinderBean.outdate();
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
