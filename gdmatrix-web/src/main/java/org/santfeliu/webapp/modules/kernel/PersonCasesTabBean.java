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
package org.santfeliu.webapp.modules.kernel;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.ResultListHelper;
import org.santfeliu.webapp.modules.cases.CaseObjectBean;
import org.santfeliu.webapp.modules.cases.CasesModuleBean;

/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class PersonCasesTabBean extends TabBean
{
  @Inject
  private PersonObjectBean personObjectBean;

  @Inject
  private CaseObjectBean caseObjectBean;

  //Helpers
  private ResultListHelper<CasePersonView> resultListHelper;

  private int firstRow;

  public PersonCasesTabBean()
  {
  }

  @PostConstruct
  public void init()
  {
    resultListHelper = new CasePersonResultListHelper();
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return personObjectBean;
  }

  public ResultListHelper<CasePersonView> getResultListHelper()
  {
    return resultListHelper;
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
  public void load()
  {
    resultListHelper.find();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{};
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      if (!isNew()) resultListHelper.find();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private class CasePersonResultListHelper extends
    ResultListHelper<CasePersonView>
  {
    @Override
    public List<CasePersonView> getResults(int firstResult, int maxResults)
    {
      try
      {
        CasePersonFilter filter = new CasePersonFilter();
        filter.setPersonId(personObjectBean.getObjectId());
        filter.setFirstResult(firstResult);
        filter.setMaxResults(maxResults);
        return CasesModuleBean.getPort(false).findCasePersonViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      return null;
    }
  }

}
