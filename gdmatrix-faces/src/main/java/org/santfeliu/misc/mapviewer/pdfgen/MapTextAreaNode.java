package org.santfeliu.misc.mapviewer.pdfgen;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.santfeliu.pdfgen.PdfGenerator;

/**
 *
 * @author realor
 */
public class MapTextAreaNode extends MapRectNode
{
  public MapTextAreaNode(String argument)
  {
    super(argument);
  }

  public String getProperty()
  {
    return getArgument();
  }
  
  @Override
  public void primitivePaint(Graphics2D g2d)
  {
    try
    {
      Map context = PdfGenerator.getCurrentInstance().getContext();
      MapContext.init(context);

      String textValue = null;
      Object value = MapContext.getProperty(context, getProperty());
      if (value != null) textValue = value.toString();

      if (textValue != null)
      {
        g2d.setColor(Color.BLACK);
        Rectangle2D boxBounds = this.getBounds();
        int x = (int)boxBounds.getX();
        int y = (int)boxBounds.getY();
        int fontHeight = (int)boxBounds.getHeight();
        Font font = new Font("Arial", Font.PLAIN, fontHeight);
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics(font);
        Rectangle2D stringBounds = fontMetrics.getStringBounds(textValue, g2d);
        int offsetX = 
          x + (int)((boxBounds.getWidth() - stringBounds.getWidth()) / 2);
        int offsetY = y + fontHeight;
        
        //g2d.drawRect(x, y, w, h);
        g2d.drawString(textValue, offsetX, offsetY);
      }
    }
    catch (Exception ex)
    {
    }
  }
}
