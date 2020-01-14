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
package org.santfeliu.doc.util;

import java.net.URL;
import uk.gov.nationalarchives.droid.AnalysisController;
import uk.gov.nationalarchives.droid.FileFormatHit;
import uk.gov.nationalarchives.droid.IdentificationFile;
import uk.gov.nationalarchives.droid.binFileReader.ByteReader;
import uk.gov.nationalarchives.droid.signatureFile.FFSignatureFile;
import uk.gov.nationalarchives.droid.signatureFile.FileFormat;
import static uk.gov.nationalarchives.droid.binFileReader.AbstractByteReader.newByteReader;


/**
 *
 * @author realor
 */
public class Droid
{
  private AnalysisController analysisControl = null;
  private String version = null;

  /**
   * Create the AnalysisController
   * and set the config and signature file.
   *
   * @param configFile
   */
  public Droid(URL configFile) throws Exception
  {
    analysisControl = new AnalysisController();
    analysisControl.readConfiguration(configFile);
  }

  /**
   * No-args constructor. To be used when no config file is required.
   *
   * @throws Exception
   */
  public Droid() throws Exception
  {
    analysisControl = new AnalysisController();
  }

  /**
   * Create the AnalysisController
   * and set the config and signature file.
   *
   * @param configFile
   */
  public Droid(URL configFile, URL sigFileURL) throws Exception
  {
    analysisControl = new AnalysisController();
    analysisControl.readConfiguration(configFile);
  }

  /**
   * Downloads a new signature file using the setting in the DROID config file but does not load it into DROID.
   *
   * @param fileName
   */
  public void downloadSigFile(String fileName)
  {
    analysisControl.downloadwwwSigFile(fileName, false);
  }

  /**
   * Read the signature file
   *
   * @param signatureFile
   */
  public void readSignatureFile(URL signatureFile) throws Exception
  {
    version = analysisControl.readSigFile(signatureFile);
  }

  /**
   * Read the signature file
   *
   * @param signatureFile
   */
  public void readSignatureFile(String signatureFile) throws Exception
  {
    version = analysisControl.readSigFile(signatureFile);
  }

  /**
   * get the signature file version
   *
   * @return
   */
  public FFSignatureFile getSignatureFile()
  {
    return analysisControl.getSigFile();
  }

  /**
   * get the file format by PUID
   *
   * @return
   */
  public FileFormat getFileFormat(String PUID)
  {
    FileFormat format = null;
    int i = 0;
    boolean found = false;
    FFSignatureFile sigFile = getSignatureFile();
    while (i < sigFile.getNumFileFormats() && !found)
    {
      format = sigFile.getFileFormat(i);
      found = format.getPUID().equals(PUID);
      i++;
    }
    return format;
  }
  
  /**
   * get the signature file version
   *
   * @return
   */
  public String getSignatureFileVersion()
  {
    return version;
  }

  /**
   * Sets the URL of the signature file webservices
   *
   * @param sigFileURL
   */
  public void setSigFileURL(String sigFileURL)
  {
    analysisControl.setSigFileURL(sigFileURL);
  }

  /**
   * identify files using droid
   *
   * @param file   full path to a disk file
   * @return IdentificationFile
   */
  public IdentificationFile identify(String file)
  {
    IdentificationFile identificationFile = new IdentificationFile(file);
    ByteReader byteReader = null;
    byteReader = newByteReader(identificationFile);
    analysisControl.getSigFile().runFileIdentification(byteReader);

    return identificationFile;
  }

  /**
   * Determines whether Pronom has a newer signature file available.
   *
   * @param currentVersion
   * @return
   */
  public boolean isNewerSigFileAvailable(int currentVersion)
  {
    return analysisControl.isNewerSigFileAvailable(currentVersion);
  }

  public static void main(String args[])
  {
    try
    {
      URL configURL = new URL("file:///C:/DROID_config.xml");
      Droid droid = new Droid(configURL);
      droid.readSignatureFile("C:/DROID_signature.xml");
      IdentificationFile idf = droid.identify("c:/serveis.doc");
      System.out.println("idt:" + idf.getClassificationText());
      System.out.println("id:" + idf.getClassification());
      int nh = idf.getNumHits();
      System.out.println("hits:" + nh);
      for (int i = 0; i < nh; i++)
      {
        FileFormatHit hit = idf.getHit(i);
        FileFormat format = hit.getFileFormat();
        System.out.println("ID: " + format.getID());
        System.out.println("mimeType: " + format.getMimeType());
        System.out.println("name: " + format.getName());
        System.out.println("PUID: " + format.getPUID());
        System.out.println("PUID: " + format.getVersion());
      }
      FileFormat format = droid.getFileFormat("fmt/109");
      System.out.println("PUID: " + format.getPUID());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}

