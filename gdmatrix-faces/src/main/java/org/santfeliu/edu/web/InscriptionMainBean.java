package org.santfeliu.edu.web;

import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.faces.model.SelectItem;

import org.matrix.edu.Inscription;

import org.matrix.edu.Property;

import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.web.obj.PageBean;

public class InscriptionMainBean extends PageBean
{
  private static final String FORM_PROPERTY = "inscription_form_url";
  private Inscription inscription;
  private Map properties = new HashMap();
  
  public InscriptionMainBean()
  {
    load();
  }

  public String show()
  {
    return "inscription_main";
  }

  public void setInscription(Inscription inscription)
  {
    this.inscription = inscription;
  }

  public Inscription getInscription()
  {
    return inscription;
  }
  
  public List<SelectItem> getPersonSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    return personBean.getSelectItems(inscription.getPersonId());
  }
  
  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{inscriptionMainBean.inscription.personId}");
  }
  
  public String showPerson()
  {    
    return getControllerBean().showObject("Person",
      inscription.getPersonId());
  }
  
  public List<SelectItem> getCourseSelectItems()
  {
    CourseBean courseBean = (CourseBean)getBean("courseBean");
    return courseBean.getSelectItems(inscription.getCourseId());
  }
  
  public String searchCourse()
  {
    return getControllerBean().searchObject("Course",
      "#{inscriptionMainBean.inscription.courseId}");
  }
  
  public String showCourse()
  {    
    return getControllerBean().showObject("Course",
      inscription.getCourseId());
  }  
  
  public String store()
  {
    try
    {
      for (Object e : properties.entrySet())
      {
        Map.Entry entry = (Map.Entry)e;
        Property property = new Property();
        property.setName((String)entry.getKey());
        Object value = entry.getValue();
        if (value != null)
        {
          property.setValue(String.valueOf(value));
        }
        inscription.getProperties().add(property);
      }
      this.inscription = 
        EducationConfigBean.getPort().storeInscription(inscription);
      setObjectId(inscription.getInscriptionId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }
  
  public String getFormUrl()
  {
    return getProperty(FORM_PROPERTY);
  }

  public Map getProperties()
  {
    return properties;
  }
  
  public void setProperties(Map properties)
  {
    this.properties = properties;
  }
  
  private void load()
  {
    if (isNew())
    {
      this.inscription = new Inscription();
    }
    else
    {
      try
      {
        this.inscription =
          EducationConfigBean.getPort().loadInscription(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        this.inscription = new Inscription();
      }
      for (Property property : inscription.getProperties())
      {
        properties.put(property.getName(), property.getValue());
      }
      inscription.getProperties().clear();
    }
  }
}
