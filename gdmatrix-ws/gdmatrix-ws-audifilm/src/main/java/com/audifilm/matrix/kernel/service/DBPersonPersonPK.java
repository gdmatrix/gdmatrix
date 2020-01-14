package com.audifilm.matrix.kernel.service;


/**
 *
 * @author blanquepa
 */
public class DBPersonPersonPK
{
  private String personId;
  private String relPersonId;

  public DBPersonPersonPK(String personPersonId)
  {
    String ids[] = personPersonId.split(KernelManager.PK_SEPARATOR);
    this.personId = ids[0];
    this.relPersonId = ids[1];
  }

  public DBPersonPersonPK(String personId, String relPersonId)
  {
    this.personId = personId;
    this.relPersonId = relPersonId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getRelPersonId()
  {
    return relPersonId;
  }

  public void setRelPersonId(String relPersonId)
  {
    this.relPersonId = relPersonId;
  }
}
