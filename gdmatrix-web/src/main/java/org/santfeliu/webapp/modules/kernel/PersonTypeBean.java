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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonFilter;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;
import static org.santfeliu.webapp.modules.kernel.KernelModuleBean.getPort;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ApplicationScoped
public class PersonTypeBean extends TypeBean<Person, PersonFilter>
{
  private static final String BUNDLE_PREFIX = "$$kernelBundle.";  
  
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.PERSON_TYPE;
  }

  @Override
  public String getObjectId(Person person)
  {
    return person.getPersonId();
  }

  @Override
  public String describe(Person person)
  {
    return person.getName()
    + (person.getFirstParticle() != null ?
      " " + person.getFirstParticle() :
      "")
    + (person.getFirstSurname() != null ?
      " " + person.getFirstSurname() :
      "")
    + (person.getSecondParticle() != null ?
      " " + person.getSecondParticle() :
      "")
    + (person.getSecondSurname() != null ?
      " " + person.getSecondSurname() :
      "");
  }

  @Override
  public Person loadObject(String objectId)
  {
    try
    {
      return getPort(true).loadPerson(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(Person person)
  {
    return person.getPersonTypeId();
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/kernel/person.xhtml");

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main", "fa fa-person",
      "/pages/kernel/person_main.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_addresses", "pi pi-building", 
      "/pages/kernel/person_addresses.xhtml",
      "personAddressesTabBean"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_contacts", "pi pi-id-card",
      "/pages/kernel/person_contacts.xhtml",
      "personContactsTabBean"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_cases", "pi pi-folder", 
      "/pages/kernel/person_cases.xhtml",
      "personCasesTabBean"));
    objectSetup.setEditTabs(editTabs);

    return objectSetup;
  }

  @Override
  public PersonFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";

    PersonFilter filter = new PersonFilter();
    if (query.matches("\\d+"))
      filter.getPersonId().add(query);
    else if (query.matches("(\\d+\\D+|\\D+\\d+)"))
      filter.setNif(query);
    else
      filter.setFullName(query);
    filter.setMaxResults(10);
    return filter;
  }

  @Override
  public String filterToQuery(PersonFilter filter)
  {
    String value = "";
    if (!filter.getPersonId().isEmpty())
      value = filter.getPersonId().get(0);
    else if (!StringUtils.isBlank(filter.getNif()))
      value = filter.getNif();
    else if (!StringUtils.isBlank(filter.getFullName()))
      value = filter.getFullName();
    return value;
  }

  @Override
  public List<Person> find(PersonFilter filter)
  {
    try
    {
      return getPort(true).findPersons(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

}
