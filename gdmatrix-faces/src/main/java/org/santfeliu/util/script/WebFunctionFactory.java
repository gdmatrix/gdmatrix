package org.santfeliu.util.script;

import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.script.function.CloneDocumentFunction;
import org.santfeliu.util.script.function.DateTimePickerForFunction;
import org.santfeliu.util.script.function.GetBeanFunction;
import org.santfeliu.util.script.function.HtmlEditorFunction;
import org.santfeliu.util.script.function.ObjectActionsManagerFunction;
import org.santfeliu.util.script.function.WebMessageFunction;

/**
 *
 * @author blanquepa
 */
public class WebFunctionFactory
{
  private static DateTimePickerForFunction dateTimePickerForFunction = new DateTimePickerForFunction();
  private static CloneDocumentFunction cloneDocumentFunction = new CloneDocumentFunction();
  private static ObjectActionsManagerFunction objectActionsManagerFunction = new ObjectActionsManagerFunction();
  private static WebMessageFunction webMessageFunction = new WebMessageFunction();
  private static HtmlEditorFunction htmlEditorFunction = new HtmlEditorFunction();
  private static GetBeanFunction getBeanFunction = new GetBeanFunction();
  
  public static void initFunctions(Scriptable scriptable)
  {
    // init built-in functions
    FunctionFactory.initFunctions(scriptable);
    scriptable.put("dateTimePickerFor", scriptable, dateTimePickerForFunction);
    scriptable.put("cloneDocument", scriptable, cloneDocumentFunction);
    scriptable.put("ObjectActionsManager", scriptable, objectActionsManagerFunction);
    scriptable.put("webMessage", scriptable, webMessageFunction);
    scriptable.put("htmlEditor", scriptable, htmlEditorFunction);    
    scriptable.put("getBean", scriptable, getBeanFunction);    
  }
}
