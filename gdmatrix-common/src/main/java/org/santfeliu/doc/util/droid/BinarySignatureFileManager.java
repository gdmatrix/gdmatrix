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
package org.santfeliu.doc.util.droid;

import java.net.URL;
import java.nio.file.Path;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.pronom.PronomService;
import uk.gov.nationalarchives.pronom.PronomServiceService;
import uk.gov.nationalarchives.pronom.signaturefile.SigFile;
import uk.gov.nationalarchives.pronom.signaturefile.SignatureFileType;

/**
 *
 * @author blanquepa
 */
public class BinarySignatureFileManager extends AbstractSignatureFileManager
{
  private static final String DEFAULT_FILENAME_PATTERN = 
    "DROID_SignatureFile_V%s.xml";
  
  private final String filenamePattern = DEFAULT_FILENAME_PATTERN;

  @Override
  public Path downloadFile(Path baseDir) throws Exception
  {
    Class cls = getClass();
    String className = cls.getName();
    int index = className.lastIndexOf(".");
    String path = "/" + className.substring(0, index).replace('.', '/');
    URL wsdlLocation = cls.getResource(path + "/PRONOM.wsdl");    
    
    PronomServiceService service = new PronomServiceService(wsdlLocation);
    PronomService pronomService = service.getPronomServiceSoap();
    SigFile sigFile = pronomService.getSignatureFileV1();
    SignatureFileType sigFileType = sigFile.getFFSignatureFile();
    SignatureFileInfo sigFileInfo = importFile(sigFileType, baseDir);
    this.file = sigFileInfo.getFile();
    return this.file;
  }

  @Override
  public Path getCurrentFile(Path baseDir) throws Exception
  {
    if (this.file != null)
      return this.file;
    else
      return getDefaultFile(baseDir, DEFAULT_FILENAME_PATTERN);
  }
  
  private SignatureFileInfo importFile(
    SignatureFileType sigFileType, Path baseDir) throws Exception
  {
    int version = sigFileType.getVersion().intValue();
    SignatureFileInfo sigInfo
      = new SignatureFileInfo(version, false, SignatureType.BINARY);
    String fileName = String.format(filenamePattern, version);

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Document doc = dbf.newDocumentBuilder().newDocument();

    JAXBContext context = JAXBContext.newInstance(SignatureFileType.class);
    Marshaller marshaller = context.createMarshaller();
    QName qName
      = new QName("http://www.nationalarchives.gov.uk/pronom/SignatureFile",
        "FFSignatureFile");
    JAXBElement element = new JAXBElement<SignatureFileType>(qName,
      SignatureFileType.class, null, sigFileType);
    marshaller.marshal(element, doc);
    
    final Path outputFile = baseDir.resolve(fileName);
    
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", 
      "4");    
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");    
    DOMSource domSource = new DOMSource(doc);
    StreamResult streamResult = new StreamResult(outputFile.toFile());
    transformer.transform(domSource, streamResult); 
    
    sigInfo.setFile(outputFile);    
            
    return sigInfo;
  }

  @Override
  public ResolverType getResolverType()
  {
    return ResolverType.BINARY;
  }
  
}
