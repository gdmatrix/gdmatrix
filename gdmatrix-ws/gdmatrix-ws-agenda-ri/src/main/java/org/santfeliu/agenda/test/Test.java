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
package org.santfeliu.agenda.test;

import java.net.URL;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.AgendaManagerService;
import org.matrix.agenda.Event;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author blanquepa
 */
public class Test
{
  public static void main(String[] args)
  {
    try
    {
      WSDirectory wsDirectory =
        WSDirectory.getInstance(new URL("http://localhost/wsdirectory"));
      WSEndpoint endpoint = wsDirectory.getEndpoint(AgendaManagerService.class);
      AgendaManagerPort port = endpoint.getPort(AgendaManagerPort.class, "xxxxx", "yyyyy");

//      Event event = new Event();
////      event.setEventId("2061367");
//      event.setSummary("EVENT DE PROVA 3");
//      event.setStartDateTime("20110915220000");
//      event.setEndDateTime("20110915230000");
//
//      System.out.println(port.storeEvent(event).getEventId());
      Event e = port.loadEvent("2272369");
      System.out.println(e);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
