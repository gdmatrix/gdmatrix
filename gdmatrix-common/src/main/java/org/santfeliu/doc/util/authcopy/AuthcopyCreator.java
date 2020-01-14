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
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfPKCS7;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.activation.DataHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.transform.TransformationManager;
import org.santfeliu.doc.transform.TransformationRequest;
import static org.santfeliu.doc.transform.impl.XMLTransformer.XSL_PROPERTY;
import org.santfeliu.signature.xmldsig.XMLSignedDocument;

/**
 *
 * @author blanquepa
 */
public class AuthcopyCreator
{
  private static final String NO_DOCUMENT_FOUND_EXCEPTION = "NO_DOCUMENT_FOUND";
  private static final String TRANSFORMATION_XSL_NOT_FOUND_EXCEPTION = "TRANSFORMATION_XSL_NOT_FOUND";
  private static final String CANNOT_TRANSFORM_TO_PDF_EXCEPTION = "CANNOT_TRANSFORM_TO_PDF";
  private static final String CANNOT_EXTRACT_SIGNATURES_EXCEPTION = "CANNOT_EXTRACT_SIGNATURES";
  private static final String DOCUMENT_WITHOUT_SIGNATURES_EXCEPTION = "DOCUMENT_WITHOUT_SIGNATURES";
  
  private Source source;
  private ResourceBundle bundle =  
    ResourceBundle.getBundle("org.santfeliu.doc.util.authcopy.resources.AuthcopyBundle", Locale.getDefault());
  
  public AuthcopyCreator()
  {
    this(new DocumentManagerSource());
  }
  
  public AuthcopyCreator(Source source)
  {
    this.source = source;
  }
  
  public AuthcopyCreator(String sourceClassName)
  {
    try
    {
      Class sourceClass = Class.forName(sourceClassName);
      this.source = (Source) sourceClass.newInstance();
    }
    catch (Exception ex)
    {
      this.source = new DocumentManagerSource();
    }
  }

  public Document create(String id) throws Exception
  {
    if (StringUtils.isBlank(id))
      throw new Exception(bundle.getString(NO_DOCUMENT_FOUND_EXCEPTION));

    Document document = source.getDocument(id);
    if (document == null)
      throw new Exception(bundle.getString(NO_DOCUMENT_FOUND_EXCEPTION));

    //Signatures
    populateSignatures(document);

    //Transforms if needed
    if (!document.isPdf())
    {
      String xsl = DictionaryUtils.getPropertyValue(document.getProperties(), XSL_PROPERTY);
      if (xsl == null)
        throw new Exception(bundle.getString(TRANSFORMATION_XSL_NOT_FOUND_EXCEPTION));        

      DataHandler dh = document.getData();
      document.setData(transformToPdf(dh, xsl));
    }

    //CSV (use sigId)
    document.setCsv(id);

    return document;      
  }
  
  private DataHandler transformToPdf(DataHandler dh, String xsl) throws Exception
  {
    try
    {
      TransformationRequest req = new TransformationRequest();
      req.setTransformerId("xml");
      req.setTransformationName("pdf");
      Map options = new HashMap();
      options.put(XSL_PROPERTY, xsl);
      req.setOptions(options);
      return TransformationManager.transform(dh, req);
    }
    catch (Exception ex)
    {
      throw new Exception(bundle.getString(CANNOT_TRANSFORM_TO_PDF_EXCEPTION));
    }
  }  
  
  private void populateSignatures(Document document) throws Exception
  {
    try
    {
      BouncyCastleProvider provider = new BouncyCastleProvider();
      Security.addProvider(provider);      
      
      if (document.isPdf())
      {
        PdfReader reader = new PdfReader(document.getData().getInputStream());
        AcroFields fields = reader.getAcroFields();
        for (String sigName : fields.getSignatureNames())
        {
          PdfPKCS7 pkcs7 = fields.verifySignature(sigName);
          X509Certificate cert = pkcs7.getSigningCertificate();
          document.addSignature(getSubjectString(cert.getSubjectDN().getName()));
        }
      }
      else if (document.isXmlSignedDocument())
      {
        XMLSignedDocument signedDocument = new XMLSignedDocument();
        signedDocument.parseDocument(document.getData().getInputStream());
        for (int i = 0; i < signedDocument.getSignaturesCount(); i++)
        {
          XMLSignature signature = signedDocument.getSignature(i);
          X509Certificate cert = signature.getKeyInfo().getX509Certificate();
          document.addSignature(getSubjectString(cert.getSubjectDN().getName()));                
        } 
      }
    }
    catch (Exception ex)
    {
      throw new Exception(bundle.getString(CANNOT_EXTRACT_SIGNATURES_EXCEPTION));
    }
    
    if (!document.isSigned())
      throw new Exception(bundle.getString(DOCUMENT_WITHOUT_SIGNATURES_EXCEPTION));      
  }
  
  private String getSubjectString(String subjectName)
  {
    String result = null;
    Map<String,String> map = new HashMap();
    String[] variables = subjectName.split(",");
    for (int i = 0; i < variables.length; i++)
    {
      String[] parts = variables[i].split("=");
      map.put(parts[0].trim(), parts[1].trim());
    }
    
    String o = map.get("O");
    String cn = map.get("CN");
    String serial = map.get("SERIALNUMBER");
    
    result = ((o != null ? o : "")
      + (cn != null ? (o != null ? " - " + cn : cn ): "") 
      + (serial != null ? " (" + serial + ")" : ""));
    
    return result;
  }
  
}
