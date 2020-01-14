package org.santfeliu.faces.menu.model;

/**
 *
 * @author realor
 */
public class MenuItemNotFoundException extends MenuException
{
  private String mid;

  public MenuItemNotFoundException(String mid)
  {
    super("Menu item not found: " + mid);
    this.mid = mid;
  }

  public String getMid()
  {
    return mid;
  }
}
