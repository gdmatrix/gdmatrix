package org.santfeliu.edu.web;

import java.util.List;

import javax.faces.model.SelectItem;

import org.matrix.edu.Course;
import org.matrix.edu.SchoolFilter;

import org.santfeliu.faces.FacesUtils;
import org.santfeliu.web.obj.PageBean;


public class CourseMainBean extends PageBean
{
  private Course course = new Course();
  private transient List<SelectItem> schoolSelectItems;

  public CourseMainBean()
  {
    load();
  }

  public void setCourse(Course course)
  {
    this.course = course;
  }

  public Course getCourse()
  {
    return course;
  }

  public String show()
  {
    return "course_main";
  }
  
  public String store()
  {
    try
    {
      this.course = 
        EducationConfigBean.getPort().storeCourse(course);
      setObjectId(course.getCourseId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }
  
  public List<SelectItem> getSchoolSelectItems()
  {
    if (schoolSelectItems == null)
    {
      try
      {
        schoolSelectItems = FacesUtils.getListSelectItems(
          EducationConfigBean.getPort().findSchools(new SchoolFilter()),
          "schoolId", "name", true);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return schoolSelectItems;
  }
  
  private void load()
  {
    if (isNew())
    {
      this.course = new Course();
    }
    else
    {
      try
      {
        this.course =
          EducationConfigBean.getPort().loadCourse(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        this.course = new Course();
      }
    }
  }
}
