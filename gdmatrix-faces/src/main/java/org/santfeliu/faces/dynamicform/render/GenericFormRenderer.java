package org.santfeliu.faces.dynamicform.render;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 *
 * @author realor
 */
public class GenericFormRenderer extends FormRenderer
{
  @Override
  public void decode(FacesContext context, UIComponent component)
  {
  }

  @Override
  public void encodeBegin(FacesContext context, UIComponent component)
    throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    writer.startElement("h1", component);
    writer.writeText("GenericFormRenderer", null);
    writer.endElement("h1");
  }

  @Override
  public void encodeChildren(FacesContext context, UIComponent component)
    throws IOException
  {
  }

  @Override
  public void encodeEnd(FacesContext context, UIComponent component)
    throws IOException
  {
  }
}
