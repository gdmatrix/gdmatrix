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

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.web.WebUtils;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.util.TextUtils;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author realor
 */
@Named
public class CaseMainTabBean extends TabBean
{
  private Case cas;
  private String caseId;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public Case getCase()
  {
    if (cas == null || !getObjectId().equals(caseId))
    {
      load();
    }
    return cas;
  }

  public void setCase(Case cas)
  {
    this.cas = cas;
  }

  public Date getStartDateTime()
  {
    if (cas != null && cas.getStartDate() != null)
      return getDate(cas.getStartDate(), cas.getStartTime());
    else
      return null;
  }

  public Date getEndDateTime()
  {
    if (cas != null && cas.getEndDate() != null)
      return getDate(cas.getEndDate(), cas.getEndTime());
    else
      return null;
  }

  public void setStartDateTime(Date date)
  {
    if (cas != null)
    {
      if (date == null)
        date = new Date();
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

  private Date getDate(String date, String time)
  {
    String dateTime = TextUtils.concatDateAndTime(date, time);
    return TextUtils.parseInternalDate(dateTime);
  }

  @Override
  public CaseObjectBean getObjectBean()
  {
    return WebUtils.getBacking("caseObjectBean");
  }

  public void save()
  {
    System.out.println(cas.getTitle());
    info("Saved.");
  }

  private void load()
  {
    caseId = getObjectId();
    if (!NEW_OBJECT_ID.equals(caseId))
    {
      try
      {
        cas = CaseConfigBean.getPort().loadCase(caseId);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else cas = new Case();
  }
}
