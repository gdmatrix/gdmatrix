package org.santfeliu.dic.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.TypePathStringComparator;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectBean;
import org.santfeliu.web.obj.ObjectDescriptionCache;

/**
 *
 * @author realor
 */
public class TypeBean extends ObjectBean
{
  private static final List<SelectItem> rootTypeIdSelectItems =
    new ArrayList<SelectItem>();

  @Override
  public String getObjectTypeId()
  {
    return "Type";
  }

  @Override
  public String getDescription(String objectId)
  {    
    try
    {
      TypeCache typeCache = TypeCache.getInstance();
      Type type = typeCache.getType(objectId);
      return getDescription(type);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }

  public String getDescription(Type type)
  {
    if (type == null)
      return "";
    else
      return type.getDescription();
  }

  @Override
  public void postStore()
  {
    // sync types
    TypeCache.getInstance().sync();
  }

  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        DictionaryConfigBean.getPort().removeType(getObjectId());
        removed();
        // sync types
        TypeCache.getInstance().sync();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return getControllerBean().show();
  }
  
  public List<SelectItem> getSelectItems(
    String rootTypeId, String selectedTypeId)
  {
    return getSelectItems(rootTypeId, selectedTypeId, true);
  }

  public List<SelectItem> getSelectItems(
    String rootTypeId, String selectedTypeId, boolean addNoInstantiableTypes)
  {
    List<SelectItem> items = new LinkedList<SelectItem>();
    List<SelectItem> selectItems = super.getSelectItems(selectedTypeId);

    TypeCache typeCache = TypeCache.getInstance();

    items.add(new SelectItem(ControllerBean.NEW_OBJECT_ID, " "));
    for (SelectItem selectItem : selectItems)
    {
      String typeId = (String)selectItem.getValue();
      if (typeId != null && typeId.trim().length() > 0)
      {
        boolean isSeparator =
          ControllerBean.SEPARATOR_ID.equals(selectItem.getValue());

        if (isSeparator)
          items.add(selectItem);
        else
        {
          org.santfeliu.dic.Type type = typeCache.getType(typeId);
          if (type != null && type.isDerivedFrom(rootTypeId) && (addNoInstantiableTypes || type.isInstantiable()))
          {
            items.add(selectItem);
          }
        }
      }
    }

    return items;
  }

  public List<SelectItem> getRootTypeIdSelectItems()
  {
    return rootTypeIdSelectItems;
  }

  static
  {
    ArrayList<String> rootTypeIds = new ArrayList<String>();
    rootTypeIds.addAll(DictionaryConstants.rootTypeIds);
    Collections.sort(rootTypeIds);

    for (String typeId : rootTypeIds)
    {
      SelectItem selectItem = new SelectItem();
      selectItem.setLabel(typeId);
      selectItem.setValue(typeId);
      rootTypeIdSelectItems.add(selectItem);
    }
  }

  public List<SelectItem> getSelectItems(List<String> typeIdList,
    String adminRole, String[] actions, boolean allTypesVisible)
  {
    Credentials credentials =
      UserSessionBean.getCurrentInstance().getCredentials();
    Set<String> userRoles = UserCache.getUser(credentials).getRoles();

    List<SelectItem> items = new LinkedList<SelectItem>();
    items.add(new SelectItem(ControllerBean.NEW_OBJECT_ID, " ")); // blank row
    ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
    boolean isAdminUser =
      adminRole != null ? userRoles.contains(adminRole) : false;

    TypeCache typeCache = TypeCache.getInstance();
    for (String typeId : typeIdList)
    {
      org.santfeliu.dic.Type type = typeCache.getType(typeId);
      if ((allTypesVisible
        || canPerformAction(type, actions, userRoles)
        || isAdminUser) && type.isInstantiable())
      {
        SelectItem item = new SelectItem();
        item.setValue(typeId);
        String description = cache.getDescription(this, typeId);
        item.setLabel(("".equals(description)) ? " " : description);
        item.setDescription(description);
        items.add(item);
      }
    }

    return items;
  }

  public List<SelectItem> getAllSelectItems(
    String rootTypeId, String adminRole, String[] actions, boolean allTypesVisible)
  {
    Credentials credentials =
      UserSessionBean.getCurrentInstance().getCredentials();
    Set<String> userRoles = UserCache.getUser(credentials).getRoles();

    List<SelectItem> selectTypeItems =
      new ArrayList<javax.faces.model.SelectItem>();

    TypeCache typeCache = TypeCache.getInstance();
    try
    {
      org.santfeliu.dic.Type type = typeCache.getType(rootTypeId);
      if (type != null)
      {
        List<org.santfeliu.dic.Type> selectTypes =
          new ArrayList<org.santfeliu.dic.Type>();
        if (type.isInstantiable())
          selectTypes.add(type);
        selectTypes.addAll(type.getDerivedTypes(true));
        Collections.sort(selectTypes,
            new TypePathStringComparator(false, true, false));

        for (org.santfeliu.dic.Type selectType : selectTypes)
        {
          boolean isAdminUser =
            adminRole != null ? userRoles.contains(adminRole) : false;

          if ((allTypesVisible
            || canPerformAction(selectType, actions, userRoles)
            || isAdminUser) && selectType.isInstantiable())
          {
            String typePath = null;
            if (type.isInstantiable() && rootTypeId.equals(selectType.getTypeId()))
              typePath = selectType.formatTypePath(false, true, true);
            else
              typePath = selectType.formatTypePath(false, true, false, rootTypeId);
            
            selectTypeItems.add(
              new SelectItem(selectType.getTypeId(), typePath));
          }
        }            
      }
    }
    catch (Exception ex)
    {
    }
    return selectTypeItems;
  }

  private boolean canPerformAction(org.santfeliu.dic.Type type,
    String[] actions, Set<String> userRoles)
  {
    boolean canPerform = false;
    for (String action : actions)
    {
      canPerform = type.canPerformAction(action, userRoles);
      if (canPerform)
        return true;
    }

    return canPerform;
  }
}
