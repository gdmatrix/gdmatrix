package org.santfeliu.edu.web;

import java.util.List;

import org.matrix.edu.CourseFilter;

import org.matrix.edu.CourseView;
import org.santfeliu.web.obj.BasicSearchBean;



public class CourseSearchBean extends BasicSearchBean
{
  private CourseFilter filter = new CourseFilter();

  public CourseSearchBean()
  {
  }

  public void setFilter(CourseFilter filter)
  {
    this.filter = filter;
  }

  public CourseFilter getFilter()
  {
    return filter;
  }

  public int countResults()
  {
    try 
    {
      return EducationConfigBean.getPort().countCourses(filter);
    }
    catch(Exception ex)
    {
      error(ex);
    }
  
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
    
      List<CourseView> results = 
        EducationConfigBean.getPort().findCourseViews(filter);
      return results;
    }
    catch(Exception ex)
    {
      error(ex);
    }   
    return null;
  }

  public String show()
  {
    return "course_search";
  }
  
  public String showCourse()
  {
    return getControllerBean().showObject("Course",
      (String)getValue("#{row.courseId}"));
  }
  
  public String selectCourse()
  {
    CourseView row = (CourseView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String courseId = row.getCourseId();
    return getControllerBean().select(courseId);
  }
}
