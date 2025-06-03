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
package org.santfeliu.webapp.exporters;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import org.apache.commons.lang.StringUtils;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.datatable.export.DataTableExporter;
import org.primefaces.component.export.DataExporters;
import org.primefaces.component.export.ExporterOptions;
import org.santfeliu.util.HTMLNormalizer;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.DataTableRow.CustomProperty;
import org.santfeliu.webapp.DataTableRowExportable;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author lopezrj-sf
 */
public class CSVDataTableRowsExporter 
  extends DataTableExporter<PrintWriter, ExporterOptions> 
{
  private static boolean registered;
  private static final String SEPARATOR = ";";
  private static final String QUOTE = "\"";

  public CSVDataTableRowsExporter() 
  {
    super(null, Collections.emptySet(), false);
  }

  public static void register() 
  {
    if (!registered) 
    {
      DataExporters.register(DataTable.class,
        CSVDataTableRowsExporter.class, "csv_dtr");
      registered = true;
    }
  }

  @Override
  protected PrintWriter createDocument(FacesContext context) throws IOException 
  {
    try 
    {
      OutputStreamWriter osw = new OutputStreamWriter(os(),
        exportConfiguration.getEncodingType());
      return new PrintWriter(osw);
    } 
    catch (UnsupportedEncodingException e) 
    {
      throw new FacesException(e);
    }
  }

  @Override
  protected void exportTable(FacesContext context, DataTable table, int index)
    throws IOException 
  {
    ApplicationBean applicationBean = WebUtils.getBean("applicationBean");
    DataTableRowExportable exportableBean = 
      (DataTableRowExportable)table.getAttributes().get("exportableBean");

    List<TableProperty> tpList = new ArrayList<>();
    List<String> customColNames = new ArrayList<>();
    boolean firstColumn = true;
    for (TableProperty column : exportableBean.getColumns()) 
    {
      tpList.add(column);
      if (isExportable(column))
      {
        if (!firstColumn) document.append(SEPARATOR);
        document.append(QUOTE).append(getHeaderText(column, applicationBean)).
          append(QUOTE);
        firstColumn = false;
      }
    }
    for (TableProperty tp : exportableBean.getTableProperties()) 
    {
      if ("row".equals(tp.getMode())) //custom property
      {
        tpList.add(tp);
        if (isExportable(tp))
        {
          if (!firstColumn) document.append(SEPARATOR);
          document.append(QUOTE).append(getHeaderText(tp, applicationBean)).
            append(QUOTE);
          firstColumn = false;
        }
        customColNames.add(tp.getName());
      }
    }
    document.append("\n");
    List<? extends DataTableRow> allRows = exportableBean.getExportableRows();
    if (allRows != null) 
    {
      for (DataTableRow row : allRows) 
      {
        int iCol = 0;
        firstColumn = true;
        //Columns
        for (DataTableRow.Value value : row.getValues()) 
        {
          if (isExportable(tpList.get(iCol)))
          {
            if (!firstColumn) document.append(SEPARATOR);
            document.append(QUOTE);
            if (value != null) 
            {
              document.append(getFieldText(value.getLabel(),
                tpList.get(iCol).isEscape()));
            }
            document.append(QUOTE);
            firstColumn = false;
          }
          iCol++;
        }
        //Rows
        for (String customColName : customColNames) 
        {
          if (isExportable(tpList.get(iCol)))
          {
            if (!firstColumn) document.append(SEPARATOR);
            document.append(QUOTE);
            CustomProperty cp = row.getCustomProperty(customColName);
            if (cp != null && cp.getValue() != null) 
            {
              document.append(getFieldText(cp.getValue().getLabel(),
                tpList.get(iCol).isEscape()));
            }
            document.append(QUOTE);
            firstColumn = false;
          }
          iCol++;
        }
        document.append("\n");
      }
    } 
  }

  @Override
  protected void exportCellValue(FacesContext context, DataTable table,
    UIColumn col, String text, int index)
  {
  }

  @Override
  public String getContentType() 
  {
    return "text/csv";
  }

  @Override
  public String getFileExtension() 
  {
    return ".csv";
  }

  private String getFieldText(String text, boolean escapeHTML)
  {
    return (escapeHTML ? 
      StringUtils.defaultString(text) : 
      HTMLNormalizer.cleanHTML(text, true));
  }

  private String getHeaderText(TableProperty tp, 
    ApplicationBean applicationBean) 
  {
    String text = Objects.toString(tp.getExportLabel(), tp.getLabel());
    return applicationBean.translate(text);
  }

  private boolean isExportable(TableProperty tp)
  {
    return (tp.getExportable() != null ? tp.getExportable() : true);
  }

}