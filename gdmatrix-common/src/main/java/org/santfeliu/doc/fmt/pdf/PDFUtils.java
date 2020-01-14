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
package org.santfeliu.doc.fmt.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfPKCS7;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author realor
 */
public class PDFUtils
{  
  public static void fillForm(String inFilename, String outFilename,
    Map variables) throws IOException, DocumentException
  {
    InputStream is = new FileInputStream(new File(inFilename));
    try
    {
      OutputStream os = new FileOutputStream(new File(outFilename));
      try
      {
        fillForm(is, os, variables);
      }
      finally
      {
        os.close();
      }
    }
    finally
    {
      is.close();
    }
  }

  public static void fillForm(InputStream is, OutputStream os,
    Map variables) throws IOException, DocumentException
  {
    PdfReader pdfTemplate = new PdfReader(is);
    try
    {
      PdfStamper stamper = new PdfStamper(pdfTemplate, os);
      try
      {
        stamper.setFormFlattening(true);
        AcroFields acroFields = stamper.getAcroFields();

        Set<String> fieldNames = acroFields.getFields().keySet();
        for (String fieldName : fieldNames)
        {
          Object value = variables.get(fieldName);
          if (value != null)
          {
            acroFields.setField(fieldName, value.toString());
          }
        }
      }
      finally
      {
        stamper.close();
      }
    }
    finally
    {
      pdfTemplate.close();
    }
  }

  public static void verify(String path, PrintStream out) throws Exception
  {
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null, null);

    PdfReader reader = new PdfReader(path);
    AcroFields af = reader.getAcroFields();
    ArrayList<String> names = af.getSignatureNames();
    for (String name : names)
    {
      out.println("Signature name: " + name);
      out.println("Signature covers whole document: " +
        af.signatureCoversWholeDocument(name));
      out.println("Document revision: " + af.getRevision(name) +
        " of " + af.getTotalRevisions());
      PdfPKCS7 pk = af.verifySignature(name);
      out.println("CRLs: " + pk.getCRLs());

      Certificate[] pkc = pk.getCertificates();
      out.println("Subject: " + pkc[0].toString());
      out.println("Signature valid: " + pk.verify());
      if (pk.verifyTimestampImprint())
      {
        out.println("TimeStamps are valid.");
      }
      else
      {
        out.println("TimeStamps are not valid.");
      }
    }
    out.flush();
    out.close();
  }

  public static void main(String args[])
  {
    try
    {
      HashMap map = new HashMap();
      map.put("d1", "xxxxx");
      map.put("d2", "yyyyy");
      map.put("d3", "zzzzz");
      PDFUtils.fillForm("c:/form.pdf", "c:/out.pdf", map);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
