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
package org.santfeliu.webapp.modules.geo.pdfgen;

import java.awt.RenderingHints;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.SVGRectElementBridge;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.w3c.dom.Element;

/**
 *
 * @author realor
 */
public class MapRectElementBridge extends SVGRectElementBridge
{

  public MapRectElementBridge()
  {
  }

  @Override
  public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e)
  {
    ShapeNode shapeNode;

    String id = e.getAttribute("id");
    NodeIdParser parser = new NodeIdParser();
    parser.parse(id);
    String componentName = parser.getComponentName();
    String argument = parser.getArgument();

    if (componentName.equals("MapView"))
    {
      shapeNode = new MapViewNode(argument);
    }
    else if (componentName.equals("MapLegend"))
    {
      shapeNode = new MapLegendNode(argument);
    }
    else if (componentName.equals("MapTextArea"))
    {
      shapeNode = new MapTextAreaNode(argument);
    }
    else
    {
      shapeNode = (ShapeNode)instantiateGraphicsNode();
    }

    // 'transform'
    setTransform(shapeNode, e, ctx);

    // 'visibility'
    shapeNode.setVisible(CSSUtilities.convertVisibility(e));

    associateSVGContext(ctx, e, shapeNode);

    // delegates to subclasses the shape construction
    buildShape(ctx, e, shapeNode);

    // 'shape-rendering' and 'color-rendering'
    RenderingHints hints = null;
    hints = CSSUtilities.convertColorRendering(e, hints);
    hints = CSSUtilities.convertShapeRendering(e, hints);
    if (hints != null)
      shapeNode.setRenderingHints(hints);

    System.out.println("shapeNode: " + shapeNode);

    return shapeNode;
  }

  @Override
  public Bridge getInstance()
  {
    return new MapRectElementBridge();
  }
}
