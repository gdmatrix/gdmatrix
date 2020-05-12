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
