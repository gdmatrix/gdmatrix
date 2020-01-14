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

/**
 *
 * @author unknown
 */
public class MenuReader
{
/*
  private DocumentBuilderFactory factory;

  public MenuReader()
  {
    factory = DocumentBuilderFactory.newInstance();
  }

  public MenuModel read(InputStream is)
    throws Exception
  {
    MenuModel menuModel = new MenuModel();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document menuDoc = builder.parse(is);
    Node node = menuDoc.getFirstChild();
    while (node != null)
    {
      if (node.getNodeName().equals("menu"))
      {
        readMenu(node, menuModel);
      }
      node = node.getNextSibling();
    }
    return menuModel;
  }

  private void readMenu(Node node, MenuModel menuModel)
    throws Exception
  {
    Node child = node.getFirstChild();
    while (child != null)
    {
      if (child.getNodeName().equals("menu-item"))
      {
        readMenuItem(child, menuModel);
      }
      child = child.getNextSibling();
    }
  }

  private MenuItemCursor readMenuItem(Node node, MenuModel menuModel) 
    throws Exception
  {
    Node midNode = node.getAttributes().getNamedItem("mid");
    if (midNode == null) 
      throw new Exception("mid attribute is missing");
    String mid = midNode.getNodeValue();
    MenuItemCursor mi = null;
    if (mid.equals(MenuModel.ROOT))
    {
      // ROOT already exists in MenuModel
      mi = menuModel.getMenuItem(mid);
    }
    else
    {
      // create new MenuItem
      mi = menuModel.createMenuItem(mid);
    }

    Node child = node.getFirstChild();
    while (child != null)
    {
      if (child.getNodeName().equals("property"))
      {
        readProperty(child, mi);
      }
      else if (child.getNodeName().equals("menu-item"))
      {
        MenuItemCursor cmi = readMenuItem(child, menuModel);
        mi.addChild(cmi);
      }
      child = child.getNextSibling();
    }
    return mi;
  }

  private void readProperty(Node node, MenuItemCursor mi)
    throws Exception
  {
    String name = null;
    String value = null;
    Node child = node.getFirstChild();
    while (child != null)
    {
      if (child.getNodeName().equals("name"))
      {
        if (child.getFirstChild() != null)
        {
          name = child.getFirstChild().getNodeValue();
        }
      }
      else if (child.getNodeName().equals("value"))
      {
        if (child.getFirstChild() != null)
        {
          value = child.getFirstChild().getNodeValue();
        }
      }
      child = child.getNextSibling();
    }
    if (name != null)
    {
      mi.getProperties().put(name, value);
    }
  }

  public static void main(String args[])
  {
    try
    {
      MenuReader r = new MenuReader();
      MenuModel model = r.read(
        new FileInputStream("c:/tomcat/webapps/web/WEB-INF/menu.xml"));
        
      model.printToStream(System.out);
      
      MenuWriter w = new MenuWriter();
      w.write(model, new FileOutputStream("c:/test.xml"));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }  
*/
}
