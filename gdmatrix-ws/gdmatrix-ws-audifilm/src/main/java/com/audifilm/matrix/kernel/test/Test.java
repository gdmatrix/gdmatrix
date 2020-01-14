package com.audifilm.matrix.kernel.test;

import java.util.List;
import org.matrix.cases.CaseManagerPort;
import org.matrix.kernel.CityFilter;
import org.matrix.kernel.KernelManagerPort;
import org.santfeliu.ws.WSPortFactory;

/**
 *
 * @author comasfc
 */
public class Test
{
  public Test()
  {
  }

  public static CaseManagerPort getCaseManagerPort() throws Exception
  {
    return WSPortFactory.getPort(CaseManagerPort.class,
          "http://localhost/services/cases?wsdl");
  }

  public static KernelManagerPort getKernelManagerPort() throws Exception
  {
    return WSPortFactory.getPort(KernelManagerPort.class,
          "http://localhost/services/kernel?wsdl");
  }

  static public void main(String [] args) throws Exception {

    System.setProperty("matrix-config", "/matrix/conf");

    CityFilter cityFilter = new CityFilter();
    cityFilter.setCityName("BARCELONA");

    KernelManagerPort kernel = getKernelManagerPort();
    System.out.println(kernel.countCities(cityFilter));

    List result = kernel.findCities(cityFilter);

    System.out.println(result.toString());


  }

}
