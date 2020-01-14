package org.santfeliu.agenda.web;

import java.util.List;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author blanquepa
 */
public class ThemeSearchBean extends BasicSearchBean
{
  private ThemeFilter filter;

  public ThemeSearchBean()
  {
    filter = new ThemeFilter();
  }

  //Getters & Setters
  public void setFilter(ThemeFilter filter)
  {
    this.filter = filter;
  }

  public ThemeFilter getFilter()
  {
    return filter;
  }

  //User actions
  public int countResults()
  {
    try
    {
      return AgendaConfigBean.getPort().countThemesFromCache(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      List results = AgendaConfigBean.getPort().findThemesFromCache(filter);

      return results;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String show()
  {
    return "theme_search";
  }

  public String showTheme()
  {
    return getControllerBean().showObject("Theme",
      (String)getValue("#{row.themeId}"));
  }

  public String selectTheme()
  {
    Theme row = (Theme)getExternalContext().getRequestMap().get("row");
    String themeId = row.getThemeId();
    return getControllerBean().select(themeId);
  }
}
