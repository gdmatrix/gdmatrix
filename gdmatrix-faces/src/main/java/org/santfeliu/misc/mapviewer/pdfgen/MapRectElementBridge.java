package org.santfeliu.misc.mapviewer.pdfgen;

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
