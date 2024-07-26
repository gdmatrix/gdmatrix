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
package org.santfeliu.webapp.modules.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.EnumTypeItem;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.report.Report;
import org.santfeliu.dic.EnumTypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import static org.santfeliu.webapp.modules.report.ReportTypeBean.REPORT_DOC_TYPE;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class ReportFinderBean extends FinderBean
{
  private static final String REPORT_THEME_PROPERTY = "reportTheme";
  
  private String smartFilter;
  private DocumentFilter filter = new DocumentFilter();  
  private List<Report> rows;
  private int firstRow;
  private boolean outdated; 
  
  private String reportId;
  private String technology;
  private String theme;
  private List<EnumTypeItem> themeItems;
  
  @Inject
  NavigatorBean navigatorBean;  
  
  @Inject
  ReportTypeBean reportTypeBean;  
  
  @Inject
  ReportObjectBean reportObjectBean;  
  
  @PostConstruct
  public void init()
  {
    try    
    {
      themeItems = EnumTypeCache.getInstance().getItems("ReportThemes");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public List<Report> getRows()
  {
    return rows;
  }

  public void setRows(List<Report> rows)
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

  public boolean isOutdated()
  {
    return outdated;
  }

  public void setOutdated(boolean outdated)
  {
    this.outdated = outdated;
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

  public String getReportId()
  {
    return reportId;
  }

  public void setReportId(String reportId)
  {
    this.reportId = reportId;
  }

  public String getTechnology()
  {
    return technology;
  }

  public void setTechnology(String technology)
  {
    this.technology = technology;
  }

  public String getTheme()
  {
    return theme;
  }

  public void setTheme(String theme)
  {
    this.theme = theme;
  }
  
  public String getConfiguredTheme()
  {
    return getProperty(REPORT_THEME_PROPERTY);
  }
  
  public boolean isThemeConfigured()
  {
    return !StringUtils.isBlank(getConfiguredTheme());
  }

  public List<EnumTypeItem> getThemeItems()
  {
    return themeItems;
  }

  @Override
  public void smartFind()
  {
  }


  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    smartFilter = reportTypeBean.filterToQuery(filter);
    doFind(true);
    filter.getProperty().clear();    
    firstRow = 0;
  }
  
  public void outdate()
  {
    this.outdated = true;
  }

  public void update()
  {
    if (outdated)
    {
      doFind(false);
    }
  }  
  
  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getReportId();
  }  
  
  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }  
  
  @Override
  public ObjectBean getObjectBean()
  {
    return reportObjectBean;
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
        theme = isThemeConfigured() ? getConfiguredTheme() : theme;
        rows = new BigList(20, 10)
        {
          @Override
          public int getElementCount()
          {
            try
            {
              addFilterWildcards(filter);
              filter.setDocTypeId(REPORT_DOC_TYPE);
              if (!StringUtils.isBlank(reportId))
                DictionaryUtils.setProperty(filter, "report", reportId);
              if (!StringUtils.isBlank(technology))
                DictionaryUtils.setProperty(filter, "technology", technology);   
              if (!StringUtils.isBlank(theme))
                DictionaryUtils.setProperty(filter, "theme", theme);               
              
              int count = DocModuleBean.getPort(false).countDocuments(filter);

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
              if (!StringUtils.isBlank(reportId))
                DictionaryUtils.setProperty(filter, "report", reportId);
              if (!StringUtils.isBlank(technology))
                DictionaryUtils.setProperty(filter, "technology", technology);
              if (!StringUtils.isBlank(theme))
                DictionaryUtils.setProperty(filter, "theme", theme);                
              filter.setDocTypeId(REPORT_DOC_TYPE);
              filter.getOutputProperty().add("report");
              filter.getOutputProperty().add("technology");
              filter.getOutputProperty().add("theme");              
              filter.getOutputProperty().add("defaultConnectionName");
              filter.setIncludeContentMetadata(false);
              
              List<Document> documents =
                DocModuleBean.getPort(false).findDocuments(filter);

              removeFilterWildcards(filter);

              return toReports(documents);
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
            navigatorBean.view(rows.get(0).getReportId());
            reportObjectBean.setSearchTabSelector(
              reportObjectBean.getEditModeSelector());
          }
          else
          {
            reportObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private List<Report> toReports(List<Document> documents) 
    throws Exception
  {
    List<Report> convertedRows = new ArrayList();
    for (Document doc : documents)
    {
      Report report = new Report();
      report.setReportId(
        DictionaryUtils.getPropertyValue(doc.getProperty(), "report"));
      report.setTechnology(
        DictionaryUtils.getPropertyValue(doc.getProperty(), "technology"));
      report.setDefaultConnectionName(
        DictionaryUtils.getPropertyValue(doc.getProperty(), "defaultConnectionName"));
      report.setDocId(doc.getDocId());
      report.setTitle(doc.getTitle());
      report.getProperty().addAll(doc.getProperty());

      convertedRows.add(report);
    }
    
    return convertedRows;       
  }  
  
}
