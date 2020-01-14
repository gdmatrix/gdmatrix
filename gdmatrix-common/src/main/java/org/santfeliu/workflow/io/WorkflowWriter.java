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
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.util.PointsFormat;
import org.santfeliu.workflow.Workflow;

/**
 *
 * @author unknown
 */
public class WorkflowWriter 
{
  static final String CHARSET = "UTF-8";

  public WorkflowWriter()
  {
  }
  
  public void write(Workflow workflow, OutputStream os)
    throws Exception
  {
    PrintWriter out = new PrintWriter(new OutputStreamWriter(os, CHARSET));
    try
    {
      writeWorkflow(out, workflow);
    }
    finally
    {
      out.close();
    }
  }
  
  private void writeWorkflow(PrintWriter out, Workflow workflow)
    throws Exception
  {
    out.println("<?xml version=\"1.0\" encoding=\"" + CHARSET +"\" ?>");
    out.println("<workflow name=\"" + workflow.getName() + 
      "\" version=\"" + workflow.getVersion() +
      "\" format=\"" + workflow.getFormat() +
      "\" undoable=\"" + workflow.isUndoable() +
      "\" first-node=\"" + workflow.getFirstNodeId() +
      "\" grid-size=\"" + workflow.getGridSize() +
      "\" grid-visible=\"" + workflow.isGridVisible() +
      "\" grid-enabled=\"" + workflow.isGridEnabled() +
      "\" node-width=\"" + workflow.getNodeWidth() +
      "\" node-height=\"" + workflow.getNodeHeight() + "\">");
    out.println("<description>");
    out.println(workflow.getDescription());
    out.println("</description>");

    // write nodes
    out.println("<nodes>");
    WorkflowNode[] nodes = workflow.getNodes();
    for (int i = 0; i < nodes.length; i++)
    {
      WorkflowNode node = nodes[i];
      writeNode(out, node);
    }
    out.println("</nodes>");

    out.print("</workflow>");
  }

  private void writeNode(PrintWriter out, WorkflowNode node)
    throws Exception
  {
    out.print("<node id=\"" + node.getId() +
    "\" class=\"" + node.getClass().getName() + 
    "\" immediate=\"" + node.isImmediate() +
    "\" hidden=\"" + node.isHidden() + "\"");

    if (node.getPoints() != null)
    {
      PointsFormat pf = new PointsFormat();
      out.print(" points=\"" + pf.format(node.getPoints()) + "\"");
    }
    out.println(">");
    if (node.getDescription() != null)
    {
      out.print("<description><![CDATA[");
      out.print(node.getDescription());
      out.println("]]></description>");
    }
    out.println("<properties>");
    BeanInfo bi = Introspector.getBeanInfo(node.getClass());
    PropertyDescriptor[] pds = bi.getPropertyDescriptors();
    for (int i = 0; i < pds.length; i++)
    {
      writeNodeProperty(out, node, pds[i]);
    }
    out.println("</properties>");
    
    out.println("<transitions>");
    WorkflowNode.Transition transitions[] = node.getTransitions();
    for (int t = 0; t < transitions.length; t++)
    {
      writeNodeTransition(out, transitions[t]);
    }
    out.println("</transitions>");
    
    out.println("</node>");
  }
  
  private void writeNodeProperty(PrintWriter out, 
    WorkflowNode node, PropertyDescriptor pd)
    throws Exception
  {
    String name = pd.getName();
    if (pd.getWriteMethod() != null && pd.getReadMethod() != null)
    {
      if (!name.equals("id") && 
          !name.equals("description") && 
          !name.equals("immediate") &&
          !name.equals("points"))
      {
        Method method = pd.getReadMethod();
        Object value = method.invoke(node, new Object[0]);
        Class valueClass = pd.getPropertyType();
        String valueClassName = valueClass.getName();

        out.print("<property name=\"" + name + 
          "\" class=\"" + valueClassName + "\">");
        ObjectSerializer serializer = new ObjectSerializer();
        out.print(serializer.serialize(value, valueClass));
        out.println("</property>");
      }
    }
  }
  
  private void writeNodeTransition(PrintWriter out, 
    WorkflowNode.Transition transition)
  {
    out.print("<next-node id=\"" + transition.getNextNodeId() + 
      "\" outcome=\"" + transition.getOutcome() + "\"");
    
    PointsFormat pf = new PointsFormat();
    Point2D outcomePosition = transition.getOutcomePosition();
    if (outcomePosition != null)
    {
      out.print(" outcome-position=\"" + pf.format(
        new Point2D[]{outcomePosition}) + "\"");
    }
    Point2D points[] = transition.getPoints();
    if (points != null)
    {
      out.print(" points=\"" + pf.format(points) + "\"");
    }
    out.println("/>");
  }
}

