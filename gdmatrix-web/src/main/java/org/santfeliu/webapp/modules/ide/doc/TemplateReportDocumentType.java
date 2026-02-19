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
    return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"
      + "<html>\n"
      + "  <head>\n"
      + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n"
      + "    <title>Nou Template Report</title>\n" // Added to comply with the standard
      + "  </head>\n"
      + "  <body>\n"
      + "  </body>\n"
      + "</html>";
  }

  // No validation
  @Override
  public List<String> validate(String source)
  {
    List<String> errors = new ArrayList<>();

    if (source == null || source.trim().isEmpty())
    {
      errors.add("The content of the REPORT cannot be empty.");
      return errors;
    }
    // Validation here if required
    return errors;
  }
}
