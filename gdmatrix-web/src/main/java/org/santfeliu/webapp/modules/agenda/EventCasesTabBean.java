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
package org.santfeliu.webapp.modules.agenda;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CaseEvent;
import org.matrix.cases.CaseEventFilter;
import org.matrix.cases.CaseEventView;
import org.primefaces.PrimeFaces;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.cases.CaseTypeBean;
import org.santfeliu.webapp.modules.cases.CasesModuleBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ViewScoped
public class EventCasesTabBean extends TabBean
{
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();

  private CaseEvent editing;
  Map<String, TabInstance> tabInstances = new HashMap();
  private String formSelector;

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseEventView> rows;
    int firstRow = 0;
    boolean groupedView = true;
  }

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  CaseTypeBean caseTypeBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = eventObjectBean.getActiveEditTab();
    if (WebUtils.getBeanName(this).equals(tab.getBeanName()))
    {
      TabInstance tabInstance = tabInstances.get(tab.getSubviewId());
      if (tabInstance == null)
      {
        tabInstance = new TabInstance();
        tabInstances.put(tab.getSubviewId(), tabInstance);
      }
      return tabInstance;
    }
    else return EMPTY_TAB_INSTANCE;
  }

  @Override
  public String getObjectId()
  {
    return getCurrentTabInstance().objectId;
  }

  @Override
  public void setObjectId(String objectId)
  {
    getCurrentTabInstance().objectId = objectId;
  }

  @Override
  public boolean isNew()
  {
    return NEW_OBJECT_ID.equals(getCurrentTabInstance().objectId);
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return eventObjectBean;
  }

  public CaseEvent getEditing()
  {
    return editing;
  }

  public void setEditing(CaseEvent editing)
  {
    this.editing = editing;
  }

  public void setCaseId(String caseId)
  {
    editing.setCaseId(caseId);
    showDialog();
  }

  public String getCaseId()
  {
    return editing.getCaseId();
  }

  public void setCaseEventTypeId(String caseEventTypeId)
  {
    if (editing != null)
      editing.setCaseEventTypeId(caseEventTypeId);

    showDialog();
  }

  public String getCaseEventTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getCaseEventTypeId();
  }

  public List<CaseEventView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CaseEventView> rows)
  {
    getCurrentTabInstance().rows = rows;
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  public boolean isGroupedView()
  {
    return isGroupedViewEnabled() && getCurrentTabInstance().groupedView;
  }

  public void setGroupedView(boolean groupedView)
  {
    getCurrentTabInstance().groupedView = groupedView;
  }

  public boolean isGroupedViewEnabled()
  {
    return Boolean.parseBoolean(eventObjectBean.getActiveEditTab().
      getProperties().getString("groupedViewEnabled"));
  }

  public String getCaseDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return caseTypeBean.getDescription(editing.getCaseId());
    }
    return "";
  }

  public String getCaseEventTypeDescription()
  {
    String typeId = null;
    CaseEventView row = (CaseEventView)getValue("#{row}");
    if (row != null)
    {
      typeId = row.getCaseEventTypeId();
      if (typeId != null)
      {
        return typeTypeBean.getDescription(typeId);
      }
    }
    return typeId;
  }

  public void edit(CaseEventView row)
  {
    if (row != null)
    {
      try
      {
        editing = CasesModuleBean.getPort(false).
          loadCaseEvent(row.getCaseEventId());
      }
      catch (Exception ex)
      {
        error(ex);
      }
      formSelector = null;
    }
    else
    {
      create();
    }
  }

  @Override
  public void load() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        String typeId = getTabBaseTypeId();
        CaseEventFilter filter = new CaseEventFilter();
        filter.setEventId(eventObjectBean.getObjectId());
        filter.setCaseEventTypeId(typeId);
        filter.setExcludeMetadata(false);
        getCurrentTabInstance().rows =
          CasesModuleBean.getPort(false).findCaseEventViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      TabInstance tabInstance = getCurrentTabInstance();
      tabInstance.objectId = NEW_OBJECT_ID;
      tabInstance.rows = Collections.EMPTY_LIST;
      tabInstance.firstRow = 0;
    }
  }

  public void create()
  {
    editing = new CaseEvent();

    String typeId = getTabBaseTypeId();
    if (typeId != null)
      editing.setCaseEventTypeId(typeId);
  }

  public void switchView()
  {
    getCurrentTabInstance().groupedView = !getCurrentTabInstance().groupedView;
  }

  @Override
  public void store()
  {
    try
    {
      if (editing != null)
      {
        String eventId = eventObjectBean.getObjectId();
        editing.setEventId(eventId);
        CasesModuleBean.getPort(false).storeCaseEvent(editing);
        refreshHiddenTabInstances();
        load();
        editing = null;
        info("STORE_OBJECT");
        hideDialog();
      }
    }
    catch (Exception ex)
    {
      error(ex);
      showDialog();
    }
  }

  public void remove(CaseEventView row)
  {
    if (row != null)
    {
      try
      {
        CasesModuleBean.getPort(false).removeCaseEvent(row.getCaseEventId());
        refreshHiddenTabInstances();
        load();
        info("REMOVE_OBJECT");
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  public void cancel()
  {
    editing = null;
  }

  public String getTabBaseTypeId()
  {
    EditTab editTab = eventObjectBean.getActiveEditTab();
    return editTab.getProperties().getString("typeId");
  }

  @Override
  public void clear()
  {
    tabInstances.clear();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (CaseEvent)stateArray[0];
      formSelector = (String)stateArray[1];
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isNew(CaseEvent attendant)
  {
    return (attendant != null && attendant.getCaseEventId() == null);
  }

  private void showDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('eventCasesDialog').show();");
  }

  private void hideDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('eventCasesDialog').hide();");
  }

  private void refreshHiddenTabInstances()
  {
    for (TabInstance tabInstance : tabInstances.values())
    {
      if (tabInstance != getCurrentTabInstance())
      {
        tabInstance.objectId = NEW_OBJECT_ID;
      }
    }
  }

}
