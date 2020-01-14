package org.santfeliu.security.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.web.DictionaryConfigBean;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author lopezrj
 */
public abstract class ACLPageBean extends PageBean
{
  public static String STATE_NEW = "N";
  public static String STATE_EDITED = "T";
  public static String STATE_EXISTING = "E";
  public static String STATE_REMOVED = "R";

  protected AccessControlItem editingAccessControlItem;
  protected List<AccessControlItem> rows;
  protected List<AccessControl> typeRows;

  private AccessControl accessControlBackup;

  public List<AccessControlItem> getRows()
  {
    if (rows == null)
    {
      rows = new ArrayList<AccessControlItem>();
      List<AccessControl> accessControlList = getMainAccessControlList();
      for (AccessControl accessControl : accessControlList)
      {
        AccessControlItem item = 
          new AccessControlItem(accessControl, STATE_EXISTING);
        rows.add(item);
      }
    }
    return rows;
  }

  public void setRows(List<AccessControlItem> rows)
  {
    this.rows = rows;
  }

  public AccessControlItem getEditingAccessControlItem()
  {
    return editingAccessControlItem;
  }

  public void setEditingAccessControlItem(
    AccessControlItem editingAccessControlItem)
  {
    this.editingAccessControlItem = editingAccessControlItem;
  }

  public AccessControl getAccessControlBackup()
  {
    return accessControlBackup;
  }

  public void setAccessControlBackup(AccessControl accessControlBackup)
  {
    this.accessControlBackup = accessControlBackup;
  }

  public String storeAccessControl()
  {
    try
    {
      String roleId = editingAccessControlItem.getAccessControl().getRoleId();
      if (roleId == null || roleId.trim().length() == 0)
      {
        if (!editingAccessControlItem.isNew())
        {
          revertAccessControlChanges();
        }        
      }
      else
      {
        if (!getRows().contains(editingAccessControlItem))
        {
          getMainAccessControlList().add(
            editingAccessControlItem.getAccessControl());
          getRows().add(editingAccessControlItem);
        }
        else //has been modified?
        {
          if (isAccessControlModified())
          {
            editingAccessControlItem.setState(STATE_EDITED);
          }
        }
      }
      accessControlBackup = null;
      editingAccessControlItem = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String cancelAccessControl()
  {
    accessControlBackup = null;
    editingAccessControlItem = null;
    return null;
  }

  public String removeAccessControl()
  {
    AccessControlItem row =
      (AccessControlItem)getExternalContext().getRequestMap().get("row");
    getMainAccessControlList().remove(row.getAccessControl());
    if (row.isNew())
    {
      getRows().remove(row);
    }
    else
    {
      row.setState(STATE_REMOVED);
    }
    return null;
  }

  public String editAccessControl()
  {
    editingAccessControlItem =
      (AccessControlItem)getExternalContext().getRequestMap().get("row");
    backupAccessControl();
    return null;
  }

  public String addAccessControl()
  {
    editingAccessControlItem = new AccessControlItem(STATE_NEW);
    return null;
  }

  @Override
  public String store()
  {
    editingAccessControlItem = null;
    accessControlBackup = null;
    rows = null;
    typeRows = null;
    return show();
  }

  public String getRowAction()
  {
    String action = (String)getValue("#{row.accessControl.action}");
    return DictionaryConfigBean.getLocalizedAction(action);
  }

  public String getTypeRowAction()
  {
    String action = (String)getValue("#{typeRow.action}");
    return DictionaryConfigBean.getLocalizedAction(action);
  }

  public String getAccessControlStyleClass()
  {
    AccessControlItem row =
      (AccessControlItem)getExternalContext().getRequestMap().get("row");
    if (row.isNew())
    {
      return "newAccessControl";
    }
    else if (row.isRemoved())
    {
      return "removedAccessControl";
    }
    else if (row.isExisting())
    {
      return "existingAccessControl";
    }
    else if (row.isEdited())
    {
      return "editedAccessControl";
    }
    return "";
  }

  public boolean isRenderTypeRows()
  {
    return getTypeRows() != null && !getTypeRows().isEmpty();
  }

  public abstract String searchRole();
  public abstract List<SelectItem> getActions();
  public abstract List<AccessControl> getTypeRows();
  protected abstract List<AccessControl> getMainAccessControlList();

  public class AccessControlItem implements Serializable
  {
    private AccessControl accessControl;
    private String state; //(N)ew, (E)xisting, (R)emoved, Edi(T)ed

    public AccessControlItem(String state)
    {
      this.accessControl = new AccessControl();
      this.state = state;
    }

    public AccessControlItem(AccessControl accessControl, String state)
    {
      this.accessControl = accessControl;
      this.state = state;
    }

    public AccessControl getAccessControl()
    {
      return accessControl;
    }

    public void setAccessControl(AccessControl accessControl)
    {
      this.accessControl = accessControl;
    }

    public String getState()
    {
      return state;
    }

    public void setState(String state)
    {
      this.state = state;
    }

    public boolean isNew()
    {
      return STATE_NEW.equals(state);
    }

    public boolean isRemoved()
    {
      return STATE_REMOVED.equals(state);
    }

    public boolean isExisting()
    {
      return STATE_EXISTING.equals(state);
    }

    public boolean isEdited()
    {
      return STATE_EDITED.equals(state);
    }
  }

  private void backupAccessControl()
  {
    accessControlBackup = new AccessControl();
    accessControlBackup.setAction(
      editingAccessControlItem.getAccessControl().getAction());
    accessControlBackup.setRoleId(
      editingAccessControlItem.getAccessControl().getRoleId());
  }

  private void revertAccessControlChanges()
  {
    editingAccessControlItem.getAccessControl().setAction(
      accessControlBackup.getAction());
    editingAccessControlItem.getAccessControl().setRoleId(
      accessControlBackup.getRoleId());
  }

  private boolean isAccessControlModified()
  {
    String a1 = editingAccessControlItem.getAccessControl().getAction();
    String r1 = editingAccessControlItem.getAccessControl().getRoleId();
    String a2 = accessControlBackup.getAction();
    String r2 = accessControlBackup.getRoleId();
    if (a1 == null && r1 == null && a2 == null && r2 == null)
    {
      return false;
    }
    else if (a1 == null && a2 == null && r1 != null && r1.equals(r2))
    {
      return false;
    }
    else if (r1 == null && r2 == null && a1 != null && a1.equals(a2))
    {
      return false;
    }
    else if (r1 != null && r1.equals(r2) && a1 != null && a1.equals(a2))
    {
      return false;
    }
    else return true;
  }

}
