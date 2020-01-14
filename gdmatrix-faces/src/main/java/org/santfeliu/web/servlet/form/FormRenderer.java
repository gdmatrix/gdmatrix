package org.santfeliu.web.servlet.form;

import java.io.Writer;
import java.util.Map;
import org.santfeliu.form.Form;

/**
 *
 * @author realor
 */
public interface FormRenderer
{
  public void renderForm(Form form, Map data, Writer writer)
    throws Exception;
}
