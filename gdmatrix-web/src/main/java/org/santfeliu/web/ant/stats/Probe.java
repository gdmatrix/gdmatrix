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
package org.santfeliu.web.ant.stats;

import java.io.IOException;
import java.io.PrintWriter;

public abstract class Probe
{
  private StatisticsTask statistics;
  private String name;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public StatisticsTask getStatistics()
  {
    return statistics;
  }

  public void setStatistics(StatisticsTask statistics)
  {
    this.statistics = statistics;
  }

  public void init()
  {
  }

  public abstract void processLine(Line line);
   
  public void close()
  {    
  }
  
  public void printResult(PrintWriter writer) throws IOException
  {
    printHeader(writer);
    printBody(writer);
    printFooter(writer);
  }

  protected void printHeader(PrintWriter writer) throws IOException
  {
  }

  protected void printBody(PrintWriter writer) throws IOException
  {
  }

  protected void printFooter(PrintWriter writer) throws IOException
  {
  }
}
