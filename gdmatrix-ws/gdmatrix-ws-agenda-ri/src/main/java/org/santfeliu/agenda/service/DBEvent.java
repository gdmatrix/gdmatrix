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


import org.matrix.agenda.Event;
import org.santfeliu.jpa.JPAUtils;
import org.santfeliu.util.audit.Auditable;

/**
 *
 * @author blanquepa
 */
public class DBEvent extends Event implements Auditable
{
  private String datainici;
  private String horainici;
  private String datafinal;
  private String horafinal;
  private String stddgr;
  private String stdhgr;
  private String stddmod;
  private String stdhmod;
  private String tipesdevcod;
  private String visibleassist;
  private byte[] text;

  public DBEvent()
  {
  }

  public DBEvent(Event event)
  {
    copyFrom(event);
  }

  public byte[] getText()
  {
    return text;
  }

  public void setText(byte[] text)
  {
    this.text = text;
  }

  public void copyTo(Event event)
  {
    event.setEventId(getEventId());
    event.setEventTypeId(getEventTypeId());
    event.setSummary(getSummary());
    event.setCreationDateTime(getCreationDateTime());
    event.setCreationUserId(getCreationUserId());
    event.setChangeDateTime(getChangeDateTime());
    event.setChangeUserId(getChangeUserId());
    event.setDescription(getDescription());
    event.setStartDateTime(getStartDateTime());
    event.setEndDateTime(getEndDateTime());
    event.setOnlyAttendants(isOnlyAttendants());
    event.setComments(getComments());
    if ((text != null) && (text.length > 0))
    {
      event.setDetail(new String(text));
    }
    else
    {
      event.setDetail(this.getDetail());
    }
  }

  public void copyFrom(Event event)
  {
    JPAUtils.copy(event, this);

    setOnlyAttendants(event.isOnlyAttendants());

    String startDT = event.getStartDateTime();
    if (startDT != null)
    {
      this.datainici = startDT.substring(0, 8);
      this.horainici = startDT.substring(8, 14);
    }

    String endDT = event.getEndDateTime();
    if (endDT != null)
    {
      this.datafinal = endDT.substring(0, 8);
      this.horafinal = endDT.substring(8, 14);
    }

    String creationDT = event.getCreationDateTime();
    if (creationDT != null)
    {
      this.stddgr = creationDT.substring(0, 8);
      this.stdhgr = creationDT.substring(8, 14);
    }

    String changeDT = event.getChangeDateTime();
    if (changeDT != null)
    {
      this.stddmod = changeDT.substring(0, 8);
      this.stdhmod = changeDT.substring(8, 14);
    }

    if (event.getDetail() != null)
      this.text = event.getDetail().getBytes();
  }

  public void merge(DBEvent dbEvent)
  {
    if (eventTypeId == null)
      eventTypeId = dbEvent.getEventTypeId();
    if (summary == null)
      summary = dbEvent.getSummary();
    if (description == null)
      description = dbEvent.getDescription();
    if (getStartDateTime() == null)
      setStartDateTime(dbEvent.getStartDateTime());
    if (getEndDateTime() == null)
      setEndDateTime(dbEvent.getEndDateTime());
    if (isOnlyAttendants() == null )
      setOnlyAttendants(dbEvent.isOnlyAttendants());
    if (comments == null)
      comments = dbEvent.getComments();
    if (detail == null)
    {
      if ((dbEvent.getText() != null) && (dbEvent.getText().length > 0))
      {
        setDetail(new String(dbEvent.getText()));
      }
      else
      {
        setDetail(dbEvent.getDetail());
      }
    }
    setCreationDateTime(dbEvent.getCreationDateTime());
    setCreationUserId(dbEvent.getCreationUserId());
    setTipesdevcod(dbEvent.getTipesdevcod());
  }

  public String getDatafinal()
  {
    return datafinal;
  }

  public void setDatafinal(String datafinal)
  {
    this.datafinal = datafinal;
  }

  public String getDatainici()
  {
    return datainici;
  }

  public void setDatainici(String datainici)
  {
    this.datainici = datainici;
  }

  public String getHorafinal()
  {
    return horafinal;
  }

  public void setHorafinal(String horafinal)
  {
    this.horafinal = horafinal;
  }

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

  @Override
  public String getStartDateTime()
  {
    String startDT = null;
    if (datainici != null)
      startDT = datainici + (horainici != null ? horainici : "000000");
    return startDT;
  }

  @Override
  public void setStartDateTime(String startDateTime)
  {
    if (startDateTime != null)
    {
      this.datainici = startDateTime.substring(0, 8);
      this.horainici = startDateTime.substring(8, 14);
    }
  }

  @Override
  public String getEndDateTime()
  {
    String endDT = null;
    if (datafinal != null)
      endDT = datafinal + (horafinal != null ? horafinal : "000000");
    return endDT;
  }

  @Override
  public void setEndDateTime(String endDateTime)
  {
    if (endDateTime != null)
    {
      this.datafinal = endDateTime.substring(0, 8);
      this.horafinal = endDateTime.substring(8, 14);
    }
  }

  @Override
  public String getCreationDateTime()
  {
    String creationDT = null;
    if (stddgr != null)
      creationDT = stddgr + (stdhgr != null ? stdhgr : "000000");
    return creationDT;
  }

  @Override
  public String getChangeDateTime()
  {
    String changeDT = null;
    if (stddmod != null)
      changeDT = stddmod + (stdhmod != null ? stdhmod : "000000");
    return changeDT;
  }

  public String getHorainici()
  {
    return horainici;
  }

  public void setHorainici(String horainici)
  {
    this.horainici = horainici;
  }

  @Override
  public void setCreationDateTime(String creationDateTime)
  {
    if (creationDateTime != null)
    {
      this.stddgr = creationDateTime.substring(0, 8);
      this.stdhgr = creationDateTime.substring(8, 14);
    }
  }

  @Override
  public void setChangeDateTime(String changeDateTime)
  {
    if (changeDateTime != null)
    {
      this.stddmod = changeDateTime.substring(0, 8);
      this.stdhmod = changeDateTime.substring(8, 14);
    }
  }

  @Override
  public Boolean isOnlyAttendants()
  {
    Boolean result = null;

    if ("S".equals(visibleassist))
      result = true;
    else if ("N".equals(visibleassist))
      result = false;

    return result;
  }

  @Override
  public void setOnlyAttendants(Boolean onlyAttendants)
  {
    if (onlyAttendants != null && onlyAttendants)
      visibleassist = "S";
    else
      visibleassist = "N";
  }

  public String getVisibleassist()
  {
    return visibleassist;
  }

  public void setVisibleassist(String visibleassist)
  {
    this.visibleassist = visibleassist;
  }

  public String getTipesdevcod()
  {
    return tipesdevcod;
  }

  public void setTipesdevcod(String tipesdevcod)
  {
    this.tipesdevcod = tipesdevcod;
  }

}

