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
package org.santfeliu.util.iarxiu.pit;

import org.santfeliu.util.iarxiu.converter.Converter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.santfeliu.util.iarxiu.mets.Div;
import org.santfeliu.util.iarxiu.mets.FileGrp;
import org.santfeliu.util.iarxiu.mets.Mets;
import org.santfeliu.util.iarxiu.mets.MetsLoader;
import org.santfeliu.util.iarxiu.mets.MetsWriter;

/**
 *
 * @author blanquepa
 */
public class PIT
{
  public static final String CASE_TEMPLATE_URN =
    "urn:iarxiu:2.0:templates:catcert:PL_expedient";
  public static final String DOCUMENT_TEMPLATE_URN =
    "urn:iarxiu:2.0:templates:catcert:PL_document";

  public static final String CASE_URN =
    "urn:iarxiu:2.0:vocabularies:catcert:Voc_expedient";
  public static final String CASE_DOCUMENT_URN =
    "urn:iarxiu:2.0:vocabularies:catcert:Voc_document_exp";
  public static final String DOCUMENT_URN =
    "urn:iarxiu:2.0:vocabularies:catcert:Voc_document";
  public static final String SIGNATURE_URN =
    "urn:iarxiu:2.0:vocabularies:catcert:Voc_signatura";

  private Mets mets;
  private File zipFile;
  private Map<String, Class> converters;
  private boolean complex;

  public PIT() throws Exception
  {
    mets = new Mets();
    URL url = this.getClass().getResource(".");
    mets.registerSchema(CASE_URN,
      this.getClass().getResourceAsStream("xsd/Voc_expedient.xsd"));
    mets.registerSchema(DOCUMENT_URN,
      this.getClass().getResourceAsStream("xsd/Voc_document.xsd"));
    mets.registerSchema(CASE_DOCUMENT_URN,
      this.getClass().getResourceAsStream("xsd/Voc_document_exp.xsd"));
    mets.registerSchema(SIGNATURE_URN,
      this.getClass().getResourceAsStream("xsd/Voc_signatura.xsd"));
  }
  
  Mets getMets()
  {
    return mets;
  }

  File getZipFile()
  {
    return zipFile;
  }

  public String getType()
  {
    return mets.getType();
  }
  
  public void setType(String type)
  {
    mets.setType(type);
  }
  
  public boolean isComplex()
  {
    return complex;
  }
  
  //Cases
  public PITCase newCase() throws Exception
  {
    complex = true;
    Div struct = mets.getStruct();
    return addCase("CAS_" + struct.getDivs().size());
  }

  private PITCase addCase(String caseId) throws Exception
  {
    complex = true;

    if (mets.getType() == null)
      mets.setType(PIT.CASE_TEMPLATE_URN);

    if (!PIT.CASE_TEMPLATE_URN.equals(mets.getType()))
      throw new Exception("Invalid type: " + mets.getType() 
        + " not allows case creation");
    
    PITCase cas = new PITCase(this);
    cas.setCaseId(caseId);
    mets.getStruct().getDivs().add(cas.getDiv());
    return cas;
  }

  public PITCase getCase(String caseId)
  {
    PITCase cas = null;
    if (caseId != null && complex)
    {
      Div struct = mets.getStruct();

      for (Div div : struct.getDivs())
      {
        if (caseId.equals(div.getLabel()))
        {
          cas = new PITCase(this);
          cas.copyFrom(div);
          return cas;
        }
      }
    }
    return cas;
  }

  public List<PITCase> getCases()
  {
    List<PITCase> result = new ArrayList<PITCase>();
    if (complex)
    {
      Div struct = mets.getStruct();
      for (Div div : struct.getDivs())
      {
        PITCase cas = new PITCase(this);
        cas.copyFrom(div);
        result.add(cas);
      }
    }
    return Collections.unmodifiableList(result);
  }

  //Documents
  public PITDocument newDocument() throws Exception
  {
    Div struct = mets.getStruct();
    return addDocument("DOC_" + struct.getDivs().size());
  }

  private PITDocument addDocument(String docId) throws Exception
  {
    if (mets.getType() == null)
      mets.setType(PIT.DOCUMENT_TEMPLATE_URN);
    
    PITDocument doc = new PITDocument(this);
    doc.setDocId(docId);
    mets.getStruct().getDivs().add(doc.getDiv());
    return doc;
  }

  public PITDocument getDocument(String docId)
  {
    PITDocument doc = null;
    if (docId != null && !complex)
    {
      Div struct = mets.getStruct();

      for (Div div : struct.getDivs())
      {
        if (docId.equals(div.getLabel()))
        {
          doc = new PITDocument(this);
          doc.copyFrom(div);      
          return doc;
        }
      }
    }
    return doc;
  }

  public List<PITDocument> getDocuments()
  {
    List<PITDocument> result = new ArrayList<PITDocument>();
    if (!complex)
    {
      Div struct = mets.getStruct();
      for (Div div : struct.getDivs())
      {
        PITDocument d = new PITDocument(this);
        d.copyFrom(div);
        result.add(d);
      }
    }
    return Collections.unmodifiableList(result);
  }

  public List<File> getFiles() throws Exception
  {
    List<File> files = new ArrayList<File>();
    for (FileGrp fileGrp : mets.getFileGrp())
    {
      files.add(fileGrp.getFile());
    }
    return files;
  }

  public void load(File inFile) throws Exception
  {
    if (inFile != null && inFile.getName().endsWith(".xml"))
    {
      mets.load(inFile);
    }
    else if (inFile.getName().endsWith(".zip"))
    {
      zipFile = inFile;

      ZipFile zf = new ZipFile(zipFile);
      ZipEntry zipEntry = zf.getEntry("mets.xml");
      InputStream is = zf.getInputStream(zipEntry);
      try
      {
        mets.setFilePath(inFile.getAbsolutePath());
        MetsLoader loader = new MetsLoader(mets);
        loader.load(is);
      }
      finally
      {
        is.close();
      }
    }
    else
      throw new Exception("INVALID_FILE_FORMAT");
  }

  public void saveAsZip(File outZip) throws Exception
  {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outZip));
    try
    {
      Collection<FileGrp> fileGrps = mets.getFileGrp();
      for (FileGrp fileGrp : fileGrps)
      {
        String name = fileGrp.getHref();
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        InputStream is = fileGrp.getInputStream();

        byte[] buffer = new byte[4096];
        int length = 0;
        while ((length = is.read(buffer)) > 0)
        {
          zos.write(buffer, 0, length);
        }
        zos.closeEntry();
      }

      MetsWriter writer = new MetsWriter(mets);
      zos.putNextEntry(new ZipEntry("mets.xml"));
      writer.save(zos);
      zos.closeEntry();
      zipFile = outZip;
    }
    finally
    {
      zos.close();
    }
  }

  public void saveAsXml(File outFile) throws Exception
  {
    FileOutputStream fos = new FileOutputStream(outFile);
    try
    {
      MetsWriter writer = new MetsWriter(mets);
      writer.save(fos);
    }
    finally
    {
      fos.close();
    }
  }

  public void registerConverter(String urn, Class converterClass)
  {
    if (converters == null)
      converters = new HashMap<String, Class>();
    converters.put(urn, converterClass);
  }

  public void registerVocabulary(String urn, File xsdFile, Class converterClass)
    throws Exception
  {
    mets.registerSchema(urn, xsdFile);
    registerConverter(urn, converterClass);
  }

  Converter getConverter(String urn)
    throws Exception
  {
    Converter converter = null;
    Class clazz = converters.get(urn);
    if (clazz != null)
    {
      Object instance = clazz.newInstance();
      if (instance instanceof Converter)
        return (Converter)instance;
    }
    return converter;
  }
}
