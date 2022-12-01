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

import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.web.WebUtils;
import org.santfeliu.cases.web.CaseConfigBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author realor
 */
@Named("casePersonsTabBean")
@ViewScoped
public class CasePersonsTabBean extends TabBean
{
  private List<CasePersonView> casePersonViews;
  private String caseId;
  private int firstRow;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public List<CasePersonView> getCasePersonViews()
  {
    return casePersonViews;
  }

  public void setCasePersonViews(List<CasePersonView> casePersonViews)
  {
    this.casePersonViews = casePersonViews;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return WebUtils.getBacking("caseObjectBean");
  }

  @Override
  public void load()
  {
    if (casePersonViews == null || !getObjectId().equals(caseId))
    {
      caseId = getObjectId();

      System.out.println("load casePersons:" + caseId);
      if (!NEW_OBJECT_ID.equals(caseId))
      {
        try
        {
          CasePersonFilter filter = new CasePersonFilter();
          filter.setCaseId(caseId);
          casePersonViews = CaseConfigBean.getPort().findCasePersonViews(filter);
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
      else casePersonViews = Collections.EMPTY_LIST;
    }
  }
}
