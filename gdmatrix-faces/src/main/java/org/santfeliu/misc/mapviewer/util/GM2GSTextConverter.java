package org.santfeliu.misc.mapviewer.util;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;

/**
 *
 * @author realor
 */
public class GM2GSTextConverter
{
  private String jdbcDriver;
  private String jdbcUrl;
  private String username;
  private String password;
  private Connection connection;

  public String getJdbcDriver()
  {
    return jdbcDriver;
  }

  public void setJdbcDriver(String jdbcDriver)
  {
    this.jdbcDriver = jdbcDriver;
  }

  public String getJdbcUrl()
  {
    return jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl)
  {
    this.jdbcUrl = jdbcUrl;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  private List<GeomediaTextLayer> getGeomediaTextLayers(String pattern)
    throws Exception
  {
    List<GeomediaTextLayer> layers = new ArrayList<GeomediaTextLayer>();
    String query =
    "select m1.table_name, m1.column_name, m2.column_name " +
    "from gdosys.gfieldmapping m1, gdosys.gfieldmapping m2 " +
    "where m1.owner = m2.owner and m1.table_name = m2.table_name and " +
    "m1.data_type = 4 and m2.data_type = 33 and m1.owner = ? " +
    "and m1.table_name like ?";
    PreparedStatement ps = connection.prepareStatement(query);
    try
    {
      ps.setString(1, username.toUpperCase());
      ps.setString(2, pattern.toUpperCase());
      ResultSet rs = ps.executeQuery();
      try
      {
        while (rs.next())
        {
          GeomediaTextLayer layer = new GeomediaTextLayer();
          layer.layerName = rs.getString(1);
          layer.idFieldName = rs.getString(2);
          layer.geometryFieldName = rs.getString(3);
          layers.add(layer);
          System.out.println(layer);
        }
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      ps.close();
    }
    return layers;
  }

  private void createGeoServerTextLayer(GeomediaTextLayer layer)
    throws Exception
  {
    String gsLayerName = layer.layerName + "_GS";
    if (gsLayerName.length() <= 32)
    {
      if (!existsLayer(gsLayerName))
      {
        createLayer(gsLayerName);
      }
      else
      {
        truncateLayer(gsLayerName);
      }
    }
  }

  private boolean existsLayer(String layerName) throws Exception
  {
    String query =
      "SELECT 1 FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = ?";
    PreparedStatement ps = connection.prepareStatement(query);
    try
    {
      ps.setString(1, layerName);
      ResultSet rs = ps.executeQuery();
      try
      {
        return (rs.next());
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      ps.close();
    }
  }

  private void createLayer(String layerName) throws Exception
  {
    System.out.println("Creating table " + layerName);
    String sql = "CREATE TABLE " + layerName + " (ID NUMBER NOT NULL, " +
      "GEOMETRY MDSYS.SDO_GEOMETRY, " +
      "TEXT VARCHAR2(1024), " +
      "ROTATION NUMBER, " +
      "ANCHORX NUMBER, " +
      "ANCHORY NUMBER, " +
      "CONSTRAINT " + layerName + "_PK PRIMARY KEY (ID) ENABLE)";
    Statement stmt = connection.createStatement();
    try
    {
      stmt.execute(sql);
    }
    finally
    {
      stmt.close();
    }

    System.out.println("Registering metadata for " + layerName);
    sql = "INSERT INTO USER_SDO_GEOM_METADATA " +
      "(table_name, column_name, diminfo) " +
      "values (?,'GEOMETRY', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',-2147483648,2147483647,0.000005)," +
      "MDSYS.SDO_DIM_ELEMENT('Y',-2147483648,2147483647,0.000005),MDSYS.SDO_DIM_ELEMENT('Z',-2147483648,2147483647,0.000005)))";
    PreparedStatement ps = connection.prepareStatement(sql);
    try
    {
      ps.setString(1, layerName.toUpperCase());
      ps.executeUpdate();
    }
    finally
    {
      ps.close();
    }

    System.out.println("Creating spatial index for " + layerName);
    sql = "CREATE INDEX " + layerName + "_IDX ON " + layerName +
      "(GEOMETRY) INDEXTYPE IS MDSYS.SPATIAL_INDEX";
    ps = connection.prepareStatement(sql);
    try
    {
      ps.execute();
    }
    finally
    {
      ps.close();
    }
  }

  private void truncateLayer(String gsLayerName) throws Exception
  {
    System.out.println("Truncating table " + gsLayerName);
    String sql = "TRUNCATE TABLE " + gsLayerName;
    Statement stmt = connection.createStatement();
    try
    {
      stmt.execute(sql);
    }
    finally
    {
      stmt.close();
    }
  }

  private void convertTextLayer(GeomediaTextLayer layer) throws Exception
  {
    String gsLayerName = layer.layerName + "_GS";

    String insert = "INSERT INTO " + gsLayerName +
      "(ID, GEOMETRY, TEXT, ROTATION, ANCHORX, ANCHORY) VALUES " +
      "(?, MDSYS.SDO_GEOMETRY(3001, NULL, MDSYS.SDO_POINT_TYPE(?, ?, 0), NULL, NULL), ?, ?, ?, ?)";
    PreparedStatement ups = connection.prepareStatement(insert);
    try
    {
      String sql = "SELECT " + layer.idFieldName + "," +
        layer.geometryFieldName + " FROM " + layer.layerName;
      PreparedStatement ps = connection.prepareStatement(sql);
      try
      {
        ResultSet rs = ps.executeQuery();
        try
        {
          int i = 0;
          while (rs.next())
          {
            String id = rs.getString(1);
            Struct geometry = (Struct)rs.getObject(2);
            GeoServerText gsText = createTextElement(geometry);
            if (gsText != null)
            {
              System.out.println("#" + i + ": " + gsText.text + " " + gsText.x + " " + gsText.y);
              ups.setString(1, id);
              ups.setDouble(2, gsText.x);
              ups.setDouble(3, gsText.y);
              ups.setString(4, gsText.text);
              ups.setDouble(5, gsText.rotation);
              ups.setDouble(6, gsText.anchorX);
              ups.setDouble(7, gsText.anchorY);
              ups.executeUpdate();
              i++;
            }
          }
        }
        finally
        {
          rs.close();
        }
      }
      finally
      {
        ps.close();
      }
    }
    finally
    {
      ups.close();
    }
  }

  public void convert(String pattern) throws Exception
  {
    Class.forName(jdbcDriver);
    connection = DriverManager.getConnection(jdbcUrl, username, password);
    try
    {
      List<GeomediaTextLayer> layers = getGeomediaTextLayers(pattern);
      for (GeomediaTextLayer layer : layers)
      {
        System.out.println(layer.layerName);
        createGeoServerTextLayer(layer);
        convertTextLayer(layer);
      }
    }
    finally
    {
      connection.close();
    }
  }

  private GeoServerText createTextElement(Struct geometry) throws Exception
  {
    if (geometry == null) return null;
    Object[] at = geometry.getAttributes();
    BigDecimal SDO_GTYPE = (BigDecimal)at[0];
    BigDecimal SDO_SRID = (BigDecimal)at[1];
    Struct SDO_POINT = (Struct)at[2];
    Array SDO_ELEM_INFO = (Array)at[3];
    Array SDO_ORDINATES = (Array)at[4];
    int gtype = SDO_GTYPE.intValue();
    int dim = gtype / 1000;
    int type = gtype % 1000;
    if (dim < 2) dim = 2;

    if (SDO_ELEM_INFO != null && SDO_ORDINATES != null)
    {
      BigDecimal info[] = (BigDecimal[])SDO_ELEM_INFO.getArray();
      BigDecimal ord[] = (BigDecimal[])SDO_ORDINATES.getArray();
      SdoElemInfoIterator iter = new SdoElemInfoIterator(info, ord);
      int offset = iter.getStartingOffset();
      int inter = iter.getInterpretation(); // must be 6001
      if (inter != 6001) return null;

      double rotation = -iter.getOrdinate(offset++).doubleValue();
      if (rotation < -180) rotation += 360;
      double xVector = iter.getOrdinate(offset++).doubleValue();
      double yVector = iter.getOrdinate(offset++).doubleValue();
      double zVector = iter.getOrdinate(offset++).doubleValue();
      int attributes = iter.getOrdinate(offset++).intValue();
      int length = iter.getOrdinate(offset++).intValue();
      int numElems = (int)Math.ceil((double)length / 4.0);

      StringBuilder buffer = new StringBuilder();
      for (int i = 0; i < numElems; i++)
      {
        int num = iter.getOrdinate(offset + i).intValue();
        int ch;
        do
        {
          ch = (num & 0x000000FF);
          if (ch != 0) buffer.append((char)ch);
          num = (num >> 8) & 0x00FFFFFF;
        } while (num > 0);
      }
      String text = buffer.toString();
      iter.next();

      offset = iter.getStartingOffset();
      inter = iter.getInterpretation(); // must be 1

      if (text.startsWith("{"))
      {
        try
        {
          JEditorPane pane = new JEditorPane();
          pane.setContentType("text/rtf");
          pane.read(new ByteArrayInputStream(text.getBytes()), "text/rtf");
          text = pane.getText();
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }

      double xPoint = iter.getOrdinate(offset++).doubleValue();
      double yPoint = iter.getOrdinate(offset++).doubleValue();
      
      // horizontal align
      double anchorX = 0;
      if ((attributes & 0x01000000) == 0x01000000)
        anchorX = 0;
      else if ((attributes & 0x02000000) == 0x02000000)
        anchorX = 1;
      else
        anchorX = 0.5;

      // vertical align
      double anchorY = 0;
      if ((attributes & 0x04000000) == 0x04000000)
        anchorY = 0;
      else if ((attributes & 0x08000000) == 0x08000000)
        anchorY = 1;
      else
        anchorY = 0.5;

      GeoServerText gsText = new GeoServerText();
      gsText.text = text;
      gsText.x = xPoint;
      gsText.y = yPoint;
      gsText.rotation = rotation;
      gsText.anchorX = anchorX;
      gsText.anchorY = anchorY;
      return gsText;
    }
    return null;
  }

  class GeomediaTextLayer
  {
    String layerName;
    String idFieldName;
    String geometryFieldName;

    @Override
    public String toString()
    {
      return layerName + "(" + idFieldName + ", " + geometryFieldName + ")";
    }
  }

  class SdoElemInfoIterator
  {
    int elem;
    BigDecimal[] info;
    BigDecimal[] ord;

    SdoElemInfoIterator(BigDecimal[] info, BigDecimal[] ord)
    {
      this.info = info;
      this.ord = ord;
      elem = 0;
    }

    boolean isDone()
    {
      return (elem + 3) > info.length;
    }

    int getStartingOffset()
    {
      return info[elem].intValue(); // offset is index 1-based
    }

    int getEType()
    {
      return info[elem + 1].intValue();
    }

    int getInterpretation()
    {
      return info[elem + 2].intValue();
    }

    BigDecimal getOrdinate(int offset) // offset is index 1-based
    {
      return ord[offset - 1];
    }

    void next()
    {
      elem += 3;
    }

    void previous()
    {
      elem -= 3;
    }

    @Override
    public String toString()
    {
      StringBuilder buffer = new StringBuilder("info: ");
      for (int i = 0; i < info.length; i++)
      {
        buffer.append(info[i]).append(" ");
      }
      buffer.append("\nord: ");
      for (int i = 0; i < ord.length; i++)
      {
        buffer.append(ord[i]).append(" ");
      }
      return buffer.toString();
    }
  }

  class GeoServerText
  {
    String text;
    double x;
    double y;
    double rotation;
    double anchorX;
    double anchorY;
  }

  public static void main(String[] args)
  {
    try
    {
      GM2GSTextConverter converter = new GM2GSTextConverter();
      converter.setJdbcDriver(args[0]);
      converter.setJdbcUrl(args[1]);
      converter.setUsername(args[2]);
      converter.setPassword(args[3]);
      converter.convert(args[4]);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
