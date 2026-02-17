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

import java.util.ArrayList;
import java.util.List;
import org.santfeliu.webapp.validators.XmlValidator;

/**
 *
 * @author granadogj
 */
public class TemplateReportDocumentType extends IdeDocumentType
{ 
  // Constructor
  public TemplateReportDocumentType()
  {
    super("template", 
      "Template report", 
      "REPORT", 
      "report", 
      "text/xml", 
      "fa fa-file-code",
      //Specific tabs
      new Tab("html_editor.xhtml", "HTML Editor", "fa fa-file-code", true)
    );
    registerCommonTabs();
  }

  @Override
  public String getTemplate()
  {
    // Basic report template
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<report>\n" +
           "    <title>Nuevo Reporte</title>\n" +
           "    <content>\n" +
           "        \n" +
           "    </content>\n" +
           "</report>";
  }

  // No validation
  @Override
  public List<String> validate(String source)
  {
    List<String> errors = new ArrayList<>();

    if (source == null || source.trim().isEmpty())
    {
      errors.add("El contenido del REPORT no puede estar vacío.");
      return errors;
    }
    // Validation here if required
    return errors;
  }
}
