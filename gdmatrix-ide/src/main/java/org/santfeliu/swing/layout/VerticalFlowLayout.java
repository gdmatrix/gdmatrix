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
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 *
 * @author unknown
 */
public class VerticalFlowLayout
  implements LayoutManager
{
  protected int vgap = 4;

  public VerticalFlowLayout()
  {
  }

  public void setVGap(int vgap)
  {
    this.vgap = vgap;
  }

  public int getVGap()
  {
    return vgap;
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
          y += d.height + vgap;
          dim.width = Math.max(dim.width, x + d.width);
          dim.height = y;
        }
      }
      return dim;
    }
  }

  public void layoutContainer(Container target)
  {
    synchronized (target.getTreeLock())
    {
      Insets insets = target.getInsets();
      int nmembers = target.getComponentCount();
      int x = insets.left;
      int y = insets.top;

      for (int i = 0; i < nmembers; i++)
      {
        Component m = target.getComponent(i);
        if (m.isVisible())
        {
          m
          .setBounds(0, 0, target.getWidth() - insets.left - insets.right, 1);
          Dimension d = m.getPreferredSize();
          m.setLocation(x, y);
          m.setSize(target.getWidth(), d.height);
          y += d.height + vgap;
          //  	      m.setSize(target.getWidth(), 20);
          //          y += 20 + vgap;

        }
      }
    }
  }

  /*
  public void layoutContainer(Container target)
  {
    synchronized (target.getTreeLock())
    {
	    Insets insets = target.getInsets();
     	int maxwidth = target.getWidth() - (insets.left + insets.right);
	    int nmembers = target.getComponentCount();
	    int x = insets.left;
      int y = insets.top;
      int rowh = 0;

	    for (int i = 0 ; i < nmembers ; i++)
      {
	      Component m = target.getComponent(i);
	      if (m.isVisible())
        {
		      Dimension d = m.getPreferredSize();
          if (x + d.width + hgap > maxwidth)
          {
            x = insets.left;
            y += rowh + vgap;
            rowh = 0;
          }
          m.setLocation(x, y);
  	      m.setSize(d.width, d.height);
          x += d.width + hgap;
          rowh = Math.max(rowh, d.height);
	      }
	    }
    }
  }
*/
}
