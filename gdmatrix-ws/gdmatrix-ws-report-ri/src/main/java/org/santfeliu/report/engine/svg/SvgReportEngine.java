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
package org.santfeliu.report.engine.svg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import org.matrix.report.ExportOptions;
import org.matrix.report.Parameter;
import org.matrix.report.ParameterDefinition;
import org.santfeliu.pdfgen.PdfGenerator;
import org.santfeliu.report.engine.ReportEngine;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.TemporaryDataSource;

/**
 *
 * @author realor
 */
public class SvgReportEngine implements ReportEngine
{
  static final Logger LOGGER = Logger.getLogger("SvgReportEngine");

  public SvgReportEngine()
  {
    init();
  }

  @Override
  public String getInfo()
  {
    return "SvgReportEngine";
  }

  @Override
  public List<ParameterDefinition> readReportParameters(
    String contentId, DataSource dataSource)
  {
    return Collections.EMPTY_LIST;
  }

  @Override
  public DataHandler executeReport(String contentId, DataSource dataSource,
    String connectionName, List<Parameter> parameters,
    ExportOptions exportOptions, Credentials credentials)
  {
    try
    {
      PdfGenerator gen = new PdfGenerator();
      Map context = gen.getContext();
      context.put("credentials", credentials);
      for (Parameter parameter : parameters)
      {
        context.put(parameter.getName(), parameter.getValue());
      }
      if (connectionName != null)
      {
        context.put("connectionName", connectionName);
      }
      File templateFile = getTemplateFile(dataSource);
      File outputFile = File.createTempFile("temp", ".pdf");
      try (FileOutputStream fos = new FileOutputStream(outputFile))
      {
        gen.open(fos);
        gen.addPage(templateFile);
        gen.close();
      }
      TemporaryDataSource result =
        new TemporaryDataSource(outputFile, "application/pdf");
      return new DataHandler(result);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, ex.toString());
    }
    return null;
  }

  private File getTemplateFile(DataSource dataSource) throws IOException
  {
    File templateFile;
    if (dataSource instanceof javax.activation.FileDataSource)
    {
      javax.activation.FileDataSource fileDataSource =
        (javax.activation.FileDataSource)dataSource;
      templateFile = fileDataSource.getFile();
    }
    else
    {
      templateFile = IOUtils.writeToFile(dataSource.getInputStream());
    }
    return templateFile;
  }

  private void init()
  {
    // register components
    try
    {
      PdfGenerator.registerBridge("org.santfeliu.misc.mapviewer.pdfgen.MapRectElementBridge");
      PdfGenerator.registerBridge("org.santfeliu.misc.mapviewer.pdfgen.MapTextElementBridge");
    }
    catch (Exception ex)
    {
    }
  }
}
