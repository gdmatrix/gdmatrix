package org.santfeliu.faces.component;

import javax.faces.webapp.UIComponentTag;

/**
 *
 * @author realor
 */
public class HtmlOverlayTag extends UIComponentTag
{
  @Override
  public String getComponentType()
  {
    return "Overlay";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }
}
