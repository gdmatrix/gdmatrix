package org.santfeliu.web.obj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class PageHistory extends ArrayList implements Serializable
{
  private String currentMid;
  private String currentObjectId;

  public void visit(String mid, String objectId)
  {
    visit(mid, objectId, null);
  }
  
  public void visit(String mid, String objectId, String currentTypeId)
  {    
    if (currentMid != null)
    {
      // objectId is always null for searchBeans and only null for searchBeans
      // if mid is a tab, objectId can not be null!!!
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor cursor1 = userSessionBean.getMenuModel().getMenuItem(mid);
      MenuItemCursor cursor2 = userSessionBean.getMenuModel().getMenuItem(currentMid);
      if ((!isSamePage(cursor1, cursor2) ||
          !isSameObject(objectId, currentObjectId)))
      {
        // page changed, add current page

        // remove it because is going to be current
        removePage(cursor1, objectId);
        // add current page/object to list unless it is new object
        if (!ControllerBean.NEW_OBJECT_ID.equals(currentObjectId))
        {
          // do not add to list new objects
          add(new Entry(currentMid, currentObjectId, currentTypeId));
        }
      }
    }
    currentMid = mid;
    currentObjectId = objectId;
  }

  public void reset()
  {
    currentObjectId = ControllerBean.NEW_OBJECT_ID;
  }

  public String close()
  {
    if (isEmpty())
    {
      currentMid = null;
      currentObjectId = null;
      return "blank";
    }
    else
    {
      Entry entry = (Entry)remove(size() - 1);
      currentMid = entry.getMid();
      currentObjectId = entry.getObjectId();

      ControllerBean controllerBean = ControllerBean.getCurrentInstance();
      if (currentObjectId == null ||
        currentObjectId.equals(ControllerBean.NEW_OBJECT_ID))
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        MenuItemCursor menuItem =
          userSessionBean.getMenuModel().getMenuItem(currentMid);
        PageBean searchBean = controllerBean.getSearchBean(menuItem);
        if (searchBean == null)
        {
          // go last object screen
          return controllerBean.show(currentMid);
        }
        else
        {
          // go search screen
          return controllerBean.search(currentMid);
        }
      }
      else
      {
        // go object screen for currentObjectId
        return controllerBean.show(currentMid, currentObjectId);
      }
    }
  }

  private void removePage(MenuItemCursor cursor, String objectId)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    int i = 0;
    boolean found = false;
    while (i < size() && !found)
    {
      Entry entry = (Entry)get(i);
      String entryMid = entry.getMid();
      String entryObjectId = entry.getObjectId();
      MenuItemCursor entryCursor =
        userSessionBean.getMenuModel().getMenuItem(entryMid);
      if (isSamePage(cursor, entryCursor) && 
          isSameObject(objectId, entryObjectId))
      {
        found = true;
      }
      else
      {
        i++;
      }
    }
    if (found) remove(i);
  }

  private boolean isSamePage(MenuItemCursor cursor1, MenuItemCursor cursor2)
  {
    if (cursor1.equals(cursor2)) return true;
    
    Map properties = (Map)cursor1.getDirectProperties();
    if (properties.containsKey(ControllerBean.OBJECT_BEAN_PROPERTY))
      return false;

    properties = (Map)cursor2.getDirectProperties();
    if (properties.containsKey(ControllerBean.OBJECT_BEAN_PROPERTY))
      return false;

    // both cursors are tabs
    if (cursor1.getParent().equals(cursor2.getParent()))
      return true;

    return false;
  }

  private boolean isSameObject(String objectId1, String objectId2)
  {
    if (objectId1 == null) return objectId2 == null;
    return objectId1.equals(objectId2);
  }

  /* Inner class Entry */
  public class Entry implements Serializable
  {
    private String mid;
    private String objectId;
    private String typeId;

    public Entry(String mid, String objectId, String typeId)
    {
      this.mid = mid;
      this.objectId = objectId;
      this.typeId = typeId;
    }

    public String getMid()
    {
      return mid;
    }

    public String getObjectId()
    {
      return objectId;
    }

    public String getTypeId()
    {
      return typeId;
    }

    public String getTitle()
    {
      String title = null;
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor cursor = userSessionBean.getMenuModel().getMenuItem(mid);
      ControllerBean controllerBean = ControllerBean.getCurrentInstance();
      if (objectId == null) // search
      {
        PageBean searchBean = controllerBean.getSearchBean(cursor);
        if (searchBean != null)
        {
          title = searchBean.getTitle(cursor);
        }
      }

      if (title == null)
      {
        ObjectBean objectBean = controllerBean.getObjectBean(cursor);
        if (objectBean != null)
        {
          title = objectBean.getTitle(cursor, typeId);
        }
      }
      return title;
    }

    public String getDescription()
    {
      String description = null;

      ControllerBean controllerBean = ControllerBean.getCurrentInstance();
      ObjectBean objectBean = controllerBean.getObjectBean(mid);
      if (objectBean != null && objectId != null)
      {
        ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
        description = cache.getDescription(objectBean, objectId);
      }
      return description;
    }

    public String show()
    {
      ControllerBean controllerBean = ControllerBean.getCurrentInstance();
      if (objectId == null)
      {
        PageBean searchBean = controllerBean.getSearchBean(mid);
        if (searchBean != null)
          return controllerBean.search(mid);
        else
          return controllerBean.show(mid);
      }
      else
      {
        return controllerBean.show(mid, objectId);
      }
    }

    public String close()
    {
      remove(this);
      return null;
    }

    public String getObjectTypeIconPath()
    {
      ControllerBean controllerBean = ControllerBean.getCurrentInstance();
      ObjectBean objectBean = controllerBean.getObjectBean(mid);
      if (objectBean == null) return null;
      String key = objectBean.getObjectTypeId().toLowerCase();
      return (String)UserSessionBean.getCurrentInstance().getObjectIcons().
        get(key);
    }

    public boolean isRenderObjectTypeIcon()
    {
      if (getObjectTypeIconPath() == null) return false;
      try
      {
        MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
          getMenuModel().getMenuItemByMid(mid);
        String value =
          menuItem.getProperty(ObjectBean.RENDER_OBJECT_TYPE_ICON_PROPERTY);
        return (value != null ? value.equals("true") : false);
      }
      catch (Exception ex)
      {
        return false;
      }
    }
  }
}
