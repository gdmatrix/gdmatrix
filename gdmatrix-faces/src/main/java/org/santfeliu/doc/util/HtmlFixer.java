package org.santfeliu.doc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.santfeliu.util.script.ScriptClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

/**
 *
 * @author realor
 */
public class HtmlFixer
{
  private final String scriptName;
  private final String userId;
  private final String password;
    
  public HtmlFixer(String scriptName, String userId, String password)
  {
    this.scriptName = scriptName;
    this.userId = userId;
    this.password = password;
  }
  
  public String fixCode(String s)
  {
    try
    {
      if (s != null && !s.trim().isEmpty())
      {
        ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());      
        ByteArrayOutputStream out = new ByteArrayOutputStream();      
        fixCode(in, out);
        return out.toString();
      }
    }
    catch (Exception ex) 
    { 
      //Return the same string
    }
    return s;
  }
  
  public void fixCode(InputStream in, OutputStream out) throws Exception
  {
    Tidy tidy = new Tidy();
    tidy.setOnlyErrors(true);
    tidy.setTidyMark(false);
    org.w3c.dom.Document documentDOM = tidy.parseDOM(in, null);
    if (scriptName != null)
    {
      fixNode(documentDOM);
    }
    tidy.pprint(documentDOM, out);
  }
  
  public void fixNode(Node node) throws Exception
  {
    applyChanges(node);
    Node child = node.getFirstChild();
    while (child != null)
    {
      fixNode(child);
      child = child.getNextSibling();
    }
  }
  
  private void applyChanges(Node node) throws Exception
  {
    if (node instanceof Element)
    {
      Element element = (Element)node;
      ScriptClient scriptClient = new ScriptClient(userId, password);
      scriptClient.put("element", element);
      scriptClient.executeScript(scriptName);
    }
  }
  
  public static void main(String[] args)
  {
    try
    {
      HtmlFixer fixer = new HtmlFixer("html_fixer", "admin", "****");

      FileInputStream in = new FileInputStream(new File("c:/Users/realor/Desktop/test.html"));
      FileOutputStream out = new FileOutputStream(new File("c:/Users/realor/Desktop/test_out.html"));
      Tidy tidy = new Tidy();
      tidy.setOnlyErrors(true);
      tidy.setTidyMark(false); 
      org.w3c.dom.Document documentDOM = tidy.parseDOM(in, null);
      fixer.fixNode(documentDOM);
      tidy.pprint(documentDOM, out);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}