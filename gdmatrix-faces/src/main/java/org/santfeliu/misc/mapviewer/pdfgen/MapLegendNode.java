package org.santfeliu.misc.mapviewer.pdfgen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.santfeliu.pdfgen.PdfGenerator;

/**
 *
 * @author realor
 */
public class MapLegendNode extends MapRectNode
{
  public MapLegendNode(String argument)
  {
    super(argument);
  }

  @Override
  protected void localPrimitivePaint(Graphics2D g2d, Rectangle2D bounds)
  {
    try
    {
      Map context = PdfGenerator.getCurrentInstance().getContext();
      MapContext.init(context);

      String text = "Legend ";
      Object value = context.get("text");
      if (value != null) text += value.toString();

      Color color = g2d.getColor();
      g2d.setColor(Color.BLACK);
      g2d.drawString(text, 10, 20);
      g2d.drawRect(0, 0, (int)bounds.getWidth(), (int)bounds.getHeight());
      g2d.setColor(color);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
