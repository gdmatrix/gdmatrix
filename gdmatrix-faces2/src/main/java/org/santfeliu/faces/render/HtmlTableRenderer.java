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
package org.santfeliu.faces.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import org.apache.myfaces.component.html.ext.HtmlDataTable;
import org.apache.myfaces.custom.column.HtmlSimpleColumn;
import org.apache.myfaces.renderkit.html.util.ColumnInfo;
import org.apache.myfaces.renderkit.html.util.RowInfo;
import org.apache.myfaces.renderkit.html.util.TableContext;

/**
 * Renderer of MyFaces HtmlDatatable to change order of footer encoding and do 
 * it after body encoding for accessibility purposes.
 *
 * @author blanquepa
 */
@FacesRenderer(componentFamily="javax.faces.Data",
	rendererType="org.apache.myfaces.Table")
public class HtmlTableRenderer extends org.apache.myfaces.renderkit.html.ext.HtmlTableRenderer
{
  @Override
  protected void beforeBody(FacesContext facesContext, UIData uiData) throws IOException
  {
    if (isGroupedTable(uiData)) {
      createColumnInfos((HtmlDataTable) uiData, facesContext);
    }
    ResponseWriter writer = facesContext.getResponseWriter();
    renderFacet(facesContext, writer, uiData, true);
  } 

  @Override
  protected void afterBody(FacesContext facesContext, UIData uiData) throws IOException
  {
    ResponseWriter writer = facesContext.getResponseWriter();
    ((HtmlDataTable)uiData).setRowIndex(-1); //reset rowIndex to avoid render footer facet as a row (clientId with dataTable index)
    renderFacet(facesContext, writer, uiData, false);
  }
  
  private boolean isGroupedTable(UIData uiData) {
      if (uiData instanceof HtmlDataTable) {
          List children = getChildren(uiData);
          for (int j = 0, size = getChildCount(uiData); j < size; j++) {
              UIComponent child = (UIComponent) children.get(j);
              if (child instanceof HtmlSimpleColumn) {
                  HtmlSimpleColumn column = (HtmlSimpleColumn) child;
                  if (column.isGroupBy()) {
                      return true;
                  }
              }
          }
      }

      return false;  //To change body of created methods use File | Settings | File Templates.
  }

  private void createColumnInfos(HtmlDataTable htmlDataTable, FacesContext facesContext)
      throws IOException {
      int first = htmlDataTable.getFirst();
      int rows = htmlDataTable.getRows();
      int last;
      int currentRowSpan = -1;
      int currentRowInfoIndex = -1;

      TableContext tableContext = htmlDataTable.getTableContext();
      RowInfo rowInfo = null;
      ColumnInfo columnInfo = null;
      HtmlSimpleColumn currentColumn = null;
      Map groupHashTable = new HashMap();

      if (rows <= 0) {
          last = htmlDataTable.getRowCount();
      }
      else {
          last = first + rows;
      }

      //Loop over the Children Columns to find the Columns with groupBy Attribute true
      List children = getChildren(htmlDataTable);
      int nChildren = getChildCount(htmlDataTable);

      for (int j = 0, size = nChildren; j < size; j++) {
          UIComponent child = (UIComponent) children.get(j);
          if (child instanceof HtmlSimpleColumn) {
              currentColumn = (HtmlSimpleColumn) child;
              if (currentColumn.isGroupBy()) {
                  groupHashTable.put(new Integer(j), null);
              }
          }
      }

      boolean groupEndReached = false;

      for (int rowIndex = first; last == -1 || rowIndex < last; rowIndex++) {
          htmlDataTable.setRowIndex(rowIndex);
          rowInfo = new RowInfo();
          //scrolled past the last row
          if (!htmlDataTable.isRowAvailable()) {
              break;
          }

          Set groupIndexList = groupHashTable.keySet();
          List currentColumnContent = null;
          for (Iterator it = groupIndexList.iterator(); it.hasNext();) {
              currentColumnContent = new ArrayList();
              Integer currentIndex = (Integer) it.next();
              currentColumn = (HtmlSimpleColumn) children.get(currentIndex.intValue());

              if (currentColumn.isGroupByValueSet()) {
                  currentColumnContent.add(currentColumn.getGroupByValue());
              }
              else {
                  // iterate the children - this avoids to add the column facet too
                  List currentColumnChildren = currentColumn.getChildren();
                  if (currentColumnChildren != null) {
                      collectChildrenValues(currentColumnContent, currentColumnChildren.iterator());
                  }
              }

              if (!isListEqual(currentColumnContent, (List) groupHashTable.get(currentIndex)) &&
                  currentRowInfoIndex > -1) {
                  groupEndReached = true;
                  groupHashTable.put(currentIndex, currentColumnContent);
              }
              else if (currentRowInfoIndex == -1) {
                  groupHashTable.put(currentIndex, currentColumnContent);
              }
          }
          currentRowSpan++;


          for (int j = 0, size = nChildren; j < size; j++) {
              columnInfo = new ColumnInfo();
              if (groupHashTable.containsKey(new Integer(j)))  // Column is groupBy
              {
                  if (currentRowSpan > 0) {
                      if (groupEndReached) {
                          ((ColumnInfo)
                              ((RowInfo)
                                  tableContext.getRowInfos().get(currentRowInfoIndex - currentRowSpan + 1)).
                                  getColumnInfos().get(j)).
                              setRowSpan(currentRowSpan);
                          columnInfo.setStyle(htmlDataTable.getRowGroupStyle());
                          columnInfo.setStyleClass(htmlDataTable.getRowGroupStyleClass());
                      }
                      else {
                          columnInfo.setRendered(false);
                      }
                  }
                  else {
                      columnInfo.setStyle(htmlDataTable.getRowGroupStyle());
                      columnInfo.setStyleClass(htmlDataTable.getRowGroupStyleClass());
                  }

              }
              else    // Column  is not group by
              {
                  if (groupEndReached) {
                      ((ColumnInfo)
                          ((RowInfo)
                              tableContext.getRowInfos().get(currentRowInfoIndex)).
                              getColumnInfos().get(j)).
                          setStyle(htmlDataTable.getRowGroupStyle());
                      ((ColumnInfo)
                          ((RowInfo)
                              tableContext.getRowInfos().get(currentRowInfoIndex)).
                              getColumnInfos().get(j)).
                          setStyleClass(htmlDataTable.getRowGroupStyleClass());
                  }
              }
              rowInfo.getColumnInfos().add(columnInfo);
          }
          if (groupEndReached) {
              currentRowSpan = 0;
              groupEndReached = false;
          }
          tableContext.getRowInfos().add(rowInfo);
          currentRowInfoIndex++;
      }

      // do further processing if we've found at least one row
      if (currentRowInfoIndex > -1) {
          for (int j = 0, size = nChildren; j < size; j++) {
              if (groupHashTable.containsKey(new Integer(j)))  // Column is groupBy
              {
                  ((ColumnInfo)
                      ((RowInfo)
                          tableContext.getRowInfos().get(currentRowInfoIndex - currentRowSpan)).
                          getColumnInfos().get(j)).
                      setRowSpan(currentRowSpan + 1);
              }
              else    // Column  is not group by
              {
                  ((ColumnInfo)
                      ((RowInfo)
                          tableContext.getRowInfos().get(currentRowInfoIndex)).
                          getColumnInfos().get(j)).
                      setStyle(htmlDataTable.getRowGroupStyle());
                  ((ColumnInfo)
                      ((RowInfo)
                          tableContext.getRowInfos().get(currentRowInfoIndex)).
                          getColumnInfos().get(j)).
                      setStyleClass(htmlDataTable.getRowGroupStyleClass());
              }
          }
      }

      htmlDataTable.setRowIndex(-1);
  }    
}
