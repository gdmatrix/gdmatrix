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
public class XslTemplateDocumentType extends IdeDocumentType
{

  // Constructor
  public XslTemplateDocumentType()
  {
    super("xsl",
      "XSL template",
      "TEMPLATE",
      "workflow.xsl",
      "text/xml",
      "fa fa-code",
      // Specific tabs
      new Tab("xml_editor.xhtml", "XSL Editor", "fa fa-code", true)
    );
    registerCommonTabs();
  }

  @Override
  public String getTemplate()
  {
    // Básic XSL template, if it does not adapt to our purpose, change it
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n"
      + "    <xsl:output method=\"html\" indent=\"yes\"/>\n\n"
      + "    <xsl:template match=\"/\">\n"
      + "        \n"
      + "    </xsl:template>\n"
      + "</xsl:stylesheet>";
  }

  @Override
  public List<String> validate(String source)
  {
    List<String> errors = new ArrayList<>();

    if (source == null || source.trim().isEmpty())
    {
      errors.add("El contenido XSL no puede estar vacío.");
      return errors;
    }
    // XML basic sintax validation
    errors = XmlValidator.validateXML(source);
    return errors;
  }
}
