package org.santfeliu.faces.dynamicform.render;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.santfeliu.faces.dynamicform.DynamicForm;
import org.santfeliu.form.Field;
import org.santfeliu.form.View;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.form.type.html.HtmlView;

/**
 *
 * @author realor
 */
public class DisabledHtmlFormRenderer extends HtmlFormRenderer
{
  @Override
  public void decode(FacesContext context, UIComponent component)
  {
  }

  @Override
  protected void encodeInput(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String type = view.getProperty("type");
    if ("submit".equals(type) || "button".equals(type)) return;

    writer.startElement("input", component);
    encodeStyleAttributes(view, writer);
    writer.writeAttribute("type", type, null);
    writer.writeAttribute("readonly", "true", null);

    String value = getValueAsString(component, view);
    if (value == null)
    {
      String checked = view.getProperty("checked");
      if (checked != null && !checked.equalsIgnoreCase("false"))
      {
        writer.writeAttribute("checked", "true", null);
      }
    }
    else
    {
      writer.writeAttribute("value", value, null);
      if ("checkbox".equals(type)) // boolean field value
      {
        if ("true".equals(value.toString()))
          writer.writeAttribute("checked", "true", null);
      }
      else if ("radio".equals(type)) // text field value
      {
        String formValue = view.getProperty("value");
        if (formValue != null && formValue.equals(value))
          writer.writeAttribute("checked", "true", null);
      }
      renderViewAttributes(view, writer,
        "name", "checked", "value", "readonly");
    }
    writer.endElement("input");
  }

  @Override
  protected void encodeSelect(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String[] stringValues = getValueAsStringArray(component, view);
    if (stringValues != null)
    {
      writer.startElement("div", component);
      renderViewAttributes(view, writer, "name", "style", "size");
      Field field = form.getField(view.getReference());
      if (field != null)
      {
        int size;
        String sizeString = view.getProperty("size");
        if (sizeString == null)
        {
          size = field.getMaxOccurs() == 1 ? 1 : 6;
        }
        else
        {
          try
          {
            size = Integer.parseInt(sizeString);
          }
          catch (NumberFormatException ex)
          {
            size = 6;
          }
        }
        int height = size * 16;

        String style = view.getProperty("style");
        if (style == null) style = "overflow:auto;height:" + height + "px";
        else style += ";overflow:auto;height:" + height + "px";
        style += ";border-width:1px";
        writer.writeAttribute("style", style, null);

        List<View> items = view.getChildren();
        Iterator<View> iter = items.iterator();
        while (iter.hasNext())
        {
          View item = iter.next();
          if (item.getViewType().equals(View.ITEM))
          {
            String itemValue = String.valueOf(item.getProperty("value"));

            String itemText = null;
            if (!item.getChildren().isEmpty())
            {
              View itemLabel = item.getChildren().get(0);
              itemText = String.valueOf(itemLabel.getProperty("text"));
            }
            if (itemText == null) itemText = itemValue;

            boolean selected = false;
            int i = 0;
            while (!selected && i < stringValues.length)
            {
              selected = itemValue.equals(stringValues[i++]);
            }
            if (selected)
            {
              writer.startElement("div", component);
              writer.writeText(itemText, null);
              writer.endElement("div");
            }
          }
        }
        writer.endElement("div");
      }
    }
  }

  @Override
  protected void encodeTextarea(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    writer.startElement("span", component);

    String style = view.getProperty("style");
    if (style != null) writer.writeAttribute("style", style + ";overflow:auto;", null);
    else writer.writeAttribute("style", "overflow:auto;", null);

    String styleClass = view.getProperty("class");
    if (styleClass != null) writer.writeAttribute("class", styleClass, null);

    String text = getValueAsString(component, view);
    if (text != null)
    {
      writer.writeText(text, null);
    }
    writer.endElement("span");
  }

  private void encodeStyleAttributes(HtmlView view, ResponseWriter writer)
    throws IOException
  {
    String style = view.getProperty("style");
    if (style != null) writer.writeAttribute("style", style, null);

    String styleClass = view.getProperty("class");
    if (styleClass != null) writer.writeAttribute("class", styleClass, null);
  }
}
