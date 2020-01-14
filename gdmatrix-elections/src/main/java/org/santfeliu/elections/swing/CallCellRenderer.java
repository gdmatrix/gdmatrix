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
package org.santfeliu.elections.swing;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;
import org.matrix.elections.Call;

/**
 *
 * @author unknown
 */
public class CallCellRenderer extends DefaultListCellRenderer
{
  public CallCellRenderer()
  {
  }
  
  @Override
  public Component getListCellRendererComponent(
      JList list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus)
  {
    setComponentOrientation(list.getComponentOrientation());
    if (isSelected)
    {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    }
    else
    {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }
    
    setIcon(null);
    setText(null);
    Call call = (Call)value;
    if (call == null) return this;

    Calendar callDate = call.getDate().toGregorianCalendar();
    String callDesc = call.getDescription();             
    
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
    String text = callDesc + " (" + 
        dateFormat.format(callDate.getTime()) + ")";
    setText(text);
    setEnabled(list.isEnabled());
    setFont(list.getFont());
    setBorder((cellHasFocus) ? 
      UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
    return this;
  }  
}
