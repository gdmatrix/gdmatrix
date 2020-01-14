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
package org.santfeliu.dic.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.ws.WebServiceException;
import org.santfeliu.dic.Type;
import org.santfeliu.ws.WSExceptionFactory;

/**
 *
 * @author blanquepa
 */
public class WSTypeValidator extends TypeValidator
{
  public WSTypeValidator(Type type)
  {
    super(type);
  }

  public void validate(Object object, String unvalidableId)
  {
    HashSet set = new HashSet();
    set.add(unvalidableId);
    validate(object, set);
  }
  
  public void validate(Object object, Set<String> unvalidable)
  {
    List<ValidationError> errors = null;
    if (type != null)
    {
      try
      {
        ObjectPropertiesConverter converter = 
          new ObjectPropertiesConverter(object);
        errors = validate(converter, unvalidable);
      }
      catch (Exception ex)
      {
        throw new WebServiceException(ex.getMessage());
      }
    }

    if (errors != null && errors.size() > 0)
    {
      String[] strErr = new String[errors.size()];
      for (int i = 0; i < errors.size(); i++)
      {
        ValidationError error = errors.get(i);
        strErr[i] =
          String.valueOf(error.getErrMessage() + "#" + error.getPropName());
      }
      throw WSExceptionFactory.create("dic:VALIDATION_ERROR", strErr);
    }
  }

}
