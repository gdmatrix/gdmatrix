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
package org.santfeliu.cases.web.detail;

import java.util.ArrayList;
import java.util.List;
import org.matrix.cases.Case;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.util.FileSizeComparator;
import org.santfeliu.doc.web.DocumentBean;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.faces.convert.FileSizeConverter;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.util.ResultsManager;
import org.santfeliu.web.obj.util.ColumnDefinition;

/**
 *
 * @author blanquepa
 */
public class DocumentsDetailPanel extends TabulatedDetailPanel
{
  public static final String ALLOWED_TYPEIDS_PROPERTY = "allowedTypeIds";
  public static final String FORBIDDEN_TYPEIDS_PROPERTY = "forbiddenTypeIds";
  public static final String RENDEREXTENSION_PROPERTY = "renderExtensions";

  private List<CaseDocumentView> caseDocuments;
  private ResultsManager resultsManager;

  public DocumentsDetailPanel()
  {
    resultsManager =
      new ResultsManager(
        "org.santfeliu.cases.web.resources.CaseBundle", "caseDocuments_");

    resultsManager.addDefaultColumn("document.title");

    ColumnDefinition sizeColDef = new ColumnDefinition("document.content.size");
    sizeColDef.setConverter(new FileSizeConverter());
    sizeColDef.setComparator(new FileSizeComparator());
    resultsManager.addDefaultColumn(sizeColDef);
  }

  @Override
  public void loadData(DetailBean detailBean)
  {
    resultsManager.setColumns(getMid());
    caseDocuments = new ArrayList();
    try
    {
      String caseId = ((CaseDetailBean) detailBean).getCaseId();
      CaseDocumentFilter filter = new CaseDocumentFilter();
      filter.setCaseId(caseId);
      List<CaseDocumentView> docs =
        CaseConfigBean.getPort().findCaseDocumentViews(filter);
      for (CaseDocumentView doc : docs)
      {
        String docTypeId = doc.getDocument().getDocTypeId();
        String caseDocTypeId = doc.getCaseDocTypeId();
        if (isAllowedTypeId(docTypeId) && isAllowedTypeId(caseDocTypeId))
        {
          List<String> columnNames = resultsManager.getColumnNames();
          for (String columnName : columnNames)
          {
            if (columnName.contains("document.property[") && columnName.contains("]"))
            {
              String docId = doc.getDocument().getDocId();
              Document document =
                DocumentConfigBean.getPort().loadDocument(docId, 0, ContentInfo.METADATA);
              doc.setDocument(document);
            }
            else if (columnName.contains("caseObject.property[") && columnName.contains("]"))
            {
              Case cas = CaseConfigBean.getPort().loadCase(caseId);
              doc.setCaseObject(cas);
            }
          }
          caseDocuments.add(doc);
        }
      }

      List<String> orderBy = getMultivaluedProperty(ResultsManager.ORDERBY);
      if (orderBy != null && !orderBy.isEmpty())
        resultsManager.sort(caseDocuments, orderBy);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public ResultsManager getResultsManager()
  {
    return resultsManager;
  }

  public void setResultsManager(ResultsManager resultsManager)
  {
    this.resultsManager = resultsManager;
  }

  public String getFileTypeImage()
  {
    CaseDocumentView caseDocumentView = (CaseDocumentView)getValue("#{row}");
    Document document = caseDocumentView.getDocument();
    if (document != null)
    {
      Content content = document.getContent();
      if (content != null)
      {
        String contentType = content.getContentType();
        if (contentType.startsWith("image"))
          return "/documents/" + content.getContentId() + "/" +
            DocumentUtils.getFilename(document.getTitle(), contentType);
        else
          return DocumentBean.getContentTypeIcon(content.getContentType());
      }
      else
      {
        return DocumentBean.getContentTypeIcon(null);
      }
    }
    else
      return "";
  }

  public String getDocumentSize()
  {
    CaseDocumentView caseDocumentView = (CaseDocumentView)getValue("#{row}");
    Document document = caseDocumentView.getDocument();

    return DocumentUtils.getSizeString(document.getContent().getSize());
  }
  
  public String getExtension()
  {
    CaseDocumentView caseDocumentView = (CaseDocumentView)getValue("#{row}");
    Document document = caseDocumentView.getDocument();
    if (document != null)
    {
      Content content = document.getContent();
      String extension = 
        MimeTypeMap.getMimeTypeMap().getExtension(content.getContentType());
      return extension;
    }
    return "";
  }  

  public List<CaseDocumentView> getCaseDocuments()
  {
    return caseDocuments;
  }

  public void setCaseDocuments(List<CaseDocumentView> caseDocuments)
  {
    this.caseDocuments = caseDocuments;
  }

  @Override
  public boolean isRenderContent()
  {
    return (caseDocuments != null && !caseDocuments.isEmpty());
  }

  @Override
  public String getType()
  {
    return "documents";
  }

  private List<String> getAllowedDocumentTypeIds()
  {
    return getMultivaluedProperty(ALLOWED_TYPEIDS_PROPERTY);
  }

  private List<String> getForbiddenDocumentTypeIds()
  {
    return getMultivaluedProperty(FORBIDDEN_TYPEIDS_PROPERTY);
  }

  private boolean isAllowedTypeId(String typeId)
  {
    return (getAllowedDocumentTypeIds().isEmpty() || isDerivedFrom(getAllowedDocumentTypeIds(), typeId)) &&
      (getForbiddenDocumentTypeIds().isEmpty() || !isDerivedFrom(getForbiddenDocumentTypeIds(), typeId));
  }
  
  private boolean isDerivedFrom(List<String> typeIds, String typeId)
  {
    if (typeId == null)
      return false;
    
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
    {
      for (String allowedTypeId : typeIds)
      {
        if (type.isDerivedFrom(allowedTypeId))
          return true;
      }
    }    
    return false;
  }  
  
  public boolean isExtensionRender()
  {
    return "true".equalsIgnoreCase(getProperty(RENDEREXTENSION_PROPERTY));
  }  

  public String sort()
  {
    resultsManager.sort(caseDocuments);
    return null;
  }
}
