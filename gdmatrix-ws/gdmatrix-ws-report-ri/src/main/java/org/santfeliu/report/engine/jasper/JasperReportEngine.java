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
package org.santfeliu.report.engine.jasper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.collections.LRUMap;
import org.matrix.report.ExportOptions;
import org.matrix.report.Parameter;
import org.matrix.report.ParameterDefinition;
import org.matrix.report.ParameterType;
import org.matrix.report.ReportConstants;
import org.matrix.security.SecurityConstants;
import org.santfeliu.report.engine.ReportEngine;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.util.TemporaryDataSource;

/**
 *
 * @author unknown
 */
public class JasperReportEngine implements ReportEngine
{
  private LRUMap reportCache = new LRUMap(20);

  public JasperReportEngine()
  {
    init();
  }

  public String getInfo()
  {
    return "JasperReportEngine 1.0";
  }

  public List<ParameterDefinition> readReportParameters(String contentId,
    DataSource dataSource)
  {
    try
    {
      // load report from cache
      JasperReport report = loadReport(contentId, dataSource);
      return fillParameterDefinitions(report);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public DataHandler executeReport(String contentId,
                                   DataSource dataSource,
                                   String connectionName,
                                   List<Parameter> parameters,
                                   ExportOptions exportOptions,
                                   Credentials credentials)
  {
    try
    {
      // load report from cache
      JasperReport report = loadReport(contentId, dataSource);

      if (exportOptions.getFormat().equalsIgnoreCase("JASPER"))
      {
        // return source compiled
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JRSaver.saveObject(report, os);
        MemoryDataSource ds = new MemoryDataSource(
          os.toByteArray(), "jasper", "application/octet-stream");
        return new DataHandler(ds);
      }
      else
      {
        // fill parameters
        Map paramsMap = fillParameters(report, parameters, credentials);

        // fill report
        JasperPrint print = fillReport(report, paramsMap, connectionName);
        return new DataHandler(exportReport(print, exportOptions));
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  // ***** private methods *****

  private void init()
  {
    try
    {
      String tempPath = System.getProperty("java.io.tmpdir");

      System.setProperty("net.sf.jasperreports.compiler.keep.java.file", "false");
      System.setProperty("net.sf.jasperreports.compiler.temp.dir", tempPath);
      System.setProperty("org.eclipse.jdt.core.compiler.codegen.TargetPlatform", "1.5");
      System.setProperty("org.eclipse.jdt.core.compiler.compliance", "1.5");
      System.setProperty("org.eclipse.jdt.core.compiler.source", "1.5");

      System.out.println("JasperReports temporary dir: " + tempPath);
      reportCache.setMaximumSize(20);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private JasperReport loadReport(String contentId, DataSource dataSource)
    throws Exception
  {
    // load report from cache
    JasperReport report = (JasperReport)reportCache.get(contentId);
    if (report == null)
    {
      System.out.println("Compiling report " + contentId);
      JasperDesign jasperDesign = JRXmlLoader.load(dataSource.getInputStream());
      report = JasperCompileManager.compileReport(jasperDesign);
      reportCache.put(contentId, report);
    }
    return report;
  }

  private List fillParameterDefinitions(JasperReport report)
  {
    ArrayList<ParameterDefinition> paramDefs =
      new ArrayList<ParameterDefinition>();

    JRParameter params[] = report.getParameters();
    for (JRParameter param : params)
    {
      if (!param.isSystemDefined())
      {
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setName(param.getName());
        paramDef.setDescription(param.getDescription());
        paramDef.setForPrompting(param.isForPrompting());
        Class valueClass = param.getValueClass();
        JRExpression expr = param.getDefaultValueExpression();
        if (expr != null)
        {
          paramDef.setDefaultValue(evalExpression(expr));
        }
        if (valueClass == String.class)
        {
          paramDef.setType(ParameterType.STRING);
        }
        else if (valueClass == Short.class ||
          valueClass == Integer.class || valueClass == Long.class)
        {
          paramDef.setType(ParameterType.INTEGER);
        }
        else if (valueClass == Double.class)
        {
          paramDef.setType(ParameterType.DOUBLE);
        }
        else if (valueClass == Float.class)
        {
          paramDef.setType(ParameterType.FLOAT);
        }
        else if (valueClass == Date.class)
        {
          paramDef.setType(ParameterType.DATE);
        }
        else if (valueClass == Boolean.class)
        {
          paramDef.setType(ParameterType.BOOLEAN);
        }
        else
        {
          paramDef.setType(ParameterType.STRING);
        }
        paramDefs.add(paramDef);
      }
    }
    return paramDefs;
  }

  private String evalExpression(JRExpression expr) // TODO: Use BeanShell
  {
    String result = null;
    String exprValue = expr.getText();
    if (exprValue == null || "null".equals(exprValue))
    {
    }
    else if (exprValue.startsWith("\"") && exprValue.endsWith("\""))
    {
      result = exprValue.substring(1, exprValue.length() - 1);
    }
    else
    {
      try
      {
        double num = Double.parseDouble(exprValue);
        DecimalFormatSymbols otherSymbols =
          new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        DecimalFormat df = new DecimalFormat("###########.#########", otherSymbols);
        result = df.format(num);
      }
      catch (Exception ex)
      {
        if (exprValue.toLowerCase().contains("true"))
          result = "true";
        else if (exprValue.toLowerCase().contains("false"))
          result = "false";
      }
    }
    return result;
  }

  private Map fillParameters(JasperReport report,
    List<Parameter> parameters, Credentials credentials)
  {
    Map paramsMap = new HashMap();
    for (Parameter parameter : parameters)
    {
      paramsMap.put(parameter.getName(), parameter.getValue());
    }
    // parameter conversion
    JRParameter[] jasperParameters = report.getParameters();
    for (JRParameter jasperParameter : jasperParameters)
    {
      String paramName = jasperParameter.getName();
      if (SecurityConstants.USERID_PARAMETER.equalsIgnoreCase(paramName))
      {
        paramsMap.put(paramName, credentials.getUserId());
      }
      else if (JRParameter.REPORT_LOCALE.equals(paramName))
      {
        // set report locale
        String localeName = (String)paramsMap.get(JRParameter.REPORT_LOCALE);
        if (localeName == null)
          localeName = (String)paramsMap.get(ReportConstants.REPORT_LOCALE);
        if (localeName != null && localeName.length() >= 2)
        {
          String language = localeName.substring(0, 2);
          Locale locale = new Locale(language);
          paramsMap.put(JRParameter.REPORT_LOCALE, locale);
        }
      }
      else
      {
        String value = (String)paramsMap.get(paramName);
        if (value == null)
        {
          paramsMap.put(paramName, null);
        }
        else if (jasperParameter.getValueClass() == String.class)
        {
        }
        else if (jasperParameter.getValueClass() == Integer.class)
        {
          paramsMap.put(paramName, Integer.valueOf(value));
        }
        else if (jasperParameter.getValueClass() == Double.class)
        {
          paramsMap.put(paramName, Double.valueOf(value));
        }
        else if (jasperParameter.getValueClass() == Float.class)
        {
          paramsMap.put(paramName, Float.valueOf(value));
        }
        else if (jasperParameter.getValueClass() == Short.class)
        {
          paramsMap.put(paramName, Short.valueOf(value));
        }
        else if (jasperParameter.getValueClass() == Long.class)
        {
          paramsMap.put(paramName, Long.valueOf(value));
        }
        else if (jasperParameter.getValueClass() == BigInteger.class)
        {
          paramsMap.put(paramName, new BigInteger(value));
        }
        else if (jasperParameter.getValueClass() == BigDecimal.class)
        {
          paramsMap.put(paramName, new BigDecimal(value));
        }
        else if (jasperParameter.getValueClass() == Boolean.class)
        {
          paramsMap.put(paramName, Boolean.valueOf(value));
        }
      }
    }
    return paramsMap;
  }

  private JasperPrint fillReport(JasperReport report,
    Map paramsMap, String connectionName) throws Exception
  {
    JasperPrint print = null;
    Connection conn = getConnection(connectionName);
    try
    {
      print = JasperFillManager.fillReport(report, paramsMap, conn);
    }
    finally
    {
      conn.close();
    }
    return print;
  }

  private DataSource exportReport(JasperPrint print,
    ExportOptions exportOptions) throws Exception
  {
    DataSource ds = null;
    String format = exportOptions.getFormat().toUpperCase();
    if ("PDF".equals(format))
    {
      File file = File.createTempFile("out", ".pdf");
      ds = new TemporaryDataSource(file, "application/pdf");
      OutputStream os = ds.getOutputStream();
      try
      {
        JasperExportManager.exportReportToPdfStream(print, os);
        os.flush();
      }
      finally
      {
        os.close();
      }
    }
    else if ("RTF".equals(format))
    {
      File file = File.createTempFile("out", ".rtf");
      ds = new TemporaryDataSource(file, "application/rtf");
      OutputStream os = ds.getOutputStream();
      try
      {
        JRRtfExporter exporter = new JRRtfExporter();
        exporter.setParameter(JRHtmlExporterParameter.JASPER_PRINT, print);
        exporter.setParameter(JRHtmlExporterParameter.OUTPUT_STREAM, os);
        exporter.exportReport();
        os.flush();
      }
      finally
      {
        os.close();
      }
    }
    else if ("CSV".equals(format))
    {
      File file = File.createTempFile("out", ".csv");
      ds = new TemporaryDataSource(file, "text/csv");
      OutputStream os = ds.getOutputStream();
      try
      {
        JRCsvExporter exporter = new JRCsvExporter();
        exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER, ";");
        exporter.setParameter(JRCsvExporterParameter.CHARACTER_ENCODING, "ISO-8859-1");
        exporter.setParameter(JRCsvExporterParameter.JASPER_PRINT, print);
        exporter.setParameter(JRCsvExporterParameter.OUTPUT_STREAM, os);
        exporter.exportReport();
        os.flush();
      }
      finally
      {
        os.close();
      }
    }
    else if ("HTML".equals(format))
    {
      File file = File.createTempFile("out", ".html");
      ds = new TemporaryDataSource(file, "text/html");
      JRExtendedHtmlExporter exporter = new JRExtendedHtmlExporter();
      OutputStream os = ds.getOutputStream();
      try
      {
        exporter.setParameter(JRHtmlExporterParameter.JASPER_PRINT, print);
        exporter.setParameter(JRHtmlExporterParameter.OUTPUT_STREAM, os);
        exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);
        exporter.setParameter(JRHtmlExporterParameter.HTML_HEADER,
        "<html>\n" +
        "<head>\n" +
        "  <title></title>\n" +
        "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
        "  <style type=\"text/css\">\n" +
        "    a {text-decoration: none}\n" +
        "  </style>\n" +
        "</head>\n" +
        "<body text=\"#000000\" link=\"#000000\" alink=\"#000000\" vlink=\"#000000\">\n" +
        "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
        "<tr><td width=\"50%\">&nbsp;</td><td align=\"left\">");
        exporter.setParameter(JRHtmlExporterParameter.HTML_FOOTER,
        "</td><td width=\"50%\">&nbsp;</td></tr>\n" +
        "</table></body></html>");
        exporter.setParameter(JRHtmlExporterParameter.JASPER_PRINT, print);

        boolean ignoreMargins = exportOptions.isIgnorePageMargins();
        if (ignoreMargins)
        {
          exporter.setParameter(JRHtmlExporterParameter.IGNORE_PAGE_MARGINS, true);
        }
        String encoding = exportOptions.getCharacterEncoding();
        if (encoding != null)
        {
          exporter.setParameter(JRHtmlExporterParameter.CHARACTER_ENCODING, encoding);
        }
        exporter.exportReport();
        os.flush();
      }
      finally
      {
        os.close();
      }
    }
    return ds;
  }

  private Connection getConnection(String connectionName)
  {
    try
    {
      if (connectionName == null)
      {
        // user default connection name
        connectionName = MatrixConfig.getProperty(
           "org.santfeliu.report.engine.jasper.defaultConnectionName");
        if (connectionName == null)
          throw new RuntimeException("report:REPORT_CONNECTION_NAME_UNDEFINED");
      }
      Context initContext = new InitialContext();
      Context envContext  = (Context)initContext.lookup("java:/comp/env");
      javax.sql.DataSource ds =
        (javax.sql.DataSource)envContext.lookup(connectionName);
      Connection conn = ds.getConnection();
      conn.setAutoCommit(false);
      return conn;
    }
    catch (Exception ex)
    {
      throw new RuntimeException("report:REPORT_CONNECTION_FAILED");
    }
  }

  public static void main(String[] args)
  {
    try
    {
      JasperReportEngine engine = new JasperReportEngine();
      for (int i = 0; i < 50; i++)
      {
        {
          System.out.println("Exporting to PDF...");
          ExportOptions options = new ExportOptions();
          options.setFormat("PDF");
          List<Parameter> parameters = new ArrayList<Parameter>();
          DataHandler dh =
            engine.executeReport("9", new FileDataSource("c:/test.jrxml"), "cn",
            parameters, options, new Credentials());
          dh.writeTo(new FileOutputStream("c:/test.pdf"));
        }
        {
          System.out.println("Exporting to HTML...");
          ExportOptions options = new ExportOptions();
          options.setFormat("HTML");
          List<Parameter> parameters = new ArrayList<Parameter>();
          DataHandler dh =
            engine.executeReport("9", new FileDataSource("c:/test.jrxml"), "cn",
            parameters, options, new Credentials());
          dh.writeTo(new FileOutputStream("c:/test.html"));
        }
        {
          System.out.println("Exporting to JASPER...");
          ExportOptions options = new ExportOptions();
          options.setFormat("JASPER");
          List<Parameter> parameters = new ArrayList<Parameter>();
          DataHandler dh =
            engine.executeReport("9", new FileDataSource("c:/test.jrxml"), "cn",
            parameters, options, new Credentials());
          dh.writeTo(new FileOutputStream("c:/test.jasper"));
        }
        {
          System.out.println("Exporting to XML...");
          ExportOptions options = new ExportOptions();
          options.setFormat("XML");
          List<Parameter> parameters = new ArrayList<Parameter>();
          DataHandler dh =
            engine.executeReport("9", new FileDataSource("c:/test.jrxml"), "cn",
            parameters, options, new Credentials());
          dh.writeTo(new FileOutputStream("c:/test.xml"));
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
