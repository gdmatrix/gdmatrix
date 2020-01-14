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
package org.santfeliu.doc.transform.impl;

import org.santfeliu.doc.transform.*;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfFileSpecification;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNameTree;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import org.apache.commons.io.IOUtils;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.santfeliu.doc.fmt.p7m.P7MDocument;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.util.TemporaryDataSource;
import org.santfeliu.util.template.Template;

/**
 *
 * @author realor
 */
public class P7MTransformer extends Transformer
{
  public static final String P7M = "p7m";
  public static final String DOC = "doc";
  public static final String SIG = "sig";
  public static final String PDF = "pdf";

  public static final String PDFCONVERTER_PROPERTY = "pdfConverter";  
  public static final String TEXT_TEMPLATEURL_PROPERTY = "textTemplateUrl";
  public static final String HTML_TEMPLATEURL_PROPERTY = "htmlTemplateUrl";
  public static final String TSURL_PROPERTY = "tsUrl";

  public static final String TIMESTAMP_OPTION = "timestamp";
  public static final String TEMPLATE_OPTION = "template";
  public static final String SIGN_INFO_PAGE_OPTION = "signInfoPage";
  public static final String EMBED_P7M = "embedP7M";

  public ArrayList<Transformation> transformations;

  @Override
  public String getDescription()
  {
    return "P7MTransformer v1.0";
  }

  @Override
  public List<Transformation> getSupportedTransformations()
  {
    if (transformations == null)
    {
      transformations = new ArrayList<Transformation>();

      transformations.add(new Transformation(id, P7M,
        "application/pkcs7-mime", null,
        "application/pkcs7-mime", null,
        null,
        "Download p7m (pkcs7-mime)"));

      transformations.add(new Transformation(id, DOC,
        "application/pkcs7-mime", null,
        "application/msword", null,
        null,
        "Extract p7m signed document"));

      transformations.add(new Transformation(id, SIG,
        "application/pkcs7-mime", null,
        "text/html", null,
        null,
        "Show p7m signatures"));

      if (properties.containsKey(PDFCONVERTER_PROPERTY))
      {
        transformations.add(new Transformation(id, PDF,
          "application/pkcs7-mime", null,
          "application/pdf", null, // TODO: define formatId for this PDF
          true,
          "Convert p7m to pdf"));
      }

      transformations.add(new Transformation(id, P7M,
        "application/pdf", null, // TODO: define formatId for this PDF
        "application/pkcs7-mime", null,
        true,
        "Extract embedded p7m"));

      transformations.add(new Transformation(id, DOC,
        "application/pdf", null, // TODO: define formatId for this PDF
        "application/msword", null,
        true,
        "Extract embedded doc"));

      transformations.add(new Transformation(id, SIG,
        "application/pdf", null, // TODO: define formatId for this PDF
        "text/html", null,
        true,
        "Show embedded p7m signatures"));
    }
    return transformations;
  }

  public DataHandler transform(Document document,
    String transformationName, Map options) throws TransformationException
  {
    try
    {
      String contentType = document.getContent().getContentType();
      if ("application/pkcs7-mime".equals(contentType))
      {
        return transformCMS(document, transformationName, options);
      }
      else if ("application/pdf".equals(contentType))
      {
        return transformPDF(document, transformationName, options);
      }
      else throw new TransformationException("Invalid transformation");
    }
    catch (TransformationException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new TransformationException(ex);
    }
  }

  private DataHandler transformCMS(Document document,
    String transformationName, Map options) throws Exception
  {
    String sigId = DocumentUtils.getPropertyValue(document, "sigId");
    DataHandler data = document.getContent().getData();
    P7MDocument cms = new P7MDocument(data.getInputStream());
    // add timestamps
    String tsUrl = properties.get(TSURL_PROPERTY);
    if (tsUrl != null && 
      options != null && options.containsKey(TIMESTAMP_OPTION))
    {
      cms.addTimeStamps(tsUrl);
    }

    if (P7M.equals(transformationName))
    {
      byte[] bytes = cms.getEncoded();
      ByteArrayDataSource ds =
        new ByteArrayDataSource(bytes, "application/pkcs7-mime");
      return new DataHandler(ds);
    }
    else if (SIG.equals(transformationName))
    {
      String signatureInfo = readSignatureInfo(document, options, cms,
        sigId, HTML_TEMPLATEURL_PROPERTY);
      byte[] bytes = signatureInfo.getBytes("UTF-8");
      return new DataHandler(new ByteArrayDataSource(bytes, "text/html"));
    }
    else if (PDF.equals(transformationName))
    {
      // assuming content data is a MS Word document
      File file = File.createTempFile("transform", ".doc");
      System.out.println(">>> msword file: " + file);
      FileOutputStream os = new FileOutputStream(file);
      byte[] bytes = cms.getSignedContent();
      IOUtils.write(bytes, os);
      os.close();

      String signatureInfo = readSignatureInfo(document, options, cms,
        sigId, TEXT_TEMPLATEURL_PROPERTY);

      String pdfConverter = properties.get(PDFCONVERTER_PROPERTY);
      Template template = Template.create(pdfConverter);
      HashMap vars = new HashMap();
      vars.put("file", file.getAbsolutePath());
      vars.put("signatureInfo", signatureInfo);
      vars.putAll(options);
      Runtime runtime = Runtime.getRuntime();
      String command = template.merge(vars);
      System.out.println("Convert command: " + command);
      runtime.exec(command);

      String filename = file.getName();
      File pdfFile = new File(file.getParent(),
        filename.substring(0, filename.length() - 3) + "pdf");
      System.out.println(">>> pdf file: " + pdfFile);
      waitForFile(pdfFile);
      if (pdfFile.exists())
      {
        System.out.println("Adding file and signature info...");
        File finalPdfFile = File.createTempFile("final", ".pdf");

        // read pdf file
        FileInputStream fis = new FileInputStream(pdfFile);
        PdfReader reader = new PdfReader(fis);
        fis.close();

        // create new pdf document
        FileOutputStream fos = new FileOutputStream(finalPdfFile);
        com.lowagie.text.Document pdfDoc = new com.lowagie.text.Document();
        PdfWriter writer = PdfWriter.getInstance(pdfDoc, fos);
        pdfDoc.open();
        pdfDoc.setPageSize(reader.getPageSize(1));
        PdfContentByte cb = writer.getDirectContent();
        for (int i = 1; i <= reader.getNumberOfPages(); i++)
        {
          pdfDoc.newPage();
          PdfImportedPage page = writer.getImportedPage(reader, i);
          cb.addTemplate(page, 0, 0);
        }

        // add signature info page
        if (options.containsKey(SIGN_INFO_PAGE_OPTION))
        {
          String signatureInfo2 = readSignatureInfo(document, options, cms,
            sigId, HTML_TEMPLATEURL_PROPERTY);
          addSignatureInfo(pdfDoc, signatureInfo2);
        }

        // add original file
        if (options.containsKey(EMBED_P7M))
        {
          String p7mFilename = cms.getSignatures().get(0).getFilename();
          PdfFileSpecification fs =
            PdfFileSpecification.fileEmbedded(writer,
          null, p7mFilename + ".p7m", cms.getEncoded());
          writer.addFileAttachment(fs);
        }

        pdfDoc.close();
        fos.close();

        file.delete();
        pdfFile.delete();
        return new DataHandler(new TemporaryDataSource(finalPdfFile));
      }
      else throw new TransformationException("Internal error");
    }
    else // DOC
    {
      // Assuming p7m content is a MS Word document
      byte[] bytes = cms.getSignedContent();
      ByteArrayDataSource ds =
        new ByteArrayDataSource(bytes, "application/msword");
      return new DataHandler(ds);
    }
  }

  private DataHandler transformPDF(Document document,
    String transformationName, Map options) throws Exception
  {
    // extract P7M
    byte[] bytes = null;
    DataHandler data = document.getContent().getData();
    InputStream is = data.getInputStream();
    PdfReader reader = new PdfReader(is);
    is.close();

    String filename = null;
    PdfDictionary root = reader.getCatalog();
    PdfDictionary names =
      (PdfDictionary)PdfReader.getPdfObject(root.get(PdfName.NAMES));
    if (names != null)
    {
      PdfDictionary embFiles = (PdfDictionary) PdfReader
          .getPdfObject(names.get(PdfName.EMBEDDEDFILES));
      if (embFiles != null)
      {
        HashMap embMap = PdfNameTree.readTree(embFiles);
        Iterator iter = embMap.values().iterator();
        while (iter.hasNext() && bytes == null)
        {
          PdfDictionary filespec =
            (PdfDictionary)PdfReader.getPdfObject((PdfObject)iter.next());
          PdfDictionary ef =
            (PdfDictionary)PdfReader.getPdfObject(filespec.get(PdfName.EF));
          if (ef != null)
          {
            PdfString fn =
             (PdfString)PdfReader.getPdfObject(filespec.get(PdfName.F));
            filename = fn.toString().toLowerCase();
            if (filename.endsWith(".p7m"))
            {
              PRStream prs =
                (PRStream)PdfReader.getPdfObject(ef.get(PdfName.F));
              if (prs != null)
              {
                bytes = PdfReader.getStreamBytes(prs);
              }
            }
          }
        }
      }
    }
    reader.close();
    if (bytes == null)
      throw new TransformationException("No p7m attached");
    MemoryDataSource ds =
     new MemoryDataSource(bytes, filename, "application/pkcs7-mime");
    data = new DataHandler(ds);

    if (P7M.equals(transformationName))
    {
      return data;
    }
    else 
    {
      Document cmsDocument = new Document();
      Content content = new Content();
      content.setData(data);
      content.setContentType("application/p7m");
      cmsDocument.setContent(content);
      if (SIG.equals(transformationName))
      {
        return transformCMS(cmsDocument, SIG, options);
      }
      else
      {
        return transformCMS(cmsDocument, DOC, options);
      }
    }
  }

  private void addSignatureInfo(com.lowagie.text.Document pdfDoc,
    String signatureInfo) throws Exception
  {
    pdfDoc.newPage();

    StyleSheet sh = new StyleSheet();
    List<Element> objects = HTMLWorker.parseToList(
      new StringReader(signatureInfo), sh);

    // top margin
    Paragraph par = new Paragraph();
    par.add(new Paragraph());
    par.add(new Paragraph());

    for (Object object : objects)
    {
      Element e = (Element)object;
      par.add(e);
    }
    pdfDoc.add(par);
  }

  private void waitForFile(File pdfFile) throws Exception
  {
    int i = 100; // seconds
    while (!pdfFile.exists() && i >= 0)
    {
      Thread.sleep(1000);
      i--;
    }
    if (pdfFile.exists())
    {
      File tmpFile = new File(pdfFile.getAbsolutePath() + ".tmp");
      i = 200; // seconds
      // rename file to detect file generation completed
      while (!pdfFile.renameTo(tmpFile) && i >= 0)
      {
        Thread.sleep(1000);
        i--;
      }
      if (i >= 0) tmpFile.renameTo(pdfFile);
    }
  }

  private String readSignatureInfo(Document document, Map options,
    P7MDocument cms, String sigId, String templateType) throws Exception
  {
    String templateURL = properties.get(TEMPLATE_OPTION);
    if (templateURL == null)
      templateURL = properties.get(templateType);
    if (templateURL == null) return "";

    URL url = new URL(templateURL);
    InputStreamReader reader = new InputStreamReader(url.openStream());
    Template template = Template.create(reader);
    HashMap vars = new HashMap();
    if (options != null) vars.putAll(options);
    vars.put("document", document);
    vars.put("cms", cms);
    vars.put("signatures", cms.getSignatures());
    vars.put("sigId", sigId);
    return template.merge(vars);
  }
}
