package org.santfeliu.web.obj.util;

import java.util.Map;

/**
 * This parameters processor manage the parameters in a CKEditor call to custom 
 * file manager call according to its File Browser API. 
 *
 * @author blanquepa
 */
public class CKEditorParametersProcessor extends ParametersProcessor
{
  public static final String EDITOR_INSTANCE_PARAM = "CKEditor";
  public static final String LANGUAGE_PARAM = "langCode";
  public static final String CALLBACK_PARAM = "CKEditorFuncNum";  
  
  private String editorInstance;
  private String callbackReference;
  private String language;

  @Override
  public String processParameters(Map parameters)
  {
    this.editorInstance = (String) parameters.get(EDITOR_INSTANCE_PARAM);
    this.language = (String) parameters.get(LANGUAGE_PARAM);
    this.callbackReference = (String) parameters.get(CALLBACK_PARAM);
    
    return null;
  }

  public String getEditorInstance()
  {
    return editorInstance;
  }

  public String getCallbackReference()
  {
    return callbackReference;
  }

  public String getLanguage()
  {
    return language;
  }
 
}
