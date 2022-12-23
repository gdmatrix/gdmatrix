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

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Address;
import org.matrix.kernel.AddressFilter;
import org.santfeliu.webapp.TypeBean;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class AddressTypeBean extends TypeBean<Address, AddressFilter>
{
  @Inject 
  StreetTypeBean streetTypeBean;
  
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.ADDRESS_TYPE;
  }

  @Override
  public String describe(Address address)
  {
    String street = streetTypeBean.getDescription(address.getStreetId());
    return street 
      + " " + (address.getNumber1() != null ? address.getNumber1() : "") 
      + " " + (address.getNumber2() != null ? address.getNumber2() : "")
      + " " + (address.getFloor() != null ? address.getFloor() : "")
      + " " + (address.getDoor() != null ? address.getDoor() : "");
  }

  @Override
  public Address loadObject(String objectId)
  {
    try
    {
      return KernelModuleBean.getPort(false).loadAddress(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public AddressFilter queryToFilter(String query, String typeId)
  {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

  @Override
  public String filterToQuery(AddressFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

  @Override
  public List<Address> find(AddressFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }


  
}
