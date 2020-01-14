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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author realor
 */
public class ReverseGenerator
{
  private String moduleName;
  private HashSet<String> types = new HashSet<String>();
  private List<Class> entities = new ArrayList<Class>();
  private List<Class> structs = new ArrayList<Class>();
  private List<Class> enumerations = new ArrayList<Class>();
  private HashSet<String> imports = new HashSet<String>();

  public void generateModule(String className,
    String title, String status, String authors,
    File outputFile) throws Exception
  {
    types.clear();
    entities.clear();
    structs.clear();
    enumerations.clear();
    imports.clear();

    Class cls = Class.forName(className);
    String parts[] = className.split("\\.");
    this.moduleName = parts[2];
    String namespace = "http://" + moduleName + ".matrix.org/";

    String portName = cls.getSimpleName();
    String serviceName = portName.replaceAll("Port", "Service");

    PrintWriter printer = new PrintWriter(outputFile);

    printer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    printer.println("<module name=\"" + moduleName + "\"\n" +
       "  namespace=\"" + namespace + "\"\n" +
       "  title=\"" + title + "\"\n" +
       "  wsdlLocation=\"http://dione.esantfeliu.org/services/" + moduleName + "?wsdl\"\n" +
       "  service=\"" + serviceName + "\"\n" +
       "  port=\"" + portName + "\"\n" +
       "  version=\"1.0\"\n" +
       "  status=\"" + status + "\"\n" +
       "  authors=\"" + authors + "\">");

    exploreTypes(cls);

    generateImports(printer, cls);    
    generateTypes(printer, cls);
    generateOperations(printer, cls);

    printer.println("</module>");
    printer.close();
  }

  public void exploreTypes(Class portClass) throws Exception
  {
    Method[] methods = portClass.getMethods();
    for (Method method : methods)
    {
      Class[] paramClasses = method.getParameterTypes();
      java.lang.reflect.Type[] paramTypes = method.getGenericParameterTypes();

      for (int i = 0; i < paramClasses.length; i++)
      {
        Class paramClass = paramClasses[i];
        Class actualClass = getActualClass(paramClass, paramTypes[i]);
        exploreType(actualClass);
      }
      Class retClass = method.getReturnType();
      java.lang.reflect.Type retType = method.getGenericReturnType();
      Class actualClass = getActualClass(retClass, retType);
      exploreType(actualClass);
    }
  }

  private void exploreType(Class cls) throws Exception
  {
    String name = cls.getName();
    if (name.startsWith("org.matrix." + moduleName))
    {
      if (!types.contains(cls.getName()))
      {
        types.add(cls.getName());

        if (name.endsWith("Filter"))
        {
          structs.add(cls);
          exploreFields(cls);
        }
        else if (name.endsWith("View"))
        {
          structs.add(cls);
          exploreFields(cls);
        }
        else if (name.endsWith("MetaData"))
        {
          structs.add(cls);
          exploreFields(cls);
        }
        else if (cls.isEnum())
        {
          enumerations.add(cls);
        }
        else
        {
          XmlType xmlType = (XmlType)cls.getAnnotation(XmlType.class);
          String fieldNames[] = xmlType.propOrder();
          if (fieldNames[0].endsWith("Id"))
          {
            entities.add(cls);
            exploreFields(cls);
          }
          else
          {
            structs.add(cls);
            exploreFields(cls);
          }
        }
      }
    }
    else if (name.startsWith("org.matrix."))
    {
      String tokens[] = name.split("\\.");
      String t = tokens[2];
      imports.add(t);
    }
  }

  public void exploreFields(Class cls) throws Exception
  {
    XmlType xmlType = (XmlType)cls.getAnnotation(XmlType.class);
    String fieldNames[] = xmlType.propOrder();

    for (int i = 0; i < fieldNames.length; i++)
    {
      String fieldName = fieldNames[i];
      if (fieldName.length() > 0)
      {
        Field field = cls.getDeclaredField(fieldName);
        Class fieldClass = field.getType();
        Class actualClass = getActualClass(fieldClass, field.getGenericType());
        exploreType(actualClass);
      }
    }
  }

  public void generateTypes(PrintWriter printer, Class cls)
    throws Exception
  {
    printer.println("\n<types>");
    for (Class entCls : entities)
    {
      generateEntity(printer, entCls);
    }
    for (Class structCls : structs)
    {
      generateStruct(printer, structCls);
    }
    for (Class enumCls : enumerations)
    {
      generateEnumeration(printer, enumCls);
    }
    printer.println("</types>");
  }

  public void generateOperations(PrintWriter printer, Class cls)
  {
    printer.println("\n<operations>");
    Method[] methods = cls.getMethods();
    for (Method method : methods)
    {
      String name = method.getName();
      Class[] paramClasses = method.getParameterTypes();
      Class retClass = method.getReturnType();
      java.lang.reflect.Type paramTypes[] = method.getGenericParameterTypes();
      java.lang.reflect.Type retType = method.getGenericReturnType();
      Annotation annotations[][] = method.getParameterAnnotations();
      WebResult resultAnnotation = method.getAnnotation(WebResult.class);

      printer.println();
      printer.println("<operation name=\"" + name + "\">");

      for (int i = 0; i < paramClasses.length; i++)
      {
        Class paramClass = paramClasses[i];
        java.lang.reflect.Type paramType = paramTypes[i];
        Annotation paramAnnotations[] = annotations[i];
        String paramName = null;
        if (paramAnnotations.length > 0)
        {
          if (paramAnnotations[0] instanceof WebParam)
          {
            paramName = ((WebParam)paramAnnotations[0]).name();
          }
        }
        String type = getTypeName(paramClass, paramType);
        printer.print("<parameter name=\"" + paramName +
          "\" type=\"" + type + "\"");
        if (paramClass == List.class)
        {
          printer.print(" minOccurs=\"0\" maxOccurs=\"unbounded\" nillable=\"true\"");
        }
        if (paramClass == javax.activation.DataHandler.class)
        {
          printer.print(" expectedContentTypes=\"*/*\"");
        }
        printer.println(">");
        printer.println("</parameter>");
      }
      if (retClass != void.class)
      {
        String resultName = null;
        if (resultAnnotation != null)
        {
          resultName = resultAnnotation.name();
          if (resultName == null || resultName.length() == 0) resultName = "return";
        }
        String type = getTypeName(retClass, retType);
        printer.print("<response name=\"" + resultName +
          "\" type=\"" + type + "\"");
        if (retClass == List.class)
        {
          printer.print(" minOccurs=\"0\" maxOccurs=\"unbounded\" nillable=\"true\"");
        }
        if (retClass == javax.activation.DataHandler.class)
        {
          printer.print(" expectedContentTypes=\"*/*\"");
        }
        printer.println(">");
        printer.println("</response>");
      }
      printer.println("</operation>");
    }
    printer.println("</operations>");
  }

  private void generateImports(PrintWriter printer, Class cls)
  {
    printer.println("\n<imports>");
    for (String _import : imports)
    {
      printer.println("  <import location=\"" + _import + ".xml\"/>");
    }
    printer.println("</imports>");
  }

  private void generateEntity(PrintWriter printer, Class cls) throws Exception
  {
    String name = cls.getSimpleName();
    XmlType xmlType = (XmlType) cls.getAnnotation(XmlType.class);
    String fieldNames[] = xmlType.propOrder();

    printer.println("\n<entity name=\"" + name + "\">");
    for (int i = 0; i < fieldNames.length; i++)
    {
      String fieldName = fieldNames[i];
      if (fieldName.length() > 0)
      {
        Field field = cls.getDeclaredField(fieldName);
        
        Class fieldClass = field.getType();
        String typeName = getTypeName(fieldClass, field.getGenericType());

        String tag = (i == 0) ? "identifier" : "property";
        printer.print("<" + tag + " name=\"" + fieldName + "\"");
        printer.print(" type=\"" + typeName + "\"");
        if (fieldClass != int.class &&
            fieldClass != long.class &&
            fieldClass != boolean.class &&
            fieldClass != float.class &&
            fieldClass != double.class)
        {
          printer.print(" minOccurs=\"0\"");
        }
        if (fieldClass == List.class)
        {
          printer.print(" maxOccurs=\"unbounded\" nillable=\"true\"");
        }
        if (fieldClass == javax.activation.DataHandler.class)
        {
          printer.print(" expectedContentTypes=\"*/*\"");
        }
        printer.println(">");
        printer.println("</" + tag + ">");
      }
    }
    // attributes
    Field fields[] = cls.getDeclaredFields();
    for (Field field : fields)
    {
      XmlAttribute attr = field.getAnnotation(XmlAttribute.class);
      if (attr != null)
      {
        String typeName = getTypeName(field.getType(), field.getGenericType());
        printer.print("<attribute name=\"" + field.getName() + "\"");
        printer.print(" type=\"" + typeName +
          "\" required=\"" + attr.required() + "\">");
        printer.println("</attribute>");
      }
    }
    printer.println("</entity>");
  }

  private void generateStruct(PrintWriter printer, Class cls) throws Exception
  {
    String name = cls.getSimpleName();
    XmlType xmlType = (XmlType) cls.getAnnotation(XmlType.class);
    String fieldNames[] = xmlType.propOrder();

    printer.println("\n<struct name=\"" + name + "\">");
    for (String fieldName : fieldNames)
    {
      if (fieldName.length() > 0)
      {
        Field field = cls.getDeclaredField(fieldName);
        Class fieldClass = field.getType();
        String typeName = getTypeName(fieldClass, field.getGenericType());

        printer.print("<property name=\"" + fieldName + "\"");
        printer.print(" type=\"" + typeName + "\"");
        if (fieldClass != int.class &&
            fieldClass != long.class &&
            fieldClass != boolean.class &&
            fieldClass != float.class &&
            fieldClass != double.class)
        {
          printer.print(" minOccurs=\"0\"");
        }
        if (fieldClass == List.class)
        {
          printer.print(" maxOccurs=\"unbounded\" nillable=\"true\"");
        }
        if (fieldClass == javax.activation.DataHandler.class)
        {
          printer.print(" expectedContentTypes=\"*/*\"");
        }
        printer.println(">");
        printer.println("</property>");
      }
    }
    // attributes
    Field fields[] = cls.getDeclaredFields();
    for (Field field : fields)
    {
      XmlAttribute attr = field.getAnnotation(XmlAttribute.class);
      if (attr != null)
      {
        String typeName = getTypeName(field.getType(), field.getGenericType());
        printer.print("<attribute name=\"" + field.getName() + "\"");
        printer.print(" type=\"" + typeName +
          "\" required=\"" + attr.required() + "\">");
        printer.println("</attribute>");
      }
    }
    printer.println("</struct>");
  }

  private void generateEnumeration(PrintWriter printer, Class cls)
  {
    String name = cls.getSimpleName();
    printer.println("\n<enumeration name=\"" + name + "\">");
    Field[] values = cls.getFields();
    for (Field value : values)
    {
      printer.println("<value name=\"" + value.getName() + "\"/>");
    }
    printer.println("</enumeration>");
  }

  private Class getActualClass(Class cls, java.lang.reflect.Type t)
  {
    Class actualClass = cls;
    if (cls == List.class && t instanceof ParameterizedType)
    {
      ParameterizedType pt = (ParameterizedType)t;
      java.lang.reflect.Type[] args = pt.getActualTypeArguments();
      actualClass = (Class)args[0];
    }
    return actualClass;
  }

  private String getTypeName(Class cls, java.lang.reflect.Type t)
  {
    Class actualClass = getActualClass(cls, t);

    String type = actualClass.getName();
    if (type.equals("java.lang.String")) type = "xs:string";
    else if (type.equals("java.lang.Long")) type = "xs:long";
    else if (type.equals("java.lang.Integer")) type = "xs:int";
    else if (type.equals("java.lang.Boolean")) type = "xs:boolean";
    else if (type.equals("java.lang.Float")) type = "xs:float";
    else if (type.equals("java.lang.Double")) type = "xs:double";
    else if (type.equals("javax.xml.datatype.XMLGregorianCalendar")) type = "xs:dateTime";
    else if (type.equals("int")) type = "xs:int";
    else if (type.equals("long")) type = "xs:long";
    else if (type.equals("boolean")) type = "xs:boolean";
    else if (type.equals("float")) type = "xs:float";
    else if (type.equals("double")) type = "xs:double";
    else if (type.equals("[B")) type = "xs:base64Binary";
    else if (type.equals("javax.activation.DataHandler")) type = "xs:base64Binary";
    else if (type.startsWith("org.matrix."))
    {
      String parts[] = type.split("\\.");
      type = parts[2] + ":" + parts[3];
    }
    else type = "xs:anyType";
    return type;
  }

  public static void main(String[] args)
  {
    try
    {
      ReverseGenerator gen = new ReverseGenerator();

      gen.generateModule("org.matrix.security.SecurityManagerPort",
        "Gestor de seguretat", "FINAL", "Ricard Real",
        new File("c:/modulegen/security.xml"));

      gen.generateModule("org.matrix.dic.DictionaryManagerPort",
        "Diccionari de tipologies", "FINAL", "Ricard Real",
        new File("c:/modulegen/dic.xml"));

      gen.generateModule("org.matrix.kernel.KernelManagerPort",
        "Gestor de persones i territori", "FINAL", "Ricard Real",
        new File("c:/modulegen/kernel.xml"));

      gen.generateModule("org.matrix.doc.DocumentManagerPort",
        "Gestor de documents", "FINAL", "Abel Blanque, Ricard Real",
        new File("c:/modulegen/doc.xml"));

      gen.generateModule("org.matrix.cases.CaseManagerPort",
        "Gestor d'expedients", "FINAL", "Abel Blanque, Ricard Real",
        new File("c:/modulegen/cases.xml"));

      gen.generateModule("org.matrix.cms.CMSManagerPort",
        "Gestor de continguts web", "FINAL", "Jordi LÃ³pez, Ricard Real",
        new File("c:/modulegen/cms.xml"));

      gen.generateModule("org.matrix.agenda.AgendaManagerPort",
        "Agenda d'esdeveniments", "FINAL", "Abel Blanque",
        new File("c:/modulegen/agenda.xml"));

      gen.generateModule("org.matrix.classif.ClassificationManagerPort",
        "Gestor del quadre de classificaciÃ³ documental", "FINAL", "Ricard Real",
        new File("c:/modulegen/classif.xml"));

      gen.generateModule("org.matrix.edu.EducationManagerPort",
        "Gestor de cursos", "FINAL", "Cecilia Comas, Ricard Real",
        new File("c:/modulegen/edu.xml"));

      gen.generateModule("org.matrix.elections.ElectionsManagerPort",
        "Gestor d'eleccions", "REVISION", "Ricard Real",
        new File("c:/modulegen/elections.xml"));

      gen.generateModule("org.matrix.forum.ForumManagerPort",
        "Gestor de forums", "REVISION", "Ricard Real",
        new File("c:/modulegen/forum.xml"));

      gen.generateModule("org.matrix.news.NewsManagerPort",
        "Gestor de noticies", "FINAL", "Jordi LÃ³pez",
        new File("c:/modulegen/news.xml"));

      gen.generateModule("org.matrix.policy.PolicyManagerPort",
        "Gestor de politiques documentals", "FINAL", "Ricard Real, Abel Blanque",
        new File("c:/modulegen/policy.xml"));

      gen.generateModule("org.matrix.report.ReportManagerPort",
        "Generador d'informes", "REVISION", "Ricard Real",
        new File("c:/modulegen/report.xml"));

      gen.generateModule("org.matrix.request.RequestManagerPort",
        "Gestor de peticions", "DEPRECATED", "Alex Salinas",
        new File("c:/modulegen/request.xml"));

      gen.generateModule("org.matrix.search.SearchManagerPort",
        "Cercador global", "REVISION", "Jordi LÃ³pez",
        new File("c:/modulegen/search.xml"));

      gen.generateModule("org.matrix.signature.SignatureManagerPort",
        "Gestor de signatures electrÃ³niques", "REVISION", "Ricard Real",
        new File("c:/modulegen/signature.xml"));

      gen.generateModule("org.matrix.sql.SQLManagerPort",
        "Executor de comandes SQL", "FINAL", "Abel Blanque",
        new File("c:/modulegen/sql.xml"));

      gen.generateModule("org.matrix.survey.SurveyManagerPort",
        "Gestor d'enquestes", "REVISION", "Jordi LÃ³pez",
        new File("c:/modulegen/survey.xml"));

      gen.generateModule("org.matrix.translation.TranslationManagerPort",
        "Gestor de traduccions", "FINAL", "Ricard Real, Jordi LÃ³pez",
        new File("c:/modulegen/translation.xml"));

      gen.generateModule("org.matrix.workflow.WorkflowManagerPort",
        "Gestor de fluxos de treball", "REVISION", "Ricard Real",
        new File("c:/modulegen/workflow.xml"));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
