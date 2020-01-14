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
package org.santfeliu.presence.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.presence.AbsenceCounter;
import org.matrix.presence.AbsenceCounterFilter;
import org.matrix.presence.AbsenceCounterView;
import org.matrix.presence.AbsenceCounting;
import org.matrix.presence.AbsenceType;
import org.matrix.presence.AbsenceTypeFilter;
import org.matrix.presence.PresenceManagerPort;
import org.matrix.presence.Worker;
import org.santfeliu.faces.beansaver.Savable;
import static org.santfeliu.presence.web.PresenceConfigBean.getPresencePort;
import org.santfeliu.web.WebBean;

/**
 *
 * @author realor
 */
public class AbsenceCountersBean extends WebBean implements Savable
{
  private int year;
  private List<AbsenceCounterView> absenceCounterViews;
  private AbsenceCounterView editingAbsenceCounterView;
  private String absenceTypeId;
  
  public AbsenceCountersBean()
  {
    currentYear();    
  }
  
  public int getYear()
  {
    return year;
  }
  
  public String show()
  {
    return "absence_counters";
  }

  public String update()
  {
    absenceCounterViews = null;
    return show();
  }

  public String getAbsenceTypeId()
  {
    return absenceTypeId;
  }

  public void setAbsenceTypeId(String absenceTypeId)
  {
    this.absenceTypeId = absenceTypeId;
  }
  
  public AbsenceCounterView getEditingAbsenceCounterView()
  {
    return editingAbsenceCounterView;
  }

  public List<AbsenceCounterView> getAbsenceCounterViews()
  {
    try
    {
      if (absenceCounterViews == null)
      {
        AbsenceCounterFilter filter = new AbsenceCounterFilter();
        String personId = getWorker().getPersonId();
        filter.setYear(String.valueOf(year));
        filter.setPersonId(personId);
        PresenceManagerPort port = PresenceConfigBean.getPresencePort();
        absenceCounterViews = port.findAbsenceCounterViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return absenceCounterViews;
  }
  
  public String getCounterCounting()
  {
    AbsenceCounting counting = (AbsenceCounting)getValue("#{absenceCounterView.absenceType.counting}");
    return PresenceConfigBean.getInstance().getAbsenceCountingLabel(counting);
  }

  public void editAbsenceCounter()
  {
    AbsenceCounterView absenceCounterView = 
      (AbsenceCounterView)getValue("#{absenceCounterView}");
    editingAbsenceCounterView = absenceCounterView;
  }
  
  public void removeAbsenceCounter()
  {
    try
    {
      AbsenceCounter absenceCounter = 
        (AbsenceCounter)getValue("#{absenceCounterView.absenceCounter}");
      PresenceManagerPort port = PresenceConfigBean.getPresencePort();
      port.removeAbsenceCounter(absenceCounter.getAbsenceCounterId());
      absenceCounterViews = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void storeAbsenceCounter()
  {
    try
    {
      AbsenceCounter absenceCounter = 
        (AbsenceCounter)getValue("#{absenceCounterView.absenceCounter}");
      PresenceManagerPort port = PresenceConfigBean.getPresencePort();
      port.storeAbsenceCounter(absenceCounter);
      absenceCounterViews = null;
      editingAbsenceCounterView = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelAbsenceCounter()
  {
    absenceCounterViews = null;
    editingAbsenceCounterView.setAbsenceCounter(null);
    editingAbsenceCounterView = null;
  }
  
  public void moveAbsenceCounter()
  {
    try
    {
      AbsenceCounterView absenceCounterView = 
        (AbsenceCounterView)getValue("#{absenceCounterView}");
      AbsenceCounter absenceCounter = absenceCounterView.getAbsenceCounter();
      if (absenceCounter.getRemainingTime() > 0)
      {
        PresenceManagerPort port = PresenceConfigBean.getPresencePort();
        double remainingTime = absenceCounter.getRemainingTime();        
        AbsenceCounterFilter filter = new AbsenceCounterFilter();
        filter.setAbsenceTypeId(absenceCounter.getAbsenceTypeId());
        int nextYear = Integer.parseInt(absenceCounter.getYear()) + 1;
        filter.setYear(String.valueOf(nextYear));
        filter.setPersonId(getWorker().getPersonId());
        List<AbsenceCounter> absenceCounters = port.findAbsenceCounters(filter);
        if (!absenceCounters.isEmpty())
        {
          absenceCounter.setRemainingTime(0);
          port.storeAbsenceCounter(absenceCounter);
          
          absenceCounter = absenceCounters.get(0);
          absenceCounter.setRemainingTime(absenceCounter.getRemainingTime() + remainingTime);
          port.storeAbsenceCounter(absenceCounter);
          absenceCounterViews = null;
          message("org.santfeliu.presence.web.resources.PresenceBundle", 
            "counterMoved", null, FacesMessage.SEVERITY_INFO);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void createAbsenceCounter()
  {
    try
    {
      System.out.println(">> " + absenceTypeId);
      if (!StringUtils.isBlank(absenceTypeId))
      {
        PresenceManagerPort port = PresenceConfigBean.getPresencePort();
        AbsenceCounter absenceCounter = new AbsenceCounter();
        absenceCounter.setPersonId(getWorker().getPersonId());
        absenceCounter.setYear(String.valueOf(year));
        absenceCounter.setAbsenceTypeId(absenceTypeId);
        port.storeAbsenceCounter(absenceCounter);
        absenceCounterViews = null;
        absenceTypeId = null;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void createAbsenceCounters()
  {
    createAbsenceCounters(false);
  }

  public void createAllAbsenceCounters()
  {
    createAbsenceCounters(true);
  }

  public void createAbsenceCounters(boolean zeroCounters)
  {
    try
    {
      PresenceManagerPort port = PresenceConfigBean.getPresencePort();
      int created = port.createAbsenceCounters(getWorker().getPersonId(), 
        String.valueOf(year), zeroCounters);
      message("org.santfeliu.presence.web.resources.PresenceBundle", 
        "createdCounters", new Object[]{created}, FacesMessage.SEVERITY_INFO);
      if (created > 0)
      {
        absenceCounterViews = null;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public List<SelectItem> getAbsenceTypeSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>();
    try
    {
      List<AbsenceType> absenceTypes;
      PresenceManagerPort port = getPresencePort();
      AbsenceTypeFilter filter = new AbsenceTypeFilter();
      absenceTypes = port.findAbsenceTypes(filter);
      for (AbsenceType absenceType : absenceTypes)
      {
        if (absenceType.getDefaultTime() >= 0)
        {
          SelectItem selectItem = new SelectItem();
          selectItem.setLabel(absenceType.getLabel());
          selectItem.setValue(absenceType.getAbsenceTypeId());
          selectItems.add(selectItem);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return selectItems;
  }
 
  public void previousYear()
  {
    year--;
    absenceCounterViews = null;
  }

  public void currentYear()
  {
    year = Calendar.getInstance().get(Calendar.YEAR);
    absenceCounterViews = null;
  }

  public void nextYear()
  {
    year++;
    absenceCounterViews = null;
  }

  public Worker getWorker()
  {
    PresenceMainBean presenceMainBean = 
      (PresenceMainBean)getBean("presenceMainBean");
    return presenceMainBean.getWorker();
  }  
}
