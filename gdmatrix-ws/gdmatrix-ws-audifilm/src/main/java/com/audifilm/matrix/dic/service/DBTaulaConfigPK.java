package com.audifilm.matrix.dic.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBTaulaConfigPK extends GenesysPK
{

  private String tccodi0;
  private String tccodi1;

  public DBTaulaConfigPK(String tccodi0, String tccodi1)
  {
    super(tccodi0, tccodi1);
    this.tccodi0 = tccodi0;
    this.tccodi1 = tccodi1;
  }

  public String getTccodi0()
  {
    return tccodi0;
  }

  public void setTccodi0(String tccodi0)
  {
    this.tccodi0 = tccodi0;
  }

  public String getTccodi1()
  {
    return tccodi1;
  }

  public void setTccodi1(String tccodi1)
  {
    this.tccodi1 = tccodi1;
  }
}
