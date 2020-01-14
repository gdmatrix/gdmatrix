package org.santfeliu.faces.component.jquery;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 *
 * @author blanquepa
 */
public class JQueryRenderUtils
{
  public static final String JQUERY_VERSION = "1.10.2";
  public static final String JQUERY_ENCODED = "JQUERY_ENCODED";  
  
//  private UIComponent component; 
  
//  public JQueryRenderUtils(UIComponent component)
//  {
//    this.component = component;
//  }
  
  public static void encodeLibraries(FacesContext context, ResponseWriter writer, UIComponent component) throws IOException
  {
    ExternalContext externalContext = context.getExternalContext();
    String contextPath = externalContext.getRequestContextPath();
    Map requestMap = externalContext.getRequestMap();
    if (!isJQueryPresent(externalContext))
    {
      requestMap.put(JQUERY_ENCODED, "true");
      writer.startElement("script", component);
      writer.writeAttribute("src", contextPath +  "/plugins/jquery/jquery-" + JQUERY_VERSION + ".js", null);
      writer.endElement("script");
    }    
  }  
  
  public static boolean isJQueryPresent(ExternalContext externalContext)
  {
    Map requestMap = externalContext.getRequestMap();
    return (requestMap.get(JQUERY_ENCODED) != null);
  }
  
  public static boolean isJQueryPresent()
  {
    return isJQueryPresent(FacesContext.getCurrentInstance().getExternalContext());
  }
  
}
