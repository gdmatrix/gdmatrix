package com.audifilm.matrix.common.service;

import com.audifilm.matrix.util.TextUtil;

/**
 *
 * @author comasfc
 */
abstract public class DBGenesysEntity implements DBGenesysEntityInterface, GenesysPKInterface
{

  private String stdugr;
  private String stddgr;
  private String stdhgr;
  private String stdumod;
  private String stddmod;
  private String stdhmod;

  abstract public String[] getIds();


  public String getStddgr()
  {
    return stddgr;
  }

  public void setStddgr(String stddgr)
  {
    this.stddgr = stddgr;
  }

  public String getStddmod()
  {
    return stddmod;
  }

  public void setStddmod(String stddmod)
  {
    this.stddmod = stddmod;
  }

  public String getStdhgr()
  {
    return stdhgr;
  }

  public void setStdhgr(String stdhgr)
  {
    this.stdhgr = stdhgr;
  }

  public String getStdhmod()
  {
    return stdhmod;
  }

  public void setStdhmod(String stdhmod)
  {
    this.stdhmod = stdhmod;
  }

  public String getStdugr()
  {
    return stdugr;
  }

  public void setStdugr(String stdugr)
  {
    this.stdugr = stdugr;
  }

  public String getStdumod()
  {
    return stdumod;
  }

  public void setStdumod(String stdumod)
  {
    this.stdumod = stdumod;
  }


  public String getCreationDateTime() {
    if (stddgr==null) return null;
    return stddgr + (stdhgr==null?"000000":stdhgr);
  }

  public void setCreationDateTime(String datetime) {
    if (datetime == null)
    {
      stddgr = null;
      stdhgr = null;
    } else {
      stddgr = datetime.substring(0,8);
      stdhgr = datetime.substring(8,6);
    }
  }

  public String getChangeDateTime() {
    if (stddmod==null) return null;
    return stddmod + (stdhmod==null?"000000":stdhgr);
  }

  public void setChangeDateTime(String datetime)
  {
    if (datetime == null)
    {
      stddmod = null;
      stdhmod = null;
    } else {
      stddmod = datetime.substring(0,8);
      stdhmod = datetime.substring(8,6);
    }
  }

  

  public int compareTo(GenesysPKInterface o)
  {
    return getIds().toString().compareTo(o.getIds().toString());
  }

  public void setAuditoriaCreacio(String stdugr)
  {
    String dateTime = TextUtil.toStringDateTime();

    this.stdugr = stdugr;
    this.stdumod = stdugr;

    this.stddgr = dateTime.substring(0,8);
    this.stddmod = dateTime.substring(0,8);

    this.stdhgr = dateTime.substring(8);
    this.stdhmod = dateTime.substring(8);
    
  }

  public void setAuditoriaModificacio(String stdumod)
  {
    String dateTime = TextUtil.toStringDateTime();

    this.stdumod = stdumod;
    this.stddmod = dateTime.substring(0,8);
    this.stdhmod = dateTime.substring(8);

  }
}
