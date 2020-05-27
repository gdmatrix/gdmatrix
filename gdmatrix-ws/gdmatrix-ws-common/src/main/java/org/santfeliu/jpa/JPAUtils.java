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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import java.net.URL;
import java.util.HashMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author unknown
 */
public class JPAUtils
{
  private static final String TOPLINK_PROVIDER =
    "oracle.toplink.essentials.PersistenceProvider";

  static final Logger logger  = Logger.getLogger("JPAUtils");

  static HashMap<String, EntityManagerFactory> factories = 
    new HashMap<String, EntityManagerFactory>();

  static Set providers;

  public static EntityManager createEntityManager(String unitName)
    throws PersistenceException
  {
    EntityManagerFactory emf = getEntityManagerFactory(unitName);
    return emf.createEntityManager();
  }

  public static synchronized EntityManagerFactory
    getEntityManagerFactory(String unitName) throws PersistenceException
  {
    EntityManagerFactory factory = factories.get(unitName);
    if (factory == null)
    {
      logger.log(Level.INFO, ">>>>>>>>>>>>>> Creating {0}", unitName);

      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(new JPAClassLoader(cl));

      Map properties = createPersistenceUnitPropertiesMap(unitName);
      if (properties != null && !properties.isEmpty())
        factory = Persistence.createEntityManagerFactory(unitName, properties);
      else
        factory = Persistence.createEntityManagerFactory(unitName);
      logger.log(Level.INFO, ">>>>>>>>>>>>>> factory created {0}", factory);
      factories.put(unitName, factory);
    }
    return factory;
  }

  public static synchronized void closeEntityManagerFactory(String unitName)
  {
    EntityManagerFactory factory = factories.remove(unitName);
    if (factory != null)
    {
      try
      {
        factory.close();
      }
      catch (Exception ex)
      {
        Logger.getLogger("JPAUtils").log(Level.SEVERE, ex.getMessage());        
      }
    }
  }
  
  public static synchronized void closeEntityManagerFactories()
  {
    for (EntityManagerFactory factory : factories.values())
    {
      try
      {
        factory.close();
      }
      catch (Exception ex)
      {
        Logger.getLogger("JPAUtils").log(Level.SEVERE, ex.getMessage());    
      }
    }
    factories.clear();
  }

  public static void copy(Object src, Object dest)
  {
    Class srcClass = src.getClass();
    Class destClass = dest.getClass();
    Method[] methods = srcClass.getMethods();
    for (Method srcMethod : methods)
    {
      String name = srcMethod.getName();
      //System.out.println(name);
      if (name.startsWith("get") && 
          srcMethod.getParameterTypes().length == 0 && 
          isBasicType(srcMethod.getReturnType()))
      {
        try
        {
          Method destMethod = destClass.getMethod(
            "set" + name.substring(3), 
            new Class[]{srcMethod.getReturnType()});          
          Object value = srcMethod.invoke(src, new Object[0]);
          destMethod.invoke(dest, new Object[]{value});
        }
        catch (NoSuchMethodException e)
        {
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    }
  }

  public static void copyList(List srcList, List destList, Class destItemClass)
  {
    for(Object srcItem : srcList)
    {
      Object destItem;
      try
      {
        destItem = destItemClass.newInstance();
        JPAUtils.copy(srcItem, destItem);
        destList.add(destItem);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  public static String getQueryText(EntityManager em, String queryName)
    throws Exception
  {
    String queryText = null;
    if (getPresistenceProviders().contains(TOPLINK_PROVIDER))
    {
      Class clazz = Class.forName("org.santfeliu.jpa.TopLinkUtils");
      if (clazz != null)
      {
        Method method =
          clazz.getMethod("getQueryText", EntityManager.class, String.class);
        queryText = (String)method.invoke(clazz, em, queryName);
      }
    }
    else
      throw new Exception("Not supported");

    return queryText;
  }

  private static Set getPresistenceProviders()
        throws IOException
  {
    if (providers == null)
    {
      providers = new HashSet();

      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL url =
        loader.getResource(
        "META-INF/services/javax.persistence.spi.PersistenceProvider");

      if (url != null)
      {
        InputStream is = url.openStream();
        try
        {
          is = url.openStream();

          providers.addAll(
            providerNamesFromReader(new BufferedReader(new InputStreamReader(is))));
        }
        finally
        {
          is.close();
        }
      }
    }

    return providers;
  }

  private static Set providerNamesFromReader(BufferedReader reader)
      throws IOException
  {
    Set names = new HashSet();
    do
    {
      String line;
      if((line = reader.readLine()) == null)
          break;
      line = line.trim();
      Matcher m = Pattern.compile("^([^#]+)").matcher(line);
      if(m.find())
          names.add(m.group().trim());
    }
    while(true);
    return names;
  }

  private static boolean isBasicType(Class cls)
  {
    if (cls == byte.class) return true;
    if (cls == char.class) return true;
    if (cls == short.class) return true;
    if (cls == int.class) return true;
    if (cls == long.class) return true;
    if (cls == float.class) return true;
    if (cls == double.class) return true;
    if (Number.class.isAssignableFrom(cls)) return true;
    if (cls == String.class) return true;
    if (cls == byte[].class) return true;
    if (Enum.class.isAssignableFrom(cls)) return true;
    return false;
  }
  
  private static Map createPersistenceUnitPropertiesMap(String unitName)
  {
    HashMap map = new HashMap();
    String nonJtaDataSource = 
      MatrixConfig.getProperty(unitName + ".nonJtaDataSource");
    String jtaDataSource = 
      MatrixConfig.getProperty(unitName + ".jtaDataSource");      
    if (nonJtaDataSource != null)
      map.put("javax.persistence.nonJtaDataSource", nonJtaDataSource);
    else if (jtaDataSource != null)
      map.put("javax.persistence.jtaDataSource", jtaDataSource);
    
    return map;
  }

}
