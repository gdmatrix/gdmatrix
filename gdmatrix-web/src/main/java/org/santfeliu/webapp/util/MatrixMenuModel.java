package org.santfeliu.webapp.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;
import static org.santfeliu.webapp.util.WebUtils.TOPWEB_PROPERTY;

/**
 *
 * @author blanquepa
 */
public class MatrixMenuModel extends DefaultMenuModel implements Savable
{
  public static final String GOMID_JS_FUNCTION = "goMid";

  private String currentMid;

  public MatrixMenuModel(org.santfeliu.faces.menu.model.MenuModel mModel)
  {
    super();
    init(mModel);
  }

  public final void init(org.santfeliu.faces.menu.model.MenuModel mModel)
  {
    if (mModel == null)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      if (userSessionBean != null)
      {
        mModel = userSessionBean.getMenuModel();
      }
    }

    if (mModel != null)
    {
      MenuItemCursor mic = mModel.getSelectedMenuItem();
      if (mic != null)
      {
        currentMid = mic.getMid();
        mic = WebUtils.getTopWebMenuItem(mic);

        getElements().addAll(
          getElements(mic.hasChildren() ? mic.getFirstChild() : mic));
      }
    }
  }

  @Override
  public void generateUniqueIds() {}

  private List<MenuElement> getElements(MenuItemCursor mic)
  {
    List<MenuElement> items = new ArrayList();

    if (mic.getDirectProperty(TOPWEB_PROPERTY) == null)
    {
      String label = mic.getDirectProperty("description") != null
        ? mic.getDirectProperty("description")
        : mic.getLabel();

      ApplicationBean applicationBean = ApplicationBean.getCurrentInstance();
      label = applicationBean.translate(label, "jsp:" + mic.getMid());

      if (mic.hasChildren())
      {
        DefaultSubMenu.Builder builder = DefaultSubMenu.builder()
          .label(label);

        //Icon
        String icon = mic.getProperty("icon");
        if (!StringUtils.isBlank(icon))
          builder.icon(icon);

        DefaultSubMenu submenu = builder.build();

        submenu.getElements().addAll(getElements(mic.getFirstChild()));
        submenu.setId(mic.getMid());
        items.add(submenu);
      }
      else
      {
        if (mic.isRendered())
        {
          MatrixMenuItem.Builder builder = MatrixMenuItem.builder(mic)
            .value(label);
          if (mic.getProperty("content") != null)
          {
            String command = "#{templateBean.show('" + mic.getMid() + "')}";
            builder.command(command);
            builder.ajax(true);
            builder.onstart("PF('menuBar').hide();");
            builder.process("@this");
            builder.update("@form:cnt");
          }
          else if (mic.getAction() == null && mic.getURL() != null)
          {
            builder.url(mic.getURL());
            builder.ajax(false);
          }
          //Target
          builder.target(mic.getTarget());

          //Icon
          String icon = mic.getProperty("icon");
          if (!StringUtils.isBlank(icon))
            builder.icon(icon);

          //Current styleClass
          if (currentMid.equals(mic.getMid()))
            builder.styleClass("current");

          MatrixMenuItem item = builder.build();
          item.setId(mic.getMid());
          items.add(item);
        }
      }
    }

    if (mic.moveNext())
    {
      List<MenuElement> elements = getElements(mic);
      items.addAll(elements);
    }

    return items;
  }

}
