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
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.web.WebUtils;
import org.santfeliu.cases.web.CaseConfigBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author realor
 */
@Named("caseDocumentsTabBean")
@SessionScoped
public class CaseDocumentsTabBean extends TabBean
{
  private List<CaseDocumentView> caseDocumentViews;
  private String caseId;
  private int firstRow;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public List<CaseDocumentView> getCaseDocumentViews()
  {
    return caseDocumentViews;
  }

  public void setCaseDocumentViews(List<CaseDocumentView> caseDocumentViews)
  {
    this.caseDocumentViews = caseDocumentViews;
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
    if (caseDocumentViews == null || !getObjectId().equals(caseId))
    {
      caseId = getObjectId();

      System.out.println("load caseDocuments:" + caseId);
      if (!NEW_OBJECT_ID.equals(caseId))
      {
        try
        {
          CaseDocumentFilter filter = new CaseDocumentFilter();
          filter.setCaseId(caseId);
          caseDocumentViews = CaseConfigBean.getPort().findCaseDocumentViews(filter);
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
      else caseDocumentViews = Collections.EMPTY_LIST;
    }
  }
}
