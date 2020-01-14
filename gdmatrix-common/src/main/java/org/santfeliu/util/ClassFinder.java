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
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author realor
 */
public class ClassFinder
{
  public static List<Class> findClasses(String packageName, String pattern,
    Class<? extends Annotation> annotation)
  {
    ArrayList<Class> classes = new ArrayList<Class>();
    try
    {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      String path = packageName.replace('.', '/');
      Enumeration<URL> resources = classLoader.getResources(path);
      List<File> dirs = new ArrayList<File>();
      while (resources.hasMoreElements())
      {
        URL resource = resources.nextElement();
        dirs.add(new File(resource.getFile()));
      }
      for (File dir : dirs)
      {
        findClasses(dir, packageName, pattern, annotation, classes);
      }
    }
    catch (Exception ex)
    {
    }
    return classes;
  }

  private static void findClasses(File dir, String packageName,
    String pattern, Class<? extends Annotation> annotation, List<Class> classes)
    throws Exception
  {
    if (dir.exists())
    {
      File[] files = dir.listFiles();
      for (File file : files)
      {
        if (file.isDirectory())
        {
          findClasses(file, packageName + "." + file.getName(), 
            pattern, annotation, classes);
        }
        else if (file.getName().endsWith(".class"))
        {
          try
          {
            String className = packageName + '.' +
              file.getName().substring(0, file.getName().length() - 6);
            if (pattern == null || className.matches(pattern))
            {
              Class cls = Class.forName(className);
              if (annotation == null || cls.getAnnotation(annotation) != null)
              {
                classes.add(cls);
              }
            }
          }
          catch (Throwable ex)
          {
          }
        }
      }
    }
  }
}
