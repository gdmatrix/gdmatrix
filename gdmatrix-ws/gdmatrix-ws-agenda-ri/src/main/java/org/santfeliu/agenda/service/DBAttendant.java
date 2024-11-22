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

import org.matrix.agenda.Attendant;
import org.santfeliu.jpa.JPAUtils;

/**
 *
 * @author blanquepa */
public class DBAttendant extends Attendant
{
  private String rol;
  private String incognit;

  private DBEvent dbEvent;

  public DBAttendant()
  {
  }

  public DBAttendant(Attendant attendant)
  {
    copyFrom(attendant);
  }

  public DBEvent getDbEvent()
  {
    return dbEvent;
  }

  public void setDbEvent(DBEvent dbEvent)
  {
    this.dbEvent = dbEvent;
  }

  @Override
  public String getAttendantId()
  {
    return this.eventId + AgendaManager.PK_SEPARATOR + this.personId;
  }

  public String getRol()
  {
    return rol;
  }

  public void setRol(String rol)
  {
    this.rol = rol;
  }

  public String getIncognit()
  {
    return incognit;
  }

  public void setIncognit(String incognit)
  {
    this.incognit = incognit;
  }

  public void copyTo(Attendant attendant)
  {
    JPAUtils.copy(this, attendant);
    attendant.setAttendantId(getAttendantId());
    attendant.setHidden(isHidden());
  }

  public void copyFrom(Attendant attendant)
  {
    JPAUtils.copy(attendant, this);
    this.attendantId = attendant.getEventId() + AgendaManager.PK_SEPARATOR +
      attendant.getPersonId();
    setHidden(attendant.isHidden());
  }

  @Override
  public Boolean isHidden()
  {
    Boolean result = null;

    if ("S".equals(incognit))
      result = true;
    else if ("N".equals(incognit))
      result = false;

    return result;
  }

  @Override
  public void setHidden(Boolean hidden)
  {
    if (hidden != null && hidden)
      incognit = "S";
    else
      incognit = "N";
  }

}
