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
package org.santfeliu.swing.form.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

import javax.swing.JEditorPane;

import org.santfeliu.swing.form.ComponentView;

/**
 *
 * @author unknown
 */
public class OutputTextAreaView extends ComponentView
{
  private String text = "Text area";
  private JEditorPane editor = new JEditorPane();

  public OutputTextAreaView()
  {
    setWidth(192);
    setHeight(48);
  }

  @Override
  public void paintView(Graphics g)
  {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    editor.setOpaque(false);
    editor.setForeground(getForeground());
    editor.setSize(getWidth() - parseWidth(getBorderLeftWidth()) - 
      parseWidth(getBorderRightWidth()), 
      getHeight() - parseWidth(getBorderTopWidth()) 
      - parseWidth(getBorderBottomWidth()));
    editor.setContentType("text/html");
    String salign = getTextAlign();
    String value = "";
    if (salign == null) salign = "left";
    
    value = "<div align=\"" + salign + "\"";
    String fontFamily = getFontFamily();
    int fontSize = getFontSize(); 
    // fix font size bug!!
    if (fontSize > 1) fontSize = (int)(fontSize * 0.75);
    if (fontFamily != null && fontFamily.trim().length() > 0)
    {
      value += " style=\"font-family:" + fontFamily;
      if (fontSize > 0)
        value += ";font-size:" + fontSize + "px";
      value += "\"";
    }
    else if (fontSize > 0)
    {
      value += "style=\"font-size:" + fontSize + "px\"";
    }
    String realText = (text != null) ? text : "";
    value += ">" + realText + "</div>";
    
    editor.setText(value);
    g.translate(parseWidth(getBorderLeftWidth()), 
      parseWidth(getBorderTopWidth()));
    Shape clip = g.getClip();
    g.setClip(0, 0, getWidth(), getHeight());
    editor.paint(g);
    g.setClip(clip);
    g.translate(-parseWidth(getBorderLeftWidth()), 
      -parseWidth(getBorderTopWidth()));
  }

  public void setText(String text)
  {
    this.text = nullWhenEmpty(text);
  }

  public String getText()
  {
    return text;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    OutputTextAreaView clone = (OutputTextAreaView)super.clone();
    clone.text = text;
    return clone;
  }
}
