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
package org.santfeliu.swing.palette;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.santfeliu.swing.layout.VerticalFlowLayout;

/**
 *
 * @author realor
 */
public class CategoryPane extends JScrollPane
{
  Palette palette;
  String categoryName;
  String displayName;
  private JPanel panel = new JPanel();

  CategoryPane(Palette palette)
  {
    this.palette = palette;
    initComponents();
  }

  public String getDisplayName()
  {
    return displayName == null ? categoryName : displayName;
  }

  public Palette getPalette()
  {
    return palette;
  }

  void addElement(ElementLabel label)
  {
    panel.add(label);
  }

  private void initComponents()
  {
    getViewport().add(panel);
    VerticalFlowLayout layout = new VerticalFlowLayout();
    layout.setVGap(1);
    panel.setLayout(layout);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
  }
}
