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
package org.santfeliu.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JLabel;

/**
 *
 * @author unknown
 */
public class PathFlowLayout implements LayoutManager
{
  protected int hgap;
  protected String mode;
  protected Component divider;
  
  public static final String MODE_CUT_LEFT = "LEFT";
  public static final String MODE_CUT_CENTER = "CENTER";
  public static final String MODE_CUT_RIGHT = "RIGHT";

  public PathFlowLayout()
  {
    this.mode = MODE_CUT_CENTER;
    this.hgap = 4;
    this.divider = new JLabel("...");
  }
  
  public PathFlowLayout(String mode, int hgap, Component divider) 
  {
    this.mode = mode;
    this.hgap = hgap;
    this.divider = divider;
  }  
  
  public void addLayoutComponent(String name, Component comp)
  {
  }

  public void removeLayoutComponent(Component comp)
  {
  }

  public Dimension preferredLayoutSize(Container target)
  {
    return minimumLayoutSize(target);
  }

  public Dimension minimumLayoutSize(Container target)
  {
    synchronized (target.getTreeLock())
    {
      Insets insets = target.getInsets();
      int nmembers = target.getComponentCount();
      int x = insets.left;
      int y = insets.top;
      Dimension dim = new Dimension(0, 0);

      for (int i = 0; i < nmembers; i++)
      {
        Component m = target.getComponent(i);
        if (m.isVisible())
        {
          Dimension d = m.getPreferredSize();
          x += (d.width + hgap);
          dim.height = Math.max(dim.height, y + d.height);
          dim.width = x;
        }
      }
      return dim;
    }
  }

  public void layoutContainer(Container target)
  {
    synchronized (target.getTreeLock())
    {
      target.remove(this.divider);
    
      Insets insets = target.getInsets();
      int nmembers = target.getComponentCount();
      int x = insets.left;

      for (int i = 0; i < nmembers; i++) 
        target.getComponent(i).setVisible(true); // initially, all components are visible

      int componentsWidth = insets.left + insets.right;
      for (int i = 0; i < nmembers; i++)       
        componentsWidth += target.getComponent(i).getPreferredSize().width + hgap;
      
      int containerWidth = target.getSize().width;      

      if (componentsWidth > containerWidth) // not enough space
      {  
        int cutSize = 0;
        int cutPoint = -1;
        int difference = (componentsWidth + this.divider.getWidth()) - containerWidth;
        if (mode == MODE_CUT_CENTER) 
        {
          int startPoint = (int)Math.round(Math.floor((nmembers - 1) / 2));
          int i1 = startPoint;
          int i2 = startPoint + 1;
          int index;
          for (int i = 0; ((i < nmembers) && (cutPoint == -1)); i++) 
          {
            if ((i % 2) == 0) index = i1--;
            else index = i2++;
            Component m = target.getComponent(index);
            m.setVisible(false);
            cutSize += m.getPreferredSize().width;
            if (cutSize > difference) cutPoint = index;            
          }
          target.add(this.divider, cutPoint);
        }
        else if (mode == MODE_CUT_LEFT) 
        {
          for (int i = 0; ((i < nmembers) && (cutPoint == -1)); i++) 
          {
            Component m = target.getComponent(i);
            m.setVisible(false);
            cutSize += m.getPreferredSize().width;
            if (cutSize > difference) cutPoint = i;
          }
          target.add(this.divider, 0);
        }
        else if (mode == MODE_CUT_RIGHT) 
        {
          for (int i = (nmembers - 1); ((i >= 0) && (cutPoint == -1)); i--) 
          {
            Component m = target.getComponent(i);
            m.setVisible(false);
            cutSize += m.getPreferredSize().width;
            if (cutSize > difference) cutPoint = i;
          }
          target.add(this.divider);
        }
      }

      // Components drawing            
      for (int i = 0; i < target.getComponentCount(); i++)
      {
        Component m = target.getComponent(i);
        if (m.isVisible())
        {
          Dimension d = m.getPreferredSize();
          int y = (target.getHeight() - d.height) / 2;
          m.setBounds(x, y, d.width, d.height);
          x += d.width + hgap;
        }
      }
    }
  }

  public void setHGap(int hgap)
  {
    this.hgap = hgap;
  }

  public int getHGap()
  {
    return hgap;
  }

  public void setMode(String mode)
  {
    this.mode = mode;
  }

  public String getMode()
  {
    return mode;
  }

  public void setDivider(Component divider)
  {
    this.divider = divider;
  }

  public Component getDivider()
  {
    return divider;
  }
  
}
