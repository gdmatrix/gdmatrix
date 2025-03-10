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
package org.santfeliu.webapp.modules.ide.doc;

import java.io.Serializable;
import java.util.List;
import org.matrix.security.AccessControl;

/**
 *
 * @author realor
 */
public class IdeDocument implements Serializable
{
  String typeName;
  String name;
  String title;
  String source;
  String docId;
  int version;
  String metadata;
  List<AccessControl> accessControl;

  public static String getReference(String typeName, String name)
  {
    if (typeName == null || name == null) return null;
    return typeName + "/" + name;
  }

  public static String getTypeName(String reference)
  {
    int index = reference.indexOf("/");
    return reference.substring(0, index);
  }

  public static String getName(String reference)
  {
    int index = reference.indexOf("/");
    return reference.substring(index + 1);
  }

  public String getReference()
  {
    return getReference(typeName, name);
  }

  public String getTypeName()
  {
    return typeName;
  }

  public void setTypeName(String typeName)
  {
    this.typeName = typeName;
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

  public String getSource()
  {
    return source;
  }

  public void setSource(String source)
  {
    this.source = source;
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public int getVersion()
  {
    return version;
  }

  public void setVersion(int version)
  {
    this.version = version;
  }

  public String getMetadata()
  {
    return metadata;
  }

  public void setMetadata(String metadata)
  {
    this.metadata = metadata;
  }

  public List<AccessControl> getAccessControl()
  {
    return accessControl;
  }

  public void setAccessControl(List<AccessControl> accessControl)
  {
    this.accessControl = accessControl;
  }
}
