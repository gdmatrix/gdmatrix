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
package org.santfeliu.webapp.data;

import java.util.List;
import org.santfeliu.security.util.Credentials;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseManagerPort;
import org.santfeliu.webapp.modules.cases.CasesModuleBean;

/**
 *
 * @author blanquepa
 */
public class CaseDocumentsQuery extends DataQuery<CaseDocumentView>
{
  CaseDocumentFilter filter;

  public CaseDocumentsQuery(Credentials credentials)
  {
    super(credentials);
    filter = new CaseDocumentFilter();
  }

  public CaseDocumentsQuery caseId(String caseId)
  {
    filter.setCaseId(caseId);
    return this;
  }
  
  public CaseDocumentsQuery docId(String docId)
  {
    filter.setDocId(docId);
    return this;
  }  
  
  public CaseDocumentsQuery volume(String volume)
  {
    filter.setVolume(volume);
    return this;
  }  

  public CaseDocumentsQuery outputProperty(String propertyName)
  {
    filter.getOutputProperty().add(propertyName);
    return this;
  }  

  @Override
  public CaseDocumentsQuery firstResult(Number firstResult)
  {
    filter.setFirstResult(firstResult.intValue());
    return this;
  }
  
  @Override
  public CaseDocumentsQuery maxResults(Number maxResults)
  {
    filter.setMaxResults(maxResults.intValue());
    return this;
  }
  
  @Override
  public List<CaseDocumentView> execute()
  {          
    CaseManagerPort port = 
      CasesModuleBean.getPort(credentials.getUserId(), credentials.getPassword());
    return port.findCaseDocumentViews(filter);
  }

}
