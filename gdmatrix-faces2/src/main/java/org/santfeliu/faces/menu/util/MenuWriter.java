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
package org.santfeliu.faces.menu.util;

import java.io.IOException;
import java.io.OutputStream;

import java.io.PrintWriter;

import java.util.Iterator;
import java.util.Map;

import java.util.Set;

import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;

/**
 *
 * @author unknown
 */
public class MenuWriter
{
  public static String INDENT = "  ";

  public MenuWriter()
  {
  }

  public void write(MenuModel menuModel, OutputStream os)
    throws IOException
  {
    PrintWriter out = new PrintWriter(os);
    out.println("<?xml version = '1.0' encoding = 'windows-1252'?>");
    out.println("<menu>");
    MenuItemCursor root = menuModel.getRootMenuItem();
    writeMenuItem(root, out, INDENT);
    out.println("</menu>");
    out.flush();
  }

  private void writeMenuItem(MenuItemCursor c, PrintWriter out, String indent)
    throws IOException
  {
    out.println(indent + "<menu-item mid=\"" + c.getMid() + "\">");
    Set set = c.getProperties().entrySet();
    Iterator iter = set.iterator();
    while (iter.hasNext())
    {
      out.println(indent + INDENT + "<property>");
      Map.Entry entry = (Map.Entry)iter.next();
      String name = (String)entry.getKey();
      out.print(indent + INDENT + INDENT + "<name>");
      out.print(name);
      out.println("</name>");
      String value = (String)entry.getValue();
      if (value != null)
      {
        out.print(indent + INDENT + INDENT + "<value>");
        out.print(encode(value));
        out.println("</value>");
      }
      out.println(indent + INDENT + "</property>");      
    }
    MenuItemCursor child = c.getFirstChild();
    while (!child.isNull())
    {
      writeMenuItem(child, out, indent + INDENT);
      child.moveNext();
    }
    out.println(indent + "</menu-item>");
  }
  
  private String encode(String value)
  {
    return value.replaceAll("&", "&amp;");
  }
}
