package org.santfeliu.faces.render;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class HtmlOutputRichTextRenderer
  extends HtmlTextRenderer
{
  public HtmlOutputRichTextRenderer()
  {
  }

  @Override
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
        writeTransformedText(writer, text);
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

  protected static void writeTransformedText(ResponseWriter writer, String text)
    throws IOException
  {
    String oldValue = text;

    String regexp = "(http(s)?://([^\\s])*)|(http(s)?://([^\\s])*(\\p{Punct})+)|\n";
    Pattern pattern = Pattern.compile(regexp);
    Matcher matcher = pattern.matcher(text);

    int index = 0;
    while (matcher.find())
    {
      writer.writeText(oldValue.substring(index, matcher.start()), null);
      String match = oldValue.substring(matcher.start(), matcher.end());
      if (match.equals("\n")) //new line
        writer.write("<br>");
      else if (match.matches("http(s)?://([^\\s])*(\\p{Punct})+")) //ends with punctuation
      {
        int i = matcher.end() - 1;
        String punct = oldValue.substring(i, matcher.end());
        while (punct.matches("(\\p{Punct})+"))
        {
          i--;
          punct = oldValue.substring(i, matcher.end());
        }
        punct = oldValue.substring(i + 1, matcher.end());
        match = match.substring(0, match.lastIndexOf(punct));
        writer.write("<a href=\"" + match + "\" target=\"_blank\">" + match + "</a>" + punct);
      }
      else
        writer.write("<a href=\"" + match + "\" target=\"_blank\">" + match + "</a>");

      index = matcher.end();
    }
    writer.writeText(oldValue.substring(index), null);
  }

}
