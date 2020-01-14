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
package org.santfeliu.report.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import org.santfeliu.security.util.BasicAuthorization;

/**
 *
 * @author unknown
 */
public class Test
{
  public Test()
  {
  }
  
  public static void main(String[] args)
  {
    try
    {
      URL url = new URL("http://localhost/reports/prova2");
      URLConnection conn = url.openConnection();
      BasicAuthorization ba = new BasicAuthorization();
      ba.setUserId("xxxxx");
      ba.setPassword("yyyyy");
      conn.setRequestProperty("Authorization", ba.toString());

      File file = new File("c:/test.pdf");
      FileOutputStream fos = new FileOutputStream(file);
      InputStream is = conn.getInputStream();
      int ch = is.read();
      while (ch != -1)
      {
        System.out.print((char)ch);
        fos.write(ch);
        ch = is.read();
      }
      is.close();
      fos.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
