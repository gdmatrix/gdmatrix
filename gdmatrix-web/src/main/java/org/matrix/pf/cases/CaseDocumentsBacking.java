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
package org.matrix.pf.cases;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Document;
import org.matrix.pf.doc.DocumentBacking;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.primefaces.model.TreeNode;
import org.santfeliu.cases.web.CaseConfigBean;

/**
 *
 * @author blanquepa
 */
@Named
public class CaseDocumentsBacking extends PageBacking 
  implements TypedTabPage, ResultListPage
{
  private static final String CASE_BACKING = "caseBacking";
  private static final String DOCUMENT_BACKING = "documentBacking";
    
  private static final String OUTCOME = "pf_case_documents";  
  
  private CaseBacking caseBacking;
  
  //Helpers
  private TypedHelper typedHelper;
  private ResultListHelper<CaseDocumentView> resultListHelper;
  private TabHelper tabHelper;
  
  private CaseDocument editing;
  
  private TreeNode<Document> root;
  
  
  public CaseDocumentsBacking()
  {
  }
  
  @PostConstruct
  public void init()
  {
    caseBacking = WebUtils.getBacking(CASE_BACKING);   
    typedHelper = new TypedHelper(this);
    resultListHelper = new ResultListHelper(this);
    tabHelper = new TabHelper(this);
    populate();
  }
  
  public CaseDocument getEditing()
  {
    return editing;
  }

  public void setEditing(CaseDocument editing)
  {
    this.editing = editing;
  } 
  
  public boolean isNew()
  {
    return isNew(editing);
  }
  
  @Override
  public String getPageObjectId()
  {
    if (editing != null)
      return editing.getCaseDocId();
    else
      return null;
  }


  public String getPageObjectDescription()
  {
    if (editing != null)
    {
      DocumentBacking addressBacking = WebUtils.getBacking(DOCUMENT_BACKING);
      return getDescription(addressBacking, editing.getDocId());
    }
    return null;
  }
  
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CASE_DOCUMENT_TYPE;
  }

  @Override
  public CaseBacking getObjectBacking()
  {
    return caseBacking;
  }
  
  @Override
  public String getTypeId()
  {
    return caseBacking.getPageTypeId();
  }
  
  @Override
  public ResultListHelper<CaseDocumentView> getResultListHelper()
  {
    return resultListHelper;
  }  
  
  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }  

  public TabHelper getTabHelper()
  {
    return tabHelper;
  }
  
  public List<CaseDocumentView> getRows()
  {
    return resultListHelper.getRows();
  }
  
  @Override
  public String show(String pageObjectId)
  {
    editDocument(pageObjectId);
    showDialog();
    return isEditing(pageObjectId) ? OUTCOME : show();
  }  
  
  @Override
  public String show()
  {
    populate();
    return OUTCOME;
  }
  
  public String editDocument(CaseDocumentView row)
  {
    String caseDocId = null;
    if (row != null)
      caseDocId = row.getCaseDocId();

    return editDocument(caseDocId);
  } 
  
  public String createDocument()
  {
    editing = new CaseDocument();
    return null;
  }  
  
  public String removeDocument(CaseDocumentView row)
  {
    return null;
  }  

  public String storeDocument()
  {
    return null;
  }
    

  @Override
  public List<CaseDocumentView> getResults(int firstResult, int maxResults)
  {
    try
    {
      CaseDocumentFilter filter = new CaseDocumentFilter();
      filter.setCaseId(caseBacking.getObjectId());        
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return CaseConfigBean.getPort().findCaseDocumentViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public String store()
  {
    return storeDocument();
  }
  
  @Override
  public void load()
  {
    resultListHelper.search();
  }

  @Override
  public void create()
  {
    editing = new CaseDocument();
  }
  
  @Override
  public String cancel()
  {
    editing = null;
    return null;
  }  
  
  @Override
  public void reset()
  {
    cancel();
    resultListHelper.reset();
  }
    
  private boolean isNew(CaseDocument caseDocument)
  {
    return (caseDocument != null && caseDocument.getCaseDocId() == null);
  }  
    
  private String editDocument(String caseDocId)
  {
    try
    {
      if (caseDocId != null && !isEditing(caseDocId))
      {
        editing = CaseConfigBean.getPort().loadCaseDocument(caseDocId);   
      }
      else if (caseDocId == null)
      {
        editing = new CaseDocument();
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }  

}
