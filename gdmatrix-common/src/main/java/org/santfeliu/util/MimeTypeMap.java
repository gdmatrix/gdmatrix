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

import java.io.File;

import java.util.HashMap;
import java.util.Map;

import javax.activation.FileTypeMap;

/**
 *
 * @author unknown
 */
public class MimeTypeMap extends FileTypeMap
{
  private static final String defaultType = "application/octet-stream";
  private Map extMap = new HashMap();
  private Map mimeMap = new HashMap();

  public MimeTypeMap()
  {
    addMimeType("text/html", new String[]{"htm", "html"});
    addMimeType("text/plain", new String[]{"txt", "text", "java"});
    addMimeType("text/css", new String[]{"css"});
    addMimeType("text/xml", new String[]{"xml"});        
    addMimeType("image/gif", new String[]{"gif"});
    addMimeType("image/jpeg", new String[]{"jpg", "jpeg"});
    addMimeType("image/tiff", new String[]{"tif", "tiff"});
    addMimeType("image/bmp", new String[]{"bmp"});
    addMimeType("image/png", new String[]{"png"});
    addMimeType("image/svg+xml", new String[]{"svg"});
    addMimeType("application/postscript", new String[]{"ai", "eps", "ps"});
    addMimeType("application/rtf", new String[]{"rtf"});
    addMimeType("application/msword", new String[]{"doc"});
    addMimeType("application/vnd.ms-excel", new String[]{"xls"});   
    addMimeType("application/vnd.ms-powerpoint", new String[]{"ppt", "pps"});
    addMimeType("application/x-msaccess", new String[]{"mdb", "accdb"});
    addMimeType("application/x-tex", new String[]{"tex"});
    addMimeType("application/x-texinfo", new String[]{"texinfo", "texi"});
    addMimeType("application/x-javascript", new String[]{"js"});
    addMimeType("application/zip", new String[]{"zip"});
    addMimeType("application/pdf", new String[]{"pdf"});
    addMimeType("application/postscript", new String[]{"ps", "eps", "ai"});
    addMimeType("application/dxf", new String[]{"dxf"});
    addMimeType("application/acad", new String[]{"dwg"});
    addMimeType("application/x-java-jnlp-file", new String[]{"jnlp"});
    addMimeType("application/pkcs7-signature", new String[]{"p7s"});
    addMimeType("application/pkcs7-mime", new String[]{"p7m"});
    addMimeType("application/vnd.oasis.opendocument.text", new String[]{"odt"});
    addMimeType("application/vnd.oasis.opendocument.spreadsheet", new String[]{"ods"});    
    addMimeType("application/vnd.oasis.opendocument.presentation", new String[]{"odp"});        
    addMimeType("application/vnd.oasis.opendocument.database", new String[]{"odb"});            
    addMimeType("application/vnd.oasis.opendocument.graphics", new String[]{"odg"});            
    addMimeType("application/vnd.oasis.opendocument.chart", new String[]{"odc"});                
    addMimeType("application/vnd.oasis.opendocument.formula", new String[]{"odf"});                
    addMimeType("application/vnd.oasis.opendocument.image", new String[]{"odi"});                    
    addMimeType("application/vnd.oasis.opendocument.text-master", new String[]{"odm"});
    addMimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document", new String[]{"docx"});
    addMimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.template", new String[]{"dotx"});
    addMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new String[]{"xlsx"});
    addMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.template", new String[]{"xltx"});
    addMimeType("application/vnd.openxmlformats-officedocument.presentationml.presentation", new String[]{"pptx"});
    addMimeType("application/vnd.openxmlformats-officedocument.presentationml.slideshow", new String[]{"ppsx"});
    addMimeType("application/tcq", new String[]{"tcq"});
    addMimeType("application/octet-stream", new String[]{"bin", "exe"});                           
    addMimeType("audio/basic", new String[]{"au"});
    addMimeType("audio/midi", new String[]{"mid", "midi"});
    addMimeType("audio/mpeg", new String[]{"mp3", "mp2"});        
    addMimeType("audio/x-mpeg", new String[]{"mpg", "mpeg"});
    addMimeType("audio/x-wav", new String[]{"wav"});
    addMimeType("video/mpeg", new String[]{"mpg", "mpe"});
    addMimeType("video/quicktime", new String[]{"qt", "mov"});
    addMimeType("video/x-msvideo", new String[]{"avi"});
    addMimeType("video/x-ms-wmv", new String[]{"wmv"});
  }
  
  public String getContentType(File file)
  {
    return getContentType(file.getName());
  }
  
  public String getContentType(String filename)
  {
    int index = filename.lastIndexOf(".");
    if (index == -1) return defaultType;
    String extension = filename.substring(index + 1).toLowerCase();
    String mimeType = (String)extMap.get(extension);
    return mimeType == null ? defaultType : mimeType;
  }
  
  public final void addMimeType(String mimeType, String[] extensions)
  {
    for (int i = 0; i < extensions.length; i++)
    {
      String extension = extensions[i].toLowerCase();      
      extMap.put(extension, mimeType);
    }
    mimeMap.put(mimeType, extensions[0]);
  }
  
  public String getExtension(String mimeType)
  {
    return (String)mimeMap.get(mimeType);
  }
  
  public static MimeTypeMap getMimeTypeMap()
  {
    MimeTypeMap mimeMap;
    
    FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();
    if (map instanceof MimeTypeMap)
    {
      mimeMap = (MimeTypeMap)map;
    }
    else
    {
      mimeMap = new MimeTypeMap();
    }
    return mimeMap;
  }
}
