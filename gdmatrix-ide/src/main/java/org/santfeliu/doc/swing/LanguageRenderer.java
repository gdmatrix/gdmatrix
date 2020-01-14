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
package org.santfeliu.doc.swing;

import java.awt.Component;
import java.util.Locale;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.matrix.doc.DocumentConstants;


/**
 *
 * @author blanquepa
 */
public class LanguageRenderer extends DefaultListCellRenderer
{
  @Override
  public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus)
  {
    if (isSelected)
    {
      super.setForeground(list.getSelectionForeground());
      super.setBackground(list.getSelectionBackground());
    }
    else
    {
      super.setForeground(list.getForeground());
      super.setBackground(list.getBackground());
    }
    setFont(list.getFont());
    
    String text = "";
    if (value != null && value instanceof String)
    {
      if (DocumentConstants.UNIVERSAL_LANGUAGE.equals((String)value))
        text = "universal";
      else
      {
        Locale locale = new Locale((String)value);
        text = locale.getDisplayLanguage(Locale.getDefault());
      }
    }
    else
    {
      text = (value == null) ? "" : value.toString();
    }
    setText(text);
    
    
    setIcon(null);
    return this;
  }
}
