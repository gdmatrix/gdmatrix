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
package org.santfeliu.kernel.service;

/**
 *
 * @author unknown
 */
public class DBCounter extends DBEntityBase
{
  private String claupref;
  private String claucod;
  private String clauorigen;
  private String claudesc;
  private int counter;
  
  public DBCounter()
  {
  }

  public void setClaupref(String claupref)
  {
    this.claupref = claupref;
  }

  public String getClaupref()
  {
    return claupref;
  }

  public void setClaucod(String claucod)
  {
    this.claucod = claucod;
  }

  public String getClaucod()
  {
    return claucod;
  }

  public void setClauorigen(String clauorigen)
  {
    this.clauorigen = clauorigen;
  }

  public String getClauorigen()
  {
    return clauorigen;
  }

  public void setClaudesc(String claudesc)
  {
    this.claudesc = claudesc;
  }

  public String getClaudesc()
  {
    return claudesc;
  }

  public void setCounter(int counter)
  {
    this.counter = counter;
  }

  public int getCounter()
  {
    return counter;
  }
}
