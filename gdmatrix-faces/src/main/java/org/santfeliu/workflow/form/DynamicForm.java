package org.santfeliu.workflow.form;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.santfeliu.form.Field;

/**
 *
 * @author realor
 */
public class DynamicForm extends Form
{
  @Override
  public Set getReadVariables()
  {
    Set variables;
    org.santfeliu.form.Form form = getForm();
    if (form != null)
    {
      Collection<Field> fields = getForm().getFields();
      variables = new HashSet();
      for (Field field: fields)
      {
        if (field.isReadOnly())
        {
          variables.add(field.getReference());
        }
      }
    }
    else variables = Collections.EMPTY_SET;
    return variables;
  }

  @Override
  public Set getWriteVariables()
  {
    Set variables;
    org.santfeliu.form.Form form = getForm();
    if (form != null)
    {
      Collection<Field> fields = getForm().getFields();
      variables = new HashSet();
      for (Field field: fields)
      {
        if (!field.isReadOnly())
        {
          variables.add(field.getReference());
        }
      }
    }
    else variables = Collections.EMPTY_SET;
    return variables;
  }

  private org.santfeliu.form.Form getForm()
  {
    String selector = (String)parameters.get("selector");
    if (selector != null)
    {
      try
      {
        return org.santfeliu.form.FormFactory.getInstance().
          getForm(selector, null);
      }
      catch (Exception ex)
      {
      }
    }
    return null;
  }
}
