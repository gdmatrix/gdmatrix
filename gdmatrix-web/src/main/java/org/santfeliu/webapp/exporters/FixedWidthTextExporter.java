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
import java.util.Collections;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.datatable.export.DataTableExporter;
import org.primefaces.component.export.DataExporters;
import org.primefaces.component.export.ExporterOptions;

/**
 *
 * @author realor
 */
public class FixedWidthTextExporter
  extends DataTableExporter<PrintWriter, ExporterOptions>
{
  private static boolean registered;
  static final String FORMAT_TAG = "$";

  public FixedWidthTextExporter()
  {
    super(null, Collections.emptySet(), false);
  }

  public static void register()
  {
    if (!registered)
    {
      DataExporters.register(DataTable.class, FixedWidthTextExporter.class, "txt");
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
  protected void postRowExport(FacesContext context, DataTable table)
  {
    document.append("\n");
  }

  @Override
  protected void exportCellValue(FacesContext context, DataTable table,
    UIColumn col, String text, int index)
  {
    String title = col.getTitle();
    int width = 10;
    if (title != null)
    {
      int pos = title.indexOf(FORMAT_TAG);
      if (pos != -1)
      {
        String swidth = title.substring(pos + FORMAT_TAG.length());
        try
        {
          width = Integer.parseInt(swidth);
        }
        catch (Exception ex)
        {
        }
      }
    }
    if (width > 0)
    {
      if (text.length() > width)
      {
        text = text.substring(0, width);
      }
      else
      {
        while (text.length() < width)
        {
          text = " " + text;
        }
      }
    }
    document.append(text);
  }

  @Override
  public String getContentType()
  {
    return "text/plain";
  }

  @Override
  public String getFileExtension()
  {
    return ".txt";
  }
}
