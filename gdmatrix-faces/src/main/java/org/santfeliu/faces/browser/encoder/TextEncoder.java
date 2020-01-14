package org.santfeliu.faces.browser.encoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;


import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.santfeliu.faces.Translator;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.util.net.HttpClient;

public class TextEncoder extends ContentEncoder
{
  public TextEncoder()
  {
  }

  public void encode(HtmlBrowser browser, HttpClient httpClient,
    ResponseWriter writer, Translator translator, String translationGroup)
    throws IOException
  {
    InputStream is = httpClient.getContentInputStream();
    if (is == null) return;
    try
    {
      BufferedReader reader;
      String contentEncoding = httpClient.getContentEncoding();
      if (contentEncoding == null)
      {
        reader = new BufferedReader(new InputStreamReader(is));
      }
      else
      {
        reader = new BufferedReader(new InputStreamReader(is, contentEncoding));
      }
      String line = reader.readLine();
      while (line != null)
      {
        if (translator != null)
        {
          StringWriter sw = new StringWriter();
          translator.translate(new StringReader(line), sw,
            "text/plain", getLanguage(), translationGroup);
          line = sw.toString();
        }
        writer.writeText(line, null);

        writer.startElement("br", browser);
        writer.endElement("br");
        line = reader.readLine();
      }
    }
    finally
    {
      is.close();
    }
  }

  private String getLanguage()
  {
    Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
    return locale.getLanguage();
  }
}
