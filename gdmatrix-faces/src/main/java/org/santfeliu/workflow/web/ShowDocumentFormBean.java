package org.santfeliu.workflow.web;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;


public class ShowDocumentFormBean extends FormBean implements Serializable
{
  transient private HtmlBrowser browser;
  private String message;
  private boolean IFrame;
  private boolean showPrintButton;

  public ShowDocumentFormBean()
  {
  }

  public void setBrowser(HtmlBrowser browser)
  {
    this.browser = browser;
  }

  public HtmlBrowser getBrowser()
  {
    return browser;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public boolean isIFrame()
  {
    return IFrame;
  }

  public void setIFrame(boolean IFrame)
  {
    this.IFrame = IFrame;
  }

  public boolean isShowPrintButton()
  {
    return showPrintButton;
  }

  public void setShowPrintButton(boolean showPrintButton)
  {
    this.showPrintButton = showPrintButton;
  }

  public String show(Form form)
  {
    Properties parameters = form.getParameters();
    
    message = "";
    browser = new HtmlBrowser();
    browser.setUrl("about:blank");
    
    Object value;
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);
    value = parameters.get("url");
    if (value != null) browser.setUrl(String.valueOf(value));
    value = parameters.get("iframe");
    if (value != null) IFrame = Boolean.parseBoolean(String.valueOf(value));
    value = parameters.get("showPrintButton");
    if (value != null) showPrintButton = Boolean.parseBoolean(String.valueOf(value));
    else showPrintButton = true;

    return "show_document_form";
  }
  
  public Map submit()
  {
    HashMap variables = new HashMap();
    return variables;
  }

}
