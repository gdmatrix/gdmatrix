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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.convert.TypeIdConverter;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.util.ColumnDefinition;
import org.santfeliu.web.obj.util.ResultsManager;

/**
 *
 * @author blanquepa
 */
public class PersonsDetailPanel extends TabulatedDetailPanel
{
  public static final String ALLOWED_TYPEIDS_PROPERTY = "allowedTypeIds";
  public static final String FORBIDDEN_TYPEIDS_PROPERTY = "forbiddenTypeIds";
  public static final String INCLUDE_FORMER_PERSONS = "includeFormerPersons";
  
  protected List<CasePersonView> casePersons;
  private ResultsManager resultsManager;

  public PersonsDetailPanel()
  {
    resultsManager =
      new ResultsManager(
        "org.santfeliu.cases.web.resources.CaseBundle", "casePersons_");

    resultsManager.addDefaultColumn("personView.fullName");
    ColumnDefinition typeIdColDef = new ColumnDefinition("casePersonTypeId");
    typeIdColDef.setConverter(new TypeIdConverter());
    resultsManager.addDefaultColumn(typeIdColDef);
  }

  @Override
  public void loadData(DetailBean detailBean)
  {
    resultsManager.setColumns(getMid());
    try
    {
      casePersons = new ArrayList();
      String caseId = ((CaseDetailBean) detailBean).getCaseId();
      CasePersonFilter filter = new CasePersonFilter();
      filter.setCaseId(caseId);
      List<CasePersonView> persons =
        CaseConfigBean.getPort().findCasePersonViews(filter);
      for (CasePersonView pers : persons)
      {
        if (isAllowedTypeId(pers.getCasePersonTypeId()) &&
          (!isFormerPerson(pers) || (isFormerPerson(pers) && isIncludeFormerPersons())))
        {
          casePersons.add(pers);
        }
      }

      List<String> orderBy = getMultivaluedProperty(ResultsManager.ORDERBY);
      if (orderBy != null && !orderBy.isEmpty())
        resultsManager.sort(casePersons, orderBy);
      else  //Default sorting
      {
        Collections.sort(casePersons, new Comparator() {
          public int compare(Object o1, Object o2)
          {
            CasePersonView p1 = (CasePersonView)o1;
            CasePersonView p2 = (CasePersonView)o2;
            if (isFormerPerson(p1) && !isFormerPerson(p2))
              return 1;
            else if (!isFormerPerson(p1) && isFormerPerson(p2))
              return -1;
            else
            {
              return p1.getPersonView().getFullName().compareTo(p2.getPersonView().getFullName());
            }
          }
        });
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public List<CasePersonView> getCasePersons()
  {
    return casePersons;
  }

  public void setCasePersons(List<CasePersonView> casePersons)
  {
    this.casePersons = casePersons;
  }

  @Override
  public boolean isRenderContent()
  {
    return (casePersons != null && !casePersons.isEmpty());
  }

  @Override
  public String getType()
  {
    return "persons";
  }

  public ResultsManager getResultsManager()
  {
    return resultsManager;
  }

  public void setResultsManager(ResultsManager resultsManager)
  {
    this.resultsManager = resultsManager;
  }

  public boolean isFormerPerson()
  {
    CasePersonView casePerson = (CasePersonView)getValue("#{row}");
    return isFormerPerson(casePerson);
  }

  public String sort()
  {
    resultsManager.sort(casePersons);
    return null;
  }

  private boolean isFormerPerson(CasePersonView casePerson)
  {
    String startDate = casePerson.getStartDate();
    String endDate = casePerson.getEndDate();
    String now = TextUtils.formatDate(new Date(), "yyyyMMdd");
    if (startDate != null && now.compareTo(startDate) < 0) return true;
    if (endDate != null && now.compareTo(endDate) > 0) return true;
    return false;
  }

  private List<String> getAllowedTypeIds()
  {
    return getMultivaluedProperty(ALLOWED_TYPEIDS_PROPERTY);
  }

  private List<String> getForbiddenTypeIds()
  {
    return getMultivaluedProperty(FORBIDDEN_TYPEIDS_PROPERTY);
  }

  private boolean isIncludeFormerPersons()
  {
    String showClosed = getProperty(INCLUDE_FORMER_PERSONS);
    return (showClosed != null && showClosed.equalsIgnoreCase("true"));
  }

  private boolean isAllowedTypeId(String typeId)
  {
    return (getAllowedTypeIds().isEmpty() || isDerivedFrom(getAllowedTypeIds(), typeId)) &&
      (getForbiddenTypeIds().isEmpty() || !isDerivedFrom(getForbiddenTypeIds(), typeId));
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
}
