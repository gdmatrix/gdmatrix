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
package org.santfeliu.doc.util.authcopy;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import javax.activation.DataHandler;
import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.signature.xmldsig.XMLSignedDocument;

/**
 *
 * @author blanquepa
 */
public class Document
{
  private static final String DOCUMENT_WITHOUT_SIGNATURES_EXCEPTION = 
    "DOCUMENT_WITHOUT_SIGNATURES";
  private static final String CANNOT_EXTRACT_SIGNATURES_EXCEPTION = 
    "CANNOT_EXTRACT_SIGNATURES";  
  
  private String docId;
  private String csv;
  private String title;
  private String docType;
  private String contentType;
  private DataHandler data;
  private List signatures = new ArrayList();
  private List<Property> properties = new ArrayList();

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }
  
  public String getTitle()
  {
    return title;
  }

  void setTitle(String title)
  {
    this.title = title;
  }

  public String getDocType()
  {
    return docType;
  }

  void setDocType(String docType)
  {
    this.docType = docType;
  }

  public String getCsv()
  {
    return csv;
  }

  void setCsv(String csv)
  {
    this.csv = csv;
  }

  public String getContentType()
  {
    return contentType;
  }

  void setContentType(String contentType)
  {
    this.contentType = contentType;
  }
  
  public DataHandler getData()
  {
    return data;
  }

  void setData(DataHandler data)
  {
    this.data = data;
  }

  public List getSignatures() throws Exception
  {
    if (!isSigned())
      throw new Exception(DOCUMENT_WITHOUT_SIGNATURES_EXCEPTION);    
    
    return this.signatures;
  }
   
  public boolean isSigned()
  {
    return (signatures != null && !signatures.isEmpty());
  }

  public List<Property> getProperties()
  {
    return properties;
  }

  void setProperties(List<Property> properties)
  {
    this.properties = properties;
  }
  
  boolean isXmlSignedDocument()
  {
    return "text/xml".equals(contentType)
      && DictionaryUtils.getPropertyValue(properties, "SignedDocumentClass") != null;
  }
  
  boolean isPdf()
  {
    return "application/pdf".equals(contentType);
  }
  
  void extractSignatures() throws Exception
  {
    signatures = new ArrayList();
    try
    {
      BouncyCastleProvider provider = new BouncyCastleProvider();
      Security.addProvider(provider);      
      
      if (isPdf())
      {
        PdfReader reader = new PdfReader(getData().getInputStream());
        AcroFields fields = reader.getAcroFields();
        for (String sigName : fields.getSignatureNames())
        {
          PdfPKCS7 pkcs7 = fields.verifySignature(sigName);
          signatures.add(pkcs7);
        }
      }
      else if (isXmlSignedDocument())
      {
        XMLSignedDocument signedDocument = new XMLSignedDocument();
        signedDocument.parseDocument(getData().getInputStream());
        for (int i = 0; i < signedDocument.getSignaturesCount(); i++)
        {
          XMLSignature signature = signedDocument.getSignature(i);
          signatures.add(signature);               
        } 
      }
    }
    catch (Exception ex)
    {
      throw new Exception(CANNOT_EXTRACT_SIGNATURES_EXCEPTION);
    }
    
  }    
  
}
