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
package org.santfeliu.util.system.windows;

/**
 *
 * @author unknown
 */
public class WindowsUtils
{
  public static final String OPEN = "open";
  public static final String OPEN_NEW = "opennew";
  public static final String EDIT = "Edit";
  public static final String PRINT = "Print";
  public static final String PRINT_TO = "printto";

  public WindowsUtils()
  {
  }
  
  public static String getMimeTypeExtension(String mimeType)
  {
    try
    {
      return (String)Registry.getKeyAttribute(
        "HKEY_CLASSES_ROOT\\MIME\\Database\\Content Type\\" + mimeType, 
        "Extension");
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public static String getMimeTypeName(String mimeType)
  {
    try
    {
      String extension = getMimeTypeExtension(mimeType);
      return (String)Registry.getKeyAttribute(
        "HKEY_CLASSES_ROOT\\" + extension, "");
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public static String getMimeTypeApplication(String mimeType, String action)
  {
    try
    {
      String name = getMimeTypeName(mimeType);
      return (String)Registry.getKeyAttribute(
        "HKEY_CLASSES_ROOT\\" + name + "\\shell\\" + action + "\\Command", "");
    }
    catch (Exception ex)
    {
      return null;
    }
  }
  
  public static void main(String args[])
  {
    System.out.println(
      WindowsUtils.getMimeTypeApplication("application/pdf", OPEN));
    System.out.println(
      WindowsUtils.getMimeTypeApplication("application/pdf", EDIT));
    System.out.println(
      WindowsUtils.getMimeTypeApplication("application/msword", OPEN));
    System.out.println(
      WindowsUtils.getMimeTypeApplication("application/msword", EDIT));
    System.out.println(
      WindowsUtils.getMimeTypeApplication("text/html", OPEN));
    System.out.println(
      WindowsUtils.getMimeTypeApplication("text/html", EDIT));
    System.out.println(
      WindowsUtils.getMimeTypeApplication("text/plain", OPEN));
    System.out.println(
      WindowsUtils.getMimeTypeApplication("text/plain", EDIT));
    System.out.println(
      WindowsUtils.getMimeTypeApplication("text/xml", OPEN));
    System.out.println(
      WindowsUtils.getMimeTypeApplication("text/xml", EDIT));
  }
}
