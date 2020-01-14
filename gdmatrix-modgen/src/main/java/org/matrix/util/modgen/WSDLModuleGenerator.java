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
import java.io.PrintWriter;
import java.net.URL;

/**
 *
 * @author realor
 */
public class WSDLModuleGenerator extends ModuleGenerator
{
  public WSDLModuleGenerator()
  {
    super();
  }

  public WSDLModuleGenerator(ModuleFactory moduleFactory)
  {
    super(moduleFactory);
  }

  @Override
  public void generateOutput(Module module) throws Exception
  {
    outputDirectory.mkdirs();
    generateXSD(module);
    generateWSDL(module);
  }

  public static void main(String[] args)
  {
    try
    {
      WSDLModuleGenerator gen = new WSDLModuleGenerator();
      gen.setInputDirectory(new File("c:/modulegen"));
      gen.setOutputDirectory(new File("c:/modulegen"));
      
      gen.generateOutput("dic.xml");
      gen.generateOutput("cases.xml");
    }
    catch (Exception ex)
    {
    }
  }

  /***** private *****/

  private void generateXSD(Module module) throws Exception
  {
    PrintWriter printer =
      new PrintWriter(new File(outputDirectory, module.getName() + ".xsd"), "UTF-8");
    try
    {
      printer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
      printer.println("<xs:schema version=\"1.0\"");
      printer.println("  targetNamespace=\"" + module.getNamespace() +"\"");
      printer.println("  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");
      printer.println("  xmlns:mime=\"http://www.w3.org/2005/05/xmlmime\"");

      generateImportPrefixes(printer, module);
      printer.println("  xmlns:" + module.getName() + "=\"" +
        module.getNamespace() + "\">");

      generateDocumentations(printer, module);

      for (Import _import : module.getImports())
      {
        generateImportSchema(printer, module, _import);
      }

      // parameter elements and types
      for (Operation operation : module.getOperations())
      {
        printer.println("\n<!-- ********** operation " + operation.getName() + " ********** -->");
        printer.println("<xs:complexType name=\"" + operation.getName() + "\">");
        generateDocumentations(printer, operation);
        printer.println("<xs:sequence>");
        for (Parameter parameter : operation.getParameters())
        {
          generateParameter(printer, parameter);
        }
        printer.println("</xs:sequence>");
        printer.println("</xs:complexType>");

        printer.println("<xs:complexType name=\"" + operation.getName() + "Response\">");
        printer.println("<xs:sequence>");
        if (operation.getResponse() != null)
        {
          generateResponse(printer, operation.getResponse());
        }
        printer.println("</xs:sequence>");
        printer.println("</xs:complexType>");

        printer.println("<xs:element name=\"" + operation.getName() +
          "\" type=\"" + module.getName() + ":" + operation.getName() + "\"/>");

        printer.println("<xs:element name=\"" + operation.getName() +
          "Response\" type=\"" + module.getName() + ":" + operation.getName() + "Response\"/>");
      }

      printer.println("\n<!-- ********** entities/structs/enums ********** -->");

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
        printer.println();
      }
      printer.println("</xs:schema>");
    }
    finally
    {
      printer.close();
    }
  }

  private void generateWSDL(Module module) throws Exception
  {
    PrintWriter printer =
      new PrintWriter(new File(outputDirectory, module.getName() + ".wsdl"), "UTF-8");
    try
    {
      printer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
      printer.println("<definitions name=\"" + module.getService() + "\"");
      printer.println("  targetNamespace=\"" + module.getNamespace() + "\"");
      printer.println("  xmlns=\"http://schemas.xmlsoap.org/wsdl/\"");
      printer.println("  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");
      printer.println("  xmlns:mime=\"http://www.w3.org/2005/05/xmlmime\"");
      printer.println("  xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"");
      printer.println("  xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/" +
        "oasis-200401-wss-wssecurity-utility-1.0.xsd\"");

      generateImportPrefixes(printer, module);
      printer.println("  xmlns:" + module.getName() + "=\"" +
        module.getNamespace() + "\">");

      generateDocumentations(printer, module);

      // types
      printer.println("\n<!-- ********** types ********** -->");
      printer.println("<types>");

      // imports
      printer.println("<xs:schema>");
      printer.println("<xs:import namespace=\"" + module.getNamespace() +
         "\" schemaLocation=\"" + module.getName() + ".xsd\"/>");
      printer.println("</xs:schema>");

      for (Import _import : module.getImports())
      {
        printer.println("<xs:schema>");
        generateImportSchema(printer, module, _import);
        printer.println("</xs:schema>");
      }

      printer.println("</types>");

      // messages
      printer.println("\n<!-- ********** messages ********** -->");
      for (Operation operation : module.getOperations())
      {
        printer.println("<message name=\"" + operation.getName() + "\">");
        printer.println("<part name=\"parameters\" element=\"" +
          module.getName() + ":" + operation.getName() + "\"/>");
        printer.println("</message>");

        printer.println("<message name=\"" + operation.getName() + "Response\">");
        printer.println("<part name=\"parameters\" element=\"" +
          module.getName() + ":" + operation.getName() + "Response\"/>");
        printer.println("</message>");
      }

      // portType
      printer.println("\n<!-- ********** portType ********** -->");
      printer.println("<portType name=\"" + module.getPort() + "\">");
      for (Operation operation : module.getOperations())
      {
        printer.println("<operation name=\"" + operation.getName() + "\">");
        generateDocumentations(printer, operation);
        printer.println("<input message=\"" + module.getName() +
          ":" + operation.getName() + "\"/>");
        printer.println("<output message=\"" + module.getName() +
          ":" + operation.getName() + "Response\"/>");
        printer.println("</operation>");
      }
      printer.println("</portType>");

      // binding
      printer.println("\n<!-- ********** binding ********** -->");
      String binding = module.getPort() + "Binding";
      printer.println("<binding name=\"" + binding +
        "\" type=\"" + module.getName() + ":" + module.getPort() + "\">");
      printer.println("<soap:binding " +
        "transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\"/>");
      for (Operation operation : module.getOperations())
      {
        printer.println("<operation name=\"" + operation.getName() + "\">");
        printer.println("<soap:operation soapAction=\"\"/>");
        printer.println("<input>");
        printer.println("<soap:body use=\"literal\"/>");
        printer.println("</input>");
        printer.println("<output>");
        printer.println("<soap:body use=\"literal\"/>");
        printer.println("</output>");
        printer.println("</operation>");
      }
      printer.println("</binding>");

      // service
      printer.println("\n<!-- ********** service ********** -->");
      printer.println("<service name=\"" + module.getService() + "\">");
      printer.println("<port name=\"" + module.getPort() + "\" binding=\"" +
        module.getName() + ":" + binding + "\">");
      printer.println("<soap:address location=\"REPLACE_WITH_ACTUAL_URL\"/>");
      printer.println("</port>");
      printer.println("</service>");

      printer.println("</definitions>");
    }
    finally
    {
      printer.close();
    }
  }

  private void generateComplexType(PrintWriter printer, ComplexType complexType)
  {
    String extendsType = complexType.getExtendsType();
    printer.println("<xs:complexType name=\"" + complexType.getName() + "\">");
    generateDocumentations(printer, complexType);
    if (extendsType != null)
    {
      printer.println("<xs:complexContent>");
      printer.println("<xs:extension base=\"" + extendsType + "\">");
    }
    printer.println("<xs:sequence>");
    Identifier identifier = complexType.getIdentifier();
    if (identifier != null) generateIdentifier(printer, identifier);
    for (Property property : complexType.getProperties())
    {
      generateProperty(printer, property);
    }
    printer.println("</xs:sequence>");
    for (Attribute attribute : complexType.getAttributes())
    {
      generateAttribute(printer, attribute);
    }
    if (extendsType != null)
    {
      printer.println("</xs:extension>");
      printer.println("</xs:complexContent>");
    }
    printer.println("</xs:complexType>");
  }

  private void generateEnumeration(PrintWriter printer, Enumeration enumeration)
  {
    String extendsType = enumeration.getExtendsType();
    printer.println("<xs:simpleType name=\"" + enumeration.getName() + "\">");
    generateDocumentations(printer, enumeration);
    printer.println("<xs:restriction base=\"xs:string\">");
    if (extendsType != null)
    {
      Enumeration extendsEnumeration =
        (Enumeration)moduleFactory.getType(extendsType);
      if (extendsEnumeration != null)
      {
        for (EnumerationValue value : extendsEnumeration.getValues())
        {
          printer.println("<xs:enumeration value=\"" + value.getName() + "\"/>");
        }
      }
    }
    for (EnumerationValue value : enumeration.getValues())
    {
      printer.println("<xs:enumeration value=\"" + value.getName() + "\"/>");
    }
    printer.println("</xs:restriction>");
    printer.println("</xs:simpleType>");
  }

  private void generateIdentifier(PrintWriter printer, Identifier identifier)
  {
    printer.println("<xs:element name=\"" + identifier.getName() +
      "\" type=\"xs:string\" minOccurs=\"0\">");
    printer.println("</xs:element>");
  }

  private void generateParameter(PrintWriter printer, Parameter parameter)
  {
    generateTypifiedComponent(printer, parameter);
  }

  private void generateResponse(PrintWriter printer, Response response)
  {
    generateTypifiedComponent(printer, response);
  }

  private void generateProperty(PrintWriter printer, Property property)
  {
    generateTypifiedComponent(printer, property);
  }

  private void generateAttribute(PrintWriter printer, Attribute attribute)
  {
    printer.print("<xs:attribute name=\"" + attribute.getName() + "\"");
    printer.print(" type=\"" + attribute.getType() + "\" ");
    if (attribute.isRequired())
    {
      printer.println("use=\"required\"/>");
    }
    else
    {
      printer.println("/>");
    }
  }

  private void generateTypifiedComponent(PrintWriter printer,
    ComplexTypifiedComponent component)
  {
    printer.print("<xs:element name=\"" + component.getName() + "\"");
    printer.print(" type=\"" + component.getType() + "\"");
    if (component.getMinOccurs() != null)
    {
      printer.print(" minOccurs=\"" + component.getMinOccurs() + "\"");
    }
    if (component.getMaxOccurs() != null)
    {
      printer.print(" maxOccurs=\"" + component.getMaxOccurs() + "\"");
    }
    if (component.isNillable())
    {
      printer.print(" nillable=\"true\"");
    }
    if (component.getExpectedContentTypes() != null)
    {
      printer.print(" mime:expectedContentTypes=\"" +
        component.getExpectedContentTypes() + "\"");
    }
    printer.print(">");
    printer.println("</xs:element>");
  }

  private void generateDocumentations(PrintWriter printer, NamedComponent component)
  {
    for (Documentation documentation : component.getDocumentations())
    {
      printer.println("<xs:annotation>");
      printer.print("<xs:documentation");
      if (documentation.getSource() != null)
      {
        printer.print(" source=\"" + documentation.getSource() + "\"");
      }
      if (documentation.getLanguage() != null)
      {
        printer.print(" xml:lang=\"" + documentation.getLanguage() + "\"");
      }
      printer.println(">");
      printer.println(documentation.getText());
      printer.println("</xs:documentation>");
      printer.println("</xs:annotation>");
    }
  }

  private void generateImportPrefixes(PrintWriter printer, Module module)
    throws Exception
  {
    for (Import _import : module.getImports())
    {
      if (!_import.isVirtual())
      {
        String moduleName = _import.getModule();
        String namespace;
        String prefix;
        if (moduleName != null) // module import
        {
          URL url = new URL(module.getLocation(), moduleName + ".xml");
          Module importModule = moduleFactory.getModule(url);
          prefix = importModule.getName();
          namespace = importModule.getNamespace();
        }
        else // external schema
        {
          namespace = _import.getNamespace();
          prefix = _import.getPrefix();
        }
        printer.println("  xmlns:" + prefix + "=\"" + namespace + "\"");
      }
    }
  }

  private void generateImportSchema(PrintWriter printer, Module module,
    Import _import) throws Exception
  {
    if (!_import.isVirtual())
    {
      String moduleName = _import.getModule();
      String namespace;
      String schemaLocation;
      if (moduleName != null) // module import
      {
        URL url = new URL(module.getLocation(), moduleName + ".xml");
        Module importModule = moduleFactory.getModule(url);
        namespace = importModule.getNamespace();
        schemaLocation = moduleName + ".xsd";
      }
      else // external schema
      {
        namespace = _import.getNamespace();
        schemaLocation = _import.getLocation();
      }
      printer.println("<xs:import namespace=\"" +
        namespace + "\" schemaLocation=\"" + schemaLocation + "\"/>");
    }
  }
}
