package org.santfeliu.faces.browser.encoder;

import java.io.IOException;


import javax.faces.context.ResponseWriter;

import org.santfeliu.faces.Translator;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.util.net.HttpClient;

public class LinkEncoder extends ContentEncoder
{
  public LinkEncoder()
  {
  }
  
  public void encode(HtmlBrowser browser, HttpClient httpClient, 
    ResponseWriter writer, Translator translator, String translationGroup)
    throws IOException
  {
    String url = browser.getUrl();
    writer.startElement("a", browser);
    writer.writeAttribute("href", url, null);
    writer.writeAttribute("target", "_blank", null);
    writer.writeText(url, null);
    writer.endElement("a");
  }
}
