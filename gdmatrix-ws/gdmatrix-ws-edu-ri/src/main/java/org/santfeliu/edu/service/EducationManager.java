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
package org.santfeliu.edu.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import javax.xml.ws.WebServiceContext;

import javax.xml.ws.WebServiceException;
import org.matrix.edu.Course;
import org.matrix.edu.CourseFilter;
import org.matrix.edu.CourseView;
import org.matrix.edu.EducationManagerPort;
import org.matrix.edu.Inscription;
import org.matrix.edu.InscriptionFilter;
import org.matrix.edu.InscriptionView;
import org.matrix.edu.Property;
import org.matrix.edu.School;
import org.matrix.edu.SchoolFilter;
import org.matrix.edu.EducationMetaData;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.KernelManagerService;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.PersonView;

import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.jpa.JPA;
import org.santfeliu.jpa.JPAQuery;
import org.santfeliu.jpa.JPAUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.ws.WSExceptionFactory;


/**
 *
 * @author unknown
 */
@WebService(endpointInterface = "org.matrix.edu.EducationManagerPort")
@HandlerChain(file="handlers.xml")
@JPA
public class EducationManager implements EducationManagerPort
{
  @Resource
  WebServiceContext wsContext;
  
  @PersistenceContext
  public EntityManager entityManager;

  protected static final Logger log = Logger.getLogger("Edu");

  private static final int COURSE_CODE_MAX_SIZE = 20;
  private static final int COURSE_NAME_MAX_SIZE = 50;
  private static final int INSCRIPTION_CODE_MAX_SIZE = 20;
  private static final int INSCRIPTION_PROPERTY_VALUE_MAX_SIZE = 200;

  public EducationManager()
  {
  }

  public EducationMetaData getEducationMetaData()
  {
    EducationMetaData metaData = new EducationMetaData();
    metaData.setCourseCodeMaxSize(COURSE_CODE_MAX_SIZE);
    metaData.setCourseNameMaxSize(COURSE_NAME_MAX_SIZE);
    metaData.setInscriptionCodeMaxSize(INSCRIPTION_CODE_MAX_SIZE);
    metaData.setInscriptionPropertyValueMaxSize(
      INSCRIPTION_PROPERTY_VALUE_MAX_SIZE);
    return metaData;
  }

  public School loadSchool(String schoolId)
  {
    DBSchool dbSchool = entityManager.find(DBSchool.class, schoolId);
    if (dbSchool == null)
      throw new WebServiceException("edu:SCHOOL_NOT_FOUND");
    return dbSchool;
  }

  public School storeSchool(School school)
  {
    DBSchool dbSchool = new DBSchool();
    JPAUtils.copy(school, dbSchool);

    String schoolId = school.getSchoolId();
    if (schoolId == null) // insert
    {
      entityManager.persist(dbSchool);
    }
    else // update
    {
      school = entityManager.merge(dbSchool);
    }
    return dbSchool;
  }

  public boolean removeSchool(String schoolId)
  {
    try
    {
      DBSchool dbSchool = entityManager.getReference(DBSchool.class, schoolId);
      entityManager.remove(dbSchool);
      return true;
    }
    catch (Exception ex)
    {
      return true;
    }
  }

  public List findSchools(SchoolFilter filter)
  {
    Query query = entityManager.createNamedQuery("findSchools");
    setSchoolFilterParameters(query, filter);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    return query.getResultList();
  }

  public int countSchools(SchoolFilter filter)
  {
    Query query = entityManager.createNamedQuery("findSchools");
    setSchoolFilterParameters(query, filter);
    Number number = (Number)query.getSingleResult();
    return number.intValue();
  }

  public Course loadCourse(String courseId)
  {
    DBCourse dbCourse = entityManager.find(DBCourse.class, courseId);
    if (dbCourse == null)
      throw new WebServiceException("edu:COURSE_NOT_FOUND");
    return dbCourse;
  }

  public Course storeCourse(Course course)
  {
    validateCourse(course);

    DBCourse dbCourse = new DBCourse();
    JPAUtils.copy(course, dbCourse);

    String courseId = course.getCourseId();
    if (courseId == null) // insert
    {
      entityManager.persist(dbCourse);
    }
    else // update
    {
      course = entityManager.merge(dbCourse);
    }
    return dbCourse;
  }

  public boolean removeCourse(String courseId)
  {
    try
    {
      DBCourse dbCourse = entityManager.getReference(DBCourse.class, courseId);
      entityManager.remove(dbCourse);
      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public List findCourseViews(CourseFilter filter)
  {
    List<CourseView> result = new ArrayList<CourseView>();
    Query query = entityManager.createNamedQuery("findCourses");
    setCourseFilterParameters(query, filter);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<Object[]> list = query.getResultList();
    for (Object[] row : list)
    {
      CourseView view = new CourseView();
      view.setCourseId((String)row[0]);
      view.setSchoolName((String)row[1]);
      view.setCourseName((String)row[2]);
      result.add(view);
    }
    return result;
  }

  public int countCourses(CourseFilter filter)
  {
    Query query = entityManager.createNamedQuery("countCourses");
    setCourseFilterParameters(query, filter);
    Number number = (Number)query.getSingleResult();
    return number.intValue();
  }

  public Inscription loadInscription(String inscriptionId)
  {
    DBInscription dbInscription = 
      entityManager.find(DBInscription.class, inscriptionId);
    if (dbInscription == null)
      throw new WebServiceException("edu:INSCRIPTION_NOT_FOUND");
    Query query =
      entityManager.createNamedQuery("loadInscriptionProperties");
    query.setParameter("inscriptionId", inscriptionId);
    List<Property> props = query.getResultList();
    dbInscription.getProperties().addAll(props);
    return dbInscription;
  }

  public Inscription storeInscription(Inscription inscription)
  {
    validateInscription(inscription);

    DBInscription dbInscription = new DBInscription();
    dbInscription.copyFrom(inscription);
    
    String inscriptionId = inscription.getInscriptionId();
    if (inscriptionId == null) // insert
    {
      entityManager.persist(dbInscription);
    }
    else // update
    {
      dbInscription = entityManager.merge(dbInscription);
      // remove previous properties
      Query query = 
        entityManager.createNamedQuery("removeInscriptionProperties");
      query.setParameter("inscriptionId", inscription.getInscriptionId());
      query.executeUpdate();
    }
    // insert new properties
    inscriptionId = dbInscription.getInscriptionId();
    for (Property p : inscription.getProperties())
    {
      DBInscriptionProperty dbip = new DBInscriptionProperty();
      dbip.setInscriptionId(inscriptionId);
      dbip.setName(p.getName());
      dbip.setValue(p.getValue());
      entityManager.persist(dbip);
    }
    return dbInscription;
  }

  public boolean removeInscription(String inscriptionId)
  {
    Query query = 
      entityManager.createNamedQuery("removeInscriptionProperties");
    query.setParameter("inscriptionId", inscriptionId);
    query.executeUpdate();
    try
    {
      DBInscription dbInscription =
        entityManager.getReference(DBInscription.class, inscriptionId);
      entityManager.remove(dbInscription);
      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public List findInscriptionViews(InscriptionFilter filter)
  {
    try
    {
      List<InscriptionView> result = new ArrayList<InscriptionView>();
      JPAQuery jpaQuery = new JPAQuery(entityManager.createNamedQuery("findInscriptions"));
      setInscriptionFilterParameters(jpaQuery, filter);
      jpaQuery.setFirstResult(filter.getFirstResult());
      jpaQuery.setMaxResults(filter.getMaxResults());
      List<String> personIdList = new ArrayList<String>();
      List<Object[]> list = jpaQuery.getResultList();
      if (list.size() > 0)
      {
        for (Object[] row : list)
        {
          InscriptionView view = new InscriptionView();
          view.setInscriptionId((String) row[0]);
          view.setCode((String) row[1]);
          String personId = (String) row[2];
          PersonView personView = new PersonView();
          personView.setPersonId(personId);
          personIdList.add(personId);
          view.setPersonView(personView);
          view.setCourseName((String) row[3]);
          view.setSchoolName((String) row[4]);
          result.add(view);
        }
        describePersons(personIdList, result);
      }
      return result;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findInscriptionViews failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  public int countInscriptions(InscriptionFilter filter)
  {
    try
    {
      JPAQuery jpaQuery = new JPAQuery(entityManager.createNamedQuery("countInscriptions"));
      setInscriptionFilterParameters(jpaQuery, filter);
      Number number = (Number) jpaQuery.getResultCount();
      return number.intValue();
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "countInscriptions failed");
      throw WSExceptionFactory.create(ex);
    }
  }
  
  private void setSchoolFilterParameters(Query query, SchoolFilter filter)
  {
    String schoolCode = filter.getCode();
    String schoolName = filter.getName();
    if (schoolName != null) schoolName = "%" + schoolName.toUpperCase() + "%";
    query.setParameter("schoolCode", schoolCode);
    query.setParameter("schoolName", schoolName);
  }
  
  private void setCourseFilterParameters(Query query, CourseFilter filter)
  {
    String courseName = filter.getCourseName();
    String schoolName = filter.getSchoolName();
    if (courseName != null) courseName = "%" + courseName.toUpperCase() + "%";
    if (schoolName != null) schoolName = "%" + schoolName.toUpperCase() + "%";
    query.setParameter("courseName", courseName);
    query.setParameter("schoolName", schoolName);
  }
  
  private void setInscriptionFilterParameters(JPAQuery query,
    InscriptionFilter filter) throws Exception
  {
    String personIdList = null;
    String personId = filter.getPersonId();
    String courseId = filter.getCourseId();
    String personName = filter.getPersonName();
    if (personId != null)
    {
      personIdList = " " + personId + " ";
    }
    else if (personName != null && personName.trim().length() > 0)
    {
      KernelManagerPort port = getKernelManagerPort();
      PersonFilter personFilter = new PersonFilter();
      personFilter.setFullName(personName);
      personFilter.setMaxResults(10000);
      StringBuilder buffer = new StringBuilder(" ");
      List<PersonView> personViews = port.findPersonViews(personFilter);
      for (PersonView personView : personViews)
      {
        buffer.append(personView.getPersonId()).append(" ");
      }
      personIdList = buffer.toString();
    }
    query.setIdParameter("personIdList", TextUtils.stringToList(personIdList, " "));
    query.setParameter("code", filter.getCode());
    query.setParameter("courseId", courseId);
  }

  private void describePersons(List<String> personIdList, 
    List<InscriptionView> inscriptionViews)
  {
    KernelManagerPort port = getKernelManagerPort();
    PersonFilter personFilter = new PersonFilter();
    personFilter.getPersonId().addAll(personIdList);
    personFilter.setMaxResults(10000);
    List<PersonView> personViews = port.findPersonViews(personFilter);
    Map<String, PersonView> map = new HashMap<String, PersonView>();
    for (PersonView personView : personViews)
    {
      map.put(personView.getPersonId(), personView);
    }
    for (InscriptionView inscriptionView : inscriptionViews)
    {
      String personId = inscriptionView.getPersonView().getPersonId();
      PersonView personView = map.get(personId);
      inscriptionView.setPersonView(personView);
    }
  }

  private void validateCourse(Course course)
  {
    EducationMetaData metaData = getEducationMetaData();
    if (course.getName() == null ||
      course.getName().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }
    else if (course.getName().length() > metaData.getCourseNameMaxSize())
    {
      throw new WebServiceException("VALUE_TOO_LARGE");
    }
    if (course.getCode() != null &&
      course.getCode().length() > metaData.getCourseCodeMaxSize())
    {
      throw new WebServiceException("VALUE_TOO_LARGE");
    }
    if (course.getSchoolId() == null || 
      course.getSchoolId().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }
  }

  private void validateInscription(Inscription inscription)
  {
    EducationMetaData metaData = getEducationMetaData();
    if (inscription.getPersonId() == null ||
      inscription.getPersonId().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }
    if (inscription.getCourseId() == null ||
      inscription.getCourseId().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }
    if (inscription.getCode() != null && inscription.getCode().length() > 
      metaData.getInscriptionCodeMaxSize())
    {
      throw new WebServiceException("VALUE_TOO_LARGE");
    }
    for (Property property : inscription.getProperties())
    {
      String value = property.getValue();
      if (value != null && value.length() >
        metaData.getInscriptionPropertyValueMaxSize())
      {
        throw new WebServiceException("VALUE_TOO_LARGE");
      }
    }
  }

  private KernelManagerPort getKernelManagerPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(KernelManagerService.class);
      return endpoint.getPort(KernelManagerPort.class,
        MatrixConfig.getProperty("adminCredentials.userId"),
        MatrixConfig.getProperty("adminCredentials.password"));
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
}
