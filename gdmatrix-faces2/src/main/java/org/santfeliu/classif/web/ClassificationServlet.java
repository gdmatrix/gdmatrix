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
package org.santfeliu.classif.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.util.enc.HtmlEncoder;

/**
 *
 * @author realor
 */
public class ClassificationServlet extends HttpServlet
{
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    String classId = request.getParameter("classId");
    String dateTime = request.getParameter("dateTime");
    String uri = request.getRequestURI();
    if (uri.endsWith("/csv") || uri.endsWith("/xls"))
    {
      exportCsv(classId, dateTime, response);
    }
    else if (uri.endsWith("/html"))
    {
      exportHtml(classId, dateTime, response);
    }
    if (uri.endsWith("/xml"))
    {
      exportXml(classId, dateTime, response);
    }
  }

  private void exportCsv(String classId, String dateTime, 
    HttpServletResponse response) throws IOException
  {
    response.setContentType("text/plain");
    response.setHeader("Content-Disposition", "attachment; filename=\"qdc.csv\"");
    PrintWriter writer = response.getWriter();
    writer.println("classId;classPath;title;indexedPath");

    ClassCache classCache = ClassCache.getInstance(dateTime);
    org.santfeliu.classif.Class classObject = classCache.getClass(classId);
    if (classObject != null) exportCsv(classObject, writer);
  }

  private void exportCsv(org.santfeliu.classif.Class classObject,
    PrintWriter writer)
  {
    String classId = classObject.getClassId();
    String title = classObject.getTitle();
    String classPath = "";
    String indexedPath = "0";
    List<org.santfeliu.classif.Class> superClasses =
      classObject.getSuperClasses();
    superClasses.add(classObject);
    for (org.santfeliu.classif.Class superClass : superClasses)
    {
      classPath += "/" + superClass.getClassId();
      org.santfeliu.classif.Class superSuperClass = superClass.getSuperClass();
      if (superSuperClass != null)
      {
        List<String> subClassIds = superSuperClass.getSubClassIds();
        int index = subClassIds.indexOf(superClass.getClassId());
        indexedPath += "." + index;
      }
    }
    writer.print("\"" + classId + "\";");
    writer.print("\"" + classPath + "\";");
    writer.print("\"" + title + "\";");
    writer.println("\"" + indexedPath + "\"");
    if (!classObject.isLeaf())
    {
      List<org.santfeliu.classif.Class> subClasses =
        classObject.getSubClasses(false);
      for (org.santfeliu.classif.Class subClass : subClasses)
      {
        exportCsv(subClass, writer);
      }
    }
  }

  private void exportHtml(String classId, String dateTime,
    HttpServletResponse response) throws IOException
  {
    response.setContentType("text/html");
    PrintWriter writer = response.getWriter();
    writer.println("<HTML>");
    writer.println("<HEAD>");
    writer.println("<STYLE>");
    writer.println("UL {list-style-type: none}");
    writer.println("</STYLE>");
    writer.println("</HEAD>");
    writer.println("<BODY>");
    writer.println("<H3>Classification export</H3>");
    
    ClassCache classCache = ClassCache.getInstance(dateTime);
    org.santfeliu.classif.Class classObject = classCache.getClass(classId);
    if (classObject != null)
    {
      writer.print("<UL>");
      exportHtml(classObject, writer);
      writer.print("</UL>");
    }
    writer.println("</BODY>");
    writer.println("</HTML>");
  }

  private void exportHtml(org.santfeliu.classif.Class classObject,
    PrintWriter writer)
  {
    writer.print("<LI>");
    String title = classObject.getTitle();
    writer.println(classObject.getClassId() + ": " + HtmlEncoder.encode(title));
    if (!classObject.isLeaf())
    {
      writer.println("<UL>");
      List<org.santfeliu.classif.Class> subClasses =
        classObject.getSubClasses(false);
      for (org.santfeliu.classif.Class subClass : subClasses)
      {
        exportHtml(subClass, writer);
      }
      writer.println("</UL>");
    }
    writer.println("</LI>");
  }

  private void exportXml(String classId, String dateTime,
    HttpServletResponse response) throws IOException
  {
    response.setContentType("text/xml");
    response.setCharacterEncoding("UTF-8");
    PrintWriter writer = response.getWriter();
    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");

    ClassCache classCache = ClassCache.getInstance(dateTime);
    org.santfeliu.classif.Class classObject = classCache.getClass(classId);
    if (classObject != null) exportXml(classObject, writer);
  }

  private void exportXml(org.santfeliu.classif.Class classObject,
    PrintWriter writer)
  {
    writer.print("<class ");
    writer.print("id=\"" + classObject.getClassId() + "\" ");
    writer.print("title=\"" + classObject.getTitle() + "\"");
    writer.println(">");
    if (!classObject.isLeaf())
    {
      List<org.santfeliu.classif.Class> subClasses =
        classObject.getSubClasses(false);
      for (org.santfeliu.classif.Class subClass : subClasses)
      {
        exportXml(subClass, writer);
      }
    }
    writer.print("</class>");
  }
}
