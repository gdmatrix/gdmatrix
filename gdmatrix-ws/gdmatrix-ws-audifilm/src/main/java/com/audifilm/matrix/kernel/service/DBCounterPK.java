package com.audifilm.matrix.kernel.service;

/**
 *
 * @author blanquepa
 */
public class DBCounterPK
{
  private String claupref;
  private String claucod;
  private String clauorigen;

  public String getClaupref()
  {
    return claupref;
  }

  public void setClaupref(String claupref)
  {
    this.claupref = claupref;
  }

  public String getClaucod()
  {
    return claucod;
  }

  public void setClaucod(String claucod)
  {
    this.claucod = claucod;
  }

  public String getClauorigen()
  {
    return clauorigen;
  }

  public void setClauorigen(String clauorigen)
  {
    this.clauorigen = clauorigen;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 97 * hash + (this.claupref != null ? this.claupref.hashCode() : 0);
    hash = 97 * hash + (this.claucod != null ? this.claucod.hashCode() : 0);
    hash = 97 * hash + (this.clauorigen != null ? this.clauorigen.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final DBCounterPK other = (DBCounterPK) obj;
    if ((this.claupref == null) ? (other.claupref != null) : !this.claupref.equals(other.claupref))
    {
      return false;
    }
    if ((this.claucod == null) ? (other.claucod != null) : !this.claucod.equals(other.claucod))
    {
      return false;
    }
    if ((this.clauorigen == null) ? (other.clauorigen != null) : !this.clauorigen.equals(other.clauorigen))
    {
      return false;
    }
    return true;
  }
  
  
}
