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
package org.santfeliu.swing;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 *
 * @author realor
 */
public class Utilities
{
  public Utilities()
  {
  }
  
  /**
   * Centers window respect component
   */
  public static void centerWindow(Component component, Window window)
  {
    if (window != null)
    {
      java.awt.Point location;
      Dimension ownerDim;      
      if (component == null)
      {
        location = new Point(0, 0);
        ownerDim = Toolkit.getDefaultToolkit().getScreenSize();
      }
      else
      {
        Window owner;
        if (component instanceof Window)
        {
          owner = (Window)component;
        }
        else
        {
          owner = SwingUtilities.getWindowAncestor(component);
        }
        location = owner.getLocation();
        ownerDim = owner.getSize();
      }
      Dimension windowDim = window.getSize();
      int width = (ownerDim.width - windowDim.width) / 2;
      int height = (ownerDim.height - windowDim.height) / 2;  
      location.x = Math.max(0, (int)(location.x + width));
      location.y = Math.max(0, (int)(location.y + height));
      window.setLocation(location);
    }
  }

  public static JDialog createDialog(String title, int width, int height,
    boolean modal, Component parent, Component component)
  {
    JDialog dialog;
    Window owner = getParentWindow(parent);
    if (owner instanceof Frame)
      dialog = new JDialog((Frame)owner, title, modal);
    else if (owner instanceof Dialog)
      dialog = new JDialog((Dialog)owner, title, modal);
    else
      dialog = new JDialog((Frame)null, title, modal);

    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.getContentPane().add(component);
    dialog.setSize(width, height);
    Utilities.centerWindow(owner, dialog);
    return dialog;
  }
  
  public static Window getParentWindow(Component component)
  {
    Window window;
    if (component == null)
    {
      window = null;
    }
    else
    {
      if (component instanceof Window)
      {
        window = (Window)component;
      }
      else
      {
        window = SwingUtilities.getWindowAncestor(component);
      }
    }
    return window;
  }
}
