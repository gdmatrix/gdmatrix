package org.santfeliu.kernel.web;

import org.matrix.kernel.Person;

import org.santfeliu.web.obj.ObjectBean;

public class PersonBean extends ObjectBean
{
  public PersonBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Person";
  }

  public String getDescription()
  {
    PersonMainBean personMainBean = (PersonMainBean)getBean("personMainBean");
    Person person = personMainBean.getPerson();
    return getDescription(person);
  }
  
  public String getDescription(String objectId)
  {
    try
    {
      if (objectId != null && objectId.contains(";")) //objectId contains description
        return objectId;
      Person person = KernelConfigBean.getPort().loadPerson(objectId);
      return getDescription(person);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }
  
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        KernelConfigBean.getPort().removePerson(getObjectId());
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return getControllerBean().show();
  }

  private String getDescription(Person person)
  {
    if (person == null) return "";
    StringBuffer buffer = new StringBuffer();
    buffer.append(person.getName());
    if (person.getFirstParticle() != null)
    {
      buffer.append(" ");
      buffer.append(person.getFirstParticle());
    }
    if (person.getFirstSurname() != null)
    {
      buffer.append(" ");
      buffer.append(person.getFirstSurname());
    }
    if (person.getSecondParticle() != null)
    {
      buffer.append(" ");
      buffer.append(person.getSecondParticle());
    }
    if (person.getSecondSurname() != null)
    {
      buffer.append(" ");
      buffer.append(person.getSecondSurname());
    }
    buffer.append(" (");
    buffer.append(person.getPersonId());
    buffer.append(")");
    return buffer.toString();
  }
}
