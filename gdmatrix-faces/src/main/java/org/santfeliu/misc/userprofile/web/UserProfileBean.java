package org.santfeliu.misc.userprofile.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;

@CMSManagedBean
public class UserProfileBean extends WebBean implements Serializable
{

  private String defaultLanguage;
  private String defaultTheme;
  private String recentPagesSize;
  private List<SelectItem> defaultLanguageList;
  private List<SelectItem> defaultThemeList;

  public String getDefaultLanguage()
  {
    if (defaultLanguage == null)
    {
      try
      {
        defaultLanguage = getUserPreferences().getDefaultLanguage();
      }
      catch (Exception ex) //unspecified
      {        
        defaultLanguage = "";
      }
    }
    return defaultLanguage;
  }

  public void setDefaultLanguage(String defaultLanguage)
  {
    this.defaultLanguage = defaultLanguage;
  }

  public String getDefaultTheme()
  {
    if (defaultTheme == null)
    {
      try
      {
        defaultTheme = getUserPreferences().getDefaultTheme();
      }
      catch (Exception ex) //unspecified
      {        
        defaultTheme = "";
      }
    }
    return defaultTheme;
  }

  public void setDefaultTheme(String defaultTheme)
  {
    this.defaultTheme = defaultTheme;
  }

  public String getRecentPagesSize()
  {
    if (recentPagesSize == null)
    {
      try
      {
        recentPagesSize = getUserPreferences().getRecentPagesSize();
      }
      catch (Exception ex) //unspecified
      {
        recentPagesSize = "";
      }
    }
    return recentPagesSize;
  }

  public void setRecentPagesSize(String recentPagesSize)
  {
    this.recentPagesSize = recentPagesSize;
  }

  public List<SelectItem> getDefaultLanguageList()
  {
    if (defaultLanguageList == null)
    {
      defaultLanguageList = new ArrayList<SelectItem>();      
      defaultLanguageList.add(new SelectItem("", getUnspecifiedLabel()));
      List<String> elems = UserSessionBean.getCurrentInstance().getMenuModel().
        getSelectedMenuItem().getMultiValuedProperty(
          UserPreferences.DEFAULT_LANGUAGE_PROPERTY);
      for (String sElem : elems)
      {        
        if (sElem != null && sElem.length() > 0)
        {
          Locale locale = new Locale(sElem);
          String displayLanguage =
            locale.getDisplayLanguage(locale).toLowerCase();
          defaultLanguageList.add(new SelectItem(sElem, displayLanguage));
        }
      }
    }
    return defaultLanguageList;
  }

  public void setDefaultLanguageList(List<SelectItem> defaultLanguageList)
  {
    this.defaultLanguageList = defaultLanguageList;
  }

  public List<SelectItem> getDefaultThemeList()
  {
    if (defaultThemeList == null)
    {
      defaultThemeList = new ArrayList<SelectItem>();      
      defaultThemeList.add(new SelectItem("", getUnspecifiedLabel()));
      List<String> elems = UserSessionBean.getCurrentInstance().getMenuModel().
        getSelectedMenuItem().getMultiValuedProperty(
          UserPreferences.DEFAULT_THEME_PROPERTY);
      for (String sElem : elems)
      {        
        if (sElem != null && sElem.length() > 0)
        {
          defaultThemeList.add(new SelectItem(sElem));
        }
      }
    }
    return defaultThemeList;
  }

  public void setDefaultThemeList(List<SelectItem> defaultThemeList)
  {
    this.defaultThemeList = defaultThemeList;
  }

  public String store()
  {
    try
    {
      changePreferences();
      info("PROFILE_UPDATED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @CMSAction
  public String show()
  {
    return "user_profile";
  }

  private void changePreferences()
  {
    //Languages
    getUserPreferences().removePreference(
      UserPreferences.DEFAULT_LANGUAGE_PROPERTY);
    if (!getDefaultLanguage().equals(""))
    {
      getUserPreferences().storePreference(
        UserPreferences.DEFAULT_LANGUAGE_PROPERTY, getDefaultLanguage());
    }
    //Themes
    getUserPreferences().removePreference(
      UserPreferences.DEFAULT_THEME_PROPERTY);
    if (!getDefaultTheme().equals(""))
    {
      getUserPreferences().storePreference(
        UserPreferences.DEFAULT_THEME_PROPERTY, getDefaultTheme());
    }

    //Recent pages size
    getUserPreferences().removePreference(
      UserPreferences.RECENT_PAGES_SIZE_PROPERTY);
    if (!getRecentPagesSize().equals(""))
    {
      getUserPreferences().storePreference(
        UserPreferences.RECENT_PAGES_SIZE_PROPERTY, getRecentPagesSize());
    }
    
  }

  private UserPreferences getUserPreferences()
  {
    return UserSessionBean.getCurrentInstance().getUserPreferences();
  }

  private String getUnspecifiedLabel()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.misc.userprofile.web.resources.UserProfileBundle", getLocale());
    return bundle.getString("unspecified");
  }

}
