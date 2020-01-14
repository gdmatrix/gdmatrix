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
package org.santfeliu.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;


/**
 *
 * @author unknown
 */
public class LibraryLoader
{
  private static String NOT_FOUND = "NOT_FOUND";

  public LibraryLoader()
  {
  }
  
  public void loadLibrary(ClassLoader classLoader, String libName, String extension)
    throws IOException, NoSuchFieldException, IllegalAccessException
  {
    System.out.println("LibraryLoader: trying to load library: " + libName);
    String fileName = libName + "." + extension;

    URL inputStreamLibURL = classLoader.getResource(fileName);
    if(inputStreamLibURL == null)
      throw new IOException("Resource not found: " + fileName);
    String libraryPath = System.getProperty("java.library.path", NOT_FOUND);
    if(libraryPath.equals(NOT_FOUND))
      throw new IOException("Library path property not found");
    String[] splitPath = libraryPath.split(File.pathSeparator);
    String tempPath = splitPath[0];

    File tempDir = new File(tempPath);
    File defaultFile = new File(tempPath, fileName);
    boolean useDefaultFile = false;
    if(defaultFile.exists())
    {
      try
      {
        useDefaultFile = defaultFile.delete();
      }
      catch(Exception e)
      {
        e.printStackTrace();
        useDefaultFile = false;
      }
    }
    else
        useDefaultFile = true;
        
    File tempFile;
    if(useDefaultFile)
        tempFile = defaultFile;
    else
        tempFile = File.createTempFile(libName, "." + extension, tempDir);
    System.out.println("LibraryLoader: tempFile = " + tempFile);
    copy(inputStreamLibURL.openStream(), tempFile, 0);
    
    System.load(tempFile.getAbsolutePath());
    
    System.out.println("LibraryLoader: loaded successfull library: " + libName 
      + " in " + tempFile.getAbsolutePath());
  }
  
  public void copy(InputStream src, File dest, int bufferSize)
    throws IOException
  {
    if(bufferSize <= 0) 
      bufferSize = 2000;
    InputStream is = src;
    OutputStream os = new BufferedOutputStream(new FileOutputStream(dest));
    byte buffer[] = new byte[bufferSize];
    int c;
    while((c = is.read(buffer)) != -1) 
      os.write(buffer, 0, c);
    is.close();
    os.close();
  }  

}
