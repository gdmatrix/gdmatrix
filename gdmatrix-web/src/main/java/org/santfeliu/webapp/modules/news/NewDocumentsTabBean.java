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
package org.santfeliu.webapp.modules.news;

import org.santfeliu.webapp.modules.news.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.news.NewDocument;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.modules.doc.DocumentTypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class NewDocumentsTabBean extends TabBean
{
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();

  private NewDocument editing;
  Map<String, TabInstance> tabInstances = new HashMap<>();

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<NewDocument> rows;
    int firstRow = 0;
  }

  @Inject
  NewObjectBean newObjectBean;

  @Inject
  DocumentTypeBean documentTypeBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = newObjectBean.getActiveEditTab();
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
    return newObjectBean;
  }
  public NewDocument getEditing()
  {
    return editing;
  }

  public void setEditing(NewDocument editing)
  {
    this.editing = editing;
  }

  public void setNewDocTypeId(String newDocTypeId)
  {
    if (editing != null)
      editing.setNewDocTypeId(newDocTypeId);
  }

  public String getNewDocTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getNewDocTypeId();
  }

  public List<NewDocument> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<NewDocument> rows)
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

  public boolean isRenderTypeColumn()
  {
    String tabTypeId = newObjectBean.getActiveEditTab().getProperties().
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

  public String getDocumentDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return documentTypeBean.getDescription(editing.getDocumentId());
    }
    return "";
  }

  public void edit(NewDocument row)
  {
    if (row != null)
    {
      try
      {
        editing = row;
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
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        List<NewDocument> auxList = NewsModuleBean.getPort(false)
          .findNewDocuments(newObjectBean.getObjectId(), null);
        String typeId = getTabBaseTypeId();
        if (typeId == null)
        {
          getCurrentTabInstance().rows = auxList;
        }
        else
        {
          List<NewDocument> result = new ArrayList();
          for (NewDocument item : auxList)
          {
            Type newDocType =
              TypeCache.getInstance().getType(item.getNewDocTypeId());
            if (newDocType.isDerivedFrom(typeId))
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
  }

  public void create()
  {
    editing = new NewDocument();
    editing.setNewDocTypeId(getCreationTypeId());
  }

  @Override
  public void store()
  {
    try
    {
      if (editing != null)
      {
        String newId = newObjectBean.getObjectId();
        editing.setNewId(newId);
        NewsModuleBean.getPort(false).storeNewDocument(editing);
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

  public void remove(NewDocument row)
  {
    if (row != null)
    {
      try
      {
        NewsModuleBean.getPort(false).removeNewDocument(
          row.getNewDocumentId());
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
      editing = (NewDocument)stateArray[0];
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isNew(NewDocument newDocument)
  {
    return (newDocument != null && newDocument.getNewDocumentId() == null);
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
