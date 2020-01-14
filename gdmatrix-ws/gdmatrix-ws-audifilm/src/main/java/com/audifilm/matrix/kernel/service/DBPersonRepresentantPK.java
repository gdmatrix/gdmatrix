package com.audifilm.matrix.kernel.service;

public class DBPersonRepresentantPK
{
  private String personId;
  private String representantId;

  public DBPersonRepresentantPK()
  {
  }

  public DBPersonRepresentantPK(String personRepresentantId)
  {
    String ids[] = personRepresentantId.split(KernelManager.PK_SEPARATOR);
    this.personId = ids[0];
    this.representantId = ids[1];
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
  
  public boolean equals(Object o)
  {
    DBPersonRepresentantPK pk = (DBPersonRepresentantPK)o;
    return pk.getPersonId().equals(personId) &&
      pk.getRepresentantId().equals(representantId);
  }
  
  public int hashCode()
  {
    return (personId + representantId).hashCode();
  }
}
