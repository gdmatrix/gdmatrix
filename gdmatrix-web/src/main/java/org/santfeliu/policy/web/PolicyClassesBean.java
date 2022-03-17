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
package org.santfeliu.policy.web;

import java.util.Date;
import java.util.List;
import org.matrix.policy.ClassPolicy;
import org.matrix.policy.ClassPolicyFilter;
import org.matrix.policy.ClassPolicyView;
import org.santfeliu.classif.Class;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class PolicyClassesBean extends PageBean
{
  private List<ClassPolicyView> rows;

  public PolicyClassesBean()
  {
    load();
  }

  public List<ClassPolicyView> getRows()
  {
    return rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public void setRows(List<ClassPolicyView> rows)
  {
    this.rows = rows;
  }

  @Override
  public String show()
  {
    return "policy_classes";
  }

  public String showClass()
  {
    return getControllerBean().showObject("Class",
      (String)getValue("#{row.clazz.classId}"));
  }

  public String getClassTitle(ClassPolicyView view)
  {
    String classId = view.getClassPolicy().getClassId();
    try
    {
      Class clazz = ClassCache.getInstance().getClass(classId);
      return clazz.getTitle();
    }
    catch (Exception ex)
    {
      return "???";
    }
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        ClassPolicyFilter filter = new ClassPolicyFilter();
        filter.setPolicyId(getObjectId());
        rows =
          PolicyConfigBean.getPort().findClassPolicyViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public boolean isClassPolicyActive(ClassPolicyView view)
  {
    ClassPolicy classPolicy = view.getClassPolicy();
    String today = TextUtils.formatDate(new Date(), "yyyyMMdd");

    String startDate = classPolicy.getStartDate();
    if (startDate == null) startDate = "00000000";
    String endDate = classPolicy.getEndDate();
    if (endDate == null) endDate = "99991231";

    return startDate.compareTo(today) <= 0 &&
      endDate.compareTo(today) >= 0;
  }
}
