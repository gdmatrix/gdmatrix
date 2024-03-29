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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.CaseAddress;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.modules.kernel.AddressTypeBean;
import org.santfeliu.webapp.modules.kernel.KernelModuleBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class CaseAddressesTabBean extends TabBean
{
  private CaseAddress editing;
  Map<String, TabInstance> tabInstances = new HashMap();
  private boolean importPersons;
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseAddressView> rows;
    int firstRow = 0;
    boolean groupedView = true;
  }

  @Inject
  CaseObjectBean caseObjectBean;

  @Inject
  AddressTypeBean addressTypeBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = caseObjectBean.getActiveEditTab();
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
    else
      return EMPTY_TAB_INSTANCE;
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
    return caseObjectBean;
  }

  public CaseAddress getEditing()
  {
    return editing;
  }

  public void setEditing(CaseAddress editing)
  {
    this.editing = editing;
  }

  public boolean isImportPersons()
  {
    return importPersons;
  }

  public void setImportPersons(boolean importPersons)
  {
    this.importPersons = importPersons;
  }

  public void setCaseAddressTypeId(String caseAddressTypeId)
  {
    if (editing != null)
      editing.setCaseAddressTypeId(caseAddressTypeId);
  }

  public String getCaseAddressTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getCaseAddressTypeId();
  }

  public List<CaseAddressView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CaseAddressView> rows)
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
    return caseObjectBean.getActiveEditTab().getProperties()
      .getBoolean("groupedViewEnabled");
  }

  public boolean isRenderTypeColumn()
  {
    if (isGroupedView())
    {
      return false;
    }
    else
    {
      String tabTypeId = caseObjectBean.getActiveEditTab().getProperties().
        getString("typeId");
      if (tabTypeId != null)
      {
        return !TypeCache.getInstance().getDerivedTypeIds(tabTypeId).isEmpty();
      }
      else
      {
        return true;
      }
    }    
  }  
  
  public String getAddressDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return addressTypeBean.getDescription(editing.getAddressId());
    }
    return "";
  }

  public Map<String, TabInstance> getTabInstances()
  {
    return tabInstances;
  }

  public void edit(CaseAddressView row)
  {
    if (row != null)
    {
      try
      {
        editing = CasesModuleBean.getPort(false).
          loadCaseAddress(row.getCaseAddressId());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      create();
    }
  }

  @Override
  public void load() throws Exception
  {
    executeTabAction("preTabLoad", null);
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        CaseAddressFilter filter = new CaseAddressFilter();
        filter.setCaseId(caseObjectBean.getObjectId());

        getCurrentTabInstance().rows =
          CasesModuleBean.getPort(false).findCaseAddressViews(filter);

        String typeId = getTabBaseTypeId();
        if (typeId != null)
        {
          List<CaseAddressView> result = new ArrayList();
          for (CaseAddressView item : getCurrentTabInstance().rows)
          {
            String caseAddressTypeId = item.getCaseAddressTypeId();
            if (caseAddressTypeId == null)
              caseAddressTypeId = DictionaryConstants.CASE_ADDRESS_TYPE;

            Type caseAddressType =
              TypeCache.getInstance().getType(caseAddressTypeId);
            if (caseAddressType.isDerivedFrom(typeId))
            {
              result.add(item);
            }
          }
          getCurrentTabInstance().rows = result;
        }
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
    executeTabAction("postTabLoad", null);    
  }

  public void create()
  {
    editing = new CaseAddress();
    editing.setCaseAddressTypeId(getCreationTypeId());
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
        String caseId = caseObjectBean.getObjectId();
        editing.setCaseId(caseId);
        editing = (CaseAddress) executeTabAction("preTabStore", editing);
        editing = CasesModuleBean.getPort(false).storeCaseAddress(editing);
        if (importPersons)
        {
          importPersonsFromEditingAddress();
          refreshCasePersonsTabInstances();
          importPersons = false;
        }
        executeTabAction("postTabStore", editing);        
        refreshHiddenTabInstances();
        load();
        editing = null;
        growl("STORE_OBJECT");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void remove(CaseAddressView row)
  {
    if (row != null)
    {
      try
      {
        row = (CaseAddressView) executeTabAction("preTabRemove", row);
        CasesModuleBean.getPort(false).removeCaseAddress(row.getCaseAddressId());
        executeTabAction("postTabRemove", row);
        refreshHiddenTabInstances();
        load();
        growl("REMOVE_OBJECT");
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

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }  
  
  @Override
  public void clear()
  {
    tabInstances.clear();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (CaseAddress)stateArray[0];
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isNew(CaseAddress caseAddress)
  {
    return (caseAddress != null && caseAddress.getCaseAddressId() == null);
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

  private void refreshCasePersonsTabInstances()
  {
    CasePersonsTabBean casePersonsTabBean =
      WebUtils.getBean("casePersonsTabBean");
    Collection<CasePersonsTabBean.TabInstance> cpTabInstances =
      casePersonsTabBean.getTabInstances().values();
    for (CasePersonsTabBean.TabInstance tabInstance : cpTabInstances)
    {
      tabInstance.objectId = NEW_OBJECT_ID;
    }
  }

  private void importPersonsFromEditingAddress() throws Exception
  {    
    if (editing != null)
    {
      String addressId = editing.getAddressId();
      if (addressId != null)
      {
        String typeId = caseObjectBean.getActiveEditTab().getProperties()
            .getString("importPersonsTypeId"); 
        if (typeId == null)
          typeId = DictionaryConstants.CASE_PERSON_TYPE;
        List<String> personIds = getCurrentPersons(editing.getCaseId(), typeId);
        List<PersonAddressView> personAddresses = getPersonAddresses(addressId);
        for (PersonAddressView personAddress : personAddresses)
        {
          String personId = personAddress.getPerson().getPersonId();
          if (!personIds.contains(personId))
          {
            CasePerson casePerson = new CasePerson();
            casePerson.setCasePersonTypeId(typeId);
            casePerson.setCaseId(editing.getCaseId());
            casePerson.setPersonId(personId);
            CasesModuleBean.getPort(false).storeCasePerson(casePerson);
          }
        }
      }
    }
  }
  
  private List<String> getCurrentPersons(String caseId, String importTypeId)
    throws Exception
  {
    List<String> results = new ArrayList();

    CasePersonFilter filter = new CasePersonFilter();
    filter.setCaseId(caseId);
    List<CasePersonView> casePersonViews =
      CasesModuleBean.getPort(true).findCasePersonViews(filter);

    for(CasePersonView casePersonView : casePersonViews)
    {
      if (casePersonView.getCasePersonTypeId().equals(importTypeId))
        results.add(casePersonView.getPersonView().getPersonId());
    }

    return results;
  }

  private List<PersonAddressView> getPersonAddresses(String addressId)
    throws Exception
  {
    PersonAddressFilter filter = new PersonAddressFilter();
    filter.setAddressId(addressId);
    return KernelModuleBean.getPort(true).findPersonAddressViews(filter);
  }
  
}
