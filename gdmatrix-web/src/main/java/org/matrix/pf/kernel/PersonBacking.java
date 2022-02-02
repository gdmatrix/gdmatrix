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

import javax.inject.Named;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonView;
import org.matrix.pf.cms.CMSContent;
import org.matrix.pf.web.ObjectBacking;
import org.matrix.pf.web.Tab;
import org.matrix.web.WebUtils;
import org.santfeliu.kernel.web.KernelConfigBean;

/**
 *
 * @author blanquepa
 */
@CMSContent(typeId = "Person")
@Named("personBacking")
public class PersonBacking extends ObjectBacking
{   
  public PersonBacking()
  {
    super();  
  }
  
  @Override
  public void loadTabs()
  {
    clearTabs();
    addTab(new Tab("Principal", 0, "#{personMainBacking.show()}", "Person"));
    addTab(new Tab("Domicilis", 1, "#{personAddressessBacking.show()}", "PersonAddress"));
    addTab(new Tab("Contactes", 2, "#{personContactsBacking.show()}", "PersonContact"));
  }

  @Override
  public PersonSearchBacking getSearchBacking()
  {
    return WebUtils.getInstance(PersonSearchBacking.class);
  }

  @Override
  public String getObjectId(Object person)
  {
    return ((PersonView)person).getPersonId();
  }
  
  @Override
  public boolean hasCustomHeader()
  {
    return true;
  }
  
  @Override
  public String getDescription()
  {
    PersonMainBacking mainBacking = 
      WebUtils.getInstance(PersonMainBacking.class);
    if (mainBacking != null)
      return getDescription(mainBacking.getPerson());
    else
      return super.getDescription();
  }
  
  @Override
  public String getDescription(String objectId)
  {
    objectId = super.getDescription(objectId);
    try
    {
      if (objectId != null && objectId.contains(";"))
        return objectId;
      Person person = KernelConfigBean.getPortAsAdmin().loadPerson(objectId);
      return getDescription(person);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }    
  
  @Override
  public String getDescription(Object obj)
  {
    Person person = (Person)obj;
    if (person == null) return "";
    StringBuilder buffer = new StringBuilder();
    buffer.append(person.getName());
    if (person.getFirstParticle() != null)
    {
      buffer.append(" ");
      buffer.append(person.getFirstParticle());
    }
    if (person.getFirstSurname() != null)
    {
      buffer.append(" ");
      buffer.append(person.getFirstSurname());
    }
    if (person.getSecondParticle() != null)
    {
      buffer.append(" ");
      buffer.append(person.getSecondParticle());
    }
    if (person.getSecondSurname() != null)
    {
      buffer.append(" ");
      buffer.append(person.getSecondSurname());
    }
    return buffer.toString();
  }  

  @Override
  public String show()
  {
//    loadTabs();
    return super.show();
  }
   
}
