package org.santfeliu.agenda.web;

import org.matrix.agenda.Theme;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author blanquepa
 */
public class ThemeMainBean extends PageBean
{
  private Theme theme;
  private boolean modified;

  public ThemeMainBean()
  {
  }

  //Accessors
  public Theme getTheme()
  {
    return theme;
  }

  public void setTheme(Theme theme)
  {
    this.theme = theme;
  }

  @Override
  public boolean isModified()
  {
    return true;
  }

  public void setModified(boolean modified)
  {
    this.modified = modified;
  }

  //Actions
  public String show()
  {
    load();
    return "theme_main";
  }

  @Override
  public String store()
  {
    try
    {
      theme = AgendaConfigBean.getPort().storeTheme(theme);
      setObjectId(theme.getThemeId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  protected void load()
  {
    if (isNew())
    {
      theme = new Theme();
    }
    else
    {
      try
      {
        theme = AgendaConfigBean.getPort().loadThemeFromCache(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        theme = new Theme();
        error(ex);
      }
    }
  }
}
