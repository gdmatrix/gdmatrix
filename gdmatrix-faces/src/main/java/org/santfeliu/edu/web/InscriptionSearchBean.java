package org.santfeliu.edu.web;

import java.util.List;

import org.matrix.edu.InscriptionFilter;


import org.santfeliu.web.obj.BasicSearchBean;


public class InscriptionSearchBean extends BasicSearchBean
{
  private InscriptionFilter filter = new InscriptionFilter();

  public InscriptionSearchBean()
  {
  }

  public int countResults()
  {
    try
    {
      return EducationConfigBean.getPort().countInscriptions(filter);
    }
    catch (Exception ex)
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
      return EducationConfigBean.getPort().findInscriptionViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String show()
  {
    return "inscription_search";
  }
  
  public String selectInscription()
  {
    return null;
  }

  public String showInscription()
  {
    return getControllerBean().showObject("Inscription",
      (String)getValue("#{row.inscriptionId}"));
  }

  public void setFilter(InscriptionFilter filter)
  {
    this.filter = filter;
  }

  public InscriptionFilter getFilter()
  {
    return filter;
  }
}
