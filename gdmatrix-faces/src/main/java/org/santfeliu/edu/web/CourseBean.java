package org.santfeliu.edu.web;

import org.matrix.edu.Course;
import org.matrix.edu.School;

import org.santfeliu.web.obj.ObjectBean;

public class CourseBean extends ObjectBean
{
  public CourseBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Course";
  }

  public String remove()
  {
    try
    {
      if (!isNew())
      {
        EducationConfigBean.getPort().removeCourse(objectId);
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return getControllerBean().show();
  }
  
  public String getDescription()
  {
    CourseMainBean mainBean = 
      (CourseMainBean)getBean("courseMainBean");
    return getDescription(mainBean.getCourse());
  }

  public String getDescription(String objectId)
  {
    String description = objectId;
    try
    {
      Course course = 
        EducationConfigBean.getPort().loadCourse(objectId);
      description = getDescription(course);
    }
    catch (Exception ex)
    {
    }
    return description;
  }
  
  private String getDescription(Course course)
  {
    if (course == null) return "";
    StringBuffer buffer = new StringBuffer();
    try
    {
      String schoolId = course.getSchoolId();
      School school = EducationConfigBean.getPort().loadSchool(schoolId);
      buffer.append(school.getName() + ": " + course.getName());
      buffer.append(" (");
      buffer.append(course.getCourseId());
      buffer.append(")");
    }
    catch (Exception ex)
    {
    }
    return buffer.toString();
  }  
  
}
