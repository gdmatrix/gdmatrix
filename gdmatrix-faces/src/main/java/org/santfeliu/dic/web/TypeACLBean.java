package org.santfeliu.dic.web;

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.security.AccessControl;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.security.web.ACLPageBean;

/**
 *
 * @author realor
 */
public class TypeACLBean extends ACLPageBean
{

  @Override
  public String show()
  {
    return "type_acl";
  }

  public String searchRole()
  {
    return getControllerBean().searchObject("Role",
      "#{typeACLBean.editingAccessControlItem.accessControl.roleId}");
  }
  
  public List<SelectItem> getActions()
  {
    List<SelectItem> items;
    if (!isNew())
    {
      String typeId = getObjectId();
      // get actions for that type
      items = DictionaryConfigBean.getActionSelectItems(typeId);
    }
    else // return standard actions
    {
      items = new ArrayList<SelectItem>();
      for (String action : DictionaryConstants.standardActions)
      {
        items.add(new SelectItem(action,
          DictionaryConfigBean.getLocalizedAction(action)));
      }
    }
    items.add(new SelectItem(
      DictionaryConstants.DERIVE_DEFINITION_ACTION,
      DictionaryConfigBean.getLocalizedAction(
        DictionaryConstants.DERIVE_DEFINITION_ACTION)));

    items.add(new SelectItem(
      DictionaryConstants.MODIFY_DEFINITION_ACTION,
      DictionaryConfigBean.getLocalizedAction(
        DictionaryConstants.MODIFY_DEFINITION_ACTION)));

    return items;
  }

  public List<AccessControl> getTypeRows()
  {
    return null;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  protected List<AccessControl> getMainAccessControlList()
  {
    TypeMainBean typeMainBean = (TypeMainBean)getBean("typeMainBean");
    return typeMainBean.getType().getAccessControl();
  }

}
