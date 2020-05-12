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
public class PersonForm extends Form
{
  public static final String PERSON_ID = "personId";
  public static final String NAME = "name";
  public static final String SURNAME1 = "surname1";
  public static final String SURNAME2 = "surname2";
  public static final String SEX = "sex";
  public static final String NIF = "NIF";
  public static final String PASSPORT = "passport";
  public static final String NATIONALITY_ID = "nationalityId"; // countryId
  public static final String CIF = "CIF";
  public static final String PHONE = "phone";
  public static final String EMAIL = "email";

  private static final String[] variables =
  {
    PERSON_ID,
    NAME,
    SURNAME1,
    SURNAME2,
    SEX,
    NIF,
    PASSPORT,
    NATIONALITY_ID,
    CIF,
    PHONE,
    EMAIL
  };

  @Override
  public Set getWriteVariables()
  {
    HashSet set = new HashSet();
    String varPrefix = (String)parameters.get("prefix");
    if (varPrefix == null) varPrefix = "";
    else varPrefix += "_";
    for (String var : variables)
    {
      set.add(varPrefix + var);
    }
    return set;
  }
}
