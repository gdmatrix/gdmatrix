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
