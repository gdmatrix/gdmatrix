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
package org.santfeliu.webapp.modules.doc;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.security.AccessControl;
import org.primefaces.PrimeFaces;
import org.santfeliu.dic.web.DictionaryConfigBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class DocumentACLTabBean extends TabBean
{
  private int firstRow;
  private AccessControl editing;
  private boolean addToList;

  @Inject
  DocumentObjectBean documentObjectBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return documentObjectBean;
  }

  public List<AccessControl> getRows()
  {
    return documentObjectBean.getDocument().getAccessControl();
  }

  public void setRows(List<AccessControl> rows)
  {
    documentObjectBean.getDocument().getAccessControl().clear();
    documentObjectBean.getDocument().getAccessControl().addAll(rows);
  }

  public AccessControl getEditing()
  {
    return editing;
  }

  public void setEditing(AccessControl accessControl)
  {
    this.editing = accessControl;
  }

  public String getRoleId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getRoleId();
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
    return DictionaryConfigBean.getActionSelectItems(
      DictionaryConstants.DOCUMENT_TYPE);
  }

  @Override
  public void store()
  {
    try
    {
      if (editing != null)
      {
        if (addToList)
        {
          List<AccessControl> accessControl = getRows();
          accessControl.add(editing);
          addToList = false;
        }
        editing = null;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    info("CANCEL_OBJECT");
    editing = null;
  }

  public void create()
  {
    editing = new AccessControl();
    addToList = true;
  }

  public void edit(AccessControl accessControl)
  {
    if (accessControl != null)
    {
      editing = accessControl;
    }
    else
    {
      create();
    }
  }

  public void remove(AccessControl accessControl)
  {
    getRows().remove(accessControl);
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing, addToList };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (AccessControl)stateArray[0];
      addToList = (Boolean)stateArray[1];
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void showDialog()
  {
    try
    {
      PrimeFaces current = PrimeFaces.current();
      current.executeScript("PF('documentACLDialog').show();");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
