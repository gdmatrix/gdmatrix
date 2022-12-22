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

import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonFilter;
import org.santfeliu.webapp.TypeBean;
import static org.santfeliu.webapp.modules.kernel.KernelModuleBean.getPort;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ApplicationScoped
public class PersonTypeBean extends TypeBean<Person, PersonFilter>
{
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.PERSON_TYPE;
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
