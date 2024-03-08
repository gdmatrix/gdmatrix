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
package org.santfeliu.webapp.modules.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.report.Report;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import static org.santfeliu.webapp.modules.report.ReportModuleBean.getPort;
import org.santfeliu.webapp.setup.ObjectSetup;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class ReportTypeBean extends TypeBean<Report, DocumentFilter>
{
  public static final String REPORT_DOC_TYPE = "REPORT";  

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.REPORT_TYPE;
  }

  @Override
  public String getObjectId(Report report)
  {
    return report.getReportId();
  }

  @Override
  public String getTypeId(Report report)
  {
    return report.getDocTypeId();
  }

  @Override
  public String describe(Report report)
  {
    return report.getTitle();
  }

  @Override
  public Report loadObject(String objectId)
  {
    try
    {
      return getPort().loadReport(objectId, false);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/report/report.xhtml");
       
    return objectSetup;
  }

  @Override
  public DocumentFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";

    DocumentFilter filter = new DocumentFilter();
    if (query.matches(".{0,4}[0-9]+"))
    {
      filter.getDocId().add(query);
    }
    if (typeId != null)
    {
      filter.setDocTypeId(REPORT_DOC_TYPE);
    }
    filter.setMaxResults(10);

    return filter;
  }

  @Override
  public String filterToQuery(DocumentFilter filter)
  {
    if (!filter.getDocId().isEmpty())
    {
      return filter.getDocId().get(0);
    }
   return "";
  }

  @Override
  public List<Report> find(DocumentFilter filter)
  {
    List<Report> reports = new ArrayList();
    try
    {
      List<Document> docs = DocModuleBean.getPort(false).findDocuments(filter);
      //TODO docs -> reports
      return reports;
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  
}
