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
package org.santfeliu.misc.mapviewer.pdfgen;

import java.awt.RenderingHints;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.SVGTextElementBridge;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.TextNode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author unknown
 */
public class MapTextElementBridge extends SVGTextElementBridge
{
  @Override
  public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e)
  {
    TextNode node = null;

    String id = e.getAttribute("id");
    NodeIdParser parser = new NodeIdParser();
    parser.parse(id);
    String componentName = parser.getComponentName();
    String argument = parser.getArgument();
    System.out.println("cn:" + componentName);

    if (componentName.equals("MapLabel") || componentName.equals("MapText"))
    {
      node = (TextNode)new MapTextNode(argument);
    }
    else
    {
      node = (TextNode) super.createGraphicsNode(ctx, e);
    }
    if (node == null)
    {
      return null;
    }

    associateSVGContext(ctx, e, node);

    // traverse the children to add context on
    // <tspan>, <tref> and <textPath>
    Node child = getFirstChild(e);
    while (child != null)
    {
      if (child.getNodeType() == Node.ELEMENT_NODE)
      {
        addContextToChild(ctx, (Element) child);
      }
      child = getNextSibling(child);
    }

    // specify the text painter to use
    if (ctx.getTextPainter() != null)
    {
      node.setTextPainter(ctx.getTextPainter());
    }

    // 'text-rendering' and 'color-rendering'
    RenderingHints hints = null;
    hints = CSSUtilities.convertColorRendering(e, hints);
    hints = CSSUtilities.convertTextRendering(e, hints);
    if (hints != null)
    {
      node.setRenderingHints(hints);
    }

    node.setLocation(getLocation(ctx, e));

    return node;
  }
}
