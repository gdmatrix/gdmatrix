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
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.dic.Property;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentUrlBuilder;
import org.santfeliu.faces.matrixclient.model.DefaultMatrixClientModel;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.TypeSelectHelper;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.modules.doc.DocumentTypeBean;
import org.santfeliu.webapp.setup.Column;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.DataTableRowComparator;
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
    TypeSelectHelper typeSelectHelper = new TypeSelectHelper()
    {
      @Override
      public List<? extends DataTableRow> getRows()
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
      public String getTabBaseTypeId()
      {
        return CaseDocumentsTabBean.this.getTabBaseTypeId();        
      }

      @Override
      public void resetFirstRow()
      {
        firstRow = 0;
      }      
    };
    
    public TypeSelectHelper getTypeSelectHelper()
    {
      return typeSelectHelper;
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
    System.out.println("Creating " + this);
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return CaseDocumentsTabBean.this.getObjectBean();
      }

      @Override
      public List<Column> getColumns()
      {
        return CaseDocumentsTabBean.this.getColumns();
      }

      @Override
      public void sortRows()
      {
        Collections.sort(getCurrentTabInstance().rows,
          new DataTableRowComparator(getColumns(), getOrderBy()));
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

  public List<Column> getColumns()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getColumns();
    else
      return Collections.EMPTY_LIST;
  }

  public List<Column> getCustomColumns()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getCustomColumns();
    else
      return Collections.EMPTY_LIST;
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

        List<CaseDocumentView> result;
        String typeId = getTabBaseTypeId();
        if (typeId == null)
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
        List<CaseDocumentsDataTableRow> auxList2 = toDataTableRows(result);
        Collections.sort(auxList2,
          new DataTableRowComparator(getColumns(), getOrderBy()));
        setRows(auxList2);
        getCurrentTabInstance().typeSelectHelper.load();        
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
      getCurrentTabInstance().typeSelectHelper.load();      
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

  public void edit(DataTableRow row)
  {
    if (row != null)
    {
      try
      {
        editing =
          CasesModuleBean.getPort(false).loadCaseDocument(row.getRowId());
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
  
  public void addRowCustomProperty(DataTableRow row, String name, String value)
  {
    Property auxProperty = new Property();
    auxProperty.setName(name);
    auxProperty.getValue().add(value);
    row.getCustomProperties().add(auxProperty);
  }  
  
  @Override
  public void clear()
  {
    tabInstances.clear();
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

  private List<CaseDocumentsDataTableRow> toDataTableRows(
    List<CaseDocumentView> caseDocuments) throws Exception
  {
    List<CaseDocumentsDataTableRow> convertedRows = new ArrayList<>();
    for (CaseDocumentView row : caseDocuments)
    {
      CaseDocumentsDataTableRow dataTableRow =
        new CaseDocumentsDataTableRow(row);
      dataTableRow.setValues(this, row, getColumns());
      dataTableRow.setCustomValues(this, row, getCustomColumns());
      convertedRows.add(dataTableRow);
    }
    return convertedRows;
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
