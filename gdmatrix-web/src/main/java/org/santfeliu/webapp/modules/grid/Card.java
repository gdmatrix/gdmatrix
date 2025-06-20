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
package org.santfeliu.webapp.modules.grid;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author realor
 */
public class Card implements Serializable
{
  private String type;
  private String id;
  private String title;
  private String imageId;
  private String aspect;
  private int priority;

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getImageId()
  {
    return imageId;
  }

  public void setImageId(String imageId)
  {
    this.imageId = imageId;
  }

  public String getAspect()
  {
    return aspect;
  }

  public void setAspect(String aspect)
  {
    this.aspect = aspect;
  }

  public int getPriority()
  {
    return priority;
  }

  public void setPriority(int priority)
  {
    this.priority = priority;
  }

  public String getJsonParameters()
  {
    // TODO: improve parameter generation (extend Card class)
    Map<String, String> params = new HashMap();
    if ("Map".equals(type))
    {
      params.put("map_name", id);
    }
    else if ("New".equals(type))
    {
      params.put("newid", id);
    }
    else if ("Query".equals(type))
    {
      params.put("name", id);
    }
    else if ("Workflow".equals(type))
    {
      params.put("instanceid", id);
    }
    return new Gson().toJson(params);
  }
}
