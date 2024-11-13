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
package org.santfeliu.webapp.modules.doc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.matrix.doc.State;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.util.BigList;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.BaseBean;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.util.DataTableRow;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class DocumentFinderBean extends FinderBean
{
  private String smartFilter;
  private DocumentFilter filter = new DocumentFilter();
  private List<DocumentDataTableRow> rows;
  private int firstRow;
  private boolean outdated;
  private String formSelector;
  private List<String> selectedStates;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  DocumentTypeBean documentTypeBean;

  @Inject
  DocumentObjectBean documentObjectBean;

  @Override
  public DocumentObjectBean getObjectBean()
  {
    return documentObjectBean;
  }

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter.setDocTypeId(baseTypeId);     
  }


  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public DocumentFilter getFilter()
  {
    return filter;
  }

  public void setFilter(DocumentFilter filter)
  {
    this.filter = filter;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  public List<TableProperty> getTableProperties()
  {
    try
    {
      if (objectSetup == null)
        loadObjectSetup();
      
      List<TableProperty> tableProperties = 
        objectSetup.getSearchTabs().get(0).getTableProperties();
      
      return tableProperties != null ? tableProperties : 
        Collections.emptyList();
    }
    catch (Exception ex)
    {
      return Collections.emptyList();
    }
  }

  public List<TableProperty> getColumns()
  {
    return TablePropertyHelper.getColumnTableProperties(getTableProperties());
  }  

  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getRowId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  public List<String> getDocIdList()
  {
    return filter.getDocId();
  }

  public void setDocIdList(List<String> docIdList)
  {
    filter.getDocId().clear();
    if (docIdList != null)
    {
      filter.getDocId().addAll(docIdList);
    }
  }

  public String getClassId()
  {
    if (filter.getClassId().isEmpty()) return null;
    return filter.getClassId().get(0);
  }

  public void setClassId(String classId)
  {
    filter.getClassId().clear();

    if (!StringUtils.isBlank(classId))
    {
      filter.getClassId().add(classId);
    }
  }

  public List<DocumentDataTableRow> getRows()
  {
    return rows;
  }

  public void setRows(List<DocumentDataTableRow> rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public List<String> getSelectedStates()
  {
    if (selectedStates == null)
    {
      selectedStates = new ArrayList();
      selectedStates.add(State.DRAFT.value());
      selectedStates.add(State.COMPLETE.value());
      selectedStates.add(State.RECORD.value());
    }
    return selectedStates;
  }

  public void setSelectedStates(List<String> selectedStates)
  {
    this.selectedStates = selectedStates;
  }

  public List<SelectItem> getLanguageValues()
  {
    List<SelectItem> results = new ArrayList();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    Locale currentLocale = new Locale(userSessionBean.getViewLanguage());

    List<Locale> locales = userSessionBean.getSupportedLocales();
    for (Locale locale : locales)
    {
      String language = locale.getLanguage();
      String displayLanguage = locale.getDisplayLanguage(currentLocale);
      SelectItem item = new SelectItem(language, displayLanguage);
      results.add(item);
    }
    results.sort((item1, item2) -> item1.getLabel().compareTo(item2.getLabel()));
    return results;
  }

  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = documentTypeBean.queryToFilter(smartFilter, baseTypeId);
    selectedStates = null;
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    if (StringUtils.isBlank(filter.getDocTypeId()))
    {
      String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      filter.setDocTypeId(baseTypeId);
    }
    filter.getStates().clear();
    if (!getSelectedStates().isEmpty())
    {
      for (String selectedState : getSelectedStates())
      {
        filter.getStates().add(State.fromValue(selectedState));        
      }
    }
    smartFilter = documentTypeBean.filterToQuery(filter);
    doFind(true);
    firstRow = 0;
  }

  public void outdate()
  {
    outdated = true;
  }

  public void update()
  {
    if (outdated)
    {
      doFind(false);
    }
  }

  public void clear()
  {
    filter = new DocumentFilter();
    smartFilter = null;
    rows = null;
    setFinding(false);
    formSelector = null;
    selectedStates = null;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(),
      filter, firstRow, getObjectPosition(), formSelector, rows, outdated, 
      selectedStates };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] stateArray = (Object[])state;
    setFinding((Boolean)stateArray[0]);
    setFilterTabSelector((Integer)stateArray[1]);
    filter = (DocumentFilter)stateArray[2];
    firstRow = (Integer)stateArray[3];
    smartFilter = documentTypeBean.filterToQuery(filter);
    formSelector = (String)stateArray[5];
    rows = (List<DocumentDataTableRow>)stateArray[6];
    outdated = (Boolean)stateArray[7];
    selectedStates = (List<String>)stateArray[8];
    setObjectPosition((Integer)stateArray[4]);
  }

  private void doFind(boolean autoLoad)
  {
    try
    {
      if (!isFinding())
      {
        rows = Collections.EMPTY_LIST;
      }
      else
      {
        if (StringUtils.isBlank(filter.getContentSearchExpression()))
        {
          filter.setContentSearchExpression(null);
        }

        rows = new BigList(20, 10)
        {
          @Override
          public int getElementCount()
          {
            try
            {
              String classId = DocumentFinderBean.this.getClassId();
              if (classId != null)
              {
                List<String> classIds =
                  ClassCache.getInstance().getTerminalClassIds(classId);
                filter.getClassId().clear();
                filter.getClassId().addAll(classIds);
              }
              addFilterWildcards(filter);

              int count = DocModuleBean.getPort(false).countDocuments(filter);
              DocumentFinderBean.this.setClassId(classId);

              removeFilterWildcards(filter);

              return count;
            }
            catch (Exception ex)
            {
              error(ex);
              return 0;
            }
          }

          @Override
          public List getElements(int firstResult, int maxResults)
          {
            try
            {
              addFilterWildcards(filter);

              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              String classId = DocumentFinderBean.this.getClassId();
              if (classId != null)
              {
                List<String> classIds =
                  ClassCache.getInstance().getTerminalClassIds(classId);
                filter.getClassId().clear();
                filter.getClassId().addAll(classIds);
              }
              List<TableProperty> tableProperties = getTableProperties();
              for (TableProperty tableProperty : tableProperties)
              {
                filter.getOutputProperty().add(tableProperty.getName());
              }
              filter.setIncludeContentMetadata(true);
              
              setOrderBy(filter);
              
              List<Document> documents =
                DocModuleBean.getPort(false).findDocuments(filter);
              DocumentFinderBean.this.setClassId(classId);

              removeFilterWildcards(filter);

              return toDataTableRows(documents);
            }
            catch (Exception ex)
            {
              error(ex);
              return null;
            }
          }

          private void addFilterWildcards(DocumentFilter filter)
          {
            String title = filter.getTitle();
            if (!StringUtils.isBlank(title))
            {
              if (!title.startsWith("%")) title = "%" + title;
              if (!title.endsWith("%")) title = title + "%";
              filter.setTitle(title);
            }
          }

          private void removeFilterWildcards(DocumentFilter filter)
          {
            String title = filter.getTitle();
            if (!StringUtils.isBlank(title))
            {
              if (title.startsWith("%")) title = title.substring(1);
              if (title.endsWith("%")) title = title.substring(0, title.length() - 1);
              filter.setTitle(title);
            }
          }
        };

        outdated = false;

        if (autoLoad)
        {
          if (rows.size() == 1)
          {
            navigatorBean.view(rows.get(0).getRowId());
            documentObjectBean.setSearchTabSelector(
              documentObjectBean.getEditModeSelector());
          }
          else
          {
            documentObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private List<DocumentDataTableRow> toDataTableRows(List<Document> documents)
    throws Exception
  {
    List<DocumentDataTableRow> convertedRows = new ArrayList<>();
    for (Document document : documents)
    {
      DocumentDataTableRow dataTableRow =
        new DocumentDataTableRow(document.getDocId(), document.getDocTypeId());
      dataTableRow.setValues(this, document, getTableProperties());
      convertedRows.add(dataTableRow);
    }
    return convertedRows;
  }
  
  private void setOrderBy(DocumentFilter filter) throws Exception
  {
    if (objectSetup == null)
      loadObjectSetup();

    int tabSelector = documentObjectBean.getSearchTabSelector();
    tabSelector = 
      tabSelector < objectSetup.getSearchTabs().size() ? tabSelector : 0;      
    List<String> orderBy = 
      objectSetup.getSearchTabs().get(tabSelector).getOrderBy();
    
    if (orderBy != null && !orderBy.isEmpty())
    {
      for (String item : orderBy)
      {
        String[] parts = item.split(":");
        OrderByProperty orderByProperty = new OrderByProperty();
        orderByProperty.setName(parts[0]);
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
        {
          orderByProperty.setDescending(true);
        }
        filter.getOrderByProperty().add(orderByProperty);
      }
    }
  }  
  
  public static class DocumentDataTableRow extends DataTableRow
  {
    private String contentId;
    private String contentType;

    public DocumentDataTableRow(String rowId, String typeId)
    {
      super(rowId, typeId);
    }

    @Override
    public void setValues(BaseBean baseBean, Object row, 
      List<TableProperty> columns) throws Exception
    {
      super.setValues(baseBean, row, columns);
      Document document = (Document)row;
      Content content = document.getContent();
      if (content != null)
      {
        contentId = content.getContentId();
        contentType = content.getContentType();
      }
    }

    public String getContentId()
    {
      return contentId;
    }

    public String getContentType()
    {
      return contentType;
    }

    public String getViewURL()
    {
      if (contentType == null) return null;

      String extension =
        MimeTypeMap.getMimeTypeMap().getExtension(contentType);

      return "/documents/" + contentId + "/" + rowId + "." + extension;
    }

    public String getDownloadURL()
    {
      if (contentType == null) return null;

      String extension =
        MimeTypeMap.getMimeTypeMap().getExtension(contentType);

      return "/documents/" + contentId + "?saveas="  + rowId + "." + extension;
    }
  }
}
