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

import java.util.List;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.dic.Property;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.DetailPanel;

/**
 *
 * @author blanquepa
 */
public class BrowserDetailPanel extends DetailPanel
{
  public static final String CASE_DOCUMENT_TYPE_PROPERTY =
    "caseDocumentTypeId";
  public static final String PROPERTY_NAME =
    "propertyName";
  public static final String PROPERTY_VALUE =
    "propertyValue";

  private String url;

  @Override
  public void loadData(DetailBean detailBean)
  {
    try
    {
      String caseId = ((CaseDetailBean) detailBean).getCaseId();
      CaseDocumentFilter filter = new CaseDocumentFilter();
      filter.setCaseId(caseId);
      List<CaseDocumentView> caseDocuments =
        CaseConfigBean.getPort().findCaseDocumentViews(filter);
      //Search for specific CaseDocumentType
      for (CaseDocumentView caseDocument : caseDocuments)
      {
        if (getCaseDocumentTypeIds().contains(caseDocument.getCaseDocTypeId()))
          url = "/documents/" + caseDocument.getDocument().getDocId();
      }

      //If not type found, search properties in document
      String propertyName = getProperty(PROPERTY_NAME);
      String propertyValue = getProperty(PROPERTY_VALUE);
      if (url == null && propertyName != null && propertyValue != null)
      {
        for (CaseDocumentView caseDocument : caseDocuments)
        {
          Document document = DocumentConfigBean.getClient().loadDocument(
            caseDocument.getDocument().getDocId(), 0, ContentInfo.METADATA);

          Property property = DictionaryUtils.getProperty(document, propertyName);
          if (property != null && property.getValue() != null &&
            property.getValue().contains(propertyValue))
          {
            url = "/documents/" + caseDocument.getDocument().getDocId();
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public List<String> getCaseDocumentTypeIds()
  {
    return getMultivaluedProperty(CASE_DOCUMENT_TYPE_PROPERTY);
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  @Override
  public boolean isRenderContent()
  {
    return getUrl() != null;
  }

  @Override
  public String getType()
  {
    return "browser";
  }
}
