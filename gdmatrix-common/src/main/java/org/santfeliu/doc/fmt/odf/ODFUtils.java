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
package org.santfeliu.doc.fmt.odf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.santfeliu.util.template.Template;

/**
 *
 * @author realor
 */
public class ODFUtils
{
  public static void merge(String in, String out, Map variables)
    throws Exception
  {
    merge(new File(in), new File(out), variables);
  }

  public static void merge(File in, File out, Map variables) throws Exception
  {
    byte[] buffer = new byte[4096];
    ZipFile zipFile = new ZipFile(in);
    try
    {
      ZipOutputStream zipos = new ZipOutputStream(new FileOutputStream(out));
      try
      {
        Enumeration entries = zipFile.entries();
        while (entries.hasMoreElements())
        {
          ZipEntry entry = (ZipEntry)entries.nextElement();
          String entryName = entry.getName();
          File contentFile = null;
          InputStream is = zipFile.getInputStream(entry);
          try
          {            
            if (entryName.equals("content.xml"))
            {
              Reader reader = new InputStreamReader(is, "UTF-8");
              contentFile = File.createTempFile("out", ".xml");
              OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(contentFile), "UTF-8");
              try
              {
                Template template = Template.create(reader);
                template.merge(variables, writer);
              }
              finally
              {
                writer.close();
              }
              is = new FileInputStream(contentFile);
              entry.setSize(contentFile.length());
              entry.setCompressedSize(-1);
            }
            // write entry to new zip file
            zipos.putNextEntry(entry);
            int nr = is.read(buffer);
            while (nr > 0)
            {
              zipos.write(buffer, 0, nr);
              nr = is.read(buffer);
            }
            zipos.closeEntry();            
          }
          finally
          {
            is.close();
            if (contentFile != null) contentFile.delete();
          }
        }
      }
      finally
      {
        zipos.close();
      }
    }
    finally
    {
      zipFile.close();
    }
  }

  public static void main(String[] args)
  {
    try
    {
      File in = new File("c:/sample.odt");
      File out = new File("c:/sample_merged.odt");
      HashMap variables = new HashMap();
      variables.put("nom", "xxxxx");
      variables.put("cognom", "yyyyy");
      variables.put("nif", "NNNNNNNNX");
      ODFUtils.merge(in, out, variables);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
