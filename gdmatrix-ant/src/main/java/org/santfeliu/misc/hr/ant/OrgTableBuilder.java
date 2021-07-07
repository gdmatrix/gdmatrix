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
package org.santfeliu.misc.hr.ant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matrix.cases.Case;
import org.matrix.cases.CaseCaseFilter;
import org.matrix.cases.CaseCaseView;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.dic.Property;

/**
 *
 * @author unknown
 */
public class OrgTableBuilder
{
  private enum PersonType {INT_ESTR, INT_NO_ESTR, EXT, REGI, NONE};

  private final String rootCaseId; //Unit root caseId
  private final Connection conn;
  private final CaseManagerPort port;

  //<caseId, LlocTreball>
  private final Map<String, LlocTreball> llocTreballMap = new HashMap<>();

  //<caseId, Person>
  private final Map<String, Person> personMap = new HashMap<>();

  private final List<String> log = new ArrayList<>();

  public OrgTableBuilder(String rootCaseId, Connection conn, 
    CaseManagerPort port)
  {
    this.rootCaseId = rootCaseId;
    this.conn = conn;
    this.port = port;
  }

  public List<String> getLog()
  {
    return log;
  }

  public void execute() throws Exception
  {    
    buildLlocTreballMap();
    log(llocTreballMap.size() + " llocs de treball trobats");
    buildPersonMap();
    log(personMap.size() + " persones trobades");    
    clearTableInDB();
    log("Taula esborrada");
    insertTableInDB();
    log("Taula actualitzada");
  }

  private void buildLlocTreballMap() throws Exception
  {    
    buildLlocTreballMap(rootCaseId, new ArrayList());
  }
  
  public void buildLlocTreballMap(String unitatOrgCaseId, List<String> parentUnitList)
  {
    Case unitatOrgCase = port.loadCase(unitatOrgCaseId);
    if (unitatOrgCase != null)
    {
      String unitName = unitatOrgCase.getTitle();
      String unitType = getPropertyValue(unitatOrgCase.getProperty(), "tipusUnitat");         
      if ("A".equals(unitType)) unitType = "ÀREA";
      else if ("D".equals(unitType)) unitType = "DEPARTAMENT";
      else if ("U".equals(unitType)) unitType = "UNITAT";
      else if ("Z".equals(unitType)) unitType = "ALTRES";
      List<Case> llocTreballCaseList = getLlocTreballCaseList(unitatOrgCaseId);
      for (Case llocTreballCase : llocTreballCaseList)
      {
        llocTreballCase = port.loadCase(llocTreballCase.getCaseId());
        String capUnitat = getPropertyValue(llocTreballCase.getProperty(), "capUnitat");
        boolean unitBoss = Boolean.parseBoolean(capUnitat);
        LlocTreball llocTreball = new LlocTreball();
        llocTreball.setCaseId(llocTreballCase.getCaseId());
        llocTreball.setBoss(unitBoss);
        llocTreball.setUnitType(unitType);
        llocTreball.setUnitCaseId(unitatOrgCase.getCaseId());
        llocTreball.getUnitList().addAll(parentUnitList);
        llocTreball.getUnitList().add(unitName);
        llocTreballMap.put(llocTreball.getCaseId(), llocTreball);        
      }      
      List<Case> unitatInferiorCaseList = getUnitatInferiorCaseList(unitatOrgCaseId);
      for (Case unitatInferiorCase : unitatInferiorCaseList)
      {
        List<String> auxParentUnitList = new ArrayList(parentUnitList);
        auxParentUnitList.add(unitName);
        buildLlocTreballMap(unitatInferiorCase.getCaseId(), auxParentUnitList);
      }
    }
  }

  private void buildPersonMap() throws Exception
  {
    int count = putPersonsFromType("sf:TreballadorIntern");
    log(count + " treballadors interns creats");
    count = putPersonsFromType("sf:TreballadorExternResident");
    log(count + " treballadors externs creats");
    count = putPersonsFromType("sf:Regidor");
    log(count + " regidors creats");

    for (LlocTreball llocTreball : llocTreballMap.values())
    {
      CaseCaseFilter filter = new CaseCaseFilter();
      filter.setRelCaseId(llocTreball.getCaseId());
      List<CaseCaseView> caseCaseViewList = port.findCaseCaseViews(filter);
      for (CaseCaseView caseCaseView : caseCaseViewList)
      {
        if ("sf:PlacaLlocTreball".equals(caseCaseView.getCaseCaseTypeId()) &&
          isCurrent(caseCaseView))
        {
          Case placaCase = caseCaseView.getMainCase();
          CaseCaseFilter filter2 = new CaseCaseFilter();
          filter2.setRelCaseId(placaCase.getCaseId());
          List<CaseCaseView> caseCaseViewList2 =
            port.findCaseCaseViews(filter2);
          for (CaseCaseView caseCaseView2 : caseCaseViewList2)
          {
            if ("sf:TreballadorOcupacio".equals(
              caseCaseView2.getCaseCaseTypeId()) && isCurrent(caseCaseView2))
            {              
              Person person = getPersonFromCaseCase(caseCaseView2);
              if (person != null)
              {
                Placa placa = new Placa();
                placa.setCaseId(placaCase.getCaseId());
                person.setPlaca(placa);                
                if (person.getType().equals(PersonType.NONE))
                {
                  person.setType(PersonType.INT_ESTR);
                  person.setLlocTreball(llocTreball);
                  person.setBoss(llocTreball.isBoss());                  
                }
              }
            }
          }
        }
        else if ("sf:TrebIntLlocTreball".equals(
          caseCaseView.getCaseCaseTypeId()) && isCurrent(caseCaseView))
        {
          Person person = getPersonFromCaseCase(caseCaseView);
          if (person != null)
          {
            person.setType(PersonType.INT_NO_ESTR);
            person.setLlocTreball(llocTreball);            
            person.setBoss(llocTreball.isBoss());
            String modality = getPropertyValue(caseCaseView.getProperty(), 
              "modalitat");
            person.setModality(modality);
          }
        }
        else if ("sf:TrebExtLlocTreball".equals(
          caseCaseView.getCaseCaseTypeId()) && isCurrent(caseCaseView))
        {
          Person person = getPersonFromCaseCase(caseCaseView);
          if (person != null)
          {
            person.setType(PersonType.EXT);
            person.setLlocTreball(llocTreball);            
            person.setBoss(llocTreball.isBoss());
            person.setModality(null);            
          }
        }
      }
      filter = new CaseCaseFilter();
      filter.setCaseId(llocTreball.getCaseId());
      filter.setCaseCaseTypeId("sf:LlocTreballRegidor");
      caseCaseViewList = port.findCaseCaseViews(filter);
      for (CaseCaseView caseCaseView : caseCaseViewList)
      {
        if (isCurrent(caseCaseView))
        {
          Person person = getPersonFromCaseCase(caseCaseView, false);
          if (person != null)
          {
            person.setType(PersonType.REGI);
            person.setLlocTreball(llocTreball);
            person.setBoss(llocTreball.isBoss());
            person.setModality(null);            
          }
        }
      }
    }
    
    //Manage substitutes and politicians
    for (Person person : personMap.values())
    {
      if (person.getType() == PersonType.NONE)
      {
        //Politician??
        if (person.isPolitician())
        {
          person.setType(PersonType.REGI);
        }
        else
        {
          //Substitute??        
          CaseCaseFilter filter = new CaseCaseFilter();
          filter.setCaseId(person.getCaseId());
          List<CaseCaseView> caseCaseViewList = port.findCaseCaseViews(filter);
          for (CaseCaseView caseCaseView : caseCaseViewList)
          {
            if ("sf:TrebIntSubstitucio".equals(
              caseCaseView.getCaseCaseTypeId()) && isCurrent(caseCaseView))
            {
              String relCaseId = caseCaseView.getRelCase().getCaseId();
              Person relPerson = personMap.get(relCaseId);
              if (relPerson != null)
              {              
                person.setType(relPerson.getType());
                person.setLlocTreball(relPerson.getLlocTreball());
                person.setBoss(relPerson.isBoss());
                person.setModality(null);
                person.setSubstitutes(relPerson.getPersonFullName());
                relPerson.setSubstitutedBy(person.getPersonFullName());
              }
            }
          }
        }
      }
    }
  }
  
  private int putPersonsFromType(String type)
  {
    int result = 0;
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    String now = format.format(new Date());
    CaseFilter caseFilter = new CaseFilter();
    caseFilter.setCaseTypeId(type);
    caseFilter.setFromDate(now);
    caseFilter.setToDate(now);
    caseFilter.setDateComparator("3");
    List<Case> caseList = port.findCases(caseFilter);
    for (Case cas : caseList)
    {
      CasePersonFilter casePersonFilter = new CasePersonFilter();
      casePersonFilter.setCaseId(cas.getCaseId());
      List<CasePersonView> casePersonViewList =
        port.findCasePersonViews(casePersonFilter);
      for (CasePersonView casePersonView : casePersonViewList)
      {
        if (isCurrent(casePersonView))
        {
          Person person = new Person();
          person.setCaseId(cas.getCaseId());
          person.setType(PersonType.NONE);
          person.setPerscod(casePersonView.getPersonView().getPersonId());
          person.setPersonFullName(
            casePersonView.getPersonView().getFullName());
          person.setIdCard(casePersonView.getPersonView().getNif());
          person.setPolitician("sf:Regidor".equals(type));
          personMap.put(cas.getCaseId(), person);
          result++;
        }
      }
    }
    return result;
  }

  private Person getPersonFromCaseCase(CaseCaseView caseCaseView)
  {
    return getPersonFromCaseCase(caseCaseView, true);
  }

  private Person getPersonFromCaseCase(CaseCaseView caseCaseView, 
    boolean personInMainCase)
  {
    String treballadorCaseId = (personInMainCase ? 
      caseCaseView.getMainCase().getCaseId() : 
      caseCaseView.getRelCase().getCaseId());
    return personMap.get(treballadorCaseId);
  }  
  
  private boolean isCurrent(CaseCaseView caseCaseView)
  {
    return isCurrent(caseCaseView.getStartDate(), caseCaseView.getEndDate());
  }

  private boolean isCurrent(CasePersonView casePersonView)
  {
    return isCurrent(casePersonView.getStartDate(),
      casePersonView.getEndDate());
  }

  private boolean isCurrent(String startDate, String endDate)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    Date now = new Date();
    try
    {
      if (startDate == null && endDate == null)
      {
        return true;
      }
      else if (startDate == null && endDate != null)
      {
        return !now.after(format.parse(endDate));
      }
      else if (startDate != null && endDate == null)
      {
        return !now.before(format.parse(startDate));
      }
      else
      {
        return !now.before(format.parse(startDate)) &&
          !now.after(format.parse(endDate));
      }
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  private int clearTableInDB() throws Exception
  {
    Statement stmt = null;
    try
    {
      stmt = conn.createStatement();
      String sql = "delete from org_trebunitat";
      return stmt.executeUpdate(sql);
    }
    finally
    {
      if (stmt != null) stmt.close();
    }
  }

  private int insertTableInDB() throws Exception
  {
    int result = 0;
    PreparedStatement pStmt = null;
    try
    {
      String sql = "insert into org_trebunitat(perscod, unit1, unit2, unit3, "
        + "unit4, unit5, unit6, unit7, unit8, unittype, idcard, personfullname, workercaseid, persontype, "
        + "jobcaseid, unitboss, substitutes, substitutedby, placecaseid, unitcaseid, modality) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      pStmt = conn.prepareStatement(sql);
      for (Person person : personMap.values())
      {
        pStmt.setInt(1, Integer.parseInt(person.getPerscod()));
        if (person.getLlocTreball() == null)
        {
          pStmt.setString(2, null);
          pStmt.setString(3, null);
          pStmt.setString(4, null);
          pStmt.setString(5, null);
          pStmt.setString(6, null);
          pStmt.setString(7, null);
          pStmt.setString(8, null);
          pStmt.setString(9, null);          
          pStmt.setString(10, null);
          pStmt.setString(15, null);
          pStmt.setString(20, null);
        }
        else
        {
          pStmt.setString(2, person.getLlocTreball().getUnit(1));
          pStmt.setString(3, person.getLlocTreball().getUnit(2));
          pStmt.setString(4, person.getLlocTreball().getUnit(3));
          pStmt.setString(5, person.getLlocTreball().getUnit(4));
          pStmt.setString(6, person.getLlocTreball().getUnit(5));
          pStmt.setString(7, person.getLlocTreball().getUnit(6));
          pStmt.setString(8, person.getLlocTreball().getUnit(7));
          pStmt.setString(9, person.getLlocTreball().getUnit(8));          
          pStmt.setString(10, person.getLlocTreball().getUnitType());
          pStmt.setString(15, person.getLlocTreball().getCaseId().substring(3));
          pStmt.setString(20, person.getLlocTreball().getUnitCaseId().substring(3));
        }
        if (person.getPlaca() == null)
        {
          pStmt.setString(19, null);
        }
        else
        {
          pStmt.setString(19, person.getPlaca().getCaseId().substring(3));          
        }
        pStmt.setString(11, person.getIdCard());
        pStmt.setString(12, person.getPersonFullName());
        pStmt.setString(13, person.getCaseId().substring(3));
        pStmt.setString(14, person.getType().toString());
        pStmt.setString(16, person.isBoss() ? "Y" : "N");
        pStmt.setString(17, person.getSubstitutes());
        pStmt.setString(18, person.getSubstitutedBy());
        pStmt.setString(21, person.getModality());
        result += pStmt.executeUpdate();
      }
      return result;
    }
    finally
    {
      if (pStmt != null) pStmt.close();
    }
  }

  private List<Case> getLlocTreballCaseList(String unitatOrgCaseId)
  {
    List<Case> result = new ArrayList();
    CaseCaseFilter filter = new CaseCaseFilter();  
    filter.setRelCaseId(unitatOrgCaseId);
    filter.setCaseCaseTypeId("sf:LlocTreballUnitatOrg");
    List<CaseCaseView> ccvList = port.findCaseCaseViews(filter);      
    for (CaseCaseView ccv : ccvList)
    {
      if (isCurrent(ccv))
      {      
        result.add(ccv.getMainCase());
      }
    }
    return result;
  }

  private List<Case> getUnitatInferiorCaseList(String unitatOrgCaseId)
  {
    List<Case> result = new ArrayList();
    CaseCaseFilter filter = new CaseCaseFilter();  
    filter.setRelCaseId(unitatOrgCaseId);
    filter.setCaseCaseTypeId("sf:UnitatOrgSuperior");
    List<CaseCaseView> ccvList = port.findCaseCaseViews(filter);      
    for (CaseCaseView ccv : ccvList)
    {    
      if (isCurrent(ccv))
      {
        result.add(ccv.getMainCase());
      }
    }
    return result;
  }  
  
  private String getPropertyValue(List<Property> propertyList, String name)
  {
    for (Property property : propertyList)
    {      
      if (property.getName().equals(name))
      {
        if (!property.getValue().isEmpty()) return property.getValue().get(0);
      }
    }
    return null;
  }
  
  private void log(String text)
  {
    log.add(text);
    System.out.println(text);
  }   
  
  private static String blankNull(String text)
  {
    return (text != null ? text : "");
  }    
  
  class LlocTreball
  {
    private String caseId;
    private final List<String> unitList = new ArrayList();
    private String unitType;
    private String unitCaseId;
    private boolean boss;

    public String getCaseId()
    {
      return caseId;
    }

    public void setCaseId(String caseId)
    {
      this.caseId = caseId;
    }

    public List<String> getUnitList()
    {
      return unitList;
    }

    public String getUnit(int idx)
    {
      if ((idx < 1) || (idx > unitList.size()))
      {
        return null;
      }
      else
      {
        return unitList.get(idx - 1);
      }
    }
    
    public String getUnitType()
    {
      return unitType;
    }

    public void setUnitType(String unitType)
    {
      this.unitType = unitType;
    }

    public String getUnitCaseId() 
    {
      return unitCaseId;
    }

    public void setUnitCaseId(String unitCaseId) 
    {
      this.unitCaseId = unitCaseId;
    }

    public boolean isBoss()
    {
      return boss;
    }

    public void setBoss(boolean boss)
    {
      this.boss = boss;
    }    
  }

  class Placa
  {
    private String caseId;

    public String getCaseId()
    {
      return caseId;
    }

    public void setCaseId(String caseId)
    {
      this.caseId = caseId;
    }
  }  
  
  class Person
  {
    private String caseId;
    private PersonType type;
    private String perscod;
    private String idCard;
    private String personFullName;
    private LlocTreball llocTreball;
    private Placa placa;
    private boolean boss;
    private boolean politician;
    private String substitutes;
    private String substitutedBy;
    private String modality;

    public String getIdCard()
    {
      return idCard;
    }

    public void setIdCard(String idCard)
    {
      this.idCard = idCard;
    }

    public LlocTreball getLlocTreball()
    {
      return llocTreball;
    }

    public void setLlocTreball(LlocTreball llocTreball)
    {
      this.llocTreball = llocTreball;
    }

    public Placa getPlaca()
    {
      return placa;
    }

    public void setPlaca(Placa placa)
    {
      this.placa = placa;
    }

    public String getPerscod()
    {
      return perscod;
    }

    public void setPerscod(String perscod)
    {
      this.perscod = perscod;
    }

    public String getPersonFullName()
    {
      return personFullName;
    }

    public void setPersonFullName(String personFullName)
    {
      this.personFullName = personFullName;
    }

    public PersonType getType()
    {
      return type;
    }

    public void setType(PersonType type)
    {
      this.type = type;
    }

    public String getCaseId()
    {
      return caseId;
    }

    public void setCaseId(String caseId)
    {
      this.caseId = caseId;
    }

    public boolean isBoss()
    {
      return boss;
    }

    public void setBoss(boolean boss)
    {
      this.boss = boss;
    }

    public boolean isPolitician()
    {
      return politician;
    }

    public void setPolitician(boolean politician)
    {
      this.politician = politician;
    }

    public String getSubstitutes()
    {
      return substitutes;
    }

    public void setSubstitutes(String substitutes)
    {
      this.substitutes = substitutes;
    }

    public String getSubstitutedBy()
    {
      return substitutedBy;
    }

    public void setSubstitutedBy(String substitutedBy)
    {
      this.substitutedBy = substitutedBy;
    }

    public String getModality() 
    {
      return modality;
    }

    public void setModality(String modality) 
    {
      this.modality = modality;
    }
  }
  
}
