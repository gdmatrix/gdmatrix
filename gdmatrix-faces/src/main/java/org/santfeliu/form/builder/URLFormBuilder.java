package org.santfeliu.form.builder;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.type.html.HtmlForm;

/**
 *
 * @author realor
 */
public class URLFormBuilder extends FileFormBuilder
{
  public static final String PREFIX = "url";

  public List<FormDescriptor> findForms(String selector)
  {
    URL url = getURL(selector);
    if (url != null)
    {
      String filename = url.getFile();
      String title = getTitle(filename);
      FormDescriptor descriptor = new FormDescriptor();
      descriptor.setTitle(title);
      descriptor.setSelector(selector);
      return Collections.singletonList(descriptor);
    }
    return Collections.EMPTY_LIST;
  }

  public Form getForm(String selector)
  {
    Form form = null;
    URL url = getURL(selector);
    if (url != null)
    {
      Class formClass = getFormClass(url.getFile());
      if (formClass == null)
      {
        try
        {
          URLConnection conn = url.openConnection();
          String contentType = conn.getContentType();
          if (contentType.indexOf("html") != -1)
          {
            formClass = HtmlForm.class;
          }
        }
        catch (Exception ex)
        {
        }
      }
      if (formClass != null)
      {
        try
        {
          form = (Form)formClass.newInstance();
          form.read(url.openStream());
          setup(form);
        }
        catch (Exception ex)
        {
          throw new RuntimeException(ex);
        }
      }
    }
    return form;
  }

  protected URL getURL(String selector)
  {
    URL url = null;
    if (selector.startsWith(PREFIX + ":"))
    {
      String urlString = selector.substring(PREFIX.length() + 1);
      try
      {
        url = new URL(urlString);
      }
      catch (MalformedURLException ex)
      {
      }
    }
    return url;
  }
}
