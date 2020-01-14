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
package org.santfeliu.doc.test;

import java.net.URL;

import org.santfeliu.doc.util.Droid;
import uk.gov.nationalarchives.droid.FileFormatHit;
import uk.gov.nationalarchives.droid.IdentificationFile;
import uk.gov.nationalarchives.droid.signatureFile.FileFormat;


/**
 *
 * @author unknown
 */
public class DroidTest
{
  public DroidTest()
  {
  }

  public static void main(String args[])
  {
    try
    {
      URL configURL = new URL("file:///C:/DROID_config.xml");
      Droid droid = new Droid(configURL);
      droid.readSignatureFile("C:/DROID_signature.xml");
      IdentificationFile idf = droid.identify("c:/Acta.doc");
      System.out.println("idt:" + idf.getClassificationText());
      System.out.println("id:" + idf.getClassification());
      int nh = idf.getNumHits();
      System.out.println("hits:" + nh);
      for (int i = 0; i < nh; i++)
      {
        FileFormatHit hit = idf.getHit(i);
        FileFormat format = hit.getFileFormat();
        System.out.println("mimeType: " + format.getMimeType());
        System.out.println("name: " + format.getName());
        System.out.println("PUID: " + format.getPUID());
        System.out.println("PUID: " + format.getVersion());
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
