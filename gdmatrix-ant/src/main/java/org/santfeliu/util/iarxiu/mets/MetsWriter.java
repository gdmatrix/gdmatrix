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
package org.santfeliu.util.iarxiu.mets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author blanquepa
 */
public class MetsWriter
{
  private Mets mets;

  private Document doc;

  public MetsWriter(Mets mets)
  {
    this.mets = mets;
  }

  public void save(OutputStream os) throws Exception
  {
    if (mets != null)
    {
      DocumentBuilderFactory dbf =
        DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      doc = dbf.newDocumentBuilder().newDocument();

      //Create root parent (mets tag)
      Element metsElement = doc.createElementNS(MetsConstants.XMLNS,
        setMetsPrefix(MetsConstants.METS_TAG));
      metsElement.setAttribute("xmlns:METS", MetsConstants.XMLNS);
      metsElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      metsElement.setAttribute("xsi:schemaLocation",
        "http://www.loc.gov/METS/ http://loc.gov/standards/mets/mets/xsd");
      metsElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
      metsElement.setAttribute(MetsConstants.TYPE_ATTRIBUTE, mets.getType());
      doc.appendChild(metsElement);

      //DmdSec
      for (Metadata metadata : mets.getDmdMetadatas())
      {
        createDmdSection(metadata, metsElement);
      }

      //AmdSec
      for (Metadata metadata : mets.getAmdMetadatas())
      {
        //TODO: Posibilitar otros tags MD <rightsMD>, <sourceMD> y <digiprovMD>
        createAmdSection(metadata, metsElement, MetsConstants.TECHMD_TAG);
      }

      //FileSec
      createFileSection(mets.getFileGrp(), metsElement);

      //StructMap
      createStructMapSection(mets.getStruct(), metsElement);
    }

    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.transform(new DOMSource(doc), 
      new StreamResult(new OutputStreamWriter(os, "UTF-8")));

//    org.apache.xml.security.Init.init();
//    XMLUtils.outputDOM(doc, os);
  }
  

  private void createDmdSection(Metadata metadata, Element metsElement)
    throws Exception
  {
    Element dmdSection =
      doc.createElement(setMetsPrefix(MetsConstants.DMD_SECTION_TAG));
    dmdSection.setAttribute(MetsConstants.ID_ATTRIBUTE, metadata.getId());
    metsElement.appendChild(dmdSection);

    createMdWrap(dmdSection, metadata);
  }

  private void createAmdSection(Metadata metadata, Element metsElement,
    String tagMD) throws Exception
  {
    Element amdSection =
      doc.createElement(setMetsPrefix(MetsConstants.AMD_SECTION_TAG));
    amdSection.setAttribute(MetsConstants.ID_ATTRIBUTE, metadata.getId());
    metsElement.appendChild(amdSection);

    Element elementMD = doc.createElement(setMetsPrefix(tagMD));
    elementMD.setAttribute(MetsConstants.ID_ATTRIBUTE, metadata.getId() + ".0");
    amdSection.appendChild(elementMD);

    createMdWrap(elementMD, metadata);
  }
  
  private void createFileSection(Collection<FileGrp> fileGrps,
    Element metsElement) throws Exception
  {
    Element fileSecElement =
        doc.createElement(setMetsPrefix(MetsConstants.FILE_SECTION_TAG));
    metsElement.appendChild(fileSecElement);

    for (FileGrp fileGrp : fileGrps)
    {
      processContent(fileGrp);
      addFileGrpToFileSec(fileSecElement, fileGrp);
    }    
  }

  private void createStructMapSection(Div struct, Element metsElement)
  {
    Element structMapElement =
      doc.createElement(setMetsPrefix(MetsConstants.STRUCT_MAP_SECTION_TAG));
    metsElement.appendChild(structMapElement);

    for (Div div : struct.getDivs())
    {
      addDivToStructMap(div, structMapElement);
    }
  }

  //Metadata
  private void createMdWrap(Element parent, Metadata metadata) throws Exception
  {
    Element mdWrap = doc.createElement(setMetsPrefix(MetsConstants.MDWRAP_TAG));

    mdWrap.setAttribute(MetsConstants.MDTYPE_ATTRIBUTE, metadata.getType());
    if (MetsConstants.OTHER_MDTYPE_VALUE.equals(metadata.getType()))
    {
      mdWrap.setAttribute(MetsConstants.OTHERMDTYPE_ATTRIBUTE,
        ((OtherMetadata)metadata).getUrn());
    }

    metadata.write(mdWrap, mets);
    parent.appendChild(mdWrap);
  }

  //Structure methods
  private void addDivToStructMap(Div struct, Element parent)
  {
    FileGrp fileGrp = struct.getFileGrp();

    Element element = doc.createElement(setMetsPrefix(MetsConstants.DIV_TAG));
    element.setAttribute(MetsConstants.LABEL_ATTRIBUTE, struct.getLabel());
    String ids = struct.getDmdMetadataIds();
    if (ids != null)
      element.setAttribute(MetsConstants.DMDID_ATTRIBUTE, ids);
    ids = struct.getAmdMetadataIds();
    if (ids != null)
      element.setAttribute(MetsConstants.ADMID_ATTRIBUTE, ids);
    parent.appendChild(element);

    if (fileGrp == null)
    {
      for (Div div : struct.getDivs())
      {
        addDivToStructMap(div, element);
      }
    }
    else if (fileGrp != null)
    {
      addFileGrpToStructMap(fileGrp, element);
    }
  }

  private void addFileGrpToStructMap(FileGrp fileGrp, Element parent)
  {
//    Element div = doc.createElement(setMetsPrefix(MetsConstants.DIV_TAG));
//    div.setAttribute(MetsConstants.LABEL_ATTRIBUTE, fileGrp.getName());
//    String metadataId = fileGrp.getMetadataId();
//    if (metadataId != null)
//      div.setAttribute(MetsConstants.ADMID_ATTRIBUTE, fileGrp.getMetadataId());
//    parent.appendChild(div);

    Element fptr = doc.createElement(setMetsPrefix(MetsConstants.FPTR_TAG));
    fptr.setAttribute(MetsConstants.FILEID_ATTRIBUTE, fileGrp.getId());
    parent.appendChild(fptr);
  }

  //File section methods
  private void addFileGrpToFileSec(Element fileSecElement, FileGrp fileGrp)
  {
    Element fileGrpElement = doc.createElement(
      setMetsPrefix(MetsConstants.FILEGRP_TAG));
    fileGrpElement.setAttribute(MetsConstants.ID_ATTRIBUTE, fileGrp.getId());
    fileSecElement.appendChild(fileGrpElement);

    Element file = doc.createElement(setMetsPrefix(MetsConstants.FILE_TAG));
    file.setAttribute(
      MetsConstants.ID_ATTRIBUTE, fileGrp.getId() + ".0"); //TODO: crear mejor
    file.setAttribute(
      MetsConstants.MIMETYPE_ATTRIBUTE, fileGrp.getMimeType());
    file.setAttribute(
      MetsConstants.CHECKSUM_ATTRIBUTE, fileGrp.getChecksum());
    file.setAttribute(
      MetsConstants.CHECKSUMTYPE_ATTRIBUTE, fileGrp.getChecksumType());
    file.setAttribute(
      MetsConstants.CREATED_ATTRIBUTE, fileGrp.getCreatedDateTime());
    fileGrpElement.appendChild(file);

    Element fLocat = doc.createElement(setMetsPrefix(MetsConstants.FLOCAT_TAG));
    fLocat.setAttribute(MetsConstants.HREF_ATTRIBUTE, fileGrp.getHref());
    fLocat.setAttribute(MetsConstants.LOCTYPE_ATTRIBUTE, "URL");
    file.appendChild(fLocat);
  }
  
  private void processContent(FileGrp fileGrp)
    throws Exception
  {
    File file = fileGrp.getFile();
    if (file != null)
    {
      FileInputStream fis = new FileInputStream(file);
      try
      {
        File tmpFile = File.createTempFile("digest", ".tmp");
        tmpFile.deleteOnExit();
        FileOutputStream dfos = new FileOutputStream(tmpFile);
        try
        {
          MessageDigest md = MessageDigest.getInstance("SHA-1");
          DigestOutputStream dos = new DigestOutputStream(dfos, md);
          try
          {

            dos.on(true);
            if (fileGrp.getHref() == null) fileGrp.setHref(file.getName());

            byte[] buffer = new byte[4096];
            int length = 0;
            while ((length = fis.read(buffer)) > 0)
            {
              dos.write(buffer, 0, length);
            }

            //File digest
            String checksum =
              String.valueOf(bytesToHex(dos.getMessageDigest().digest()));
            fileGrp.setChecksum(checksum);
            fileGrp.setChecksumType("SHA-1");
            fileGrp.setCreatedDateTime(getDateAsISO8601String(new Date()));
          }
          finally
          {
            dos.close();
          }
        }
        finally
        {
          dfos.close();
        }
      }
      finally
      {
        fis.close();
      }
    }
  }


  private static final char[] kDigits = { '0', '1', '2', '3', '4', '5', '6',
    '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  private static char[] bytesToHex(byte[] raw)
  {
    int length = raw.length;
    char[] hex = new char[length * 2];
    for (int i = 0; i < length; i++) {
      int value = (raw[i] + 256) % 256;
      int highIndex = value >> 4;
      int lowIndex = value & 0x0f;
      hex[i * 2 + 0] = kDigits[highIndex];
      hex[i * 2 + 1] = kDigits[lowIndex];
    }
    return hex;
  }

  private String getDateAsISO8601String(Date date)
  {
    String result =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(date);
    //convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00
    //- note the added colon for the Timezone
    result = result.substring(0, result.length()-2)
      + ":" + result.substring(result.length()-2);
    return result;
  }

  private String setMetsPrefix(String tag)
  {
    return MetsConstants.METS_PREFIX + ":" + tag;
  }
}
