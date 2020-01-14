package org.santfeliu.util.script.function;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class DateTimePickerForFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    String fieldName = (String)args[0];
    String language = UserSessionBean.getCurrentInstance().getViewLanguage();

    StringBuilder buffer = new StringBuilder();

    buffer.append("&lt;link rel=\"stylesheet\" href=\"/plugins/jquery/ui/1.11.4/themes/smoothness/jquery-ui.css\"&gt;");
    buffer.append("<script src=\"/plugins/jquery/jquery-1.10.2.js\"></script>");
    buffer.append("<script src=\"/plugins/jquery/ui/1.11.4/jquery-ui.js\"></script>");
    buffer.append("<script src=\"/plugins/jquery/datepicker/datepicker-ca.js\"></script>");
    buffer.append("<script src=\"/plugins/jquery/datepicker/datepicker-es.js\"></script>");
    buffer.append("<script src=\"/plugins/jquery/timepicker/jquery-ui-timepicker-addon.js\"></script>");    
    buffer.append("<script src=\"/plugins/jquery/timepicker/jquery-ui-timepicker-es.js\"></script>");
    buffer.append("<script src=\"/plugins/jquery/timepicker/jquery-ui-timepicker-ca.js\"></script>");
    buffer.append("&lt;link rel=\"stylesheet\" href=\"/plugins/jquery/timepicker/jquery-ui-timepicker-addon.css\"&gt;");    
    
    buffer.append("<script>");
    buffer.append("$(function() {");
    buffer.append("$( \"#").append(fieldName).append("\" ).datetimepicker( $.timepicker.regional[ \"").append(language).append("\" ] );");
    buffer.append("});");
    buffer.append("</script>    ");
//    buffer.append("<input type=\"text\" id=\"").append(fieldName).append("\" name=\"").append(fieldName).append("\" format=\"datetime:dd/MM/yyyy HH:mm\">");
    return buffer.toString();
  }  
}
