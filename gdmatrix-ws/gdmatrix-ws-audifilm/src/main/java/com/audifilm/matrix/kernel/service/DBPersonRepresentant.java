package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.dic.service.personrepresentant.PersonRepresentantType;
import com.audifilm.matrix.dic.service.types.DicTypeAdmin;
import org.matrix.kernel.PersonRepresentant;
import org.matrix.util.WSEndpoint;

public class DBPersonRepresentant extends DBEntityBase
{
  private String personId;
  private String representantId;
  private String representationTypeId;
  private String comments;
  private String valdata;
  private DBPerson person;
  private DBPerson representant;

  public DBPersonRepresentant()
  {
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setRepresentantId(String representantId)
  {
    this.representantId = representantId;
  }

  public String getRepresentantId()
  {
    return representantId;
  }

  public void setRepresentationTypeId(String representationTypeId)
  {
    this.representationTypeId = representationTypeId;
  }

  public String getRepresentationTypeId()
  {
    return representationTypeId;
  }

  public void setComments(String comments)
  {
    this.comments = comments;
  }

  public String getComments()
  {
    return comments;
  }

  public void setValdata(String valdata)
  {
    this.valdata = valdata;
  }

  public String getValdata()
  {
    return valdata;
  }

  /* relationships */
  public void setPerson(DBPerson person)
  {
    this.person = person;
  }

  public DBPerson getPerson()
  {
    return person;
  }

  public void setRepresentant(DBPerson representant)
  {
    this.representant = representant;
  }

  public DBPerson getRepresentant()
  {
    return representant;
  }

  public void copyTo(WSEndpoint endpoint, PersonRepresentant personRepresentant)
  {
    personRepresentant.setPersonRepresentantId(
      personId + KernelManager.PK_SEPARATOR + representantId);
    personRepresentant.setPersonId(personId);
    personRepresentant.setRepresentantId(representantId);
    personRepresentant.setRepresentationTypeId(
      DicTypeAdmin.getInstance(PersonRepresentantType.class)
      .toGlobalId(endpoint, representationTypeId));
    personRepresentant.setComments(comments);
  }
  
  public void copyFrom(WSEndpoint endpoint, PersonRepresentant personRepresentant)
  {
    this.personId = personRepresentant.getPersonId();
    this.representantId = personRepresentant.getRepresentantId();
    this.representationTypeId = DicTypeAdmin.getInstance(PersonRepresentantType.class)
      .toLocalId(endpoint, personRepresentant.getRepresentationTypeId());
    this.comments = personRepresentant.getComments();
  }
}
