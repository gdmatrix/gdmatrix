package org.santfeliu.form.builder;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;

/**
 *
 * @author realor
 */
public class PathFormBuilder extends FileFormBuilder
{
  public static final String PREFIX = "path";

  public List<FormDescriptor> findForms(String selector)
  {
    List<FormDescriptor> formDescriptors;
    File file = getFile(selector);
    if (file == null)
    {
      // return empty list
      formDescriptors = Collections.EMPTY_LIST;
    }
    else if (file.isDirectory())
    {
      formDescriptors = new ArrayList();
      File files[] = file.listFiles();
      for (File cfile : files)
      {
        if (getFormClass(cfile.getName()) != null)
        {
          FormDescriptor descriptor = new FormDescriptor();
          descriptor.setSelector(PREFIX + ":" + cfile.getAbsolutePath());
          descriptor.setTitle(getTitle(cfile.getName()));
          formDescriptors.add(descriptor);
        }
      }
    }
    else if (file.isFile())
    {
      FormDescriptor descriptor = new FormDescriptor();
      descriptor.setSelector(PREFIX + ":" + file.getAbsolutePath());
      descriptor.setTitle(getTitle(file.getName()));
      formDescriptors = Collections.singletonList(descriptor);
    }
    else
    {
      formDescriptors = Collections.EMPTY_LIST;
    }
    return formDescriptors;
  }

  public Form getForm(String selector)
  {
    Form form = null;
    File file = getFile(selector);
    if (file != null && file.isFile())
    {
      Class formClass = getFormClass(file.getName());
      if (formClass != null)
      {
        try
        {
          form = (Form)formClass.newInstance();
          form.read(new FileInputStream(file));
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

  protected File getFile(String selector)
  {
    File file = null;
    if (selector.startsWith(PREFIX + ":"))
    {
      String path = selector.substring(PREFIX.length() + 1);
      file = new File(path);
      if (!file.exists()) file = null;
    }
    return file;
  }
}
