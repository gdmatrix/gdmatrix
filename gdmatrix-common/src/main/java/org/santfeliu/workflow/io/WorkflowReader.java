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
package org.santfeliu.workflow.io;

import java.awt.geom.Point2D;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import org.santfeliu.workflow.Workflow;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.DocumentNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.workflow.node.SignatureNode;
import org.santfeliu.workflow.util.PointsFormat;

/**
 *
 * @author unknown
 */
public class WorkflowReader
{
  private static final String DEFAULT_NODE_PACKAGE = "org.santfeliu.workflow.node";
  private String nodesPackage;

  public WorkflowReader()
  {
    this(DEFAULT_NODE_PACKAGE);
  }

  public WorkflowReader(String nodesPackage)
  {
    this.nodesPackage = nodesPackage;
  }

  public Workflow read(InputStream is) throws Exception
  {
    Workflow workflow = new Workflow();

    LinkedList transitions = new LinkedList();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(is);

    Node workflowNode = findElement(document.getFirstChild());
    if (workflowNode == null || !"workflow".equals(workflowNode.getNodeName()))
      throw new Exception("Invalid workflow file");

    workflow.setName(getStringAttribute(workflowNode, "name"));
    workflow.setVersion(getStringAttribute(workflowNode, "version", "1.0"));
    workflow.setFormat(getIntAttribute(workflowNode, "format", 0));
    workflow.setUndoable(getBooleanAttribute(workflowNode, "undoable", true));
    String firstNodeId = getStringAttribute(workflowNode, "first-node");
    if (firstNodeId == null)
    {
      //legacy format
      firstNodeId = getStringAttribute(workflowNode, "firstnode");
      if (firstNodeId == null)
      {
        firstNodeId = "0";
      }
    }
    workflow.setFirstNodeId(firstNodeId);
    workflow.setGridSize(getIntAttribute(workflowNode, "grid-size", 8));
    workflow.setNodeWidth(getIntAttribute(workflowNode, "node-width", 100));
    workflow.setNodeHeight(getIntAttribute(workflowNode, "node-height", 50));
    workflow.setGridVisible(
      getBooleanAttribute(workflowNode, "grid-visible", true));
    workflow.setGridEnabled(
      getBooleanAttribute(workflowNode, "grid-enabled", true));

    Node childNode = workflowNode.getFirstChild();
    while (childNode != null)
    {
      if (childNode instanceof Element)
      {
        String nodeName = childNode.getNodeName();
        if (nodeName.equals("description"))
        {
          String description = childNode.getTextContent();
          description = description.replace('\n', ' ').trim();
          workflow.setDescription(description);
        }
        else if (nodeName.equals("nodes"))
        {
          Node node = childNode.getFirstChild();
          while (node != null)
          {
            if (node instanceof Element)
            {
              WorkflowNode wfNode = readNode(node, workflow, transitions);
              if (workflow.getFormat() < Workflow.DEFAULT_FORMAT)
              {
                convertLegacyNode(wfNode, workflow.getFormat());
              }
            }
            node = node.getNextSibling();
          }
        }
      }
      childNode = childNode.getNextSibling();
    }
    linkTransitions(workflow, transitions);

    workflow.setFormat(Workflow.DEFAULT_FORMAT);

    return workflow;
  }

  private WorkflowNode readNode(Node node,
    Workflow workflow, LinkedList transitions) throws Exception
  {
    // get node class
    String className = getStringAttribute(node, "class");

    if (className.lastIndexOf(".") > 0)
      className = nodesPackage + className.substring(className.lastIndexOf("."));
    else
      className = nodesPackage + "." + className;

    Class cls = Class.forName(className);
    org.santfeliu.workflow.WorkflowNode wfNode =
      (org.santfeliu.workflow.WorkflowNode)cls.getConstructor().newInstance();

    // set id
    String nodeId = getStringAttribute(node, "id");
    wfNode.setId(nodeId);

    // set immediate
    wfNode.setImmediate(getBooleanAttribute(node, "immediate", false));

    // set hidden
    wfNode.setHidden(getBooleanAttribute(node, "hidden", false));

    // set points
    String pstr = getStringAttribute(node, "points");
    if (pstr != null)
    {
      Point2D[] points = parsePoints(pstr);
      wfNode.setPoints(points);
    }
    Node childNode = node.getFirstChild();
    while (childNode != null)
    {
      if (childNode instanceof Element)
      {
        String nodeName = childNode.getNodeName();
        if (nodeName.equals("description"))
        {
          wfNode.setDescription(childNode.getTextContent());
        }
        else if (nodeName.equals("properties"))
        {
          Node propNode = childNode.getFirstChild();
          while (propNode != null)
          {
            if (propNode instanceof Element)
            {
              String str = propNode.getTextContent();
              String propertyClassName =
                getStringAttribute(propNode, "class", "java.lang.String");
              Class propertyClass;
              if (propertyClassName.equals("boolean"))
              {
                propertyClass = boolean.class;
              }
              else if (propertyClassName.equals("int"))
              {
                propertyClass = int.class;
              }
              else if (propertyClassName.equals("long"))
              {
                propertyClass = long.class;
              }
              else if (propertyClassName.equals("short"))
              {
                propertyClass = short.class;
              }
              else if (propertyClassName.equals("float"))
              {
                propertyClass = float.class;
              }
              else if (propertyClassName.equals("double"))
              {
                propertyClass = double.class;
              }
              else
              {
                propertyClass = Class.forName(propertyClassName);
              }
              ObjectDeserializer deserializer = new ObjectDeserializer();
              String propName = getStringAttribute(propNode, "name");
              String firstLetter = propName.substring(0, 1).toUpperCase();
              String methodName = "set" + firstLetter + propName.substring(1);
              try
              {
                Method method = cls.getMethod(methodName,
                  new Class[]{propertyClass});
                Object value = deserializer.deserialize(str, propertyClass);
                if (value != null)
                {
                  method.invoke(wfNode, new Object[]{value});
                }
              }
              catch (NoSuchMethodException ex)
              {
                // may be legacy format, try to adapt property
                convertLegacyMethod(wfNode, methodName, str);
              }
            }
            propNode = propNode.getNextSibling();
          }
        }
        else if (nodeName.equals("transitions"))
        {
          Node nextNode = childNode.getFirstChild();
          while (nextNode != null)
          {
            if (nextNode instanceof Element)
            {
              String nextNodeId = getStringAttribute(nextNode, "id");
              String sPoints = getStringAttribute(nextNode, "points");
              String outcome = getStringAttribute(nextNode, "outcome", "");
              Boolean isErrorOutcome = Boolean.valueOf(
                getStringAttribute(nextNode, "onerror", "false"));
              if (isErrorOutcome) outcome = WorkflowNode.ERROR_OUTCOME;

              String sOutcomePosition =
                getStringAttribute(nextNode, "outcome-position");

              Point2D[] points = null;
              if (sPoints != null)
              {
                points = parsePoints(sPoints);
              }
              Point2D outcomePosition = null;
              if (sOutcomePosition != null)
              {
                outcomePosition = parsePoints(sOutcomePosition)[0];
              }
              transitions.add(new Object[]
                {nodeId, nextNodeId, points, outcome, outcomePosition});
            }
            nextNode = nextNode.getNextSibling();
          }
        }
      }
      childNode = childNode.getNextSibling();
    }
    workflow.addNode(wfNode);
    return wfNode;
  }

  private void convertLegacyNode(WorkflowNode wfNode, int oldFormat)
  {
    if (oldFormat == 0)
    {
      if (wfNode.getRoles() == null) // default role
      {
        wfNode.setRoles("[${username}]");
      }
    }
    if (oldFormat <= 1)
    {
      if (wfNode instanceof SignatureNode)
      {
        SignatureNode sigNode = (SignatureNode)wfNode;
        String url = sigNode.getServiceURL();
        url = url.replaceAll("/axis/", "/");
        url = url.replaceAll("SignatureManager", "signature");
        sigNode.setServiceURL(url);
      }
      if (wfNode instanceof DocumentNode)
      {
        DocumentNode docNode = (DocumentNode)wfNode;
        String url = docNode.getServiceURL();
        url = url.replaceAll("/axis/", "/");
        url = url.replaceAll("DocumentManager", "doc");
        docNode.setServiceURL(url);
      }
    }
  }

  private void convertLegacyMethod(WorkflowNode wfNode,
    String methodName, String svalue)
  {
    ObjectDeserializer deserializer = new ObjectDeserializer();
    Class cls = wfNode.getClass();
    if (wfNode instanceof FormNode)
    {
      FormNode formNode = (FormNode)wfNode;
      try
      {
        Class propertyClass = org.santfeliu.util.Properties.class;
        Method method = cls.getMethod(methodName,
          new Class[]{propertyClass});
        Properties parameters;
        if ("custom".equals(formNode.getFormType()))
        {
          String type;
          String ref;
          String tokens[] = svalue.split(":");
          if (tokens[0].equals("http") || tokens[1].equals("https"))
          {
            type = "url";
            ref = svalue;
          }
          else
          {
            type = tokens[0];
            ref = tokens[1].trim();
          }
          parameters = new Properties();
          parameters.setProperty("type", type);
          parameters.setProperty("ref", ref);
        }
        else
        {
          parameters = (Properties)
            deserializer.deserialize(svalue, propertyClass);
        }
        if (parameters != null)
        {
          method.invoke(wfNode, new Object[]{parameters});
        }
      }
      catch (Exception ex2)
      {
        // discard property
      }
    }
    else if (wfNode instanceof SignatureNode || wfNode instanceof DocumentNode)
    {
      try
      {
        Class propertyClass = org.santfeliu.util.Properties.class;
        Method method = cls.getMethod(methodName,
          new Class[]{propertyClass});
        Properties parameters = (Properties)
          deserializer.deserialize(svalue, propertyClass);
        if (parameters != null)
        {
          method.invoke(wfNode, new Object[]{parameters});
        }
      }
      catch (Exception ex)
      {
        // discard property
      }
    }
  }

  private String getStringAttribute(Node node, String name)
  {
    return getStringAttribute(node, name, null);
  }

  private String getStringAttribute(Node node, String name, String defaultValue)
  {
    String value = defaultValue;
    if (node instanceof Element)
    {
      Element element = (Element)node;
      Attr attr = element.getAttributeNode(name);
      if (attr != null)
      {
        value = attr.getValue();
      }
    }
    return value;
  }

  private boolean getBooleanAttribute(Node node, String name)
  {
    return getBooleanAttribute(node, name, false);
  }

  private boolean getBooleanAttribute(Node node, String name,
    boolean defaultValue)
  {
    boolean value = defaultValue;
    if (node instanceof Element)
    {
      Element element = (Element)node;
      Attr attr = element.getAttributeNode(name);
      if (attr != null)
      {
        value = Boolean.valueOf(attr.getValue()).booleanValue();
      }
    }
    return value;
  }

  private int getIntAttribute(Node node, String name)
  {
    return getIntAttribute(node, name, 0);
  }

  private int getIntAttribute(Node node, String name, int defaultValue)
  {
    int value = defaultValue;
    if (node instanceof Element)
    {
      Element element = (Element)node;
      Attr attr = element.getAttributeNode(name);
      if (attr != null)
      {
        try
        {
          value = Integer.parseInt(attr.getValue());
        }
        catch (NumberFormatException ex)
        {
        }
      }
    }
    return value;
  }

  private Point2D[] parsePoints(String pstr)
  {
    PointsFormat format = new PointsFormat();
    return format.parsePoints(pstr);
  }

  private void linkTransitions(Workflow workflow, LinkedList transitions)
  {
    Iterator iter = transitions.iterator();
    while (iter.hasNext())
    {
      Object[] link = (Object[])iter.next();
      String nodeId = (String)link[0];
      String nextNodeId = (String)link[1];
      Point2D points[] = (Point2D[])link[2];
      String outcome = (String)link[3];
      Point2D outcomePosition = (Point2D)link[4];

      WorkflowNode node = workflow.getNode(nodeId);
      WorkflowNode nextNode = workflow.getNode(nextNodeId);
      node.addTransition(outcome, nextNode, outcomePosition, points);
    }
  }

  private Node findElement(Node node)
  {
    while (node != null && !(node instanceof Element))
    {
      node = node.getNextSibling();
    }
    return node;
  }
}
