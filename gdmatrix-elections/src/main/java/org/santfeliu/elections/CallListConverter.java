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

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.matrix.elections.Call;

import org.matrix.elections.ElectionsConstants;

import org.santfeliu.util.Table;

/**
 *
 * @author unknown
 */
public class CallListConverter
{
  public CallListConverter()
  {
  }

  public static List<Call> fromTable(Table table)
  {
    List<Call> result = new ArrayList();
    if (table != null)
    {
      for (int i = 0; i < table.getRowCount(); i++)
      {
        Table.Row row = table.getRow(i);
        result.add(fromRow(row));
      }
    }
    
    return result;    
  }
  
  public static Table toTable(List<Call> callList)
  {
    Table result = new Table(new String[]{ElectionsConstants.CALLID, 
      ElectionsConstants.CALLDESC, ElectionsConstants.CALLDATE, 
      ElectionsConstants.COUNCILLORSCOUNT});
    
    for (Call row : callList)
    {
      result.addRow(new Object[]{row.getCallId(), row.getDescription(), 
        row.getDate(), row.getCouncillorsCount()});
    }
    
    return result;
  }  
  
  private static Call fromRow(Table.Row row)
  {
    Call call = new Call();
    call.setCallId((String)row.get(ElectionsConstants.CALLID));
    call.setDescription((String)row.get(ElectionsConstants.CALLDESC));
    call.setDate((XMLGregorianCalendar)row.get(ElectionsConstants.CALLDATE));
    call.setCouncillorsCount(((Integer)row.get(ElectionsConstants.COUNCILLORSCOUNT)).intValue());
    
    return call;
  }
}
