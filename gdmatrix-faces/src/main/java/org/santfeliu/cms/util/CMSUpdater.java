package org.santfeliu.cms.util;

import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.CMSManagerService;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.cms.Property;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class CMSUpdater
{
  CMSManagerPort cmsPort;
  
  public void connect(String wsDirUrl) throws Exception
  {
    WSDirectory wsdir = WSDirectory.getInstance(new URL(wsDirUrl));
      WSEndpoint endpoint = wsdir.getEndpoint(CMSManagerService.class);
    cmsPort = endpoint.getPort(CMSManagerPort.class, "admin", "*****");
  }

  public void processNode(String workspaceId, String nodeId)
  {
    Node node = cmsPort.loadNode(workspaceId, nodeId);
    processNode(node);
  }
  
  public void processNode(Node node)
  {
    String label = null;
    String detail = null;
    String url = null;
    boolean store = false;
    Property labelProp = null;

    for (Property property : node.getProperty())
    {
      String name = property.getName();
      String value = property.getValue().get(0);

      if (name.equals("externalTitle"))
      {
        int index = value.indexOf("</h2>");
        if (index != -1)
        {
          detail = value.substring(index + 5);
          label = TextUtils.removeTags(value.substring(0, index));
        }
        else detail = value;
      }
      if (name.equals("label"))
      {
        if (label == null) label = value;
        labelProp = property;
      }
      if (name.equals("style"))
      {
        property.setName("iconStyle");
      }
      if (name.equals("externalTitle"))
      {
        property.setName("----externalTitle");
      }
      if (name.equals("url"))
      {
        url = value;
      }
      if (name.equals("contentBuilder"))
      {
        store = "BannerWidgetBuilder".equals(value);
        property.getValue().set(0, "ButtonWidgetBuilder");
      }
    }
    if (label != null && !label.contains("-----") && store)
    {
      System.out.println("label: " + label);        
      System.out.println("detail: " + detail);
      System.out.println("url: " + url);
      System.out.println("------------");

      if (labelProp != null)
      {
        labelProp.getValue().set(0, label);
      }
      
      if (detail != null && detail.trim().length() > 0)
      {
        Property property = new Property();
        property.setName("detail");
        property.getValue().add(detail);
        node.getProperty().add(property);
      }
      Property property = new Property();
      property.setName("styleClass");
      property.getValue().add("button_widget");
      node.getProperty().add(property);
      
      cmsPort.storeNode(node);
      try
      {
        Thread.sleep(1000);
      }
      catch (Exception ex)
      {        
      }
    }

    NodeFilter filter = new NodeFilter();
    filter.getWorkspaceId().add(node.getWorkspaceId());
    filter.getParentNodeId().add(node.getNodeId());
    List<Node> children = cmsPort.findNodes(filter);
    Collections.sort(children, new NodeSorter());
    for (Node child : children)
    {
      processNode(child);
    }
  }
  
  public static void main(String[] args)
  {
    try
    {
      CMSUpdater updater = new CMSUpdater();
      updater.connect("http://www.santfeliu.cat/wsdirectory");
      updater.processNode("27", "24625");
//      updater.processNode("27", "25598");
    }
    catch (Exception ex)
    {      
      ex.printStackTrace();
    }
  }  

  class NodeSorter implements Comparator<Node>
  {
    public int compare(Node o1, Node o2)
    {
      return o1.getIndex() - o2.getIndex();
    }
  }
}
