package org.santfeliu.faces.render;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.renderkit.html.ext.HtmlTextRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;


public class HtmlOutputTextRenderer
  extends HtmlTextRenderer
{
  public HtmlOutputTextRenderer()
  {
  }

  public void encodeEnd(FacesContext facesContext, UIComponent component)
    throws IOException
  {
    RendererUtils.checkParamValidity(facesContext, component, null);

    if (component instanceof UIInput)
    {
      renderInput(facesContext, component);
    }
    else if (component instanceof UIOutput)
    {
      renderOutput(facesContext, component);
    }
    else
    {
      throw new IllegalArgumentException("Unsupported component class " + 
                                         component.getClass().getName());
    }
  }


  protected static void renderOutput(FacesContext facesContext, 
                                     UIComponent component)
    throws IOException
  {
    String text = RendererUtils.getStringValue(facesContext, component);
    boolean escape;
    if (component instanceof HtmlOutputText)
    {
      escape = ((HtmlOutputText) component).isEscape();
    }
    else
    {
      escape = 
          RendererUtils.getBooleanAttribute(component, JSFAttr.ESCAPE_ATTR, 
                                            true); //default is to escape
    }
    renderOutputText(facesContext, component, text, escape);
  }

  public static void renderOutputText(FacesContext facesContext, 
                                      UIComponent component, String text, 
                                      boolean escape)
    throws IOException
  {
    if (text != null)
    {
      ResponseWriter writer = facesContext.getResponseWriter();
      boolean span = false;

      if (component.getId() != null && 
          !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
      {
        span = true;

        writer.startElement(HTML.SPAN_ELEM, component);

        HtmlRendererUtils.writeIdIfNecessary(writer, component, 
                                             facesContext);

        HtmlRendererUtils.renderHTMLAttributes(writer, component, 
                                               HTML.COMMON_PASSTROUGH_ATTRIBUTES);

      }
      else
      {
        span = 
            HtmlRendererUtils.renderHTMLAttributesWithOptionalStartElement(
            writer, component, 
            HTML.SPAN_ELEM, HTML.COMMON_PASSTROUGH_ATTRIBUTES);
      }
      if (escape)
      {
        String[] values = text.split("\n");
        if (values.length > 0)
        {
          for (int i = 0; i < values.length - 1; i++)
          {
            writer.writeText(values[i], null);
            writer.write("<br>");
          }
          writer.writeText(values[values.length - 1], null);
        }
      }
      else
      {
        writer.write(text);
      }

      if (span)
      {
        writer.endElement(HTML.SPAN_ELEM);
      }
    }
  }
}
