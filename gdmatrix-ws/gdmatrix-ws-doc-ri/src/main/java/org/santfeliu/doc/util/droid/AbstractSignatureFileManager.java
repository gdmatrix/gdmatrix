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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 *
 * @author blanquepa
 */
public abstract class AbstractSignatureFileManager 
  implements DroidSignatureFileManager
{
  protected Path file;
   
  protected Path getDefaultFile(Path baseDir, String filename) 
    throws IOException
  {
    Path file = null;
    Stream<Path> stream = Files.walk(baseDir, 1);
    String pattern = "glob:**" + 
    filename.replace("%s", "*");
    PathMatcher matcher =
    FileSystems.getDefault().getPathMatcher(pattern);
    Iterator<Path> it = stream.iterator();
    while (it.hasNext() && file == null)
    {
      Path item = it.next();
      if (matcher.matches(item))
        file = item;
    }
    
    return file;
  }  
}
