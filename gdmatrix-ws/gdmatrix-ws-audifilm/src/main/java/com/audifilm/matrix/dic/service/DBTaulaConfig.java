package com.audifilm.matrix.dic.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author comasfc
 */
public class DBTaulaConfig extends DBGenesysEntity
{

  String tccodi0;
  String tccodi1;
  String tccodi2;
  String description;
  String tcdesc2;
  String tcqual;
  String tcvnum;

  public DBTaulaConfig()
  {
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String[] getIds()
  {
    return new String[]
            {
              tccodi0, tccodi1
            };
  }

  public String getTccodi0()
  {
    return tccodi0;
  }

  public void setTccodi0(String tccodi0)
  {
    this.tccodi0 = tccodi0 != null ? tccodi0.trim() : null;
  }

  public String getTccodi1()
  {
    return tccodi1;
  }

  public void setTccodi1(String tccodi1)
  {
    this.tccodi1 = tccodi1 != null ? tccodi1.trim() : null;
  }

  public String getTccodi2()
  {
    return tccodi2;
  }

  public void setTccodi2(String tccodi2)
  {
    this.tccodi2 = tccodi2 != null ? tccodi2.trim() : null;
  }

  public String getTcdesc2()
  {
    return tcdesc2;
  }

  public void setTcdesc2(String tcdesc2)
  {
    this.tcdesc2 = tcdesc2;
  }

  public String getTcqual()
  {
    return tcqual;
  }

  public void setTcqual(String tcqual)
  {
    this.tcqual = tcqual;
  }

  public String getTcvnum()
  {
    return tcvnum;
  }

  public void setTcvnum(String tcvnum)
  {
    this.tcvnum = tcvnum;
  }
}
