package org.santfeliu.util.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import org.santfeliu.util.script.WebScriptableBase;

public class WebTemplate extends Template
{
  public WebTemplate(Reader reader) throws IOException
  {
    super(reader);
  }

  public WebTemplate(String s)
  {
    super(s);
  }
  
  public static WebTemplate create(String s)
  {
    return new WebTemplate(s);
  }
  
  public static WebTemplate create(Reader reader) throws IOException
  {
    return new WebTemplate(reader);
  }

  public static WebTemplate create(File file) throws IOException
  {
    return new WebTemplate(new InputStreamReader(new FileInputStream(file)));
  }  
  
  @Override
  protected Scriptable createScriptable(Context cx, Map variables)
  {
    Scriptable scriptable = new WebScriptableBase(cx, variables);
    return scriptable;
  }  
  
}
