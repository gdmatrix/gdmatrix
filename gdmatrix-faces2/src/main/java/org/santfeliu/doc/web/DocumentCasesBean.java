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
package org.santfeliu.doc.web;

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.cases.web.CaseBean;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author unknown
 */
public class DocumentCasesBean extends PageBean
{
  private List<CaseDocumentView> rows;
  private String editingCaseId;

  public DocumentCasesBean()
  {
    load();
  }

  public List<CaseDocumentView> getRows()
  {
    return rows;
  }

  public void setRows(List<CaseDocumentView> rows)
  {
    this.rows = rows;
  }

  public String getEditingCaseId()
  {
    return editingCaseId;
  }

  public void setEditingCaseId(String editingCaseId)
  {
    this.editingCaseId = editingCaseId;
  }

  public String show()
  {
    return "document_cases";
  }

  public String showDocumentCase()
  {
    return getControllerBean().showObject("Case",
      (String)getValue("#{row.caseObject.caseId}"));
  }

  public String searchCase()
  {
    return getControllerBean().searchObject("Case",
      "#{documentCasesBean.editingCaseId}");
  }

  public String storeDocumentCase()
  {
    try
    {
      String[] objectIdArray = DocumentConfigBean.fromObjectId(getObjectId());
      String docId = objectIdArray[0];
      CaseDocument caseDocument = new CaseDocument();
      caseDocument.setCaseId(editingCaseId);
      caseDocument.setDocId(docId);
      caseDocument.setCaseDocTypeId(DictionaryConstants.CASE_DOCUMENT_TYPE);
      CaseConfigBean.getPort().storeCaseDocument(caseDocument);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      editingCaseId = null;
      load();
    }
    return null;
  }

  public String removeDocumentCase()
  {
    try
    {
      CaseDocumentView row =
        (CaseDocumentView)getExternalContext().getRequestMap().get("row");
      CaseConfigBean.getPort().removeCaseDocument(row.getCaseDocId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public List<SelectItem> getDocumentCaseSelectItems()
  {
    CaseBean caseBean = (CaseBean)getBean("caseBean");
    return caseBean.getSelectItems(getEditingCaseId());
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        String[] objectIdArray = DocumentConfigBean.fromObjectId(getObjectId());
        String docId = objectIdArray[0];
        CaseDocumentFilter filter = new CaseDocumentFilter();
        filter.setDocId(docId);
        rows = CaseConfigBean.getPort().findCaseDocumentViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
