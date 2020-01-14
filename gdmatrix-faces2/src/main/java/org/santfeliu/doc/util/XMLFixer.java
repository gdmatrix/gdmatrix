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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MemoryDataSource;

/**
 *
 * @author realor
 */
public class XMLFixer
{
  // some XML files may have a trailing line like this (due to web transfer)
  // uuid: xxxx-xxxx-xxxx-xxxx-xxxx
  // fix: trim to last symbol '>' (ascii 62)
    
  private final int size;
  private final byte[] bytes;
  
  public XMLFixer(InputStream is) throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    IOUtils.writeToStream(is, bos);
    bytes = bos.toByteArray();
    int i = bytes.length - 1;
    while (i >= 0 && bytes[i] != 62)
    {
      i--;
    }
    size = i + 1;
  }
  
  public int getFixedSize()
  {
    return size;
  }
  
  public InputStream getFixedStream()
  {
    return new ByteArrayInputStream(bytes, 0, size);
  }
  
  public MemoryDataSource getFixedDataSource()
  {
    return new MemoryDataSource(bytes, 0, size, "xml", "text/xml");
  }
}
