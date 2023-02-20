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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.primefaces.PrimeFaces;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class CaseDocumentsTabBean extends TabBean
{
  Map<String, TabInstance> tabInstances = new HashMap<>();
  CaseDocument editing;

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseDocumentView> rows;
    int firstRow = 0;
  }

  @Inject
  CaseObjectBean caseObjectBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
  }

  public TabInstance getCurrentTabInstance()
  {
    EditTab editTab = caseObjectBean.getActiveEditTab();
    TabInstance tabInstance = tabInstances.get(editTab.getSubviewId());
    if (tabInstance == null)
    {
      tabInstance = new TabInstance();
      tabInstances.put(editTab.getSubviewId(), tabInstance);
    }
    return tabInstance;
  }

  @Override
  public String getObjectId()
  {
    return getCurrentTabInstance().objectId;
  }

  @Override
  public void setObjectId(String objectId)
  {
    getCurrentTabInstance().objectId = objectId;
  }

  @Override
  public boolean isNew()
  {
    return NEW_OBJECT_ID.equals(getCurrentTabInstance().objectId);
  }

  public List<CaseDocumentView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CaseDocumentView> caseDocumentViews)
  {
    getCurrentTabInstance().rows = caseDocumentViews;
  }

  public CaseDocument getEditing()
  {
    return editing;
  }

  public void setEditing(CaseDocument caseDocument)
  {
    editing = caseDocument;
  }

  public String getDocId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getDocId();
  }

  public void setDocId(String docId)
  {
    if (editing != null)
    {
      editing.setDocId(docId);
      showDialog();
    }
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
     getCurrentTabInstance().firstRow = firstRow;
  }

  @Override
  public void load()
  {
    String objectId = getObjectId();
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      try
      {
        CaseDocumentFilter filter = new CaseDocumentFilter();
        EditTab tab = caseObjectBean.getActiveEditTab();
        String volume = tab.getProperties().getString("volume");
        filter.setVolume(volume);
        filter.setCaseId(objectId);
        getCurrentTabInstance().rows =
          CasesModuleBean.getPort(false).findCaseDocumentViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      TabInstance tabInstance = getCurrentTabInstance();
      tabInstance.objectId = NEW_OBJECT_ID;
      tabInstance.rows = Collections.EMPTY_LIST;
      tabInstance.firstRow = 0;
    }
  }

  @Override
  public void store()
  {
    try
    {
      editing.setCaseId(getObjectId());
      if (editing.getCaseDocTypeId() == null)
      {
        editing.setCaseDocTypeId("CaseDocument");
      }
      CasesModuleBean.getPort(false).storeCaseDocument(editing);
      load();
      editing = null;
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
    editing = new CaseDocument();
  }

  public void edit(CaseDocumentView caseDocView)
  {
    if (caseDocView != null)
    {
      try
      {
        editing = CasesModuleBean.getPort(false)
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

  public void remove(CaseDocumentView caseDocView)
  {
    if (caseDocView != null)
    {
      try
      {
        CasesModuleBean.getPort(false)
          .removeCaseDocument(caseDocView.getCaseDocId());
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
      editing = (CaseDocument)stateArray[0];

      load();
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
      current.executeScript("PF('caseDocumentsDialog').show();");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
