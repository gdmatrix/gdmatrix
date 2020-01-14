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
package org.matrix.util.modgen.ant;

import java.io.File;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.XMLFragment;
import org.matrix.util.modgen.HTMLModuleGenerator;
import org.w3c.dom.Node;

/**
 *
 * @author realor
 */
public class HTMLModuleGeneratorTask extends ModuleGeneratorTask
{
  Section header;
  Section footer;
  String cssUri;

  public class Section extends XMLFragment
  {
    public Section()
    {
    }
  }

  public Section createHeader()
  {
    this.header = new Section();
    return header;
  }

  public Section createFooter()
  {
    this.footer = new Section();
    return footer;
  }

  public String getCssUri()
  {
    return cssUri;
  }

  public void setCssUri(String cssUri)
  {
    this.cssUri = cssUri;
  }

  @Override
  public void execute() throws BuildException
  {
    try
    {
      log("destDir: " + getDestDir());
      log("sourceDir: " + getSourceDir());
      log("moduleFile: " + getModuleFile());

      HTMLModuleGenerator gen = new HTMLModuleGenerator();
      gen.setInputDirectory(new File(getSourceDir()));
      gen.setOutputDirectory(new File(getDestDir()));
      if (header != null)
      {
        gen.setHeader(nodeToString(header.getFragment()));
      }
      if (footer != null)
      {
        gen.setFooter(nodeToString(footer.getFragment()));
      }
      if (cssUri != null)
      {
        gen.setCssUri(cssUri);
      }
      gen.generateOutput(getModuleFile());
    }
    catch (Exception ex)
    {
      throw new BuildException(ex);
    }
  }

  private String nodeToString(Node node)
  {
    try
    {
      TransformerFactory transFactory = TransformerFactory.newInstance();
      Transformer transformer = transFactory.newTransformer();
      StringWriter writer = new StringWriter();
      StreamResult stream = new StreamResult(writer);
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.transform(new DOMSource(node), stream);
      writer.close();
      return writer.toString();
    }
    catch (Exception ex)
    {
      return node.getTextContent();
    }
  }
}
