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
package org.santfeliu.webapp.modules.geo.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.security.AccessControl;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.util.template.WebTemplate;
import static org.santfeliu.webapp.modules.geo.io.MapStore.MAP_CATEGORY_NAME_PROPERTY;
import static org.santfeliu.webapp.modules.geo.io.MapStore.MAP_DESCRIPTION_PROPERTY;
import static org.santfeliu.webapp.modules.geo.io.MapStore.MAP_KEYWORDS_PROPERTY;
import static org.santfeliu.webapp.modules.geo.io.MapStore.MAP_NAME_PROPERTY;
import static org.santfeliu.webapp.modules.geo.io.MapStore.MAP_SUMMARY_PROPERTY;
import static org.santfeliu.webapp.modules.geo.io.MapStore.BASE_MAP_NAME_PROPERTY;
import static org.santfeliu.webapp.modules.geo.io.MapStore.MAP_FEATURED_END_DATE_PROPERTY;
import static org.santfeliu.webapp.modules.geo.io.MapStore.MAP_FEATURED_START_DATE_PROPERTY;
import static org.santfeliu.webapp.modules.geo.io.MapStore.MAP_SNAPSHOT_DOCID_PROPERTY;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.matrix.dic.DictionaryConstants.DELETE_ACTION;
import static org.matrix.dic.DictionaryConstants.READ_ACTION;
import static org.matrix.dic.DictionaryConstants.WRITE_ACTION;

/**
 *
 * @author realor
 */
public class MapDocument implements Serializable
{
  String name;
  String title;
  String summary;
  String baseMapName;
  String description;
  String keywords;
  String categoryName;
  String creationDate;
  String captureUserId;
  String captureDateTime;
  String changeUserId;
  String changeDateTime;
  String featuredStartDate;
  String featuredEndDate;
  String snapshotDocId;
  Style style; // MapLibre style
  final List<Property> property = new ArrayList<>();
  final List<AccessControl> accessControl = new ArrayList<>();

  public MapDocument()
  {
    style = new Style();
    name = "new_map";
    title = "New map";
  }

  public MapDocument(Style style)
  {
    this.style = style;
  }

  public MapDocument(Document document) throws IOException
  {
    InputStream is = document.getContent().getData().getInputStream();
    this.style = new Style();
    this.style.read(new InputStreamReader(is, "UTF-8"));
    this.readProperties(document);
    this.setAccessControl(document.getAccessControl());
  }

  public Style getStyle()
  {
    return style;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getSummary()
  {
    return summary;
  }

  public void setSummary(String summary)
  {
    this.summary = summary;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getBaseMapName()
  {
    return baseMapName;
  }

  public void setBaseMapName(String baseMapName)
  {
    this.baseMapName = baseMapName;
  }

  public String getMergedSummary()
  {
    String sum = summary == null ? "" : summary;
    return mergeMapTemplate(sum);
  }

  public String getMergedSummaryAndDescription()
  {
    String sum = summary == null ? "" : summary;
    String desc = description == null ? "" : description;
    return mergeMapTemplate(sum + desc);
  }

  public String getKeywords()
  {
    return keywords;
  }

  public void setKeywords(String keywords)
  {
    this.keywords = keywords;
  }

  public String getCategoryName()
  {
    return categoryName;
  }

  public void setCategoryName(String categoryName)
  {
    if (isBlank(categoryName)) categoryName = null;
    this.categoryName = categoryName;
  }

  public String getCreationDate()
  {
    return creationDate;
  }

  public void setCreationDate(String creationDate)
  {
    this.creationDate = creationDate;
  }

  public String getCaptureUserId()
  {
    return captureUserId;
  }

  public void setCaptureUserId(String captureUserId)
  {
    this.captureUserId = captureUserId;
  }

  public String getCaptureDateTime()
  {
    return captureDateTime;
  }

  public void setCaptureDateTime(String captureDateTime)
  {
    this.captureDateTime = captureDateTime;
  }

  public String getChangeUserId()
  {
    return changeUserId;
  }

  public void setChangeUserId(String changeUserId)
  {
    this.changeUserId = changeUserId;
  }

  public String getChangeDateTime()
  {
    return changeDateTime;
  }

  public void setChangeDateTime(String changeDateTime)
  {
    this.changeDateTime = changeDateTime;
  }

  public String getFeaturedStartDate()
  {
    return featuredStartDate;
  }

  public void setFeaturedStartDate(String featuredStartDate)
  {
    this.featuredStartDate = featuredStartDate;
  }

  public String getFeaturedEndDate()
  {
    return featuredEndDate;
  }

  public void setFeaturedEndDate(String featuredEndDate)
  {
    this.featuredEndDate = featuredEndDate;
  }

  public String getSnapshotDocId()
  {
    return snapshotDocId;
  }

  public void setSnapshotDocId(String snapshotDocId)
  {
    this.snapshotDocId = snapshotDocId;
  }

  public List<Property> getProperty()
  {
    return property;
  }

  public List<AccessControl> getFullAccessControl()
  {
    List<AccessControl> fullAcl = new ArrayList<>();
    for (AccessControl ac : accessControl)
    {
      String roleId = ac.getRoleId();
      String action = ac.getAction();
      fullAcl.add(ac);
      if (WRITE_ACTION.equals(action) ||
          DELETE_ACTION.equals(action))
      {
        AccessControl nac = new AccessControl();
        nac.setRoleId(roleId);
        nac.setAction(READ_ACTION);
        fullAcl.add(ac);
      }
      else if (DELETE_ACTION.equals(action))
      {
        AccessControl nac = new AccessControl();
        nac.setRoleId(roleId);
        nac.setAction(WRITE_ACTION);
        fullAcl.add(ac);
      }
    }
    return fullAcl;
  }

  public List<AccessControl> getAccessControl()
  {
    return accessControl;
  }

  public final void setAccessControl(List<AccessControl> acl)
  {
    HashMap<String, AccessControl> roles = new HashMap<>();

    for (AccessControl ac : acl)
    {
      String roleId = ac.getRoleId();
      String action = ac.getAction();
      AccessControl prevAc = roles.get(roleId);

      if (prevAc == null ||
          (READ_ACTION.equals(prevAc.getAction()) &&
           (WRITE_ACTION.equals(action) || DELETE_ACTION.equals(action))) ||
          (WRITE_ACTION.equals(prevAc.getAction()) && DELETE_ACTION.equals(action)))
      {
        roles.put(roleId, ac);
      }
    }
    this.accessControl.clear();
    this.accessControl.addAll(roles.values());
  }

  public List<String> getWriteRoles()
  {
    List<String> roles = new ArrayList<>();
    for (AccessControl ac : accessControl)
    {
      if (WRITE_ACTION.equals(ac.getAction()) ||
          DELETE_ACTION.equals(ac.getAction()))
      {
        roles.add(ac.getRoleId());
      }
    }
    return roles;
  }

  public String mergeMapTemplate(String templateSource)
  {
    WebTemplate template = WebTemplate.create(templateSource);
    HashMap<String, Object> variables = new HashMap<>();
    for (Property prop : getProperty())
    {
      variables.put(prop.getName(), prop.getValue().get(0));
    }
    variables.put("title", getTitle());
    variables.put("creationDate", getCreationDate());
    variables.put("captureDateTime", getCaptureDateTime());
    variables.put("changeDateTime", getChangeDateTime());
    variables.put("captureUserId", getCaptureUserId());
    variables.put("changeUserId", getChangeUserId());
    return template.merge(variables);
  }

  public final void readProperties(Document document)
  {
    this.title = document.getTitle();
    this.creationDate = document.getCreationDate();
    this.changeUserId = document.getChangeUserId();
    this.changeDateTime = document.getChangeDateTime();
    this.captureUserId = document.getCaptureUserId();
    this.captureDateTime = document.getCaptureDateTime();

    getProperty().clear();
    for (Property documentProperty : document.getProperty())
    {
      String propertyName = documentProperty.getName();
      String value = documentProperty.getValue().get(0);
      switch (propertyName)
      {
        case MAP_NAME_PROPERTY:
          name = value;
          break;
        case BASE_MAP_NAME_PROPERTY:
          baseMapName = value;
          break;
        case MAP_SUMMARY_PROPERTY:
          summary = value;
          break;
        case MAP_DESCRIPTION_PROPERTY:
          description = value;
          break;
        case MAP_KEYWORDS_PROPERTY:
          keywords = value;
          break;
        case MAP_CATEGORY_NAME_PROPERTY:
          categoryName = value;
          break;
        case MAP_SNAPSHOT_DOCID_PROPERTY:
          snapshotDocId = value;
          break;
        case MAP_FEATURED_START_DATE_PROPERTY:
          featuredStartDate = value;
          break;
        case MAP_FEATURED_END_DATE_PROPERTY:
          featuredEndDate = value;
          break;
        default:
          getProperty().add(documentProperty);
      }
    }
  }
}