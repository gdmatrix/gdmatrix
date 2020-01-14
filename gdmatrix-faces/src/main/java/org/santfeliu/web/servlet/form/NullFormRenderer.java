package org.santfeliu.web.servlet.form;

import java.io.Writer;
import java.util.Map;
import org.santfeliu.form.Form;
import java.io.IOException;
import org.santfeliu.util.HTMLCharTranslator;

/**
 *
 * @author realor
 */
public class NullFormRenderer implements FormRenderer
{
  public static final String ENTITY_FIELD_NAME = "entity";
  public static final String FORMSEED_FIELD_NAME = "formseed";

  public void renderForm(Form form, Map data, Writer writer) throws IOException
  {
    // Form is null
    String entity = (String)data.get(ENTITY_FIELD_NAME);
    if (entity == null) entity = "ENTITY";
    writer.write("<div>");
    writer.write(entity + ":");
    writer.write("</div>");
    writer.write("<ul>");
    for (Object key : data.keySet())
    {
      String name = String.valueOf(key);
      if (!name.equals(ENTITY_FIELD_NAME) &&
          !name.equals(FORMSEED_FIELD_NAME))
      {
        writer.write("<li>");
        String value = String.valueOf(data.get(key));
        value = HTMLCharTranslator.toHTMLText(value);
        writer.write(name + ": " + value);
        writer.write("</li>");
      }
    }
    writer.write("</ul>");
  }
}
