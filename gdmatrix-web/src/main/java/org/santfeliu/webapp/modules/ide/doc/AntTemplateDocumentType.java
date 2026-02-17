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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

/**
 *
 * @author granadogj
 */
public class AntTemplateDocumentType extends IdeDocumentType
{

  //Constructor
  public AntTemplateDocumentType()
  {
    super("ant",
      "ANT project",
      "ANT",
      "ide.ant",
      "text/xml",
      "fa fa-bug",
      //Specific tabs
      new Tab("xml_editor.xhtml", "ANT Editor", "fa fa-bug", true)
    );
    registerCommonTabs();
  }

  @Override
  public String getTemplate()
  {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<project name=\"MyProject\" default=\"dist\" basedir=\".\">\n"
      + "    <description>\n"
      + "        Build file simple per el projecte\n"
      + "    </description>\n\n"
      + "    \n"
      + "    <property name=\"src\" location=\"src\"/>\n"
      + "    <property name=\"build\" location=\"build\"/>\n\n"
      + "    <target name=\"init\">\n"
      + "        \n"
      + "        <tstamp/>\n"
      + "    </target>\n\n"
      + "    <target name=\"dist\" depends=\"init\" description=\"generate the distribution\">\n"
      + "        <echo>Construint el projecte...</echo>\n"
      + "    </target>\n"
      + "</project>";
  }

  // No validation
  @Override
  public List<String> validate(String source)
  {
    // Basic HTML validation
    List<String> errors = new ArrayList<>();
    if (source == null || source.trim().isEmpty()) {
        errors.add("El contenido del ANT no puede estar vacío.");
        return errors;
    }
    // ANT validation here if required
    return errors;
  }
}
