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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
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
import org.santfeliu.webapp.exporters.CSVDataTableRowsExporter;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.DataTableRowExportable;
import org.santfeliu.webapp.helpers.RowsExportHelper;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class DocumentFinderBean extends FinderBean 
  implements DataTableRowExportable
{
  private String smartFilter;
  private DocumentFilter filter = new DocumentFilter();
  private List<DocumentDataTableRow> rows;
  private int firstRow;
  private boolean outdated;
  private String formSelector;
  private List<String> selectedStates;
  private String sortBy;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  DocumentTypeBean documentTypeBean;

  @Inject
  DocumentObjectBean documentObjectBean;

  @PostConstruct
  public void init()
  {
    CSVDataTableRowsExporter.register();
  }  
  
  @Override
  public DocumentObjectBean getObjectBean()
  {
    return documentObjectBean;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  @Override
  public DocumentFilter getFilter()
  {
    if (filter != null && StringUtils.isBlank(filter.getDocTypeId()))
    {
      String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      filter.setDocTypeId(baseTypeId);
    }
    return filter != null ? (DocumentFilter) getSessionProperties(filter) : filter;
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

  @Override
  public List<TableProperty> getTableProperties()
  {
    try
    {
      List<TableProperty> tableProperties =
        getObjectSetup().getSearchTabs().get(0).getTableProperties();

      return tableProperties != null ? tableProperties :
        Collections.emptyList();
    }
    catch (Exception ex)
    {
      return Collections.emptyList();
    }
  }

  @Override
  public List<TableProperty> getColumns()
  {
    return TablePropertyHelper.getColumnTableProperties(getTableProperties());
  }
  
  @Override
  public List<? extends DataTableRow> getExportableRows()
  {
    if (rows.size() <= getPageSize())
    {
      return rows;
    }
    else
    {
      if (StringUtils.isBlank(filter.getContentSearchExpression()))
      {
        filter.setContentSearchExpression(null);
      }
      return ((BigList)rows).getElements(0, Integer.MAX_VALUE);
    }
  }

  @Override
  public int getRowExportLimit()
  {
    return RowsExportHelper.getActiveSearchTabRowExportLimit(
      documentObjectBean);
  }
  
  @Override
  public boolean isExportable()
  {
    return RowsExportHelper.isActiveSearchTabExportable(documentObjectBean);
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

  @Override
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

  public String getSortBy() 
  {
    return sortBy;
  }

  public void setSortBy(String sortBy) 
  {
    this.sortBy = sortBy;
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

  public void sortByColumn(String columnName)
  {
    if (sortBy == null) //first sort
    {
      sortBy = columnName + ":asc";  
    }
    else
    {
      String currentColumnName = sortBy.split(":")[0];
      if (currentColumnName.equals(columnName)) //direction switch
      {
        if (sortBy.endsWith(":desc"))
        {
          sortBy = columnName + ":asc";
        }
        else
        {
          sortBy = columnName + ":desc";
        }
      }
      else
      {
        sortBy = columnName + ":asc";
      }
    }
    find();
  }
  
  public String getSortIcon(String columnName)
  {
    if (!getOrderByColumns().contains(columnName) || !isFinding())
    {
      return null; //no sorting enabled
    }
    else if (sortBy == null) //sorting enabled, but no sort column selected
    {
      return "pi pi-sort-alt";
    }    
    else //sorting enabled, and sort column selected
    {
      String currentColumnName = sortBy.split(":")[0];
      if (currentColumnName.equals(columnName)) //sorted by this column
      {
        if (sortBy.endsWith(":desc")) //desc
        {
          return "pi pi-sort-amount-down";
        }
        else //asc
        {
          return "pi pi-sort-amount-up";
        }
      }
      else //not sorted by this column
      {
        return "pi pi-sort-alt";
      }
    }
  }  
  
  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = documentTypeBean.queryToFilter(smartFilter, baseTypeId);    
    selectedStates = null;
    clearSessionProperties();      
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
    setSessionProperties(filter);    
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

  @Override
  public void clear()
  {
    super.clear();
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
      selectedStates, getPageSize(), sortBy };
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
    setObjectPosition((Integer)stateArray[4]);
    formSelector = (String)stateArray[5];
    rows = (List<DocumentDataTableRow>)stateArray[6];
    outdated = (Boolean)stateArray[7];
    selectedStates = (List<String>)stateArray[8];
    setPageSize((Integer)stateArray[9]);
    sortBy = (String)stateArray[10];    
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

        rows = new BigList(2 * getPageSize() + 1, getPageSize())
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
              filter.getOutputProperty().clear();
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
      dataTableRow.setStyleClass(getRowStyleClass(document));      
      convertedRows.add(dataTableRow);
    }
    return convertedRows;
  }

  private List<String> getOrderByColumns()
  {
    try
    {
      int tabSelector = documentObjectBean.getSearchTabSelector();
      tabSelector =
        tabSelector < getObjectSetup().getSearchTabs().size() ? tabSelector : 0;
      List<String> orderByColumns = 
        getObjectSetup().getSearchTabs().get(tabSelector).getOrderByColumns();
      if (orderByColumns == null || orderByColumns.isEmpty())
      {
        //default value
        orderByColumns = Arrays.asList("docId", "title", "docTypeId");
      }
      if (!isFinding())
      {
        sortBy = null;
      }
      return new ArrayList(orderByColumns);      
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }  
  
  private void setOrderBy(DocumentFilter filter) throws Exception
  {
    List<String> orderBy;
    if (sortBy == null)
    {
      int tabSelector = documentObjectBean.getSearchTabSelector();
      tabSelector =
        tabSelector < getObjectSetup().getSearchTabs().size() ? tabSelector : 0;
      orderBy = getObjectSetup().getSearchTabs().get(tabSelector).getOrderBy();
    }
    else
    {
      orderBy = Arrays.asList(sortBy);
    }

    filter.getOrderByProperty().clear();
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

  private String getRowStyleClass(Document document)
  {
    if (document.getState() != null)
    {
      return document.getState().value().toLowerCase();
    }
    return "";
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

    @Override
    protected Value getTablePropertyValue(BaseBean baseBean, 
      TableProperty tableProperty, Object row) throws Exception
    {
      if (tableProperty.getName().equals("title"))
      {
        Document document = (Document)row;
        return new DefaultValue(DocumentTypeBean.formatTitle(document), 
          DocumentTypeBean.getContentIcon(document) + 
          " " + tableProperty.getIcon());
      }
      else
        return super.getTablePropertyValue(baseBean, tableProperty, row);
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
