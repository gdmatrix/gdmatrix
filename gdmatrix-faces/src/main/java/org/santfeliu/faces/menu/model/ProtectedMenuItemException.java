package org.santfeliu.faces.menu.model;

/**
 *
 * @author realor
 */
public class ProtectedMenuItemException extends MenuException
{
  private String mid;

  public ProtectedMenuItemException(String mid)
  {
    super("Protected menu item: " + mid);
    this.mid = mid;
  }

  public String getMid()
  {
    return mid;
  }
}
