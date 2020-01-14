package org.santfeliu.faces.browser.encoder;

import java.io.IOException;

import javax.faces.context.ResponseWriter;

import org.santfeliu.faces.Translator;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.util.net.HttpClient;


public abstract class ContentEncoder
{
  public abstract void encode(HtmlBrowser browser, HttpClient httpClient, 
    ResponseWriter writer, Translator translator, String translationGroup)
    throws IOException;
}
