
package org.santfeliu.form.builder;

import org.santfeliu.form.type.html.HtmlForm;

/**
 *
 * @author realor
 */
public abstract class FileFormBuilder extends AbstractFormBuilder
{
  public static final Object[][] formTypes = new Object[][]
  {
    {"htm", HtmlForm.class},
    {"html", HtmlForm.class},
    {"form", HtmlForm.class}
  };

  protected Class getFormClass(String filename)
  {
    Class formClass = null;
    int i = 0;
    while (i < formTypes.length && formClass == null)
    {
      if (filename.endsWith("." + formTypes[i][0]))
      {
        formClass = (Class)formTypes[i][1];
      }
      i++;
    }
    return formClass;
  }

  protected String getTitle(String filename)
  {
    int index = filename.lastIndexOf("/");
    if (index != -1)
    {
      filename = filename.substring(index + 1);
    }

    String title = filename;
    int index2 = title.lastIndexOf(".");
    if (index2 != -1)
    {
      title = title.substring(0, index2);
    }
    title = title.substring(0, 1).toUpperCase() + title.substring(1);
    return title;
  }
}
