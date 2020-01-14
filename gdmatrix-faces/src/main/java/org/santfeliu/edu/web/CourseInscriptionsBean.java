package org.santfeliu.edu.web;

import java.util.List;

import org.matrix.edu.InscriptionFilter;
import org.matrix.edu.InscriptionView;

import org.santfeliu.web.obj.PageBean;

public class CourseInscriptionsBean extends PageBean
{
  private List<InscriptionView> inscriptionViews;

  public CourseInscriptionsBean()
  {
    load();
  }

  public String show()
  {
    return "course_inscriptions";
  }

  public void setInscriptionViews(List<InscriptionView> inscriptionViews)
  {
    this.inscriptionViews = inscriptionViews;
  }

  public List<InscriptionView> getInscriptionViews()
  {
    return inscriptionViews;
  }

  public int getRowCount()
  {
    return (getInscriptionViews() == null ? 0 : getInscriptionViews().size());
  }
  
  public String showInscription()
  {    
    return getControllerBean().showObject("Inscription",
      (String)getValue("#{row.inscriptionId}"));
  }
  
  private void load()
  {
    try
    {
      InscriptionFilter filter = new InscriptionFilter();
      filter.setCourseId(getObjectId());
      inscriptionViews = EducationConfigBean.getPort().findInscriptionViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
