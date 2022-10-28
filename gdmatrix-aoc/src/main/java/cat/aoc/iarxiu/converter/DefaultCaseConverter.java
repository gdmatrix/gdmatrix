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
package cat.aoc.iarxiu.converter;

import java.util.HashMap;
import java.util.List;
import org.matrix.security.AccessControl;
import org.matrix.cases.Case;
import org.matrix.dic.Property;
import cat.aoc.iarxiu.mets.Metadata;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class DefaultCaseConverter implements Converter
{
  private HashMap<String, String> properties = new HashMap<>();

  public void setProperty(String name, String value)
  {
    properties.put(name, value);
  }

  public String getProperty(String name)
  {
    return properties.get(name);
  }

  @Override
  public void convert(Object src, Metadata md) throws Exception
  {
    if (!(src instanceof Case))
      throw new Exception("Invalid source class: " + src.getClass());

    Case cas = ((Case)src);

    //Mandatory
    md.setProperty("codi_referencia", cas.getCaseId());
    md.setProperty("numero_expedient", cas.getCaseId());

    String classId = "UNDEFINED";
    List<String> classIdList = cas.getClassId();
    if (classIdList != null && classIdList.size() > 0)
      classId = classIdList.get(0);
    md.setProperty("codi_classificacio", classId);

    String classTitle = getValue(cas, "classTitle");
    md.setProperty("titol_serie_documental",
      classTitle != null ? classTitle : classId);
    md.setProperty("nivell_descripcio", "Unitat documental composta");
    md.setProperty("titol", cas.getTitle());

    String startDate = TextUtils.formatDateAsISO8601String(
      TextUtils.parseInternalDate(cas.getStartDate()));
    md.setProperty("data_obertura", startDate);

    if (cas.getEndDate() == null)
      throw new Exception("Case not closed");
    String endDate = TextUtils.formatDateAsISO8601String(
      TextUtils.parseInternalDate(cas.getEndDate()));
    md.setProperty("data_tancament", endDate);

    md.setProperty("nom_productor", "Ajuntament de Sant Feliu de Llobregat");
    md.setProperty("classificacio_seguretat_acces",
      isPublic(cas) ? "Accés públic" : "Accés restringit");

    //Optional
//      md.setProperty("unitat_productora",);
    md.setProperty("descripcio", cas.getDescription());
//      md.setProperty("descriptors", );
//      md.setProperty("documentacio_relacionada");
//      md.setProperty("tipus_relacio");
//      md.setProperty("sensibilitat_dades_LOPD",);

    for (String propertyName : properties.keySet())
    {
      String propertyValue = properties.get(propertyName);
      md.setProperty(propertyName, propertyValue);
    }
  }

  private String getValue(Case cas, String name)
  {
    String value = null;
    List<Property> properties = cas.getProperty();
    if (properties != null)
    {
      for (Property p : properties)
      {
        if (name.equals(p.getName()))
          value = p.getValue().get(0);
      }
    }

    return value;
  }

  private static boolean isPublic(Case cas)
  {
    List<AccessControl> acl = cas.getAccessControl();
    for(AccessControl ac : acl)
    {
      String action = ac.getAction();
      String roleId = ac.getRoleId();
      if ("Read".equals(action) && "EVERYONE".equals(roleId))
        return true;
    }

    return false;
  }
}
