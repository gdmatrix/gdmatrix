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
package org.santfeliu.elections;

import java.util.HashMap;

/**
 *
 * @author unknown
 */
public class Board
{
  public static final int ELECTORS = 0;
  public static final int TOTAL_VOTES = 1;
  public static final int BLANK_VOTES = 2;
  public static final int INVALID_VOTES = 3;

  private String description;
  private String district;
  private String section;
  private String boardName;
  private int votes[] = new int[4];
  private HashMap votesByParty = new HashMap();
  
  public String getId()
  {
    return district + ":" + section + ":" + boardName;
  }

  public boolean isScrutinized()
  {
    return votes[TOTAL_VOTES] > 0;
  }
  
  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDistrict(String district)
  {
    this.district = district;
  }

  public String getDistrict()
  {
    return district;
  }

  public void setSection(String section)
  {
    this.section = section;
  }

  public String getSection()
  {
    return section;
  }

  public void setBoardName(String boardName)
  {
    this.boardName = boardName;
  }

  public String getBoardName()
  {
    return boardName;
  }
  
  public void setVotes(int electors, int totalVotes, int blankVotes, int invalidVotes)
  {
    votes[ELECTORS] = electors;
    votes[TOTAL_VOTES] = totalVotes;
    votes[BLANK_VOTES] = blankVotes;
    votes[INVALID_VOTES] = invalidVotes;
  }
  
  public void setVotes(String partyId, int votes)
  {
    votesByParty.put(partyId, new Integer(votes));
  }

  public int getVotes(String partyId)
  {
    Object v = votesByParty.get(partyId);
    return v == null ? 0 : ((Number)v).intValue();
  }
  
  public int getElectors()
  {
    return votes[ELECTORS];
  }

  public int getTotalVotes()
  {
    return votes[TOTAL_VOTES];
  }
  
  public int getBlankVotes()
  {
    return votes[BLANK_VOTES];
  }

  public int getInvalidVotes()
  {
    return votes[INVALID_VOTES];
  }

  public int getVotes(int key)
  {
    return votes[key];
  }
    
  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(
      description + ":[" + district + " " + 
      section + " " + boardName + "] " + 
      votes[0] + " " + votes[1] + " " + votes[2] + " " + votes[3] + 
      " " + votesByParty + "\n");
    return buffer.toString();
  }
}
