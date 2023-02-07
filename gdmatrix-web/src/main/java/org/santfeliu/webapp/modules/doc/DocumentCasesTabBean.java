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
import org.santfeliu.webapp.modules.cases.*;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.FacesEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.Intervention;
import org.primefaces.PrimeFaces;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class DocumentCasesTabBean extends TabBean
{
  private List<CaseDocumentView> caseDocumentViews;
  private int firstRow;
  private CaseDocument caseDocument;

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

  public List<CaseDocumentView> getCaseDocumentViews()
  {
    return caseDocumentViews;
  }

  public void setCaseDocumentViews(List<CaseDocumentView> caseDocumentViews)
  {
    this.caseDocumentViews = caseDocumentViews;
  }

  public CaseDocument getCaseDocument()
  {
    return caseDocument;
  }

  public void setCaseDocument(CaseDocument caseDocument)
  {
    this.caseDocument = caseDocument;
  }

  public String getCaseId()
  {
    return caseDocument == null ? NEW_OBJECT_ID : caseDocument.getCaseId();
  }

  public void setCaseId(String caseId)
  {
    if (caseDocument != null)
    {
      caseDocument.setCaseId(caseId);
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

  @Override
  public void load()
  {
    System.out.println("load caseDocuments:" + objectId);
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      try
      {
        CaseDocumentFilter filter = new CaseDocumentFilter();
        filter.setDocId(objectId);
        caseDocumentViews =
          CasesModuleBean.getPort(false).findCaseDocumentViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else caseDocumentViews = Collections.EMPTY_LIST;
  }

  @Override
  public void store()
  {
    try
    {
      caseDocument.setDocId(objectId);
      if (caseDocument.getCaseDocTypeId() == null)
      {
        caseDocument.setCaseDocTypeId("CaseDocument");
      }
      CasesModuleBean.getPort(false).storeCaseDocument(caseDocument);
      load();
      caseDocument = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    info("CANCEL_OBJECT");
    caseDocument = null;
  }

  public void create()
  {
    caseDocument = new CaseDocument();
  }

  public void edit(CaseDocumentView caseDocView)
  {
    if (caseDocView != null)
    {
      try
      {
        caseDocument = CasesModuleBean.getPort(false)
          .loadCaseDocument(caseDocView.getCaseDocId());
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

  @Override
  public Serializable saveState()
  {
    return new Object[]{ caseDocument };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      caseDocument = (CaseDocument)stateArray[0];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void onTypeSelect(FacesEvent event)
  {
    System.out.println("onTypeSelect");
  }

  private void showDialog()
  {
    try
    {
      PrimeFaces current = PrimeFaces.current();
      current.executeScript("PF('documentCasesDialog').show();");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
