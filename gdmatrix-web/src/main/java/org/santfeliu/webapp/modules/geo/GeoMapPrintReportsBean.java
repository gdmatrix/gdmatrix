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
package org.santfeliu.webapp.modules.geo;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.doc.Document;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.santfeliu.util.IOUtils;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.SvgStore;
import org.santfeliu.webapp.modules.geo.metadata.PrintReport;
import org.santfeliu.webapp.modules.geo.metadata.StyleMetadata;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapPrintReportsBean extends WebBean implements Serializable
{
  private PrintReport editingPrintReport;
  private PrintReport reportToUpload;

  @Inject
  GeoMapBean geoMapBean;

  public List<PrintReport> getPrintReports()
  {
    StyleMetadata styleMetadata = new StyleMetadata(geoMapBean.getStyle());
    return styleMetadata.getPrintReports(true);
  }

  public PrintReport getEditingPrintReport()
  {
    return editingPrintReport;
  }

  public void setPrintReportToUpload(PrintReport printReport)
  {
    reportToUpload = printReport;
  }

  public void uploadPrintReportFile(FileUploadEvent event)
  {
    UploadedFile reportFileToUpload = event.getFile();
    if (reportToUpload != null)
    {
      File fileToStore = null;
      try
      {
        fileToStore = File.createTempFile("template", ".svg");
        try (InputStream is = reportFileToUpload.getInputStream())
        {
          IOUtils.writeToFile(is, fileToStore);
        }
        geoMapBean.getSvgStore().storeSvg(reportToUpload.getReportName(),
          reportToUpload.getLabel(), fileToStore);
        reportToUpload = null;
      }
      catch (Exception ex)
      {
        error(ex);
      }
      finally
      {
        try
        {
          if (fileToStore != null) fileToStore.delete();
          reportFileToUpload.delete();
        }
        catch (Exception ex2)
        {
        }
      }
    }
  }

  public void addPrintReport()
  {
    editingPrintReport = new PrintReport();
    geoMapBean.setDialogVisible(true);
  }

  public void editPrintReport(PrintReport printReport)
  {
    editingPrintReport = printReport;
    geoMapBean.setDialogVisible(true);
  }

  public void removePrintReport(PrintReport printReport)
  {
    getPrintReports().remove(printReport);
  }

  public void acceptPrintReport()
  {
    if (!getPrintReports().contains(editingPrintReport))
    {
      getPrintReports().add(editingPrintReport);
    }

    if (StringUtils.isBlank(editingPrintReport.getLabel()))
    {
      String reportName = editingPrintReport.getReportName();
      try
      {
        Document document =
          geoMapBean.getSvgStore().getReportDocument(reportName, null, false);
        String title = document.getTitle();
        int index = title.indexOf(":");
        String label = index == -1 ? title : title.substring(index + 1);
        editingPrintReport.setLabel(label);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }

    editingPrintReport = null;
    geoMapBean.setDialogVisible(false);
  }

  public void cancelPrintReport()
  {
    editingPrintReport = null;
    geoMapBean.setDialogVisible(false);
  }

  public String getPrintReportUrl(String reportName)
  {
    return geoMapBean.getSvgStore().getReportUrl(reportName);
  }

  public boolean isSvgPrintReport(String reportName)
  {
    return geoMapBean.getSvgStore().getReportUrl(reportName).endsWith(".svg");
  }

  public boolean isUploadablePrintReport(String reportName)
  {
    String url = geoMapBean.getSvgStore().getReportUrl(reportName);
    return url.endsWith(".svg") || url.equals("#");
  }

  public List<String> completeReportName(String text)
  {
    try
    {
      SvgStore svgStore = geoMapBean.getSvgStore();
      return svgStore.findSvg(text);
    }
    catch (Exception ex)
    {
      // ignore;
      return Collections.EMPTY_LIST;
    }
  }

}
