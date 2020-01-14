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
package org.santfeliu.presence.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import org.apache.commons.lang.StringUtils;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.KernelManagerService;
import org.matrix.kernel.Person;
import org.matrix.presence.Absence;
import org.matrix.presence.AbsenceCounter;
import org.matrix.presence.AbsenceCounterFilter;
import org.matrix.presence.AbsenceCounterView;
import org.matrix.presence.AbsenceCounting;
import org.matrix.presence.AbsenceFilter;
import org.matrix.presence.AbsenceType;
import org.matrix.presence.AbsenceTypeFilter;
import org.matrix.presence.AbsenceView;
import org.matrix.presence.DayType;
import org.matrix.presence.DayTypeFilter;
import org.matrix.presence.Holiday;
import org.matrix.presence.HolidayFilter;
import org.matrix.presence.PresenceEntry;
import org.matrix.presence.PresenceEntryFilter;
import org.matrix.presence.PresenceEntryType;
import org.matrix.presence.PresenceEntryTypeFilter;
import org.matrix.presence.PresenceManagerPort;
import org.matrix.presence.PresenceParameter;
import org.matrix.presence.ScheduleEntry;
import org.matrix.presence.WeekType;
import org.matrix.presence.WeekTypeFilter;
import org.matrix.presence.WorkReduction;
import org.matrix.presence.WorkReductionFilter;
import org.matrix.presence.Worker;
import org.matrix.presence.WorkerFilter;
import org.matrix.presence.WorkerSchedule;
import org.matrix.presence.WorkerStatistics;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.jpa.JPA;
import org.santfeliu.presence.util.Utils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.security.UserCache;
import org.santfeliu.util.MatrixConfig;
import static org.santfeliu.presence.util.Utils.*;
import static org.matrix.presence.PresenceConstants.*;
import org.matrix.presence.ScheduleFault;
import org.santfeliu.security.User;

/**
 *
 * @author realor
 */
@WebService(endpointInterface = "org.matrix.presence.PresenceManagerPort")
@HandlerChain(file="handlers.xml")
@JPA
public class PresenceManager implements PresenceManagerPort
{
  @Resource
  WebServiceContext wsContext;

  @PersistenceContext
  public EntityManager entityManager;

  protected static final Logger log = Logger.getLogger("Presence");

  public static final String WEEK_DEFAULT = "WEEK_DEFAULT";

  private static final int MAX_ENTRY_REASON_LENGTH = 200;
  private static final int MAX_ABSENCE_REASON_LENGTH = 200;

  /* Worker */

  @Override
  public int countWorkers(WorkerFilter filter)
  {
    Query query = entityManager.createNamedQuery("countWorkers");
    setWorkerFilter(query, filter);
    return ((Number)query.getSingleResult()).intValue();
  }

  @Override
  public List<Worker> findWorkers(WorkerFilter filter)
  {
    Query query = entityManager.createNamedQuery("findWorkers");
    setWorkerFilter(query, filter);
    return query.getResultList();
  }

  @Override
  public Worker loadWorker(String personId)
  {
    log.log(Level.INFO, "loadWorker {0}", personId);
    DBWorker dbWorker =
      entityManager.find(DBWorker.class, personId);
    if (dbWorker == null)
      throw new WebServiceException("presence:WORKER_NOT_FOUND");

    return dbWorker;
  }

  @Override
  public Worker storeWorker(Worker worker)
  {
    log.log(Level.INFO, "storeWorker {0}", worker.getPersonId());
    DBWorker dbWorker;

    String personId = worker.getPersonId();
    if (personId == null)
      throw new WebServiceException("presence:PERSONID_IS_MANDATORY");

    Person person = getKernelPort().loadPerson(personId);
    if (person == null)
      throw new WebServiceException("presence:PERSON_NOT_FOUND");

    HashSet<String> personIds = new HashSet<String>();
    personIds.add(personId);
    checkValidationLoop(worker.getValidatorPersonId(), personIds);

    dbWorker = entityManager.find(DBWorker.class, personId);
    if (dbWorker == null) // create
    {
      dbWorker = new DBWorker();
      dbWorker.copyFrom(worker);
      if (StringUtils.isBlank(dbWorker.getFullName()))
      {
        dbWorker.setFullName(getFullName(person));
      }
      entityManager.persist(dbWorker);
    }
    else // update
    {
      dbWorker.copyFrom(worker);
      if (StringUtils.isBlank(dbWorker.getFullName()))
      {
        dbWorker.setFullName(getFullName(person));
      }
      dbWorker = entityManager.merge(dbWorker);
    }
    return dbWorker;
  }

  @Override
  public boolean removeWorker(String personId)
  {
    log.log(Level.INFO, "removeWorker {0}", personId);
    boolean removed;
    try
    {
      DBWorker dbWorker =
        entityManager.getReference(DBWorker.class, personId);
      entityManager.remove(dbWorker);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      entityManager.getTransaction().rollback();
      removed = false;
    }
    return removed;
  }

  /* AbsenceCounter */

  @Override
  public int countAbsenceCounters(AbsenceCounterFilter filter)
  {
    Query query = entityManager.createNamedQuery("countAbsenceCounters");
    setAbsenceCounterFilter(query, filter);
    return ((Number)query.getSingleResult()).intValue();
  }

  @Override
  public List<AbsenceCounter> findAbsenceCounters(AbsenceCounterFilter filter)
  {
    Query query = entityManager.createNamedQuery("findAbsenceCounters");
    setAbsenceCounterFilter(query, filter);
    return query.getResultList();
  }

  @Override
  public List<AbsenceCounterView> findAbsenceCounterViews(AbsenceCounterFilter filter)
  {
    Query query = entityManager.createNamedQuery("findAbsenceCounterViews");
    setAbsenceCounterFilter(query, filter);
    List list = query.getResultList();
    List<AbsenceCounterView> views = new ArrayList<AbsenceCounterView>();
    for (Object elem : list)
    {
      Object[] row = (Object[])elem;
      DBAbsenceCounter dbAbsenceCounter = (DBAbsenceCounter)row[0];
      dbAbsenceCounter.updateAbsenceCounterId();
      DBAbsenceType dbAbsenceType = (DBAbsenceType)row[1];
      AbsenceCounterView view = new AbsenceCounterView();
      view.setAbsenceCounter(dbAbsenceCounter);
      view.setAbsenceType(dbAbsenceType);
      views.add(view);
    }
    return views;
  }

  @Override
  public AbsenceCounter loadAbsenceCounter(String absenceCounterId)
  {
    log.log(Level.INFO, "loadAbsenceCounter {0}", absenceCounterId);
    DBAbsenceCounterPK pk = new DBAbsenceCounterPK(absenceCounterId);
    DBAbsenceCounter dbAbsenceCounter =
      entityManager.find(DBAbsenceCounter.class, pk);
    if (dbAbsenceCounter == null)
      throw new WebServiceException("presence:ABSENCE_COUNTER_NOT_FOUND");
    return dbAbsenceCounter;
  }

  @Override
  public AbsenceCounter storeAbsenceCounter(AbsenceCounter absenceCounter)
  {
    log.log(Level.INFO, "storeAbsenceCounter {0}",
      absenceCounter.getAbsenceCounterId());

    if (absenceCounter.getTotalTime() < 0)
      absenceCounter.setTotalTime(0);
    if (absenceCounter.getRemainingTime() < 0)
      absenceCounter.setRemainingTime(0);

    String absenceCounterId = absenceCounter.getAbsenceCounterId();
    DBAbsenceCounter dbAbsenceCounter;
    if (absenceCounterId == null)
    {
      dbAbsenceCounter = new DBAbsenceCounter();
    }
    else
    {
      DBAbsenceCounterPK pk = new DBAbsenceCounterPK(absenceCounterId);
      dbAbsenceCounter =
        entityManager.getReference(DBAbsenceCounter.class, pk);
    }
    dbAbsenceCounter.copyFrom(absenceCounter);
    dbAbsenceCounter.updateAbsenceCounterId();
    return entityManager.merge(dbAbsenceCounter);
  }

  @Override
  public boolean removeAbsenceCounter(String absenceCounterId)
  {
    log.log(Level.INFO, "removeAbsenceCounter {0}", absenceCounterId);
    boolean removed;
    try
    {
      DBAbsenceCounterPK pk = new DBAbsenceCounterPK(absenceCounterId);
      DBAbsenceCounter dbAbsenceCounter =
        entityManager.getReference(DBAbsenceCounter.class, pk);
      entityManager.remove(dbAbsenceCounter);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      entityManager.getTransaction().rollback();
      removed = false;
    }
    return removed;
  }

  @Override
  public int createAbsenceCounters(String personId, String year, 
    boolean createZeroCounters)
  {
    Query query = entityManager.createNamedQuery("findAbsenceTypes");
    query.setParameter("label", null);
    List<DBAbsenceType> dbAbsenceTypes = query.getResultList();
    int counters = 0;
    for (DBAbsenceType dbAbsenceType : dbAbsenceTypes)
    {
      if (dbAbsenceType.isEnabled() && 
         (dbAbsenceType.getDefaultTime() > 0 ||
        (dbAbsenceType.getDefaultTime() == 0 && createZeroCounters)))
      {
        String absenceTypeId = dbAbsenceType.getAbsenceTypeId();
        DBAbsenceCounterPK pk =
          new DBAbsenceCounterPK(personId, year, absenceTypeId);
        DBAbsenceCounter dbAbsenceCounter =
          entityManager.find(DBAbsenceCounter.class, pk);
        if (dbAbsenceCounter == null) // counter do not exists for this year
        {
          DBAbsenceCounter dbAbsenceCounterNew = new DBAbsenceCounter();
          dbAbsenceCounterNew.setPersonId(personId);
          dbAbsenceCounterNew.setYear(year);
          dbAbsenceCounterNew.setAbsenceTypeId(absenceTypeId);

          if (dbAbsenceType.isCarry())
          {
            // look for previous year
            String prevYear = String.valueOf(Integer.parseInt(year) - 1);
            pk = new DBAbsenceCounterPK(personId, prevYear, absenceTypeId);
            dbAbsenceCounter = entityManager.find(DBAbsenceCounter.class, pk);
            if (dbAbsenceCounter == null) // no previous counter exists
            {
              dbAbsenceCounterNew.setTotalTime(dbAbsenceType.getDefaultTime());
              dbAbsenceCounterNew.setRemainingTime(dbAbsenceType.getDefaultTime());
            }
            else
            {
              dbAbsenceCounterNew.setTotalTime(dbAbsenceCounter.getTotalTime());
              dbAbsenceCounterNew.setRemainingTime(dbAbsenceCounter.getTotalTime());
            }
          }
          else // init counter with value from dbAbsenceType
          {
            dbAbsenceCounterNew.setTotalTime(dbAbsenceType.getDefaultTime());
            dbAbsenceCounterNew.setRemainingTime(dbAbsenceType.getDefaultTime());
          }
          entityManager.persist(dbAbsenceCounterNew);
          counters++;
        }
      }
    }
    return counters;
  }

  @Override
  public int copyAbsenceCounters(String fromPersonId,
    List<String> toPersonIds, String year)
  {
    int counters = 0;
    Query query = entityManager.createNamedQuery("findAbsenceCounters");
    query.setParameter("year", year);
    query.setParameter("personId", fromPersonId);
    query.setParameter("absenceTypeId", null);
    query.setParameter("counterVisibleValue", null);
    List<DBAbsenceCounter> results = query.getResultList();
    for (DBAbsenceCounter dbAbsenceCounter : results)
    {
      String absenceTypeId = dbAbsenceCounter.getAbsenceTypeId();
      for (String personId : toPersonIds)
      {
        DBAbsenceCounterPK pk =
          new DBAbsenceCounterPK(personId, year, absenceTypeId);
        DBAbsenceCounter dbAbsenceCounterOther =
          entityManager.find(DBAbsenceCounter.class, pk);
        if (dbAbsenceCounterOther == null)
        {
          dbAbsenceCounterOther = new DBAbsenceCounter();
          dbAbsenceCounterOther.setYear(year);
          dbAbsenceCounterOther.setAbsenceTypeId(absenceTypeId);
          dbAbsenceCounterOther.setPersonId(personId);
          dbAbsenceCounterOther.setTotalTime(dbAbsenceCounter.getTotalTime());
          dbAbsenceCounterOther.setRemainingTime(
            dbAbsenceCounter.getRemainingTime());
          entityManager.persist(dbAbsenceCounterOther);
          counters++;
        }
      }
    }
    return counters;
  }

  /* PresenceEntryType */

  @Override
  public int countPresenceEntryTypes(PresenceEntryTypeFilter filter)
  {
    Query query = entityManager.createNamedQuery("countPresenceEntryTypes");
    setPresenceEntryTypeFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<PresenceEntryType> findPresenceEntryTypes(PresenceEntryTypeFilter filter)
  {
    Query query = entityManager.createNamedQuery("findPresenceEntryTypes");
    setPresenceEntryTypeFilter(query, filter);
    return query.getResultList();
  }
  
  @Override
  public PresenceEntryType loadPresenceEntryType(String entryTypeId)
  {
    log.log(Level.INFO, "loadEntryType {0}", entryTypeId);
    DBPresenceEntryType dbEntryType =
      entityManager.find(DBPresenceEntryType.class, entryTypeId);
    if (dbEntryType == null)
      throw new WebServiceException("presence:ENTRYTYPE_NOT_FOUND");
    return dbEntryType;
  }

  @Override
  public PresenceEntryType storePresenceEntryType(
    PresenceEntryType presenceEntryType)
  {
    log.log(Level.INFO, "storePresenceEntryType {0}",
      presenceEntryType.getEntryTypeId());
    DBPresenceEntryType dbPresenceEntryType;
    String entryTypeId = presenceEntryType.getEntryTypeId();
    if (entryTypeId == null)
    {
      dbPresenceEntryType = new DBPresenceEntryType();
    }
    else
    {
      dbPresenceEntryType = entityManager.getReference(
        DBPresenceEntryType.class, entryTypeId);
    }
    dbPresenceEntryType.copyFrom(presenceEntryType);
    String filter = dbPresenceEntryType.getFilter();
    if (!StringUtils.isBlank(filter)) // normalize filter expression: "(+|-)bTEAM+b"
    {
      filter = filter.trim();
      if ("+".equals(filter) || "-".equals(filter))
      {
        filter = null;
      }
      else if (filter.startsWith("+") || filter.startsWith("-"))
      {
        filter = filter.substring(0, 1) + " " + 
          filter.substring(1).trim() + " ";
      }
      else
      {
        filter = "+ " + filter + " ";
      }
      dbPresenceEntryType.setFilter(filter);
    }
    return entityManager.merge(dbPresenceEntryType);
  }

  @Override
  public boolean removePresenceEntryType(String entryTypeId)
  {
    log.log(Level.INFO, "removePresenceEntryType {0}", entryTypeId);
    boolean removed;
    try
    {
      DBPresenceEntryType dbPresenceEntryType =
        entityManager.getReference(DBPresenceEntryType.class, entryTypeId);
      entityManager.remove(dbPresenceEntryType);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      entityManager.getTransaction().rollback();
      removed = false;
    }
    return removed;
  }

  /* PresenceEntry */

  @Override
  public int countPresenceEntries(PresenceEntryFilter filter)
  {
    String sql = "SELECT count(e) FROM DBPresenceEntry e WHERE ";
    if (filter.getPersonId().size() > 0)
    {
      sql += "e.personId IN " + toSQL(filter.getPersonId()) + " AND ";
    }
    sql += " (e.startDateTime <= :endDateTime OR :endDateTime IS NULL) AND " +
      " (e.endDateTime >= :startDateTime OR e.endDateTime IS NULL) AND " +
      " (e.entryTypeId = :entryTypeId OR :entryTypeId IS NULL) AND " + 
      " (e.manipulatedValue = :manipulatedValue OR :manipulatedValue IS NULL) AND " +
      " (e.reason LIKE :reason OR :reason IS NULL)";

    Query query = entityManager.createQuery(sql);
    setPresenceEntryFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<PresenceEntry> findPresenceEntries(PresenceEntryFilter filter)
  {
    String sql = "SELECT e FROM DBPresenceEntry e WHERE ";
    if (filter.getPersonId().size() > 0)
    {
      sql += "e.personId IN " + toSQL(filter.getPersonId()) + " AND ";
    }
    sql += " (e.startDateTime <= :endDateTime OR :endDateTime IS NULL) AND " +
      " (e.endDateTime >= :startDateTime OR e.endDateTime IS NULL) AND " +
      " (e.entryTypeId = :entryTypeId OR :entryTypeId IS NULL) AND " + 
      " (e.manipulatedValue = :manipulatedValue OR :manipulatedValue IS NULL) AND " +
      " (e.reason LIKE :reason OR :reason IS NULL) " +
      " ORDER BY e.startDateTime";
    
    Query query = entityManager.createQuery(sql);
    setPresenceEntryFilter(query, filter);
    List<PresenceEntry> presenceEntries =
      (List<PresenceEntry>)query.getResultList();
    if (filter.isSplitByDay())
    {
      List<PresenceEntry> splitted = new ArrayList<PresenceEntry>();
      for (PresenceEntry presenceEntry : presenceEntries)
      {
        splitted.addAll(splitPresenceEntry(presenceEntry));
      }
      presenceEntries = splitted;
      Iterator<PresenceEntry> iter = presenceEntries.iterator();
      while (iter.hasNext())
      {
        PresenceEntry presenceEntry = iter.next();
        if (compareDates(presenceEntry.getEndDateTime(), filter.getStartDateTime()) <= 0 ||
          compareDates(presenceEntry.getStartDateTime(), filter.getEndDateTime()) > 0 ||
          (presenceEntry.getEndDateTime() == null &&
           presenceEntry.getStartDateTime().compareTo(filter.getStartDateTime()) < 0))
        {
          iter.remove();
        }
      }
    }
    return presenceEntries;
  }

  @Override
  public PresenceEntry loadPresenceEntry(String entryId)
  {
    log.log(Level.INFO, "loadPresenceEntry {0}", entryId);
    DBPresenceEntryPK pk = new DBPresenceEntryPK(entryId);
    DBPresenceEntry dbPresenceEntry =
      entityManager.find(DBPresenceEntry.class, pk);
    if (dbPresenceEntry == null)
      throw new WebServiceException("presence:PRESENCE_ENTRY_NOT_FOUND");
    return dbPresenceEntry;
  }

  @Override
  public PresenceEntry storePresenceEntry(PresenceEntry presenceEntry)
  {
    log.log(Level.INFO, "storePresenceEntry {0}", presenceEntry.getEntryId());
    String nowDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");

    User user = UserCache.getUser(wsContext);
    if (presenceEntry.isManipulated()) // manipulated entry
    {
      if (compareDates(nowDateTime, presenceEntry.getStartDateTime()) < 0)
        throw new WebServiceException("presence:FUTURE_DATE_NOT_ALLOWED");

      if (!user.isInRole(PRESENCE_ADMIN_ROLE))
      {
        if (!isEntryEditionEnabled(presenceEntry.getStartDateTime()))
          throw new WebServiceException("presence:EDITION_BLOCKED");

        if (StringUtils.isBlank(presenceEntry.getReason()))
          throw new WebServiceException("presence:ENTRY_REASON_IS_MANDATORY");
      }
    }
    else // not manipulated
    {
      if (presenceEntry.getEntryId() == null) // new entry
      {
        // take nowDateTime as startDateTime
        presenceEntry.setStartDateTime(nowDateTime);
      }
    }
    String reason = truncate(presenceEntry.getReason(), MAX_ENTRY_REASON_LENGTH);
    presenceEntry.setReason(reason);

    lockWorker(presenceEntry.getPersonId());

    DBPresenceEntry dbPresenceEntry;
    String entryId = presenceEntry.getEntryId();

    if (entryId == null) // new entry
    {
      dbPresenceEntry = new DBPresenceEntry();
      dbPresenceEntry.copyFrom(presenceEntry);

      Query query = entityManager.createNamedQuery("findPresenceEntryOnDate");
      query.setParameter("personId", presenceEntry.getPersonId());
      query.setParameter("dateTime", presenceEntry.getStartDateTime());
      List<DBPresenceEntry> list = (List<DBPresenceEntry>)query.getResultList();
      String startDateTime = presenceEntry.getStartDateTime();
      String endDateTime = null;
      DBPresenceEntry dbPresenceEntryPrev = null;
      if (list.isEmpty()) // no previous entry exists in dateTime
      {
        query = entityManager.createNamedQuery("findNextPresenceEntry");
        query.setParameter("personId", presenceEntry.getPersonId());
        query.setParameter("dateTime", presenceEntry.getStartDateTime());
        query.setMaxResults(1);
        list = query.getResultList();
        if (!list.isEmpty()) // look for next entry
        {
          DBPresenceEntry dbPresenceEntryNext = list.get(0);
          endDateTime = dbPresenceEntryNext.getStartDateTime();
        }
      }
      else // previous entry exists in dateTime
      {
        dbPresenceEntryPrev = list.get(0);
        
        if (dbPresenceEntryPrev.getStartDateTime().equals(startDateTime))
          throw new WebServiceException("presence:ENTRY_EXISTS_AT_SAME_TIME");

        if (dbPresenceEntryPrev.getEntryTypeId().equals(
          presenceEntry.getEntryTypeId()) && !presenceEntry.isManipulated())
          throw new WebServiceException("presence:CONTIGUOUS_ENTRIES_OF_SAME_TYPE"); 
        
        if (dbPresenceEntryPrev.getAbsenceId() != null &&
          !user.isInRole(PRESENCE_ADMIN_ROLE) &&
          !isEntryTypeEnabled(dbPresenceEntryPrev))
          throw new WebServiceException("presence:NOT_AUTHORIZED");

        endDateTime = dbPresenceEntryPrev.getEndDateTime();
      }
      String ipAddress = dbPresenceEntry.getIpAddress();
      if (ipAddress == null)
      {
        MessageContext mc = wsContext.getMessageContext();
        HttpServletRequest req =
          (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST);
        ipAddress = req.getRemoteAddr();
      }
      if (!isValidIpAddress(ipAddress))
      {
        dbPresenceEntry.setManipulated(true);
      }

      // apply compensation
      int compensationTime = getCompensationTime(dbPresenceEntry, dbPresenceEntryPrev);
      if (compensationTime > 0 && isCompensableIpAddress(ipAddress))
      {
        startDateTime = Utils.addDateTime(startDateTime, -compensationTime);
        dbPresenceEntry.setCompensationTime(compensationTime);
      }
      if (dbPresenceEntryPrev != null)
      {
        dbPresenceEntryPrev.setEndDateTime(startDateTime);
        updateDuration(dbPresenceEntryPrev);
        entityManager.merge(dbPresenceEntryPrev);
        entityManager.flush();
      }
      dbPresenceEntry.setStartDateTime(startDateTime);
      dbPresenceEntry.setEndDateTime(endDateTime);
      dbPresenceEntry.setCreationDateTime(nowDateTime);
      dbPresenceEntry.setCreationUserId(user.getUserId());
      dbPresenceEntry.setChangeDateTime(nowDateTime);
      dbPresenceEntry.setChangeUserId(user.getUserId());
      updateDuration(dbPresenceEntry);
      entityManager.persist(dbPresenceEntry);
      dbPresenceEntry.updateEntryId();
    }
    else // update entry
    {
      DBPresenceEntryPK pk = new DBPresenceEntryPK(entryId);
      dbPresenceEntry = entityManager.getReference(DBPresenceEntry.class, pk);

      if (dbPresenceEntry.getAbsenceId() != null &&
        !user.isInRole(PRESENCE_ADMIN_ROLE))
        throw new WebServiceException("presence:NOT_AUTHORIZED");

      String startDateTime = presenceEntry.getStartDateTime();
      String oldStartDateTime = dbPresenceEntry.getStartDateTime();
      String oldEndDateTime = dbPresenceEntry.getEndDateTime();

      if (startDateTime.equals(oldStartDateTime))
      {
        // simple change: startDateTime is not modified
        if (!StringUtils.equals(presenceEntry.getEntryTypeId(),
          dbPresenceEntry.getEntryTypeId()))
        {
          // type change
          dbPresenceEntry.setEntryTypeId(presenceEntry.getEntryTypeId());
        }
        updateDuration(dbPresenceEntry);
        dbPresenceEntry.setReason(presenceEntry.getReason());
        dbPresenceEntry.setChangeDateTime(nowDateTime);
        dbPresenceEntry.setChangeUserId(user.getUserId());
        dbPresenceEntry = entityManager.merge(dbPresenceEntry);
      }
      else
      {
        // complex change: startDateTime change
        if (oldEndDateTime != null &&
          startDateTime.compareTo(oldEndDateTime) >= 0)
        {
          throw new WebServiceException("presence:INVALID_ENTRY_TIME_CHANGE");
        }
        Query query = entityManager.createNamedQuery("findPresenceEntryOnDate");
        query.setParameter("personId", presenceEntry.getPersonId());
        query.setParameter("dateTime", Utils.addDateTime(oldStartDateTime, -1));
        List<DBPresenceEntry> list = (List<DBPresenceEntry>)query.getResultList();
        if (!list.isEmpty())
        {
          DBPresenceEntry dbPresenceEntryPrev = list.get(0);
          if (dbPresenceEntryPrev.getStartDateTime().compareTo(startDateTime) >= 0)
            throw new WebServiceException("presence:INVALID_ENTRY_TIME_CHANGE");

          if (dbPresenceEntryPrev.getAbsenceId() != null &&
            !user.isInRole(PRESENCE_ADMIN_ROLE))
            throw new WebServiceException("presence:NOT_AUTHORIZED");

          dbPresenceEntryPrev.setEndDateTime(startDateTime);
          updateDuration(dbPresenceEntryPrev);
          entityManager.merge(dbPresenceEntryPrev);
        }
        DBPresenceEntry dbPresenceEntryNew = new DBPresenceEntry();
        dbPresenceEntryNew.copyFrom(dbPresenceEntry);
        dbPresenceEntryNew.setStartDateTime(startDateTime);
        dbPresenceEntryNew.setReason(presenceEntry.getReason());
        dbPresenceEntryNew.setChangeDateTime(nowDateTime);
        dbPresenceEntryNew.setChangeUserId(user.getUserId());
        dbPresenceEntryNew.setManipulated(true);
        dbPresenceEntryNew.setCompensationTime(0);
        updateDuration(dbPresenceEntryNew);
        dbPresenceEntryNew.updateEntryId();

        entityManager.remove(dbPresenceEntry);
        entityManager.persist(dbPresenceEntryNew);
        dbPresenceEntry = dbPresenceEntryNew;
      }
    }
    return dbPresenceEntry;
  }

  @Override
  public boolean removePresenceEntry(String entryId)
  {
    log.log(Level.INFO, "removePresenceEntry {0}", entryId);
    boolean removed;
    try
    {
      User user = UserCache.getUser(wsContext);

      DBPresenceEntryPK pk = new DBPresenceEntryPK(entryId);
      DBPresenceEntry dbPresenceEntry =
        entityManager.getReference(DBPresenceEntry.class, pk);

      if (!user.isInRole(PRESENCE_ADMIN_ROLE))
      {
        if (!isEntryEditionEnabled(dbPresenceEntry.getStartDateTime()))
          throw new WebServiceException("presence:EDITION_BLOCKED");

        if (dbPresenceEntry.getAbsenceId() != null)
          throw new WebServiceException("presence:NOT_AUTHORIZED");
      }
      String personId = dbPresenceEntry.getPersonId();
      String startDateTime = dbPresenceEntry.getStartDateTime();
      String endDateTime = dbPresenceEntry.getEndDateTime();
      entityManager.remove(dbPresenceEntry);
      entityManager.flush();
      Query query = entityManager.createNamedQuery("findPreviousPresenceEntry");
      query.setParameter("personId", personId);
      query.setParameter("dateTime", startDateTime);
      query.setMaxResults(1);
      List<DBPresenceEntry> list = (List<DBPresenceEntry>)query.getResultList();
      if (!list.isEmpty())
      {
        DBPresenceEntry dbPresenceEntryPrev = list.get(0);
        dbPresenceEntryPrev.setEndDateTime(endDateTime);
        updateDuration(dbPresenceEntryPrev);
        entityManager.merge(dbPresenceEntryPrev);
      }
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      entityManager.getTransaction().rollback();
      removed = false;
    }
    return removed;
  }

  /* Absence */

  @Override
  public int countAbsences(AbsenceFilter filter)
  {
    String sql = "SELECT count(a) FROM DBAbsence a WHERE " +
      "(a.absenceId = :absenceId OR :absenceId IS NULL) AND ";
    if (filter.getPersonId().size() > 0)
    {
      sql += "a.personId IN " + toSQL(filter.getPersonId()) + " AND ";
    }
    sql += "(a.absenceTypeId = :absenceTypeId OR :absenceTypeId IS NULL) AND" +
      "(a.startDateTime <= :endDateTime OR :endDateTime IS NULL) AND " +
      "(a.endDateTime >= :startDateTime OR :startDateTime IS NULL) AND " +
      "(a.status = :status OR :status IS NULL)";

    Query query = entityManager.createQuery(sql);
    setAbsenceFilter(query, filter);
    Number number = (Number)query.getSingleResult();
    return number.intValue();
  }

  @Override
  public List<Absence> findAbsences(AbsenceFilter filter)
  {
    String sql = "SELECT a FROM DBAbsence a WHERE " +
      "(a.absenceId = :absenceId OR :absenceId IS NULL) AND ";
    if (filter.getPersonId().size() > 0)
    {
      sql += "a.personId IN " + toSQL(filter.getPersonId()) + " AND ";
    }
    sql += "(a.absenceTypeId = :absenceTypeId OR :absenceTypeId IS NULL) AND" +
      "(a.startDateTime <= :endDateTime OR :endDateTime IS NULL) AND " +
      "(a.endDateTime >= :startDateTime OR :startDateTime IS NULL) AND " +
      "(a.status = :status OR :status IS NULL) " +
      "ORDER BY a.personId, a.startDateTime";
    Query query = entityManager.createQuery(sql);
    setAbsenceFilter(query, filter);
    List<Absence> absences = query.getResultList();
    if (filter.isSplitByDay())
    {
      List<Absence> splitted = new ArrayList<Absence>();
      for (Absence absence : absences)
      {
        splitted.addAll(splitAbsence(absence));
      }
      absences = splitted;
      Iterator<Absence> iter = absences.iterator();
      while (iter.hasNext())
      {
        Absence absence = iter.next();
        if (compareDates(absence.getEndDateTime(), filter.getStartDateTime()) <= 0 ||
          compareDates(absence.getStartDateTime(), filter.getEndDateTime()) > 0)
        {
          iter.remove();
        }
      }
    }
    return absences;
  }

  @Override
  public List<AbsenceView> findAbsenceViews(AbsenceFilter filter)
  {
    String sql =
      "SELECT a, t, w FROM DBAbsence a, DBAbsenceType t, DBWorker w WHERE ";
    if (filter.getPersonId().size() > 0)
    {
      sql += "a.personId IN " + toSQL(filter.getPersonId()) + " AND ";
    }
    sql += "a.personId = w.personId AND " +
      "(a.absenceId = :absenceId OR :absenceId IS NULL) AND " +
      "a.absenceTypeId = t.absenceTypeId AND " +
      "(a.absenceTypeId = :absenceTypeId OR :absenceTypeId IS NULL) AND " +
      "(a.startDateTime <= :endDateTime OR :endDateTime IS NULL) AND " +
      "(a.endDateTime >= :startDateTime OR :startDateTime IS NULL) AND " +
      "(a.status = :status OR :status IS NULL) " +
      "ORDER BY a.personId, a.startDateTime";

    Query query = entityManager.createQuery(sql);
    setAbsenceFilter(query, filter);
    List resultList = query.getResultList();
    List<AbsenceView> absenceViews = new ArrayList<AbsenceView>();
    for (Object elem : resultList)
    {
      Object[] row = (Object[])elem;
      DBAbsence dbAbsence = (DBAbsence)row[0];
      DBAbsenceType dbAbsenceType = (DBAbsenceType)row[1];
      DBWorker dbWorker = (DBWorker)row[2];
      AbsenceView absenceView = new AbsenceView();
      absenceView.setAbsence(dbAbsence);
      absenceView.setAbsenceType(dbAbsenceType);
      absenceView.setWorker(dbWorker);
      String combinedStatus = dbAbsence.getStatus();
      if (!StringUtils.isBlank(dbAbsence.getStatusDetail()))
      {
        combinedStatus += " " + dbAbsence.getStatusDetail();
      }
      absenceView.setStatus(combinedStatus);
      absenceViews.add(absenceView);
    }
    return absenceViews;
  }

  @Override
  public Absence loadAbsence(String absenceId)
  {
    log.log(Level.INFO, "loadAbsence {0}", absenceId);
    DBAbsence dbAbsence =
      entityManager.find(DBAbsence.class, absenceId);
    if (dbAbsence == null)
      throw new WebServiceException("presence:ABSENCE_NOT_FOUND");
    return dbAbsence;
  }

  @Override
  public Absence storeAbsence(Absence absence)
  {
    log.log(Level.INFO, "storeAbsence {0}", absence.getAbsenceId());
    if (absence.getStartDateTime() == null || absence.getEndDateTime() == null)
      throw new WebServiceException("presence:UNDEFINED_DATE");
    if (absence.getStartDateTime().compareTo(absence.getEndDateTime()) >= 0)
      throw new WebServiceException("presence:INVALID_PERIOD");
    if (StringUtils.isBlank(absence.getAbsenceTypeId()))
      throw new WebServiceException("presence:ABSENCE_TYPE_IS_MANDATORY");

    String reason = truncate(absence.getReason(), MAX_ABSENCE_REASON_LENGTH);
    absence.setReason(reason);

    Query query = entityManager.createNamedQuery("findAbsenceOverlap");
    query.setParameter("absenceId", absence.getAbsenceId());
    query.setParameter("personId", absence.getPersonId());
    query.setParameter("startDateTime", absence.getStartDateTime());
    query.setParameter("endDateTime", absence.getEndDateTime());
    int overlapped = ((Number)query.getSingleResult()).intValue();
    if (overlapped > 0)
      throw new WebServiceException("presence:ABSENCE_OVERLAP");

    DBAbsence dbAbsence;
    String absenceId = absence.getAbsenceId();
    if (absenceId == null)
    {
      dbAbsence = new DBAbsence();
    }
    else
    {
      dbAbsence = entityManager.getReference(DBAbsence.class, absenceId);
      updateCounter(dbAbsence, +1); // restore counter

      if (CONSOLIDATED_STATUS.equals(dbAbsence.getStatus()))
      {
        // absence was consolidated, remove entries first
        removeAbsenceEntries(dbAbsence);
      }
    }
    dbAbsence.copyFrom(absence); // copy new absence data
    dbAbsence.setConsolidatedTime(0);
    dbAbsence.setConsolidatedDays(0);
    if (dbAbsence.getStatus() == null)
    {
      dbAbsence.setStatus(PENDENT_STATUS);
    }
    List<ScheduleEntry> scheduleEntries = updateAbsenceTimings(dbAbsence);

    String status = dbAbsence.getStatus();
    if (PENDENT_STATUS.equals(status) || CANCELLED_STATUS.equals(status))
    {
      dbAbsence.setStatusDetail(null);
      dbAbsence.setInstanceId(null);
      dbAbsence.setAbsenceDocId(null);
      dbAbsence.setJustificantDocId(null);
    }
    else if (CONSOLIDATED_STATUS.equals(dbAbsence.getStatus()))
    {
      consolidateAbsenceEntries(dbAbsence, scheduleEntries);
    }
    updateCounter(dbAbsence, -1); // decrement counter
    dbAbsence = entityManager.merge(dbAbsence);
    return dbAbsence;
  }

  @Override
  public boolean removeAbsence(String absenceId)
  {
    log.log(Level.INFO, "removeAbsence {0}", absenceId);
    boolean removed;
    try
    {
      DBAbsence dbAbsence =
        entityManager.getReference(DBAbsence.class, absenceId);
      if (CONSOLIDATED_STATUS.equals(dbAbsence.getStatus())) // absence consolidated!
      {
        User user = UserCache.getUser(wsContext);
        if (!user.isInRole(PRESENCE_ADMIN_ROLE))
        {
          throw new WebServiceException("presence:NOT_AUTHORIZED");
        }
        else
        {
          // remove absence entries
          removeAbsenceEntries(dbAbsence);
        }
      }
      updateCounter(dbAbsence, +1); // restore counter

      entityManager.remove(dbAbsence);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      entityManager.getTransaction().rollback();
      removed = false;
    }
    return removed;
  }

  /* AbsenceType */

  @Override
  public int countAbsenceTypes(AbsenceTypeFilter filter)
  {
    Query query = entityManager.createNamedQuery("countAbsenceTypes");
    setAbsenceTypeFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<AbsenceType> findAbsenceTypes(AbsenceTypeFilter filter)
  {
    Query query = entityManager.createNamedQuery("findAbsenceTypes");
    setAbsenceTypeFilter(query, filter);
    return query.getResultList();
  }

  @Override
  public AbsenceType loadAbsenceType(String absenceTypeId)
  {
    log.log(Level.INFO, "loadAbsenceType {0}", absenceTypeId);
    DBAbsenceType dbAbsenceType =
      entityManager.find(DBAbsenceType.class, absenceTypeId);
    if (dbAbsenceType == null)
      throw new WebServiceException("presence:ABSENCETYPE_NOT_FOUND");
    return dbAbsenceType;
  }

  @Override
  public AbsenceType storeAbsenceType(AbsenceType absenceType)
  {
    log.log(Level.INFO, "storeAbsenceType {0}", absenceType.getAbsenceTypeId());
    DBAbsenceType dbAbsenceType;
    String absenceTypeId = absenceType.getAbsenceTypeId();
    if (absenceTypeId == null)
    {
      dbAbsenceType = new DBAbsenceType();
    }
    else
    {
      dbAbsenceType =
        entityManager.getReference(DBAbsenceType.class, absenceTypeId);
    }
    if (absenceType.getDefaultTime() < 0) absenceType.setDefaultTime(-1);
    dbAbsenceType.copyFrom(absenceType);
    return entityManager.merge(dbAbsenceType);
  }

  @Override
  public boolean removeAbsenceType(String absenceTypeId)
  {
    log.log(Level.INFO, "removeAbsenceType {0}", absenceTypeId);
    boolean removed;
    try
    {
      DBAbsenceType dbAbsenceType =
        entityManager.getReference(DBAbsenceType.class, absenceTypeId);
      entityManager.remove(dbAbsenceType);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      entityManager.getTransaction().rollback();
      removed = false;
    }
    return removed;
  }

  /* WeekType */

  @Override
  public int countWeekTypes(WeekTypeFilter filter)
  {
    Query query = entityManager.createNamedQuery("countWeekTypes");
    setWeekTypeFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<WeekType> findWeekTypes(WeekTypeFilter filter)
  {
    Query query = entityManager.createNamedQuery("findWeekTypes");
    setWeekTypeFilter(query, filter);
    return query.getResultList();
  }

  @Override
  public WeekType loadWeekType(String weekTypeId)
  {
    log.log(Level.INFO, "loadWeekType {0}", weekTypeId);
    DBWeekType dbWeekType = entityManager.find(DBWeekType.class, weekTypeId);
    if (dbWeekType == null)
      throw new WebServiceException("presence:WEEKTYPE_NOT_FOUND");
    return dbWeekType;
  }

  @Override
  public WeekType storeWeekType(WeekType weekType)
  {
    log.log(Level.INFO, "storeWeekType {0}", weekType.getWeekTypeId());
    DBWeekType dbWeekType;
    String weekTypeId = weekType.getWeekTypeId();
    if (weekTypeId == null)
    {
      dbWeekType = new DBWeekType();
    }
    else
    {
      dbWeekType = entityManager.getReference(DBWeekType.class, weekTypeId);
    }
    dbWeekType.copyFrom(weekType);
    return entityManager.merge(dbWeekType);
  }

  @Override
  public boolean removeWeekType(String weekTypeId)
  {
    log.log(Level.INFO, "removeWeekType {0}", weekTypeId);
    boolean removed;
    try
    {
      DBWeekType dbWeekType =
        entityManager.getReference(DBWeekType.class, weekTypeId);
      entityManager.remove(dbWeekType);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      entityManager.getTransaction().rollback();
      removed = false;
    }
    return removed;
  }

  /* DayType */

  @Override
  public int countDayTypes(DayTypeFilter filter)
  {
    Query query = entityManager.createNamedQuery("countDayTypes");
    setDayTypeFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<DayType> findDayTypes(DayTypeFilter filter)
  {
    Query query = entityManager.createNamedQuery("findDayTypes");
    setDayTypeFilter(query, filter);
    return query.getResultList();
  }

  @Override
  public DayType loadDayType(String dayTypeId)
  {
    log.log(Level.INFO, "loadWeekType {0}", dayTypeId);
    DBDayType dbDayType = entityManager.find(DBDayType.class, dayTypeId);
    if (dbDayType == null)
      throw new WebServiceException("presence:DAYTYPE_NOT_FOUND");
    return dbDayType;
  }

  @Override
  public DayType storeDayType(DayType dayType)
  {
    log.log(Level.INFO, "storeDayType {0}", dayType.getDayTypeId());
    
    String inTime1 = dayType.getInTime1();
    String outTime1 = dayType.getOutTime1();
    String inTime2 = dayType.getInTime2();
    String outTime2 = dayType.getOutTime2();
    
    if (StringUtils.isBlank(inTime1) || 
        StringUtils.isBlank(outTime1) ||
        (!StringUtils.isBlank(inTime2) && StringUtils.isBlank(outTime2)) ||
        (StringUtils.isBlank(inTime2) && !StringUtils.isBlank(outTime2)))    
      throw new WebServiceException("presence:INVALID_DAY_TYPE_DEFINITION");

    DBDayType dbDayType;
    String dayTypeId = dayType.getDayTypeId();
    if (dayTypeId == null)
    {
      dbDayType = new DBDayType();
    }
    else
    {
      dbDayType = entityManager.getReference(DBDayType.class, dayTypeId);
    }
    dbDayType.copyFrom(dayType);
    return entityManager.merge(dbDayType);
  }

  @Override
  public boolean removeDayType(String dayTypeId)
  {
    log.log(Level.INFO, "removeDayType {0}", dayTypeId);
    boolean removed;
    try
    {
      DBDayType dbDayType =
        entityManager.getReference(DBDayType.class, dayTypeId);
      entityManager.remove(dbDayType);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      entityManager.getTransaction().rollback();
      removed = false;
    }
    return removed;
  }

  /* Holiday */

  @Override
  public int countHolidays(HolidayFilter filter)
  {
    Query query = entityManager.createNamedQuery("countHolidays");
    setHolidayFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<Holiday> findHolidays(HolidayFilter filter)
  {
    log.log(Level.INFO, "findHolidays {0}", filter.getStartDate());
    Query query = entityManager.createNamedQuery("findHolidays");
    setHolidayFilter(query, filter);
    return query.getResultList();
  }

  @Override
  public Holiday loadHoliday(String holidayId)
  {
    log.log(Level.INFO, "loadHoliday {0}", holidayId);
    DBHoliday dbHoliday = entityManager.find(DBHoliday.class, holidayId);
    if (dbHoliday == null)
      throw new WebServiceException("presence:HOLIDAY_NOT_FOUND");
    return dbHoliday;
  }

  @Override
  public Holiday storeHoliday(Holiday holiday)
  {
    log.log(Level.INFO, "storeHoliday {0} {1}",
      new Object[]{holiday.getStartDate(), holiday.getDescription()});
    DBHoliday dbHoliday;
    String holidayId = holiday.getHolidayId();
    if (holidayId == null)
    {
      dbHoliday = new DBHoliday();
      dbHoliday.copyFrom(holiday);
      entityManager.persist(dbHoliday);
    }
    else
    {
      dbHoliday = entityManager.getReference(DBHoliday.class, holidayId);
      dbHoliday.copyFrom(holiday);
      dbHoliday = entityManager.merge(dbHoliday);
    }
    dbHoliday.copyTo(holiday);
    return holiday;
  }

  @Override
  public boolean removeHoliday(String holidayId)
  {
    log.log(Level.INFO, "removeHoliday {0}", holidayId);
    boolean removed;
    try
    {
      DBHoliday dbHoliday =
        entityManager.getReference(DBHoliday.class, holidayId);
      entityManager.remove(dbHoliday);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      entityManager.getTransaction().rollback();
      removed = false;
    }
    return removed;
  }

  /* WorkReduction */

  @Override
  public int countWorkReductions(WorkReductionFilter filter)
  {
    Query query = entityManager.createNamedQuery("countWorkReductions");
    setWorkReductionFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<WorkReduction> findWorkReductions(WorkReductionFilter filter)
  {
    log.log(Level.INFO, "findWorkReductions {0}", filter.getStartDate());
    Query query = entityManager.createNamedQuery("findWorkReductions");
    setWorkReductionFilter(query, filter);
    return query.getResultList();
  }

  @Override
  public WorkReduction loadWorkReduction(String reductionId)
  {
    log.log(Level.INFO, "loadWorkReduction {0}", reductionId);
    DBWorkReduction dbWorkReduction =
      entityManager.find(DBWorkReduction.class, reductionId);
    if (dbWorkReduction == null)
      throw new WebServiceException("presence:WORKREDUCTION_NOT_FOUND");
    return dbWorkReduction;
  }

  @Override
  public WorkReduction storeWorkReduction(WorkReduction workReduction)
  {
    log.log(Level.INFO, "storeWorkReduction {0} {1}",
      new Object[]{workReduction.getStartDate(), workReduction.getDescription()});
    DBWorkReduction dbWorkReduction;
    String reductionId = workReduction.getReductionId();
    if (reductionId == null)
    {
      dbWorkReduction = new DBWorkReduction();
    }
    else
    {
      dbWorkReduction =
        entityManager.getReference(DBWorkReduction.class, reductionId);
    }
    dbWorkReduction.copyFrom(workReduction);
    return entityManager.merge(dbWorkReduction);
  }

  @Override
  public boolean removeWorkReduction(String reductionId)
  {
    log.log(Level.INFO, "removeWorkReduction {0}", reductionId);
    boolean removed;
    try
    {
      DBWorkReduction dbWorkReduction =
        entityManager.getReference(DBWorkReduction.class, reductionId);
      entityManager.remove(dbWorkReduction);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      entityManager.getTransaction().rollback();
      removed = false;
    }
    return removed;
  }

  /* Schedule */

  @Override
  public String setWorkerWeekType(String personId,
    String startDate, String endDate, String weekTypeId)
  {
    Query query = entityManager.createNamedQuery("findWorkerWeekOnPeriod");
    query.setParameter("personId", personId);
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    List<DBWorkerWeek> list = (List<DBWorkerWeek>)query.getResultList();
    for (DBWorkerWeek dbWorkerWeek : list)
    {
      String dbStartDate = dbWorkerWeek.getStartDate();
      String dbEndDate = dbWorkerWeek.getEndDate();
      if (compareDates(dbStartDate, startDate) < 0 &&
          compareDates(dbEndDate, endDate) <= 0)
      {
        // move endDate
        dbWorkerWeek.setEndDate(addDate(startDate, -1));
        entityManager.merge(dbWorkerWeek);
      }
      else if (compareDates(startDate, dbStartDate) <= 0 &&
               compareDates(endDate, dbEndDate) < 0)
      {
        // move startDate, remove and insert cause startDate is PK
        DBWorkerWeek dbWorkerWeek2 = new DBWorkerWeek();
        dbWorkerWeek2.setPersonId(personId);
        dbWorkerWeek2.setStartDate(addDate(endDate, +1));
        dbWorkerWeek2.setEndDate(dbWorkerWeek.getEndDate());
        dbWorkerWeek2.setWeekTypeId(dbWorkerWeek.getWeekTypeId());
        entityManager.remove(dbWorkerWeek);
        entityManager.flush();
        entityManager.persist(dbWorkerWeek2);
      }
      else if (compareDates(startDate, dbStartDate) <= 0 &&
               compareDates(dbEndDate, endDate) <= 0)
      {
        // remove
        entityManager.remove(dbWorkerWeek);
        entityManager.flush();
      }
      else
      {
        // split in 2 parts
        dbWorkerWeek.setEndDate(addDate(startDate, -1));
        entityManager.merge(dbWorkerWeek);
        DBWorkerWeek dbWorkerWeek2 = new DBWorkerWeek();
        dbWorkerWeek2.setPersonId(personId);
        dbWorkerWeek2.setStartDate(addDate(endDate, +1));
        dbWorkerWeek2.setEndDate(dbEndDate);
        dbWorkerWeek2.setWeekTypeId(dbWorkerWeek.getWeekTypeId());
        entityManager.persist(dbWorkerWeek2);
      }
    }
    if (!StringUtils.isBlank(weekTypeId)) // insert new period
    {
      DBWorkerWeek dbWorkerWeek = new DBWorkerWeek();
      dbWorkerWeek.setPersonId(personId);
      dbWorkerWeek.setStartDate(startDate);
      dbWorkerWeek.setEndDate(endDate);
      dbWorkerWeek.setWeekTypeId(weekTypeId);
      entityManager.persist(dbWorkerWeek);
    }
    return weekTypeId;
  }

  @Override
  public String setWorkerDayType(String personId, String date, String dayTypeId)
  {
    if (WEEK_DEFAULT.equals(dayTypeId))
    {
      DBWorkerDayPK pk = new DBWorkerDayPK(personId, date);
      try
      {
        DBWorkerDay dbWorkerDay =
          entityManager.getReference(DBWorkerDay.class, pk);
        entityManager.remove(dbWorkerDay);
      }
      catch (EntityNotFoundException ex)
      {
        entityManager.getTransaction().rollback();
      }
    }
    else
    {
      DBWorkerDay workerDay = new DBWorkerDay();
      workerDay.setPersonId(personId);
      workerDay.setDate(date);
      workerDay.setDayTypeId(dayTypeId);
      entityManager.merge(workerDay);
    }
    return dayTypeId;
  }

  @Override
  public List<WorkerSchedule> getSchedule(List<String> personIdList,
    String startDate, String endDate)
  {
    if (startDate == null || endDate == null ||
      compareDates(startDate, endDate) > 0)
      throw new WebServiceException("presence:INVALID_PERIOD");

    List<WorkerSchedule> scheduleList = new ArrayList<WorkerSchedule>();

    HashMap<String, List<DBWorkerWeek>> wwMap =
      new HashMap<String, List<DBWorkerWeek>>();
    HashMap<String, List<DBWorkerDay>> wdMap =
      new HashMap<String, List<DBWorkerDay>>();
    HashMap<String, DBWeekType> wtMap = new HashMap<String, DBWeekType>();
    HashMap<String, DBDayType> dtMap = new HashMap<String, DBDayType>();

    loadWeekAndDayTypes(personIdList, startDate, endDate,
      wwMap, wdMap, wtMap, dtMap);

    // Holidays
    Query query = entityManager.createNamedQuery("findHolidays");
    query.setParameter("description", null);
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    List<DBHoliday> dbHolidays = query.getResultList();

    Calendar calendar = Calendar.getInstance();
    for (String personId : personIdList)
    {
      //System.out.println(">> " + personId);
      WorkerSchedule schedule = new WorkerSchedule();
      schedule.setPersonId(personId);
      schedule.setStartDate(startDate);
      schedule.setEndDate(endDate);
      scheduleList.add(schedule);

      List<DBWorkerWeek> wwPersonList = wwMap.get(personId);
      List<DBWorkerDay> wdPersonList = wdMap.get(personId);
      Date sd = TextUtils.parseInternalDate(startDate);
      Date ed = TextUtils.parseInternalDate(endDate);
      calendar.setTime(sd);
      Date cd = calendar.getTime();
      String date = startDate;

      while (cd.before(ed) || cd.equals(ed))
      {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String nextDate = TextUtils.formatDate(calendar.getTime(), "yyyyMMdd");

        DBWeekType weekType = null;
        DBDayType dayType = null;

        DBWorkerWeek workerWeek = findWorkerWeek(wwPersonList, date);
        if (workerWeek != null)
        {
          weekType = wtMap.get(workerWeek.getWeekTypeId());
          dayType = dtMap.get(weekType.getDayTypeId(dayOfWeek));
        }
        DBHoliday dbHoliday = findHoliday(dbHolidays, date);
        if (dbHoliday != null && !dbHoliday.isOptional())
        {
          if (dayType != null && dayType.isHolidaysEnabled())
          {
            dayType = null;
          }
        }
        DBWorkerDay workerDay = findWorkerDay(wdPersonList, date);
        if (workerDay != null)
        {
          if (workerDay.getDayTypeId() == null) dayType = null;
          else dayType = dtMap.get(workerDay.getDayTypeId());
        }
        if (dayType != null)
        {
          schedule.getDayTypeId().add(dayType.getDayTypeId());
        }
        else
        {
          schedule.getDayTypeId().add(null);
        }
        cd = calendar.getTime();
        date = nextDate;
      }
    }
    return scheduleList;
  }

  @Override
  public List<ScheduleEntry> getScheduleEntries(List<String> personIdList,
    String startDateTime, String endDateTime, 
    boolean splitByDay, boolean cropByPeriod)
  {
    if (startDateTime == null || endDateTime == null ||
      compareDates(startDateTime, endDateTime) > 0)
      throw new WebServiceException("presence:INVALID_PERIOD");

    String startDate = startDateTime.substring(0, 8);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(TextUtils.parseInternalDate(startDate));
    calendar.add(Calendar.DAY_OF_MONTH, -1);
    startDate = TextUtils.formatDate(calendar.getTime(), "yyyyMMdd");
    String endDate = endDateTime.substring(0, 8);

    HashMap<String, List<DBWorkerWeek>> wwMap =
      new HashMap<String, List<DBWorkerWeek>>();
    HashMap<String, List<DBWorkerDay>> wdMap =
      new HashMap<String, List<DBWorkerDay>>();
    HashMap<String, DBWeekType> wtMap = new HashMap<String, DBWeekType>();
    HashMap<String, DBDayType> dtMap = new HashMap<String, DBDayType>();

    loadWeekAndDayTypes(personIdList, startDate, endDate,
      wwMap, wdMap, wtMap, dtMap);

    // Holidays
    Query query = entityManager.createNamedQuery("findHolidays");
    query.setParameter("description", null);
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    List<DBHoliday> dbHolidays = query.getResultList();

    // Reductions
    query = entityManager.createNamedQuery("findWorkReductions");
    query.setParameter("description", null);
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    List<DBWorkReduction> dbWorkReductions = query.getResultList();

    List<ScheduleEntry> scheduleEntries = new ArrayList<ScheduleEntry>();
    for (String personId : personIdList)
    {
      List<DBWorkerWeek> wwPersonList = wwMap.get(personId);
      List<DBWorkerDay> wdPersonList = wdMap.get(personId);
      Date sd = TextUtils.parseInternalDate(startDate);
      Date ed = TextUtils.parseInternalDate(endDate);
      calendar.setTime(sd);
      Date cd = calendar.getTime();
      String date = startDate;

      while (cd.before(ed) || cd.equals(ed))
      {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String nextDate = TextUtils.formatDate(calendar.getTime(), "yyyyMMdd");

        DBDayType dayType = null;

        DBWorkerWeek workerWeek = findWorkerWeek(wwPersonList, date);
        if (workerWeek != null)
        {
          DBWeekType weekType = wtMap.get(workerWeek.getWeekTypeId());
          dayType = dtMap.get(weekType.getDayTypeId(dayOfWeek));
        }
        DBHoliday dbHoliday = findHoliday(dbHolidays, date);
        if (dbHoliday != null && !dbHoliday.isOptional())
        {
          if (dayType != null && dayType.isHolidaysEnabled())
          {
            dayType = null; // apply holiday: clear dayType
          }
        }
        DBWorkerDay workerDay = findWorkerDay(wdPersonList, date);
        if (workerDay != null)
        {
          if (workerDay.getDayTypeId() == null) dayType = null;
          else dayType = dtMap.get(workerDay.getDayTypeId());
        }
        double reductionFactor = 0;
        List<String> reductionIds = new ArrayList<String>();
        if (dayType != null && dayType.isReductionsEnabled())
        {
          reductionFactor = getWorkReductionFactor(dbWorkReductions, date,
            reductionIds);
          if (reductionFactor >= 100)
          {
            dayType = null;
          }
        }
        if (dayType != null)
        {
          ScheduleEntry entry1 = new ScheduleEntry();
          entry1.setPersonId(personId);
          entry1.setDayTypeId(dayType.getDayTypeId());
          entry1.setFlexibility(dayType.getFlexibility1());
          String startDate1 = date;
          String endDate1 =
            dayType.getInTime1().compareTo(dayType.getOutTime1()) < 0 ?
            date : nextDate;
          entry1.setStartDateTime(startDate1 + dayType.getInTime1());
          entry1.setEndDateTime(endDate1 + dayType.getOutTime1());
          ScheduleEntry entry2 = null;
          if (dayType.getInTime2() != null)
          {
            entry2 = new ScheduleEntry();
            entry2.setPersonId(personId);
            entry2.setDayTypeId(dayType.getDayTypeId());
            entry2.setFlexibility(dayType.getFlexibility2());
            String startDate2 =
              dayType.getOutTime1().compareTo(dayType.getInTime2()) < 0 ?
              endDate1 : nextDate;
            String endDate2 =
              dayType.getInTime2().compareTo(dayType.getOutTime2()) < 0 ?
              startDate2 : nextDate;
            entry2.setStartDateTime(startDate2 + dayType.getInTime2());
            entry2.setEndDateTime(endDate2 + dayType.getOutTime2());
          }
          int dayDuration = dayType.getDuration();
          int reductionTime = 0;
          if (reductionFactor > 0) // apply work reduction
          {
            reductionTime = (int)Math.round(
              (double)dayDuration * 0.01 * reductionFactor);
            dayDuration -= reductionTime;
          }
          entry1.setInitialDayDuration(dayType.getDuration());
          entry1.setDayDuration(dayDuration);
          entry1.getReductionId().addAll(reductionIds);
          if (reductionTime > 0 && entry2 == null)
          {
            shrink(entry1, reductionTime);
          }
          if (overlaps(entry1, startDateTime, endDateTime))
          {
            scheduleEntries.add(entry1);
          }
          if (entry2 != null)
          {
            entry2.setInitialDayDuration(dayType.getDuration());
            entry2.setDayDuration(dayDuration);
            entry2.getReductionId().addAll(reductionIds);
            if (reductionTime > 0)
            {
              shrink(entry2, reductionTime);
            }
            if (overlaps(entry2, startDateTime, endDateTime))
            {
              scheduleEntries.add(entry2);
            }
          }
        }
        cd = calendar.getTime();
        date = nextDate;
      }
    }
    if (cropByPeriod)
    {
      cropScheduleEntries(scheduleEntries, startDateTime, endDateTime);
    }
    if (splitByDay)
    {
      List<ScheduleEntry> splitted = new ArrayList<ScheduleEntry>();
      for (ScheduleEntry scheduleEntry : scheduleEntries)
      {
        splitted.addAll(splitScheduleEntry(scheduleEntry));
      }
      scheduleEntries = splitted;
    }
    return scheduleEntries;
  }

  @Override
  public WorkerStatistics getWorkerStatistics(String personId,
    String startDateTime, String endDateTime)
  {
    log.log(Level.INFO, "getWorkerStatistics {0}", personId);

    String nowDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");

    WorkerStatistics workerStatistics = new WorkerStatistics();
    workerStatistics.setPersonId(personId);
    workerStatistics.setStartDateTime(startDateTime);
    workerStatistics.setEndDateTime(endDateTime);

    List<String> idList = new ArrayList<String>();
    idList.add(personId);
    List<ScheduleEntry> scheduleEntries =
      getScheduleEntries(idList, startDateTime, endDateTime, true, true);
    workerStatistics.getScheduleEntry().addAll(scheduleEntries);

    PresenceEntryFilter presenceEntryFilter = new PresenceEntryFilter();
    presenceEntryFilter.getPersonId().add(personId);
    presenceEntryFilter.setStartDateTime(startDateTime);
    presenceEntryFilter.setEndDateTime(endDateTime);
    presenceEntryFilter.setSplitByDay(true);
    List<PresenceEntry> presenceEntries =
      findPresenceEntries(presenceEntryFilter);
    workerStatistics.getPresenceEntry().addAll(presenceEntries);

    int daysToWork = 0;
    int timeToWork = 0;
    int instantTimeToWork = 0;
    int instantDaysToWork = 0;
    int workedDays = 0;
    int workedTime = 0;
    int realWorkedTime = 0;
    int absenceTime = 0;
    int scheduleFaultTime = 0;
    int scheduleFaults = 0;
    int compensationTime = 0;
    int entryCount = 0;
    int manipulatedEntryCount = 0;
    int compensatedEntryCount = 0;
    int suspiciousEntryCount = 0;
    int lastEntryDuration = 0;
    int lastEntryWorkedTime = 0;

    String lastDate = null;
    boolean addNextDayContinuation = false;
    for (ScheduleEntry scheduleEntry : scheduleEntries)
    {
      timeToWork += scheduleEntry.getDuration();
      if (scheduleEntry.getEndDateTime().compareTo(nowDateTime) <= 0)
      {
        instantTimeToWork += scheduleEntry.getDuration();
      }
      else if (scheduleEntry.getStartDateTime().compareTo(nowDateTime) < 0)
      {
        instantTimeToWork += scheduleEntry.getDuration();
        if (scheduleEntry.getEndDateTime().endsWith("235959"))
        {
          addNextDayContinuation = true;
        }
      }
      else if (addNextDayContinuation && 
        scheduleEntry.getStartDateTime().endsWith("000000"))
      {
        instantTimeToWork += scheduleEntry.getDuration();
        addNextDayContinuation = false;
      }
      String date = scheduleEntry.getStartDateTime().substring(0, 8);
      if (!date.equals(lastDate))
      {
        daysToWork++;
        lastDate = date;
        if (date.compareTo(nowDateTime.substring(0, 8)) <= 0)
        {
          instantDaysToWork++;
        }
      }
    }
    lastDate = null;
    String lastEntryId = null;
    for (PresenceEntry entry : presenceEntries)
    {
      if (entry.getChangeDateTime() != null)
      {
        entryCount++;
      }
      if (entry.isManipulated() && entry.getCreationDateTime() != null)
      {
        manipulatedEntryCount++;
      }
      if (entry.getCompensationTime() > 0)
      {
        compensatedEntryCount++;
        compensationTime += entry.getCompensationTime();
      }
      if (isAbsenceEntry(entry))
      {
        absenceTime += entry.getDuration();
      }
      String date = entry.getStartDateTime().substring(0, 8);
      if (!date.equals(lastDate) && isWorkEntry(entry))
      {
        workedDays++;
        lastDate = date;
      }
      if (entry.getEndDateTime() != null)
      {
        workedTime += entry.getWorkedTime();
        if (entry.getWorkedTime() > 0 && isRealWorkEntry(entry))
        {
          realWorkedTime += entry.getWorkedTime();
        }
      }
      else // last entry
      {
        DBPresenceEntry dbLastEntry = new DBPresenceEntry();
        dbLastEntry.copyFrom(entry);
        dbLastEntry.setEndDateTime(nowDateTime);
        updateDuration(dbLastEntry);
        lastEntryDuration = dbLastEntry.getDuration();
        lastEntryWorkedTime = dbLastEntry.getWorkedTime();
        workedTime += lastEntryWorkedTime;
        if (lastEntryWorkedTime > 0 && isRealWorkEntry(entry))
        {
          realWorkedTime += lastEntryWorkedTime;
        }
      }
      if (entry.getWorkedTime() > 12 * 3600 &&
        !entry.getEntryId().equals(lastEntryId))
      {
        suspiciousEntryCount++;
        lastEntryId = entry.getEntryId();
      }
    }
    Iterator<PresenceEntry> iterp = presenceEntries.iterator();
    Iterator<ScheduleEntry> iters = scheduleEntries.iterator();

    PresenceEntry presenceEntry = iterp.hasNext() ? iterp.next() : null;
    ScheduleEntry scheduleEntry = iters.hasNext() ? iters.next() : null;
    int duration;
    while (scheduleEntry != null)
    {
      //System.out.println(">>> schedule: " + scheduleEntry.getStartDateTime() +
      //  "/" + scheduleEntry.getEndDateTime());

      if (presenceEntry == null)
      {
        //
        // [SSSSSSS]
        if (scheduleEntry.getEndDateTime().compareTo(nowDateTime) < 0)
        {
          duration = scheduleEntry.getDuration();
          int flexibility = scheduleEntry.getFlexibility();
          if (duration > flexibility)
          {
            scheduleFaultTime += (duration - flexibility);
            scheduleFaults++;
            workerStatistics.getScheduleFault().add(
              createScheduleFault(scheduleEntry, duration - flexibility));
          }
        }
        scheduleEntry = iters.hasNext() ? iters.next() : null;
      }
      else
      {
        String ps = presenceEntry.getStartDateTime();
        String pe = presenceEntry.getEndDateTime() == null ?
          nowDateTime : presenceEntry.getEndDateTime();
        String ss = scheduleEntry.getStartDateTime();
        String se = scheduleEntry.getEndDateTime();
        boolean consolidable = isConsolidableEntry(presenceEntry);
          lastEntryWorkedTime : presenceEntry.getWorkedTime();
        int entryDuration = presenceEntry.getEndDateTime() == null ?
          lastEntryDuration : presenceEntry.getDuration();

//        System.out.println(">>> presence: " +
//          ps + "/" + pe + " wt:" + entryWorkedTime + " dur:" + entryDuration);

        if (ps.compareTo(ss) <= 0 && se.compareTo(pe) <= 0)
        {
          //System.out.println("Case 1");
          // [PPPPPPPPP]
          //    [SSS]
          if (consolidable)
          {
            duration = scheduleEntry.getDuration();
            int flexibility = scheduleEntry.getFlexibility();
            if (duration > flexibility)
            {
              scheduleFaultTime += (duration - flexibility);
              scheduleFaults++;
              workerStatistics.getScheduleFault().add(
                createScheduleFault(scheduleEntry, duration - flexibility));
            }
          }
          scheduleEntry = iters.hasNext() ? iters.next() : null;
        }
        else if (ps.compareTo(ss) <= 0 && pe.compareTo(se) <= 0 &&
          ss.compareTo(pe) < 0)
        {
          //System.out.println("Case 2");
          // [PPPPPPPPP]
          //      [SSSSSSSS]
          if (consolidable)
          {
            duration = getDuration(ss, pe);
            int flexibility = scheduleEntry.getFlexibility();
            if (duration > flexibility)
            {
              scheduleFaultTime += (duration - flexibility);
              scheduleFaults++;
              workerStatistics.getScheduleFault().add(
                createScheduleFault(scheduleEntry, duration - flexibility));
            }
          }
          scheduleEntry = duplicateScheduleEntry(scheduleEntry);
          scheduleEntry = cropScheduleEntry(scheduleEntry, pe, se);
          presenceEntry = iterp.hasNext() ? iterp.next() : null;
        }
        else if (ss.compareTo(ps) <= 0 && se.compareTo(pe) <= 0 &&
          ps.compareTo(se) < 0)
        {
          //System.out.println("Case 3");
          //     [PPPPPPPPP]
          // [SSSSSSSS]
          if (consolidable)
          {
            duration = getDuration(ps, se);
            int flexibility = scheduleEntry.getFlexibility();
            if (duration > flexibility)
            {
              scheduleFaultTime += (duration - flexibility);
              scheduleFaults++;
              workerStatistics.getScheduleFault().add(
                createScheduleFault(scheduleEntry, duration - flexibility));
            }
          }
          scheduleEntry = iters.hasNext() ? iters.next() : null;
        }
        else if (ss.compareTo(ps) < 0 && pe.compareTo(se) < 0)
        {
          //System.out.println("Case 4");
          //    [PPP]
          // [SSSSSSSSS]
          if (consolidable)
          {
            duration = entryDuration;
            int flexibility = scheduleEntry.getFlexibility();
            if (duration > flexibility)
            {
              scheduleFaultTime += (duration - flexibility);
              scheduleFaults++;
              workerStatistics.getScheduleFault().add(
                createScheduleFault(scheduleEntry, duration - flexibility));
            }
          }
          scheduleEntry = duplicateScheduleEntry(scheduleEntry);
          scheduleEntry = cropScheduleEntry(scheduleEntry, pe, se);
          presenceEntry = iterp.hasNext() ? iterp.next() : null;
        }
        else // not overlapped
        {
          if (ps.compareTo(ss) < 0)
          {
            //System.out.println("Case 5");
            presenceEntry = iterp.hasNext() ? iterp.next() : null;
          }
          else
          {
            //System.out.println("Case 6");
            scheduleEntry = iters.hasNext() ? iters.next() : null;
          }
        }
      }
      //System.out.println("sft: " + scheduleFaultTime / 3600.0);
    }
    workerStatistics.setDaysToWork(daysToWork);
    workerStatistics.setTimeToWork(timeToWork);
    workerStatistics.setInstantTimeToWork(instantTimeToWork);
    workerStatistics.setWorkedDays(workedDays);
    workerStatistics.setNotWorkedDays(Math.max(instantDaysToWork - workedDays, 0));
    workerStatistics.setWorkedTime(workedTime);
    workerStatistics.setRealWorkedTime(realWorkedTime);
    workerStatistics.setAbsenceTime(absenceTime);
    workerStatistics.setScheduleFaultTime(scheduleFaultTime);
    workerStatistics.setScheduleFaults(scheduleFaults);
    workerStatistics.setCompensationTime(compensationTime);
    workerStatistics.setEntryCount(entryCount);
    workerStatistics.setManipulatedEntryCount(manipulatedEntryCount);
    workerStatistics.setCompensatedEntryCount(compensatedEntryCount);
    workerStatistics.setSuspiciousEntryCount(suspiciousEntryCount);
    workerStatistics.setWorkedTimeDifference(workedTime - instantTimeToWork);

    if (entryCount > 0)
    {
      float manipulationDegree = 100.0f * manipulatedEntryCount / entryCount;
      workerStatistics.setVeracityDegree(100.0f - manipulationDegree);
    }
    else
    {
      workerStatistics.setVeracityDegree(100.0f);
    }
    if (instantTimeToWork > 0)
    {
      float absenceDegree = 100.0f * absenceTime / instantTimeToWork;
      workerStatistics.setAbsenceDegree(absenceDegree);

      float complianceDegree = 100.0f * workedTime / instantTimeToWork;
      workerStatistics.setComplianceDegree(complianceDegree);
      
      float presenceDegree = 100.0f * realWorkedTime / instantTimeToWork;
      workerStatistics.setPresenceDegree(presenceDegree);
      
      float faultDegree = 100.0f * scheduleFaultTime / instantTimeToWork;
      workerStatistics.setPunctualityDegree(100.0f - faultDegree);
    }
    else
    {
      workerStatistics.setAbsenceDegree(0f);
      workerStatistics.setComplianceDegree(100f);
      workerStatistics.setPresenceDegree(100f);
      workerStatistics.setPunctualityDegree(100f);
    }
    return workerStatistics;
  }

  @Override
  public List<PresenceParameter> findParameters()
  {
    Query query = entityManager.createNamedQuery("findParameters");
    List<PresenceParameter> parameters = query.getResultList();
    return parameters;
  }

  @Override
  public PresenceParameter loadParameter(String parameterId)
  {
    log.log(Level.INFO, "loadParameter {0}", parameterId);
    DBPresenceParameter dbParameter =
      entityManager.find(DBPresenceParameter.class, parameterId);
    if (dbParameter == null)
      throw new WebServiceException("presence:PARAMETER_NOT_FOUND");
    return dbParameter;
  }

  @Override
  public PresenceParameter storeParameter(PresenceParameter parameter)
  {
    log.log(Level.INFO, "storeParameter {0}", parameter.getParameterId());
    DBPresenceParameter dbParameter = new DBPresenceParameter();
    dbParameter.setParameterId(parameter.getParameterId());
    dbParameter.setValue(parameter.getValue());
    String nowDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    dbParameter.setChangeDateTime(nowDateTime);
    String userId = UserCache.getUser(wsContext).getUserId();
    dbParameter.setChangeUserId(userId);
    dbParameter = entityManager.merge(dbParameter);
    return dbParameter;
  }

  @Override
  public boolean removeParameter(String parameterId)
  {
    log.log(Level.INFO, "removeParameter {0}", parameterId);
    boolean removed;
    try
    {
      DBPresenceParameter dbPresenceParameter =
        entityManager.getReference(DBPresenceParameter.class, parameterId);
      entityManager.remove(dbPresenceParameter);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      entityManager.getTransaction().rollback();
      removed = false;
    }
    return removed;
  }

  @Override
  public List<String> getWorkerGroup(String personId)
  {
    Query query = entityManager.createNamedQuery("getWorkerGroup");
    query.setParameter("personId", personId);
    return (List<String>)query.getResultList();
  }

  @Override
  public int setWorkerGroup(String personId, List<String> relatedPersonIdList)
  {
    Query query = entityManager.createNamedQuery("clearWorkerGroup");
    query.setParameter("personId", personId);
    query.executeUpdate();
    entityManager.flush();
    int position = 0;
    for (String relatedPersonId : relatedPersonIdList)
    {
      DBWorkerGroup dbWorkerGroup = new DBWorkerGroup();
      dbWorkerGroup.setPersonId(personId);
      dbWorkerGroup.setRelatedPersonId(relatedPersonId);
      dbWorkerGroup.setPosition(++position);
      entityManager.persist(dbWorkerGroup);
    }
    return position;
  }
  
  private DBHoliday findHoliday(List<DBHoliday> list, String date)
  {
    if (list == null) return null;
    boolean found = false;
    DBHoliday dbHoliday = null;
    Iterator<DBHoliday> iter = list.iterator();
    while (iter.hasNext() && !found)
    {
      dbHoliday = iter.next();
      found = compareDates(dbHoliday.getStartDate(), date) <= 0 &&
        compareDates(date, dbHoliday.getEndDate()) <= 0;
    }
    return found ? dbHoliday : null;
  }

  private double getWorkReductionFactor(List<DBWorkReduction> list,
    String date, List<String> reductionIds)
  {
    double factor = 0;
    Iterator<DBWorkReduction> iter = list.iterator();
    while (iter.hasNext())
    {
      DBWorkReduction dbWorkReduction = iter.next();
      if (compareDates(dbWorkReduction.getStartDate(), date) <= 0 &&
        compareDates(date, dbWorkReduction.getEndDate()) <= 0)
      {
        reductionIds.add(dbWorkReduction.getReductionId());
        factor += dbWorkReduction.getFactor();
      }
    }
    return factor;
  }

  private void loadWeekAndDayTypes(List<String> personIdList,
    String startDate, String endDate,
    HashMap<String, List<DBWorkerWeek>> wwMap,
    HashMap<String, List<DBWorkerDay>> wdMap,
    HashMap<String, DBWeekType> wtMap,
    HashMap<String, DBDayType> dtMap)
  {
    String sqlIdList = toSQL(personIdList);
    Query query = entityManager.createQuery(
    "SELECT ww FROM DBWorkerWeek ww WHERE " +
    " ww.personId IN " + sqlIdList + " AND " +
    " ww.startDate <= :endDate AND " +
    " (ww.endDate >= :startDate OR ww.endDate IS NULL) " +
    " ORDER BY ww.personId, ww.startDate");
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    List<DBWorkerWeek> wwList = (List<DBWorkerWeek>)query.getResultList();
    for (DBWorkerWeek ww : wwList)
    {
      String personId = ww.getPersonId();
      List<DBWorkerWeek> wwPersonList = wwMap.get(personId);
      if (wwPersonList == null)
      {
        wwPersonList = new ArrayList<DBWorkerWeek>();
        wwMap.put(personId, wwPersonList);
      }
      wwPersonList.add(ww);
      wtMap.put(ww.getWeekTypeId(), null);
    }

    query = entityManager.createQuery(
    "SELECT wd FROM DBWorkerDay wd WHERE " +
    " wd.personId IN " + sqlIdList + " AND " +
    " :startDate <= wd.date AND wd.date <= :endDate" +
    " ORDER BY wd.personId, wd.date");
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    List<DBWorkerDay> wdList = (List<DBWorkerDay>)query.getResultList();
    for (DBWorkerDay wd : wdList)
    {
      String personId = wd.getPersonId();
      List<DBWorkerDay> wdPersonList = wdMap.get(personId);
      if (wdPersonList == null)
      {
        wdPersonList = new ArrayList<DBWorkerDay>();
        wdMap.put(personId, wdPersonList);
      }
      wdPersonList.add(wd);
      dtMap.put(wd.getDayTypeId(), null);
    }

    if (!wtMap.isEmpty())
    {
      query = entityManager.createQuery("SELECT wt FROM DBWeekType wt WHERE " +
      " wt.weekTypeId IN " + toSQL(wtMap.keySet()));
      List<DBWeekType> wtList = query.getResultList();
      for (DBWeekType weekType : wtList)
      {
        wtMap.put(weekType.getWeekTypeId(), weekType);
        dtMap.put(weekType.getMondayTypeId(), null);
        dtMap.put(weekType.getTuesdayTypeId(), null);
        dtMap.put(weekType.getWednesdayTypeId(), null);
        dtMap.put(weekType.getThursdayTypeId(), null);
        dtMap.put(weekType.getFridayTypeId(), null);
        dtMap.put(weekType.getSaturdayTypeId(), null);
        dtMap.put(weekType.getSundayTypeId(), null);
      }
    }

    dtMap.remove(null);
    if (!dtMap.isEmpty())
    {
      query = entityManager.createQuery("SELECT dt FROM DBDayType dt WHERE " +
      " dt.dayTypeId IN " + toSQL(dtMap.keySet()));
      List<DBDayType> dtList = query.getResultList();
      for (DBDayType dayType : dtList)
      {
        dtMap.put(dayType.getDayTypeId(), dayType);
      }
    }
  }

  private DBWorkerWeek findWorkerWeek(List<DBWorkerWeek> list, String date)
  {
    if (list == null) return null;
    boolean found = false;
    DBWorkerWeek dbWorkerWeek = null;
    Iterator<DBWorkerWeek> iter = list.iterator();
    while (iter.hasNext() && !found)
    {
      dbWorkerWeek = iter.next();
      found = compareDates(dbWorkerWeek.getStartDate(), date) <= 0 &&
        compareDates(date, dbWorkerWeek.getEndDate()) <= 0;
    }
    return found ? dbWorkerWeek : null;
  }

  private DBWorkerDay findWorkerDay(List<DBWorkerDay> list, String date)
  {
    if (list == null) return null;
    boolean found = false;
    DBWorkerDay dbWorkerDay = null;
    Iterator<DBWorkerDay> iter = list.iterator();
    while (iter.hasNext() && !found)
    {
      dbWorkerDay = iter.next();
      found = dbWorkerDay.getDate().equals(date);
    }
    return found ? dbWorkerDay : null;
  }

  private String toSQL(Collection<String> idCol)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("(");
    boolean first = true;
    Iterator<String> iter = idCol.iterator();
    while (iter.hasNext())
    {
      if (first)
      {
        first = false;
      }
      else
      {
        buffer.append(", ");
      }
      buffer.append("'").append(iter.next()).append("'");
    }
    buffer.append(")");
    return buffer.toString();
  }

  private void setHolidayFilter(Query query, HolidayFilter filter)
  {
    String description = filter.getDescription();
    if (description != null) description = "%" + description + "%";
    query.setParameter("description", description);
    query.setParameter("startDate", filter.getStartDate());
    query.setParameter("endDate", filter.getEndDate());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private void setWorkReductionFilter(Query query, WorkReductionFilter filter)
  {
    String description = filter.getDescription();
    if (description != null) description = "%" + description + "%";
    query.setParameter("description", description);
    query.setParameter("startDate", filter.getStartDate());
    query.setParameter("endDate", filter.getEndDate());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private void setPresenceEntryTypeFilter(Query query,
    PresenceEntryTypeFilter filter)
  {
    String label = filter.getLabel();
    if (label != null) label = "%" + label + "%";
    query.setParameter("label", label);
    if (filter.isEnabled() == null)
    {
      query.setParameter("enabled", null);
    }
    else
    {
      query.setParameter("enabled", filter.isEnabled() ? "T" : "F");
    }
    String team = filter.getTeam();
    if (StringUtils.isBlank(team))
    {
      team = null;
    }
    else if (!"%".equals(team))
    {
      team = "% " + team.trim() + " %";
    }
    query.setParameter("team", team);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private void setPresenceEntryFilter(Query query, PresenceEntryFilter filter)
  {
    query.setParameter("startDateTime", filter.getStartDateTime());
    query.setParameter("endDateTime", filter.getEndDateTime());
    query.setParameter("entryTypeId", filter.getEntryTypeId());
    if (filter.isManipulated() == null)
    {
      query.setParameter("manipulatedValue", null);
    }
    else
    {
      boolean manipulated = filter.isManipulated();
      query.setParameter("manipulatedValue", manipulated ? "T" : "F");
    }
    String reason = filter.getReason();
    if (reason != null) reason = "%" + reason + "%";
    query.setParameter("reason", reason);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private void setAbsenceFilter(Query query, AbsenceFilter filter)
  {
    String startDateTime = filter.getStartDateTime();
    if (startDateTime != null && startDateTime.length() == 8)
    {
      startDateTime += "000000";
    }
    String endDateTime = filter.getEndDateTime();
    if (endDateTime != null && endDateTime.length() == 8)
    {
      endDateTime += "235959";
    }
    if (startDateTime != null && endDateTime != null &&
      startDateTime.compareTo(endDateTime) > 0)
      throw new WebServiceException("presence:INVALID_PERIOD");

    String absenceId = filter.getAbsenceId();
    if (!StringUtils.isBlank(absenceId))
    {
      try
      {
        Integer.parseInt(absenceId);
      }
      catch (NumberFormatException ex)
      {
        throw new WebServiceException("presence:INVALID_ABSENCEID");
      }
    }
    query.setParameter("absenceId", absenceId);
    query.setParameter("startDateTime", startDateTime);
    query.setParameter("endDateTime", endDateTime);
    query.setParameter("absenceTypeId", filter.getAbsenceTypeId());
    query.setParameter("status", filter.getStatus());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private void setAbsenceCounterFilter(Query query, AbsenceCounterFilter filter)
  {
    String year = filter.getYear();
    String date = filter.getDate();
    if (date != null)
    {
      year = getAbsencesYear(date);
    }
    query.setParameter("personId", filter.getPersonId());
    query.setParameter("year", year);
    query.setParameter("absenceTypeId", filter.getAbsenceTypeId());
    if (filter.isCounterVisible() == null)
    {
      query.setParameter("counterVisibleValue", null);
    }
    else
    {
      query.setParameter("counterVisibleValue", 
        filter.isCounterVisible() ? "T" : "F");      
    }
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private void setAbsenceTypeFilter(Query query, AbsenceTypeFilter filter)
  {
    query.setParameter("label", filter.getLabel());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private void setWeekTypeFilter(Query query, WeekTypeFilter filter)
  {
    query.setParameter("label", filter.getLabel());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private void setDayTypeFilter(Query query, DayTypeFilter filter)
  {
    query.setParameter("label", filter.getLabel());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private void setWorkerFilter(Query query, WorkerFilter filter)
  {
    query.setParameter("personId", filter.getPersonId());
    
    String fullName = filter.getFullName();
    fullName = StringUtils.isBlank(fullName) ? null : 
      "%" + fullName.toLowerCase() + "%";
    query.setParameter("fullName", fullName);
    
    String team = filter.getTeam();
    team = StringUtils.isBlank(team) ? null :  
      "%" + team.toLowerCase() + "%";
    query.setParameter("team", team);

    query.setParameter("validatorPersonId", filter.getValidatorPersonId());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
  }

  private void shrink(ScheduleEntry scheduleEntry, int seconds)
  {
    scheduleEntry.setEndDateTime(Utils.shrink(
     scheduleEntry.getStartDateTime(),
      scheduleEntry.getEndDateTime(), seconds));
  }

  private void updateDuration(ScheduleEntry scheduleEntry)
  {
    int duration = Utils.getDuration(
      scheduleEntry.getStartDateTime(), scheduleEntry.getEndDateTime());
    scheduleEntry.setDuration(duration);
  }

  private void updateDuration(DBPresenceEntry dbPresenceEntry)
  {
    dbPresenceEntry.updateDuration();
    String entryTypeId = dbPresenceEntry.getEntryTypeId();
    DBPresenceEntryType dbPresenceEntryType =
      entityManager.find(DBPresenceEntryType.class, entryTypeId);
    if (dbPresenceEntryType != null)
    {
      int maxWorkedTime = dbPresenceEntryType.getMaxWorkedTime();
      int duration = dbPresenceEntry.getDuration();
      dbPresenceEntry.setWorkedTime(Math.min(maxWorkedTime, duration));
    }
  }
  
  private boolean overlaps(ScheduleEntry entry, 
    String startDateTime, String endDateTime)
  {
    return (entry.getStartDateTime().compareTo(endDateTime) <= 0 && 
      entry.getEndDateTime().compareTo(startDateTime) >= 0);
  }

  private void cropScheduleEntries(List<ScheduleEntry> scheduleEntries, 
    String startDateTime, String endDateTime)
  {
    int i = 0;
    while (i < scheduleEntries.size())
    {
      ScheduleEntry entry = scheduleEntries.get(i);
      entry = cropScheduleEntry(entry, startDateTime, endDateTime);
      if (entry == null) scheduleEntries.remove(i);
      else i++;
    }
  }
  
  private ScheduleEntry cropScheduleEntry(ScheduleEntry scheduleEntry,
    String startDateTime, String endDateTime)
  {
    String sdt = scheduleEntry.getStartDateTime();
    String edt = scheduleEntry.getEndDateTime();
    if (edt.compareTo(startDateTime) < 0 || endDateTime.compareTo(sdt) < 0)
    {
      scheduleEntry = null;
    }
    else
    {
      if (sdt.compareTo(startDateTime) < 0)
      {
        scheduleEntry.setStartDateTime(startDateTime);
      }
      if (endDateTime.compareTo(edt) < 0)
      {
        scheduleEntry.setEndDateTime(endDateTime);
      }
      updateDuration(scheduleEntry);
    }
    return scheduleEntry;
  }

  private List<ScheduleEntry> splitScheduleEntry(ScheduleEntry scheduleEntry)
  {
    List<ScheduleEntry> parts = new ArrayList<ScheduleEntry>();
    String startDateTime = scheduleEntry.getStartDateTime();
    String endDateTime = scheduleEntry.getEndDateTime();
    Date startDate = TextUtils.parseInternalDate(startDateTime);
    Date endDate = endDateTime == null ?
      new Date() : TextUtils.parseInternalDate(endDateTime);

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    Date date = calendar.getTime();
    while (date.before(endDate))
    {
      ScheduleEntry part = new ScheduleEntry();
      part.setStartDateTime(TextUtils.formatDate(startDate, "yyyyMMddHHmmss"));
      part.setEndDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
      part.setPersonId(scheduleEntry.getPersonId());
      part.setDayTypeId(scheduleEntry.getDayTypeId());
      part.setInitialDayDuration(scheduleEntry.getInitialDayDuration());
      part.setDayDuration(scheduleEntry.getDayDuration());
      part.getReductionId().addAll(scheduleEntry.getReductionId());
      part.setFlexibility(scheduleEntry.getFlexibility());
      updateDuration(part);
      parts.add(part);

      startDate = date;
      calendar.add(Calendar.DAY_OF_MONTH, 1);
      date = calendar.getTime();
    }
    ScheduleEntry part = new ScheduleEntry();
    part.setStartDateTime(TextUtils.formatDate(startDate, "yyyyMMddHHmmss"));
    part.setEndDateTime(endDateTime);
    part.setPersonId(scheduleEntry.getPersonId());
    part.setDayTypeId(scheduleEntry.getDayTypeId());
    part.setInitialDayDuration(scheduleEntry.getInitialDayDuration());
    part.setDayDuration(scheduleEntry.getDayDuration());
    part.getReductionId().addAll(scheduleEntry.getReductionId());
    part.setFlexibility(scheduleEntry.getFlexibility());
    updateDuration(part);
    parts.add(part);
    return parts;
  }

  private ScheduleFault createScheduleFault(ScheduleEntry scheduleEntry, int time)
  {
    ScheduleFault scheduleFault = new ScheduleFault();
    scheduleFault.setPersonId(scheduleEntry.getPersonId());
    scheduleFault.setDateTime(scheduleEntry.getStartDateTime());
    scheduleFault.setFaultDuration(time);
    return scheduleFault;
  }
  
  private List<PresenceEntry> splitPresenceEntry(PresenceEntry presenceEntry)
  {
    List<PresenceEntry> parts = new ArrayList<PresenceEntry>();
    String startDateTime = presenceEntry.getStartDateTime();
    String endDateTime = presenceEntry.getEndDateTime();
    Date startDate = TextUtils.parseInternalDate(startDateTime);
    Date endDate = endDateTime == null ?
      new Date() : TextUtils.parseInternalDate(endDateTime);

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    Date date = calendar.getTime();
    while (date.before(endDate))
    {
      DBPresenceEntry part = new DBPresenceEntry();
      part.copyFrom(presenceEntry);
      part.setStartDateTime(TextUtils.formatDate(startDate, "yyyyMMddHHmmss"));
      part.setEndDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
      part.setEntryId(presenceEntry.getEntryId());
      updateDuration(part);
      parts.add(part);

      startDate = date;
      calendar.add(Calendar.DAY_OF_MONTH, 1);
      date = calendar.getTime();
    }
    DBPresenceEntry part = new DBPresenceEntry();
    part.copyFrom(presenceEntry);
    part.setStartDateTime(TextUtils.formatDate(startDate, "yyyyMMddHHmmss"));
    part.setEndDateTime(endDateTime);
    part.setEntryId(presenceEntry.getEntryId());
    updateDuration(part);
    parts.add(part);

    for (int i = 1; i < parts.size(); i++)
    {
      part = (DBPresenceEntry)parts.get(i);
      part.setCreationDateTime(null);
      part.setChangeDateTime(null);
    }
    return parts;
  }

  private List<Absence> splitAbsence(Absence absence)
  {
    List<Absence> parts = new ArrayList<Absence>();
    String startDateTime = absence.getStartDateTime();
    String endDateTime = absence.getEndDateTime();
    Date startDate = TextUtils.parseInternalDate(startDateTime);
    Date endDate = TextUtils.parseInternalDate(endDateTime);

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    Date date = calendar.getTime();
    while (date.before(endDate))
    {
      DBAbsence part = new DBAbsence();
      part.copyFrom(absence);
      part.setStartDateTime(TextUtils.formatDate(startDate, "yyyyMMddHHmmss"));
      part.setEndDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
      parts.add(part);

      startDate = date;
      calendar.add(Calendar.DAY_OF_MONTH, 1);
      date = calendar.getTime();
    }
    DBAbsence part = new DBAbsence();
    part.copyFrom(absence);
    part.setStartDateTime(TextUtils.formatDate(startDate, "yyyyMMddHHmmss"));
    part.setEndDateTime(endDateTime);
    parts.add(part);

    return parts;
  }

  private void removeAbsenceEntries(DBAbsence dbAbsence)
  {
    String absenceId = dbAbsence.getAbsenceId();
    System.out.println("Remove absence entries from absence " + absenceId);
    Query query = entityManager.createNamedQuery("findAbsenceEntries");
    query.setParameter("absenceId", absenceId);
    List<DBPresenceEntry> dbAbsenceEntries = query.getResultList();
    String nowDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    for (DBPresenceEntry dbAbsenceEntry : dbAbsenceEntries)
    {
      String previousEntryTypeId = dbAbsenceEntry.getPreviousEntryTypeId();
      if (previousEntryTypeId == null)
      {
        removePresenceEntry(dbAbsenceEntry.getEntryId());
      }
      else
      {
        dbAbsenceEntry.setEntryTypeId(previousEntryTypeId);
        dbAbsenceEntry.setAbsenceId(null);
        dbAbsenceEntry.setChangeDateTime(nowDateTime);
        updateDuration(dbAbsenceEntry);
        entityManager.merge(dbAbsenceEntry);
      }
    }
    entityManager.flush();
  }

  private List<ScheduleEntry> updateAbsenceTimings(DBAbsence dbAbsence)
  {    
    String personId = dbAbsence.getPersonId();
    String startDateTime = dbAbsence.getStartDateTime();
    String endDateTime = dbAbsence.getEndDateTime();
    List<String> personIdList = new ArrayList<String>();
    personIdList.add(personId);
    List<ScheduleEntry> scheduleEntries = getScheduleEntries(personIdList,
      startDateTime, endDateTime, false, false);

    DBAbsenceType dbAbsenceType =
      entityManager.find(DBAbsenceType.class, dbAbsence.getAbsenceTypeId());

    if (!scheduleEntries.isEmpty() && dbAbsenceType.getDefaultTime() >= 0) 
    {
      // extend scheduleEntries only when absence has a counter
      String entryStartDateTime;
      String entryEndDateTime;
      int flexibility;
      
      ScheduleEntry firstEntry = scheduleEntries.get(0);
      entryStartDateTime = firstEntry.getStartDateTime();
      entryEndDateTime = firstEntry.getEndDateTime();
      flexibility = firstEntry.getFlexibility();
      if (flexibility > 0)
      {
        if (startDateTime.compareTo(entryStartDateTime) < 0 &&
          endDateTime.compareTo(entryEndDateTime) < 0 && 
          entryStartDateTime.compareTo(endDateTime) < 0)
        {
          // extend by start
          int move = Utils.getDuration(startDateTime, entryStartDateTime);
          if (move > flexibility) move = flexibility;
          // move down scheduleEntry -move
          firstEntry.setStartDateTime(addDateTime(entryStartDateTime, -move));
          firstEntry.setEndDateTime(addDateTime(entryEndDateTime, -move));
        }
      }
      
      ScheduleEntry lastEntry = scheduleEntries.get(scheduleEntries.size() - 1);
      entryStartDateTime = lastEntry.getStartDateTime();
      entryEndDateTime = lastEntry.getEndDateTime();
      flexibility = lastEntry.getFlexibility();
      if (flexibility > 0)
      {
        if (entryStartDateTime.compareTo(startDateTime) < 0 &&
          entryEndDateTime.compareTo(endDateTime) < 0 && 
          startDateTime.compareTo(entryEndDateTime) < 0)
        {
          // extend by end
          int move = Utils.getDuration(entryEndDateTime, endDateTime);
          if (move > flexibility) move = flexibility;
          // move down scheduleEntry +move
          lastEntry.setStartDateTime(addDateTime(entryStartDateTime, move));
          lastEntry.setEndDateTime(addDateTime(entryEndDateTime, move));
        }
      }
    }
    
    cropScheduleEntries(scheduleEntries, startDateTime, endDateTime);
    
    double days = 0;
    int seconds = 0;
    for (ScheduleEntry scheduleEntry : scheduleEntries)
    {
      days += (double)scheduleEntry.getDuration() /
        (double)scheduleEntry.getDayDuration();
      seconds += scheduleEntry.getDuration();
    }
    dbAbsence.setRequestedDays(days);
    dbAbsence.setRequestedTime(seconds);
    return scheduleEntries;
  }

  private void consolidateAbsenceEntries(DBAbsence dbAbsence,
    List<ScheduleEntry> scheduleEntries)
  {
    DBAbsenceType dbAbsenceType =
      entityManager.find(DBAbsenceType.class, dbAbsence.getAbsenceTypeId());
    String entryTypeId = dbAbsenceType.getEntryTypeId();
    if (entryTypeId == null) return;

    String nowDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    if (nowDateTime.compareTo(dbAbsence.getStartDateTime()) < 0)
    {
      throw new WebServiceException(
        "presence:CAN_NOT_CONSOLIDATE_ABSENCE_IN_THE_FUTURE");
    }    
    int i = scheduleEntries.size() - 1;
    boolean crop = true;
    while (crop && i >= 0) // remove ScheduleEntries in the future
    {
      ScheduleEntry scheduleEntry = scheduleEntries.get(i);
      if (nowDateTime.compareTo(scheduleEntry.getStartDateTime()) < 0)
      {
        scheduleEntries.remove(i);
        i--;
      }
      else if (nowDateTime.compareTo(scheduleEntry.getEndDateTime()) < 0)
      {
        scheduleEntry.setEndDateTime(nowDateTime);
        crop = false;
      }
      else
      {
        crop = false;
      }
    }
    System.out.println("> Schedule entries after crop: " + scheduleEntries);
    int consolidatedTime = 0; // seconds
    double consolidatedDays = 0; // days
    System.out.println("\n\nConsolidate absence:");
    Query query = entityManager.createNamedQuery("findPresenceEntries");
    query.setParameter("personId", dbAbsence.getPersonId());
    query.setParameter("startDateTime", dbAbsence.getStartDateTime());
    query.setParameter("endDateTime", dbAbsence.getEndDateTime());
    query.setParameter("entryTypeId", null);
    query.setParameter("manipulatedValue", null);
    query.setParameter("reason", null);
    List<DBPresenceEntry> dbPresenceEntries = query.getResultList();
    Iterator<DBPresenceEntry> iterp = dbPresenceEntries.iterator();
    Iterator<ScheduleEntry> iters = scheduleEntries.iterator();
    DBPresenceEntry dbPresenceEntry;
    if (iterp.hasNext())
    {
      dbPresenceEntry = iterp.next();
    }
    else
    {
      // worker has no previous entry!!!, create a fake one.
      dbPresenceEntry = new DBPresenceEntry();
      dbPresenceEntry.setPersonId(dbAbsence.getPersonId());
      dbPresenceEntry.setEntryTypeId(getNoWorkPresenceEntryTypeId());
      if (dbPresenceEntry.getEntryTypeId() == null)
        throw new WebServiceException("presence:CAN_NOT_CONSOLIDATE_ABSENCE");

      String startDateTime =
        Utils.addDateTime(dbAbsence.getStartDateTime(), -3600); // -1 hour
      dbPresenceEntry.setStartDateTime(startDateTime);
      query = entityManager.createNamedQuery("findFirstPresenceEntryDateTime");
      query.setParameter("personId", dbAbsence.getPersonId());
      query.setMaxResults(1);
      List<String> result = query.getResultList();
      if (!result.isEmpty())
      {
        String dateTime = result.get(0);
        dbPresenceEntry.setEndDateTime(dateTime);
      }
      dbPresenceEntry.setCreationDateTime(nowDateTime);
      dbPresenceEntry.setChangeDateTime(nowDateTime);
      dbPresenceEntry.setCreationUserId("admin");
      dbPresenceEntry.setChangeUserId("admin");
      dbPresenceEntry.setIpAddress("127.0.0.1");
      updateDuration(dbPresenceEntry);
      dbPresenceEntry.updateEntryId();
      entityManager.persist(dbPresenceEntry);
      entityManager.flush();
    }
    ScheduleEntry scheduleEntry = iters.hasNext() ? iters.next() : null;
    String absenceId = dbAbsence.getAbsenceId();
    String personId = dbAbsence.getPersonId();
    while (dbPresenceEntry != null && scheduleEntry != null)
    {
      if (!isConsolidableEntry(dbPresenceEntry)) // no consolidable, skip
      {
        if (dbAbsence.getAbsenceId().equals(dbPresenceEntry.getAbsenceId()))
        {
          consolidatedTime += dbPresenceEntry.getDuration();
          consolidatedDays += ((double)dbPresenceEntry.getDuration() / 
            (double)scheduleEntry.getDayDuration());
        }
        dbPresenceEntry = iterp.hasNext() ? iterp.next() : null;
      }
      else // consolidable entry
      {
        String as = dbPresenceEntry.getStartDateTime();
        String ae = dbPresenceEntry.getEndDateTime();
        String ss = scheduleEntry.getStartDateTime();
        String se = scheduleEntry.getEndDateTime();
        //System.out.println(as + " " + ae + " - " + ss + " " + se);

        if (ae != null && ss.compareTo(as) <= 0 && ae.compareTo(se) <= 0)
        {
          // case 1: all absence entry is contained in schedule
          // [SSSSSS]
          //   [AA]
          if (!nowDateTime.equals(dbPresenceEntry.getCreationDateTime()))
          {
            // existing entry, preserve entryTypeId to restore later
            String previousEntryTypeId = dbPresenceEntry.getEntryTypeId();
            dbPresenceEntry.setPreviousEntryTypeId(previousEntryTypeId);
            dbPresenceEntry.setAbsenceId(absenceId);
          }
          dbPresenceEntry.setEntryTypeId(entryTypeId);
          updateDuration(dbPresenceEntry);
          consolidatedTime += dbPresenceEntry.getDuration();
          consolidatedDays += ((double)dbPresenceEntry.getDuration() / 
            (double)scheduleEntry.getDayDuration());          
          entityManager.merge(dbPresenceEntry);
          //System.out.println("\nCase 1");
          entityManager.flush();

          dbPresenceEntry = iterp.hasNext() ? iterp.next() : null;
        }
        else if (ss.compareTo(as) <= 0 && (ae == null || se.compareTo(ae) <= 0)
          && as.compareTo(se) < 0)
        {
          // case 2: partial overlap, schedule first
          // [SSSSSS]
          //    [AAAAAA]
          String previousEntryTypeId = dbPresenceEntry.getEntryTypeId();
          if (!nowDateTime.equals(dbPresenceEntry.getCreationDateTime()))
          {
            // existing entry, preserve entryTypeId
            dbPresenceEntry.setPreviousEntryTypeId(previousEntryTypeId);
            dbPresenceEntry.setAbsenceId(absenceId);
          }
          dbPresenceEntry.setEntryTypeId(entryTypeId);
          dbPresenceEntry.setEndDateTime(se);
          updateDuration(dbPresenceEntry);
          consolidatedTime += dbPresenceEntry.getDuration();
          consolidatedDays += ((double)dbPresenceEntry.getDuration() / 
            (double)scheduleEntry.getDayDuration());
          entityManager.merge(dbPresenceEntry);
          //System.out.println("\nCase 2");
          entityManager.flush();

          dbPresenceEntry = new DBPresenceEntry();
          dbPresenceEntry.setAbsenceId(absenceId);
          dbPresenceEntry.setPersonId(personId);
          dbPresenceEntry.setEntryTypeId(previousEntryTypeId);
          dbPresenceEntry.setStartDateTime(se);
          dbPresenceEntry.setEndDateTime(ae);
          dbPresenceEntry.setCreationDateTime(nowDateTime);
          dbPresenceEntry.setChangeDateTime(nowDateTime);
          dbPresenceEntry.updateEntryId();
          updateDuration(dbPresenceEntry);

          scheduleEntry = iters.hasNext() ? iters.next() : null;
        }
        else if (ae != null && as.compareTo(ss) <= 0 &&
          ae.compareTo(se) <= 0 && ss.compareTo(ae) < 0)
        {
          // case 3: partial overlap, absence first
          //    [SSSSSS]
          // [AAAAAA]
          dbPresenceEntry.setEndDateTime(ss);
          updateDuration(dbPresenceEntry);
          entityManager.merge(dbPresenceEntry);
          //System.out.println("\nCase 3.1");
          entityManager.flush();

          dbPresenceEntry = new DBPresenceEntry();
          dbPresenceEntry.setPersonId(personId);
          dbPresenceEntry.setAbsenceId(absenceId);
          dbPresenceEntry.setEntryTypeId(entryTypeId);
          dbPresenceEntry.setStartDateTime(ss);
          dbPresenceEntry.setEndDateTime(ae);
          dbPresenceEntry.setCreationDateTime(nowDateTime);
          dbPresenceEntry.setChangeDateTime(nowDateTime);
          dbPresenceEntry.updateEntryId();
          updateDuration(dbPresenceEntry);
          consolidatedTime += dbPresenceEntry.getDuration();
          consolidatedDays += ((double)dbPresenceEntry.getDuration() / 
            (double)scheduleEntry.getDayDuration());
          entityManager.merge(dbPresenceEntry);
          //System.out.println("\nCase 3.2");
          entityManager.flush();

          dbPresenceEntry = iterp.hasNext() ? iterp.next() : null;
        }
        else if (as.compareTo(ss) <= 0 && (ae == null || se.compareTo(ae) <= 0))
        {
          // case 4: absence entry cointains schedule
          //   [SS]
          // [AAAAAA]
          dbPresenceEntry.setEndDateTime(ss);
          updateDuration(dbPresenceEntry);
          DBPresenceEntry dbPrev = entityManager.merge(dbPresenceEntry);
          //System.out.println("\nCase 4.1");
          entityManager.flush();

          dbPresenceEntry = new DBPresenceEntry();
          dbPresenceEntry.setPersonId(personId);
          dbPresenceEntry.setAbsenceId(absenceId);
          dbPresenceEntry.setEntryTypeId(entryTypeId);
          dbPresenceEntry.setStartDateTime(ss);
          dbPresenceEntry.setEndDateTime(se);
          dbPresenceEntry.setCreationDateTime(nowDateTime);
          dbPresenceEntry.setChangeDateTime(nowDateTime);
          dbPresenceEntry.updateEntryId();
          updateDuration(dbPresenceEntry);
          consolidatedTime += dbPresenceEntry.getDuration();
          consolidatedDays += ((double)dbPresenceEntry.getDuration() / 
            (double)scheduleEntry.getDayDuration());
          entityManager.merge(dbPresenceEntry);
          //System.out.println("\nCase 4.2");
          entityManager.flush();

          dbPresenceEntry = new DBPresenceEntry();
          dbPresenceEntry.setPersonId(personId);
          dbPresenceEntry.setAbsenceId(absenceId);
          dbPresenceEntry.setStartDateTime(se);
          dbPresenceEntry.setEndDateTime(ae);
          dbPresenceEntry.setEntryTypeId(dbPrev.getEntryTypeId());
          dbPresenceEntry.setReason(dbPrev.getReason());
          dbPresenceEntry.setManipulated(false);
          dbPresenceEntry.setCreationDateTime(nowDateTime);
          dbPresenceEntry.setChangeDateTime(nowDateTime);
          dbPresenceEntry.updateEntryId();
          updateDuration(dbPresenceEntry);

          scheduleEntry = iters.hasNext() ? iters.next() : null;
        }
        else
        {
          // no overlap, advance the earlier period
          entityManager.merge(dbPresenceEntry);
          //System.out.println("\nCase 5");
          entityManager.flush();

          if (ss.compareTo(as) < 0)
          {
            scheduleEntry = iters.hasNext() ? iters.next() : null;
          }
          else
          {
            dbPresenceEntry = iterp.hasNext() ? iterp.next() : null;
          }
        }
      }
    }
    if (dbPresenceEntry != null)
    {
      entityManager.merge(dbPresenceEntry);
      //System.out.println("\nCase 6");
      entityManager.flush();
    }
    dbAbsence.setConsolidatedTime(consolidatedTime);
    dbAbsence.setConsolidatedDays(consolidatedDays);
  }

  private void updateCounter(DBAbsence dbAbsence, double factor)
    throws WebServiceException
  {
    DBAbsenceType dbAbsenceType =
      entityManager.find(DBAbsenceType.class, dbAbsence.getAbsenceTypeId());
    if (dbAbsenceType.getDefaultTime() < 0) return; // no counters for that type

    String absenceTypeId = dbAbsence.getAbsenceTypeId();
    String personId = dbAbsence.getPersonId();
    String year = getAbsencesYear(dbAbsence.getStartDateTime());
    DBAbsenceCounterPK pk =
      new DBAbsenceCounterPK(personId, year, absenceTypeId);
    DBAbsenceCounter dbAbsenceCounter =
      entityManager.find(DBAbsenceCounter.class, pk);
    if (dbAbsenceCounter == null)
      throw new WebServiceException("presence:ABSENCE_REQUEST_REJECTED");

    // Update for consistency
    if (dbAbsence.getRequestedTime() == dbAbsence.getConsolidatedTime())
    {
      dbAbsence.setConsolidatedDays(dbAbsence.getRequestedDays());
    }
    
    double remainingTime = dbAbsenceCounter.getRemainingTime();
    if (dbAbsence.getStatus().equals(CONSOLIDATED_STATUS)) // Consolidated
    {
      if (dbAbsenceType.getCounting().equals(AbsenceCounting.HOURS))
      {
        double hours = (double)dbAbsence.getConsolidatedTime() / 3600.0;
        remainingTime +=  hours * factor;
      }
      else // DAYS
      {
        remainingTime += dbAbsence.getConsolidatedDays() * factor;      
      }
    }
    else
    {
      if (dbAbsenceType.getCounting().equals(AbsenceCounting.HOURS))
      {
        double hours = (double)dbAbsence.getRequestedTime() / 3600.0;
        remainingTime +=  hours * factor;
      }
      else // DAYS
      {
        remainingTime += dbAbsence.getRequestedDays() * factor;      
      }
    }
    if (remainingTime < -0.000001)
      throw new WebServiceException("presence:NOT_ENOUGH_TIME");
    if (remainingTime < 0) remainingTime = 0;
    dbAbsenceCounter.setRemainingTime(remainingTime);
    entityManager.merge(dbAbsenceCounter);
  }

  private void lockWorker(String personId)
  {
    Query query = entityManager.createNamedQuery("lockWorker");
    query.setParameter("personId", personId);
    query.executeUpdate();
    entityManager.flush();
  }

  private String getAbsencesYear(String date)
  {
    return getAbsencesYear(date, getCountersInitDay());
  }

  private String getAbsencesYear(String date, String initMonthDay)
  {
    int year = Integer.parseInt(date.substring(0, 4));
    String monthDay = date.substring(4);
    if (monthDay.compareTo(initMonthDay) < 0)
    {
      return String.valueOf(year - 1);
    }
    else return String.valueOf(year);
  }

  private String getNoWorkPresenceEntryTypeId()
  {
    DBPresenceParameter dbParameter =
      entityManager.find(DBPresenceParameter.class, NO_WORK_ENTRY_TYPE_ID_PARAM);
    if (dbParameter == null) return null;
    return dbParameter.getValue();
  }
  
  private String getCountersInitDay()
  {
    String initMonthDay = null;
    DBPresenceParameter dbParameter =
      entityManager.find(DBPresenceParameter.class, COUNTERS_INIT_DAY_PARAM);
    if (dbParameter != null)
    {
      initMonthDay = dbParameter.getValue();
    }
    if (StringUtils.isBlank(initMonthDay) || initMonthDay.length() != 4)
      initMonthDay = "0101";

    return initMonthDay;
  }

  private boolean isValidIpAddress(String ipAddress)
  {
    String ipPattern = null;
    DBPresenceParameter dbParameter =
      entityManager.find(DBPresenceParameter.class, VALID_IP_ADDRESS_PARAM);
    if (dbParameter != null)
    {
      ipPattern = dbParameter.getValue();
    }
    if (StringUtils.isBlank(ipPattern)) return true;
    return ipAddress.matches(ipPattern);
  }

  private boolean isCompensableIpAddress(String ipAddress)
  {
    String noCompensableAddresses = null;
    DBPresenceParameter dbParameter =
      entityManager.find(DBPresenceParameter.class, NO_COMPENSABLE_IP_ADDRESSES_PARAM);
    if (dbParameter != null)
    {
      noCompensableAddresses = dbParameter.getValue();
    }
    if (StringUtils.isBlank(noCompensableAddresses)) return true;
    String[] ipAddresses = noCompensableAddresses.split(",");
    boolean compensable = true;
    int i = 0;
    while (compensable && i < ipAddresses.length)
    {
      compensable = !ipAddresses[i].trim().equals(ipAddress);
      i++;
    }
    return compensable;
  }

  private int getCompensationMinRestHours()
  {
    String compensationMinHours = null;
    DBPresenceParameter dbParameter =
      entityManager.find(DBPresenceParameter.class, COMPENSATION_MIN_REST_HOURS_PARAM);
    if (dbParameter != null)
    {
      compensationMinHours = dbParameter.getValue();
    }
    if (!StringUtils.isBlank(compensationMinHours))
    {
      try
      {
        return Integer.parseInt(compensationMinHours);
      }
      catch (NumberFormatException ex)
      {
      }
    }
    return -1;
  }

  private boolean isEntryEditionEnabled(String dateTime)
  {
    String sdays = null;
    DBPresenceParameter dbParameter =
      entityManager.find(DBPresenceParameter.class, ENTRY_EDITION_DAYS_PARAM);
    if (dbParameter != null)
    {
      sdays = dbParameter.getValue();
    }
    if (StringUtils.isBlank(sdays)) return true;
    try
    {
      int maxSeconds = Integer.parseInt(sdays) * 24 * 3600;
      String nowDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
      int ellapsed = Utils.getDuration(dateTime, nowDateTime);
      return ellapsed < maxSeconds;
    }
    catch (NumberFormatException ex)
    {
      return true;
    }
  }

  public static KernelManagerPort getKernelPort()
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(KernelManagerService.class);
    return endpoint.getPort(KernelManagerPort.class,
      MatrixConfig.getProperty("adminCredentials.userId"),
      MatrixConfig.getProperty("adminCredentials.password"));
  }

  private String getFullName(Person person)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(person.getName());
    if (person.getFirstParticle() != null)
    {
      buffer.append(" ").append(person.getFirstParticle());
    }
    if (person.getFirstSurname() != null)
    {
      buffer.append(" ").append(person.getFirstSurname());
    }
    if (person.getSecondParticle() != null)
    {
      buffer.append(" ").append(person.getSecondParticle());
    }
    if (person.getSecondSurname() != null)
    {
      buffer.append(" ").append(person.getSecondSurname());
    }
    return buffer.toString();
  }

  private int getCompensationTime(DBPresenceEntry dbPresenceEntry,
    DBPresenceEntry dbPresenceEntryPrev)
  {
    int compensationTime = 0;

    if (!dbPresenceEntry.isManipulated() && isWorkEntry(dbPresenceEntry))
    {
      int compensationMinRestHours = getCompensationMinRestHours();
      if (compensationMinRestHours >= 0)
      {
        if (dbPresenceEntryPrev == null ||
          (!isWorkEntry(dbPresenceEntryPrev) &&
           dbPresenceEntryPrev.getCurrentDuration() >
           compensationMinRestHours * 3600))
        {
          String personId = dbPresenceEntry.getPersonId();
          DBWorker dbWorker = entityManager.find(DBWorker.class, personId);
          if (dbWorker != null)
          {
            compensationTime = dbWorker.getCompensationTime();
          }
        }
      }
    }
    return compensationTime;
  }

  private boolean isWorkEntry(PresenceEntry presenceEntry)
  {
    String entryTypeId = presenceEntry.getEntryTypeId();
    DBPresenceEntryType dbEntryType =
      entityManager.find(DBPresenceEntryType.class, entryTypeId);
    return dbEntryType != null && dbEntryType.getMaxWorkedTime() > 0;
  }

  private boolean isConsolidableEntry(PresenceEntry presenceEntry)
  {
    String entryTypeId = presenceEntry.getEntryTypeId();
    DBPresenceEntryType dbEntryType =
      entityManager.find(DBPresenceEntryType.class, entryTypeId);
    return dbEntryType != null && dbEntryType.isConsolidable();
  }

  private boolean isAbsenceEntry(PresenceEntry presenceEntry)
  {
    String entryTypeId = presenceEntry.getEntryTypeId();
    DBPresenceEntryType dbEntryType =
      entityManager.find(DBPresenceEntryType.class, entryTypeId);
    return dbEntryType != null && dbEntryType.isAbsence();    
  }
  
  private boolean isEntryTypeEnabled(PresenceEntry presenceEntry)
  {
    String entryTypeId = presenceEntry.getEntryTypeId();
    DBPresenceEntryType dbEntryType =
      entityManager.find(DBPresenceEntryType.class, entryTypeId);
    return dbEntryType.isEnabled();
  }

  private boolean isRealWorkEntry(PresenceEntry presenceEntry)
  {
    String entryTypeId = presenceEntry.getEntryTypeId();
    DBPresenceEntryType dbEntryType =
      entityManager.find(DBPresenceEntryType.class, entryTypeId);
    return dbEntryType != null && dbEntryType.isRealWork();
  }

  private ScheduleEntry duplicateScheduleEntry(ScheduleEntry scheduleEntry)
  {
    ScheduleEntry duplicated = new ScheduleEntry();
    duplicated.setDayTypeId(scheduleEntry.getDayTypeId());
    duplicated.setStartDateTime(scheduleEntry.getStartDateTime());
    duplicated.setEndDateTime(scheduleEntry.getEndDateTime());
    duplicated.setDuration(scheduleEntry.getDuration());
    duplicated.setInitialDayDuration(scheduleEntry.getInitialDayDuration());
    duplicated.setDayDuration(scheduleEntry.getDayDuration());
    duplicated.getReductionId().addAll(scheduleEntry.getReductionId());
    duplicated.setFlexibility(scheduleEntry.getFlexibility());
    return duplicated;
  }

  private void checkValidationLoop(String validatorId,
    Collection<String> personIds)
  {
    if (StringUtils.isBlank(validatorId)) return;

    if (personIds.contains(validatorId))
      throw new WebServiceException("presence:VALIDATION_LOOP");

    personIds.add(validatorId);

    DBWorker dbWorker = entityManager.find(DBWorker.class, validatorId);
    if (dbWorker == null)
      throw new WebServiceException("presence:WORKER_NOT_FOUND");

    checkValidationLoop(dbWorker.getValidatorPersonId(), personIds);
  }

  private String truncate(String value, int length)
  {
    if (value != null)
    {
      if (value.length() > length)
      {
        value = value.substring(0, length);
      }
    }
    return value;
  }
}
