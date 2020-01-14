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
package org.santfeliu.misc.presence;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author realor
 */
public class PresenceEntryType implements Serializable
{
  private String type;
  private String label;
  private String key;
  private Integer maxWorkedTime;
  private HashSet<String> previousTypes = new HashSet<String>();
  private HashSet<String> disableTypes = new HashSet<String>();

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public Integer getMaxWorkedTime()
  {
    return maxWorkedTime;
  }

  public void setMaxWorkedTime(Integer maxTime)
  {
    this.maxWorkedTime = maxTime;
  }

  public boolean isWork()
  {
    if (maxWorkedTime == null) return true;
    return maxWorkedTime.intValue() > 0;
  }

  public boolean isPreviousTypeValid(String previousType)
  {
    return previousTypes.contains(previousType);
  }

  public boolean isDisabled(List<PresenceEntry> entries)
  {
    boolean disabled = false;
    if (entries != null)
    {
      Iterator<PresenceEntry> iter = entries.iterator();
      while (iter.hasNext() && !disabled)
      {
        PresenceEntry entry = iter.next();
        disabled = disableTypes.contains(entry.getType());
      }
    }
    return disabled;
  }
  
  public boolean isChangeableTo(PresenceEntryType entryType)
  {
    if (entryType == null) return false;
    if (maxWorkedTime == null)
    {
      return entryType.maxWorkedTime == null;
    }
    return maxWorkedTime.equals(entryType.maxWorkedTime);
  }

  public int getWorkedTime(int duration)
  {
    if (maxWorkedTime != null && duration > maxWorkedTime) return maxWorkedTime;
    return duration;
  }

  public Collection<String> getPreviousTypes()
  {
    return previousTypes;
  }

  public Collection<String> getDisableTypes()
  {
    return disableTypes;
  }

  /*
   * format: type;label;maxWorkedTime(minutes);key;[disableTypes]*;[nextTypes]*
   */
  public static PresenceEntryType parse(String line)
  {
    PresenceEntryType entryType = null;
    String tokens[] = line.split(";");
    if (tokens.length == 6)
    {
      String type = tokens[0];
      String label = tokens[1];
      String value = tokens[2];
      Integer maxWorkedTime = null;
      if (value.length() > 0)
      {
        if (Character.isDigit(value.charAt(0)))
        {
          try
          {
            maxWorkedTime = Integer.parseInt(value) * 60;
          }
          catch (NumberFormatException ex)
          {
          }
        }
      }
      String key = tokens[3];
      if (key.length() > 0)
      {
        key = key.substring(0, 1).toUpperCase();
      }
      List<String> previousTypes = parseList(tokens[4]);
      List<String> disableTypes = parseList(tokens[5]);

      // PresenceEntryType creation
      entryType = new PresenceEntryType();
      entryType.setType(type);
      entryType.setLabel(label);
      entryType.setMaxWorkedTime(maxWorkedTime);
      entryType.setKey(key);
      entryType.previousTypes.addAll(previousTypes);
      entryType.disableTypes.addAll(disableTypes);
    }
    return entryType;
  }

  private static List<String> parseList(String text)
  {
    if (text == null || text.trim().length() == 0 || text.trim().equals("-"))
      return Collections.EMPTY_LIST;
    String types[] = text.split(",");
    return Arrays.asList(types);
  }
}
