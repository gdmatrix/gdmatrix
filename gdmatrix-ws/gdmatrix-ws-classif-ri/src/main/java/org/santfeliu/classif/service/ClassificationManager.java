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
package org.santfeliu.classif.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.matrix.classif.Class;
import org.matrix.classif.ClassFilter;
import org.matrix.classif.ClassificationConstants;
import org.matrix.classif.ClassificationManagerPort;
import org.matrix.dic.Property;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.WSTypeValidator;
import org.santfeliu.jpa.JPA;
import org.santfeliu.jpa.JPAUtils;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.ws.WSUtils;

/**
 *
 * @author realor
 */
@WebService(endpointInterface = "org.matrix.classif.ClassificationManagerPort")
@HandlerChain(file="handlers.xml")
@JPA
public class ClassificationManager implements ClassificationManagerPort
{
  @Resource
  WebServiceContext wsContext;
  private WSEndpoint endpoint;

  @PersistenceContext
  public EntityManager entityManager;

  protected static final Logger log = Logger.getLogger("Classification");
  protected static final String FINAL_DATE_TIME = "99991231235959";

  private static final String PERIOD_SEQUENCE_NAME = "classif.period";

  public Class loadClass(String classId, String dateTime)
  {
    if (classId == null)
      throw new WebServiceException("classif:CLASSID_IS_MANDATORY");

    classId = getWSEndpoint().toLocalId(Class.class, classId);

    // load current period
    DBClass dbClass = entityManager.find(DBClass.class, classId);
    if (dbClass == null)
      throw new WebServiceException("classif:CLASS_NOT_FOUND");

    // find last period
    Query query = entityManager.createNamedQuery("findClassPeriodAtDateTime");
    query.setParameter("classId", classId);
    if (dateTime == null) dateTime = getCurrentDateTime();
    query.setParameter("dateTime", dateTime);
    query.setFirstResult(0);
    query.setMaxResults(1);
    try
    {
      DBClassPeriod dbClassPeriod = (DBClassPeriod)query.getSingleResult();
      Class classObject = new Class();
      dbClass.copyTo(classObject);
      dbClassPeriod.copyTo(classObject);
      loadProperties(classObject.getProperty(), dbClassPeriod);
      classObject = getWSEndpoint().toGlobal(Class.class, classObject);
      updateClassTypeId(classObject);
      return classObject;
    }
    catch (NoResultException ex)
    {
      // class has no periods
      throw new WebServiceException("classif:CLASS_NOT_FOUND");
    }
  }

  public Class storeClass(Class classObject)
  {
    classObject = getWSEndpoint().toLocal(Class.class, classObject);

    // check security
    User user = UserCache.getUser(wsContext);
    if (!user.isInRole(ClassificationConstants.CLASSIF_ADMIN_ROLE))
      throw new WebServiceException("ACTION_DENIED");

    String classId = classObject.getClassId();
    String title = classObject.getTitle();
    String classTypeId = classObject.getClassTypeId();
    String superClassId = classObject.getSuperClassId();
    String startDateTime = classObject.getStartDateTime();
    String endDateTime = classObject.getEndDateTime();
    String currentDateTime = getCurrentDateTime();

    if (classId == null || classId.trim().length() == 0)
      throw new WebServiceException("classif:CLASSID_IS_MANDATORY");

    if (classTypeId == null || classTypeId.trim().length() == 0)
      throw new WebServiceException("classif:CLASSTYPEID_IS_MANDATORY");

    if (startDateTime == null || startDateTime.trim().length() == 0)
      throw new WebServiceException("classif:STARTDATETIME_IS_MANDATORY");

    if (endDateTime == null || endDateTime.trim().length() == 0)
      endDateTime = FINAL_DATE_TIME;

    if (endDateTime.compareTo(startDateTime) <= 0)
      throw new WebServiceException("classif:INVALID_PERIOD_DATES");

    validateDicProperties(classObject);

    DBClass dbClass = entityManager.find(DBClass.class, classId);
    boolean newClass = (dbClass == null);
    // store DBClass
    if (newClass)
    {
      // new class
      log.log(Level.INFO, "NEW CLASS: {0}", new Object[]{classId});
      dbClass = new DBClass();
      dbClass.copyFrom(classObject);
      dbClass.setCreationDateTime(currentDateTime);
      dbClass.setCreationUserId(user.getUserId());
      dbClass.setChangeDateTime(currentDateTime);
      entityManager.persist(dbClass);
      entityManager.flush();
    }
    else
    {
      // update changeDateTime class
      dbClass.setChangeDateTime(currentDateTime);
      entityManager.merge(dbClass);
    }

    // update class period
    if (title != null && title.trim().length() > 0) // add or modify period
    {
      // check path
      if (superClassId != null)
      {
        HashSet ancestors = new HashSet();
        ancestors.add(classId);
        checkPath(superClassId, ancestors, startDateTime, endDateTime);
      }

      DBClassPeriod dbClassPeriod;
      DBClassPeriodPK pk = new DBClassPeriodPK(classId, startDateTime);
      dbClassPeriod = entityManager.find(DBClassPeriod.class, pk);
      if (dbClassPeriod != null &&
        dbClassPeriod.getStartDateTime().equals(startDateTime) &&
        dbClassPeriod.getEndDateTime().equals(endDateTime))
      {
        // period dates have not changed
        dbClassPeriod.copyFrom(classObject);
        dbClassPeriod.setStartDateTime(startDateTime);
        dbClassPeriod.setEndDateTime(endDateTime);
        dbClassPeriod.setChangeDateTime(currentDateTime);
        dbClassPeriod.setChangeUserId(user.getUserId());
        entityManager.merge(dbClassPeriod);
        entityManager.flush();
        storeProperties(classObject.getProperty(), dbClassPeriod);
      }
      else
      {
        // period dates have change
        makeClassPeriodHole(classId, startDateTime, endDateTime);
        // insert class period
        dbClassPeriod = new DBClassPeriod();
        dbClassPeriod.copyFrom(classObject);
        dbClassPeriod.setStartDateTime(startDateTime);
        dbClassPeriod.setEndDateTime(endDateTime);
        dbClassPeriod.setChangeDateTime(currentDateTime);
        dbClassPeriod.setChangeUserId(user.getUserId());
        entityManager.persist(dbClassPeriod);
        entityManager.flush();
        storeProperties(classObject.getProperty(), dbClassPeriod);
      }
      entityManager.flush();

      dbClass.copyTo(classObject);
      dbClassPeriod.copyTo(classObject);
    }
    else // remove class period
    {
      checkTerminalClass(classId, startDateTime, endDateTime);
      makeClassPeriodHole(classId, startDateTime, endDateTime);
      classObject = new Class();
      classObject.setClassId(classId);
      classObject.setClassTypeId("Class");
    }
    classObject = getWSEndpoint().toGlobal(Class.class, classObject);
    updateClassTypeId(classObject);
    return classObject;
  }

  public boolean removeClass(String classId)
  {
    if (classId == null)
      throw new WebServiceException("classif:CLASSID_IS_MANDATORY");

    classId = getWSEndpoint().toLocalId(Class.class, classId);

    User user = UserCache.getUser(wsContext);
    if (!user.isInRole(ClassificationConstants.CLASSIF_ADMIN_ROLE))
      throw new WebServiceException("ACTION_DENIED");

    try
    {
      DBClass dbClass = entityManager.getReference(DBClass.class, classId);

      Query query = entityManager.createNamedQuery("findSubClassPeriods");
      query.setParameter("superClassId", classId);
      query.setFirstResult(0);
      query.setMaxResults(1);
      if (query.getResultList().size() > 0)
        throw new WebServiceException("classif:NOT_TERMINAL_CLASS");

      // remove class periods
      query = entityManager.createNamedQuery("removeClassPeriods");
      query.setParameter("classId", classId);
      query.executeUpdate();

      // update class changeDateTime to refresh cache
      String currentDateTime = getCurrentDateTime();
      dbClass.setChangeDateTime(currentDateTime);
      entityManager.merge(dbClass);
      return true;
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
  }

  public int countClasses(ClassFilter filter)
  {
    filter = getWSEndpoint().toLocal(ClassFilter.class, filter);
    Query query = entityManager.createNamedQuery("countClasses");
    applyFilterParameters(filter, query);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  public List<Class> findClasses(ClassFilter filter)
  {
    filter = getWSEndpoint().toLocal(ClassFilter.class, filter);
    List<Class> classList = new ArrayList<Class>();
    Query query = entityManager.createNamedQuery("findClasses");
    applyFilterParameters(filter, query);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List result = query.getResultList();
    for (Object row : result)
    {
      Object[] c = (Object[])row;
      DBClass dbClass = (DBClass)c[0];
      DBClassPeriod dbClassPeriod = (DBClassPeriod)c[1];
      org.matrix.classif.Class classObject = new org.matrix.classif.Class();
      dbClass.copyTo(classObject);
      dbClassPeriod.copyTo(classObject);
      classObject = getWSEndpoint().toGlobal(Class.class, classObject);
      updateClassTypeId(classObject);
      classList.add(classObject);
    }
    return classList;
  }

  public List<String> listModifiedClasses(String dateTime1, String dateTime2)
  {
    Query query = entityManager.createNamedQuery("listModifiedClasses");
    query.setParameter("dateTime1", dateTime1);
    query.setParameter("dateTime2", dateTime2);
    return query.getResultList();
  }

  // ************** private methods *****************

  private void validateDicProperties(Class classObject)
  {
    String classTypeId = classObject.getClassTypeId();
    classTypeId =
      getWSEndpoint().toGlobalId(org.matrix.dic.Type.class, classTypeId);
    Type type = TypeCache.getInstance().getType(classTypeId);
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(classObject, "classId");
  }

  private void loadProperties(List<Property> properties,
    DBClassPeriod dbClassPeriod)
  {
    System.out.println(">>>>>>>>>> Load properties ");

    Integer periodId = dbClassPeriod.getPeriodId();
    if (periodId == null) return;

    Query query = entityManager.createNamedQuery("findClassPeriodPropertyValues");
    query.setParameter("periodId", periodId);
    List<DBClassPeriodPropertyValue> pvs = query.getResultList();
    String previousName = "";
    Property property = null;
    properties.clear();
    for (DBClassPeriodPropertyValue pv : pvs)
    {
      System.out.println("pv >>>>>> " + pv.getName() + " " + pv.getValue());
      String name = pv.getName();
      if (!previousName.equals(name))
      {
        previousName = name;
        property = new Property();
        property.setName(name);
        properties.add(property);
      }
      property.getValue().add(pv.getValue());
    }
    System.out.println(">>>>>>>>>>>>>>> " + properties);
  }

  private void storeProperties(List<Property> properties,
    DBClassPeriod dbClassPeriod)
  {
    System.out.println(">>>>>>>>>> Store properties " + properties);

    Integer periodId = dbClassPeriod.getPeriodId();
    if (properties.isEmpty() && periodId == null) return;

    if (periodId == null) // no properties assigned yet
    {
      System.out.println(">>>> increment counter");
      Query query = entityManager.createNamedQuery("incrementCounter");
      query.setParameter("name", PERIOD_SEQUENCE_NAME);
      int modified = query.executeUpdate();
      entityManager.flush();

      if (modified == 1)
      {
        System.out.println(">>>> read counter");
        DBCounter counter =
          entityManager.find(DBCounter.class, PERIOD_SEQUENCE_NAME);        
        if (counter == null) throw new RuntimeException("Can't read counter");
        entityManager.refresh(counter);
        periodId = counter.getValue();
        System.out.println(">>>>>>>>>>> COUNTER VALUE:" + periodId);
      }
      else // create counter
      {
        System.out.println(">>>> create counter");
        periodId = new Integer(0); // initial value
        DBCounter counter = new DBCounter();
        counter.setName(PERIOD_SEQUENCE_NAME);
        counter.setValue(periodId);
        entityManager.persist(counter);
      }
      dbClassPeriod.setPeriodId(periodId);
      entityManager.merge(dbClassPeriod);
    }
    else
    {
      System.out.println(">>>> remove old properties");
      // remove old values
      Query query = entityManager.createNamedQuery("findClassPeriodPropertyValues");
      query.setParameter("periodId", periodId);
      List<DBClassPeriodPropertyValue> pvl = query.getResultList();
      for (DBClassPeriodPropertyValue pv : pvl)
      {
        System.out.println(">>>> remove old property " + pv.getName());
        entityManager.remove(pv);
      }
    }
    entityManager.flush();

    System.out.println(">>>> saving properties " + properties +
      " for periodId:" + periodId);
    for (Property property : properties)
    {
      String name = property.getName();      
      int index = 0;
      for (String value : property.getValue())
      {
        if (value != null)
        {
          System.out.println(
            ">>>> save property " + name + "[" + index + "]=" + value);
          DBClassPeriodPropertyValue pv = new DBClassPeriodPropertyValue();
          pv.setPeriodId(periodId);
          pv.setName(name);
          pv.setIndex(index++);
          pv.setValue(value);
          entityManager.persist(pv);
        }
      }
    }
  }

  private void makeClassPeriodHole(String classId,
    String startDateTime, String endDateTime)
  {
    // enveloping period
    Query query;
    query = entityManager.createNamedQuery("findEnvelopingClassPeriod");
    query.setParameter("classId", classId);
    query.setParameter("startDateTime", startDateTime);
    query.setParameter("endDateTime", endDateTime);
    List<DBClassPeriod> periods = query.getResultList();
    if (periods.size() > 0)
    {
      // split period in 2 parts
      DBClassPeriod periodPart1 = periods.get(0);
      DBClassPeriod periodPart2 = new DBClassPeriod();
      JPAUtils.copy(periodPart1, periodPart2);      

      periodPart1.setEndDateTime(startDateTime);
      entityManager.merge(periodPart1);

      periodPart2.setStartDateTime(endDateTime);
      periodPart2.setPeriodId(null); // force new periodId
      entityManager.persist(periodPart2);
      entityManager.flush();
      List<Property> properties = new ArrayList<Property>();
      loadProperties(properties, periodPart1);
      storeProperties(properties, periodPart2);
    }
    else
    {
      // update previous period
      query = entityManager.createNamedQuery("cropPreviousClassPeriod");
      query.setParameter("classId", classId);
      query.setParameter("dateTime", startDateTime);
      query.executeUpdate();
      
      // update next period
      query = entityManager.createNamedQuery("cropNextClassPeriod");
      query.setParameter("classId", classId);
      query.setParameter("dateTime", endDateTime);
      query.executeUpdate();

      // remove enveloped periods
      query = entityManager.createNamedQuery("removeEnvelopedClassPeriods");
      query.setParameter("classId", classId);
      query.setParameter("startDateTime", startDateTime);
      query.setParameter("endDateTime", endDateTime);
      query.executeUpdate();
    }
  }

  private void checkPath(String classId, HashSet ancestors,
    String startDateTime, String endDateTime)
  {
    if (ancestors.contains(classId))
      throw new WebServiceException("classif:CYCLIC_CLASS_DEFINITION");
    
    Query query = entityManager.createNamedQuery("findOverlappedClassPeriods");
    query.setParameter("classId", classId);
    query.setParameter("startDateTime", startDateTime);
    query.setParameter("endDateTime", endDateTime);
    List<DBClassPeriod> periods = query.getResultList();
    Iterator<DBClassPeriod> iter = periods.iterator();
    if (iter.hasNext())
    {
      do
      {
        DBClassPeriod period = iter.next();
        String superClassId = period.getSuperClassId();
        if (superClassId != null)
        {
          HashSet newAncestors = new HashSet();
          newAncestors.addAll(ancestors);
          newAncestors.add(classId);
          String dateTime1 = max(startDateTime, period.getStartDateTime());
          String dateTime2 = min(endDateTime, period.getEndDateTime());
          checkPath(superClassId, newAncestors, dateTime1, dateTime2);
        }
      } while (iter.hasNext());
    }
    else throw new WebServiceException("classif:SUPER_CLASS_NOT_FOUND");
  }

  private void checkTerminalClass(String classId,
    String startDateTime, String endDateTime)
  {
    Query query = entityManager.createNamedQuery("findOverlappedSubClassPeriods");
    query.setParameter("superClassId", classId);
    query.setParameter("startDateTime", startDateTime);
    query.setParameter("endDateTime", endDateTime);
    query.setFirstResult(0);
    query.setMaxResults(1);
    if (query.getResultList().size() > 0)
      throw new WebServiceException("classif:NOT_TERMINAL_CLASS");
  }

  private String addWildcards(String value)
  {
    if (value == null || value.length() == 0) return null;
    return "%" + value.toUpperCase() + "%";
  }

  private String getCurrentDateTime()
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    return df.format(new Date());
  }

  private void applyFilterParameters(ClassFilter filter, Query query)
  {
    query.setParameter("classId", filter.getClassId());
    query.setParameter("startDateTime", filter.getStartDateTime());
    query.setParameter("endDateTime", filter.getEndDateTime());
    query.setParameter("superClassId", filter.getSuperClassId());
    query.setParameter("title", addWildcards(filter.getTitle()));
    query.setParameter("description", addWildcards(filter.getDescription()));
  }

  private String min(String dateTime1, String dateTime2)
  {
    return (dateTime1.compareTo(dateTime2) > 0) ? dateTime2 : dateTime1;
  }

  private String max(String dateTime1, String dateTime2)
  {
    return (dateTime1.compareTo(dateTime2) > 0) ? dateTime1 : dateTime2;
  }

  private WSEndpoint getWSEndpoint()
  {
    if (endpoint == null)
    {
      String endpointName = WSUtils.getServletAdapter(wsContext).getName();
      endpoint = WSDirectory.getInstance().getEndpoint(endpointName);
    }
    return endpoint;
  }

  private void updateClassTypeId(Class classObject)
  {
    if (classObject.getClassTypeId().endsWith(":Class"))
      classObject.setClassTypeId("Class");
  }
}
