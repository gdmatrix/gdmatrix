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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;

/**
 *
 * @author realor
 */
@Named
@ManualScoped
public class DocumentFinderBean extends FinderBean
{
  private String smartFilter;
  private DocumentFilter filter = new DocumentFilter();
  private List<Document> rows;
  private int firstRow;
  private boolean isSmartFind;

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

  public DocumentFinderBean()
  {
  }

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
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

  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getDocId();
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

  public List<Document> getRows()
  {
    return rows;
  }

  public void setRows(List<Document> rows)
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

  @Override
  public void smartFind()
  {
    isSmartFind = true;
    doFind(true);
  }

  public void smartClear()
  {
    smartFilter = null;
    rows = null;
  }

  @Override
  public void find()
  {
    isSmartFind = false;
    doFind(true);
  }

  public void clear()
  {
    filter = new DocumentFilter();
    rows = null;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isSmartFind, filter, firstRow, getObjectPosition() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] stateArray = (Object[])state;
    isSmartFind = (Boolean)stateArray[0];
    filter = (DocumentFilter)stateArray[1];

    doFind(false);

    firstRow = (Integer)stateArray[2];
    setObjectPosition((Integer)stateArray[3]);
  }

  private void doFind(boolean autoLoad)
  {
    try
    {
      firstRow = 0;
      String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();

      if (isSmartFind)
      {
        setTabIndex(0);
        filter = documentTypeBean.queryToFilter(smartFilter, baseTypeId);
      }
      else
      {
        smartFilter = documentTypeBean.filterToQuery(filter);
        filter.setDocTypeId(baseTypeId);
        setTabIndex(1);
      }

      filter.setMaxResults(40);
      rows = DocModuleBean.getPort(false).findDocuments(filter);

      if (autoLoad)
      {
        if (rows.size() == 1)
        {
          navigatorBean.view(rows.get(0).getDocId());
          documentObjectBean.setSearchTabIndex(1);
        }
        else
        {
          documentObjectBean.setSearchTabIndex(0);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
