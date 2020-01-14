package org.santfeliu.web.obj.util;

import java.io.Serializable;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
class NodeColumnRenderer extends ColumnRenderer implements Serializable
{

  public NodeColumnRenderer()
  {
  }

  public Object getValue(String columnName, Object row)
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(columnName);
  }

  @Override
  public boolean isValueEscaped()
  {
    return false;
  }

}
