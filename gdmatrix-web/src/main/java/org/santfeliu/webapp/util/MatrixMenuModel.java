package org.santfeliu.webapp.util;

import org.santfeliu.webapp.util.WebUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;

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
    
    MenuItemCursor mic = mModel.getSelectedMenuItem();
    if (mic != null)
    {
      currentMid = mic.getMid();
      mic = WebUtils.getTopWebMenuItem(mic);

      super.getElements().addAll(
        getElements(mic.hasChildren() ? mic.getFirstChild() : mic));

      generateUniqueIds();
    }  
  }

  private List<MenuElement> getElements(MenuItemCursor mic)
  {
    List<MenuElement> items = new ArrayList();

    String label = mic.getDirectProperty("description") != null
      ? mic.getDirectProperty("description")
      : mic.getLabel();

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
      items.add(submenu);
    }
    else
    {
      if (mic.isRendered())
      {
        MatrixMenuItem.Builder builder = MatrixMenuItem.builder(mic)
          .value(label);

        if (mic.getAction() != null && mic.getAction().equals("blank"))
        {
          builder.command("#{templateBacking.searchBacking.show()}"); //TODO
          builder.onclick(encodeGoFunction(mic.getMid()));              
        }
        else if (mic.getAction() != null && !mic.getAction().equals("blank"))
        {
          builder.url(mic.getActionURL());
          builder.onclick(encodeGoFunction(mic.getMid()));     
        }
        else if (mic.getAction() == null && mic.getURL() != null)
        {
          builder.url(mic.getURL());
          builder.ajax(true);
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

        items.add(item);
      }
    }
    
    if (mic.moveNext())
    {
      List<MenuElement> elements = getElements(mic);
      items.addAll(elements);
    }    

    return items;
  }
  
  private String encodeGoFunction(String mid)
  {
    if (!StringUtils.isBlank(mid))
      return "return " + GOMID_JS_FUNCTION + "('" + mid +"');";
    else
      return "#";
  }
    

}
