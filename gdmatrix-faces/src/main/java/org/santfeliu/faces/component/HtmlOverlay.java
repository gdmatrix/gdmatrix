package org.santfeliu.faces.component;

import java.io.IOException;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import org.santfeliu.faces.HtmlRenderUtils;

/**
 *
 * @author realor
 */
public class HtmlOverlay extends UIComponentBase
{
  @Override
  public String getFamily()
  {
    return "Overlay";
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    HtmlRenderUtils.renderOverlay(context.getResponseWriter());
  }
}
