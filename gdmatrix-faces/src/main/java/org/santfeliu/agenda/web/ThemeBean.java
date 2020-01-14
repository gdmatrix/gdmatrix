package org.santfeliu.agenda.web;

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import static org.santfeliu.agenda.web.EventSearchBean.MAIN_AGENDA_MID;
import static org.santfeliu.agenda.web.EventSearchBean.SEARCH_EVENT_THEME;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectBean;

/**
 *
 * @author blanquepa
 */
public class ThemeBean extends ObjectBean
{
  public ThemeBean()
  {
  }

  @Override
  public String getObjectTypeId()
  {
    return "Theme";
  }

  @Override
  public String getDescription(String oid)
  {
    String description = "";
    try
    {
      Theme theme = AgendaConfigBean.getPort().loadThemeFromCache(oid);
      description = theme.getDescription();
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;
  }

  public List<SelectItem> getAllSelectItems(String selectedObjectId)
  {
    List<SelectItem> selectItems = new ArrayList();
    try
    {
      selectItems.add(new SelectItem(ControllerBean.NEW_OBJECT_ID, " "));
      List<Theme> themes =
        AgendaConfigBean.getPort().findThemesFromCache(new ThemeFilter());
      for (Theme theme : themes)
      {
        SelectItem item = new SelectItem(theme.getThemeId(), theme.getDescription());
        selectItems.add(item);
      }
      
      markPublicThemes(selectItems);
      FacesUtils.sortSelectItems(selectItems);
    }
    catch (Exception ex)
    {
    }
    
    return selectItems;
  }
  
  public void markPublicThemes(List<SelectItem> currentItems)
  {
    String mid = getProperty(MAIN_AGENDA_MID);
    if (mid != null)
    {
      try
      {
        MenuItemCursor cursor =
          UserSessionBean.getCurrentInstance().getMenuModel().getMenuItemByMid(mid);
        List<String> currentThemes =
          cursor.getMultiValuedProperty(SEARCH_EVENT_THEME);
        for (SelectItem item : currentItems)
        {
          String themeId = (String)item.getValue();
          if (!StringUtils.isBlank(themeId) && currentThemes.contains(themeId))
          {
            item.setLabel(item.getLabel() + " " + ((char)0x24CC) + " ");
          }
        }
      }
      catch (Exception ex)
      {
      }
    }    
  }  
}
