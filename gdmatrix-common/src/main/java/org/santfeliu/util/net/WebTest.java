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
package org.santfeliu.util.net;

/**
 *
 * @author realor
 */
public class WebTest
{
  private String url;

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }
  
  public void open()
  {
    Thread thread = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          System.out.println("Opening " + url + " from " + this.getName());
          HttpClient client = new HttpClient();
          client.setURL(url);
          client.doGet();
          String data = client.getContentAsString();
          if (data == null) data = "";
          System.out.println("Done from " + this.getName() + ": " + data.length());
        }
        catch (Exception ex)
        {
        }
      }
    };
    
    thread.start();
  }
  
  public void open(int loops, int num, long millis) throws InterruptedException
  {
    for (int l = 0; l < loops; l++)
    {
      for (int i = 0; i < num; i++)
      {
        open();
      }
      Thread.sleep(millis);
    }
  }
  
  public static void main(String[] args)
  {
    WebTest test = new WebTest();
    test.setUrl("http://pc04431/go.faces?xmid=1");
    try
    {
      test.open(100, 20, 1000);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
