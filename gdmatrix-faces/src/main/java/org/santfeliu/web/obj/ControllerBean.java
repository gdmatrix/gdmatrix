package org.santfeliu.web.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;


import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;

public class ControllerBean extends FacesBean implements Savable
{
  public static String EDIT_VIEW = "edit";
  public static String DETAIL_VIEW = "detail";

  public static String CONTROLLER_BEAN_NAME = "controllerBean";
  public static String OBJECT_BEAN_PROPERTY = "oc.objectBean";
  public static String SEARCH_BEAN_PROPERTY = "oc.searchBean";
  public static String PAGE_BEAN_PROPERTY = "oc.pageBean";
  public static String MID_PROPERTY_SUFFIX = "SearchMid";
  public static String NEW_OBJECT_ID = "";
  public static String SEPARATOR_ID = "SEPARATOR";
  public static String INVALID_NODE_CONFIG = "INVALID_NODE_CONFIG";

  // internal structures
  private PageHistory pageHistory = new PageHistory();
  private ReturnStack returnStack = new ReturnStack();

  private int pageHistoryVisibility; // 0:none, 1:all
  private int RECENT_PAGES_SIZE = 5;

  public ControllerBean()
  {
  }

  public static ControllerBean getCurrentInstance()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();
    return (ControllerBean)application.getVariableResolver().
      resolveVariable(context, CONTROLLER_BEAN_NAME);
  }

  // bean accessors

  public int getPageHistoryVisibility()
  {
    return pageHistoryVisibility;
  }

  public void setPageHistoryVisibility(int pageHistoryVisibility)
  {
    this.pageHistoryVisibility = pageHistoryVisibility;
  }

  public ObjectBean getObjectBean()
  {
    return getObjectBean(getSelectedMenuItem());
  }

  public ObjectBean getObjectBean(String mid)
  {
    MenuItemCursor menuItem = 
      UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(mid);
    return getObjectBean(menuItem);
  }

  public ObjectBean getObjectBean(MenuItemCursor menuItem)
  {
    menuItem = getHeadMenuItem(menuItem);
    if (menuItem.isNull()) return null;
    String objectBeanName = menuItem.getProperty(OBJECT_BEAN_PROPERTY);
    if (objectBeanName == null) return null;
    ObjectBean objectBean = (ObjectBean)getBean(objectBeanName);
    return objectBean;
  }

  // not all searchBeans extends SearchBean
  public PageBean getSearchBean()
  {
    return getSearchBean(getSelectedMenuItem());
  }
  
  public PageBean getSearchBean(String mid)
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(mid);
    return getSearchBean(menuItem);
  }
  
  public PageBean getSearchBean(MenuItemCursor menuItem)
  {
    if (menuItem.isNull()) return null;
    Map map = menuItem.getDirectProperties();
    String beanName = (String)map.get(SEARCH_BEAN_PROPERTY);
    if (beanName == null) return null;
    return (PageBean)getBean(beanName);
  }

  public PageBean getPageBean()
  {
    return getPageBean(getSelectedMenuItem());
  }

  public PageBean getPageBean(String mid)
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(mid);
    return getPageBean(menuItem);
  }

  public PageBean getPageBean(MenuItemCursor menuItem)
  {
    if (menuItem.isNull()) return null;
    Map map = menuItem.getDirectProperties();
    String beanName = (String)map.get(PAGE_BEAN_PROPERTY);
    if (beanName == null) return null;
    return (PageBean)getBean(beanName);
  }

  // actions

  public String markObjectAsFavorite(String objectTypeId, String objectId)
  {
    UserSessionBean.getCurrentInstance().getUserPreferences().
      storePreference(objectTypeId, objectId);
    return null;
  }

  public String unmarkObjectAsFavorite(String objectTypeId, String objectId)
  {
    UserSessionBean.getCurrentInstance().getUserPreferences().
      removePreference(objectTypeId, objectId);
    return null;
  }

  public boolean isObjectFavorite(String objectTypeId, String objectId)
  {
    return UserSessionBean.getCurrentInstance().getUserPreferences().
      existsPreference(objectTypeId, objectId);
  }

  public String create()
  {
    return create(null);
  }
  
  public String create(String mid)
  {
    MenuItemCursor menuItem = null;
    if (mid == null)
    {
      menuItem = getSelectedMenuItem();
    }
    else
    {
      menuItem = 
        UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(mid);
    }

    String oldTypeId = getActualTypeId(menuItem);

    menuItem = getHeadMenuItem(menuItem);
    if (menuItem.isNull())
    {
      error(INVALID_NODE_CONFIG);
      return null;
    }
    ObjectBean objectBean = getObjectBean(menuItem);
    objectBean.setObjectId(NEW_OBJECT_ID);
    clearBeans(menuItem);

    menuItem = getLeafMenuItem(menuItem);
    mid = menuItem.getMid();
    setCurrentMid(mid);

    PageBean pageBean = getPageBean(menuItem);
    if (pageBean == null)
    {
      error(INVALID_NODE_CONFIG);
      return null;
    }

    pageHistory.visit(menuItem.getMid(), NEW_OBJECT_ID, oldTypeId);

    return pageBean.show();
  }

  public String create(String mid, String valueBinding)
  {
    MenuItemCursor menuItem = null;
    if (mid == null)
    {
      menuItem = getSelectedMenuItem();
    }
    else
    {
      menuItem =
        UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(mid);
    }

    String oldTypeId = getActualTypeId(menuItem);

    menuItem = getHeadMenuItem(menuItem);
    if (menuItem.isNull())
    {
      error(INVALID_NODE_CONFIG);
      return null;
    }
    ObjectBean objectBean = getObjectBean(menuItem);
    objectBean.setObjectId(NEW_OBJECT_ID);
    clearBeans(menuItem);

    menuItem = getLeafMenuItem(menuItem);
    mid = menuItem.getMid();

    if (valueBinding != null) // creation with return
    {
      MenuItemCursor returnMenuItem = getSelectedMenuItem();
      String returnMid = returnMenuItem.getMid();
      Object beans = saveBeans(returnMenuItem);

      Map map = returnMenuItem.getDirectProperties();

      if (map.containsKey(SEARCH_BEAN_PROPERTY))
      {
        // return node is seach page
         returnStack.push(mid, returnMid,
          null, valueBinding, beans);
      }
      else
      {
        // return node is object page
        ObjectBean returnBean = getObjectBean(returnMid);
         returnStack.push(mid, returnMid,
          returnBean.getObjectId(), valueBinding, beans);
      }
    }

    setCurrentMid(mid);

    PageBean pageBean = getPageBean(menuItem);
    if (pageBean == null)
    {
      error(INVALID_NODE_CONFIG);
      return null;
    }

    pageHistory.visit(menuItem.getMid(), NEW_OBJECT_ID, oldTypeId);

    return pageBean.show();
  }

  public String createObject(String typeId)
  {
    MenuItemCursor cursor = getSelectedMenuItem();
    String propertyName = getTypePropertyName(typeId);
    String searchMid = cursor.getProperty(propertyName);
    if (searchMid != null)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      cursor = userSessionBean.getMenuModel().getMenuItem(searchMid);
      return create(cursor.getMid());
    }
    else
    {
      cursor = getHeadMenuItem(getSelectedMenuItem());
      if (!cursor.isNull())
      {
        ObjectBean objectBean = getObjectBean(cursor);
        if (objectBean.getObjectTypeId().equals(typeId))
        {
          cursor = getLeafMenuItem(cursor);
          if (getPageBean(cursor) != null)
          {
            return create(cursor.getMid());
          }
        }
      }

      error(INVALID_NODE_CONFIG);
      return null;
    }



  }

  public String createObject(String typeId, String valueBinding)
  {
    MenuItemCursor cursor = getHeadMenuItem(getSelectedMenuItem());

    if (!cursor.isNull())
    {
      ObjectBean objectBean = getObjectBean(cursor);
      if (objectBean.getObjectTypeId().equals(typeId))
      {
        cursor = getLeafMenuItem(cursor);
        String creationMid = cursor.getMid();

        if (creationMid == null)
        {
          error(INVALID_NODE_CONFIG);
          return null;
        }
        else
        {
          return create(creationMid, valueBinding);
        }
      }
    }

    cursor = getSelectedMenuItem();
    String propertyName = getTypePropertyName(typeId);
    String searchMid = cursor.getProperty(propertyName);
    if (searchMid == null)
    {
      error(INVALID_NODE_CONFIG);
      return null;
    }
    else
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      cursor = userSessionBean.getMenuModel().getMenuItem(searchMid);
      cursor = getLeafMenuItem(cursor);
      return create(cursor.getMid(), valueBinding);
    }
  }

  // It shows first tab of current object
  public String show()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();

    menuItem = getHeadMenuItem(menuItem);
    if (menuItem.isNull())
    {
      error(INVALID_NODE_CONFIG);
      return null;
    }
    menuItem = getLeafMenuItem(menuItem);
    return show(menuItem.getMid());
  }

  // It shows tab tabMid of current object
  public String show(String tabMid)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setSelectedMid(tabMid);
    PageBean pageBean = getPageBean();
    if (pageBean != null)
    {
      pageHistory.visit(tabMid, getObjectBean().getObjectId());
      return pageBean.show();
    }
    return null;
  }

  public String show(String tabMid, String objectId)
  {
    if (tabMid == null)
    {
      error(INVALID_NODE_CONFIG);
      return null;
    }
    MenuItemCursor oldMenuItem = getSelectedMenuItem();
    ObjectBean oldObjectBean = getObjectBean(oldMenuItem);

    String oldTypeId = getActualTypeId(oldMenuItem);

    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getMenuItem(tabMid);
    ObjectBean newObjectBean = getObjectBean(menuItem);

    if (newObjectBean == null)
    {
      error(INVALID_NODE_CONFIG);
      return null;
    }
    if (objectId == null) objectId = newObjectBean.getObjectId();

    setCurrentMid(tabMid);

    PageBean oldPageBean = getPageBean(oldMenuItem);
    if (oldObjectBean == null ||
      !oldObjectBean.getObjectId().equals(objectId) ||
      !oldObjectBean.getObjectTypeId().equals(newObjectBean.getObjectTypeId()) ||
      oldPageBean instanceof DetailBean)
    {
      // object change
      clearBeans(menuItem);
      newObjectBean.setObjectId(objectId);
      // refresh description
      ObjectDescriptionCache.getInstance().
        clearDescription(newObjectBean, objectId);
    }
    
    if (!menuItem.isRendered()) //target tab not visible -> main tab instead
    {  
      MenuItemCursor cursor = getHeadMenuItem(menuItem);
      if (!cursor.isNull())
      {
        cursor = getLeafMenuItem(cursor);
        if (getPageBean(cursor) != null)
        {
          tabMid = cursor.getMid();
          setCurrentMid(tabMid);
          menuItem = UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(tabMid);
        }
      }    
    }
    
    PageBean tabBean = getPageBean(menuItem);
    if (tabBean == null)
    {
      error(INVALID_NODE_CONFIG);
      return null;
    }

    String outcome = tabBean.show();
    if (!newObjectBean.isNew())
    {
      // update historyList
      ObjectHistory historyList = newObjectBean.getObjectHistory();
      historyList.setObject(objectId);
    }

    pageHistory.visit(tabMid, objectId, oldTypeId);

    return outcome;
  }

  public String showObject(String typeId)
  {
    return showObject(typeId, null);
  }

  public String showObject(String typeId, String objectId)
  {
    return showObject(typeId, objectId, EDIT_VIEW);
  }

  public String showObject(String typeId, String objectId, String view)
  {
    MenuItemCursor cursor = getSelectedMenuItem();
    String searchMid = cursor.getProperty(getTypePropertyName(typeId, view));
    if (searchMid == null) //try default view
      searchMid = cursor.getProperty(getTypePropertyName(typeId, null));

    if (searchMid != null)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      cursor = userSessionBean.getMenuModel().getMenuItem(searchMid);
      cursor = getLeafMenuItem(cursor);
      return show(cursor.getMid(), objectId);
    }
    else
    {
      //Try compatibility with use of oc.objectBean
      cursor = getHeadMenuItem(getSelectedMenuItem());
      if (!cursor.isNull())
      {
        ObjectBean objectBean = getObjectBean(cursor);
        if (objectBean.getObjectTypeId().equals(typeId))
        {
          cursor = getLeafMenuItem(cursor);
          if (getPageBean(cursor) != null)
          {
            return show(cursor.getMid(), objectId);
          }
        }
      }

      error(INVALID_NODE_CONFIG);
      return null;
    }
  }

  // current node contains a SearchBean
  public String search()
  {
    MenuItemCursor cursor = getSelectedMenuItem();
    return search(cursor.getMid(), null);
  }

  // mid node contains a SearchBean
  public String search(String mid)
  {
    return search(mid, null);
  }

  // mid node contains a SearchBean
  public String search(String mid, String valueBinding)
  {
    MenuItemCursor searchMenuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(mid);
    PageBean searchBean = getSearchBean(searchMenuItem);
    if (searchBean == null)
    {
      error(INVALID_NODE_CONFIG);
      return null;
    }
    String searchMid = searchMenuItem.getMid();

    if (valueBinding != null) // search with return
    {
      MenuItemCursor returnMenuItem = getSelectedMenuItem();
      String returnMid = returnMenuItem.getMid();
      Object beans = saveBeans(returnMenuItem);

      Map map = returnMenuItem.getDirectProperties();

      if (map.containsKey(SEARCH_BEAN_PROPERTY))
      {
        // return node is seach page
        returnStack.push(searchMid, returnMid,
          null, valueBinding, beans);
      }
      else
      {
        // return node is object page
        ObjectBean objectBean = getObjectBean(returnMid);
         returnStack.push(searchMid, returnMid,
          objectBean.getObjectId(), valueBinding, beans);
      }
    }

    String oldTypeId = getActualTypeId(getSelectedMenuItem());

    setCurrentMid(searchMid);

    pageHistory.visit(searchMid, null, oldTypeId);

    return searchBean.show();
  }

  // can only be call from edit nodes
  public String searchObject()
  {
    MenuItemCursor cursor = getHeadMenuItem(getSelectedMenuItem());
    if (!cursor.isNull())
    {
      PageBean searchBean = getSearchBean(cursor);
      if (searchBean != null)
      {
        return search(cursor.getMid(), null);
      }
      else
      {
        ObjectBean objectBean = getObjectBean(cursor);
        return searchObject(objectBean.getObjectTypeId(), null);
      }
    }
    else
    {
      error(INVALID_NODE_CONFIG);
    }
    return null;
  }

  public String searchObject(String typeId)
  {
    return searchObject(typeId, null);
  }

  public String searchObject(String typeId, String valueBinding)
  {
    String propertyName = getTypePropertyName(typeId);
    
    MenuItemCursor cursor = getSelectedMenuItem();
    String searchMid = cursor.getProperty(propertyName);
    if (searchMid == null)
    {
      error(INVALID_NODE_CONFIG);
      return null;
    }
    else
    {
      return search(searchMid, valueBinding);
    }
  }

  public String close()
  {
    return pageHistory.close();
  }

  public String showPageHistory()
  {
    if (pageHistoryVisibility < 1)
      pageHistoryVisibility++;
    return null;
  }

  public String hidePageHistory()
  {
    if (pageHistoryVisibility > 0)
      pageHistoryVisibility--;
    return null;
  }

  /* Use select() instead */
  @Deprecated
  public String back()
  {
    return select(null);
  }

  public String select()
  {
    return select(null);
  }

  public String select(String selectedObjectId)
  {
    if (returnStack.isEmpty()) return null;

    ReturnStack.Entry entry = returnStack.pop();
    String searchMid = entry.getSearchMid();
    String returnMid = entry.getReturnMid();
    String objectId = entry.getObjectId();
    String valueBinding = entry.getValueBinding();
    Object beans = entry.getBeans();

    String oldTypeId = getActualTypeId(getSelectedMenuItem());

    ObjectBean searchObjectBean = getObjectBean(searchMid);
    if (selectedObjectId == null)
    {      
      selectedObjectId = searchObjectBean.getObjectId();
    }
    else
    {
      searchObjectBean.getObjectHistory().setObject(selectedObjectId);
    }
    
    if (objectId != null && 
      searchObjectBean.getObjectHistory().containsObject(objectId))
    {
      searchObjectBean.getObjectHistory().setObject(objectId);
    }    

    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();

    setCurrentMid(returnMid);
    MenuItemCursor returnMenuItem = menuModel.getMenuItem(returnMid);

    ObjectBean returnObjectBean = getObjectBean(returnMid);
    returnObjectBean.setObjectId(objectId);

    clearBeans(returnMenuItem);
    restoreBeans(beans);

    setValueBinding(valueBinding, selectedObjectId);
    
    pageHistory.visit(returnMid, objectId, oldTypeId);

    Map map = (Map)returnMenuItem.getDirectProperties();
    String beanName = (String)map.get(ControllerBean.SEARCH_BEAN_PROPERTY);
    if (beanName == null)
    {
      beanName = (String)map.get(ControllerBean.PAGE_BEAN_PROPERTY);
    }
    PageBean bean = (PageBean)getBean(beanName);
    return bean.show();
  }
  
  public ObjectBean getSearchObjectBean()
  {
    if (returnStack.isEmpty()) return null;
    ReturnStack.Entry entry = returnStack.peek();

    String searchMid = entry.getSearchMid();
    return getObjectBean(searchMid);
  }
  
  public boolean isSelectableNode()
  {
    ObjectBean searchObjectBean = getSearchObjectBean();
    if (searchObjectBean == null) return false;
    
    ObjectBean objectBean = getObjectBean();
    if (objectBean == null) return false;
    
    return objectBean == searchObjectBean;
  }
  
  public boolean isSelectableObject()
  {
    ObjectBean searchObjectBean = getSearchObjectBean();
    if (searchObjectBean == null) return false;
    
    ObjectBean objectBean = getObjectBean();
    if (objectBean == null) return false;
    
    return objectBean == searchObjectBean && !objectBean.isNew();
  }

  public MenuItemCursor getLeafMenuItem(MenuItemCursor menuItem)
  {
    MenuItemCursor leafMenuItem = menuItem.getClone();
    if (!leafMenuItem.isNull())
    {
      while (leafMenuItem.hasChildren())
      {
        leafMenuItem.moveFirstChild();
      }
    }
    return leafMenuItem;
  }

  public MenuItemCursor getHeadMenuItem(MenuItemCursor menuItem)
  {
    boolean found = false;
    menuItem = menuItem.getClone(); // to avoid lateral effects
    while (!menuItem.isNull() && !found)
    {
      if (menuItem.getDirectProperties().get(OBJECT_BEAN_PROPERTY) != null)
      {
        found = true;
      }
      else
      {
        menuItem = menuItem.getParent();
      }
    }
    return menuItem;
  }

  public MenuItemCursor getPageMenuItem(MenuItemCursor menuItem,
    String pageBeanName)
  {
    MenuItemCursor pageMenuItem = menuItem.getClone();
    pageMenuItem = getHeadMenuItem(pageMenuItem);
    if (!pageMenuItem.isNull() && pageMenuItem.hasChildren())
    {
      pageMenuItem.moveFirstChild();
      boolean found = false;
      while (!found)
      {
        found = 
          pageBeanName.equals(pageMenuItem.getDirectProperty(PAGE_BEAN_PROPERTY));
        if (found) return pageMenuItem;
        pageMenuItem.moveNext();
      }
    }
    return pageMenuItem;
  }

  public String getBeanName(MenuItemCursor menuItem)
  {
    String name = (String)menuItem.getProperties().get(PAGE_BEAN_PROPERTY);
    if (name == null)
    {
      name = (String)menuItem.getProperties().get(OBJECT_BEAN_PROPERTY);
      if (name == null)
      {
        name = (String)menuItem.getProperties().get(SEARCH_BEAN_PROPERTY);
      }
    }
    return name;
  }

  public boolean existsBean(String beanName)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext extContext = context.getExternalContext();
    Map requestMap = extContext.getRequestMap();
    return requestMap.containsKey(beanName);
  }

  public void clearBean(String beanName)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext extContext = context.getExternalContext();
    Map requestMap = extContext.getRequestMap();
    requestMap.remove(beanName);

    //TODO : detect bean scope, and remove it from session
  }

  public void clearBeans(MenuItemCursor menuItem)
  {
    menuItem = getHeadMenuItem(menuItem);
    if (!menuItem.isNull())
    {
      menuItem.moveFirstChild();
      while (!menuItem.isNull())
      {
        String beanName = menuItem.getProperty(PAGE_BEAN_PROPERTY);
        if (beanName != null) clearBean(beanName);
        menuItem.moveNext();
      }
    }
    ObjectBean objectBean = getObjectBean();
    if (objectBean != null) 
      objectBean.clearObjectActions();
  }

  public void setCurrentMid(String mid)
  {
    UserSessionBean.getCurrentInstance().getMenuModel().setSelectedMid(mid);
  }

  public PageHistory getPageHistory()
  {
    return pageHistory;
  }

  public List getRecentPageHistory()
  {
    int size = pageHistory.size();
    return pageHistory.subList(Math.max(size - getRecentPagesSize(), 0), size);
  }

  public String getActualTypeId(MenuItemCursor menuItem)
  {
    String typeId = null;
    ObjectBean oldObjectBean = getObjectBean(menuItem);
    if (oldObjectBean != null)
    {
      typeId = oldObjectBean.getActualTypeId();
    }
    return typeId;
  }

  /************* private methods ***************/
  private int getRecentPagesSize()
  {
    try
    {
      UserPreferences userPreferences =
        UserSessionBean.getCurrentInstance().getUserPreferences();
      if (userPreferences != null)
      {
        String pageSizePreference =
          userPreferences.getRecentPagesSize();

        if (pageSizePreference != null)
          return Integer.valueOf(pageSizePreference);
      }
    }
    catch (Exception ex)
    {
    }

    return RECENT_PAGES_SIZE;
  }

  private String getTypePropertyName(String typeId)
  {
    return getTypePropertyName(typeId, null);
  }

  private String getTypePropertyName(String typeId, String view)
  {
    // properties format examples: caseSearchMid, personSearchMid, etc..
    String propertyName = typeId.substring(0, 1).toLowerCase() +
      typeId.substring(1) + MID_PROPERTY_SUFFIX + (view != null ? ":" + view : "");
    return propertyName;
  }
  
  private Object saveBeans(MenuItemCursor menuItem)
  {
    ArrayList beanList = new ArrayList();
    Object beans;
    if (menuItem.hasChildren()) // search node
    {
      String beanName = getBeanName(menuItem);
      Object bean = getBean(beanName);
      beans = new Object[]{beanName, bean};
    }
    else // tab node
    {
      menuItem = getHeadMenuItem(menuItem);
      if (!menuItem.isNull())
      {
        menuItem.moveFirstChild();
        while (!menuItem.isNull())
        {
          String beanName = getBeanName(menuItem);
          if (existsBean(beanName))
          {
            PageBean tabBean = (PageBean)getBean(beanName);
            if (tabBean.isModified())
            {
              beanList.add(beanName);
              beanList.add(tabBean);
            }
          }
          menuItem.moveNext();
        }
      }
    }
    return beanList.toArray();
  }

  private void restoreBeans(Object beans)
  {
    // TODO: restore in correct scope
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext extContext = context.getExternalContext();
    Map requestMap = extContext.getRequestMap();

    Object[] beanArray = (Object[])beans;
    for (int i = 0; i < beanArray.length; i += 2)
    {
      Object beanName = beanArray[i];
      Object bean = beanArray[i + 1];
      requestMap.put(beanName, bean);
    }
  }

  private void setValueBinding(String valueBinding, String value)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ValueBinding vb = 
      context.getApplication().createValueBinding(valueBinding);
    vb.setValue(context, value);
  }

  private MenuItemCursor getSelectedMenuItem()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
  }

}
