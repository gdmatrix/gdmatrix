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
package org.santfeliu.webapp.modules.policy;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.policy.*;
import org.santfeliu.classif.ClassCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.classif.ClassObjectBean;
import static org.santfeliu.webapp.modules.policy.PolicyModuleBean.getPort;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class ClassPoliciesTabBean extends TabBean
{
  private List<ClassPolicyView> rows;
  private int firstRow;
  private ClassPolicy editing;

  @Inject
  ClassObjectBean classObjectBean;

  @PostConstruct
  public void init()
  {
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return classObjectBean;
  }

  public List<ClassPolicyView> getRows()
  {
    return rows;
  }

  public void setRows(List<ClassPolicyView> rows)
  {
    this.rows = rows;
  }

  public ClassPolicy getEditing()
  {
    return editing;
  }

  public void setEditing(ClassPolicy userInRole)
  {
    this.editing = userInRole;
  }

  public String getClassId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getClassId();
  }

  public void setClassId(String roleId)
  {
    if (editing != null)
    {
      editing.setClassId(roleId);
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
  
  public String getClassTitle(ClassPolicyView view)
  {
    String classId = view.getClassPolicy().getClassId();
    try
    {
      org.santfeliu.classif.Class clazz = 
        ClassCache.getInstance().getClass(classId);
      return clazz.getTitle();
    }
    catch (Exception ex)
    {
      return "???";
    }
  }  

  @Override
  public void load()
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        ClassPolicyFilter filter = new ClassPolicyFilter();
        filter.setClassId(getObjectId());
        rows = getPort(false).findClassPolicyViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else rows = Collections.EMPTY_LIST;
  }

  @Override
  public void store()
  {
    try
    {
      editing.setClassId(getObjectId());
      try
      {
        getPort(false).storeClassPolicy(editing);
      }
      catch (Exception ex)
      {
        throw new Exception("INVALID_OPERATION");
      }
      load();
      editing = null;
      growl("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }  
  
  public void create()
  {
    editing = new ClassPolicy();
  }

  public void edit(ClassPolicyView classPolicyView)
  {
    if (classPolicyView != null)
    {
      try
      {
        editing = getPort(false)
          .loadClassPolicy(classPolicyView.getClassPolicy().getClassPolicyId());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      create();
    }
  }

  public void remove(ClassPolicyView classPolicyView)
  {
    if (classPolicyView != null)
    {
      try
      {
        String classPolicyId = 
          classPolicyView.getClassPolicy().getClassPolicyId();
        getPort(false).removeClassPolicy(classPolicyId);
        load();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (ClassPolicy)stateArray[0];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
