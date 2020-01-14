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
package org.santfeliu.elections.service;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceContext;

import javax.xml.ws.WebServiceException;
import org.matrix.elections.Board;
import org.matrix.elections.Councillor;
import org.matrix.elections.District;
import org.matrix.elections.ElectionsManagerPort;
import org.matrix.elections.ElectionsResult;
import org.matrix.elections.PoliticalParty;

import org.santfeliu.jpa.JPA;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.jpa.JPAUtils;


/**
 * @autor: Abel Blanque, Ricard Real
 * @version: 0.3
 */
/**
 *
 * @author unknown
 */
@WebService(endpointInterface = "org.matrix.elections.ElectionsManagerPort")
@HandlerChain(file="handlers.xml")
@JPA
public class ElectionsManager implements ElectionsManagerPort
{
  @Resource
  WebServiceContext wsContext;

  @PersistenceContext
  public EntityManager entityManager;

  protected static final Logger log = Logger.getLogger("Elections");

  public List<Councillor> listCouncillors(XMLGregorianCalendar date,
    String callId)
  {
    Calendar calendar = (date != null ? date.toGregorianCalendar() : null);
    ElectionsManager.Call call = createCall(calendar, callId);
    List<Councillor> result = new ArrayList();
    List<DBCouncillor> dbCouncillors = call.listCouncillors();
    if (dbCouncillors != null)
    {
      for (DBCouncillor dbCouncillor : dbCouncillors)
      {
        Councillor councillor = new Councillor();
        JPAUtils.copy(dbCouncillor, councillor);
        result.add(councillor);
      }
    }
    return result;
  }

  public List<PoliticalParty> listPoliticalParties(
    XMLGregorianCalendar date, String callId)
  {
    List<PoliticalParty> result = new ArrayList();
    Calendar calendar = (date != null ? date.toGregorianCalendar() : null);
    ElectionsManager.Call call = createCall(calendar, callId);
    List<DBPoliticalParty> dbPoliticalParties = call.listPoliticalParties();
    if (dbPoliticalParties != null)
    {
      for (DBPoliticalParty dbPoliticalParty : dbPoliticalParties)
      {
        PoliticalParty politicalParty = new PoliticalParty();
        JPAUtils.copy(dbPoliticalParty, politicalParty);
        result.add(politicalParty);
      }
    }
    return result;
  }

  public List<Board> listBoards(XMLGregorianCalendar date, String callId)
  {
    Calendar calendar = (date != null ? date.toGregorianCalendar() : null);
    ElectionsManager.Call call = createCall(calendar, callId);
    List<Board> result = new ArrayList();
    List<DBBoard> dbBoards = call.listBoards();
    if (dbBoards != null)
    {
      for (DBBoard dbBoard : dbBoards)
      {
        Board board = new Board();
        JPAUtils.copy(dbBoard, board);
        result.add(board);
      }
    }
    return result;
  }

  public List<ElectionsResult> listResults(XMLGregorianCalendar date,
    String callId)
  {
    log.log(Level.INFO, "listResults date:{0} call:{1}",
      new Object[]{date.toString(), callId});

    Calendar calendar = (date != null ? date.toGregorianCalendar() : null);
    ElectionsManager.Call call = createCall(calendar, callId);
    List<ElectionsResult> electionsResults = new ArrayList();
    List<DBElectionsResult> dbResults = call.loadResults();
    if (dbResults != null)
    {
      for (DBElectionsResult dbResult : dbResults)
      {
        ElectionsResult result = new ElectionsResult();
        JPAUtils.copy(dbResult, result);
        electionsResults.add(result);
      }
    }
    return electionsResults;
  }

  public List<org.matrix.elections.Call> listCalls()
  {
    log.log(Level.INFO, "listCalls");

    List result = new ArrayList();
    try
    {
      String provinceId =
        MatrixConfig.getClassProperty(ElectionsManager.class, "provinceId");
      String townId =
        MatrixConfig.getClassProperty(ElectionsManager.class, "townId");
      Query query = entityManager.createNamedQuery("listCalls");
      query.setParameter("provinceId", provinceId);
      query.setParameter("townId", townId);

      List<Object[]> queryResultList = query.getResultList();
      if (queryResultList != null)
      {
        for (Object[] resultItem : queryResultList)
        {
          DBCall dbCall = (DBCall)resultItem[0];
          DBCallType dbCallType = (DBCallType)resultItem[1];
          org.matrix.elections.Call call = new org.matrix.elections.Call();
          JPAUtils.copy(dbCall, call);

          String sDate = dbCall.getDateString();
          Date date = new SimpleDateFormat("yyyyMMdd").parse(sDate);
          GregorianCalendar c = new GregorianCalendar();
          c.setTime(date);
          call.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
          call.setDescription(dbCallType.getDescription());
          result.add(call);
        }
      }
    }
    catch (Exception ex)
    {
      throw new WebServiceException(ex);
    }
    return result;
  }

  public List<District> listDistricts(XMLGregorianCalendar date)
  {
    log.log(Level.INFO, "listDistricts");

    List result = new ArrayList();
    String provinceId =
      MatrixConfig.getClassProperty(ElectionsManager.class, "provinceId");
    String townId =
      MatrixConfig.getClassProperty(ElectionsManager.class, "townId");
    Calendar calendar = date.toGregorianCalendar();
    String sdate = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());

    Query query = entityManager.createNamedQuery("listDistricts");
    query.setParameter("provinceId", provinceId);
    query.setParameter("townId", townId);
    query.setParameter("dateFilter", sdate);

    List<DBDistrict> dbDistricts = query.getResultList();
    if (dbDistricts != null)
    {
      for (DBDistrict dbDistrict : dbDistricts)
      {
        District district = new District();
        JPAUtils.copy(dbDistrict, district);
        result.add(district);
      }
    }
    return result;
  }

  // *** private methods ***

  private Call createCall(Calendar date, String callid)
  {
    String provinceId =
      MatrixConfig.getClassProperty(ElectionsManager.class, "provinceId");
    String townId =
      MatrixConfig.getClassProperty(ElectionsManager.class, "townId");

    return new Call(provinceId, townId, date, callid);
  }

  public class Call
  {
    public String provinceId;
    public String townId;
    public Calendar date;
    public String callId;


    public Call(Calendar date, String callid)
    {
      this.date = date;
      this.callId = callid;
    }

    public Call(String provinceid, String townid, Calendar date, String callid)
    {
      this.provinceId = provinceid;
      this.townId = townid;
      this.date = date;
      this.callId = callid;
    }

    public List<DBPoliticalParty> listPoliticalParties()
    {
      Query query = createNamedQuery("listPoliticalParties");
      List<DBPoliticalParty> dbPoliticalParties = query.getResultList();
      return dbPoliticalParties;
    }

    public List<DBCouncillor> listCouncillors()
    {
      Query query = createNamedQuery("listCouncillors");
      List<DBCouncillor> dbCouncillors = query.getResultList();
      return dbCouncillors;
    }

    public List<DBElectionsResult> loadResults()
    {
      Query query = createNamedQuery("listElectionsResults");
      List<DBElectionsResult> dbResults = query.getResultList();
      return dbResults;
    }

    public List<DBBoard> listBoards()
    {
      Query query = createNamedQuery("listBoards");
      List<DBBoard> dbBoards = query.getResultList();
      return dbBoards;
    }

    private Query createNamedQuery(String queryName)
    {
      Query query = entityManager.createNamedQuery(queryName);
      String sdate = new SimpleDateFormat("yyyyMMdd").format(date.getTime());
      query.setParameter("provinceId", provinceId);
      query.setParameter("townId", townId);
      query.setParameter("dateFilter", sdate);
      query.setParameter("callTypeId", callId);
      query.setHint("toplink.refresh", "true");
      return query;
    }
  }
}
