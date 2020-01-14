package com.audifilm.matrix.common.service;

/**
 *
 * @author comasfc
 */
public interface DBGenesysEntityInterface
{
  public static String entity = "";

  public String getStddgr();
  public void setStddgr(String stddgr);

  public String getStddmod();
  public void setStddmod(String stddmod);

  public String getStdhgr();
  public void setStdhgr(String stdhgr);

  public String getStdhmod();
  public void setStdhmod(String stdhmod);

  public String getStdugr();
  public void setStdugr(String stdugr);

  public String getStdumod();
  public void setStdumod(String stdumod);

  public String getCreationDateTime();
  public void setCreationDateTime(String datetime);

  public String getChangeDateTime();
  public void setChangeDateTime(String datetime);

  public String [] getIds();

  public void setAuditoriaCreacio(String stdugr);
  public void setAuditoriaModificacio(String stdumod);

}
