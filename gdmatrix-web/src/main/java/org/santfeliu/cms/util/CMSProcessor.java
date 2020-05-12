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
package org.santfeliu.cms.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.matrix.cases.Case;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cms.CMSManagerPort;
import org.matrix.cms.CMSManagerService;
import org.matrix.cms.Node;
import org.matrix.cms.NodeFilter;
import org.matrix.dic.Property;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.security.AccessControl;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.util.PojoUtils;

/**
 *
 * @author realor
 */
public class CMSProcessor
{
  private CMSManagerPort cmsPort;
  private DocumentManagerClient docClient;
  private CaseManagerPort casesPort;

  private boolean onlyPublic = true;
  private boolean onlyRendered = true;
  private int procs = 0;

  public void connect(String wsDirUrl) throws Exception
  {
    WSDirectory wsdir = WSDirectory.getInstance(new URL(wsDirUrl));
      WSEndpoint endpoint = wsdir.getEndpoint(CMSManagerService.class);
    cmsPort = endpoint.getPort(CMSManagerPort.class);
  }

  public void connect(String wsDirUrl, String userId, String password)
    throws Exception
  {
    WSDirectory wsdir = WSDirectory.getInstance(new URL(wsDirUrl));

    WSEndpoint endpoint = wsdir.getEndpoint(CMSManagerService.class);
    cmsPort = endpoint.getPort(CMSManagerPort.class, userId, password);

    endpoint = wsdir.getEndpoint("cases_sf");
    casesPort = endpoint.getPort(CaseManagerPort.class, userId, password);

    docClient = new DocumentManagerClient(new URL(wsDirUrl), userId, password);
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

  public void process(String workspaceId, String rootNodeId) throws Exception
  {
    procs = 0;
    if (cmsPort == null) throw new Exception("Not connected");
    Node node = cmsPort.loadNode(workspaceId, rootNodeId);
    processNode(node, Collections.EMPTY_LIST);
    System.out.println("Procediments vinculats: " + procs);
  }

  private void processNode(Node node, List parentRoles)
    throws Exception
  {
    node = cmsPort.loadNode(node.getWorkspaceId(), node.getNodeId());
    List<String> roles = getRoles(node);
    if (roles.isEmpty()) roles = parentRoles;
    if (roles.isEmpty())
    {
      roles = new ArrayList<String>();
      roles.add("EVERYONE");
    }

    String rendered = getValue(node, "rendered");
    if (onlyRendered && "false".equals(rendered)) return;
    if (onlyPublic && !isPublic(node)) return;
    String workflow = getValue(node, "workflow");

    if (workflow != null)
    {
      System.out.println("\n> workflow: " + workflow + " " + roles);
      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId("sf:WORKFLOW");
      Property property = new Property();
      property.setName("workflow.xml");
      property.getValue().add(workflow);
      filter.getProperty().add(property);
      filter.setMaxResults(1);
      List<Document> list = docClient.findDocuments(filter);
      if (!list.isEmpty())
      {
        Document doc = list.get(0);
        System.out.println(" docid: " + doc.getDocId() + " " + doc.getTitle());
        doc = docClient.loadDocument(doc.getDocId(), 0, ContentInfo.METADATA);
        doc.getAccessControl().clear();
        for (String role : roles)
        {
          if (!"WEBMASTER".equals(role) && !"CHONI".equals(role))
          {
            AccessControl ac = new AccessControl();
            ac.setRoleId(role);
            ac.setAction("Read");
            doc.getAccessControl().add(ac);
          }
        }
        AccessControl ac = new AccessControl();
        ac.setAction("Write");
        ac.setRoleId("WF_ADMIN");
        doc.getAccessControl().add(ac);
        for (AccessControl a : doc.getAccessControl())
        {
          System.out.println("    >" + a.getAction() + " " + a.getRoleId());
        }
        doc.setIncremental(true);
        docClient.storeDocument(doc);

        CaseDocumentFilter cfilter = new CaseDocumentFilter();
        cfilter.setDocId(doc.getDocId());
        List<CaseDocumentView> cdList = casesPort.findCaseDocumentViews(cfilter);
        if (!cdList.isEmpty())
        {
          for (CaseDocumentView cd  : cdList)
          {
            Case _case = cd.getCaseObject();
            String caseId = _case.getCaseId();
            String caseTitle = _case.getTitle();
            String caseTypeId = _case.getCaseTypeId();
            if ("sf:ProcedimentCase".equals(caseTypeId))
            {
              System.out.println(">>>>>>>> " + caseId + ": " + caseTitle + " " + caseTypeId);
              String pcaseId = caseId.substring(3);
              _case = casesPort.loadCase(pcaseId);
              List<AccessControl> acl = _case.getAccessControl();
              addReadRoles(acl, roles);
              for (AccessControl a : acl)
              {
                System.out.println("         >" + a.getAction() + " " + a.getRoleId());
              }
              casesPort.storeCase(_case);
              procs++;
            }
          }
        }
      }
    }

    // process children
    NodeFilter filter = new NodeFilter();
    filter.getWorkspaceId().add(node.getWorkspaceId());
    filter.getParentNodeId().add(node.getNodeId());
    List<Node> nodes = cmsPort.findNodes(filter);
    Collections.sort(nodes, new NodeSorter());
    for (Node child : nodes)
    {
      processNode(child, roles);
    }
  }

  public static void main(String[] args)
  {
    try
    {
      CMSProcessor processor = new CMSProcessor();

      processor.connect("http://xxxxxx/wsdirectory",
        "realor", "*****");
      processor.setOnlyPublic(false);
      processor.process("1", "759");
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

  private List getRoles(Node node) throws Exception
  {
    Object value = PojoUtils.getDynamicProperty(node.getProperty(),
      "roles.select");
    if (value instanceof List)
    {
      return (List)value;
    }
    return Collections.EMPTY_LIST;
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

  private void addReadRoles(List<AccessControl> acl, List<String> roles)
  {
    for (String role : roles)
    {
      addReadRole(acl, role);
    }
  }

  private void addReadRole(List<AccessControl> acl, String role)
  {
    boolean found = false;
    boolean isEveryone = false;
    Iterator<AccessControl> iter = acl.iterator();
    while (iter.hasNext() && !found)
    {
      AccessControl ac = iter.next();
      if ("Read".equals(ac.getAction()))
      {
        if (ac.getRoleId().equals(role)) found = true;
        if (ac.getRoleId().equals("EVERYONE")) isEveryone = true;
      }
    }
    if (!found && !isEveryone)
    {
      AccessControl newAc = new AccessControl();
      newAc.setAction("Read");
      newAc.setRoleId(role);
      acl.add(newAc);
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
