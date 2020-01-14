package org.santfeliu.misc.mapviewer.pdfgen;

import com.itextpdf.awt.PdfGraphics2D;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.gvt.ShapeNode;

/**
 *
 * @author realor
 */
public class MapRectNode extends ShapeNode
{
  private String argument;

  public MapRectNode()
  {
  }

  public MapRectNode(String argument)
  {
    this.argument = argument;
  }

  public String getArgument()
  {
    return argument;
  }

  public void setArgument(String argument)
  {
    this.argument = argument;
  }

  @Override
  public void primitivePaint(Graphics2D g2d)
  {
    super.primitivePaint(g2d);
    localPaint(g2d);
  }

  protected final void localPaint(Graphics2D g2d)
  {
    Rectangle2D bounds = getBounds();
    PdfGraphics2D pdf = (PdfGraphics2D)g2d;
    g2d.translate(bounds.getX(), bounds.getY());
    localPrimitivePaint(pdf, bounds);
    g2d.translate(-bounds.getX(), -bounds.getY());
  }
  
  protected void localPrimitivePaint(Graphics2D g2d, Rectangle2D bounds)
  {
  }
}
