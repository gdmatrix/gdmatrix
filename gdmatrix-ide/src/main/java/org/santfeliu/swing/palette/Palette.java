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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 *
 * @author realor
 */
public class Palette extends JPanel
  implements ActionListener
{
  public static final String EMPTY = " ";
  private BorderLayout borderLayout = new BorderLayout();
  private FlowLayout flowLayout = new FlowLayout();
  private JPanel northPanel = new JPanel();
  private JLabel categoryLabel = new JLabel();
  private JPanel categoriesPanel = new JPanel();
  private CardLayout cardLayout = new CardLayout();
  private HashMap<String, CategoryPane> categoriesMap = new HashMap();
  private String selectedCategory;
  private ElementLabel selectedElementLabel;

  public Palette()
  {
    try
    {
      initComponents();
    }
    catch (Exception e)
    {
    }
  }

  private void initComponents() throws Exception
  {
    this.setSize(new Dimension(349, 459));
    this.setLayout(borderLayout);
    categoryLabel.setText(" ");
    categoriesPanel.setLayout(cardLayout);
    Color borderColor = UIManager.getColor("Panel.background").darker();
    categoriesPanel.setBorder(BorderFactory.createLineBorder(borderColor, 1));
    flowLayout.setAlignment(FlowLayout.LEFT);
    northPanel.setLayout(flowLayout);
    northPanel.add(categoryLabel, null);
    this.add(northPanel, BorderLayout.NORTH);
    this.add(categoriesPanel, BorderLayout.CENTER);
    CategoryPane emptyPane = new CategoryPane(this);
    emptyPane.categoryName = EMPTY;
    categoriesPanel.add(emptyPane, EMPTY);
    categoriesMap.put(EMPTY, emptyPane);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    ElementLabel elementLabel = (ElementLabel)e.getSource();
    if (elementLabel == selectedElementLabel)
    {
      selectedElementLabel.setSelected(true);
    }
    else if (selectedElementLabel != null)
    {
      selectedElementLabel.setSelected(false);
    }
    selectedElementLabel = elementLabel;
  }

  public void clearSelectedElement()
  {
    if (selectedElementLabel != null)
    {
      selectedElementLabel.setSelected(false);
      selectedElementLabel = null;
    }
  }

  public void setSelectedCategory(String categoryName)
  {
    if (selectedElementLabel != null)
    {
      selectedElementLabel.setSelected(false);
      selectedElementLabel = null;
    }
    selectedCategory = categoryName;
    CategoryPane category = categoriesMap.get(categoryName);
    categoryLabel.setText(category.getDisplayName());
    cardLayout.show(categoriesPanel, categoryName);
  }

  public String getSelectedCategory()
  {
    return selectedCategory;
  }

  public String getSelectedElement()
  {
    String elementName = null;
    if (selectedElementLabel != null)
    {
      elementName = selectedElementLabel.elementName;
    }
    return elementName;
  }

  public String getSelectedElementAttribute(String attributeName)
  {
    String value = null;
    if (selectedElementLabel != null)
    {
      value = selectedElementLabel.attributes.get(attributeName);
    }
    return value;
  }

  public void read(InputStream is) throws Exception
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(is);

    Node node = document.getFirstChild();
    while (!(node instanceof Element)) node = node.getNextSibling();

    node = node.getFirstChild();
    while (node != null)
    {
      if (node instanceof Element)
      {
        Element element = (Element)node;
        String tag = element.getTagName();
        if ("category".equals(tag))
        {
          parseCategory(element);
        }
      }
      node = node.getNextSibling();
    }
  }

  private void parseCategory(Element element)
  {
    CategoryPane categoryPane = new CategoryPane(this);
    categoryPane.categoryName = element.getAttribute("name");
    categoryPane.displayName = element.getAttribute("displayName");
    categoriesMap.put(categoryPane.categoryName, categoryPane);
    categoriesPanel.add(categoryPane, categoryPane.categoryName);

    Node node = element.getFirstChild();
    while (node != null)
    {
      if (node instanceof Element)
      {
        Element child = (Element)node;
        String tag = child.getTagName();
        if ("element".equals(tag))
        {
          ElementLabel button = new ElementLabel(categoryPane);
          parseElement(child, button);
          categoryPane.addElement(button);
        }
      }
      node = node.getNextSibling();
    }
  }

  private void parseElement(Element element, ElementLabel elementLabel)
  {
    elementLabel.elementName = element.getAttribute("name");
    elementLabel.displayName = element.getAttribute("displayName");
    elementLabel.addActionListener(this);
    elementLabel.setHorizontalAlignment(SwingConstants.LEFT);
    elementLabel.setText(elementLabel.elementName);
    elementLabel.setIcon(getIcon(element.getAttribute("icon")));

    Node node = element.getFirstChild();
    while (node != null)
    {
      if (node instanceof Element)
      {
        Element child = (Element)node;
        String tag = child.getTagName();
        if ("attribute".equals(tag))
        {
          String attributeName = child.getAttribute("name");
          String value = child.getAttribute("value");
          if (value == null || value.trim().length() == 0)
          {
            value = child.getTextContent();
          }
          elementLabel.attributes.put(attributeName, value);
        }
      }
      node = node.getNextSibling();
    }
  }

  private ImageIcon getIcon(String path)
  {
    ImageIcon icon = null;
    try
    {
      icon = new ImageIcon(getClass().getResource(path));
    }
    catch (Exception ex)
    {
    }
    return icon;
  }
}
