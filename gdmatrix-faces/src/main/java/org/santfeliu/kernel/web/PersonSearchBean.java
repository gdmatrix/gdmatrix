package org.santfeliu.kernel.web;

import java.util.List;
import org.matrix.kernel.PersonFilter;
import org.santfeliu.web.obj.BasicSearchBean;

public class PersonSearchBean extends BasicSearchBean
{
  private PersonFilter filter;
  private String personId;

  public PersonSearchBean()
  {
    filter = new PersonFilter();
  }

  public void setFilter(PersonFilter filter)
  {
    this.filter = filter;
  }

  public PersonFilter getFilter()
  {
    return filter;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
  }

  @Override
  public int countResults()
  {
    try
    {
      filter.getPersonId().clear();
      if (personId != null && personId.trim().length() > 0)
      {
        filter.getPersonId().add(personId);
      }
      return KernelConfigBean.getPort().countPersons(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.getPersonId().clear();
      if (personId != null && personId.trim().length() > 0)
      {
        filter.getPersonId().add(personId);
      }
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return KernelConfigBean.getPort().findPersonViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String showPerson()
  {
    return getControllerBean().showObject("Person",
      (String)getValue("#{row.personId}"));
  }

  public String selectPerson()
  {
    return getControllerBean().select((String)getValue("#{row.personId}"));
  }

  public String show()
  {
    return "person_search";
  }
  
  public String clearFilter()
  {
    this.personId = null;
    filter.getPersonId().clear();
    filter.setFullName(null);
    filter.setName(null);
    filter.setFirstSurname(null);
    filter.setSecondSurname(null);
    filter.setNif(null);
    filter.setPassport(null);
    filter.setFirstResult(0);
    filter.setMaxResults(0);    
    
    //Clear results
    reset();

    return show();    
  }
}

