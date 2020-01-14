package org.santfeliu.faces.menu.util;

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
