package org.santfeliu.faces.menu.util;

import java.io.IOException;
import java.io.OutputStream;

import java.io.PrintWriter;

import java.util.Iterator;
import java.util.Map;

import java.util.Set;

import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;

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
