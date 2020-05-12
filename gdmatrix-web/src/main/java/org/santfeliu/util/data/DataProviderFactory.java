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
package org.santfeliu.util.data;

import java.util.HashMap;
import org.santfeliu.util.Table;

/**
 *
 * @author realor
 */
public class DataProviderFactory
{
  private static DataProviderFactory instance;
  private HashMap<String, String> providerClasses =
    new HashMap<String, String>();

  public static DataProviderFactory getInstance()
  {
    if (instance == null)
    {
      instance = new DataProviderFactory();
      instance.registerProviderClass("sql", 
        "org.santfeliu.util.data.SQLDataProvider");
      instance.registerProviderClass("enumtype", 
        "org.santfeliu.util.data.EnumTypeDataProvider");
    }
    return instance;
  }

  public DataProvider createProvider(String dataref) throws Exception
  {
    int index = dataref.indexOf(":");
    if (index == -1) throw new Exception("Invalid reference");
    String name = dataref.substring(0, index);
    String reference = dataref.substring(index + 1);
    String className = providerClasses.get(name);
    if (className == null) return null;
    Class cls = Class.forName(className);
    DataProvider provider = (DataProvider)cls.newInstance();
    provider.init(reference);
    return provider;
  }

  public void registerProviderClass(String name, String providerClassName)
  {
    providerClasses.put(name, providerClassName);
  }

  public void unregisterProviderClass(String name)
  {
    providerClasses.remove(name);
  }

  public static void main(String[] args)
  {
    try
    {
      DataProviderFactory factory = DataProviderFactory.getInstance();
      DataProvider provider = factory.createProvider(
        //"sql:jdbc/xxxxxx:::select persnom, perscog1 from ncl_persona where persnom = {nom}"
        "enumtype:sf:Departament"
        );
      System.out.println("Parameters: " + provider.getParameters());
      HashMap context = new HashMap();
      context.put("nom", "RICARD");
      Table data = provider.getData(context);
      System.out.println(data);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
