/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.common.service.PKUtil;
import java.io.Serializable;

/**
 *
 * @author comasfc
 */
public class DBPersonHistoricPK implements Serializable
{

  private String personId;
  private String datainicial;
  private String horainicial;
  private String datafinal;
  private String horafinal;

  public DBPersonHistoricPK()
  {
  }

  public DBPersonHistoricPK(String personHistoricId)
  {
    String ids[] = PKUtil.decomposePK(personHistoricId);
    this.personId = (ids==null)?null:ids[0];
    this.datainicial = (ids==null)?null:ids[1];
    this.horainicial = (ids==null)?null:ids[2];
    this.datafinal = (ids==null)?null:ids[3];
    this.horafinal = (ids==null)?null:ids[4];
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public String getDatafinal()
  {
    return datafinal;
  }

  public String getDatainicial()
  {
    return datainicial;
  }

  public String getHorafinal()
  {
    return horafinal;
  }

  public String getHorainicial()
  {
    return horainicial;
  }

  public boolean equals(Object o)
  {
    DBPersonAddressPK pk = (DBPersonAddressPK)o;
    return pk.getPersonId().equals(personId) 
      && pk.getPersonAddressNumber().equals(datainicial )
      && pk.getPersonAddressNumber().equals(horainicial )
      && pk.getPersonAddressNumber().equals(datafinal )
      && pk.getPersonAddressNumber().equals(horafinal )
      ;
  }

  public int hashCode()
  {
    return (personId + datainicial + horainicial + datainicial + horainicial).hashCode();
  }
}


