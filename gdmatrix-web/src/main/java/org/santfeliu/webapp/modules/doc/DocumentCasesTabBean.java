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
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.setup.TableProperty;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class DocumentCasesTabBean extends TabBean
{
  private List<CaseDocumentView> rows;
  private int firstRow;
  private CaseDocument editing;
  private GroupableRowsHelper groupableRowsHelper;  

  @Inject
  DocumentObjectBean documentObjectBean;

  @Inject
  TypeTypeBean typeTypeBean;  
  
  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return DocumentCasesTabBean.this.getObjectBean();
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return Collections.EMPTY_LIST;        
      }

      @Override
      public void sortRows()
      {
      }

      @Override
      public String getRowTypeColumnName()
      {
        return "caseDocTypeId";
      }
      
      @Override
      public String getFixedColumnValue(Object row, String columnName)
      {
        CaseDocumentView caseDocumentView = (CaseDocumentView)row;
        if ("caseId".equals(columnName))
        {
          return caseDocumentView.getCaseObject().getCaseId();
        }
        else if ("caseTitle".equals(columnName))
        {
          return caseDocumentView.getCaseObject().getTitle();
        }
        else if ("caseTypeId".equals(columnName))
        {
          return typeTypeBean.getDescription(
            caseDocumentView.getCaseObject().getCaseTypeId());
        }
        else
        {
          return null;
        }
      }      
    };
  }

  public GroupableRowsHelper getGroupableRowsHelper()
  {
    return groupableRowsHelper;
  }

  public void setGroupableRowsHelper(GroupableRowsHelper groupableRowsHelper)
  {
    this.groupableRowsHelper = groupableRowsHelper;
  }  
  
  @Override
  public ObjectBean getObjectBean()
  {
    return documentObjectBean;
  }

  public List<CaseDocumentView> getRows()
  {
    return rows;
  }

  public void setRows(List<CaseDocumentView> caseDocumentViews)
  {
    this.rows = caseDocumentViews;
  }

  public CaseDocument getEditing()
  {
    return editing;
  }

  public void setEditing(CaseDocument caseDocument)
  {
    this.editing = caseDocument;
  }

  public String getCaseId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getCaseId();
  }

  public void setCaseId(String caseId)
  {
    if (editing != null)
    {
      editing.setCaseId(caseId);
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
    System.out.println("load caseDocuments:" + getObjectId());
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        CaseDocumentFilter filter = new CaseDocumentFilter();
        filter.setDocId(getObjectId());
        rows =
          CasesModuleBean.getPort(false).findCaseDocumentViews(filter);
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
      editing.setDocId(getObjectId());
      if (editing.getCaseDocTypeId() == null)
      {
        editing.setCaseDocTypeId("CaseDocument");
      }
      CasesModuleBean.getPort(false).storeCaseDocument(editing);
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

  public void onTypeSelect(FacesEvent event)
  {
    System.out.println("onTypeSelect");
  }

}
