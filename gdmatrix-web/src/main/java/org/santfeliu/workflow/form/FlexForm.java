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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.santfeliu.form.Field;

/**
 *
 * @author realor
 */
public class FlexForm extends Form
{
  @Override
  public Set getReadVariables()
  {
    Set variables;
    org.santfeliu.form.Form form = getForm();
    if (form != null)
    {
      Collection<Field> fields = getForm().getFields();
      variables = new HashSet();
      for (Field field: fields)
      {
        if (field.isReadOnly())
        {
          variables.add(field.getReference());
        }
      }
    }
    else variables = Collections.EMPTY_SET;
    return variables;
  }

  @Override
  public Set getWriteVariables()
  {
    Set variables;
    org.santfeliu.form.Form form = getForm();
    if (form != null)
    {
      Collection<Field> fields = getForm().getFields();
      variables = new HashSet();
      for (Field field: fields)
      {
        if (!field.isReadOnly())
        {
          variables.add(field.getReference());
        }
      }
    }
    else variables = Collections.EMPTY_SET;
    return variables;
  }

  private org.santfeliu.form.Form getForm()
  {
    String selector = (String)parameters.get("selector");
    if (selector != null)
    {
      try
      {
        return org.santfeliu.form.FormFactory.getInstance().
          getForm(selector, null);
      }
      catch (Exception ex)
      {
      }
    }
    return null;
  }
}
