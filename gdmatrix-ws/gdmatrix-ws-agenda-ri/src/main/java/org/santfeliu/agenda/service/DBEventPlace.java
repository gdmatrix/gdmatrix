/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.agenda.service;

import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.EventPlace;
import org.santfeliu.jpa.JPAUtils;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author blanquepa
 */
public class DBEventPlace extends EventPlace
{
  private String tipesdevcod;
  private String numordre;

  private String paiscod;
  private String provcod;
  private String municod;
  private String carcod;
  private String domcod;
  private String salacod;
  private String numero;

  private DBEvent dbEvent;

  public DBEventPlace()
  {
  }
  
  public DBEventPlace(EventPlace eventPlace)
  {
    copyFrom(eventPlace);
  }

  public void copyTo(EventPlace eventPlace)
  {
    JPAUtils.copy(this, eventPlace);
  }

  public void copyFrom(EventPlace eventPlace)
  {
    JPAUtils.copy(eventPlace, this);
    if (eventPlaceId != null)
    {
      String[] pk = eventPlaceId.split(AgendaManager.PK_SEPARATOR);
      eventId = pk[0];
      numordre = pk[1];
    }
    if (roomId != null && roomId.length() > 0)
    {
      String[] roomIdArray = roomId.split(AgendaManager.PK_SEPARATOR);
      domcod = roomIdArray[0];
      salacod = roomIdArray[1];
    }
    else
      domcod = addressId;

    if (paiscod == null)
    {
      String defaultCountryId =
        MatrixConfig.getClassProperty(AgendaManager.class, "defaultCountryId");
      paiscod = defaultCountryId;
    }
  }

  @Override
  public String getEventPlaceId()
  {
    return eventId + AgendaManager.PK_SEPARATOR + numordre;
  }

  public String getNumordre()
  {
    return numordre;
  }

  public void setNumordre(String numordre)
  {
    this.numordre = numordre;
  }

  public String getTipesdevcod()
  {
    return tipesdevcod;
  }

  public void setTipesdevcod(String tipesdevcod)
  {
    this.tipesdevcod = tipesdevcod;
  }

  public String getCarcod()
  {
    return carcod;
  }

  public void setCarcod(String carcod)
  {
    this.carcod = carcod;
  }

  public String getDomcod()
  {
    return domcod;
  }

  public void setDomcod(String domcod)
  {
    this.domcod = domcod;
  }

  public String getSalacod()
  {
    return salacod;
  }

  public void setSalacod(String salacod)
  {
    this.salacod = salacod;
  }

  public String getNumero()
  {
    return numero;
  }

  public void setNumero(String numero)
  {
    this.numero = numero;
  }

  public String getMunicod()
  {
    return municod;
  }

  public void setMunicod(String municod)
  {
    this.municod = municod;
  }

  public String getPaiscod()
  {
    return paiscod;
  }

  public void setPaiscod(String paiscod)
  {
    this.paiscod = paiscod;
  }

  public String getProvcod()
  {
    return provcod;
  }

  public void setProvcod(String provcod)
  {
    this.provcod = provcod;
  }

  public String getCountryId()
  {
    return paiscod;
  }

  public String getProvinceId()
  {
    return getCountryId() + AgendaManager.PK_SEPARATOR + provcod;
  }

  public String getCityId()
  {
    return getProvinceId() + AgendaManager.PK_SEPARATOR + municod;
  }

  public String getStreetId()
  {
    return getCityId() +  AgendaManager.PK_SEPARATOR + carcod;
  }

  @Override
  public String getAddressId()
  {
    return domcod;
  }

  @Override
  public String getRoomId()
  {
    String roomId = null;
    if (salacod != null)
      roomId = domcod + AgendaManager.PK_SEPARATOR + salacod;

    return roomId;
  }

  public boolean isRoom()
  {
    return !StringUtils.isBlank(salacod);
  }

  public boolean isAddress()
  {
    return (!isRoom() && !StringUtils.isBlank(domcod));
  }

  public boolean isStreet()
  {
    return (!isRoom() && !isAddress() && !StringUtils.isBlank(carcod));
  }

  public DBEvent getDbEvent()
  {
    return dbEvent;
  }

  public void setDbEvent(DBEvent dbEvent)
  {
    this.dbEvent = dbEvent;
  }
}
