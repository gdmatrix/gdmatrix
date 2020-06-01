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
package org.santfeliu.matrix.ide;

import java.util.Map;
import javax.swing.ImageIcon;

/**
 *
 * @author realor
 */
public class DocumentType
{
  private String displayName;
  private String documentPanelClassName;
  private String extension;
  private String propertyName;
  private String mimeType;
  private ImageIcon icon;
  private String docTypeId;
  private Map fixedProperties;

  public DocumentType()
  {
  }

  @Override
  public String toString()
  {
    return displayName;
  }

  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public void setDocumentPanelClassName(String documentPanelClassName)
  {
    this.documentPanelClassName = documentPanelClassName;
  }

  public String getDocumentPanelClassName()
  {
    return documentPanelClassName;
  }

  public void setExtension(String extension)
  {
    this.extension = extension;
  }

  public String getExtension()
  {
    return extension;
  }

  public void setPropertyName(String propertyName)
  {
    this.propertyName = propertyName;
  }

  public String getPropertyName()
  {
    return propertyName;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  public String getMimeType()
  {
    return mimeType;
  }

  public void setIcon(ImageIcon icon)
  {
    this.icon = icon;
  }

  public ImageIcon getIcon()
  {
    return icon;
  }

  public void setDocTypeId(String docTypeId)
  {
    this.docTypeId = docTypeId;
  }

  public String getDocTypeId()
  {
    return docTypeId;
  }

  public Map getFixedProperties()
  {
    return fixedProperties;
  }

  public void setFixedProperties(Map docProperties)
  {
    this.fixedProperties = docProperties;
  }
}
