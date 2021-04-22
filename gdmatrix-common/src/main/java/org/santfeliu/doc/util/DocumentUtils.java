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
package org.santfeliu.doc.util;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;
import javax.activation.DataHandler;

import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.matrix.dic.Property;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;

import org.matrix.translation.TranslationConstants;
import org.santfeliu.util.MimeTypeMap;

/**
 *
 * @author blanquepa
 */
public class DocumentUtils
{
  public static void setProperties(Document document, Map<String, Object> properties)
    throws Exception
  {
    for (Map.Entry property : properties.entrySet())
    {
      String name = (String)property.getKey();
      Object value = property.getValue();
      setProperty(document, name, value);
    }
  }

  public static Map<String, List<String>> getProperties(Document document)
    throws Exception
  {
    Map<String, List<String>> result;

    result = getUserProperties(document);
    result.putAll(getSystemProperties(document));
    return result;
  }

  public static String getPropertiesAsString(Map<String, Object> properties)
  {
    StringBuilder sb = new StringBuilder();

    if (properties != null && properties.size() > 0)
    {
      Set<Map.Entry<String, Object>> entries = properties.entrySet();
      for (Map.Entry<String, Object> entry : entries)
      {
        String name = entry.getKey();
        Object value = entry.getValue();
        if (value != null)
        {
          if (value instanceof List && ((List)value).size() > 0)
          {
            Iterator it = ((List)value).iterator();
            while (it.hasNext())
            {
              sb.append(name);
              sb.append("=");
              sb.append(String.valueOf(it.next()));
              sb.append(";\n");
            }
          }
          else
          {
            sb.append(name);
            sb.append("=");
            sb.append(String.valueOf(value));
            sb.append(";\n");
          }
        }
      }
      if (sb.length() > 1)
        sb.deleteCharAt(sb.length() - 1);
      return sb.toString();
    }
    else return "";
  }

  public static List<Property> getPropertiesFromString(String propertiesString)
  {
    Map<String, List<String>> map = new HashMap();
    List<Property> properties = new ArrayList();

    if (propertiesString != null && propertiesString.contains(";"))
    {
      String[] props = propertiesString.split(";");
      for (String propString : props)
      {
        String[] splitProp = propString.split("=");
        if (splitProp.length == 2)
        {
          String name = splitProp[0].trim();
          String value = splitProp[1];
          List<String> list = map.get(name);
          if (list == null)
          {
            Property p = new Property();
            p.setName(name);
            list = p.getValue();
            map.put(name, list);
            properties.add(p);
          }
          list.add(value);
        }
      }
    }
    
    return properties;
  }

  public static Map<String, List<String>> getUserProperties(Document document)
    throws Exception
  {
    Map<String, List<String>> result = new HashMap();
    
    //Property list
    List<Property> properties = document.getProperty();
    for (Property property : properties)
    {
      String propName = property.getName();
      List<String> values = new ArrayList();
      if (property.getValue() != null)
      {
        for (String value : property.getValue())
        {
          if (value != null && value.length() > 0) //avoid unvalued properties
            values.add(value);
        }
        result.put(propName, values);
      }
    }
    
    return result;
  }

  public static Map<String, List<String>> getSystemProperties(Document document)
    throws Exception
  {
    Map<String, List<String>> result = new HashMap();
    
    //System properties
    Class docClass = document.getClass();
    Method[] docClassMethods = docClass.getDeclaredMethods();
    for (Method docClassMethod : docClassMethods)
    {
      String methodName = docClassMethod.getName();
      if ((methodName.startsWith("get") ||
        methodName.startsWith("is")) &&
        !methodName.equalsIgnoreCase("getRelatedDocument") &&
        !methodName.equalsIgnoreCase("getSignature") &&
        !methodName.equalsIgnoreCase("getProperty"))
      {
        if (methodName.startsWith("get")) methodName = methodName.substring(3);
        else if (methodName.startsWith("is")) methodName = methodName.substring(2);
        //Property p = new Property();
        String propertyName = 
          methodName.substring(0, 1).toLowerCase() + 
          methodName.substring(1, methodName.length());

        Object value = docClassMethod.invoke(document, new Object[]{});
        if (value instanceof List)
        {
          result.put(propertyName, (List<String>)value);
        }
        else if (!(value instanceof Collection))
        {
          List list = new ArrayList();
          list.add(String.valueOf(value));
          result.put(propertyName, list);
        }
      }
    }
    
    return result;
  }

  public static void transferUserToSystemProperties(Document document) throws Exception
  {
    Map userProperties = getUserProperties(document);
    setProperties(document, userProperties);
  }

  /*
   * @deprecated replaced by @link org.santfeliu.dic.util.DictionaryUtils.getProperty
   */
  @Deprecated
  public static Property getProperty(Document document, String propertyName)
  {
    try
    {
      //Search in property list
      List<Property> properties = document.getProperty();
      for (Property property : properties)
      {
        if (propertyName.equalsIgnoreCase(property.getName()))
          return property;
      }

      //Search in document getters
      Class docClass = document.getClass();
      Method[] docClassMethods = docClass.getMethods();
      for (Method docClassMethod : docClassMethods)
      {
        String methodName = docClassMethod.getName();
        if (methodName.equalsIgnoreCase("get" + propertyName) ||
          methodName.equalsIgnoreCase("is" + propertyName))
        {
          Property p = new Property();
          p.setName(propertyName);
          Object value = docClassMethod.invoke(document, new Object[]{});
          if (value instanceof Collection)
          {
            for (Object v : (Collection)value)
            {
              p.getValue().add(String.valueOf(v));
            }
          }
          else
            p.getValue().add(String.valueOf(value));

          return p;
        }
      }
      return null;
    }
    catch (Exception e)
    {
      return null;
    }
  }

  public static List<String> getPropertyValues(Document document,
    String propertyName)
  {
    try
    {
      Property property = getProperty(document, propertyName);
      return property.getValue();
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public static String getPropertyValue(Document document,
    String propertyName)
  {
    try
    {
      Property property = getProperty(document, propertyName);
      return property.getValue().get(0);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

   /*
   * @deprecated replaced by @link org.santfeliu.dic.util.DictionaryUtils.setProperty
   */
  @Deprecated
  public static void setProperty(Document document, String propertyName,
    Object value)
  {
    setProperty(document, propertyName, value, false);
  }

   /*
   * @deprecated replaced by @link org.santfeliu.dic.util.DictionaryUtils.setProperty
   */
  @Deprecated
  public static void setProperty(Document document,
    String propertyName, Object value, boolean addIfMultivalued)
  {
    //System properties
    Class docClass = document.getClass();
    Method[] docClassMethods = docClass.getDeclaredMethods();
    boolean propertyFound = false;
    int i = 0;
    while (i < docClassMethods.length && !propertyFound)
    {
      Method docClassMethod = docClassMethods[i];
      String methodName = docClassMethod.getName();
      if (methodName.equalsIgnoreCase("set" + propertyName))
      {
        Class[] parameterTypes = docClassMethod.getParameterTypes();
        Class parameterType = parameterTypes[0];
        try
        {
          if (!(Collection.class.isAssignableFrom(parameterType))
            && value != null)
          {
            if (parameterType.equals(value.getClass()))
              docClassMethod.invoke(document, new Object[]{value});
            else if (List.class.isAssignableFrom(value.getClass()))
              docClassMethod.invoke(document, new Object[]{((List)value).get(0)});
          }
          propertyFound = true;
        }
        catch (Exception e)
        {
          propertyFound = false;
        }
      }
      else if (methodName.equalsIgnoreCase("get" + propertyName)
        && !"property".equalsIgnoreCase(propertyName))
      {
        Class returnType = docClassMethod.getReturnType();
        if (List.class.isAssignableFrom(returnType))
        {
          try
          {
            List values =
              (List)docClassMethod.invoke(document, new Object[]{});
            if (!addIfMultivalued) values.clear();
            if (value != null && List.class.isAssignableFrom(value.getClass()))
            {
              for (int j = 0; j < ((List)value).size(); j++)
              {
                values.add(((List)value).get(j));
              }
            }
            else if (value != null)
              values.add(value);
            propertyFound = true;
          }
          catch (Exception e)
          {
            propertyFound = false;
          }
        }
      }
      i++;
    }

    //Dynamic Properties
    if (!propertyFound)
    {
      List<Property> properties = document.getProperty();
      List<Property> removed = new ArrayList();
      boolean propFound = false;
      for (Property property : properties)
      {
        if (propertyName.equalsIgnoreCase(property.getName()))
        {
          if (!addIfMultivalued) property.getValue().clear();
          if (value != null && List.class.isAssignableFrom(value.getClass()))
            property.getValue().addAll((List)value);
          else if (value != null)
            property.getValue().add(String.valueOf(value));
          else if (value == null)
            //properties.remove(property);
            removed.add(property);
          propFound = true;
        }
      }
      for (Property p : removed)
      {
        properties.remove(p);
      }
      if (!propFound)
      {
        Property p = new Property();
        p.setName(propertyName);
        if (value != null && List.class.isAssignableFrom(value.getClass()))
          p.getValue().addAll((List)value);
        else
          p.getValue().add(String.valueOf(value));
        document.getProperty().add(p);
      }
    }
  }
  
  public static void setMultivaluedProperty(Document document, 
    String propertyName, List<String> values)
    throws Exception
  {
    Class docClass = document.getClass();
    Method[] docClassMethods = docClass.getMethods();
    boolean propertyFound = false;
    for (Method docClassMethod : docClassMethods)
    {
      String methodName = docClassMethod.getName();
      if (methodName.equalsIgnoreCase("get" + propertyName))
      {
        Class returnType = docClassMethod.getReturnType();        
        if (List.class.isAssignableFrom(returnType))
        {
          List curValues = 
            (List)docClassMethod.invoke(document, new Object[]{});
          values.clear();
          values.addAll(curValues);
        }
        propertyFound = true;
      }
    }
   
    if (!propertyFound)
    {
      List<Property> properties = document.getProperty();
      boolean propFound = false;
      for (Property property : properties)
      {
        if (propertyName.equalsIgnoreCase(property.getName()))
        {
          property.getValue().clear();
          property.getValue().addAll(values);
          propFound = true;
        }
      }
      if (!propFound)
      {
        Property p = new Property();
        p.setName(propertyName);
        p.getValue().addAll(values);
        document.getProperty().add(p);
      }
    }
  }  
  
  public static void removeProperty(Document document, String propertyName)
    throws Exception
  {
    setProperty(document, propertyName, null);
  }
  
  public static RelatedDocument getRelatedDocument(Document document, RelationType relType,
    String relName)
  {
    List<RelatedDocument> relDocs = document.getRelatedDocument();
    if (relDocs != null)
    {
      for (RelatedDocument relDoc : relDocs)
      {
        if (relDoc.getName().equals(relName) && relDoc.getRelationType().equals(relType))
          return relDoc;
      }
    }
    
    return null;
  }
  
  public static RelationType revertRelation(RelationType relType)
  {
    if (relType == null)
      return relType;
    
    String text = relType.toString();
    if (text.startsWith("REV_"))
      return RelationType.valueOf(text.substring(4));
    else
      return RelationType.valueOf("REV_" + text);
  }

  /*
   * @deprecated replaced by @link org.santfeliu.dic.util.DictionaryUtils.setProperty
   */
  @Deprecated
  public static void setProperty(DocumentFilter filter, String propertyName, 
    String propertyValue)
  {
    List<Property> properties = filter.getProperty();
    boolean propFound = false;
    for (Property property : properties)
    {
      if (propertyName.equalsIgnoreCase(property.getName()))
      {
        property.getValue().clear();
        property.getValue().add(propertyValue);
        propFound = true;
      }
    }
    if (!propFound)
    {
      Property p = new Property();
      p.setName(propertyName);
      p.getValue().add(propertyValue);
      filter.getProperty().add(p);
    }
  }

  public static void setOrderByProperty(DocumentFilter filter, String name, boolean desc)
  {
    OrderByProperty p = new OrderByProperty();
    p.setName(name);
    p.setDescending(desc);
    filter.getOrderByProperty().add(p);
  }
  
  public static String getContentId(Document document)
  {
    if (document != null && document.getContent() != null)
      return document.getContent().getContentId();
    else
      return null;
  }
  
  public static String getContentType(Document document)
  {
    if (document != null && document.getContent() != null)
      return document.getContent().getContentType();
    else
      return null;
  }  
  
  public static void setContentData(Document document, DataHandler dh)
  {
    Content content = document.getContent();
    if (content == null) content = new Content();
    content.setData(dh);
    document.setContent(content);
  }
  
  public static DataHandler getContentData(Document document)
  {
    if (document != null && document.getContent() != null && 
      document.getContent().getData() != null)
      return document.getContent().getData();
    else
      return null;
  }

  private static Property cloneProperty(Property property)
  {
    Property result;
    if (property != null)
    {
      result = new Property();
      result.setName(property.getName());
      result.getValue().addAll(property.getValue());
    }
    else
      result = property;

    return result;
  }

  public static List<Property> cloneProperties(List<Property> properties)
  {
    List<Property> result;

    if (properties != null)
    {
      result = new ArrayList();
      for (Property property : properties)
      {
        Property p = cloneProperty(property);
        result.add(p);
      }
    }
    else
       result = properties;

    return result;
  }

  public static long getSize(String sizeString)
  {
    long size = 0;
    if (sizeString != null)
    {
      String[] array = sizeString.split(" ");
      if (array != null && array.length == 2)
      {
        String digit = array[0];
        digit = digit.replaceAll("\\.", "");
        size = Long.parseLong(digit);

        String unit = array[1];
        if ("KB".equalsIgnoreCase(unit))
          size = size * 1024;
        else if ("MB".equalsIgnoreCase(unit))
          size = size * 1024 * 1024;
        else if ("GB".equalsIgnoreCase(unit))
          size = size * 1024 * 1024 * 1024;
      }
    }
    return size;
  }

  public static String getSizeString(long bytes)
  {
    Long size = bytes;
    String sizeUnit = " ";
    String strSize = String.valueOf(size.longValue());
    if (size < 1024)
    {
      sizeUnit += "B";
    }
    else if ((size >= 1024) && (size < 1024*1024))
    {
      sizeUnit += "KB";
      float kbSize = (size) / 1024f;
      strSize = String.valueOf(kbSize);
    }
    else if ((size >= 1024*1024) && (size < 1024*1024*1024))
    {
      sizeUnit += "MB";
      float mbSize = (size) / (1024f*1024f);
      strSize = String.valueOf(mbSize);
    }
    else
    {
      sizeUnit += "GB";
      float gbSize = (size.longValue()) / (1024f*1024f*1024f);
      strSize = String.valueOf(gbSize);
    }
    int indexComma = strSize.indexOf('.');
    if (indexComma >= 0)
    {
      int numDecimals = strSize.length() - indexComma - 1;
      if (numDecimals > 2)
      {
        strSize = strSize.substring(0, indexComma + 3);
      }
    }
    return strSize + sizeUnit;
  }

  public static String extendLanguage(String language)
  {
    if (language == null)
      return "";
    if (language.equals("ca"))
      return "català";
    if (language.equals("es"))
      return "castellà";
    if (language.equals("en"))
      return "anglès";
    if (language.equals("fr"))
      return "francès";
    if (language.equals("it"))
      return "italià";
    if (language.equals("de"))
      return "alemany";
    if (language.equals(TranslationConstants.UNIVERSAL_LANGUAGE))
      return "universal";
    return "";
  }

  public static String typeToImage(String basePath, String mimeType)
  {
    if (mimeType == null)
      return basePath + "altre.gif";

    // Web formats
    if (mimeType.equals("text/html"))
      return basePath + "html.gif";
    if (mimeType.equals("text/plain"))
      return basePath + "txt.gif";
    if (mimeType.equals("text/xml") || mimeType.equals("application/xml"))
      return basePath + "xml.gif";
    if (mimeType.equals("text/xhtml"))
      return basePath + "html.gif";
    if (mimeType.equals("text/css"))
      return basePath + "css.gif";
    if (mimeType.startsWith("text/"))
      return basePath + "txt.gif";
   
    // images
    if (mimeType.startsWith("image/"))
      return basePath + "png.gif";

    // Office formats
    if (mimeType.equals("application/msword"))
      return basePath + "doc.gif";
    if (mimeType.equals("application/x-msaccess"))
      return basePath + "mdb.gif";
    if (mimeType.equals("application/vnd.ms-excel"))
      return basePath + "xls.gif";
    if (mimeType.equals("application/vnd.ms-powerpoint"))
      return basePath + "ppt.gif";

    // other applications
    if (mimeType.equals("application/pdf"))
      return basePath + "pdf.gif";
    if (mimeType.equals("application/zip"))
      return basePath + "zip.gif";
    if (mimeType.equals("application/acad"))
      return basePath + "dwg.gif";
    if (mimeType.equals("application/x-javascript"))
      return basePath + "js.gif";

    return basePath + "altre.gif";
  }

  /**
   * creates a valid filename from document title
   * @param title
   * @return
   */
  public static String getFilename(String title)
  {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < title.length(); i++)
    {
      char ch = title.charAt(i);
      if (ch >= 'a' && ch <= 'z') buffer.append(ch);
      else if (ch >= 'A' && ch <= 'Z') buffer.append(ch);
      else if (ch >= '0' && ch <= '9') buffer.append(ch);
      else if (ch == ' ') buffer.append("_");
      else if (ch == ':') buffer.append("_");

      else if (ch == 'à') buffer.append("a");
      else if (ch == 'è') buffer.append("e");
      else if (ch == 'é') buffer.append("e");
      else if (ch == 'í') buffer.append("i");
      else if (ch == 'ï') buffer.append("i");
      else if (ch == 'ò') buffer.append("o");
      else if (ch == 'ó') buffer.append("o");
      else if (ch == 'ú') buffer.append("u");
      else if (ch == 'ü') buffer.append("u");

      else if (ch == 'À') buffer.append("A");
      else if (ch == 'È') buffer.append("E");
      else if (ch == 'É') buffer.append("E");
      else if (ch == 'Í') buffer.append("I");
      else if (ch == 'Ï') buffer.append("I");
      else if (ch == 'Ò') buffer.append("O");
      else if (ch == 'Ó') buffer.append("O");
      else if (ch == 'Ú') buffer.append("U");
      else if (ch == 'Ü') buffer.append("U");
    }
    return buffer.toString();
  }

  public static String getFilename(String title, String mimeType)
  {
    String extension =
      MimeTypeMap.getMimeTypeMap().getExtension(mimeType);

    return getFilename(title) + "." + extension;
  }

  public static String getLanguageFlag(String basePath, String language)
  {
    if (language != null && !DocumentConstants.UNIVERSAL_LANGUAGE.equals(language))
      return basePath + language + ".gif";
    else
      return null;
  }

  public static String printDocument(Document document)
    throws Exception
  {
    StringBuilder sb = new StringBuilder();
    Map<String,List<String>> map = DocumentUtils.getProperties(document);
    for (Map.Entry<String, List<String>> entry : map.entrySet())
    {
      String name = entry.getKey();
      List<String> values = entry.getValue();
      if (values != null && values.size() > 0 &&
          values.get(0) != null && values.get(0).length() > 0)
      {
        sb.append(name).append("=[");
        for (String value : values)
        {
          sb.append(value).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("];");
      }
    }
    sb.deleteCharAt(sb.lastIndexOf(";"));

    Content content = document.getContent();
    if (content != null)
    {
      sb.append("Content=[").append(content.getContentId()).append(";")
        .append(content.getContentType()).append(";").append(content.getSize())
        .append("]");
    }

    return sb.toString();
  }

  public static String printProperty(Property property)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(property.getName()).append("={");
    for (String value : property.getValue())
    {
      sb.append(value).append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("}");

    return sb.toString();
  }

  public static void main(String[] args)
  {
    Document document = new Document();
    try
    {
      Property p = new Property();
      p.setName("test");
      p.getValue().add("1");
      p.getValue().add("2");
      p.getValue().add("3");
      DocumentUtils.setProperty(document, p.getName(), p.getValue());
      System.out.println(DocumentUtils.getProperties(document));
    }
    catch (Exception e)
    {
    }
  }  
}
