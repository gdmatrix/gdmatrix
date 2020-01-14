package org.santfeliu.faces;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

public class HtmlRenderUtils
{
  public static final String HIDDEN_LINK_ID = "_idsflink";
  public static final String HIDDEN_LINK_RENDERED = "HIDDEN_LINK_RENDERED";
  public static final String OVERLAY_RENDERED = "OVERLAY_RENDERED";

  public static void renderOverlay(ResponseWriter writer) throws IOException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map requestMap = context.getExternalContext().getRequestMap();

    if (!requestMap.containsKey(OVERLAY_RENDERED))
    {
      writer.startElement("div", null);
      writer.writeAttribute("id", "_overlay_", null);
      writer.startElement("div", null);
      writer.writeAttribute("id", "_overlay_loading", null);
      writer.endElement("div");      
      writer.endElement("div");
      requestMap.put(OVERLAY_RENDERED, Boolean.TRUE);
    }
  }

  public static void renderHiddenLink(UIComponent component, 
    FacesContext context) throws IOException
  {
    Map requestMap = context.getExternalContext().getRequestMap();
    if (!requestMap.containsKey(HIDDEN_LINK_RENDERED))
    {
      String formId = FacesUtils.getParentFormId(component, context);
      if (formId != null)
      {
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("input", component);
        writer.writeAttribute("type", "hidden", null);
        writer.writeAttribute("name", formId + ":" + HIDDEN_LINK_ID, null);
        writer.endElement("input");
      }
      requestMap.put(HIDDEN_LINK_RENDERED, Boolean.TRUE);
    }
  }
}
