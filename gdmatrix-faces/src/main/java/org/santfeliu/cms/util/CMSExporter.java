package org.santfeliu.cms.util;

import java.io.PrintStream;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.CMSManagerService;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.PojoUtils;

/**
 *
 * @author realor
 */
public class CMSExporter
{
  private CMSManagerPort port;
  private boolean onlyPublic = true;
  private boolean onlyRendered = true;

  public void connect(String wsDirUrl) throws Exception
  {
    WSDirectory wsdir = WSDirectory.getInstance(new URL(wsDirUrl));
      WSEndpoint endpoint = wsdir.getEndpoint(CMSManagerService.class);
    port = endpoint.getPort(CMSManagerPort.class);
  }

  public void connect(String wsDirUrl, String userId, String password)
    throws Exception
  {
    WSDirectory wsdir = WSDirectory.getInstance(new URL(wsDirUrl));
      WSEndpoint endpoint = wsdir.getEndpoint(CMSManagerService.class);
    port = endpoint.getPort(CMSManagerPort.class, userId, password);
  }

  public boolean isOnlyPublic()
  {
    return onlyPublic;
  }

  public void setOnlyPublic(boolean onlyPublic)
  {
    this.onlyPublic = onlyPublic;
  }

  public boolean isOnlyRendered()
  {
    return onlyRendered;
  }

  public void setOnlyRendered(boolean onlyRendered)
  {
    this.onlyRendered = onlyRendered;
  }

  public void exportCSV(String filename,
    String workspaceId, String rootNodeId) throws Exception
  {
    if (port == null) throw new Exception("Not connected");
    PrintStream out = new PrintStream(filename);
    try
    {
      Node node = port.loadNode(workspaceId, rootNodeId);
      exportNodeCSV(node, out, 0);
    }
    finally
    {
      out.close();
    }
  }

  private void exportNodeCSV(Node node, PrintStream out, int level)
    throws Exception
  {
    System.out.println(node.getNodeId());
    node = port.loadNode(node.getWorkspaceId(), node.getNodeId());
    String label = getValue(node, "label");
    String rendered = getValue(node, "rendered");
    if (onlyRendered && "false".equals(rendered)) return;
    if (onlyPublic && !isPublic(node)) return;

    out.print(node.getNodeId());
    out.print(";");
    for (int i = 0; i < level; i++) out.print(";");
    out.print(label);
    out.println(";");

    NodeFilter filter = new NodeFilter();
    filter.getWorkspaceId().add(node.getWorkspaceId());
    filter.getParentNodeId().add(node.getNodeId());
    List<Node> nodes = port.findNodes(filter);
    Collections.sort(nodes, new NodeSorter());
    for (Node child : nodes)
    {
      exportNodeCSV(child, out, level + 1);
    }
  }

  public static void main(String[] args)
  {
    try
    {
      CMSExporter exporter = new CMSExporter();

      exporter.connect("http://xxxxx/wsdirectory");
      exporter.exportCSV("c:/out.csv", "1", "15482");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private String getValue(Node node, String property) throws Exception
  {
    String text = null;
    Object value = PojoUtils.getDynamicProperty(node.getProperty(), property);
    if (value instanceof List)
    {
      List list = (List)value;
      if (!list.isEmpty()) text = (String)list.get(0);
    }
    else if (value != null)
    {
      text = String.valueOf(value);
    }
    return text;
  }

  private boolean isPublic(Node node) throws Exception
  {
    Object value = PojoUtils.getDynamicProperty(node.getProperty(),
      "roles.select");
    if (value instanceof List)
    {
      List list = (List)value;
      if (!list.contains("EVERYONE") && !list.isEmpty()) return false;
    }
    return true;
  }

  class NodeSorter implements Comparator<Node>
  {
    public int compare(Node o1, Node o2)
    {
      return o1.getIndex() - o2.getIndex();
    }    
  }
}
