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
package org.santfeliu.security.swing;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author realor
 */
public class RolesGraphIO
{
  private Map<String, Role> roleMap = new HashMap();
  private Map<String, List<String>> linkMap = new HashMap();

  public void read(InputStream is, RolesGraph graph) throws Exception
  {
    roleMap.clear();
    linkMap.clear();
    graph.removeRoles();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(is);
    Node graphNode = document.getFirstChild();
    while (graphNode != null && !(graphNode instanceof Element))
      graphNode = graphNode.getNextSibling();

    Element graphNodeElement = (Element)graphNode;
    String xcenter = graphNodeElement.getAttribute("x");
    String ycenter = graphNodeElement.getAttribute("y");
    String zoom = graphNodeElement.getAttribute("zoom");
    if (xcenter != null && ycenter != null &&
      xcenter.trim().length() > 0 && ycenter.trim().length() > 0)
    {
      Point2D center = new Point2D.Double(
        Double.parseDouble(xcenter), Double.parseDouble(ycenter));
      graph.setCenter(center);
    }
    if (zoom != null && zoom.trim().length() > 0)
    {
      graph.setZoom(Double.parseDouble(zoom));
    }
    Node roleNode = graphNode.getFirstChild();
    while (roleNode != null)
    {
      if (roleNode instanceof Element)
      {
        Role role = readRole((Element)roleNode);
        graph.addRole(role);
        roleMap.put(role.getRoleId(), role);
      }
      roleNode = roleNode.getNextSibling();
    }
    for (Role role : graph.getRoles())
    {
      String roleId = role.getRoleId();
      List<String> inRoleIds = linkMap.get(roleId);
      if (inRoleIds != null)
      {
        for (String inRoleId : inRoleIds)
        {
          Role inRole = roleMap.get(inRoleId);
          if (inRole != null)
          {
            role.getInRoles().add(inRole);
          }
        }
      }
    }
  }

  public void write(OutputStream os, RolesGraph graph) throws IOException
  {
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
    try
    {
      writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
      writer.println("<roles-graph version=\"1.0\"");
      Point2D center = graph.getCenter();
      writer.println(" x=\"" + center.getX() + "\"");
      writer.println(" y=\"" + center.getY() + "\"");
      writer.println(" zoom=\"" + graph.getZoom() + "\">");

      Collection<Role> roles = graph.getRoles();
      for (Role role : roles)
      {
        writer.println("  <role roleId=\"" + role.getRoleId() + "\">");
        if (role.getName() != null)
        {
          writer.println("    <name>" +
            toXML(role.getName()) + "</name>");
        }
        if (role.getComments() != null)
        {
          writer.println("    <comments>" +
            toXML(role.getComments()) + "</comments>");
        }
        Point2D pos = role.getPosition();
        writer.println("    <position x=\"" + pos.getX() +
          "\" y=\"" + pos.getY() + "\"/>");
        for (Role inRole : role.getInRoles())
        {
          writer.println("    <in-role roleId=\"" + inRole.getRoleId() + "\" />");
        }
        writer.println("  </role>");
      }
      writer.println("</roles-graph>");
    }
    finally
    {
      writer.close();
    }
  }

  private Role readRole(Element roleElem) throws Exception
  {
    Role role = new Role();
    String roleId = roleElem.getAttribute("roleId");
    role.setRoleId(roleId);
    Node child = roleElem.getFirstChild();
    while (child != null)
    {
      if (child instanceof Element)
      {
        Element elem = (Element)child;
        String name = elem.getNodeName();
        if (name.equals("name") || name.equals("description"))
        {
          role.setName(elem.getTextContent());
        }
        else if (name.equals("comments"))
        {
          role.setComments(elem.getTextContent());
        }
        else if (name.equals("position"))
        {
          Point2D position = new Point2D.Double();
          position.setLocation(Double.parseDouble(elem.getAttribute("x")),
           Double.parseDouble(elem.getAttribute("y")));
          role.setPosition(position);
        }
        else if (name.equals("in-role"))
        {
          String inRoleId = elem.getAttribute("roleId");
          link(roleId, inRoleId);
        }
      }
      child = child.getNextSibling();
    }
    return role;
  }

  private void link(String roleId, String inRoleId)
  {
    List list = linkMap.get(roleId);
    if (list == null)
    {
      list = new ArrayList();
      linkMap.put(roleId, list);
    }
    list.add(inRoleId);
  }

  private String toXML(String text)
  {
    text = text.replaceAll("&", "&amp;");
    text = text.replaceAll("<", "&lt;");
    text = text.replaceAll(">", "&gt;");    
    return text;
  }
}
