package org.santfeliu.form;

import java.io.Serializable;

/**
 *
 * @author realor
 */
public class FormDescriptor implements Serializable
{
  String selector;
  String title;

  public FormDescriptor()
  {
  }

  public FormDescriptor(String selector, String title)
  {
    this.selector = selector;
    this.title = title;
  }

  public String getSelector()
  {
    return selector;
  }

  public void setSelector(String selector)
  {
    this.selector = selector;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  @Override
  public String toString()
  {
    return "(\"" + selector + "\", \"" + title + "\")";
  }
}
