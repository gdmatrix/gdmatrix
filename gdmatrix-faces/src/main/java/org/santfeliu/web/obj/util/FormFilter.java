package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.List;
import org.santfeliu.dic.util.DictionaryUtils;

/**
 *
 * @author blanquepa
 */
public abstract class FormFilter implements Serializable
{
  private Backup backup;

  public FormFilter()
  {
  }

  //actions
  public void restore()
  {
    if (backup != null)
      copy(backup.getFormFilter(), this);
    backup = null;
  }

  public void backup()
  {
    backup = new Backup(this);
  }

  public void setProperty(String propName, List<String> propValues)
  {
    if (propName != null)
    {
      //set input properties
      DictionaryUtils.setProperty(this, propName, propValues);
      if (!DictionaryUtils.containsProperty(this, propName))
      {
        //set inner object filter
        DictionaryUtils.setProperty(getObjectFilter(), propName, propValues);
      }
    }
  }

  protected abstract Object getObjectFilter();

  public abstract void setMaxResults(int value);

  public abstract void setFirstResult(int value);

  protected abstract void copy(FormFilter src, FormFilter dst);

  protected abstract void clearAll();

  public abstract boolean isEmpty();

  private class Backup implements Serializable
  {
    private FormFilter formFilter;

    public Backup(FormFilter formFilter)
    {
      copy(formFilter, this.formFilter);
    }

    public FormFilter getFormFilter()
    {
      return formFilter;
    }
  }
}
