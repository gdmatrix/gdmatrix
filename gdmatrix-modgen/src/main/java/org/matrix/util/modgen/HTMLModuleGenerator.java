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
package org.matrix.util.modgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author realor
 */
public class HTMLModuleGenerator extends ModuleGenerator
{
  private String header;
  private String footer;
  private String cssUri;

  public String getFooter()
  {
    return footer;
  }

  public void setFooter(String footer)
  {
    this.footer = footer;
  }

  public String getHeader()
  {
    return header;
  }

  public void setHeader(String header)
  {
    this.header = header;
  }

  public String getCssUri()
  {
    return cssUri;
  }

  public void setCssUri(String cssUri)
  {
    this.cssUri = cssUri;
  }

  public void generateOutput(Module module) throws Exception
  {
    outputDirectory.mkdirs();
    generateHTML(module);
    updateModulesIndex(module);
  }

  private void generateHTML(Module module) throws Exception
  {
    File file = new File(outputDirectory, module.getName() + ".html");
    PrintWriter printer = new PrintWriter(file, "UTF-8");

    printer.println("<!DOCTYPE HTML PUBLIC "
      + "\"-//W3C//DTD HTML 4.01 Transitional//EN\" "
      + "\"http://www.w3.org/TR/html4/loose.dtd\">");
    printer.println("<html>");
    printer.println("<head>");
    printer.print("<title>");
    printer.print(module.getName());
    printer.println("</title>");
    printer.println("<meta http-equiv=\"Content-Type\" " +
      "content=\"text/html; charset=UTF-8\" />");
    if (cssUri != null)
    {
      printer.println("<link href=\"" + cssUri + "\" type=\"text/css\" " +
        "rel=\"stylesheet\" />");
    }
    printer.println("</head>");
    printer.println("<body>");
    printer.println(getModuleHeader());

    printer.println("<table class=\"outer\">");
    printer.println("<tr class=\"module\">");
    printer.println("<td colspan=\"2\">Module " +
      module.getName() + "</td>");
    printer.println("</tr>");

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Name:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      module.getName() + "</span></td>");
    printer.println("</tr>");

    if (module.getTitle() != null)
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Title:</td>");
      printer.println("<td class=\"col2\">" + module.getTitle() + "</td>");
      printer.println("</tr>");
    }

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Namespace:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      module.getNamespace() + "</span></td>");
    printer.println("</tr>");

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Service:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      module.getService() + "</span></td>");
    printer.println("</tr>");

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Port:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      module.getPort() + "</span></td>");
    printer.println("</tr>");    

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Java package:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      module.getJavaPackage() + "</span></td>");
    printer.println("</tr>");

    if (module.getVersion() != null)
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Version:</td>");
      printer.println("<td class=\"col2\">" + module.getVersion() + "</td>");
      printer.println("</tr>");
    }

    if (module.getStatus() != null)
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Status:</td>");
      printer.println("<td class=\"col2\"><span class=\"code\">" +
        module.getStatus() + "</span></td>");
      printer.println("</tr>");
    }

    if (module.getAuthors() != null)
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Authors:</td>");
      printer.println("<td class=\"col2\">" + module.getAuthors() + "</td>");
      printer.println("</tr>");
    }

    if (module.getWsdlLocation() != null)
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">WSDL:</td>");
      printer.println("<td class=\"col2\"><a href=\"" + 
        module.getWsdlLocation() + "\"><span class=\"code\">" +
        module.getName() + ".wsdl</span></a></td>");
      printer.println("</tr>");
    }

    // imports
    printer.println("<tr>");
    printer.println("<td class=\"col1\">Imports:</td>");
    printer.println("<td class=\"col2multiline\">");

    printer.println("<table class=\"inner\">");
    printer.println("<tr class=\"columns\">");
    printer.println("<td width=\"25%\">prefix</td>");
    printer.println("<td width=\"75%\">namespace</td>");
    printer.println("</tr>");
    printer.println("<tr>");
    printer.println("<td><span class=\"code\">xs</span></td>");
    printer.println("<td><span class=\"code\">http://www.w3.org/2001/XMLSchema</span></td>");
    printer.println("</tr>");
    printer.println("<tr>");
    printer.println("<td><span class=\"code\">mime</span></td>");
    printer.println("<td><span class=\"code\">http://www.w3.org/2005/05/xmlmime</span></td>");
    printer.println("</tr>");

    for (Import _import : module.getImports())
    {
      String moduleName = _import.getModule();
      printer.println("<tr>");
      if (moduleName != null) // module import
      {
        URL url = new URL(module.getLocation(), moduleName + ".xml");
        Module importModule = moduleFactory.getModule(url);

        printer.println("<td><a href=\"" + moduleName +
          ".html\"><span class=\"code\">" + moduleName +
          "</span></a></td>");
        printer.println("<td><span class=\"code\">" +
          importModule.getNamespace() + "</span></td>");
      }
      else // external schema
      {
        String namespace = _import.getNamespace();
        String prefix = _import.getPrefix();
        printer.println("<td><span class=\"code\">" + prefix + "</span></td>");
        printer.println("<td><a href=\"" + _import.getLocation() +
          "\"><span class=\"code\">" + namespace + "</span></a></td>");
      }
      printer.println("</tr>");
    }
    printer.println("</table>");
    printer.println("</td>");
    printer.println("</tr>");

    // documentation
    if (!module.getDocumentations().isEmpty())
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Documentation:</td>");
      printer.println("<td class=\"col2multiline\">");
      generateDocumentation(printer, module);
      printer.println("</td>");
      printer.println("</tr>");
    }
    printer.println("</table>");

    // link to types and operations
    printer.println("<p>[<a href=\"#types\">Types</a>] " +
      "[<a href=\"#operations\">Operations</a>]</p>");

    // types
    if (!module.getTypes().isEmpty())
    {
      generateTypesIndex(printer, module);
      
      for (Type type : module.getTypes())
      {
        if (type instanceof ComplexType)
        {
          generateComplexType(printer, (ComplexType)type);
        }
        else if (type instanceof Enumeration)
        {
          generateEnumeration(printer, (Enumeration)type);
        }
      }
    }

    // operations
    if (!module.getOperations().isEmpty())
    {
      generateOperationsIndex(printer, module);

      for (Operation operation : module.getOperations())
      {
        generateOperation(printer, operation);
      }
    }

    printer.println(getModuleFooter());

    printer.println("</body>");
    printer.println("</html>");
    printer.close();
  }

  private String getHTMLSignature(Operation operation)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("<span class=\"xmlcode\">");
    buffer.append("<span class=\"operationName\">");
    buffer.append(operation.getName());    
    buffer.append(" (");
    buffer.append("</span>");
    int i = 0;
    while (i < operation.getParameters().size())
    {
      if (i > 0)
      {
        buffer.append(", ");
      }
      Parameter p = operation.getParameters().get(i);
      buffer.append("<span class=\"parameterName\">");
      buffer.append(p.getName());
      buffer.append("</span>");
      buffer.append(" {").append(p.getType()).append("}");
      if ("unbounded".equals(p.getMaxOccurs()))
      {
        if ("0".equals(p.getMinOccurs())) buffer.append("*");
        else buffer.append("+");
      }
      i++;
    }
    buffer.append("<span class=\"operationName\">");
    buffer.append(")");
    buffer.append("</span>");

    Response res = operation.getResponse();
    if (res != null)
    {
      buffer.append(" => ");
      buffer.append("<span class=\"parameterName\">");
      buffer.append(res.getName());
      buffer.append("</span>");
      buffer.append(" {").append(res.getType()).append("}");
      if ("unbounded".equals(res.getMaxOccurs()))
      {
        if ("0".equals(res.getMinOccurs())) buffer.append("*");
        else buffer.append("+");
      }
    }
    buffer.append("</span>");
    return buffer.toString();
  }

  private String htmlEncode(String s)
  {
    s = s.replaceAll("&", "&amp;");
    s = s.replaceAll("<", "&lt;");
    s = s.replaceAll(">", "&gt;");    
    return s;
  }

  private void updateModulesIndex(Module module) throws Exception
  {
    StringBuilder builder = new StringBuilder();
    builder.append("<tr>");

    builder.append("<td><span class=\"code\">");
    builder.append("<a href=\"");
    builder.append(module.getName());
    builder.append(".html\">");
    builder.append(module.getName());
    builder.append("</a></span>");
    builder.append("</td>");

    builder.append("<td><span class=\"code\">");
    builder.append(module.getNamespace());
    builder.append("</span></td>");

    builder.append("<td>");
    builder.append(module.getTitle());
    builder.append("</td>");

    builder.append("<td><span class=\"code\">");
    builder.append(module.getStatus());
    builder.append("</span></td>");

    builder.append("</tr>");

    updateModulesIndex(module.getName(), builder.toString());
  }

  private void updateModulesIndex(String name, String fragment) throws Exception
  {
    String startTag = "<!-- start " + name + " -->";
    String endTag = "<!-- end " + name + " -->";
    String lastTag = "<!-- last -->";

    String page = null;
    File file = new File(outputDirectory, "index.html");
    if (file.exists())
    {
      InputStreamReader reader =
        new InputStreamReader(new FileInputStream(file), "UTF-8");
      StringBuilder builder = new StringBuilder();
      int ch = reader.read();
      while (ch != -1)
      {
        builder.append((char)ch);
        ch = reader.read();
      }
      reader.close();
      page = builder.toString();
      int start = page.indexOf(startTag);
      int end = page.indexOf(endTag);
      if (start == -1 || end == -1)
      {
        // append to index
        int last = page.indexOf(lastTag);
        page = page.substring(0, last) + startTag + "\n" + fragment + "\n" +
          endTag + "\n" + page.substring(last);
      }
      else
      {
        // replace
        page = page.substring(0, start + startTag.length()) + 
          "\n" + fragment + "\n" + page.substring(end);
      }
    }
    else // new
    {
      StringBuilder buffer = new StringBuilder();
      buffer.append(
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" ");
      buffer.append("\"http://www.w3.org/TR/html4/loose.dtd\">");
      buffer.append("<html><head>\n");
      buffer.append("<title>Index</title>\n");
      buffer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
      if (cssUri != null)
      {
        buffer.append("<link href=\"");
        buffer.append(cssUri);
        buffer.append("\" type=\"text/css\" rel=\"stylesheet\" />");
      }
      buffer.append("</head><body>\n");
      buffer.append(getIndexHeader());
      buffer.append("<h3>Index of modules</h3>");
      buffer.append("<table class=\"index\">");
      buffer.append("<tr class=\"columns\"><td width=\"15%\">name</td>");
      buffer.append("<td width=\"25%\">namespace</td>");
      buffer.append("<td width=\"48%\">title</td>");
      buffer.append("<td width=\"12%\">status</td></tr>");
      buffer.append(startTag);
      buffer.append("\n");
      buffer.append(fragment);
      buffer.append("\n");
      buffer.append(endTag);
      buffer.append("\n\n");
      buffer.append(lastTag);
      buffer.append("\n</table>");
      buffer.append(getIndexFooter());
      buffer.append("</body></html>");
      page = buffer.toString();
    }
    PrintWriter printer = new PrintWriter(file, "UTF-8");
    printer.println(page);
    printer.close();
  }

  private void generateTypesIndex(PrintWriter printer, Module module)
  {
    ArrayList<Type> entities = new ArrayList<Type>();
    ArrayList<Type> structs = new ArrayList<Type>();
    ArrayList<Type> enumerations = new ArrayList<Type>();
    for (Type type : module.getTypes())
    {
      if (type instanceof Entity)
      {
        entities.add(type);
      }
      else if (type instanceof Struct)
      {
        structs.add(type);
      }
      else if (type instanceof Enumeration)
      {
        enumerations.add(type);
      }
    }

    printer.println("<a name=\"types\"></a>");
    printer.println("<h3>Types</h3>");

    printer.println("<ul>");

    if (entities.size() > 0)
    {
      printer.println("<li>Entities:");
      generateTypesIndex(printer, entities, "entity");
      printer.println("</li>");
    }

    if (structs.size() > 0)
    {
      printer.println("<li>Structs:");
      generateTypesIndex(printer, structs, "struct");
      printer.println("</li>");
    }

    if (enumerations.size() > 0)
    {
      printer.println("<li>Enumerations:");
      generateTypesIndex(printer, enumerations, "enumeration");
      printer.println("</li>");
    }
    printer.println("</ul>");
  }

  private void generateTypesIndex(PrintWriter printer, List<Type> types,
    String className)
  {
    printer.println("<ul>");
    for (Type type : types)
    {
      printer.println("<li>");
      printer.println("<a href=\"#type-" + type.getName() + "\">");
      printer.println("<span class=\"" + className + "\">" +
        type.getName() + "</span>");
      printer.println("</a>");
      printer.println("</li>");
    }
    printer.println("</ul>");
  }

  private void generateComplexType(PrintWriter printer, ComplexType complexType)
  {
    printer.println("<a name=\"type-" + complexType.getName() + "\"></a>");
    printer.println("<table class=\"outer\">");
    printer.println("<tr class=\"struct\">");
    printer.println("<td colspan=\"2\">" + getTypeHeader(complexType) + "</td>");
    printer.println("</tr>");

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Name:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      complexType.getName() + "</span></td>");
    printer.println("</tr>");

    printer.println("<tr>");
    printer.println("<td class=\"col1\">QName:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      complexType.getQName() + "</span></td>");
    printer.println("</tr>");

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Java class name:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      complexType.getJavaClassName() + "</span></td>");
    printer.println("</tr>");

    generateExtendsType(printer, complexType);

    if (!complexType.getDocumentations().isEmpty())
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Description:</td>");
      printer.println("<td class=\"col2multiline\">");
      generateDocumentation(printer, complexType);
      printer.println("</td>");
      printer.println("</tr>");
    }

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Properties:</td>");
    printer.println("<td class=\"col2multiline\">");

    printer.println("<table class=\"inner\">");
    generateComplexTypifiedHeader(printer);

    Identifier identifier = complexType.getIdentifier();
    if (identifier != null) generateIdentifier(printer, identifier);

    for (Property property : complexType.getProperties())
    {
      generateComplexTypifiedComponent(printer, property);
    }
    printer.println("</table>");

    List<NamedComponent> list = new ArrayList<NamedComponent>();
    if (identifier != null) list.add(identifier);
    list.addAll(complexType.getProperties());
    generateDocumentation(printer, list);

    printer.println("</td>");
    printer.println("</tr>");

    generateAttributes(printer, complexType.getAttributes());

    generateRelatedOperations(printer, complexType);

    printer.println("</table>");
  }

  private void generateEnumeration(PrintWriter printer, Enumeration enumeration)
  {
    printer.println("<a name=\"type-" + enumeration.getName() + "\"></a>");
    printer.println("<table class=\"outer\">");
    printer.println("<tr class=\"enumeration\">");
    printer.println("<td colspan=\"2\">" + getTypeHeader(enumeration) + "</td>");
    printer.println("</tr>");

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Name:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      enumeration.getName() + "</span></td>");
    printer.println("</tr>");

    printer.println("<tr>");
    printer.println("<td class=\"col1\">QName:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      enumeration.getQName() + "</span></td>");
    printer.println("</tr>");

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Java class name:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      enumeration.getJavaClassName() + "</span></td>");
    printer.println("</tr>");

    if (!enumeration.getDocumentations().isEmpty())
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Description:</td>");
      printer.println("<td class=\"col2multiline\">");
      generateDocumentation(printer, enumeration);
      printer.println("</td>");
      printer.println("</tr>");
    }

    generateExtendsType(printer, enumeration);

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Values:</td>");
    printer.println("<td class=\"col2multiline\">");
    printer.println("<table class=\"inner\">");
    printer.println("<tr class=\"columns\">");
    printer.println("<td width=\"25%\">name</td>" +
      "<td width=\"75%\">description</td>");
    printer.println("</tr>");
    String extendsType = enumeration.getExtendsType();
    if (extendsType != null)
    {
      Enumeration extendsEnumeration =
       (Enumeration)moduleFactory.getType(extendsType);
      if (extendsEnumeration != null)
      {
        generateEnumerationValues(printer, extendsEnumeration);
      }
    }
    generateEnumerationValues(printer, enumeration);    
    printer.println("</table>");
    printer.println("</td>");
    printer.println("</tr>");
    printer.println("</table>");
  }

  private void generateEnumerationValues(PrintWriter printer,
    Enumeration enumeration)
  {
    for (EnumerationValue value : enumeration.getValues())
    {
      printer.println("<tr>");
      printer.println("<td>");
      printer.println("<span class=\"code\">" + value.getName() + "</span>");
      printer.println("</td>");
      printer.println("<td>");
      generateDocumentation(printer, value);
      printer.println("</td>");
      printer.println("</tr>");
    }
  }

  private void generateIdentifier(PrintWriter printer, Identifier identifier)
  {
    printer.println("<tr>");
    printer.println("<td><span class=\"identifier\" title=\"Identifier\">" +
      identifier.getName() + "</span></td>");
    printer.println("<td><span class=\"code\">xs:string</span></td>");
    printer.println("<td><span class=\"code\">0..1</span></td>");
    printer.println("<td><span class=\"code\">false</span></td>");
    printer.println("<td><span class=\"code\">false</span></td>");
    printer.println("<td></td>");
    printer.println("</tr>");
  }

  private void generateAttributes(PrintWriter printer, List<Attribute> attributes)
  {
    if (!attributes.isEmpty())
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Attributes:</td>");
      printer.println("<td class=\"col2multiline\">");

      printer.println("<table class=\"inner\">");
      printer.println("<tr class=\"columns\">");
      printer.println("<td width=\"25%\">name</td>");
      printer.println("<td width=\"25%\">xml type</td>");
      printer.println("<td width=\"75%\">required</td>");
      printer.println("</tr>");
      for (Attribute attribute : attributes)
      {
        generateAttribute(printer, attribute);
      }
      printer.println("</table>");

      generateDocumentation(printer, attributes);

      printer.println("</td>");
      printer.println("</tr>");
    }
  }

  private void generateOperationsIndex(PrintWriter printer, Module module)
  {
    printer.println("<a name=\"operations\"></a>");
    printer.println("<h3>Operations</h3>");

    printer.println("<ul>");
    for (Operation operation : module.getOperations())
    {
      printer.println("<li>");
      printer.println("<a href=\"#oper-" + operation.getName() + "\">");
      printer.println("<span class=\"code\">" +
        getHTMLSignature(operation) + "</span>");
      printer.println("</a>");
      printer.println("</li>");
    }
    printer.println("</ul>");
  }

  private void generateOperation(PrintWriter printer, Operation operation)
  {
    printer.println("<a name=\"oper-" + operation.getName() + "\"></a>");
    printer.println("<table class=\"outer\">");
    printer.println("<tr class=\"operation\">");
    printer.println("<td colspan=\"2\">Operation " + operation.getName() + "</td>");
    printer.println("</tr>");

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Name:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      operation.getName() + "</span></td>");
    printer.println("</tr>");

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Signature:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      getHTMLSignature(operation) + "</span></td>");
    printer.println("</tr>");
    
    printer.println("<tr>");
    printer.println("<td class=\"col1\">Java signature:</td>");
    printer.println("<td class=\"col2\"><span class=\"code\">" +
      htmlEncode(operation.getJavaSignature(moduleFactory)) + "</span></td>");
    printer.println("</tr>");

    if (!operation.getDocumentations().isEmpty())
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Description:</td>");
      printer.println("<td class=\"col2multiline\">");
      generateDocumentation(printer, operation);
      printer.println("</td>");
      printer.println("</tr>");
    }

    printer.println("<tr>");
    printer.println("<td class=\"col1\">Parameters:</td>");
    printer.println("<td class=\"col2multiline\">");
    printer.println("<table class=\"inner\">");
    generateComplexTypifiedHeader(printer);
    for (Parameter p : operation.getParameters())
    {
      generateComplexTypifiedComponent(printer, p);
    }
    printer.println("</table>");

    generateDocumentation(printer, operation.getParameters());

    printer.println("</td>");
    printer.println("</tr>");

    if (operation.getResponse() != null)
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Response:</td>");
      printer.println("<td class=\"col2multiline\">");
      printer.println("<table class=\"inner\">");
      generateComplexTypifiedHeader(printer);
      generateComplexTypifiedComponent(printer, operation.getResponse());
      printer.println("</table>");

      if (!operation.getResponse().getDocumentations().isEmpty())
      {
        List list = Collections.singletonList(operation.getResponse());
        generateDocumentation(printer, list);
      }
      printer.println("</td>");
      printer.println("</tr>");
    }

    if (!operation.getErrors().isEmpty())
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Errors:</td>");
      printer.println("<td class=\"col2multiline\">");

      printer.println("<table class=\"inner\">");
      printer.println("<tr class=\"columns\">");
      printer.println("<td width=\"30%\">name</td>");
      printer.println("<td width=\"70%\">message</td>");
      printer.println("</tr>");
      for (Error error : operation.getErrors())
      {
        printer.println("<tr>");
        printer.println("<td><span class=\"error\">" + error.getName() + "</span></td>");
        printer.println("<td><span class=\"code\">" + error.getMessage() + "</span></td>");
        printer.println("</tr>");
      }
      printer.println("</table>");

      generateDocumentation(printer, operation.getErrors());

      printer.println("</td>");
      printer.println("</tr>");      
    }
    generateSampleInputMessage(printer, operation);
    generateSampleOutputMessage(printer, operation);

    printer.println("</table>");
  }

  private void generateComplexTypifiedHeader(PrintWriter printer)
  {
    printer.println("<tr class=\"columns\">");
    printer.println("<td width=\"24%\">name</td>");
    printer.println("<td width=\"24%\">xml type</td>");
    printer.println("<td width=\"8%\">occurs</td>");
    printer.println("<td width=\"9%\">nillable</td>");
    printer.println("<td width=\"9%\">read only</td>");
    printer.println("<td width=\"26%\">references</td>");
    printer.println("</tr>");
  }

  private void generateComplexTypifiedComponent(PrintWriter printer,
    ComplexTypifiedComponent comp)
  {
    printer.println("<tr>");
    printer.println("<td><span class=\"code\">" + comp.getName() + "</span></td>");
    printer.println("<td><span class=\"code\">" + getType(comp) + "</span></td>");
    printer.println("<td><span class=\"code\">" + getOccurrences(comp) + "</span></td>");
    printer.println("<td><span class=\"code\">" + comp.isNillable() + "</span></td>");
    printer.println("<td><span class=\"code\">" + comp.isReadOnly() + "</span></td>");
    printer.println("<td><span class=\"code\">" + getReferences(comp) + "</span></td>");
  }

  private void generateAttribute(PrintWriter printer, Attribute attribute)
  {
    printer.println("<tr>");
    printer.println("<td><span class=\"code\">" + attribute.getName() + "</span></td>");
    printer.println("<td><span class=\"code\">" + attribute.getType() + "</span></td>");
    printer.println("<td><span class=\"code\">" + attribute.isRequired() + "</span></td>");
    printer.println("</tr>");
  }

  private void generateDocumentation(PrintWriter printer,
     List components)
  {
    // look for documentation
    boolean hasDocumentation = false;
    Iterator iter = components.iterator();
    while (!hasDocumentation && iter.hasNext())
    {
      Object comp = iter.next();
      if (comp instanceof NamedComponent)
      {
        NamedComponent nc = (NamedComponent)comp;
        hasDocumentation = nc.getDocumentations().size() > 0;
      }
    }

    if (hasDocumentation)
    {
      printer.println("<ul>");
      for (Object comp : components)
      {
        if (comp instanceof NamedComponent)
        {
          NamedComponent component = (NamedComponent)comp;
          if (!component.getDocumentations().isEmpty())
          {
            printer.println("<li>");
            printer.print("<span class=\"code\">" + component.getName() +
               ":</span>");
            printer.print(" <span class=\"documentation\">");
            for (Documentation documentation : component.getDocumentations())
            {
              printer.print(documentation.getText());
            }
            printer.println("</span>");
            printer.println("</li>");
          }
        }
      }
      printer.println("</ul>");
    }
  }

  private void generateDocumentation(PrintWriter printer, NamedComponent comp)
  {
    for (Documentation documentation : comp.getDocumentations())
    {
      printer.print("<div class=\"documentation\">");
      printer.print(documentation.getText());
      printer.println("</div>");
    }
  }

  private void generateExtendsType(PrintWriter printer, Type type)
  {
    String extendsType = type.getExtendsType();
    if (extendsType != null)
    {
      printer.println("<tr>");
      printer.println("<td class=\"col1\">Super type:</td>");
      printer.println("<td class=\"col2\"><span class=\"code\">" +
        getTypeReference(extendsType) + "</span></td>");
      printer.println("</tr>");
    }
  }

  private String getTypeHeader(Type type)
  {
    StringBuilder buffer = new StringBuilder();
    if (type instanceof Entity)
    {
      buffer.append("Entity");
    }
    else if (type instanceof Struct)
    {
      buffer.append("Struct");
    }
    else if (type instanceof Enumeration)
    {
      buffer.append("Enumeration");
    }
    buffer.append(" ");
    buffer.append(type.getName());
    String extendsType = type.getExtendsType();
    if (extendsType != null)
    {
      buffer.append(" :: ");
      buffer.append(getTypeReference(extendsType));
    }
    return buffer.toString();
  }

  private String getTypeReference(String qname)
  {
    String reference;
    int index = qname.indexOf(":");
    String prefix = qname.substring(0, index);
    if (moduleFactory.getModule(prefix) == null)
    {
      reference = qname;
    }
    else // type from module
    {
      String name = qname.substring(index + 1);
      reference = "<a href=\"" + prefix + ".html#type-" + name + "\">" +
        qname + "</a>";
    }
    return reference;
  }

  private String getType(TypifiedComponent comp)
  {
    String type = comp.getType();
    int index = type.indexOf(":");
    if (index == -1) return type;
    String prefix = type.substring(0, index);
    if ("xs".equals(prefix)) return type;
    if ("mime".equals(prefix)) return type;
    if (moduleFactory.getModule(prefix) == null) return type;
    String name = type.substring(index + 1);
    return "<a href=\"" + prefix + ".html#type-" + name + "\">" + type + "</a>";
  }

  private String getReferences(ComplexTypifiedComponent comp)
  {
    String qname = comp.getReferences();
    return qname == null ? "" : getTypeReference(qname);
  }

  private String getOccurrences(ComplexTypifiedComponent comp)
  {
    String minOccurs = comp.getMinOccurs();
    if (minOccurs == null) minOccurs = "1";
    String maxOccurs = comp.getMaxOccurs();
    if (maxOccurs == null) maxOccurs = "1";
    else if("unbounded".equals(maxOccurs)) maxOccurs = "N";

    if (minOccurs.equals(maxOccurs)) return minOccurs;
    return minOccurs + ".." + maxOccurs;
  }

  private void generateRelatedOperations(PrintWriter printer, Type type)
  {
    List<Operation> relatedOperations = new ArrayList<Operation>();
    Module module = (Module)type.getParent();
    for (Operation operation : module.getOperations())
    {
      boolean found = false;
      if (operation.getResponse() != null)
      {
        found = (operation.getResponse().getType().equals(type.getQName()));
      }
      
      Iterator<Parameter> iter = operation.getParameters().iterator();
      while (!found && iter.hasNext())
      {
        Parameter p = iter.next();
        found = p.getType().equals(type.getQName());
      }

      if (found)
      {
        relatedOperations.add(operation);
      }
    }
    if (relatedOperations.size() > 0)
    {
      printer.println("<tr><td class=\"col1\">Related operations:</td>");
      printer.println("<td class=\"col2multiline\"><ul>");
      for (Operation operation : relatedOperations)
      {
        printer.println("<li>");
        printer.println("<a href=\"#oper-" + operation.getName() + "\">");
        printer.println("<span class=\"code\">" +
          getHTMLSignature(operation) + "</span>");
        printer.println("</a>");
        printer.println("</li>");
      }
      printer.println("</ul></td></tr>");
    }
  }

  private void generateSampleInputMessage(PrintWriter printer,
    Operation operation)
  {
    String operationName = operation.getName();
    String moduleName = ((Module)operation.getParent()).getName();

    printer.println("<tr><td class=\"col1\">Sample input message:</td>");
    printer.println("<td class=\"col2multiline\"><pre class=\"xml\">");

    printer.println("&lt;?xml version=\"1.0\" ?&gt;");
    printer.println("<span class=\"tag\">&lt;S:Envelope</span> " +
     "<span class=\"attribute\">xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"</span>" +
     "<span class=\"tag\">&gt;</span>");
    printer.println(indent(2) + "<span class=\"tag\">&lt;S:Body&gt;</span>");
    printer.println(indent(4) + "<span class=\"tag\">&lt;ns2:" + operationName +
      "</span> <span class=\"attribute\">xmlns:ns2=\"http://" + moduleName +
      ".matrix.org/\"</span><span class=\"tag\">&gt;</span>");

    for (Parameter parameter : operation.getParameters())
    {
      String qname = parameter.getType();
      Type type = moduleFactory.getType(qname);
      if (type instanceof Enumeration || type == null)
      {
        printer.print(indent(6) + "<span class=\"tag\">&lt;" +
          parameter.getName() + "&gt;</span>");
        printer.print("{" + qname + "}");
        printer.println("<span class=\"tag\">&lt;/" +
          parameter.getName() + "&gt;</span>");
      }
      else
      {
        printer.print(indent(6) + "<span class=\"tag\">&lt;" +
          parameter.getName() + "</span>");
        if (type instanceof ComplexType)
        {
          generateSampleAttributes(printer, ((ComplexType)type).getAttributes());
          printer.println("<span class=\"tag\">&gt;</span>");
          generateSampleInstance(printer, (ComplexType)type, 8);
        }
        else printer.println("<span class=\"tag\">&gt;</span>");
        printer.println(indent(6) + "<span class=\"tag\">&lt;/" +
          parameter.getName() + "&gt;</span>");
      }
    }
    printer.println(indent(4) + "<span class=\"tag\">&lt;/ns2:" +
      operationName + "&gt;</span>");
    printer.println(indent(2) + "<span class=\"tag\">&lt;/S:Body&gt;</span>");
    printer.print("<span class=\"tag\">&lt;/S:Envelope&gt;</span>");
    printer.println("</pre></td></tr>");
  }

  private void generateSampleOutputMessage(PrintWriter printer,
    Operation operation)
  {
    String operationName = operation.getName();
    String moduleName = ((Module)operation.getParent()).getName();

    printer.println("<tr><td class=\"col1\">Sample output message:</td>");
    printer.println("<td class=\"col2multiline\"><pre class=\"xml\">");

    printer.println("&lt;?xml version=\"1.0\" ?&gt;");
    printer.println("<span class=\"tag\">&lt;S:Envelope</span> " +
     "<span class=\"attribute\">xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"</span>" +
     "<span class=\"tag\">&gt;</span>");
    printer.println(indent(2) + "<span class=\"tag\">&lt;S:Body&gt;</span>");
    printer.println(indent(4) + "<span class=\"tag\">&lt;ns2:" + operationName +
      "Response</span> <span class=\"attribute\">xmlns:ns2=\"http://" +
      moduleName + ".matrix.org/\"</span><span class=\"tag\">&gt;</span>");

    Response response = operation.getResponse();
    if (response != null)
    {
      String qname = response.getType();
      Type type = moduleFactory.getType(qname);
      if (type instanceof Enumeration || type == null)
      {
        printer.print(indent(6) + "<span class=\"tag\">&lt;" +
          response.getName() + "&gt;</span>");
        printer.print("{" + qname + "}");
        printer.println("<span class=\"tag\">&lt;/" +
          response.getName() + "&gt;</span>");
      }
      else
      {
        printer.print(indent(6) + "<span class=\"tag\">&lt;" +
          response.getName() + "</span>");
        if (type instanceof Entity)
        {
          generateSampleAttributes(printer, ((Entity)type).getAttributes());
          printer.println("<span class=\"tag\">&gt;</span>");
          generateSampleInstance(printer, (Entity)type, 8);
        }
        else if (type instanceof Struct)
        {
          generateSampleAttributes(printer, ((Struct)type).getAttributes());
          printer.println("<span class=\"tag\">&gt;</span>");
          generateSampleInstance(printer, (Struct)type, 8);
        }
        else printer.println("<span class=\"tag\">&gt;</span>");
        printer.println(indent(6) + "<span class=\"tag\">&lt;/" +
         response.getName() + "&gt;</span>");
      }
    }
    printer.println(indent(4) + "<span class=\"tag\">&lt;/ns2:" +
      operationName + "Response&gt;</span>");
    printer.println(indent(2) + "<span class=\"tag\">&lt;/S:Body&gt;</span>");
    printer.print("<span class=\"tag\">&lt;/S:Envelope&gt;</span>");
    printer.println("</pre></td></tr>");
  }

  private void generateSampleAttributes(PrintWriter printer,
    List<Attribute> attributes)
  {
    printer.print("<span class=\"attribute\">");
    for (Attribute attribute : attributes)
    {
      printer.print(" " + attribute.getName() + "=\"{" + attribute.getType() + "}\"");
    }
    printer.print("</span>");
  }

  private void generateSampleInstance(PrintWriter printer, 
    ComplexType complexType, int indent)
  {
    String extendsType = complexType.getExtendsType();
    if (extendsType != null)
    {
      ComplexType extendsComplexType =
        (ComplexType)moduleFactory.getType(extendsType);
      if (extendsComplexType != null)
      {
        generateSampleInstance(printer, extendsComplexType, indent);
      }
      else
      {
        printer.println(indent(indent) + "{" + extendsType + "}");
      }
    }
    Identifier identifier = complexType.getIdentifier();
    if (identifier != null)
    {
      printer.print(indent(indent) + "<span class=\"tag\">&lt;" +
        identifier.getName() + "&gt;</span>");
      printer.print("{xs:string}");
      printer.println("<span class=\"tag\">&lt;/" + identifier.getName() +
        "&gt;</span>");
    }
    for (Property property : complexType.getProperties())
    {
      String qname = property.getType();
      Type type = moduleFactory.getType(qname);
      if (type instanceof Enumeration || type == null)
      {
        printer.print(indent(indent) + "<span class=\"tag\">&lt;" +
          property.getName() + "&gt;</span>");
        printer.print("{" + qname + "}");
        printer.println("<span class=\"tag\">&lt;/" + property.getName() +
          "&gt;</span>");
      }
      else
      {
        printer.print(indent(indent) + "<span class=\"tag\">&lt;" +
          property.getName() + "</span>");
        if (type instanceof ComplexType)
        {
          generateSampleAttributes(printer, ((ComplexType)type).getAttributes());
          printer.println("<span class=\"tag\">&gt;</span>");
          generateSampleInstance(printer, (ComplexType)type, indent + 2);
        }
        else printer.println("<span class=\"tag\">&gt;</span>");
        printer.println(indent(indent) + "<span class=\"tag\">&lt;/" +
          property.getName() + "&gt;</span>");
      }
    }
  }
 
  private String indent(int num)
  {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < num; i++)
    {
      builder.append(" ");
    }
    return builder.toString();
  }

  private String getIndexHeader()
  {
    StringBuilder builder = new StringBuilder();
    if (header != null)
    {
      builder.append(header);
    }
    return builder.toString();
  }

  private String getIndexFooter()
  {
    return getModuleFooter();
  }

  private String getModuleHeader()
  {
    StringBuilder builder = new StringBuilder();
    if (header != null)
    {
      builder.append(header);
    }
    builder.append("<a href=\"index.html\">Index of modules</a>\n");
    builder.append("<hr/>\n");
    return builder.toString();
  }
  
  private String getModuleFooter()
  {
    StringBuilder builder = new StringBuilder();
    if (footer != null)
    {
      builder.append(footer);
    }
    return builder.toString();
  }

  public static void main(String[] args)
  {
    try
    {
      HTMLModuleGenerator gen = new HTMLModuleGenerator();
      gen.setInputDirectory(new File("c:/modulegen"));
      gen.setOutputDirectory(new File("c:/modulegen/html"));

      gen.generateOutput("security.xml");
      gen.generateOutput("dic.xml");
      gen.generateOutput("kernel.xml");
      gen.generateOutput("doc.xml");
      gen.generateOutput("cases.xml");
      gen.generateOutput("classif.xml");
      gen.generateOutput("policy.xml");
      gen.generateOutput("workflow.xml");
      gen.generateOutput("signature.xml");
      gen.generateOutput("report.xml");
      gen.generateOutput("cms.xml");
      gen.generateOutput("agenda.xml");
      gen.generateOutput("news.xml");
      gen.generateOutput("search.xml");
      gen.generateOutput("survey.xml");
      gen.generateOutput("forum.xml");
      gen.generateOutput("translation.xml");
      gen.generateOutput("sql.xml");
      gen.generateOutput("edu.xml");
      gen.generateOutput("elections.xml");
      gen.generateOutput("request.xml");
      gen.generateOutput("task.xml");
    }
    catch (Exception ex)
    {
    }
  }
}
