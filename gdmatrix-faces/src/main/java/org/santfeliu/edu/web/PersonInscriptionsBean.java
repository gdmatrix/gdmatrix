package org.santfeliu.edu.web;

import java.util.List;

import org.matrix.edu.InscriptionFilter;
import org.matrix.edu.InscriptionView;

import org.santfeliu.web.obj.PageBean;

public class PersonInscriptionsBean extends PageBean
{
  List<InscriptionView> inscriptionViews;

  public PersonInscriptionsBean()
  {
    load();
  }

  public void setInscriptionViews(List<InscriptionView> inscriptionViews)
  {
    this.inscriptionViews = inscriptionViews;
  }

  public List<InscriptionView> getInscriptionViews()
  {
    return inscriptionViews;
  }

  public String show()
  {
    return "person_inscriptions";
  }
  
  public String showInscription()
  {
    return getControllerBean().showObject("Inscription",
      (String)getValue("#{row.inscriptionId}"));
  }

  public int getRowCount()
  {
    return (getInscriptionViews() == null ? 0 : getInscriptionViews().size());
  }
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        InscriptionFilter filter = new InscriptionFilter();
        filter.setPersonId(getObjectId());
        this.inscriptionViews =
          EducationConfigBean.getPort().findInscriptionViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
