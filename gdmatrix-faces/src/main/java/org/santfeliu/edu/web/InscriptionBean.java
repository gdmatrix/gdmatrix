package org.santfeliu.edu.web;

import org.matrix.edu.Inscription;

import org.matrix.kernel.Person;

import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.web.obj.ObjectBean;

public class InscriptionBean extends ObjectBean
{
  public InscriptionBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Inscription";
  }
  
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        EducationConfigBean.getPort().removeInscription(objectId);
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
    InscriptionMainBean mainBean = 
      (InscriptionMainBean)getBean("inscriptionMainBean");
    return getDescription(mainBean.getInscription());
  }

  public String getDescription(String objectId)
  {
    String description = objectId;
    try
    {
      Inscription inscription = 
        EducationConfigBean.getPort().loadInscription(objectId);
      description = getDescription(inscription);
    }
    catch (Exception ex)
    {
    }
    return description;
  }
  
  private String getDescription(Inscription inscription)
  {
    if (inscription == null) return "";
    StringBuffer buffer = new StringBuffer();
    try
    {
      String code = inscription.getCode();
      String personId = inscription.getPersonId();
      Person person = KernelConfigBean.getPort().loadPerson(personId);
      buffer.append(code + ": " + person.getName() + " " +
        person.getFirstSurname() + " " +
        person.getSecondSurname() + " - " + inscription.getCourseId());
      buffer.append(" (");
      buffer.append(inscription.getInscriptionId());
      buffer.append(")");
    }
    catch (Exception ex)
    {
    }
    return buffer.toString();
  }
}
