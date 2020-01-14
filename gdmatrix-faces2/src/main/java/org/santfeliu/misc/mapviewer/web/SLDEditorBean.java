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
package org.santfeliu.misc.mapviewer.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.misc.mapviewer.SLDStore;
import org.santfeliu.misc.mapviewer.io.SLDReader;
import org.santfeliu.misc.mapviewer.io.SLDWriter;
import org.santfeliu.misc.mapviewer.sld.SLDExternalGraphic;
import org.santfeliu.misc.mapviewer.sld.SLDFill;
import org.santfeliu.misc.mapviewer.sld.SLDFont;
import org.santfeliu.misc.mapviewer.sld.SLDGraphic;
import org.santfeliu.misc.mapviewer.sld.SLDLineSymbolizer;
import org.santfeliu.misc.mapviewer.sld.SLDMark;
import org.santfeliu.misc.mapviewer.sld.SLDNamedLayer;
import org.santfeliu.misc.mapviewer.sld.SLDPointSymbolizer;
import org.santfeliu.misc.mapviewer.sld.SLDPolygonSymbolizer;
import org.santfeliu.misc.mapviewer.sld.SLDRoot;
import org.santfeliu.misc.mapviewer.sld.SLDRule;
import org.santfeliu.misc.mapviewer.sld.SLDStroke;
import org.santfeliu.misc.mapviewer.sld.SLDSymbolizer;
import org.santfeliu.misc.mapviewer.sld.SLDTextSymbolizer;
import org.santfeliu.misc.mapviewer.sld.SLDUserStyle;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;

/**
 *
 * @author realor
 */
public class SLDEditorBean extends WebBean implements Savable
{
  public static final String SLD_ENCODING = "ISO-8859-1";

  private String sldName;
  private SLDRoot sld;
  private transient String source;
  private boolean advancedMode;
  private String serviceUrl;
  private Set<String> visibleLayers = new HashSet<String>();

  public String getSldName()
  {
    return sldName;
  }

  public void setSldName(String sldName)
  {
    this.sldName = sldName;
  }

  public SLDRoot getSld()
  {
    return sld;
  }

  public String getSource()
  {
    return source;
  }

  public void setSource(String source)
  {
    this.source = source;
  }

  public String getServiceUrl()
  {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl)
  {
    this.serviceUrl = serviceUrl;
  }

  public String getBasicModeScripts()
  {
    StringBuilder buffer = new StringBuilder();
    addScriptFile("/plugins/mapviewer/OpenLayers.js", buffer);
    addScriptFile("/plugins/mapviewer/Controls.js", buffer);
    addScriptFile("/plugins/mapviewer/autocomplete.js", buffer);
    addScriptFile("/plugins/color/jscolor.js", buffer);
    buffer.append("<script type=\"text/javascript\">");
    MapBean mapBean = MapBean.getInstance();
    String baseUrl = mapBean.getBaseUrl();
    buffer.append("var baseUrl = '").append(baseUrl).append("';\n");
    buffer.append("var serviceUrl = '").append(serviceUrl).append("';\n");
    buffer.append("</script>");
    return buffer.toString();
  }

  public String getAdvancedModeScripts()
  {
    StringBuilder buffer = new StringBuilder();
    addScriptFile("/plugins/codemirror/codemirror.js", buffer);
    addScriptFile("/plugins/codemirror/xml.js", buffer);
    addScriptFile("/plugins/codemirror/matchtags.js", buffer);
    addScriptFile("/plugins/codemirror/xml-fold.js", buffer);
    buffer.append("<script type=\"text/javascript\">var editor = " +
      "document.getElementById(\"sld_editor\");var myCodeMirror = " +
      "CodeMirror.fromTextArea(editor, {lineNumbers:false, matchTags:{bothTags: true}});</script>");
    return buffer.toString();
  }

  public boolean isAdvancedMode()
  {
    return advancedMode;
  }

  public boolean isNamedLayerExpanded()
  {
    SLDNamedLayer namedLayer = (SLDNamedLayer)getValue("#{namedLayer}");
    return namedLayer.getCustomData() != null;
  }

  public boolean isUserStyleExpanded()
  {
    SLDUserStyle userStyle = (SLDUserStyle)getValue("#{userStyle}");
    return userStyle.getCustomData() != null;
  }

  public boolean isRuleExpanded()
  {
    SLDRule rule = (SLDRule)getValue("#{rule}");
    return rule.getCustomData() != null;
  }

  public boolean isSymbolizerExpanded()
  {
    SLDSymbolizer symbolizer = (SLDSymbolizer)getValue("#{symbolizer}");
    return symbolizer.getCustomData() != null;
  }

  public boolean isNamedLayerVisible()
  {
    SLDNamedLayer namedLayer = (SLDNamedLayer)getValue("#{namedLayer}");
    return visibleLayers.isEmpty() ||
      visibleLayers.contains(namedLayer.getLayerName());
  }

  public int getNamedLayerCount()
  {
    return sld.getNamedLayers().size();
  }

  public int getNamedLayerIndex()
  {
    SLDNamedLayer namedLayer = (SLDNamedLayer)getValue("#{namedLayer}");
    return sld.getNamedLayers().indexOf(namedLayer);
  }

  public boolean isFocusingNamedLayer()
  {
    return !visibleLayers.isEmpty();
  }

  public String getRuleSummary()
  {
    SLDRule rule = (SLDRule)getValue("#{rule}");
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

  public String getPointSymbolizerSummary()
  {
    SLDPointSymbolizer symbolizer =
      (SLDPointSymbolizer)getValue("#{symbolizer}");
    SLDGraphic graphic = symbolizer.getGraphic();
    SLDMark mark = graphic.getMark();
    
    StringBuilder buffer = new StringBuilder();
    if (!StringUtils.isBlank(mark.getWellKnownName()))
    {
      buffer.append(mark.getWellKnownName());
      buffer.append(" ");
      if (!StringUtils.isBlank(graphic.getSizeAsXml()))
      {
        buffer.append(graphic.getSizeAsXml());
      }
      SLDFill fill = mark.getFill();
      SLDStroke stroke = mark.getStroke();
      buffer.append(getBox(fill, stroke));
    }
    SLDExternalGraphic externalGraphic = graphic.getExternalGraphic();
    String resource = externalGraphic.getOnlineResource();
    if (!StringUtils.isBlank(resource))
    {
      if (!StringUtils.isBlank(graphic.getSizeAsXml()))
      {
        buffer.append(" ");
        buffer.append(graphic.getSizeAsXml());
      }
      // adapt icon url
      resource = resource.replaceAll("http://", "https://");
      if (resource.indexOf("/documents/") != -1)
      {
        if (resource.indexOf("?") == -1) resource += "?cache=1000000";
        else resource += "&cache=1000000";
      }
      buffer.append("<img src=\"");
      buffer.append(resource);
      buffer.append("\" alt=\"\" class=\"icon\">");
    }
    return buffer.toString();
  }

  public String getLineSymbolizerSummary()
  {
    SLDLineSymbolizer symbolizer =
      (SLDLineSymbolizer)getValue("#{symbolizer}");
    SLDStroke stroke = symbolizer.getStroke();

    StringBuilder buffer = new StringBuilder();
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
    if (!StringUtils.isBlank(stroke.getStrokeColor()))
    {
      buffer.append(getBox(stroke.getStrokeColor()));
    }
    return buffer.toString();
  }

  public String getPolygonSymbolizerSummary()
  {
    SLDPolygonSymbolizer symbolizer =
      (SLDPolygonSymbolizer)getValue("#{symbolizer}");
    SLDStroke stroke = symbolizer.getStroke();
    SLDFill fill = symbolizer.getFill();

    StringBuilder buffer = new StringBuilder();
    buffer.append(getBox(fill, stroke));
    return buffer.toString();
  }

  public String getTextSymbolizerSummary()
  {
    SLDTextSymbolizer symbolizer =
      (SLDTextSymbolizer)getValue("#{symbolizer}");
    SLDFill fill = symbolizer.getFill();

    StringBuilder buffer = new StringBuilder();
    String label = symbolizer.getLabelAsCql();
    if (!StringUtils.isBlank(label))
    {
      buffer.append("Label: ");
      buffer.append(label);
    }
    SLDFont font = symbolizer.getFont();
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
    if (!StringUtils.isBlank(fill.getFillColor()))
    {
      buffer.append(getBox(fill.getFillColor()));
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

  private String getBox(SLDFill fill, SLDStroke stroke)
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

  // actions
  public String show()
  {
    advancedMode = false;
    return "sld_editor";
  }

  public void showAllNamedLayers()
  {
    visibleLayers.clear();
  }

  public void focusNamedLayer()
  {
    visibleLayers.clear();
    SLDNamedLayer namedLayer = (SLDNamedLayer)getValue("#{namedLayer}");
    visibleLayers.add(namedLayer.getLayerName());
    namedLayer.setCustomData("exp");
  }

  public void expandNamedLayer()
  {
    SLDNamedLayer namedLayer = (SLDNamedLayer)getValue("#{namedLayer}");
    namedLayer.setCustomData("exp");
  }

  public void collapseNamedLayer()
  {
    SLDNamedLayer namedLayer = (SLDNamedLayer)getValue("#{namedLayer}");
    namedLayer.setCustomData(null);
  }

  public void expandUserStyle()
  {
    SLDUserStyle userStyle = (SLDUserStyle)getValue("#{userStyle}");
    userStyle.setCustomData("exp");
  }

  public void collapseUserStyle()
  {
    SLDUserStyle userStyle = (SLDUserStyle)getValue("#{userStyle}");
    userStyle.setCustomData(null);
  }

  public void expandRule()
  {
    SLDRule rule = (SLDRule)getValue("#{rule}");
    rule.setCustomData("exp");
  }

  public void collapseRule()
  {
    SLDRule rule = (SLDRule)getValue("#{rule}");
    rule.setCustomData(null);
  }

  public void expandSymbolizer()
  {
    SLDSymbolizer symbolizer = (SLDSymbolizer)getValue("#{symbolizer}");
    symbolizer.setCustomData("exp");
  }

  public void collapseSymbolizer()
  {
    SLDSymbolizer symbolizer = (SLDSymbolizer)getValue("#{symbolizer}");
    symbolizer.setCustomData(null);
  }

  public void addNamedLayer()
  {
    SLDNamedLayer namedLayer = sld.addNamedLayer();
    namedLayer.setCustomData("exp");
  }

  public void addUserStyle()
  {
    SLDNamedLayer namedLayer = (SLDNamedLayer)getValue("#{namedLayer}");
    SLDUserStyle userStyle = namedLayer.addUserStyle();
    userStyle.setCustomData("exp");
  }

  public void addRule()
  {
    SLDUserStyle userStyle = (SLDUserStyle)getValue("#{userStyle}");
    SLDRule rule = userStyle.addRule();
    rule.setCustomData("exp");
  }

  public void addPointSymbolizer()
  {
    SLDRule rule = (SLDRule)getValue("#{rule}");
    SLDPointSymbolizer pointSymbolizer = rule.addPointSymbolizer();
    pointSymbolizer.setCustomData("exp");
  }

  public void addLineSymbolizer()
  {
    SLDRule rule = (SLDRule)getValue("#{rule}");
    SLDLineSymbolizer lineSymbolizer = rule.addLineSymbolizer();
    lineSymbolizer.setCustomData("exp");
  }

  public void addPolygonSymbolizer()
  {
    SLDRule rule = (SLDRule)getValue("#{rule}");
    SLDPolygonSymbolizer polygonSymbolizer = rule.addPolygonSymbolizer();
    polygonSymbolizer.setCustomData("exp");
  }

  public void addTextSymbolizer()
  {
    SLDRule rule = (SLDRule)getValue("#{rule}");
    SLDTextSymbolizer textSymbolizer = rule.addTextSymbolizer();
    textSymbolizer.setCustomData("exp");
  }

  public String editSLD(String sldName, 
    List<String> layers, List<String> styles)
  {
    try
    {
      visibleLayers.clear();
      visibleLayers.addAll(layers);
      SLDRoot root = SLDStore.loadSld(sldName);
      if (root == null)
      {
        root = SLDStore.createSld(layers, styles);
      }
      this.sld = root;
      this.source = null;
      this.sldName = sldName;
      this.advancedMode = false;
      root.addNamedLayers(layers, styles);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "sld_editor";
  }

  public void clear()
  {
    sldName = null;
    sld = null;
  }

  public void enterAdvancedMode()
  {
    advancedMode = true;
    try
    {
      loadSourceFromSld();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void exitAdvancedMode()
  {
    advancedMode = false;
    try
    {
      loadSldFromSource();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void saveSLD()
  {
    try
    {
      if (advancedMode)
      {
        loadSldFromSource();
      }
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      SLDStore.storeSld(sldName, sld,
        userSessionBean.getUserId(), userSessionBean.getPassword());
      info("SLD_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void reloadSLD()
  {
    try
    {
      sld = SLDStore.loadSld(sldName);
      info("SLD_RELOADED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String editMap()
  {
    return "map_editor";
  }

  // private methods
  
  private void addScriptFile(String path, StringBuilder buffer)
  {
    String contextPath = getContextPath();
    buffer.append("<script src=\"").append(contextPath);
    buffer.append(path);
    buffer.append("\" type=\"text/javascript\">\n</script>\n");
  }

  private void loadSourceFromSld() throws Exception
  {
    SLDWriter writer = new SLDWriter();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    writer.write(sld, bos);
    source = bos.toString(SLD_ENCODING);
  }

  private void loadSldFromSource() throws Exception
  {
    SLDReader reader = new SLDReader();
    ByteArrayInputStream bis =
      new ByteArrayInputStream(source.getBytes(SLD_ENCODING));
    sld = reader.read(bis);
  }
}
