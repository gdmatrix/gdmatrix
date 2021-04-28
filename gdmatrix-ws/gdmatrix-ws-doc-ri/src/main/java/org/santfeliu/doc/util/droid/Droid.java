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

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.FileFormatCollection;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;

/**
 *
 * @author blanquepa
 */
public class Droid
{
 
  private final File baseDir;
  private SignatureResolvers resolvers;
  private SignatureFileManagers signatureFileManagers;
  private final DroidUpdateService updateService;

  public Droid(File baseDir)
  {
    this(baseDir, false);
  }
  
  /**
   * Set baseDir of signatures files location. 
   * If forceUpdate is true then it force the download of last versions of those 
   * files in baseDir directory.
   * 
   * @param baseDir
   * @param forceUpdate 
   */
  public Droid(File baseDir, boolean forceUpdate)
  { 
    this.baseDir = baseDir;    
    this.resolvers = SignatureResolvers.createInstance();      
    this.signatureFileManagers = SignatureFileManagers.createInstance();
    this.updateService = new DroidUpdateService(baseDir, signatureFileManagers);   
    if (forceUpdate)
      updateService.update();
    readSignatureFiles(signatureFileManagers);
  }

  /**
   * Read the signature file
   *
   * @param signatureFile
   * @throws java.lang.Exception
   */
  public void readSignatureFile(File signatureFile) throws Exception
  {
    resolvers.getBinaryResolver().readSignatureFile(signatureFile);
  }

  /**
   * Read the signature file
   *
   * @param signatureFile
   * @throws java.lang.Exception
   */
  public void readSignatureFile(String signatureFile) throws Exception
  {
    readSignatureFile(new File(signatureFile));
  }
    
  /**
   * get the signature file version
   *
   * @return
   */
  public FFSignatureFile getSignatureFile()
  {
    return resolvers.getBinaryResolver().getDroidCore().getSigFile();
  }

  /**
   * get the file format by PUID
   *
   * @param PUID
   * @return
   */
  public FileFormat getFileFormat(String PUID)
  {
    FileFormat format = null;
    BinarySignatureResolver bResolver = resolvers.getBinaryResolver();
    ContainerSignaturesResolver cResolver = bResolver.getContainerResolver();
    if (!cResolver.isContainerFormat(PUID))
    {
      int i = 0;
      boolean found = false;         
      FFSignatureFile sigFile = bResolver.getDroidCore().getSigFile();
      while (i < sigFile.getNumFileFormats() && !found)
      {
        format = sigFile.getFileFormat(i);
        found = format.getPUID().equals(PUID);
        i++;
      }
    }
    return format;
  }

  /**
   * identify files using droid
   *
   * @param filename full path to a disk file
   * @return IdentificationFile
   */
  public FileFormat identify(String filename) 
  {
    FileFormat result = null;
    try
    {
      BinarySignatureResolver resolver = resolvers.getBinaryResolver();
      IdentificationResult idenResult = resolver.identify(filename);

      if (idenResult != null)
      {
        FileFormatCollection fileFormatCollection
          = resolver.getDroidCore().getSigFile().getFileFormatCollection();
        result = fileFormatCollection.getFormatForPUID(idenResult.getPuid());  
      }
    }
    catch (Exception ex)
    {
      Logger.getLogger(Droid.class.getName()).log(Level.SEVERE, null, ex);
    }
          
    return result;
  }

  /**
   * Determines whether Pronom has a newer signature file available.
   *
   * @param currentVersion
   * @return
   */
  public boolean isNewerSigFileAvailable(int currentVersion)
  {
    return false;
  }
  
  private boolean readSignatureFiles(SignatureFileManagers 
    signatureFileManagers) 
  {
    try
    {
      for (Resolver resolver : resolvers.getList())
      {
        DroidSignatureFileManager manager =
          signatureFileManagers.get(resolver.getType());
        Path dir = baseDir.toPath();
        Path sigFile = manager.getCurrentFile(dir);
        if (sigFile == null)
          sigFile = manager.downloadFile(dir);
        resolver.readSignatureFile(sigFile.toFile());        
      }
           
      return true;
    }
    catch (Exception ex)
    {
      Logger.getLogger(Droid.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
  }   
 
  public static void main(String args[])
  {
    try
    {
      Droid droid = new Droid(new File("C:/gdmatrix/conf"));  
      FileFormat format = droid.identify("c:/tmp/container-test/apache-tomcat-6.0.47.zip");
      System.out.println("VSD PUID: " + format.getPUID() + " " + 
        format.getName() + " " + format.getVersion());        
    }
    catch (Exception ex)
    {
    }
  }
}
