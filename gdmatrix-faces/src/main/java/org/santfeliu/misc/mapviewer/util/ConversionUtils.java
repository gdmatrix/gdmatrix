package org.santfeliu.misc.mapviewer.util;

import org.apache.commons.lang.StringUtils;
import org.santfeliu.misc.mapviewer.expr.Expression;
import org.santfeliu.misc.mapviewer.io.CQLReader;
import org.santfeliu.misc.mapviewer.io.CQLWriter;
import org.santfeliu.misc.mapviewer.io.OGCReader;
import org.santfeliu.misc.mapviewer.io.OGCWriter;

/**
 *
 * @author realor
 */
public class ConversionUtils
{
  public static String xmlToCql(String xml)
  {
    if (StringUtils.isBlank(xml)) return null;

    OGCReader ogcReader = new OGCReader();
    Expression expression = ogcReader.fromString(xml);
    CQLWriter cqlWriter = new CQLWriter();
    return cqlWriter.toString(expression);
  }

  public static String cqlToXml(String cql)
  {
    if (StringUtils.isBlank(cql)) return null;

    CQLReader cqlReader = new CQLReader();
    Expression expression = cqlReader.fromString(cql);
    OGCWriter ogcWriter = new OGCWriter();
    ogcWriter.setPrefix("ogc"); // TODO: set correct prefix
    return ogcWriter.toString(expression);
  }

  public static void main(String[] args)
  {
    System.out.println(ConversionUtils.xmlToCql("<ogc:Literal>&#234;</ogc:Literal>"));
  }
}
