package org.santfeliu.web.servlet.form;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import org.santfeliu.form.View;

/**
 *
 * @author realor
 */
public class ReadOnlyFormRenderer extends EditableFormRenderer
{
  @Override
  protected void writeAttributes(View view, Writer writer,
    String ...excluded) throws IOException
  {
    Collection<String> propertyNames = view.getPropertyNames();
    for (String propertyName : propertyNames)
    {
      boolean isExcluded = propertyName.toLowerCase().equals("disabled") ||
        specialAttributes.contains(propertyName.toLowerCase());
      int i = 0;
      while (!isExcluded && i < excluded.length)
      {
        isExcluded = propertyName.equalsIgnoreCase(excluded[i]);
        i++;
      }
      if (!isExcluded)
      {
        String value = String.valueOf(view.getProperty(propertyName));
        writer.write(" " + propertyName + "=\"" + value + "\"");
      }
    }
    String tag = view.getNativeViewType().toLowerCase();
    if (tag.equals("input") || tag.equals("select") || tag.equals("textarea"))
    {
      writer.write(" disabled");
    }
  }
}
