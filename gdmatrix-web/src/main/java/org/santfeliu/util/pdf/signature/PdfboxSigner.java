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
package org.santfeliu.util.pdf.signature;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import javax.xml.transform.TransformerException;
import org.apache.pdfbox.examples.signature.CreateSignedTimeStamp;
import org.apache.pdfbox.examples.signature.SigUtils;
import org.apache.pdfbox.examples.signature.ValidationTimeStamp;
import org.apache.pdfbox.examples.signature.validation.AddValidationInformation;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;


import org.apache.xmpbox.xml.XmpSerializer;

/**
 *
 * @author blanquepa
 */
public class PdfboxSigner extends PDFSigner
{ 
  @Override
  protected void doSign(InputStream is, OutputStream os, 
    SignatureLevel sigLevel, DigestAlgorithm digAlg, EncryptionAlgorithm encAlg, 
    SignatureInfo sigInfo, DocumentInfo docInfo)
      throws Exception
  {
    PDDocument document = null;
    try
    {   
      document = PDDocument.load(is);      
      int accessPermissions = SigUtils.getMDPPermission(document);
      if (accessPermissions == 1)
        throw new IllegalStateException(
          "No changes to the document are permitted due to DocMDP transform "
            + "parameters dictionary");
      
      if (docInfo != null)
        document = setDocumentInfo(document, docInfo);

      // create signature dictionary
      PDSignature signature = new PDSignature();
      signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
      signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);

      // put signature info
      if (sigInfo != null)
      {
        signature.setLocation(sigInfo.getLocation());
        signature.setReason(sigInfo.getReason());
      }
      signature.setSignDate(Calendar.getInstance());

      // register signature dictionary and sign interface
      SignatureInterfaceImpl signInterface;
      signInterface = new SignatureInterfaceImpl(keyStore, ksPassword);
      signInterface.setTsaUrl(this.tsaUrl);
      signInterface.setAlgorithm(digAlg, encAlg);
      document.addSignature(signature, signInterface);
     
      if (sigLevel.equals(SignatureLevel.LT)
        || sigLevel.equals(SignatureLevel.LTA))
      {
        addValidationInformation(document, os);
        //TODO: if (sigLevel.equals(SignatureLevel.LTA) add document timestamp         
      }
      else
      {
        document.saveIncremental(os);
      }
    }
    finally
    {
      is.close();
      if (document != null) document.close();
    }
  }
  
  private PDDocument setDocumentInfo(PDDocument document, DocumentInfo docInfo) 
    throws TransformerException, IOException
  {
    PDDocument outDocument = null;
    if (docInfo != null)
    {
      PDDocumentInformation info = document.getDocumentInformation();    

      //Change document metadata  
      if (docInfo.getAuthor() != null)
        info.setAuthor(docInfo.getAuthor());
      if (docInfo.getTitle() != null)
        info.setTitle(docInfo.getTitle());
      if (docInfo.getSubject() != null)
        info.setSubject(docInfo.getSubject());
      if (docInfo.getKeywords() != null)
        info.setKeywords(docInfo.getKeywords());
      document.setDocumentInformation(info);

      XMPMetadata metadata = XMPMetadata.createXMPMetadata();

      AdobePDFSchema pdfSchema = metadata.createAndAddAdobePDFSchema();
      if (info.getKeywords() != null)
        pdfSchema.setKeywords(info.getKeywords());
      if (info.getProducer()!= null)
        pdfSchema.setProducer(info.getProducer());

      XMPBasicSchema basicSchema = metadata.createAndAddXMPBasicSchema();
      if (info.getModificationDate()!= null)
        basicSchema.setModifyDate(info.getModificationDate());
      if (info.getCreationDate() != null)
        basicSchema.setCreateDate(info.getCreationDate());
      if (info.getCreator() != null)
        basicSchema.setCreatorTool(info.getCreator());
      if (info.getCreationDate() != null)
        basicSchema.setMetadataDate(info.getCreationDate());

      DublinCoreSchema dcSchema = metadata.createAndAddDublinCoreSchema();
      if (info.getTitle() != null)
        dcSchema.setTitle(info.getTitle());
      if (info.getAuthor() != null)
        dcSchema.addCreator(info.getAuthor());
      if (info.getSubject() != null)
        dcSchema.setDescription(info.getSubject());

      PDMetadata metadataStream = new PDMetadata(document);
      PDDocumentCatalog catalog = document.getDocumentCatalog();
      catalog.setMetadata(metadataStream);

      XmpSerializer serializer = new XmpSerializer();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      serializer.serialize(metadata, out, false);
      metadataStream.importXMPMetadata(out.toByteArray());        

      //Save all document & reload is needed.
      File tmpFile = File.createTempFile("metadata", ".pdf");
      try (FileOutputStream fos = new FileOutputStream(tmpFile);
           FileInputStream fis = new FileInputStream(tmpFile))
      {
        document.save(fos);        
        outDocument = PDDocument.load(fis);
        document.close();
      }
      finally
      {
        tmpFile.delete();
      }
    }
    return outDocument;
  }
  
  @Override
  protected void doTimestamp(InputStream is, OutputStream os, 
    boolean ltvEnabled) throws IOException
  {
    CreateSignedTimeStamp creator = new CreateSignedTimeStamp(tsaUrl);
    try (PDDocument doc = PDDocument.load(is))
    {
      if (ltvEnabled)
      {
        //Write to file to avoid closing stream by creator
        File f = File.createTempFile("tstamp", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(f))
        {
          creator.signDetached(doc, fos);
          try (FileInputStream fis = new FileInputStream(f);
            PDDocument auxDoc = PDDocument.load(fis))
          {
            addValidationInformation(auxDoc, os);
          }          
        }
        finally
        {
          f.delete();
        }
      }
      else
      {
        creator.signDetached(doc, os);
        doc.saveIncremental(os);
      }        
    }
  } 
  
  @Override
  protected void doPreserve(InputStream is, OutputStream os) 
    throws Exception
  {
    File pFile = File.createTempFile("preserve", ".pdf");    
    try (FileOutputStream fos = new FileOutputStream(pFile);
      PDDocument doc = PDDocument.load(is))
    {
      addValidationInformation(doc, fos);
      try (FileInputStream fis = new FileInputStream(pFile))
      {
        timestamp(fis, os, true);
      }
    }
    finally
    {
      pFile.delete();
    }
  }  
  
  private void addValidationInformation(PDDocument document, OutputStream os) 
    throws IOException
  {
    File vFile = File.createTempFile("sigval", ".pdf");
    try (FileOutputStream fos = new FileOutputStream(vFile))
    {
      document.saveIncremental(fos);
      fos.close();

      AddValidationInformation avi = new AddValidationInformation();  
      avi.validateSignature(vFile, os);   
    }
    finally
    {
      vFile.delete();
    }    
  }



  private class SignatureInterfaceImpl implements SignatureInterface
  {

    private Certificate[] certificateChain;
    private PrivateKey privateKey;
    private String tsaUrl;
    private String algorithm = "SHA256WithRSA"; //Default

    public SignatureInterfaceImpl(KeyStore ks, char[] pin)
      throws KeyStoreException, UnrecoverableKeyException, 
        NoSuchAlgorithmException, IOException, CertificateException
    {
      //Private Key for signing
      privateKey = (PrivateKey) ks.getKey(alias, pin);

      //Get certificate chain from alias 
      Certificate[] certChain = ks.getCertificateChain(alias);
      if (certChain != null)
      {
        certificateChain = certChain;
        Certificate cert = certChain[0];
        if (cert instanceof X509Certificate)
        {
          // avoid expired certificate
          ((X509Certificate) cert).checkValidity();
          SigUtils.checkCertificateUsage((X509Certificate) cert);
        }
      }
    }

    public String getTsaUrl()
    {
      return tsaUrl;
    }

    public void setTsaUrl(String tsaUrl)
    {
      this.tsaUrl = tsaUrl;
    }

    public String getAlgorithm()
    {
      return algorithm;
    }

    public void setAlgorithm(String algorithm)
    {
      this.algorithm = algorithm;
    }
    
    public void setAlgorithm(DigestAlgorithm digAlg, EncryptionAlgorithm encAlg) 
    {
      if (digAlg != null && encAlg != null)
        this.algorithm = digAlg.name() + "With" + encAlg.name();
    }

    @Override
    public byte[] sign(InputStream content) throws IOException
    {
      try
      {
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        X509Certificate cert = (X509Certificate) certificateChain[0];

        ContentSigner sha1Signer
          = new JcaContentSignerBuilder(algorithm).build(privateKey);
        gen.addSignerInfoGenerator(
          new JcaSignerInfoGeneratorBuilder(
            new JcaDigestCalculatorProviderBuilder().build())
            .build(sha1Signer, cert));
        gen.addCertificates(new JcaCertStore(Arrays.asList(certificateChain)));
        CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
        CMSSignedData signedData = gen.generate(msg, false);
        
        //If tsaUrl is set then put a ValidationTimeStamp 
        if (tsaUrl != null && tsaUrl.length() > 0)
        {
          ValidationTimeStamp validation = new ValidationTimeStamp(tsaUrl);
          signedData = validation.addSignedTimeStamp(signedData);
        }
        return signedData.getEncoded();
      }
      catch (GeneralSecurityException | CMSException | OperatorCreationException e)
      {
        throw new IOException(e);
      }
    }
  };

  class CMSProcessableInputStream implements CMSTypedData
  {

    private InputStream in;
    private final ASN1ObjectIdentifier contentType;

    CMSProcessableInputStream(InputStream is)
    {
      this(new ASN1ObjectIdentifier(CMSObjectIdentifiers.data.getId()), is);
    }

    CMSProcessableInputStream(ASN1ObjectIdentifier type, InputStream is)
    {
      contentType = type;
      in = is;
    }

    @Override
    public Object getContent()
    {
      return in;
    }

    @Override
    public void write(OutputStream out) throws IOException, CMSException
    {
      // read the content only one time
      IOUtils.copy(in, out);
      in.close();
    }

    @Override
    public ASN1ObjectIdentifier getContentType()
    {
      return contentType;
    }
  }
  
}
