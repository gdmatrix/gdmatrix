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
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

/**
 *
 * @author realor
 */
public class TabbedPane extends JTabbedPane
{
  protected boolean showCloseButton;
  protected Action closeAction;

  public TabbedPane()
  {
  }

  public boolean isShowCloseButton()
  {
    return showCloseButton;
  }

  public void setShowCloseButton(boolean showCloseButton)
  {
    if (showCloseButton != this.showCloseButton)
    {
      this.showCloseButton = showCloseButton;
      for (int index = 0; index < getTabCount(); index++)
      {
        TabComponent tabComponent = (TabComponent)getTabComponentAt(index);
        tabComponent.setCloseButtonVisible(showCloseButton);
      }
    }
  }

  public Action getCloseAction()
  {
    return closeAction;
  }

  public void setCloseAction(Action closeAction)
  {
    this.closeAction = closeAction;
  }

  @Override
  public void insertTab(String title, Icon icon, 
    Component component, String tip, int index) 
  {
    super.insertTab(title, null, component, tip, index);
    TabComponent tabComponent = new TabComponent(title, icon);
    setTabComponentAt(index, tabComponent);
    tabComponent.setCloseButtonVisible(showCloseButton);
  }

  @Override
  public void setTitleAt(int index, String title)
  {
    TabComponent tabComponent = (TabComponent)getTabComponentAt(index);
    tabComponent.setTitle(title);
  }

  @Override
  public void paintComponent(Graphics g)
  {
    if (getTabCount() > 0)
    {
      super.paintComponent(g);
    }
    else
    {
      g.setColor(UIManager.getColor("Panel.background"));
      g.fillRect(0, 0, getWidth(), getHeight());
    }
  }
  
  /* tabComponent */
  public class TabComponent extends JPanel
  {
    JLabel titleLabel = new JLabel();
    JToolBar toolBar = new JToolBar();
    JButton closeButton = new JButton();

    public TabComponent(String title, Icon icon)
    {
      titleLabel.setText(title);
      titleLabel.setIcon(icon);
      titleLabel.setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 4));

      toolBar.setFloatable(false);
      toolBar.setOpaque(false);
      toolBar.setBorder(null);
      toolBar.setBorderPainted(false);
      toolBar.setRollover(true);

      Icon closeIcon = new ImageIcon(getClass().getResource(
        "resources/images/close.png"));
      closeButton = new JButton();
      closeButton.setIcon(closeIcon);
      closeButton.setOpaque(false);
      closeButton.setRolloverIcon(closeIcon);
      closeButton.setMargin(new Insets(1, 0, 1, 0));
      closeButton.setRolloverEnabled(true);
      closeButton.setFocusPainted(false);
      closeButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          if (closeAction != null)
          {
            int index = getIndexOfTabComponent(TabComponent.this);
            if (index >= 0)
            {
              ActionEvent closeEvent = new ActionEvent(
                TabbedPane.this.getComponentAt(index), 0, "close");
              closeAction.actionPerformed(closeEvent);
            }
          }
        }
      });

      toolBar.add(closeButton);

      setOpaque(false);
      setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      add(titleLabel);
      add(toolBar);
    }

    void setTitle(String title)
    {
      titleLabel.setText(title);
    }

    void setCloseButtonVisible(boolean visible)
    {
      toolBar.setVisible(visible);
    }
  }

  private int getIndexOfTabComponent(Component tab)
  {
    int index = 0;
    boolean found = false;
    while (index < TabbedPane.this.getTabCount() && !found)
    {
      if (getTabComponentAt(index) == tab)
      {
        found = true;
      }
      else index++;
    }
    return found ? index : -1;
  }
}
