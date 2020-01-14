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
