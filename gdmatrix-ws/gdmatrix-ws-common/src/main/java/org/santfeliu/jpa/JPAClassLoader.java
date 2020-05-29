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
package org.santfeliu.jpa;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public class JPAClassLoader extends ClassLoader
{
  static Logger logger = Logger.getLogger("JPAClassLoader");

  public JPAClassLoader(ClassLoader loader)
  {
    super(loader);
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException
  {
    logger.log(Level.INFO, "JPAClassLoader.getResources from " + name);
    Enumeration<URL> urls;
    if (name.equals("META-INF/persistence.xml"))
    {
      File dir = new File(MatrixConfig.getDirectory(), "jpa");
      List<URL> persistenceFiles = new ArrayList<URL>();
      findPersistenceFiles(dir, persistenceFiles);
      logger.log(Level.INFO,
        "JPAClassLoader.getResources return " + persistenceFiles);
      urls = Collections.enumeration(persistenceFiles);
    }
    else 
      urls = super.getResources(name);
      
      while(urls.hasMoreElements())
      {
        logger.log(Level.INFO, urls.nextElement().toString()); 
      }
      
      return urls;
  }
  
  private void findPersistenceFiles(File dir, List<URL> persistenceFiles)
    throws MalformedURLException
  {
    File[] files = dir.listFiles();
    for (File file : files)
    {
      if (file.isDirectory())
      {
        findPersistenceFiles(file, persistenceFiles);
      }
      else if (file.isFile())
      {
        if (dir.getName().equals("META-INF") && 
          file.getName().endsWith("persistence.xml"))
        {
          persistenceFiles.add(file.toURI().toURL());
        }
      }
    }
  }
}
