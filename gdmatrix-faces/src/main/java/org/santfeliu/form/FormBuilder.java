package org.santfeliu.form;

import java.util.List;

/**
 *
 * @author realor
 */
public interface FormBuilder
{
  List<FormDescriptor> findForms(String selector);
  Form getForm(String selector);
}
