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
package org.santfeliu.webapp.modules.cases;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.util.TextUtils;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class CaseObjectBean extends ObjectBean
{
  private Case cas = new Case();
  private String formSelector;

  @Inject
  CaseTypeBean caseTypeBean;

  @Inject
  CaseFinderBean caseFinderBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CASE_TYPE;
  }

  @Override
  public CaseTypeBean getTypeBean()
  {
    return caseTypeBean;
  }

  @Override
  public CaseFinderBean getFinderBean()
  {
    return caseFinderBean;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : cas.getTitle();
  }

  @Override
  public Case getObject()
  {
    return isNew() ? null : cas;
  }

  public Case getCase()
  {
    return cas;
  }

  public void setCase(Case cas)
  {
    this.cas = cas;
  }

  public void setNewClassId(String classId)
  {
    if (!StringUtils.isBlank(classId))
    {
      List<String> currentClassIdList = cas.getClassId();
      if (!currentClassIdList.contains(classId))
      {
        currentClassIdList.add(classId);
      }
    }
  }
  public Date getStartDateTime()
  {
    if (cas != null && cas.getStartDate() != null)
    {
      return getDate(cas.getStartDate(), cas.getStartTime());
    }
    else
    {
      return null;
    }
  }

  public Date getEndDateTime()
  {
    if (cas != null && cas.getEndDate() != null)
    {
      return getDate(cas.getEndDate(), cas.getEndTime());
    }
    else
    {
      return null;
    }
  }

  public void setStartDateTime(Date date)
  {
    if (cas != null)
    {
      if (date == null)
      {
        date = new Date();
      }
      cas.setStartDate(TextUtils.formatDate(date, "yyyyMMdd"));
      cas.setStartTime(TextUtils.formatDate(date, "HHmmss"));
    }
  }

  public void setEndDateTime(Date date)
  {
    if (date != null && cas != null)
    {
      cas.setEndDate(TextUtils.formatDate(date, "yyyyMMdd"));
      cas.setEndTime(TextUtils.formatDate(date, "HHmmss"));
    }
  }

  public String getPropertyLabel(String propName, String altName)
  {
    return caseTypeBean.getPropertyLabel(cas, propName, altName);
  }

  public boolean isPropertyHidden(String propName)
  {
    return caseTypeBean.isPropertyHidden(cas, propName);
  }

  /**
   * Not rendered when base Type has "classId" PropertyDefinition with default
   * value set, minium occurrences greater than zero, and read only.
   */
  public boolean isRenderClassId()
  {
    String typeId = getBaseTypeInfo().getBaseTypeId();
    PropertyDefinition pd =
      caseTypeBean.getPropertyDefinition(typeId, "classId");

    return !(pd != null && pd.getValue() != null && pd.getMinOccurs() > 0
      && pd.isReadOnly());
  }

  private Date getDate(String date, String time)
  {
    String dateTime = TextUtils.concatDateAndTime(date, time);
    return TextUtils.parseInternalDate(dateTime);
  }

  @Override
  public void loadObject() throws Exception
  {
    formSelector = null;

    if (!NEW_OBJECT_ID.equals(objectId))
      cas = CasesModuleBean.getPort(false).loadCase(objectId);
    else
    {
      cas = new Case();
      cas.setCaseTypeId(getBaseTypeInfo().getBaseTypeId());
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    cas = CasesModuleBean.getPort(false).storeCase(cas);
    setObjectId(cas.getCaseId());

    caseFinderBean.outdate();
  }

  @Override
  public void removeObject() throws Exception
  {
    CasesModuleBean.getPort(false).removeCase(cas.getCaseId());

    caseFinderBean.outdate();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[] { cas, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] array = (Object[])state;
    this.cas = (Case) array[0];
    this.formSelector = (String)array[1];
  }

}
