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
package org.santfeliu.webapp.data;

import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.matrix.cases.Case;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.ObjectDumper;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormFactory;

/**
 *
 * @author blanquepa
 */
public class HtmlFormFunction implements Function
{

  @Override
  public Object execute(Map<String, Object> args, PebbleTemplate self, 
    EvaluationContext context, int lineNumber)
  {
    String formSelector = (String) args.get("formSelector"); 
    Case object = (Case) args.get("caseObject");
    
    if (object != null && formSelector != null)
    {
      Type type = TypeCache.getInstance().getType(DictionaryConstants.CASE_TYPE);
      Map data = new ObjectDumper().dumpAsMap(object, type);
      for (Property prop : object.getProperty())
      {
        data.put(prop.getName(), prop.getValue().get(0));
      }

      try     
      {
        FormFactory formFactory = FormFactory.getInstance();
        Form form = formFactory.getForm(formSelector, data);

        FormRenderer formRenderer = new FormRenderer(form, data);
        return formRenderer.encode();
      }
      catch (Exception ex)
      {
        Logger.getLogger(HtmlFormFunction.class.getName()).log(Level.SEVERE, null, ex);
      }
    }    
    return null;   
  }

  @Override
  public List<String> getArgumentNames()
  {
    return List.of("formSelector", "caseObject");
  }
  
}
