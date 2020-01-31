package org.santfeliu.misc.mapviewer.pdfgen;

import java.awt.RenderingHints;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.SVGTextElementBridge;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.bridge.TextNode;

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
