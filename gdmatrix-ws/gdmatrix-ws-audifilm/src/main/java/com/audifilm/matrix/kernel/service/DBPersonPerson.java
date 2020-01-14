package com.audifilm.matrix.kernel.service;

import org.matrix.kernel.PersonPerson;
import org.santfeliu.jpa.JPAUtils;

/**
 *
 * @author blanquepa
 */
public class DBPersonPerson extends PersonPerson
{
  public DBPersonPerson()
  {
  }

  public DBPersonPerson(PersonPerson personPerson)
  {
    JPAUtils.copy(personPerson, this);
  }

  public void copyTo(PersonPerson personPerson)
  {
    JPAUtils.copy(this, personPerson);
  }

}
