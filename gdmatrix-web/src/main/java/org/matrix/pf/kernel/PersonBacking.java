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
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.kernel.KernelConstants;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.PersonView;
import org.matrix.pf.cms.CMSContent;
import org.matrix.pf.web.ObjectBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.kernel.web.KernelConfigBean;

/**
 *
 * @author blanquepa
 */
@CMSContent(typeId = "Person")
@Named("personBacking")
public class PersonBacking extends ObjectBacking<PersonView>
{   
  public PersonBacking()
  {
    super();  
  }
 
  @Override
  public PersonSearchBacking getSearchBacking()
  {
    return WebUtils.getBacking("personSearchBacking");
  }

  @Override
  public String getObjectId(PersonView person)
  {
    return person.getPersonId();
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
      WebUtils.getBacking("personMainBacking");
    if (mainBacking != null)
      return getDescription(mainBacking.getPerson().getPersonId());
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
      
      PersonFilter filter = new PersonFilter();
      filter.getPersonId().add(objectId);
      List<PersonView> persons = 
        KernelConfigBean.getPortAsAdmin().findPersonViews(filter);
      
      if (persons != null && !persons.isEmpty())
        return getDescription(persons.get(0));
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }    
  
  @Override
  public String getDescription(PersonView personView)
  {
    return personView.getFullName();
  }  
  
  @Override
  public List<SelectItem> getFavorites()
  {
    return getFavorites(getRootTypeId());
  }  

  @Override
  public String show()
  {
    return super.show();
  }

  @Override
  public String getAdminRole()
  {
    return KernelConstants.KERNEL_ADMIN_ROLE;
  }
  
  @Override
  public boolean remove(String objectId)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
     
}
