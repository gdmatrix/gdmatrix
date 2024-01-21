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
package org.santfeliu.webapp.modules.geo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.web.WebBean;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.modules.doc.DocModuleBean;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoMapControlsBean extends WebBean implements Serializable
{
  public static final String SCRIPT_DOCTYPEID = "CODE";
  public static final String SCRIPT_NAME_PROPERTY = "workflow.js";
  public static final String SCRIPT_PURPOSE_PROPERTY = "purpose";
  public static final String SCRIPT_PURPOSE_VALUE = "geo";
  public static final String PROFILE_METADATA = "profile";
  public static final String SCRIPTS_METADATA = "scripts";

  private String scriptToAdd;

  private transient List<String> profiles;
  private transient List<String> scripts;

  @Inject
  GeoMapBean geoMapBean;

  public String getProfile()
  {
    Map<String, Object> metadata = geoMapBean.getStyle().getMetadata();
    return (String)metadata.get(PROFILE_METADATA);
  }

  public void setProfile(String profile)
  {
    Map<String, Object> metadata = geoMapBean.getStyle().getMetadata();
    if (StringUtils.isBlank(profile))
    {
      metadata.remove(PROFILE_METADATA);
    }
    else
    {
      metadata.put(PROFILE_METADATA, profile);
    }
  }

  public List<String> getProfiles()
  {
    if (profiles == null)
    {
      profiles = new ArrayList<>();
      String path = getFacesContext().getExternalContext().getRealPath(
        "/resources/gdmatrixfaces/maplibre/profiles/list.txt");
      File file = new File(path);
      if (file.exists())
      {
        try (BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
          String line = reader.readLine();
          while (line != null)
          {
            line = line.trim();
            if (line.length() > 0)
            {
              profiles.add(line);
            }
            line = reader.readLine();
          }
        }
        catch (IOException ex)
        {
        }
      }
    }
    return profiles;
  }

  public List<String> getScripts()
  {
    if (scripts == null)
    {
      Map<String, Object> metadata = geoMapBean.getStyle().getMetadata();
      scripts = (List<String>)metadata.get(SCRIPTS_METADATA);
      if (scripts == null)
      {
        scripts = new ArrayList<>();
        metadata.put(SCRIPTS_METADATA, scripts);
      }
    }
    return scripts;
  }

  public String getScriptToAdd()
  {
    return scriptToAdd;
  }

  public void setScriptToAdd(String scriptToAdd)
  {
    this.scriptToAdd = scriptToAdd;
  }

  public void addScript()
  {
    if (!StringUtils.isBlank(scriptToAdd))
    {
      List<String> sns = getScripts();
      if (!sns.contains(scriptToAdd))
      {
        sns.add(scriptToAdd);
      }
    }
    scriptToAdd = null;
  }

  public void removeScript(String scriptName)
  {
    getScripts().remove(scriptName);
  }

  public List<String> completeScriptName(String name)
  {
    List<String> results = new ArrayList<>();
    try
    {
      DocumentFilter filter = new DocumentFilter();
      filter.setMaxResults(100);
      filter.setDocTypeId(SCRIPT_DOCTYPEID);

      Property property = new Property();
      property.setName(SCRIPT_PURPOSE_PROPERTY);
      property.getValue().add(SCRIPT_PURPOSE_VALUE);
      filter.getProperty().add(property);

      property = new Property();
      property.setName(SCRIPT_NAME_PROPERTY);
      property.getValue().add("%" + name + "%");
      filter.getProperty().add(property);

      filter.getOutputProperty().add(SCRIPT_NAME_PROPERTY);
      List<Document> documents =
        getDocPort().findDocuments(filter);
      for (Document document : documents)
      {
        String value = DictionaryUtils.getPropertyValue(document.getProperty(),
          SCRIPT_NAME_PROPERTY);
        results.add(value);
      }
      Collections.sort(results);
    }
    catch (Exception ex)
    {
      // ignore
    }
    return results;
  }

  public DocumentManagerPort getDocPort()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    String password = userSessionBean.getPassword();
    return DocModuleBean.getPort(userId, password);
  }
}
