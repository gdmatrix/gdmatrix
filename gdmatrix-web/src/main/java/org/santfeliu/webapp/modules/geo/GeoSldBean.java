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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.geo.io.SldReader;
import org.santfeliu.webapp.modules.geo.io.SldStore;
import static org.santfeliu.webapp.modules.geo.io.SldStore.SLD_ENCODING;
import org.santfeliu.webapp.modules.geo.io.SldWriter;
import org.santfeliu.webapp.modules.geo.sld.*;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class GeoSldBean extends WebBean implements Serializable
{
  String sldName;
  SldRoot sld;
  boolean sldNameChanged;
  Set<String> visibleLayers = new HashSet<>();

  @PostConstruct
  public void init()
  {
    newSld();
  }

  public String getSldName()
  {
    return sldName;
  }

  public void setSldName(String sldName)
  {
    this.sldName = sldName;
  }

  public SldRoot getSld()
  {
    return sld;
  }

  public String getXmlSld()
  {
    try
    {
      SldWriter writer = new SldWriter();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      writer.write(sld, bos);
      return bos.toString(SLD_ENCODING);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public void setXmlSld(String xml)
  {
    try
    {
      SldReader reader = new SldReader();
      ByteArrayInputStream bis =
        new ByteArrayInputStream(xml.getBytes(SLD_ENCODING));
      sld = reader.read(bis);
    }
    catch (Exception ex)
    {
    }
  }

  public boolean isPanelExpanded(SldNode node)
  {
    return node.getCustomData() != null;
  }

  public void expandPanel(SldNode node)
  {
    node.setCustomData(true);
  }

  public void collapsePanel(SldNode node)
  {
    node.setCustomData(null);
  }

  public boolean isNamedLayerVisible()
  {
    SldNamedLayer namedLayer = (SldNamedLayer)getValue("#{namedLayer}");
    return visibleLayers.isEmpty() ||
      visibleLayers.contains(namedLayer.getLayerName());
  }

  public int getNamedLayerCount()
  {
    return sld.getNamedLayers().size();
  }

  public int getNamedLayerIndex()
  {
    SldNamedLayer namedLayer = (SldNamedLayer)getValue("#{namedLayer}");
    return sld.getNamedLayers().indexOf(namedLayer);
  }

  public boolean isFocusingNamedLayer()
  {
    return !visibleLayers.isEmpty();
  }

  public String getMarkerIcon(String symbol)
  {
    switch (symbol)
    {
      case "circle": return "pi pi-circle";
      case "cross": return "pi pi-plus";
      case "square": return "pi pi-stop";
      case "star": return "pi pi-star";
      case "triangle": return "pi pi-caret-up";
      case "x": return "pi pi-times";
      case "shape://horline": return "pi pi-minus";
      case "shape://vertline": return "pi pi-info";
      case "shape://dot": return "pi pi-circle";
      case "shape://plus": return "pi pi-plus";
      case "shape://times": return "pi pi-times";
      case "shape://slash": return "fa fa-slash";
    }
    return "";
  }

  public String getRuleSummary()
  {
    SldRule rule = (SldRule)getValue("#{rule}");
    String minScale = rule.getMinScaleDenominator();
    String maxScale = rule.getMaxScaleDenominator();
    String filter = rule.getFilterAsCql();
    StringBuilder buffer = new StringBuilder();
    if (!StringUtils.isBlank(minScale) || !StringUtils.isBlank(maxScale))
    {
      buffer.append("Scales:[");
      if (!StringUtils.isBlank(minScale))
      {
        buffer.append(minScale);
      }
      buffer.append("..");
      if (!StringUtils.isBlank(maxScale))
      {
        buffer.append(maxScale);
      }
      buffer.append("]");
    }
    if (!StringUtils.isBlank(filter))
    {
      buffer.append(" Filter: ");
      buffer.append(filter);
    }
    return buffer.toString();
  }

  public String getSymbolizerIcon(SldSymbolizer symbolizer)
  {
    switch (symbolizer.getSymbolizerType())
    {
      case "Point" : return "fa fa-location-dot";
      case "Line" : return "fa fa-slash";
      case "Polygon" : return "fa fa-vector-square";
      case "Text" : return "fa fa-t";
    }
    return null;
  }

  public String getSymbolizerSummary(SldSymbolizer symbolizer)
  {
    String className = getSymbolizerIcon(symbolizer);
    if (className == null) return "";

    StringBuilder buffer = new StringBuilder();
    buffer.append("<span class=\"").append(className)
      .append("\" style=\"width:16px\"></span> ");
    switch (symbolizer.getSymbolizerType())
    {
      case "Point": buffer.append(getPointSymbolizerSummary()); break;
      case "Line": buffer.append(getLineSymbolizerSummary()); break;
      case "Polygon": buffer.append(getPolygonSymbolizerSummary()); break;
      case "Text": buffer.append(getTextSymbolizerSummary()); break;
    }
    return buffer.toString();
  }

  public String getPointSymbolizerSummary()
  {
    SldPointSymbolizer symbolizer =
      (SldPointSymbolizer)getValue("#{symbolizer}");
    SldGraphic graphic = symbolizer.getGraphic();
    SldMark mark = graphic.getMark();

    StringBuilder buffer = new StringBuilder();
    SldExternalGraphic externalGraphic = graphic.getExternalGraphic();
    String resource = externalGraphic.getOnlineResource();

    if (!StringUtils.isBlank(resource) &&
        !resource.contains("//chart?") &&
        !resource.contains("${"))
    {
      // adapt icon url
      resource = resource.replaceAll("http://", "https://");
      if (resource.contains("/documents/"))
      {
        if (!resource.contains("?")) resource += "?cache=1000000";
        else resource += "&cache=1000000";
      }
      buffer.append("<img src=\"");
      buffer.append(resource);
      buffer.append("\" alt=\"\" class=\"icon\" style=\"width:16px\">");
    }
    if (!StringUtils.isBlank(mark.getWellKnownName()))
    {
      SldFill fill = mark.getFill();
      SldStroke stroke = mark.getStroke();
      buffer.append(getBox(fill, stroke));

      String className = getMarkerIcon(mark.getWellKnownName());
      if (!StringUtils.isBlank(className))
      {
        buffer.append("<span class=\"").append(className).append("\"></span> ");
      }
      buffer.append(mark.getWellKnownName());
    }

    String size = graphic.getSizeAsCql();
    if (!StringUtils.isBlank(size))
    {
      buffer.append(" ");
      buffer.append(size);
    }
    return buffer.toString();
  }

  public String getLineSymbolizerSummary()
  {
    SldLineSymbolizer symbolizer =
      (SldLineSymbolizer)getValue("#{symbolizer}");
    SldStroke stroke = symbolizer.getStroke();

    StringBuilder buffer = new StringBuilder();
    if (!StringUtils.isBlank(stroke.getStrokeColor()))
    {
      buffer.append(getBox(stroke.getStrokeColor()));
    }
    if (!StringUtils.isBlank(stroke.getStrokeWidth()))
    {
      buffer.append(" ");
      buffer.append(stroke.getStrokeWidth());
    }
    if (!StringUtils.isBlank(stroke.getStrokeDashArray()))
    {
      buffer.append(" [");
      buffer.append(stroke.getStrokeDashArray());
      buffer.append("]");
    }
    return buffer.toString();
  }

  public String getPolygonSymbolizerSummary()
  {
    SldPolygonSymbolizer symbolizer =
      (SldPolygonSymbolizer)getValue("#{symbolizer}");
    SldStroke stroke = symbolizer.getStroke();
    SldFill fill = symbolizer.getFill();

    StringBuilder buffer = new StringBuilder();
    buffer.append(getBox(fill, stroke));

    if (!StringUtils.isBlank(fill.getFillOpacity()))
    {
      buffer.append(" O:");
      buffer.append(fill.getFillOpacity());
    }
    return buffer.toString();
  }

  public String getTextSymbolizerSummary()
  {
    SldTextSymbolizer symbolizer =
      (SldTextSymbolizer)getValue("#{symbolizer}");
    SldFill fill = symbolizer.getFill();

    StringBuilder buffer = new StringBuilder();
    if (!StringUtils.isBlank(fill.getFillColor()))
    {
      buffer.append(getBox(fill.getFillColor()));
    }
    String label = symbolizer.getLabelAsCql();
    if (!StringUtils.isBlank(label))
    {
      buffer.append("Label: ");
      buffer.append(label);
    }
    SldFont font = symbolizer.getFont();
    if (!StringUtils.isBlank(font.getFontFamily()))
    {
      buffer.append(" \"");
      buffer.append(font.getFontFamily());
      buffer.append("\"");
    }
    if (!StringUtils.isBlank(font.getFontSize()))
    {
      buffer.append(" ");
      buffer.append(font.getFontSize());
    }
    if (!StringUtils.isBlank(font.getFontStyle()))
    {
      buffer.append(" ");
      buffer.append(font.getFontStyle());
    }
    if (!StringUtils.isBlank(font.getFontWeight()))
    {
      buffer.append(" ");
      buffer.append(font.getFontWeight());
    }
    return buffer.toString();
  }

  private String getBox(String color)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("<div class=\"box\" style=\"border:none;").
      append("background-color:").append(color).append("\">").
      append("</div>");
   return buffer.toString();
  }

  private String getBox(SldFill fill, SldStroke stroke)
  {
    String fillColor = fill.getFillColor();
    String strokeColor = stroke.getStrokeColor();
    String strokeWidth = stroke.getStrokeWidth();
    StringBuilder buffer = new StringBuilder();
    if (!StringUtils.isBlank(fillColor) ||
        !StringUtils.isBlank(strokeColor) ||
        !StringUtils.isBlank(strokeWidth))
    {
      if (StringUtils.isBlank(fillColor))
      {
        fillColor = "#FFFFFF";
      }
      if (StringUtils.isBlank(strokeColor))
      {
        strokeColor = "#FFFFFF";
      }
      else if (StringUtils.isBlank(strokeWidth))
      {
        strokeWidth = "1";
      }
      if (StringUtils.isBlank(strokeWidth))
      {
        strokeWidth = "0";
      }
      buffer.append("<div class=\"box\" style=\"").
        append("background-color:").append(fillColor).
        append(";border-width:").append(strokeWidth).
        append("px;border-color:").append(strokeColor).append("\">").
        append("</div>");
    }
    return buffer.toString();
  }

  public void showAllNamedLayers()
  {
    visibleLayers.clear();
  }

  public void focusNamedLayer()
  {
    visibleLayers.clear();
    SldNamedLayer namedLayer = (SldNamedLayer)getValue("#{namedLayer}");
    visibleLayers.add(namedLayer.getLayerName());
    namedLayer.setCustomData(true);
  }

  public void duplicateNamedLayer()
  {
    SldNamedLayer namedLayer = (SldNamedLayer)getValue("#{namedLayer}");
    SldNamedLayer newNamedLayer = namedLayer.duplicate();
    newNamedLayer.setLayerName(null);
    visibleLayers.clear();
    growl("NAMED_LAYER_DUPLICATED");
  }

  public void duplicateUserStyle()
  {
    SldUserStyle userStyle = (SldUserStyle)getValue("#{userStyle}");
    SldUserStyle newUserStyle = userStyle.duplicate();
    newUserStyle.setStyleName(null);
    growl("USER_STYLE_DUPLICATED");
  }

  public void addNamedLayer()
  {
    SldNamedLayer namedLayer = sld.addNamedLayer();
    namedLayer.setCustomData(true);
  }

  public void addUserStyle()
  {
    SldNamedLayer namedLayer = (SldNamedLayer)getValue("#{namedLayer}");
    SldUserStyle userStyle = namedLayer.addUserStyle();
    userStyle.setCustomData(true);
  }

  public void addRule()
  {
    SldUserStyle userStyle = (SldUserStyle)getValue("#{userStyle}");
    SldRule rule = userStyle.addRule();
    rule.setCustomData(true);
  }

  public void addPointSymbolizer()
  {
    SldRule rule = (SldRule)getValue("#{rule}");
    SldPointSymbolizer pointSymbolizer = rule.addPointSymbolizer();
    pointSymbolizer.setCustomData(true);
  }

  public void addLineSymbolizer()
  {
    SldRule rule = (SldRule)getValue("#{rule}");
    SldLineSymbolizer lineSymbolizer = rule.addLineSymbolizer();
    lineSymbolizer.setCustomData(true);
  }

  public void addPolygonSymbolizer()
  {
    SldRule rule = (SldRule)getValue("#{rule}");
    SldPolygonSymbolizer polygonSymbolizer = rule.addPolygonSymbolizer();
    polygonSymbolizer.setCustomData(true);
  }

  public void addTextSymbolizer()
  {
    SldRule rule = (SldRule)getValue("#{rule}");
    SldTextSymbolizer textSymbolizer = rule.addTextSymbolizer();
    textSymbolizer.setCustomData(true);
  }



  public void newSld()
  {
    newSld(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
  }

  public void newSld(List<String> layerNames, List<String> styles)
  {
    try
    {
      sldName = "new_sld";
      sld = getSldStore().createSld(layerNames, styles);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void loadSld(String sldName)
  {
    loadSld(sldName, null);
  }

  public void loadSld(String sldName, String view)
  {
    try
    {
      this.sldName = sldName;
      sld = getSldStore().loadSld(sldName);
      if (sld == null)
      {
        newSld();
        error("SLD_NOT_FOUND");
      }
      else if (view != null)
      {
        CDI.current().select(GeoMapBean.class).get().setView(view);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void editSld(String sldName, List<String> layers, List<String> styles)
  {
    try
    {
      sld = getSldStore().loadSld(sldName);
      if (sld == null)
      {
        newSld(layers, styles);
      }
      else
      {
        sld.addNamedLayers(layers, styles);
      }
      visibleLayers.clear();
      visibleLayers.addAll(layers);
      this.sldName = sldName;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void reloadSld()
  {
    try
    {
      SldRoot sldReloaded = getSldStore().loadSld(sldName);
      if (sldReloaded == null)
      {
        error("SLD_NOT_FOUND");
      }
      else
      {
        sld = sldReloaded;
        growl("SLD_RELOADED");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void saveSld()
  {
    try
    {
      if (getSldStore().storeSld(sldName, sld, sldNameChanged))
      {
        growl("SLD_SAVED");
      }
      else
      {
        warn("A SLD with that name already exists. Save again to replace it.");
        sldNameChanged = false;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeSld()
  {
    error("NOT_IMPLEMENTED");
    newSld();
  }

  private SldStore getSldStore()
  {
    SldStore sldStore = CDI.current().select(SldStore.class).get();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    sldStore.setCredentials(userSessionBean.getUserId(), userSessionBean.getPassword());
    return sldStore;
  }

}
