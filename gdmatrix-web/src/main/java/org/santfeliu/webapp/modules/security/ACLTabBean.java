/*
 * GDMatrix
 *
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.webapp.modules.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import org.matrix.security.AccessControl;
import org.matrix.security.SecurityConstants;
import org.primefaces.PrimeFaces;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.dic.DicModuleBean;

/**
 *
 * @author realor
 */
public abstract class ACLTabBean extends TabBean
{
  private int firstRow;
  private AccessControlEdit editing;
  private List<AccessControlEdit> rows = new ArrayList<>();

  @Inject
  DicModuleBean dicModuleBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public List<AccessControlEdit> getRows()
  {
    return rows;
  }

  public void setRows(List<AccessControlEdit> rows)
  {
    this.rows = rows;
  }

  public AccessControlEdit getEditing()
  {
    return editing;
  }

  public void setEditingEdit(AccessControlEdit edit)
  {
    this.editing = edit;
  }

  public String getRoleId()
  {
    return editing == null ? NEW_OBJECT_ID :
      editing.getRoleId();
  }

  public void setRoleId(String roleId)
  {
    if (editing != null)
    {
      editing.setRoleId(roleId);
      showDialog();
    }
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public List<SelectItem> getActionSelectItems()
  {
    String rootTypeId = getObjectBean().getRootTypeId();
    return dicModuleBean.getActionSelectItems(rootTypeId);
  }

  @Override
  public boolean isModified()
  {
    return true;
  }

  @Override
  public void load()
  {
    List<AccessControl> accessControlList = getAccessControlList();
    accessControlList.sort((a, b) -> a.getRoleId().compareTo(b.getRoleId()));

    rows.clear();
    AccessControlEdit edit = null;
    for (AccessControl accessControl : accessControlList)
    {
      if (edit == null || !edit.getRoleId().equals(accessControl.getRoleId()))
      {
        edit = new AccessControlEdit();
        edit.setRoleId(accessControl.getRoleId());
        rows.add(edit);
      }
      edit.actions.add(accessControl.getAction());
    }
  }

  @Override
  public void store()
  {
    load();
  }

  public void accept()
  {
    if (editing == null) return;

    if (!editing.getActions().isEmpty())
    {
      int index = rows.indexOf(editing);
      if (index == -1) // new edit
      {
        index = indexOfRole(editing.roleId);
        if (index == -1)
        {
          rows.add(editing);
        }
        else
        {
          rows.set(index, editing);
        }
      }
    }
    editing.setModified(true);
    editing = null;

    syncRows();
  }

  public void cancel()
  {
    editing = null;
  }

  public void create()
  {
    editing = new AccessControlEdit();
  }

  public void edit(AccessControlEdit edit)
  {
    if (edit != null)
    {
      editing = edit;
    }
    else
    {
      create();
    }
  }

  public void remove(AccessControlEdit edit)
  {
    edit.actions.clear();
    edit.setModified(true);
    syncRows();
  }

  public abstract List<AccessControl> getAccessControlList();

  public boolean isPersistentRole(String roleId)
  {
    return !roleId.startsWith(SecurityConstants.SELF_ROLE_PREFIX);
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing, rows };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (AccessControlEdit)stateArray[0];
      rows = (List<AccessControlEdit>)stateArray[1];
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  protected void syncRows()
  {
    List<AccessControl> accessControlList = getAccessControlList();
    accessControlList.clear();
    for (AccessControlEdit edit : rows)
    {
      for (String action : edit.actions)
      {
        AccessControl accessControl = new AccessControl();
        accessControl.setRoleId(edit.roleId);
        accessControl.setAction(action);
        accessControlList.add(accessControl);
      }
    }
  }

  protected int indexOfRole(String roleId)
  {
    for (int i = 0; i < rows.size(); i++)
    {
      AccessControlEdit edit = rows.get(i);
      if (edit.roleId.equals(roleId)) return i;
    }
    return -1;
  }

  protected void showDialog()
  {
    try
    {
      PrimeFaces current = PrimeFaces.current();
      current.executeScript("PF('" + getDialogWidgetVar() + "').show();");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  protected abstract String getDialogWidgetVar();

  public class AccessControlEdit implements Serializable
  {
    String roleId;
    List<String> actions = new ArrayList<>();
    boolean modified;

    public AccessControlEdit()
    {
    }

    public AccessControlEdit(String roleId)
    {
      this.roleId = roleId;
    }

    public String getRoleId()
    {
      return roleId;
    }

    public void setRoleId(String roleId)
    {
      this.roleId = roleId;
    }

    public List<String> getActions()
    {
      return actions;
    }

    public void setActions(List<String> actions)
    {
      this.actions = actions;
    }

    public boolean isModified()
    {
      return modified;
    }

    public void setModified(boolean modified)
    {
      this.modified = modified;
    }

    public String getActionsString()
    {
      FacesContext context = FacesContext.getCurrentInstance();
      Locale locale = context.getViewRoot().getLocale();
      String bundleName = context.getApplication().getMessageBundle();
      ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);

      List<String> actionLabels = new ArrayList<>(actions.size());
      for (String action : actions)
      {
        String value;
        try
        {
          value = bundle.getString(action);
        }
        catch (MissingResourceException ex)
        {
          value = action;
        }
        actionLabels.add(value);
      }
      return String.join(", ", actionLabels);
    }
  }
}

