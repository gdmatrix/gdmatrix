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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author realor
 */
public class IdeDocumentType implements Serializable
{
  static final Map<String, IdeDocumentType> typeCache = new HashMap<>();

  String typeName;
  String label;
  String docTypeId;
  String docProperty;
  String docContentType;
  String icon;
  List<Tab> tabs = new ArrayList<>();

  public IdeDocumentType(String typeName, String label,
    String docTypeId, String docProperty, String docContentType,
    String icon, Tab ...tabs)
  {
    this.typeName = typeName;
    this.label = label;
    this.docTypeId = docTypeId;
    this.docProperty = docProperty;
    this.docContentType = docContentType;
    this.icon = icon;
    for (Tab tab : tabs)
    {
      this.tabs.add(tab);
    }
  }

  public String getTypeName()
  {
    return typeName;
  }

  public void setTypeName(String typeName)
  {
    this.typeName = typeName;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getDocTypeId()
  {
    return docTypeId;
  }

  public void setDocTypeId(String docTypeId)
  {
    this.docTypeId = docTypeId;
  }

  public String getDocProperty()
  {
    return docProperty;
  }

  public void setDocProperty(String docProperty)
  {
    this.docProperty = docProperty;
  }

  public String getDocContentType()
  {
    return docContentType;
  }

  public void setDocContentType(String docContentType)
  {
    this.docContentType = docContentType;
  }

  public String getIcon()
  {
    return icon;
  }

  public void setIcon(String icon)
  {
    this.icon = icon;
  }

  public List<Tab> getTabs()
  {
    return tabs;
  }

  static void registerType(String typeName, String label,
    String docType, String docProperty, String docContentType,
    String icon, Tab ...tabs)
  {
    typeCache.put(typeName, new IdeDocumentType(typeName, label, docType,
      docProperty, docContentType, icon, tabs));
  }

  static
  {
    Tab metadata = new Tab("metadata_editor.xhtml", "Metadata", "pi pi-list");

    registerType("javascript", "Javascript", "CODE", "workflow.js",
      "text/javascript", "fa fa-brands fa-js",
      new Tab("javascript_editor.xhtml", "JS Editor", "fa fa-brands fa-js"),
      new Tab("javascript_runner.xhtml", "JS Runner", "pi pi-play"),
      metadata);

    registerType("html", "HTML form", "FORM", "workflow.html",
      "text/html", "fa fa-brands fa-html5",
      new Tab("html_editor.xhtml", "HTML Editor", "fa fa-brands fa-html5"),
      new Tab("html_preview.xhtml", "HTML preview", "pi pi-eye"),
      metadata);

    registerType("ObjectSetup", "Object setup", "ObjectSetup", "setupName",
      "application/json", "fa fa-gear",
      new Tab("json_editor.xhtml", "JSON Editor", "fa fa-gear"), metadata);
  }

  public static List<IdeDocumentType> getTypes()
  {
    return new ArrayList<>(typeCache.values());
  }

  public static IdeDocumentType getInstance(String typeName)
  {
    return typeCache.get(typeName);
  }

  public static class Tab implements Serializable
  {
    private String label;
    private String icon;
    private String viewId;

    public Tab(String viewId, String label, String icon)
    {
      this.viewId = viewId;
      this.label = label;
      this.icon = icon;
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public String getIcon()
    {
      return icon;
    }

    public void setIcon(String icon)
    {
      this.icon = icon;
    }

    public String getViewId()
    {
      return viewId;
    }

    public void setViewId(String viewId)
    {
      this.viewId = viewId;
    }
  }
}
