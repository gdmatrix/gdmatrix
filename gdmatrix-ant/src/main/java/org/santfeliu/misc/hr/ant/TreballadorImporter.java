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

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.matrix.cases.Case;
import org.matrix.cases.CaseCase;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.Intervention;
import org.matrix.dic.Property;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ant.cases.Importer;
import org.santfeliu.util.PojoUtils;

/**
 *
 * @author lopezrj
 */
public class TreballadorImporter implements Importer
{  
  private CaseManagerPort casesPort = null;
  private Connection conn = null;

  private List<String> fullLog = new ArrayList<String>();
  private List<String> summaryLog = new ArrayList<String>();

  private List<String> tmpLog = new ArrayList<String>();

  private Map<String, String> dniSynonymMap = new HashMap<String, String>();
  private Set<String> processedDniSynonymSet = new HashSet<String>();
  
  private int newCaseCount = 0;
  private int updatedCaseCount = 0;
  private int newInterventionCount = 0;
  private int updatedInterventionCount = 0;
  private int newRelatedSDECaseCount = 0;

  public void execute(Connection conn, CaseManagerPort casesPort)
    throws Exception
  {
    this.conn = conn;
    this.casesPort = casesPort;

    newCaseCount = 0;
    newInterventionCount = 0;
    updatedInterventionCount = 0;
    newRelatedSDECaseCount = 0;

    log("Indexant les relacions laborals existents...");
    //Map <dni, intervencions>
    Map<String, List<IntervTreballador>> interventionMap = findInterventions();
    int intervSize = 0;
    for (List<IntervTreballador> list : interventionMap.values())
    {
      intervSize = intervSize + list.size();
    }
    log(intervSize + " relacions laborals trobades");

    log("Cercant treballadors a importar...");
    List<String> dniList = findDniList(); //De taules de nòmines
    log(dniList.size() + " treballadors trobats");

    for (String dni : dniList)
    {      
      if (!interventionMap.containsKey(dni))
      {
        interventionMap.put(dni, new ArrayList<IntervTreballador>());
      }
    }

//int iTest = 0;
    for (String dni : interventionMap.keySet())
    {
//iTest++;      
//if (iTest >= 1 && iTest <= 100) //Test
//{
      boolean update = false;
      logTmp("---------------------------------------------------------------");
      logTmp("Gestionant el treballador " + dni + "...");

      if (processedDniSynonymSet.contains(dni))
      {
        logTmp("Treballador ja gestionat mitjançant el sinònim " + dniSynonymMap.get(dni));
      }
      else
      {
        logTmp("Cercant les relacions laborals del treballador a taules de "
          + "nòmines...");
        List<PayslipRow> payslipRowList = findPayslipRows(dni);
        logTmp(payslipRowList.size() + " relacions laborals trobades");

        if (payslipRowList.isEmpty() && dniSynonymMap.containsKey(dni))
        {
          logTmp("Cercant a taules de nòmines pel dni sinònim " + dniSynonymMap.get(dni));
          payslipRowList = findPayslipRows(dniSynonymMap.get(dni));
          logTmp(payslipRowList.size() + " relacions laborals trobades");
        }

        if (payslipRowList.isEmpty())
        {
          logTmp("<b>Treballador no trobat a nòmines</b>");
          update = true;
        }
        else
        {        
          PayslipRow referencePayslipRow =
            payslipRowList.get(payslipRowList.size() - 1);

          logTmp("Treballador: " + referencePayslipRow.getFullName());
          logTmp("Cercant les intervencions del treballador a l'aplicació de RRHH...");        
          Case cTrebInt = null;
          Case cRegidor = null;

          List<IntervTreballador> interventions = interventionMap.get(dni);
          logTmp(interventions.size() + " intervencions trobades");

          if (interventions.isEmpty() && dniSynonymMap.containsKey(dni))
          {
            logTmp("Cercant intervencions pel dni sinònim " + dniSynonymMap.get(dni));
            interventions = interventionMap.get(dniSynonymMap.get(dni));
            logTmp(interventions.size() + " intervencions trobades");
          }

          boolean hasTreballadorInternPayslip = hasTreballadorInternPayslip(payslipRowList);
          boolean hasRegidorPayslip = hasRegidorPayslip(payslipRowList);
          
          if (hasTreballadorInternPayslip)
          {
            cTrebInt = loadTreballadorCase(true, referencePayslipRow);
            if (cTrebInt == null) //not found
            {
              cTrebInt = createTreballadorCase(true, payslipRowList, referencePayslipRow);
              newCaseCount++;
              update = true;                                        
            }
            else //found
            {
              if (updateEndDate(cTrebInt, payslipRowList))
              {
                updatedCaseCount++;
                update = true;
              }
            }
          }
          
          if (hasRegidorPayslip)
          {
            cRegidor = loadTreballadorCase(false, referencePayslipRow);
            if (cRegidor == null) //not found
            {
              cRegidor = createTreballadorCase(false, payslipRowList, referencePayslipRow);
              newCaseCount++;
              update = true;              
            }
            else //found
            {
              if (updateEndDate(cRegidor, payslipRowList))
              {
                updatedCaseCount++;
                update = true;
              }              
            }            
          }          

          for (PayslipRow row : payslipRowList)
          {
            String searchCaseType = 
              (isRegidorPayslipRow(row) ? "Regidor" : "TreballadorIntern");
            String payslipId = row.getPayslipId();
            logTmp("Relació laboral de número " + payslipId +              
              " Inici: " + row.getStartDate() + " Final: " + row.getEndDate());
            IntervTreballador auxIntervTreballador = null;
            boolean found = false;
            for (int i = 0; i < interventions.size() && !found; i++)
            {
              IntervTreballador intervTreballador = interventions.get(i);
              if (intervTreballador.getCaseTypeId().equals(searchCaseType) &&
                (payslipId.equals(intervTreballador.getPayslipId()))) 
              {
                auxIntervTreballador = intervTreballador;
                found = true;
              }
            }
            if (!found) //non existing intervention -> create
            {
              logTmp("<b>La relació laboral no existeix -> Nova intervenció</b>");
              if (isRegidorPayslipRow(row))
              {
                createIntervention(cRegidor.getCaseId(), row);                
              }
              else //treballador intern
              {
                createIntervention(cTrebInt.getCaseId(), row);                
              }
              newInterventionCount++;
              update = true;
            }
            else //existing intervention -> update
            {
              logTmp("La relació laboral existeix");
              if (updateIntervention(auxIntervTreballador, row))
              {
                updatedInterventionCount++;
                update = true;
              }
            }
          }

          String personId = referencePayslipRow.getPersonId();

          if (personId == null) //Search in synonym payslips
          {
            logTmp("Perscod no trobat a taula de nòmines. Cercant per sinònim");
            if (dniSynonymMap.containsKey(referencePayslipRow.getDni()))
            {
              String dniSynonym = dniSynonymMap.get(referencePayslipRow.getDni());
              List<PayslipRow> payslips2 = findPayslipRows(dniSynonym);
              if (!payslips2.isEmpty())
              {
                logTmp("Sinònim trobat a nòmines");
                PayslipRow referencePayslipRow2 = payslips2.get(payslips2.size() - 1);                
                personId = referencePayslipRow2.getPersonId();
              }
              else //Search in kernel
              {
                logTmp("Sinònim no trobat a nòmines. Cercant al kernel...");
                personId = findPerscod(dniSynonym);
                if (personId != null) logTmp("Perscod trobat al kernel pel dni " + dniSynonym + ": " + personId);
              }
            }              
          }            

          if (personId != null)
          {
            //TODO Quizás repartir por fecha
            if (cTrebInt != null)
            {
              logTmp("Cercant expedients relacionats de SDE encara no annexats...");
              Map<String, String> relatedSDECases = 
                findRelatedSDECases(cTrebInt.getCaseId(), personId);
              logTmp(relatedSDECases.size() + " nous expedients de SDE trobats");
              if (!relatedSDECases.isEmpty())
              {
                storeSDECases(cTrebInt, relatedSDECases);
                update = true;
              }              
            }
            if (cRegidor != null)
            {
              logTmp("Cercant expedients relacionats de SDE encara no annexats...");
              Map<String, String> relatedSDECases = 
                findRelatedSDECases(cRegidor.getCaseId(), personId);
              logTmp(relatedSDECases.size() + " nous expedients de SDE trobats");
              if (!relatedSDECases.isEmpty())
              {
                storeSDECases(cRegidor, relatedSDECases);
                update = true;
              }              
            }            
          }
          else
          {
            logTmp("WARNING: DNI sense persona al nucli: dni " + dni + 
              (dniSynonymMap.containsKey(dni) ? " (sinònim " + dniSynonymMap.get(dni) + ")" : "") +
              " -> No podem associar expedients SDE");
          }
        }
        flushTmpLog(update);

        if (dniSynonymMap.containsKey(dni))
        {
          processedDniSynonymSet.add(dni);
          processedDniSynonymSet.add(dniSynonymMap.get(dni));
        }
      }
//} //end if test      
    }
    log("-----------------------------------------------------------------");
    
    log("Afegint propietats 'usuari' a expedients...", false);
    int count = updateUsuariProperties();
    log((count > 0 ? "<b>" : "") + count + " propietats 'usuari' afegides" +
      (count > 0 ? "</b>" : ""), false);
    log("", false);

    log("Comprovant consistència de les relacions de substitució entre "
      + "treballadors...", false);
    List<String> substWarningList = checkSubstitutions();
    if (substWarningList.isEmpty())
    {
      log("Sense inconsistències a les substitucions entre treballadors",
        false);
    }
    else
    {
      for (String substWarning : substWarningList)
      {
        log(substWarning, false);
      }
    }
    log("", false);
    
    log("***** RESULTAT DE LA MIGRACIÓ *****");
    log(newCaseCount + " expedients de treballador nous");
    log(updatedCaseCount + " expedients de treballador actualitzats");
    log(newInterventionCount + " relacions laborals noves");
    log(updatedInterventionCount + " relacions laborals actualitzades");
    log(newRelatedSDECaseCount + " expedients SDE relacionats nous");
  }

  public List<String> getFullLog()
  {
    return fullLog;
  }

  public List<String> getSummaryLog()
  {
    return summaryLog;
  }

  private String getMinDate(List<PayslipRow> rowList, boolean treballadorIntern)
  {
    String minDate = "99999999";
    List<PayslipRow> auxRowList = new ArrayList();
    for (PayslipRow row : rowList)
    {
      if (
          (treballadorIntern && isTreballadorInternPayslipRow(row)) || 
          (!treballadorIntern && isRegidorPayslipRow(row))
        )
      {
        auxRowList.add(row);
      }
    }
    for (PayslipRow row : auxRowList)
    {
      if (row.getStartDate() != null && row.getStartDate().compareTo(minDate) < 0)
      {
        minDate = row.getStartDate();
      }
    }
    return minDate;
  }

  private String getMaxDate(List<PayslipRow> rowList, boolean treballadorIntern)
  {
    String maxDate = "00000000";
    List<PayslipRow> auxRowList = new ArrayList();
    for (PayslipRow row : rowList)
    {
      if (
          (treballadorIntern && isTreballadorInternPayslipRow(row)) || 
          (!treballadorIntern && isRegidorPayslipRow(row))
        )
      {
        auxRowList.add(row);
      }
    }
    for (PayslipRow row : auxRowList)
    {    
      if (row.getEndDate() == null)
      {
        return null;
      }
      else if (row.getEndDate().compareTo(maxDate) > 0)
      {
        maxDate = row.getEndDate();
      }
    }
    return maxDate;
  }

  private Map<String, List<IntervTreballador>> findInterventions()
    throws Exception
  {
    Map<String, List<IntervTreballador>> result =
      new HashMap<String, List<IntervTreballador>>();

    String sql = "select c.caseid, ci.interventionid, p.NIFNUM||p.NIFDC as " +
      "dni_cp, cipr.propvalue as idnomina, cpr.propvalue as dni_prop, c.type as casetype " +
      "from CAS_CASE c full outer join CAS_INTERVENTION ci on (c.caseid = " +
      "ci.caseid and ci.type in ('TrebIntEstrIntervention', " +
      "'TrebIntNoEstrIntervention', 'TrebIntNouIntervention', 'RegidorRegidorIntervention')) " +
      "left outer join CAS_PERSON cp on (c.caseid = cp.caseid) " +
      "left outer join GENESYS5.NCL_PERSONA p on (cp.perscod = p.perscod) " +
      "left outer join CAS_CASEPROP cpr on (cpr.caseid = c.caseid and cpr.propname = 'dni' and cpr.indx = 0) " +
      "left outer join CAS_INTERVENTIONPROP cipr on (ci.interventionid = " +
      "cipr.interventionid and cipr.propname = 'id_nomina') " +
      "where c.type in ('TreballadorIntern', " +
      "'Regidor', " +
      "'TreballadorExternResident')";

    Statement stmt = null;
    try
    {
      stmt = conn.createStatement();
      ResultSet rs = null;
      try
      {
        rs = stmt.executeQuery(sql);
        while (rs.next())
        {
          String caseId = rs.getString("caseid");
          String caseTypeId = rs.getString("casetype");          
          String interventionId = rs.getString("interventionid");
          String dniCasePerson = rs.getString("dni_cp");
          String payslipId = rs.getString("idnomina");
          String dniProperty = rs.getString("dni_prop");
          if (dniProperty != null && dniCasePerson != null && !dniProperty.trim().equals(dniCasePerson.trim()))
          {
            dniSynonymMap.put(dniCasePerson, dniProperty);
            dniSynonymMap.put(dniProperty, dniCasePerson);
          }
          String dni;
          if (dniCasePerson != null) dni = dniCasePerson;
          else dni = dniProperty;
          if (dni != null)
          {
            IntervTreballador interv = new IntervTreballador();
            interv.setCaseId(caseId);
            interv.setCaseTypeId(caseTypeId);
            interv.setInterventionId(interventionId);
            interv.setPayslipId(payslipId);
            if (!result.containsKey(dni))
            {
              result.put(dni, new ArrayList<IntervTreballador>());
            }
            result.get(dni).add(interv);
          }
        }
      }
      finally
      {
        if (rs != null) rs.close();
      }
    }
    finally
    {
      if (stmt != null) stmt.close();
    }
    return result;
  }

  private List<String> findDniList() throws Exception
  {
    List<String> result = new ArrayList<String>();

    String sql = "SELECT distinct replace(ex.dni,' ','') as nif " +
      "FROM GENESYS.NOBD_EMPLE ex " +
      "WHERE ex.dni <> '00000000T' AND replace(ex.dni,' ','') is not null";
    Statement stmt = null;
    try
    {
      stmt = conn.createStatement();
      ResultSet rs = null;
      try
      {
        rs = stmt.executeQuery(sql);
        while (rs.next())
        {
          result.add(rs.getString("nif"));
        }
      }
      finally
      {
        if (rs != null) rs.close();
      }
    }
    finally
    {
      if (stmt != null) stmt.close();
    }
    return result;
  }

  private List<PayslipRow> findPayslipRows(String dni) throws Exception
  {
    List<PayslipRow> result = new ArrayList<PayslipRow>();

    String sql = "SELECT replace(e.dni,' ','') as dni, e.falt, e.fbaj, " +
      "e.tipo_tra, e.numero, e.lloc_tra, e.nss, e.hijos_1, e.dire_sg, " +
      "e.dire, e.dire_num, e.dire_piso, e.cp, e.pobl, e.nom_nom, e.nom_cog1, " +
      "e.nom_cog2, p.perscod, e.cat, e.id_emple " +
      "FROM GENESYS.NOBD_EMPLE e, " +
      "(select min(perscod) as perscod, NIFNUM||NIFDC as nif from " +
      "GENESYS5.NCL_PERSONA group by NIFNUM||NIFDC) p " +
      "WHERE replace(e.dni,' ','') = ? AND replace(e.dni,' ','') = p.NIF(+) " +
      "ORDER BY e.falt";

    PreparedStatement pStmt = null;
    try
    {
      pStmt = conn.prepareStatement(sql);
      pStmt.setString(1, dni);
      ResultSet rs = null;
      try
      {
        rs = pStmt.executeQuery();
        while (rs.next())
        {
          PayslipRow row = new PayslipRow();
          row.setPayslipId(rs.getString("id_emple"));
          row.setDni(rs.getString("dni"));
          row.setStartDate(rs.getString("falt"));
          row.setEndDate(rs.getString("fbaj"));
          row.setJobType(rs.getString("tipo_tra"));
          row.setPost(rs.getString("lloc_tra"));
          row.setSocSecNumber(rs.getString("nss"));
          row.setChildren(rs.getString("hijos_1"));
          row.setAddress(getFullAddress(rs.getString("dire_sg"),
            rs.getString("dire"), rs.getString("dire_num"),
            rs.getString("dire_piso")));
          row.setPostcode(rs.getString("cp").trim());
          row.setCity(rs.getString("pobl"));
          String fullName = getFullName(rs.getString("nom_nom"),
            rs.getString("nom_cog1"), rs.getString("nom_cog2"));
          row.setFullName(fullName);
          row.setPersonId(rs.getString("perscod"));
          row.setCategory(rs.getString("cat"));
          result.add(row);
        }
      }
      finally
      {
        if (rs != null) rs.close();
      }
    }
    finally
    {
      if (pStmt != null) pStmt.close();
    }
    return result;
  }

  private String findPerscod(String dni) throws Exception
  {
    String result = null;

    String sql = "select p.perscod from " +
      "(select min(perscod) as perscod, NIFNUM||NIFDC as nif from " +
      "GENESYS5.NCL_PERSONA group by NIFNUM||NIFDC) p " +
      "where p.nif = ?";
    
    PreparedStatement pStmt = null;
    try
    {
      pStmt = conn.prepareStatement(sql);
      pStmt.setString(1, dni);
      ResultSet rs = null;
      try
      {
        rs = pStmt.executeQuery();
        if (rs.next())
        {
          result = rs.getString("perscod");
        }
      }
      finally
      {
        if (rs != null) rs.close();
      }
    }
    finally
    {
      if (pStmt != null) pStmt.close();
    }
    return result;
  }  
  
  private String getFullAddress(String type, String name, String num,
    String floor)
  {
    if (name == null || name.trim().isEmpty()) return null;

    String auxType = "";
    String auxName = "";
    String auxNum = "";
    String auxFloor = "";
    if (type != null && !type.trim().isEmpty()) auxType = type.trim() + " ";
    auxName = name.trim() + " ";
    if (num != null && !num.trim().isEmpty()) auxNum = num.trim() + " ";
    if (floor != null && !floor.trim().isEmpty()) auxFloor = floor.trim();
    return auxType + auxName + auxNum + auxFloor;
  }

  private Map<String, String> findRelatedSDECases(String caseId, String perscod)
    throws Exception
  {
    Map<String, String> result = new HashMap<String, String>();

    String sql = "SELECT e.sdenum, e.sdetext " +
      "FROM GENESYS5.SDE_EXPEDIENT e " +
      "WHERE e.perscod = ? AND e.texpcod IN (select texpcod from " +
      "genesys5.wkf_expedient where depcod = '4200') " +
      "AND NOT EXISTS (select 1 from cas_casecase c where 'g5:'||e.sdenum = " +
      "c.relcaseid and c.caseId = ?)";

    PreparedStatement pStmt = null;
    try
    {
      pStmt = conn.prepareStatement(sql);
      pStmt.setString(1, perscod);
      caseId = caseId.contains("sf:") ? caseId.split("sf:")[1] : caseId;
      pStmt.setString(2, caseId);
      ResultSet rs = null;
      try
      {
        rs = pStmt.executeQuery();
        while (rs.next())
        {
          String relCaseId = "g5:" + rs.getString("sdenum");
          String description = rs.getString("sdetext") == null ? "" :
            rs.getString("sdetext");
          result.put(relCaseId, description);
        }
      }
      finally
      {
        if (rs != null) rs.close();
      }
    }
    finally
    {
      if (pStmt != null) pStmt.close();
    }
    return result;
  }

  private List<String> checkSubstitutions() throws Exception
  {
    List<String> result = new ArrayList<String>();
    String sql =
      "select c1.caseid as caseid1, c1.title, c2.caseid as caseid2, c2.title," +
      "  (case when nvl(cc1.startdate, '00000000') > nvl(cc2.startdate, '00000000') then cc1.startdate else cc2.startdate end) as solap1," +
      "  (case when nvl(cc1.enddate, '99999999') < nvl(cc2.enddate, '99999999') then cc1.enddate else cc2.enddate end) as solap2" +
      " from cas_case c1, cas_case c2, cas_casecase cc1, cas_casecase cc2" +
      " where cc1.caseid = c1.caseid" +
      "  and cc2.caseid = c2.caseid" +
      "  and cc1.caseid <> cc2.caseid" +
      "  and cc1.relcaseid = cc2.relcaseid" +
      "  and cc1.casecasetypeid = 'TreballadorOcupacio'" +
      "  and cc2.casecasetypeid = 'TreballadorOcupacio'" +
      "  and nvl(cc1.startdate, '00000000') < nvl(cc2.enddate, '99999999')" + 
      "  and nvl(cc2.startdate, '00000000') < nvl(cc1.enddate, '99999999')" + 
      "  and nvl(cc1.startdate, '00000000') >= nvl(cc2.startdate, '00000000')" +
      "  and not exists" +
      "  (" +
      "    select *" +
      "    from cas_casecase cc" +
      "    where cc.casecasetypeid = 'TrebIntSubstitucio'" +
      "    and cc.caseid = c1.caseid" +
      "    and cc.relcaseid = 'sf:' || c2.caseid" +
      "    and nvl(cc.startdate, '00000000') = (case when nvl(cc1.startdate, '00000000') > nvl(cc2.startdate, '00000000') then nvl(cc1.startdate, '00000000') else nvl(cc2.startdate, '00000000') end)" +
      "    and nvl(cc.enddate, '99999999') = (case when nvl(cc1.enddate, '99999999') < nvl(cc2.enddate, '99999999') then nvl(cc1.enddate, '99999999') else nvl(cc2.enddate, '99999999') end)" +
      "  )";
    Statement stmt = null;
    try
    {
      stmt = conn.createStatement();
      ResultSet rs = null;
      try
      {
        rs = stmt.executeQuery(sql);
        while (rs.next())
        {
          String caseId1 = rs.getString(1);
          String title1 = rs.getString(2);
          String caseId2 = rs.getString(3);
          String title2 = rs.getString(4);
          String solap1 = rs.getString(5);
          String solap2 = rs.getString(6);
          result.add("Falta relació de substitució entre " + caseId1 + " (" +
            title1 + ") i " + caseId2 + " (" + title2 + ") entre les dates " +
            solap1 + " i " + solap2);
        }
      }
      finally
      {
        if (rs != null) rs.close();
      }
    }
    finally
    {
      if (stmt != null) stmt.close();
    }
    return result;
  }

  private int updateUsuariProperties() throws Exception
  {
    String sql = "INSERT INTO CAS_CASEPROP (CASEID, PROPNAME, PROPVALUE) " +
      "SELECT c.caseid, 'usuari', ou.usrcod " +
      "FROM ORG_USUARI ou, CAS_PERSON p, CAS_CASE c, " +
      "  (select perscod, max(u.stddmod||u.stdhmod) stdmod from ORG_USUARI u " +
      "group by perscod) u " +
      "WHERE ou.perscod = p.perscod and c.caseid = p.caseid " +
      "  and u.perscod = ou.perscod and u.stdmod = (ou.stddmod||ou.stdhmod) " +
      "  and c.caseid NOT IN " +
      "  (select caseid from cas_caseprop where propname = 'usuari' and " +
      "usrcod = ou.usrcod) " +
      "  and c.type in ('TreballadorIntern', " +
      "  'Regidor', " +
      "  'TreballadorExternResident')";
    return runUpdate(sql);
  }

  private Intervention createIntervention(String caseId, PayslipRow row)
    throws Exception
  {
    Intervention i = getIntervention(caseId, row);
    i = casesPort.storeIntervention(i);
    logTmp("<b>Nova relació laboral creada: " + i.getIntId() + " (caseId " +
      i.getCaseId() + ")</b>");
    return i;
  }

  private boolean updateIntervention(IntervTreballador interv,
    PayslipRow row) throws Exception
  {    
    Intervention i = casesPort.loadIntervention(interv.getInterventionId());
    //TODO Actualizar más campos, además de las fechas
    if (mustUpdateIntervention(i, row))
    {
      i.setStartDate(row.getStartDate());
      i.setStartTime("000000");
      if (row.getEndDate() != null)
      {
        i.setEndDate(row.getEndDate());
        i.setEndTime("000000");
      }
      else
      {
        i.setEndDate(null);
        i.setEndTime(null);
      }
      casesPort.storeIntervention(i);
      logTmp("<b>Relació laboral actualitzada</b>");
      return true;
    }
    else
    {
      logTmp("Relació laboral sense canvis");
      return false;
    }
  }

  private boolean mustUpdateIntervention(Intervention i, PayslipRow row)
  {
    if (!i.getStartDate().equals(row.getStartDate()))
    {
      return true;
    }
    else
    {
      String iEndDate = i.getEndDate() == null ? "99999999" : i.getEndDate();
      String rowEndDate = row.getEndDate() == null ? "99999999" :
        row.getEndDate();
      if (!iEndDate.equals(rowEndDate))
      {
        return true;
      }
    }
    return false;
  }

  private Intervention getIntervention(String caseId, PayslipRow row) throws Exception
  {
    //TODO Regidor fields
    Intervention i = new Intervention();
    i.setCaseId(caseId);
    i.setStartDate(row.getStartDate());
    i.setStartTime("000000");
    if (row.getEndDate() != null)
    {
      i.setEndDate(row.getEndDate());
      i.setEndTime("000000");
    }    
    String description = row.getPayslipId() + ": " + row.getPost();
    i.setComments(description);
    if (isTreballadorInternPayslipRow(row))
    {
      i.setIntTypeId("sf:TrebIntNouIntervention");      
    }
    else
    {
      i.setIntTypeId("sf:RegidorRegidorIntervention");
    }
    PojoUtils.setDynamicProperty(i.getProperty(), "id_nomina",
      row.getPayslipId(), Property.class);
    PojoUtils.setDynamicProperty(i.getProperty(), "tipo_tra",
      row.getJobType(), Property.class);
    return i;
  }

  private void storeSDECases(Case c, Map<String, String> relCaseIdMap)
  {
    for (String relCaseId : relCaseIdMap.keySet())
    {
      CaseCase cc = new CaseCase();
      cc.setCaseId(c.getCaseId());
      cc.setRelCaseId(relCaseId);
      cc.setCaseCaseTypeId("sf:TreballadorBPMCaseCase");
      casesPort.storeCaseCase(cc);
      logTmp("<b>Nou expedient de SDE relacionat: caseId " + c.getCaseId() +
        " relCaseId " + relCaseId + " (" + relCaseIdMap.get(relCaseId) +
        ")</b>");
      newRelatedSDECaseCount++;
    }
  }

  private void storeCasePerson(Case c, String perscod)
  {
    if (perscod != null && !perscod.isEmpty())
    {
      String caseId = c.getCaseId();
      CasePersonFilter filter = new CasePersonFilter();
      filter.setCaseId(caseId);
      List cps = casesPort.findCasePersonViews(filter);
      if (cps == null  || cps.isEmpty())
      {
        CasePerson casePerson = new CasePerson();
        casePerson.setCaseId(caseId);
        casePerson.setPersonId(perscod);
        casePerson.setCasePersonTypeId("sf:TreballadorCasePerson");
        casePerson = casesPort.storeCasePerson(casePerson);
        logTmp("<b>Nou casePerson: caseId " + caseId + " perscod " + perscod +
          "</b>");
      }
    }
    else
    {
      logTmp("Expedient sense caseperson, ja que perscod = null");
    }
  }

  private boolean updateEndDate(Case c, List<PayslipRow> payslipRowList)
  {
    boolean isTreballadorIntern = "sf:TreballadorIntern".equals(c.getCaseTypeId());
    String maxDate = getMaxDate(payslipRowList, isTreballadorIntern);
    String payslipMaxDate = (maxDate == null ? "99999999" : maxDate);
    String caseEndDate = (c.getEndDate() == null ? "99999999" : c.getEndDate());
    if (!payslipMaxDate.equals(caseEndDate))
    {
      c.setEndDate(maxDate);
      c.setEndTime("000000");
      casesPort.storeCase(c);
      logTmp("<b>Data final de l'expedient actualitzada des de taula de "
        + "nòmines</b>");
      return true;
    }
    return false;
  }

  private void log(String s)
  {
    log(s, true);
  }

  private void log(String s, boolean toSummary)
  {
    fullLog.add(s);
    if (toSummary)
    {
      summaryLog.add(s);
    }
    System.out.println(s);
  }

  private void logTmp(String s)
  {
    tmpLog.add(s);
  }

  private void flushTmpLog(boolean update)
  {
    if (update) //to full log and summary log
    {
      for (String s : tmpLog) log(s);
    }
    else //to full log
    {
      for (String s : tmpLog) log(s, false);
    }
    tmpLog.clear();
  }

  private String getFullName(String name, String surname1, String surname2)
  {
    StringBuilder sb = new StringBuilder();
    if (name != null)
    {
      sb.append(name.trim());
      if (surname1 != null)
      {
        sb.append(" ");
        sb.append(surname1.trim());
        if (surname2 != null)
        {
          sb.append(" ");
          sb.append(surname2.trim());
        }
      }
    }
    return sb.toString();
  }

  private int runUpdate(String sql) throws Exception
  {
    Statement stmt = null;
    try
    {
      stmt = conn.createStatement();
      return stmt.executeUpdate(sql);
    }
    finally
    {
      if (stmt != null) stmt.close();
    }
  }
  
  private Case createTreballadorCase(boolean isTreballadorIntern, 
    List<PayslipRow> payslipRowList, PayslipRow referencePayslipRow) throws Exception
  {
    Case c = new Case();
    String startDate = getMinDate(payslipRowList, isTreballadorIntern);
    c.setStartDate(startDate);    
    c.setStartTime("000000");
    String endDate = getMaxDate(payslipRowList, isTreballadorIntern);
    if (endDate != null)
    {
      c.setEndDate(endDate);
      c.setEndTime("000000");
    }
    c.setTitle(referencePayslipRow.getFullName());
    c.setDescription("Expedient del " + (isTreballadorIntern ? "treballador intern " : "regidor ") + 
      referencePayslipRow.getFullName());

    String caseTypeId = (isTreballadorIntern ? "sf:TreballadorIntern" : "sf:Regidor");
    c.setCaseTypeId(caseTypeId);
    
    PojoUtils.setDynamicProperty(c.getProperty(), "dni",
      referencePayslipRow.getDni(), Property.class);
    PojoUtils.setDynamicProperty(c.getProperty(), "adreca",
      referencePayslipRow.getAddress(), Property.class);
    PojoUtils.setDynamicProperty(c.getProperty(), "cp",
      referencePayslipRow.getPostcode(), Property.class);
    PojoUtils.setDynamicProperty(c.getProperty(), "poblacio",
      referencePayslipRow.getCity(), Property.class);
    PojoUtils.setDynamicProperty(c.getProperty(), "num_ss",
      referencePayslipRow.getSocSecNumber(), Property.class);
    PojoUtils.setDynamicProperty(c.getProperty(), "fills",
      referencePayslipRow.getChildren(), Property.class);
        
    c = casesPort.storeCase(c);
    logTmp("<b>Nou expedient creat: " + c.getCaseId() + "</b>");
    storeCasePerson(c, referencePayslipRow.getPersonId());
    return c;
  }    
  
  private boolean isTreballadorInternPayslipRow(PayslipRow row)
  {
    return !isRegidorPayslipRow(row);
  }
  
  private boolean isRegidorPayslipRow(PayslipRow row)
  {
    String cat = row.getCategory();    
    if (cat == null) return false;    
    String trimCat = cat.trim();
    return (
      trimCat.startsWith("02") ||
      trimCat.startsWith("03") ||
      trimCat.startsWith("04") ||
      trimCat.endsWith("REGI") ||
      trimCat.endsWith("PORT") ||
      trimCat.endsWith("POLI")      
      );
  }
  
  private boolean hasRegidorPayslip(List<PayslipRow> payslipRowList)
  {
    if (payslipRowList != null)
    {
      for (PayslipRow row : payslipRowList)
      {
        if (isRegidorPayslipRow(row)) return true;
      }      
    }    
    return false;
  }
  
  private boolean hasTreballadorInternPayslip(List<PayslipRow> payslipRowList)
  {
    if (payslipRowList != null)
    {
      for (PayslipRow row : payslipRowList)
      {
        if (isTreballadorInternPayslipRow(row)) return true;
      }      
    }    
    return false;    
  }

  private Case loadTreballadorCase(boolean treballadorIntern, PayslipRow referencePayslipRow) throws Exception
  {
    String caseId = null;
    String caseTypeId = (treballadorIntern ? "TreballadorIntern" : "Regidor");
    String perscod = referencePayslipRow.getPersonId();
    String dni = referencePayslipRow.getDni();
    String sql =
      "select distinct c.caseid " +
      "from cas_case c " +
      "left outer join cas_person cp on (c.caseid = cp.caseid) " +
      "left outer join cas_caseprop cpr on (c.caseid = cpr.caseid and cpr.propname = 'dni') " +
      "where c.type = ? " +
      "and (cp.perscod = ? or cpr.propvalue = ?) " +
      "order by c.caseid";

    PreparedStatement pStmt = null;
    try
    {
      pStmt = conn.prepareStatement(sql);
      pStmt.setString(1, caseTypeId);
      pStmt.setString(2, perscod);
      pStmt.setString(3, dni);
      ResultSet rs = null;
      try
      {
        rs = pStmt.executeQuery();
        if (rs.next())
        {
          caseId = rs.getString(1);
        }
      }
      finally
      {
        if (rs != null) rs.close();
      }
    }
    finally
    {
      if (pStmt != null) pStmt.close();
    }
    return (caseId != null ? casesPort.loadCase(caseId) : null);
  }

  public static void main (String args[]) throws Exception
  {        
    Connection dbConn = null;
    try
    {
      //DB Connection
      Class.forName("oracle.jdbc.driver.OracleDriver");
      dbConn = DriverManager.getConnection("jdbc:oracle:thin:@*****:*****:*****", "*****", "*****");
      
      //Port
      WSDirectory wsDirectory = WSDirectory.getInstance(new URL("http://www.santfeliu.cat/wsdirectory"));
      WSEndpoint endpoint = wsDirectory.getEndpoint(CaseManagerService.class);
      CaseManagerPort port = endpoint.getPort(CaseManagerPort.class, "*****", "*****");

      //Run
      TreballadorImporter timp = new TreballadorImporter();
      timp.execute(dbConn, port);      
    }
    catch (Exception ex)
    {
      dbConn.rollback();
      throw ex;
    }
    finally
    {
      dbConn.close();
    }    
  }
  
  class IntervTreballador
  {
    private String caseId;
    private String caseTypeId;
    private String interventionId;
    private String payslipId;

    public String getCaseId()
    {
      return caseId;
    }

    public void setCaseId(String caseId)
    {
      this.caseId = caseId;
    }

    public String getCaseTypeId()
    {
      return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId)
    {
      this.caseTypeId = caseTypeId;
    }

    public String getInterventionId()
    {
      return interventionId;
    }

    public void setInterventionId(String interventionId)
    {
      this.interventionId = interventionId;
    }

    public String getPayslipId()
    {
      return payslipId;
    }

    public void setPayslipId(String payslipId)
    {
      this.payslipId = payslipId;
    }
  }

  class PayslipRow //RelacioLaboral
  {
    //Relació laboral
    private String payslipId; //idNomina
    private String startDate; //falt
    private String endDate; //fbaj
    private String post; //llocTra
    private String jobType; //tipoTra
    private String category; //cat
    
    //Expedient
    private String dni; //dni
    private String socSecNumber; //nss    
    private String children; //fills1
    private String address; //dire
    private String postcode; //cp
    private String city; //pobl
    private String fullName; //nomComplet
    private String personId; //perscod

    public String getAddress()
    {
      return address;
    }

    public void setAddress(String address)
    {
      this.address = address;
    }

    public String getChildren()
    {
      return children;
    }

    public void setChildren(String children)
    {
      this.children = children;
    }

    public String getCity()
    {
      return city;
    }

    public void setCity(String city)
    {
      this.city = city;
    }

    public String getEndDate()
    {
      return endDate;
    }

    public void setEndDate(String endDate)
    {
      this.endDate = trim(endDate);
    }

    public String getFullName()
    {
      return fullName;
    }

    public void setFullName(String fullName)
    {
      this.fullName = fullName;
    }

    public String getDni()
    {
      return dni;
    }

    public void setDni(String dni)
    {
      this.dni = trim(dni);
    }

    public String getPayslipId()
    {
      return trim(payslipId);
    }

    public void setPayslipId(String payslipId)
    {
      this.payslipId = payslipId;
    }

    public String getJobType()
    {
      return jobType;
    }

    public void setJobType(String jobType)
    {
      this.jobType = jobType;
    }

    public String getCategory()
    {
      return category;
    }

    public void setCategory(String category)
    {
      this.category = category;
    }

    public String getPersonId()
    {
      return personId;
    }

    public void setPersonId(String personId)
    {
      this.personId = personId;
    }

    public String getPost()
    {
      return post;
    }

    public void setPost(String post)
    {
      this.post = post;
    }

    public String getPostcode()
    {
      return postcode;
    }

    public void setPostcode(String postcode)
    {
      this.postcode = postcode;
    }

    public String getSocSecNumber()
    {
      return socSecNumber;
    }

    public void setSocSecNumber(String socSecNumber)
    {
      this.socSecNumber = socSecNumber;
    }

    public String getStartDate()
    {
      return startDate;
    }

    public void setStartDate(String startDate)
    {
      this.startDate = trim(startDate);
    }
    
    private String trim(String value)
    {
      if (value != null && value.trim().length() > 0)
        return value.trim();
      else
        return null;
    }
  }
  
}
