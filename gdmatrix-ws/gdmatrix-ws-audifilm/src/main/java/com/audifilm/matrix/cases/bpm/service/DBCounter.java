package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;


public class DBCounter extends DBGenesysEntity
{
  private String claupref;
  private String claucod;
  private String clauorigen;
  private String claudesc;
  private int counter;
  
  public DBCounter()
  {
  }

  public void setClaupref(String claupref)
  {
    this.claupref = claupref;
  }

  public String getClaupref()
  {
    return claupref;
  }

  public void setClaucod(String claucod)
  {
    this.claucod = claucod;
  }

  public String getClaucod()
  {
    return claucod;
  }

  public void setClauorigen(String clauorigen)
  {
    this.clauorigen = clauorigen;
  }

  public String getClauorigen()
  {
    return clauorigen;
  }

  public void setClaudesc(String claudesc)
  {
    this.claudesc = claudesc;
  }

  public String getClaudesc()
  {
    return claudesc;
  }

  public void setCounter(int counter)
  {
    this.counter = counter;
  }

  public int getCounter()
  {
    return counter;
  }

  @Override
  public String[] getIds()
  {
    return new String[] {claupref, claucod, clauorigen};
  }
}
