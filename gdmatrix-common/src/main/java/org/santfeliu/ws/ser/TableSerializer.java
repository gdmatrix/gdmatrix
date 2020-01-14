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
package org.santfeliu.ws.ser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.santfeliu.util.Table;


/**
 *
 * @author unknown
 */
public class TableSerializer
{
  public static byte[] toByteArray(Table table)
  {
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream os = new ObjectOutputStream(bos);
      os.writeObject(table);
      os.close();
      return bos.toByteArray();
    }
    catch (Exception ex)
    {
      return new byte[0]; // empty array
    }
  }
  
  public static Table toTable(byte[] byteArray)
  {
    Table table = null;
    try
    {
      ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
      ObjectInputStream ois = new ObjectInputStream(bis);
      table = (Table)ois.readObject();
      ois.close();
    }
    catch (Exception ex)
    {
      table = new Table(new String[0]); // empty table
    }
    return table;
  }
  
  public static void main(String[] args)
  {
    Table table = new Table(new String[]{"a", "b", "c"});
    
    table.addRow(new Object[]{"2", "0", 4});
    table.addRow(new Object[]{"6", "9", 3});
    table.addRow(new Object[]{"3", "5", 1});
    table.addRow(new Object[]{"2", "2", 2});
    table.addRow(new Object[]{"5", "1", 8});
    
    byte[] a = TableSerializer.toByteArray(table);
    table = TableSerializer.toTable(a);
    
    System.out.println(table);
  }
}
