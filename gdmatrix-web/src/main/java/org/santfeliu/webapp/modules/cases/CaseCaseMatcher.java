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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.matrix.cases.Case;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.cases.CaseCaseView;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class CaseCaseMatcher
{

  private final Case mainCase;
  private final Map<String, List<Period>> periods = new HashMap<>();
  private final TreeMap<String, CaseCaseView> results = new TreeMap<>();

  public CaseCaseMatcher(Case mainCase)
  {
    this.mainCase = mainCase;
  }

  public List<CaseCaseView> matchByPersons(String cpTypeId1,
    String cpTypeId2) throws Exception
  {
    return matchByPersons(mainCase.getCaseId(), cpTypeId1, cpTypeId2);
  }

  public List<CaseCaseView> matchByPersons(String caseId, String cpTypeId1,
    String cpTypeId2) throws Exception
  {
    CasePersonFilter filter = new CasePersonFilter();
    filter.setCaseId(caseId);
    filter.setCasePersonTypeId(cpTypeId1);
    List<CasePersonView> casePersons
      = CasesModuleBean.getPort(false).findCasePersonViews(filter);

    for (CasePersonView casePerson : casePersons)
    {
      storePeriods(casePerson.getPersonView().getPersonId(),
        casePerson.getStartDate(), casePerson.getEndDate());
    }

    for (String personId : getIds())
    {
      filter = new CasePersonFilter();
      filter.setPersonId(personId);
      filter.setCasePersonTypeId(cpTypeId2);
      casePersons
        = CasesModuleBean.getPort(false).findCasePersonViews(filter);
      compare(casePersons);
    }

    return getResults();
  }

  public List<CaseCaseView> matchByAddresses(String caseId) throws Exception
  {
    CaseAddressFilter filter = new CaseAddressFilter();
    filter.setCaseId(caseId);
    List<CaseAddressView> caseAddresses
      = CasesModuleBean.getPort(false).findCaseAddressViews(filter);

    for (CaseAddressView caseAddress : caseAddresses)
    {
      storePeriods(caseAddress.getAddressView().getAddressId(),
        caseAddress.getStartDate(), caseAddress.getEndDate());
    }

    for (String addressId : getIds())
    {
      filter = new CaseAddressFilter();
      filter.setAddressId(addressId);
      caseAddresses
        = CasesModuleBean.getPort(false).findCaseAddressViews(filter);
      compare(caseAddresses);
    }

    return getResults();
  }

  private List<CaseCaseView> getResults()
  {
    List<CaseCaseView> list = new ArrayList<>();
    list.addAll(results.values());
    return list;
  }

  private Set<String> getIds()
  {
    return periods.keySet();
  }

  private void compare(List items)
  {
    for (Object item : items)
    {
      if (item instanceof CaseAddressView)
      {
        addMatch((CaseAddressView) item);
      } else if (item instanceof CasePersonView)
      {
        addMatch((CasePersonView) item);
      }
    }
  }

  private void addMatch(CasePersonView casePerson)
  {
    String personId = casePerson.getPersonView().getPersonId();
    String casePersonId = casePerson.getCasePersonId();
    CaseCaseView item = results.get(casePersonId);
    if (item == null)
    {
      Case relCase = casePerson.getCaseObject();
      item = createCaseCaseView(casePerson.getPersonView().getPersonId(),
        casePerson.getStartDate(), casePerson.getEndDate(), relCase,
        relCase.getCaseTypeId());
      if (item != null)
      {
        item.setRelCase(relCase);
        item.setCaseCaseTypeId(relCase.getCaseTypeId());
        DictionaryUtils.setProperty(item, "personId", personId);
        results.put(casePersonId, item);
      }
    }
  }

  private void addMatch(CaseAddressView caseAddress)
  {
    String addressId = caseAddress.getAddressView().getAddressId();
    String caseAddressId = caseAddress.getCaseAddressId();
    CaseCaseView caseCaseView = results.get(caseAddressId);
    if (caseCaseView == null)
    {
      Case relCase = caseAddress.getCaseObject();
      caseCaseView
        = createCaseCaseView(caseAddress.getAddressView().getAddressId(),
          caseAddress.getStartDate(), caseAddress.getEndDate(),
          relCase, relCase.getCaseTypeId());
      if (caseCaseView != null)
      {
        DictionaryUtils.setProperty(caseCaseView, "addressId", addressId);
        results.put(caseAddressId, caseCaseView);
      }
    }
  }

  private CaseCaseView createCaseCaseView(String id, String startDate,
    String endDate, Case relCase, String caseCaseTypeId)
  {
    Period p = isWithinRange(id, startDate, endDate);
    if (p != null)
    {
      CaseCaseView caseCaseView = new CaseCaseView();
      caseCaseView.setMainCase(mainCase);
      caseCaseView.setStartDate(p.getFormattedStartDate());
      caseCaseView.setEndDate(p.getFormattedEndDate());
      caseCaseView.setRelCase(relCase);
      caseCaseView.setCaseCaseTypeId(caseCaseTypeId);
      return caseCaseView;
    }
    return null;
  }

  private Period isWithinRange(String id, String startDate, String endDate)
  {
    List<Period> list = periods.get(id);
    if (list != null && !list.isEmpty())
    {
      for (Period period : list)
      {
        Period p = period.isWithinRange(startDate, endDate);
        if (p != null)
        {
          return p;
        }
      }
    }
    return null;
  }

  private void storePeriods(String id, String startDate, String endDate)
  {
    Period period = new Period(startDate, endDate);
    List<Period> list = periods.get(id);
    if (list == null)
    {
      list = new ArrayList<>();
    }
    list.add(period);
    periods.put(id, list);
  }

  private class Period
  {

    private Date startDate;
    private Date endDate;

    public Period(String startDate, String endDate)
    {
      if (startDate != null)
      {
        this.startDate = TextUtils.parseInternalDate(startDate);
      }
      if (endDate != null)
      {
        this.endDate = TextUtils.parseInternalDate(endDate);
      }
    }

    public Date getStartDate()
    {
      return startDate;
    }

    public void setStartDate(Date startDate)
    {
      this.startDate = startDate;
    }

    public Date getEndDate()
    {
      return endDate;
    }

    public void setEndDate(Date endDate)
    {
      this.endDate = endDate;
    }

    public String getFormattedStartDate()
    {
      return TextUtils.formatDate(startDate, "yyyyMMdd");
    }

    public String getFormattedEndDate()
    {
      return TextUtils.formatDate(endDate, "yyyyMMdd");
    }

    public Period isWithinRange(String sDate, String eDate)
    {
      Date cpStart = TextUtils.parseInternalDate(sDate);
      Date cpEnd = TextUtils.parseInternalDate(eDate);

      boolean sdBtw = startDate == null
        || cpEnd == null
        || startDate.getTime() <= cpEnd.getTime();
      boolean edBtw = endDate == null
        || cpStart == null
        || endDate.getTime() >= cpStart.getTime();

      if (sdBtw && edBtw)
      {
        return getMergedPeriod(sDate, eDate);
      } else
      {
        return null;
      }
    }

    private Period getMergedPeriod(String startDate, String endDate)
    {
      Period result = new Period(startDate, endDate);

      if (this.startDate != null)
      {
        if (result.getStartDate() == null
          || result.getStartDate().getTime() <= this.startDate.getTime())
        {
          result.setStartDate(this.startDate);
        }
      }

      if (this.endDate != null)
      {
        if (result.getEndDate() == null
          || result.getEndDate().getTime() > this.endDate.getTime())
        {
          result.setEndDate(this.endDate);
        }
      }

      return result;
    }
  }
}
