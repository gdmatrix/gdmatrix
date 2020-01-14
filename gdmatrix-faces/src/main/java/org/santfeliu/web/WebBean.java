package org.santfeliu.web;

import java.util.Map;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.bean.CMSProperty;

public abstract class WebBean extends FacesBean
{
  @CMSProperty
  public static String FRAME_PROPERTY = "frame";
  @CMSProperty
  public static String TEMPLATE_PROPERTY = "template";
  @CMSProperty
  public static String LANGUAGE_PROPERTY = "language";
  @CMSProperty
  public static String RENDERED_PROPERTY = "rendered";
  @CMSProperty
  public static String ROLES_SELECT_PROPERTY = "roles.select";
  @CMSProperty
  public static String ROLES_UPDATE_PROPERTY = "roles.update";
  @CMSProperty
  public static String ROLES_ACCESS_PROPERTY = "roles.access";
  @CMSProperty
  public static String LABEL_PROPERTY = "label";
  @CMSProperty
  public static String ACTION_PROPERTY = "action";
  @CMSProperty
  public static String THEME_PROPERTY = "theme";
  @CMSProperty
  public static String NODECSS_PROPERTY = "nodeCSS";
  @CMSProperty
  public static String TRANSLATION_ENABLED_PROPERTY = "translationEnabled";
  @CMSProperty
  public static String INHERIT_NODE_CSS_PATH_PROPERTY = "inheritNodeCSSPath";
  @CMSProperty
  public static String TARGET_PROPERTY = "target";
  @CMSProperty
  public static String ENABLED_PROPERTY = "enabled";
  @CMSProperty
  public static String CERTIFICATE_REQUIRED_PROPERTY = "certificateRequired";

  protected String getProperty(String name)
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(name);
  }

  protected Map getProperties()
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    return menuItem.getProperties();
  }

  protected MenuItemCursor getSelectedMenuItem()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
  }
}
