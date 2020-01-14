package com.audifilm.matrix.dic.test;

import com.audifilm.matrix.util.TextUtil;
import java.util.List;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.Type;

import org.matrix.dic.TypeFilter;
import org.santfeliu.ws.WSPortFactory;


public class Test
{
  public Test()
  {
  }

  public static DictionaryManagerPort getPort(String service) throws Exception
  {
    return WSPortFactory.getPort(DictionaryManagerPort.class,
      "http://localhost/services/" + service + "?wsdl", "admin", "******");
  }

  public static void main(String[] args)
  {
    try
    {
      DictionaryManagerPort portAudi = getPort("dic_g5");
      String typeId = "A_ACTA";

     // Type type = portAudi.loadType(typeId);
     // System.out.println(TextUtil.toString(type));

      TypeFilter typeFilter = new TypeFilter();
      typeFilter.setSuperTypeId("SDE");
      typeFilter.setTypeId(null);  
      //typeFilter.setTypeId(typeId);
      typeFilter.setFirstResult(0);
      typeFilter.setMaxResults(1000);

      long count = portAudi.countTypes(typeFilter);
      System.out.println("TOTAL: " + count);

      List<Type> result = portAudi.findTypes(typeFilter);
      for(Type t : result) {
            System.out.println(TextUtil.toString(t));
      }

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

}
