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
package org.santfeliu.misc.mapviewer.util;

import java.net.URL;
import java.text.Normalizer;
import java.util.List;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.misc.mapviewer.MapDocument;
import org.santfeliu.misc.mapviewer.MapStore;
import org.santfeliu.misc.mapviewer.MapView;

/**
 *
 * @author realor
 */
public class MapSRSUpdater
{
  public MapSRSUpdater()
  {
  }

  public void updateSRS(String pattern, String srs) throws Exception
  {
    DocumentManagerClient client = new DocumentManagerClient(
      new URL("http://localhost/wsdirectory"), "admin", "****");
    MapStore store = new MapStore(client);
    System.out.println("Executing...");
    List<MapView> mapViews = store.findMapViews(null, pattern, null, 0, 200);
    System.out.println("Processing results:");    
    for (MapView mapView : mapViews)
    {
      String mapName = mapView.getName();
      MapDocument map = store.loadMap(mapName);
      System.out.println("Map: " + mapName + " " + map.getCaptureUserId());
      map.setSrs(srs);
      store.storeMap(map);
      Thread.sleep(500);
    }
  }

  public String stripAccents(String s) 
  {
    s = Normalizer.normalize(s, Normalizer.Form.NFD);
    s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    return s;
  }  
  
  public static void main(String[] args)
  {
    try
    {
      MapSRSUpdater updater = new MapSRSUpdater();
      updater.updateSRS("prova", "EPSG:23031");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
