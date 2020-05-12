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
package org.santfeliu.workflow.form;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author realor
 */
public class AddressForm extends Form
{
  public static final String ADDRESS_ID = "addressId";
  public static final String COUNTRY_ID = "countryId";
  public static final String PROVINCE = "province";
  public static final String CITY = "city";
  public static final String STREET_TYPE = "streetType";
  public static final String STREET = "street";
  public static final String NUMBER = "number";
  public static final String BIS = "bis";
  public static final String NO_NUMBER = "noNumber";
  public static final String KM = "km";
  public static final String BLOCK = "block";
  public static final String ENTRANCE_HALL = "entranceHall";
  public static final String STAIR = "stair";
  public static final String FLOOR = "floor";
  public static final String DOOR = "door";
  public static final String POSTAL_CODE = "postalCode";
  public static final String POST_OFFICE_BOX = "postOfficeBox";

  private static final String[] variables =
  {
   ADDRESS_ID,
   COUNTRY_ID,
   PROVINCE,
   CITY,
   STREET_TYPE,
   STREET,
   NUMBER,
   BIS,
   NO_NUMBER,
   KM,
   BLOCK,
   ENTRANCE_HALL,
   STAIR,
   FLOOR,
   DOOR,
   POSTAL_CODE,
   POST_OFFICE_BOX
  };

  @Override
  public Set getWriteVariables()
  {
    HashSet set = new HashSet();
    String prefix = (String)parameters.get("prefix");
    if (prefix == null) prefix = "";
    else prefix += "_";
    for (String var : variables)
    {
      set.add(prefix + var);
    }
    return set;
  }
}
