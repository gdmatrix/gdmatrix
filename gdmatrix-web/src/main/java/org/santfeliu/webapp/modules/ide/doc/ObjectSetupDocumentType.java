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
package org.santfeliu.webapp.modules.ide.doc;

import dev.harrel.jsonschema.Validator;
import dev.harrel.jsonschema.ValidatorFactory;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.santfeliu.webapp.setup.ObjectSetup;
import org.xml.sax.SAXParseException;

/**
 *
 * @author granadogj
 */
public class ObjectSetupDocumentType extends IdeDocumentType
{

  //Constructor
  public ObjectSetupDocumentType()
  {
    super("ObjectSetup",
      "Object setup",
      "ObjectSetup",
      "setupName",
      "application/json",
      "fa fa-gear",
      // Specific tab
      new Tab("json_editor.xhtml", "JSON Editor", "fa fa-gear", true)
    );
    registerCommonTabs();
  }

  @Override
  public List<String> validate(String source)
  {
    List<String> errors = new ArrayList<>();

    // If the source equals null or empty, return the error list empty
    if (source == null || source.trim().isEmpty())
    {
      return errors;
    }

    try (InputStream is = ObjectSetup.class.getResourceAsStream("ObjectSetup.schema.json"); 
      Scanner scanner = new Scanner(is, StandardCharsets.UTF_8))
    {
      // Read the schema
      String schema = scanner.useDelimiter("\\A").next();

      // Validate the 'source' input by parameter
      Validator.Result result = new ValidatorFactory().validate(schema, source);

      if (!result.isValid())
      {
        for (dev.harrel.jsonschema.Error error : result.getErrors())
        {
          StringBuilder sbError = new StringBuilder();
          sbError.append(error.getInstanceLocation())
            .append(": ")
            .append(error.getError());
          errors.add(sbError.toString());
        }
      }
    }
    catch (Exception ex)
    {
      errors.add("Error validando JSON Schema: " + ex.toString());
    }
    return errors;
  }

  @Override
  public String getTemplate()
  {
    // A valid JSON object
    return "{}";
  }
}
