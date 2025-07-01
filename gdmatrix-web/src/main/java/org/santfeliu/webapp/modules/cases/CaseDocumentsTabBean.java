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
import javax.enterprise.context.RequestScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseConstants;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.security.AccessControl;
import org.primefaces.PrimeFaces;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentUrlBuilder;
import org.santfeliu.faces.matrixclient.model.DefaultMatrixClientModel;
import static org.santfeliu.faces.matrixclient.model.DocMatrixClientModels.DOCTYPES_PARAMETER;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.webapp.BaseBean;
import org.santfeliu.webapp.DataTableRowExportable;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.RowsExportHelper;
import org.santfeliu.webapp.helpers.RowsFilterHelper;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.modules.doc.DocumentTypeBean;
import static org.santfeliu.webapp.setup.Action.POST_TAB_EDIT_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_LOAD_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_REMOVE_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_STORE_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_EDIT_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_LOAD_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_STORE_ACTION;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.DataTableRowComparator;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class CaseDocumentsTabBean extends TabBean  
  implements DataTableRowExportable
{
  public static final String UNLINK = "unlink";
  public static final String REMOVE = "remove";
  public static final String REMOVE_ALL = "removeAll";

  public static final String SPREAD_ROLES_PROPERTY = "_documentsSpreadRoles";
  
  Map<String, TabInstance> tabInstances = new HashMap<>();
  CaseDocument editing;
  CaseDocumentsDataTableRow caseDocumentToRemove;
  String removeMode;

  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();
  private final List<SelectItem> volumeSelectItems = new ArrayList<>();
  private GroupableRowsHelper groupableRowsHelper;
  private DefaultMatrixClientModel clientModel;

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseDocumentsDataTableRow> rows;
    int firstRow = 0;
    String currentVolume;
    RowsFilterHelper rowsFilterHelper = RowsFilterHelper.create(null, prev -> 
      new RowsFilterHelper<CaseDocumentsDataTableRow>(prev)
    {
      @Override
      public ObjectBean getObjectBean() 
      {
        return CaseDocumentsTabBean.this.getObjectBean();
      }
      
      @Override
      public List<CaseDocumentsDataTableRow> getRows()
      {
        return rows;
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return CaseDocumentsTabBean.this.getGroupableRowsHelper().
          isGroupedViewEnabled();
      }

      @Override
      public void resetFirstRow()
      {
        firstRow = 0;
      }

      @Override
      public List<TableProperty> getColumns() 
      {
        return CaseDocumentsTabBean.this.getColumns();        
      }

      @Override
      public Item getFixedColumnValue(CaseDocumentsDataTableRow row, 
        String columnName) 
      {
        return null; //No fixed columns        
      }

      @Override
      public String getRowTypeId(CaseDocumentsDataTableRow row) 
      {
        return row.getTypeId();               
      }
    });

    RowsFilterHelper rowsFilterHelper2 = 
      RowsFilterHelper.create(rowsFilterHelper, prev -> 
        new RowsFilterHelper<CaseDocumentsDataTableRow>(prev)
    {
      @Override
      public ObjectBean getObjectBean() 
      {
        return CaseDocumentsTabBean.this.getObjectBean();
      }
      
      @Override
      public List<CaseDocumentsDataTableRow> getRows()
      {
        return prev.getFilteredRows();
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return CaseDocumentsTabBean.this.getGroupableRowsHelper().
          isGroupedViewEnabled();
      }

      @Override
      public void resetFirstRow()
      {
        firstRow = 0;
      }

      @Override
      public List<TableProperty> getColumns() 
      {
        return CaseDocumentsTabBean.this.getColumns();        
      }

      @Override
      public Item getFixedColumnValue(CaseDocumentsDataTableRow row, 
        String columnName) 
      {
        return null; //No fixed columns        
      }

      @Override
      public String getRowTypeId(CaseDocumentsDataTableRow row) 
      {
        return row.getTypeId();               
      }      
    });
    
    public RowsFilterHelper getRowsFilterHelper()
    {
      return rowsFilterHelper;
    }
    
    public RowsFilterHelper getRowsFilterHelper2()
    {
      return rowsFilterHelper2;
    }
    
    public RowsFilterHelper getActiveRowsFilterHelper()
    {
      if (rowsFilterHelper2.isRendered())
      {
        return rowsFilterHelper2;
      }
      else
      {
        return rowsFilterHelper;
      }
    }
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
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return CaseDocumentsTabBean.this.getObjectBean();
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return CaseDocumentsTabBean.this.getColumns();
      }

      @Override
      public void sortRows()
      {
        if (getOrderBy() != null)
        {
          Collections.sort(getCurrentTabInstance().rows,
            new DataTableRowComparator(getColumns(), getOrderBy()));
        }
      }

      @Override
      public String getRowTypeColumnName()
      {
        return "caseDocumentTypeId";
      }

      @Override
      public String getFixedColumnValue(Object row, String columnName)
      {
        return null; //No fixed columns
      }
      
      @Override
      public boolean isColumnRendered(String columnName)
      {
        boolean renderColumn = super.isColumnRendered(columnName);
        if (renderColumn) //check volume column
        {
          if ("volume".equals(columnName))
          {
            String currentVol = CaseDocumentsTabBean.this.getCurrentVolume();
            renderColumn = CaseConstants.SHOW_ALL_VOLUMES.equals(currentVol);
          }
        }
        return renderColumn;
      }
    };
  }

  public GroupableRowsHelper getGroupableRowsHelper()
  {
    return groupableRowsHelper;
  }

  public void setGroupableRowsHelper(GroupableRowsHelper groupableRowsHelper)
  {
    this.groupableRowsHelper = groupableRowsHelper;
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

  public Map<String, TabInstance> getTabInstances()
  {
    return tabInstances;
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

  public List<CaseDocumentsDataTableRow> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CaseDocumentsDataTableRow> rows)
  {
    getCurrentTabInstance().rows = rows;
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
    CaseDocumentsDataTableRow docView = WebUtils.getValue("#{row}");
    return docView.getDocViewUrl();
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

  public String getDocumentDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return documentTypeBean.getDescription(editing.getDocId());
    }
    return "";
  }

  public List<String> getOrderBy()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getOrderBy();
    else
      return Collections.EMPTY_LIST;
  }

  @Override
  public List<TableProperty> getTableProperties()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getTableProperties();
    else
      return Collections.EMPTY_LIST;
  }

  @Override
  public List<TableProperty> getColumns()
  {
    return TablePropertyHelper.getColumnTableProperties(getTableProperties());
  }

  @Override
  public List<? extends DataTableRow> getExportableRows() 
  {
    return getCurrentTabInstance().getActiveRowsFilterHelper().
      getFilteredRows();    
  }  

  @Override
  public int getRowExportLimit()
  {
    return RowsExportHelper.getActiveEditTabRowExportLimit(caseObjectBean);
  }
  
  @Override
  public boolean isExportable()
  {
    return RowsExportHelper.isActiveEditTabExportable(caseObjectBean);
  }
  
  @Override
  public void load()
  {
    executeTabAction(PRE_TAB_LOAD_ACTION, null);
    String objectId = getObjectId();
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      try
      {
        List<CaseDocumentView> result;
        volumeSelectItems.clear();        
        CaseManagerPort port = CasesModuleBean.getPort(false);
        List<String> volumes = port.findCaseVolumes(objectId);
        if (volumes != null && !volumes.isEmpty())
        {
          Collections.sort(volumes, (s1, s2) ->
          {
            if (s1 == null) s1 = "";
            if (s2 == null) s2 = "";
              return s1.compareTo(s2);
          });
          boolean addUndefined = false;
          for (String value : volumes)
          {
            SelectItem selectItem;
            if (!StringUtils.isBlank(value))
            {
              selectItem = new SelectItem(value, value);
              volumeSelectItems.add(selectItem);
            }
            else //null or whitespaces
            {
              addUndefined = true;
            }
          }

          if (addUndefined)
          {
            volumeSelectItems.add(0, new SelectItem(
              CaseConstants.UNDEFINED_VOLUME, ""));          
          }
          EditTab tab = caseObjectBean.getActiveEditTab();
          if ("true".equals(tab.getProperties().getString(
            "enableShowAllVolumes")))
          {
            volumeSelectItems.add(0, new SelectItem(
              CaseConstants.SHOW_ALL_VOLUMES, 
              ApplicationBean.getCurrentInstance().translate(
                "$$objectBundle.showAll")));                 
          }

          if (!existsSelectItem(volumeSelectItems, getCurrentVolume())) 
          {
            String volume = tab.getProperties().getString("volume");
            if (volume != null && existsSelectItem(volumeSelectItems, volume))
            {
              setCurrentVolume(volume); //default value
            }
            else
            {
              setCurrentVolume((String)volumeSelectItems.get(0).getValue());
            }
          }

          String filterVolume = (CaseConstants.SHOW_ALL_VOLUMES.equals(
            getCurrentVolume()) ? null : getCurrentVolume());
          CaseDocumentFilter filter = new CaseDocumentFilter();
          filter.setVolume(filterVolume);
          filter.setCaseId(objectId);
          List<CaseDocumentView> auxListPre = port.findCaseDocumentViews(filter);

          //Show only rows with document
          List<CaseDocumentView> auxList = new ArrayList();
          for (CaseDocumentView cdv : auxListPre)
          {
            if (cdv.getDocument() != null) auxList.add(cdv);
          }

          String typeId = getTabBaseTypeId();
          if (typeId == null || tab.isShowAllTypes())
          {
            result = auxList;
          }
          else
          {
            result = new ArrayList();
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
          }
        }
        else
        {
          result = Collections.EMPTY_LIST;
        }
        List<CaseDocumentsDataTableRow> auxList2 = toDataTableRows(result);
        if (getOrderBy() != null)
        {
          Collections.sort(auxList2,
            new DataTableRowComparator(getColumns(), getOrderBy()));
        }
        setRows(auxList2);
        getCurrentTabInstance().rowsFilterHelper.reset();
        getCurrentTabInstance().rowsFilterHelper2.reset();
        executeTabAction(POST_TAB_LOAD_ACTION, null);
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
      getCurrentTabInstance().rowsFilterHelper.reset();
      getCurrentTabInstance().rowsFilterHelper2.reset();
      tabInstance.firstRow = 0;
    }
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
      String editingVolume = editing.getVolume();
      if (editingVolume != null)
      {
        editingVolume = editingVolume.replace('\u00A0', ' ');      
        if (CaseConstants.UNDEFINED_VOLUME.equals(editingVolume) || 
          StringUtils.isBlank(editingVolume))
        {
          editing.setVolume(null);
        }
        else
        {
          editing.setVolume(editingVolume.trim());
        }
      }
      editing = (CaseDocument) executeTabAction(PRE_TAB_STORE_ACTION, editing);
      editing = CasesModuleBean.getPort(false).storeCaseDocument(editing);
      spreadDocumentRoles(editing.getDocId());
      executeTabAction(POST_TAB_STORE_ACTION, editing);
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
    executeTabAction(PRE_TAB_EDIT_ACTION, null);
    editing = new CaseDocument();
    editing.setCaseDocTypeId(getCreationTypeId());
    executeTabAction(POST_TAB_EDIT_ACTION, editing);
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

  public List<SelectItem> getRealVolumeSelectItems()
  {
    List<SelectItem> list = new ArrayList<>();
    for (SelectItem item : volumeSelectItems)
    {
      if (!item.getValue().equals(CaseConstants.SHOW_ALL_VOLUMES)) //real value
      {
        list.add(item);
      }
    }
    return list;
  }  

  public void volumeChanged(AjaxBehaviorEvent e)
  {
    load();
  }

  public void edit(DataTableRow row)
  {
    if (row != null)
    {
      try
      {
        executeTabAction(PRE_TAB_EDIT_ACTION, row);
        editing =
          CasesModuleBean.getPort(false).loadCaseDocument(row.getRowId());
        executeTabAction(POST_TAB_EDIT_ACTION, editing);
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
  public void edit(String rowId)
  {
    List<? extends DataTableRow> rows = getRows();
    for (DataTableRow row : rows)
    {       
      if (row.getRowId().equals(rowId))
      {
        edit(row);
        PrimeFaces.current().executeScript("PF('caseDocumentsDialog').show()");
        return;
      }
    }
  }  

  public void remove()
  {
    System.out.println("Remove mode:" + removeMode);
    System.out.println("caseDocumentView:" + caseDocumentToRemove.getRowId());

    if (removeMode == null || caseDocumentToRemove == null) return;

    try
    {
      caseDocumentToRemove = (CaseDocumentsDataTableRow)executeTabAction(
        "preTabRemove", caseDocumentToRemove);
      switch (removeMode)
      {
        case UNLINK:
        {
          CasesModuleBean.getPort(false)
            .removeCaseDocument(caseDocumentToRemove.getRowId());
        }
        break;

        case REMOVE:
        {
          String docId = caseDocumentToRemove.getDocId();
          if (docId != null)
          {
            CasesModuleBean.getPort(false)
              .removeCaseDocument(caseDocumentToRemove.getRowId());
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
        }
        break;

        case REMOVE_ALL:
        {
          String docId = caseDocumentToRemove.getDocId();
          if (docId != null)
          {
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
        }
        break;

        default:
          break;
      }
      executeTabAction(POST_TAB_REMOVE_ACTION, caseDocumentToRemove);
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
  
  public DefaultMatrixClientModel getSendClientModel()
  {
    clientModel = getClientModel();
    Map creationDocTypes = 
      DocModuleBean.getUserDocTypes(DictionaryConstants.CREATE_ACTION);
    clientModel.putParameter(DOCTYPES_PARAMETER, creationDocTypes);
    return clientModel;
  }

  public DefaultMatrixClientModel getClientModel()
  {
    if (clientModel == null)
    {
      clientModel = new DefaultMatrixClientModel();
    }
    return clientModel;
  }

  public void setClientModel(DefaultMatrixClientModel clientModel)
  {
    this.clientModel = clientModel;
  }

  public void documentEdited()
  {
    try
    {
      clientModel.parseResult();
      this.load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public String documentSent()
  {   
    try
    {
      String docId = (String) clientModel.parseResult();
      if (docId != null)
      {
        spreadDocumentRoles(docId);
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setCaseId(getObjectId());
        caseDocument.setDocId(docId);
        String currentVolume = getCurrentVolume();
        if (currentVolume != null && !"".equals(currentVolume) 
            && !CaseConstants.UNDEFINED_VOLUME.equals(currentVolume))
          caseDocument.setVolume(currentVolume);
        
        String caseDocTypeId = getCreationTypeId();
        if (caseDocTypeId == null) 
          caseDocTypeId = DictionaryConstants.CASE_DOCUMENT_TYPE;
        caseDocument.setCaseDocTypeId(caseDocTypeId);
        
        CasesModuleBean.getPort(false).storeCaseDocument(caseDocument);
        
        this.load();
      }
    }
    catch (Exception ex)
    {
      if (!"NO_FILE".equals(ex.getMessage()))
        error(ex);
    }

    return null;
  }  

  public CaseDocumentsDataTableRow getCaseDocumentToRemove()
  {
    return caseDocumentToRemove;
  }

  public void setCaseDocumentToRemove(
    CaseDocumentsDataTableRow caseDocumentToRemove)
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

  public boolean isRenderVolumeSelector()
  {
    return !getRealVolumeSelectItems().isEmpty();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing, getCurrentVolume() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (CaseDocument)stateArray[0];
      setCurrentVolume((String)stateArray[1]);

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

  private List<CaseDocumentsDataTableRow> toDataTableRows(
    List<CaseDocumentView> caseDocuments) throws Exception
  {
    List<CaseDocumentsDataTableRow> convertedRows = new ArrayList<>();
    for (CaseDocumentView row : caseDocuments)
    {
      CaseDocumentsDataTableRow dataTableRow =
        new CaseDocumentsDataTableRow(row);
      dataTableRow.setValues(this, row, getTableProperties());
      convertedRows.add(dataTableRow);
    }
    return convertedRows;
  }

  private void spreadDocumentRoles(String docId) throws Exception
  {
    Case caseObject = caseObjectBean.getCase();
    String caseTypeId = caseObject.getCaseTypeId();
    String spreadRoles = getSpreadRolesValue(caseTypeId);

    if (spreadRoles != null) //Spread roles is set
    {
      Document document =
        DocModuleBean.getPort(true).loadDocument(docId, 0, ContentInfo.ID);
      if (document != null)
      {
        boolean update = false;
        if ("true".equalsIgnoreCase(spreadRoles)) //Spread Case roles
        {
          Type caseType = TypeCache.getInstance().getType(caseTypeId);
          List<AccessControl> accessControlList = new ArrayList();
          if (caseType != null)
            accessControlList.addAll(caseType.getAccessControl());
          accessControlList.addAll(caseObject.getAccessControl());
          for (AccessControl ac : accessControlList)
          {
            if (!containsAC(document.getAccessControl(), ac))
            {
              update = true;
              document.getAccessControl().add(ac);
            }
          }
        }
        else //Spread role defined in SPREAD_ROLES_PROPERTY value
        {
          String[] actions = {DictionaryConstants.READ_ACTION,
            DictionaryConstants.WRITE_ACTION,
            DictionaryConstants.DELETE_ACTION};
          for (String action : actions)
          {
            AccessControl ac = new AccessControl();
            ac.setAction(action);
            ac.setRoleId(spreadRoles);
            if (!containsAC(document.getAccessControl(), ac))
            {
              document.getAccessControl().add(ac);
              update = true;
            }
          }
        }

        if (update)
        {
          DocModuleBean.getPort(true).storeDocument(document);
          info("DOCUMENT_SECURITY_UPDATED");
        }
      }
    }
  }

  private String getSpreadRolesValue(String caseTypeId)
  {
    String spreadRoles = null;
    if (caseTypeId != null)
    {
      Type caseType = TypeCache.getInstance().getType(caseTypeId);
      if (caseType != null)
      {
        PropertyDefinition pd =
          caseType.getPropertyDefinition(SPREAD_ROLES_PROPERTY);
        if (pd != null && pd.getValue() != null && !pd.getValue().isEmpty())
        {
          String value = pd.getValue().get(0);
          if (!"false".equals(value))
            spreadRoles = value;
        }
      }
    }
    return spreadRoles;
  }

  private boolean containsAC(List<AccessControl> acl, AccessControl ac)
  {
    for (AccessControl item : acl)
    {
      if (ac.getAction().equals(item.getAction()) &&
          ac.getRoleId().equals(item.getRoleId()))
        return true;
    }
   return false;
  }
  
  private boolean existsSelectItem(List<SelectItem> items, String value)
  {
    for (SelectItem item : items)
    {
      if (item.getValue().equals(value)) return true;
    }
    return false;
  }
  
  public class CaseDocumentsDataTableRow extends DataTableRow
  {
    private String docId;
    private Integer docVersion;
    private String docTypeId;
    private String docTitle;
    private String docSummary;
    private String docLanguage;
    private String docLockUserId;
    private String docState;
    private String docCreationDate;
    private String docCaptureDateTime;
    private String docCaptureUserId;
    private String docChangeDateTime;
    private String docChangeUserId;
    private String docViewUrl;

    public CaseDocumentsDataTableRow(CaseDocumentView row)
    {
      super(row.getCaseDocId(), row.getCaseDocTypeId());
      if (row.getDocument() != null)
      {
        Document doc = row.getDocument();
        docId = doc.getDocId();
        docVersion = doc.getVersion();
        docTypeId = doc.getDocTypeId();
        docTitle = doc.getTitle();
        docSummary = doc.getSummary();
        docLanguage = getDocumentLanguage(doc);
        docLockUserId = doc.getLockUserId();
        if (doc.getState() != null)
        {
          docState = doc.getState().value();
        }
        docCreationDate = doc.getCreationDate();
        docCaptureDateTime = doc.getCaptureDateTime();
        docCaptureUserId = doc.getCaptureUserId();
        docChangeDateTime = doc.getChangeDateTime();
        docChangeUserId = doc.getChangeUserId();
        docViewUrl = DocumentUrlBuilder.getDocumentUrl(doc);
      }
    }

    public String getDocId()
    {
      return docId;
    }

    public void setDocId(String docId)
    {
      this.docId = docId;
    }

    public Integer getDocVersion()
    {
      return docVersion;
    }

    public void setDocVersion(Integer docVersion)
    {
      this.docVersion = docVersion;
    }

    public String getDocTypeId()
    {
      return docTypeId;
    }

    public void setDocTypeId(String docTypeId)
    {
      this.docTypeId = docTypeId;
    }

    public String getDocTitle()
    {
      return docTitle;
    }

    public void setDocTitle(String docTitle)
    {
      this.docTitle = docTitle;
    }

    public String getDocSummary()
    {
      return docSummary;
    }

    public void setDocSummary(String docSummary)
    {
      this.docSummary = docSummary;
    }

    public String getDocLanguage()
    {
      return docLanguage;
    }

    public void setDocLanguage(String docLanguage)
    {
      this.docLanguage = docLanguage;
    }

    public String getDocLockUserId()
    {
      return docLockUserId;
    }

    public void setDocLockUserId(String docLockUserId)
    {
      this.docLockUserId = docLockUserId;
    }

    public String getDocState()
    {
      return docState;
    }

    public void setDocState(String docState)
    {
      this.docState = docState;
    }

    public String getDocCreationDate()
    {
      return docCreationDate;
    }

    public void setDocCreationDate(String docCreationDate)
    {
      this.docCreationDate = docCreationDate;
    }

    public String getDocCaptureDateTime()
    {
      return docCaptureDateTime;
    }

    public void setDocCaptureDateTime(String docCaptureDateTime)
    {
      this.docCaptureDateTime = docCaptureDateTime;
    }

    public String getDocCaptureUserId()
    {
      return docCaptureUserId;
    }

    public void setDocCaptureUserId(String docCaptureUserId)
    {
      this.docCaptureUserId = docCaptureUserId;
    }

    public String getDocChangeDateTime()
    {
      return docChangeDateTime;
    }

    public void setDocChangeDateTime(String docChangeDateTime)
    {
      this.docChangeDateTime = docChangeDateTime;
    }

    public String getDocChangeUserId()
    {
      return docChangeUserId;
    }

    public void setDocChangeUserId(String docChangeUserId)
    {
      this.docChangeUserId = docChangeUserId;
    }

    public String getDocViewUrl()
    {
      return docViewUrl;
    }

    public void setDocViewUrl(String docViewUrl)
    {
      this.docViewUrl = docViewUrl;
    }
    
    @Override
    protected Value getTablePropertyValue(BaseBean baseBean, 
      TableProperty tableProperty, Object row) throws Exception
    {
      if (tableProperty.getName().equals("docTitle"))
      {
        Document document = ((CaseDocumentView)row).getDocument();
        return new DefaultValue(DocumentTypeBean.formatTitle(document), 
          DocumentTypeBean.getContentIcon(document) + 
          " " + tableProperty.getIcon());
      }
      else
        return super.getTablePropertyValue(baseBean, tableProperty, row);
    }    

    @Override
    protected DataTableRow.Value getDefaultValue(String columnName)
    {
      if (columnName != null)
      {
        switch (columnName)
        {
          case "docId":
            return new NumericValue(getDocId());
          case "docVersion":
            return new NumericValue(String.valueOf(getDocVersion()));
          case "docTypeId":
            return new TypeValue(getDocTypeId());
          case "docTitle":
            return new DefaultValue(getDocTitle());
          case "docSummary":
            return new DefaultValue(getDocSummary());
          case "docLanguage":
            return new DefaultValue(getDocLanguage());
          case "docLockUserId":
            return new DefaultValue(getDocLockUserId());
          case "docState":
            return new DefaultValue(getDocState());
          case "docCreationDate":
            return new DateValue(getDocCreationDate());
          case "docCaptureDateTime":
            return new DateValue(getDocCaptureDateTime());
          case "docCaptureUserId":
            return new DefaultValue(getDocCaptureUserId());
          case "docChangeDateTime":
            return new DateValue(getDocChangeDateTime());
          case "docChangeUserId":
            return new DefaultValue(getDocChangeUserId());
          case "docViewUrl":
            return new DefaultValue(getDocViewUrl());
          default:
            break;
        }
      }
      return super.getDefaultValue(columnName);
    }
  }


}
