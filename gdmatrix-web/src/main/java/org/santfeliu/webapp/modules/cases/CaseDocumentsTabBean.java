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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.modules.doc.DocumentTypeBean;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class CaseDocumentsTabBean extends TabBean
{
  public static final String UNLINK = "unlink";
  public static final String REMOVE = "remove";
  public static final String REMOVE_ALL = "removeAll";

  Map<String, TabInstance> tabInstances = new HashMap<>();
  CaseDocument editing;
  CaseDocumentView caseDocumentToRemove;
  String removeMode;

  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();
  private final List<SelectItem> volumeSelectItems = new ArrayList<>();

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseDocumentView> rows;
    int firstRow = 0;
    boolean groupedView = true;
    String currentVolume;
  }

  @Inject
  CaseObjectBean caseObjectBean;

  @Inject
  DocumentTypeBean documentTypeBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
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

  public List<CaseDocumentView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CaseDocumentView> caseDocumentViews)
  {
    getCurrentTabInstance().rows = caseDocumentViews;
  }

  public String getDocumentIcon(Document document)
  {
    return DocumentTypeBean.getContentIcon(document);
  }

  public String getDocumentLanguage(Document document)
  {
    String language = document.getLanguage();
    if (language == null || 
      language.equals(DocumentConstants.UNIVERSAL_LANGUAGE))
    {
      return "";
    }
    else
    {
      String languageDescr = DocumentUtils.extendLanguage(language);
      return ("".equals(languageDescr) ? language : languageDescr);
    }
  }

  public String getViewURL()
  {
    CaseDocumentView docView = WebUtils.getValue("#{row}");
    if (docView.getDocument() != null)
    {
      String docId = docView.getDocument().getDocId();

      ExternalContext extContext = getExternalContext();
      HttpServletRequest request = (HttpServletRequest)extContext.getRequest();
      String contextPath = request.getContextPath();

      return contextPath + "/documents/" + docId;
    }
    else
      return null;
  }

  public CaseDocument getEditing()
  {
    return editing;
  }

  public void setEditing(CaseDocument caseDocument)
  {
    editing = caseDocument;
  }

  public String getDocId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getDocId();
  }

  public void setDocId(String docId)
  {
    if (editing != null)
    {
      editing.setDocId(docId);
    }
  }

  public void setCaseDocTypeId(String caseDocTypeId)
  {
    if (editing != null)
      editing.setCaseDocTypeId(caseDocTypeId);
  }

  public String getCaseDocTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getCaseDocTypeId();
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
    return caseObjectBean.getActiveEditTab().
      getProperties().getBoolean("groupedViewEnabled");
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
  
  public String getDocumentDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return documentTypeBean.getDescription(editing.getDocId());
    }
    return "";
  }

  @Override
  public void load()
  {
    executeTabAction("preTabLoad", null);    
    String objectId = getObjectId();
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      try
      {
        CaseManagerPort port = CasesModuleBean.getPort(false);
        List<String> volumes = port.findCaseVolumes(objectId);
        Collections.sort(volumes, (s1, s2) ->
        {
          if (s1 == null) s1 = "";
          if (s2 == null) s2 = "";
          return s2.compareTo(s1);
        });
        volumeSelectItems.clear();
        volumeSelectItems.add(new SelectItem(null, ""));
        for (String value : volumes)
        {
          if (!StringUtils.isBlank(value))
          {
            SelectItem selectItem = new SelectItem(value, value);
            volumeSelectItems.add(selectItem);
          }
        }

        CaseDocumentFilter filter = new CaseDocumentFilter();
        EditTab tab = caseObjectBean.getActiveEditTab();
        String volume = tab.getProperties().getString("volume");
        if (volume != null && getCurrentVolume() == null)
        {
          setCurrentVolume(volume);
        }

        filter.setVolume(getCurrentVolume());
        filter.setCaseId(objectId);
        List<CaseDocumentView> auxListPre = port.findCaseDocumentViews(filter);
        
        //Show only rows with document        
        List<CaseDocumentView> auxList = new ArrayList();
        for (CaseDocumentView cdv : auxListPre)
        {
          if (cdv.getDocument() != null) auxList.add(cdv);
        }
        
        String typeId = getTabBaseTypeId();
        if (typeId == null)
        {
          getCurrentTabInstance().rows = auxList;
        }
        else
        {
          List<CaseDocumentView> result = new ArrayList();
          for (CaseDocumentView item : auxList)
          {
            try
            {
              Type caseDocType =
                TypeCache.getInstance().getType(item.getCaseDocTypeId());
              if (caseDocType.isDerivedFrom(typeId))
              {
                result.add(item);
              }
            }
            catch (Exception ex)
            {
              // ignore: bad type?
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

  @Override
  public void store()
  {
    try
    {
      editing.setCaseId(getObjectId());
      if (editing.getCaseDocTypeId() == null)
      {
        editing.setCaseDocTypeId("CaseDocument");
      }
      editing = (CaseDocument) executeTabAction("preTabStore", editing);      
      editing = CasesModuleBean.getPort(false).storeCaseDocument(editing);
      executeTabAction("postTabStore", editing);      
      refreshHiddenTabInstances();
      load();
      editing = null;
      growl("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
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

  public void create()
  {
    editing = new CaseDocument();
    editing.setCaseDocTypeId(getCreationTypeId());
  }

  public String getCurrentVolume()
  {
    return getCurrentTabInstance().currentVolume;
  }

  public void setCurrentVolume(String currentVolume)
  {
    getCurrentTabInstance().currentVolume = currentVolume;
  }

  public List<SelectItem> getVolumeSelectItems()
  {
    return volumeSelectItems;
  }

  public void volumeChanged(AjaxBehaviorEvent e)
  {
    load();
  }

  public void switchView()
  {
    getCurrentTabInstance().groupedView = !getCurrentTabInstance().groupedView;
  }

  public void edit(CaseDocumentView caseDocView)
  {
    if (caseDocView != null)
    {
      try
      {
        editing = CasesModuleBean.getPort(false)
          .loadCaseDocument(caseDocView.getCaseDocId());
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

  public void remove()
  {
    System.out.println("Remove mode:" + removeMode);
    System.out.println("caseDocumentView:" + caseDocumentToRemove.getCaseDocId());

    if (removeMode == null || caseDocumentToRemove == null) return;

    try
    {
      caseDocumentToRemove = 
        (CaseDocumentView) executeTabAction("preTabRemove", caseDocumentToRemove);          
      switch (removeMode)
      {
        case UNLINK:
        {
          CasesModuleBean.getPort(false)
            .removeCaseDocument(caseDocumentToRemove.getCaseDocId());
        }
        break;

        case REMOVE:
        {
          String docId = caseDocumentToRemove.getDocument().getDocId();

          CasesModuleBean.getPort(false)
            .removeCaseDocument(caseDocumentToRemove.getCaseDocId());

          CaseDocumentFilter filter = new CaseDocumentFilter();
          filter.setDocId(docId);
          int count =
            CasesModuleBean.getPort(true).countCaseDocuments(filter);
          System.out.println("Count: " + count);
          if (count == 0)
          {
            DocModuleBean.getPort(false).removeDocument(docId, 0);
            NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
            NavigatorBean.BaseTypeInfo baseTypeInfo =
              navigatorBean.getBaseTypeInfo("Document");
            if (baseTypeInfo != null)
            {
              baseTypeInfo.remove(docId);
            }
          }
        }
        break;

        case REMOVE_ALL:
        {
          String docId = caseDocumentToRemove.getDocument().getDocId();
          CaseDocumentFilter filter = new CaseDocumentFilter();
          filter.setDocId(docId);
          CaseManagerPort port = CasesModuleBean.getPort(false);
          List<CaseDocumentView> views = port.findCaseDocumentViews(filter);
          for (CaseDocumentView view : views)
          {
            port.removeCaseDocument(view.getCaseDocId());
          }
          DocModuleBean.getPort(false).removeDocument(docId, 0);
          NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
          NavigatorBean.BaseTypeInfo baseTypeInfo =
            navigatorBean.getBaseTypeInfo("Document");
          if (baseTypeInfo != null)
          {
            baseTypeInfo.remove(docId);
          }
        }
        break;

        default:
          break;
      }
      executeTabAction("postTabRemove", caseDocumentToRemove);      
      refreshHiddenTabInstances();
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelRemove()
  {
    this.caseDocumentToRemove = null;
  }

  @Override
  public void clear()
  {
    tabInstances.clear();
  }

  public CaseDocumentView getCaseDocumentToRemove()
  {
    return caseDocumentToRemove;
  }

  public void setCaseDocumentToRemove(CaseDocumentView caseDocumentToRemove)
  {
    this.caseDocumentToRemove = caseDocumentToRemove;
    this.removeMode = "unlink";
  }

  public String getRemoveMode()
  {
    return removeMode;
  }

  public void setRemoveMode(String removeMode)
  {
    this.removeMode = removeMode;
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
      editing = (CaseDocument)stateArray[0];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isNew(CaseDocument caseDocument)
  {
    return (caseDocument != null && caseDocument.getCaseDocId() == null);
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
