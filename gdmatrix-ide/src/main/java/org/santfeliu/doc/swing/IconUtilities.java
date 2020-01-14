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
package org.santfeliu.doc.swing;

import java.net.URL;

import javax.activation.FileTypeMap;

import javax.swing.ImageIcon;

import org.santfeliu.util.MimeTypeMap;

/**
 *
 * @author unknown
 */
public class IconUtilities
{
  public static ImageIcon getIcon(String mimeType)
  {
    ImageIcon icon = null;
    String extension = "binary";
    FileTypeMap map = MimeTypeMap.getDefaultFileTypeMap();
    if (map instanceof MimeTypeMap)
    {
      String ext = ((MimeTypeMap)map).getExtension(mimeType);
      if (ext != null) extension = ext;
    }
    String base = "/org/santfeliu/doc/swing/resources/icon/ext/";
    String urlString = base + extension + ".png";
    IconUtilities iu = new IconUtilities();
    try
    {
      URL url = iu.getClass().getResource(urlString);
      icon = new ImageIcon(url);
    }
    catch (Exception ex)
    {
      try
      {
        URL url = iu.getClass().getResource(base + "binary.png");
        icon = new ImageIcon(url);
      }
      catch (Exception ex2)
      {
      }
    }
    return icon;
  }
}
