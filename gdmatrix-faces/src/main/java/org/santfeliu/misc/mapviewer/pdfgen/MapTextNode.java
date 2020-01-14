package org.santfeliu.misc.mapviewer.pdfgen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.gvt.TextNode;
import org.santfeliu.pdfgen.PdfGenerator;

/**
 *
 * @author realor
 */
public class MapTextNode extends TextNode
{
  private String property;

  public MapTextNode(String property)
  {
    this.property = property;
  }

  @Override
  public void primitivePaint(Graphics2D g2d)
  {
    try
    {
      Map context = PdfGenerator.getCurrentInstance().getContext();
      MapContext.init(context);

      String textValue = null;
      Object value = MapContext.getProperty(context, property);
      if (value != null) textValue = value.toString();

      if (textValue != null)
      {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        Rectangle2D bounds = this.getBounds();
        int x = (int)bounds.getMinX();
        int y = (int)bounds.getMinY();
        int w = (int)bounds.getWidth();
        int h = (int)bounds.getHeight();
        Font font = new Font("Arial", Font.PLAIN, h);
        g2d.setFont(font);
        //g2d.drawRect(x, y, w, h);
        g2d.drawString(textValue, x, y + h);
      }
    }
    catch (Exception ex)
    {
    }
  }
}
