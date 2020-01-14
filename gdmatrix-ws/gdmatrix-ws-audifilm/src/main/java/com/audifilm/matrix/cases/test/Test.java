package com.audifilm.matrix.cases.test;

import com.audifilm.matrix.cases.service.CaseManager;
import com.audifilm.matrix.cases.test.MockObjects.DBCase;
import com.audifilm.matrix.common.service.PKUtil;
import com.audifilm.matrix.util.TextUtil;
import java.util.List;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CaseManagerPort;
import org.matrix.dic.Property;
import org.matrix.dic.Type;
import org.matrix.util.Entity;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
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

  public static CaseManagerPort getPort(String service) throws Exception
  {
    return WSPortFactory.getPort(CaseManagerPort.class,
      "http://localhost/services/" + service + "?wsdl");
  }

  static public void main(String [] args) throws Exception {

    testExpressions(args);
 
  }

  static public void testWs(String [] args) throws Exception {
    System.setProperty("matrix-config", "/matrix/conf");


    WSDirectory directory = WSDirectory.getInstance();
    WSEndpoint endpoint = directory.getEndpoint("cases_g5");
    Entity caseEntity = endpoint.getInternalEntity(Case.class.getName());
    Entity typeEntity = endpoint.getInternalEntity(Type.class.getName());

    String sdenum = "X2010000001";

    CaseManagerPort audiCaseManager = getPort("cases");
    Case exp = audiCaseManager.loadCase(PKUtil.makeMatrixPK(caseEntity , sdenum));
    System.out.println(TextUtil.toString(exp));


    List<Property> listProperties = exp.getProperty();
    for(Property p : listProperties)
    {
      System.out.println(p.getName() + " " + p.getValue());
    }


    String tipusExpedient = "RUN2";
    CaseFilter caseFilter = new CaseFilter();
    caseFilter.setCaseTypeId(PKUtil.makeMatrixPK(typeEntity ,tipusExpedient));

    caseFilter.setDescription("");
    caseFilter.setDateComparator("0");

    List<Case> resultList = audiCaseManager.findCases(caseFilter);
    for(Case c: resultList) {
      System.out.println(c.getCaseId() + " - " + c.getCaseTypeId() + " - " + c.getDescription());
    }
  }

  static public void testExpressions(String [] args) {
    System.out.println(CaseManager.evalExpression("${value}; ${t.getDescription}; ${c.getCaseTypeNum}", new DBCase(), "EXEMPLE"));
  }

}
