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

import com.sun.xml.ws.developer.StreamingDataHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import javax.activation.DataHandler;

/**
 *
 * @author unknown
 */
public class IOUtils
{
  public static void writeToStream(DataHandler dh, OutputStream os)
    throws IOException
  {
    if (dh instanceof StreamingDataHandler)
    {
      StreamingDataHandler sdh = (StreamingDataHandler)dh;
      try
      {
        // flush sdh to temporary file to ensure sdh.inputStream is closed
        File tempFile = File.createTempFile("file", ".tmp");
        try
        {
          sdh.moveTo(tempFile);
          writeToStream(new FileInputStream(tempFile), os);
        }
        finally
        {
          tempFile.delete();
        }
      }
      finally
      {
        sdh.close();
      }
    }
    else
    {
      try
      {
        dh.writeTo(os);
      }
      finally
      {
        os.close();
      }
    }
  }

  public static void writeToFile(DataHandler dh, File file) 
    throws IOException
  {
    if (dh instanceof StreamingDataHandler)
    {
      StreamingDataHandler sdh = (StreamingDataHandler)dh;
      try
      {
        sdh.moveTo(file);
      }
      finally
      {
        sdh.close();
      }
    }
    else
    {
      OutputStream os = new FileOutputStream(file);
      try
      {
        dh.writeTo(os);
      }
      finally
      {
        os.close();
      }
    }
  }

  public static File writeToFile(DataHandler dh) 
    throws IOException
  {
    File tempFile = File.createTempFile("file", ".tmp");
    writeToFile(dh, tempFile);
    return tempFile;
  }
  
  public static void writeToStream(InputStream is, OutputStream os) 
    throws IOException
  {
    if (is != null && os != null)
    {
      byte buffer[] = new byte[8 * 1024];
      int bytesRead;
      try
      {
        try 
        {
          while ((bytesRead = is.read(buffer)) > 0)
          {
            os.write(buffer, 0, bytesRead);
          }
        }
        finally
        {
          is.close();
        }        
      }
      finally
      {
        os.close();
      }
    }
  }

  public static void writeToFile(InputStream is, File file) throws IOException
  {
    try (OutputStream os = new FileOutputStream(file))
    {
      writeToStream(is, os);
    }
  }
  
  public static File writeToFile(InputStream is) throws IOException
  {
    File tempFile = File.createTempFile("stream", ".tmp");
    tempFile.deleteOnExit();
    writeToStream(is, new FileOutputStream(tempFile));    
    return tempFile;
  }

  public static void cropFile(File source, File dest,
    long startPos, long endPos) throws IOException
  {
    RandomAccessFile file = new RandomAccessFile(source, "r");
    long length = source.length();
    if (endPos > length) endPos = length;

    FileOutputStream os = new FileOutputStream(dest);
    try
    {
      file.seek(startPos);
      while (file.getFilePointer() < length)
      {
        int ch = file.read();
        os.write(ch);
      }
    }
    finally
    {
      os.close();
    }
  }

  public static void tailFile(File source, File dest, long size)
     throws IOException
  {
    RandomAccessFile file = new RandomAccessFile(source, "r");
    long length = source.length();
    long startPos = length - size;
    if (startPos < 0) startPos = 0;

    FileOutputStream os = new FileOutputStream(dest);
    try
    {
      file.seek(startPos);
      int ch = file.read();
      while (ch != -1)
      {
        os.write(ch);
        ch = file.read();
      }
    }
    finally
    {
      os.close();
    }
  }

  public static void main(String[] args)
  {
    try
    {
      IOUtils.tailFile(new File("xxxxx.log"),
        new File("c:/error.txt"), 1000000);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}