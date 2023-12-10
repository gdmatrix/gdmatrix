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
package org.santfeliu.webapp.modules.geo.util;

import static org.apache.commons.lang.StringUtils.isBlank;
import org.santfeliu.webapp.modules.geo.expr.Expression;
import org.santfeliu.webapp.modules.geo.io.CqlReader;
import org.santfeliu.webapp.modules.geo.io.CqlWriter;
import org.santfeliu.webapp.modules.geo.io.OgcReader;
import org.santfeliu.webapp.modules.geo.io.OgcWriter;

/**
 *
 * @author realor
 */
public class ConversionUtils
{
  public static String xmlToCql(String xml)
  {
    if (isBlank(xml)) return null;

    OgcReader ogcReader = new OgcReader();
    Expression expression = ogcReader.fromString(xml);
    CqlWriter cqlWriter = new CqlWriter();
    return cqlWriter.toString(expression);
  }

  public static String cqlToXml(String cql)
  {
    if (isBlank(cql)) return null;

    CqlReader cqlReader = new CqlReader();
    Expression expression = cqlReader.fromString(cql);
    OgcWriter ogcWriter = new OgcWriter();
    ogcWriter.setPrefix("ogc"); // TODO: set correct prefix
    return ogcWriter.toString(expression);
  }

  public static void main(String[] args)
  {
    System.out.println(ConversionUtils.xmlToCql("<ogc:Literal>&#234;</ogc:Literal>"));
  }
}
