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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.santfeliu.security.util.Credentials;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CaseManagerPort;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.webapp.modules.cases.CasesModuleBean;

/**
 *
 * @author blanquepa
 */
public class CaseQuery extends DataQuery<Case>
{
  CaseFilter filter;

  public CaseQuery(Credentials credentials)
  {
    super(credentials);
    filter = new CaseFilter();
  }

  public CaseQuery caseId(String caseId)
  {
    filter.getCaseId().add(caseId);
    return this;
  }
  
  public CaseQuery caseTypeId(String caseTypeId)
  {
    filter.setCaseTypeId(caseTypeId);
    return this;
  }  

  public CaseQuery fromDate(String fromDate)
  {
    filter.setFromDate(fromDate);
    return this;
  }

  public CaseQuery toDate(String toDate)
  {
    filter.setToDate(toDate);
    return this;
  }
  
  public CaseQuery dateComparator(String dateComparator)
  {
    filter.setDateComparator(dateComparator);
    return this;
  }  
    
  public CaseQuery title(String title)
  {
    filter.setTitle(title);
    return this;
  }  
  
  @Override
  public DataQuery<Case> eq(String field, Object value)
  {
    Property property = new Property();
    property.setName(field);
    property.getValue().add(String.valueOf(value));
    filter.getProperty().add(property);
    return this;
  }
    
  @Override
  public CaseQuery orderBy(String field, boolean descending)
  {
    StringBuilder buffer = new StringBuilder();
    
    String searchExpression = filter.getSearchExpression();
    if (!StringUtils.isBlank(searchExpression))
    {
      buffer.append(searchExpression);
      boolean firstColumn = !searchExpression.contains("ORDER BY");

      if (!firstColumn)
        buffer.append(", ");
      else
        buffer.append(" ORDER BY ");
    }
    else
    {
      buffer.append("ORDER BY ");
    }
    
    
    if (field.startsWith("property[") && field.endsWith("]"))  
      field = field.replaceAll("property\\[|\\]", "");  
    
    if (field.contains(":"))
    {
      String[] parts = field.split(":");
      field = parts[0];
      if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
        descending = true;
    }
    
    buffer.append(field);
    if (descending)
      buffer.append(" desc ");  
    
    filter.setSearchExpression(buffer.toString());     
    
    return this;
  }
  
  public CaseQuery outputProperty(String propName)
  {
    filter.getOutputProperty().add(propName);
    return this;
  }

  @Override
  public CaseQuery firstResult(Number firstResult)
  {
    filter.setFirstResult(firstResult.intValue());
    return this;
  }
  
  @Override
  public CaseQuery maxResults(Number maxResults)
  {
    filter.setMaxResults(maxResults.intValue());
    return this;
  }
  
  @Override
  public List<Case> execute()
  {
    return execute(false);
  }
  public List<Case> execute(boolean runAsAdmin)
  {          
    //Set default filters
    String now = new SimpleDateFormat("yyyyMMdd").format(new Date());
    if (filter.getFromDate() == null)
      filter.setFromDate(now);
    if (filter.getToDate() == null)
      filter.setToDate(now);
    if (filter.getDateComparator() == null)
      filter.setDateComparator("3");
    
    String userId = runAsAdmin ? 
      MatrixConfig.getProperty("adminCredentials.userId") : 
      credentials.getUserId();
    String password = runAsAdmin ?
      password = MatrixConfig.getProperty("adminCredentials.password") :
      credentials.getPassword();
    
    CaseManagerPort port = CasesModuleBean.getPort(userId, password);
    
    return port.findCases(filter);
  }

  public Case load(String caseId)
  {
    CaseManagerPort port = 
      CasesModuleBean.getPort(credentials.getUserId(), credentials.getPassword());
    return port.loadCase(caseId);
  }
}
