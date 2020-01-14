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
package org.santfeliu.swing.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;


/**
 *
 * @author unknown
 */
public class BordersDialog
  extends JDialog
{
  public static final int OK = 1;
  public static final int CANCEL = 2;
  
  private int result = CANCEL;
  private Color borderTopColor = Color.black;
  private Color borderBottomColor = Color.black;
  private Color borderLeftColor = Color.black;
  private Color borderRightColor = Color.black;

  private int borderTopWidth = 1;
  private int borderBottomWidth = 1;
  private int borderLeftWidth = 1;
  private int borderRightWidth = 1;

  private String borderTopStyle;
  private String borderBottomStyle;
  private String borderLeftStyle;
  private String borderRightStyle;

  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel southPanel = new JPanel();
  private JButton acceptButton = new JButton();
  private JButton cancelButton = new JButton();
  private JPanel centerPanel = new JPanel();
  private ColorButton topColorButton = new ColorButton();
  private JSpinner topWidthSpinner = new JSpinner();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JSpinner bottomWidthSpinner = new JSpinner();
  private ColorButton bottomColorButton = new ColorButton();
  private JSpinner leftWidthSpinner = new JSpinner();
  private ColorButton leftColorButton = new ColorButton();
  private JSpinner rightWidthSpinner = new JSpinner();
  private ColorButton rightColorButton = new ColorButton();
  private SampleView sampleView = new SampleView();
  private JLabel colorLabel = new JLabel();
  private JLabel sampleLabel = new JLabel();
  private JCheckBox topCheckBox = new JCheckBox();
  private JCheckBox bottomCheckBox = new JCheckBox();
  private JCheckBox leftCheckBox = new JCheckBox();
  private JCheckBox rightCheckBox = new JCheckBox();
  private JComboBox topComboBox = new JComboBox();
  private JComboBox bottomComboBox = new JComboBox();
  private JComboBox leftComboBox = new JComboBox();
  private JComboBox rightComboBox = new JComboBox();
  private JLabel applyLabel = new JLabel();
  private JLabel widthLabel = new JLabel();
  private JLabel styleLabel = new JLabel();
  private JLabel topLabel = new JLabel();
  private JLabel bottomLabel = new JLabel();
  private JLabel leftLabel = new JLabel();
  private JLabel rightLabel = new JLabel();

  public BordersDialog(Frame frame)
  {
    this(frame, "Borders dialog", true);
  }

  public BordersDialog(Frame parent, String title, boolean modal)
  {
    super(parent, title, modal);
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setSize(new Dimension(421, 301));
    this.getContentPane().setLayout(borderLayout1);
    acceptButton.setText("Accept");
    acceptButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            acceptButton_actionPerformed(e);
          }
        });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            cancelButton_actionPerformed(e);
          }
        });
    centerPanel.setLayout(gridBagLayout2);
    topWidthSpinner.setPreferredSize(new Dimension(50, 24));
    topWidthSpinner.setMinimumSize(new Dimension(50, 24));
    topWidthSpinner.addChangeListener(new ChangeListener()
        {
          public void stateChanged(ChangeEvent e)
          {
            topWidthSpinner_stateChanged(e);
          }
        });
    bottomWidthSpinner.addChangeListener(new ChangeListener()
        {
          public void stateChanged(ChangeEvent e)
          {
            bottomWidthSpinner_stateChanged(e);
          }
        });
    leftWidthSpinner.addChangeListener(new ChangeListener()
        {
          public void stateChanged(ChangeEvent e)
          {
            leftWidthSpinner_stateChanged(e);
          }
        });
    rightWidthSpinner.addChangeListener(new ChangeListener()
        {
          public void stateChanged(ChangeEvent e)
          {
            rightWidthSpinner_stateChanged(e);
          }
        });


    topColorButton.setText("...");
    topColorButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            topColorButton_actionPerformed(e);
          }
        });
    bottomWidthSpinner.setPreferredSize(new Dimension(50, 24));
    bottomWidthSpinner.setMinimumSize(new Dimension(50, 24));
    bottomColorButton.setText("...");
    bottomColorButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            bottomColorButton_actionPerformed(e);
          }
        });
    leftWidthSpinner.setPreferredSize(new Dimension(50, 24));
    leftWidthSpinner.setMinimumSize(new Dimension(50, 24));
    leftColorButton.setText("...");
    leftColorButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            leftColorButton_actionPerformed(e);
          }
        });
    rightWidthSpinner.setPreferredSize(new Dimension(50, 24));
    rightWidthSpinner.setMinimumSize(new Dimension(50, 24));
    rightColorButton.setText("...");
    rightColorButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            rightColorButton_actionPerformed(e);
          }
        });
    colorLabel.setText("Color:");
    sampleLabel.setText("Sample:");
    topCheckBox.setSelected(true);
    topCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            topCheckBox_actionPerformed(e);
          }
        });
    bottomCheckBox.setSelected(true);
    bottomCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            bottomCheckBox_actionPerformed(e);
          }
        });
    leftCheckBox.setSelected(true);
    leftCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            leftCheckBox_actionPerformed(e);
          }
        });
    rightCheckBox.setSelected(true);
    rightCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            rightCheckBox_actionPerformed(e);
          }
        });
    topComboBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            topComboBox_actionPerformed(e);
          }
        });
    bottomComboBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            bottomComboBox_actionPerformed(e);
          }
        });
    leftComboBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            leftComboBox_actionPerformed(e);
          }
        });
    rightComboBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            rightComboBox_actionPerformed(e);
          }
        });
    applyLabel.setText("Apply:");
    widthLabel.setText("Width:");
    styleLabel.setText("Style:");
    topLabel.setText("Top:");
    bottomLabel.setText("Bottom:");
    leftLabel.setText("Left:");
    rightLabel.setText("Right:");
    sampleView.setPreferredSize(new Dimension(100, 50));
    southPanel.add(acceptButton, null);
    southPanel.add(cancelButton, null);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);
    centerPanel.add(topWidthSpinner, 
                    new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(topColorButton, 
                    new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(bottomWidthSpinner, 
                    new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(bottomColorButton, 
                    new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(leftWidthSpinner, 
                    new GridBagConstraints(6, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(leftColorButton, 
                    new GridBagConstraints(5, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(rightWidthSpinner, 
                    new GridBagConstraints(6, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(rightColorButton, 
                    new GridBagConstraints(5, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(sampleView, 
                    new GridBagConstraints(0, 1, 1, 4, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
                                           new Insets(0, 0, 0, 0), 0, 0));
    centerPanel.add(colorLabel, 
                    new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(sampleLabel, 
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(topCheckBox, 
                    new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(bottomCheckBox, 
                    new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(2, 2, 2, 2), 0, 0));
    centerPanel.add(leftCheckBox, 
                    new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(2, 2, 2, 2), 0, 0));
    centerPanel.add(rightCheckBox, 
                    new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(2, 2, 2, 2), 0, 0));
    centerPanel.add(topComboBox, 
                    new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(bottomComboBox, 
                    new GridBagConstraints(7, 2, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(leftComboBox, 
                    new GridBagConstraints(7, 3, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(rightComboBox, 
                    new GridBagConstraints(7, 4, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(applyLabel, 
                    new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(widthLabel, 
                    new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(styleLabel, 
                    new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                           new Insets(4, 4, 4, 4), 0, 0));
    centerPanel.add(topLabel, 
                    new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                           new Insets(4, 8, 4, 4), 0, 0));
    centerPanel.add(bottomLabel, 
                    new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                           new Insets(4, 8, 4, 4), 0, 0));
    centerPanel.add(leftLabel, 
                    new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                           new Insets(4, 8, 4, 4), 0, 0));
    centerPanel.add(rightLabel, 
                    new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                           new Insets(4, 8, 4, 4), 0, 0));
    this.getContentPane().add(centerPanel, BorderLayout.CENTER);
    topWidthSpinner.setModel(new SpinnerNumberModel(0, 0, 1000, 1));
    bottomWidthSpinner.setModel(new SpinnerNumberModel(0, 0, 1000, 1));
    leftWidthSpinner.setModel(new SpinnerNumberModel(0, 0, 1000, 1));
    rightWidthSpinner.setModel(new SpinnerNumberModel(0, 0, 1000, 1));
  
    String[] styles = 
      new String[]{"", "none", "solid", "dashed", "dotted", "groove", "doble"};

    for (int i = 0; i < styles.length; i++)
    {
      topComboBox.addItem(styles[i]);
      bottomComboBox.addItem(styles[i]);
      leftComboBox.addItem(styles[i]);
      rightComboBox.addItem(styles[i]);
    }
  }
  
  public boolean isApplyTop()
  {
    return topCheckBox.isSelected();
  }

  public boolean isApplyBottom()
  {
    return bottomCheckBox.isSelected();
  }

  public boolean isApplyLeft()
  {
    return leftCheckBox.isSelected();
  }

  public boolean isApplyRight()
  {
    return rightCheckBox.isSelected();
  }

  public int showDialog()
  {
    topColorButton.setBackground(borderTopColor);
    bottomColorButton.setBackground(borderBottomColor);
    leftColorButton.setBackground(borderLeftColor);
    rightColorButton.setBackground(borderRightColor);
    
    topWidthSpinner.setValue(new Integer(borderTopWidth));
    bottomWidthSpinner.setValue(new Integer(borderBottomWidth));
    leftWidthSpinner.setValue(new Integer(borderLeftWidth));
    rightWidthSpinner.setValue(new Integer(borderRightWidth));

    topComboBox.setSelectedItem(borderTopStyle);
    bottomComboBox.setSelectedItem(borderBottomStyle);
    leftComboBox.setSelectedItem(borderLeftStyle);
    rightComboBox.setSelectedItem(borderRightStyle);

    setVisible(true);
    return result;
  }

  public void setBorderTopColor(Color borderTopColor)
  {
    this.borderTopColor = borderTopColor;
  }

  public Color getBorderTopColor()
  {
    return borderTopColor;
  }

  public void setBorderBottomColor(Color borderBottomColor)
  {
    this.borderBottomColor = borderBottomColor;
  }

  public Color getBorderBottomColor()
  {
    return borderBottomColor;
  }

  public void setBorderLeftColor(Color borderLeftColor)
  {
    this.borderLeftColor = borderLeftColor;
  }

  public Color getBorderLeftColor()
  {
    return borderLeftColor;
  }

  public void setBorderRightColor(Color borderRightColor)
  {
    this.borderRightColor = borderRightColor;
  }

  public Color getBorderRightColor()
  {
    return borderRightColor;
  }

  public void setBorderTopWidth(int borderTopWidth)
  {
    this.borderTopWidth = borderTopWidth;
  }

  public int getBorderTopWidth()
  {
    return borderTopWidth;
  }

  public void setBorderBottomWidth(int borderBottomWidth)
  {
    this.borderBottomWidth = borderBottomWidth;
  }

  public int getBorderBottomWidth()
  {
    return borderBottomWidth;
  }

  public void setBorderLeftWidth(int borderLeftWidth)
  {
    this.borderLeftWidth = borderLeftWidth;
  }

  public int getBorderLeftWidth()
  {
    return borderLeftWidth;
  }

  public void setBorderRightWidth(int borderRightWidth)
  {
    this.borderRightWidth = borderRightWidth;
  }

  public int getBorderRightWidth()
  {
    return borderRightWidth;
  }

  public void setBorderTopStyle(String borderTopStyle)
  {
    this.borderTopStyle = borderTopStyle;
  }

  public String getBorderTopStyle()
  {
    return borderTopStyle;
  }

  public void setBorderBottomStyle(String borderBottomStyle)
  {
    this.borderBottomStyle = borderBottomStyle;
  }

  public String getBorderBottomStyle()
  {
    return borderBottomStyle;
  }

  public void setBorderLeftStyle(String borderLeftStyle)
  {
    this.borderLeftStyle = borderLeftStyle;
  }

  public String getBorderLeftStyle()
  {
    return borderLeftStyle;
  }

  public void setBorderRightStyle(String borderRightStyle)
  {
    this.borderRightStyle = borderRightStyle;
  }

  public String getBorderRightStyle()
  {
    return borderRightStyle;
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    result = OK;
    setVisible(false);
    dispose();
  }

  private void cancelButton_actionPerformed(ActionEvent e)
  {
    result = CANCEL;
    setVisible(false);
    dispose();
  }

  private void topWidthSpinner_stateChanged(ChangeEvent e)
  {
    borderTopWidth = ((Number)topWidthSpinner.getValue()).intValue();
    sampleView.repaint();
  }

  private void bottomWidthSpinner_stateChanged(ChangeEvent e)
  {
    borderBottomWidth = ((Number)bottomWidthSpinner.getValue()).intValue();
    sampleView.repaint();
  }

  private void leftWidthSpinner_stateChanged(ChangeEvent e)
  {
    borderLeftWidth = ((Number)leftWidthSpinner.getValue()).intValue();
    sampleView.repaint();
  }

  private void rightWidthSpinner_stateChanged(ChangeEvent e)
  {
    borderRightWidth = ((Number)rightWidthSpinner.getValue()).intValue();
    sampleView.repaint();
  }

  private void topColorButton_actionPerformed(ActionEvent e)
  {
    Color color = 
      JColorChooser.showDialog(this, "Select color", borderTopColor);
    if (color != null)
    {
      borderTopColor = color;
      topColorButton.setBackground(color);
      sampleView.repaint();
    }
  }

  private void bottomColorButton_actionPerformed(ActionEvent e)
  {
    Color color = 
      JColorChooser.showDialog(this, "Select color", borderBottomColor);
    if (color != null)
    {
      borderBottomColor = color;
      bottomColorButton.setBackground(color);      
      sampleView.repaint();
    }
  }

  private void leftColorButton_actionPerformed(ActionEvent e)
  {
    Color color = 
      JColorChooser.showDialog(this, "Select color", borderLeftColor);
    if (color != null)
    {
      borderLeftColor = color;
      leftColorButton.setBackground(color);
      sampleView.repaint();
    }
  }

  private void rightColorButton_actionPerformed(ActionEvent e)
  {
    Color color = 
      JColorChooser.showDialog(this, "Select color", borderRightColor);
    if (color != null)
    {
      borderRightColor = color;
      rightColorButton.setBackground(color);
      sampleView.repaint();
    }
  }

  private void topCheckBox_actionPerformed(ActionEvent e)
  {
    boolean apply = topCheckBox.isSelected();
    topColorButton.setEnabled(apply);
    topWidthSpinner.setEnabled(apply);
    topComboBox.setEnabled(apply);
  }

  private void bottomCheckBox_actionPerformed(ActionEvent e)
  {
    boolean apply = bottomCheckBox.isSelected();
    bottomColorButton.setEnabled(apply);
    bottomWidthSpinner.setEnabled(apply);
    bottomComboBox.setEnabled(apply);
  }

  private void leftCheckBox_actionPerformed(ActionEvent e)
  {
    boolean apply = leftCheckBox.isSelected();
    leftColorButton.setEnabled(apply);
    leftWidthSpinner.setEnabled(apply);
    leftComboBox.setEnabled(apply);
  }

  private void rightCheckBox_actionPerformed(ActionEvent e)
  {
    boolean apply = rightCheckBox.isSelected();
    rightColorButton.setEnabled(apply);
    rightWidthSpinner.setEnabled(apply);
    rightComboBox.setEnabled(apply);
  }

  private void topComboBox_actionPerformed(ActionEvent e)
  {
    borderTopStyle = (String)topComboBox.getSelectedItem();
    sampleView.repaint();
  }

  private void bottomComboBox_actionPerformed(ActionEvent e)
  {
    borderBottomStyle = (String)bottomComboBox.getSelectedItem();
    sampleView.repaint();
  }

  private void leftComboBox_actionPerformed(ActionEvent e)
  {
    borderLeftStyle = (String)leftComboBox.getSelectedItem();
    sampleView.repaint();
  }

  private void rightComboBox_actionPerformed(ActionEvent e)
  {
    borderRightStyle = (String)rightComboBox.getSelectedItem();
    sampleView.repaint();
  }

  class SampleView extends Component
  {
    public void paint(Graphics g)
    {
      int width = getWidth();
      int height = getHeight();
      g.setColor(Color.white);
      g.fillRect(0, 0, width, height);
      if (borderTopWidth > 0 && !"none".equals(borderTopStyle))
      {
        if (borderTopColor == null) g.setColor(Color.black);
        else g.setColor(borderTopColor);
        g.fillRect(0, 0, width, borderTopWidth);
      }
      if (borderBottomWidth > 0 && !"none".equals(borderBottomStyle))
      {
        if (borderBottomColor == null) g.setColor(Color.black);
        else g.setColor(borderBottomColor);
        g.fillRect(0, height - borderBottomWidth, width, borderBottomWidth);
      }
      if (borderLeftWidth > 0 && !"none".equals(borderLeftStyle))
      {
        if (borderLeftColor == null) g.setColor(Color.black);
        else g.setColor(borderLeftColor);
        g.fillRect(0, 0, borderLeftWidth, height + 1);
      }
      if (borderRightWidth > 0 && !"none".equals(borderRightStyle))
      {
        if (borderRightColor == null) g.setColor(Color.black);
        else g.setColor(borderRightColor);
        g.fillRect(width - borderRightWidth, 0, borderRightWidth, height + 1);
      }
    }
  }

  class ColorButton extends JButton
  {
    public ColorButton()
    {
      this.setBorder(new BevelBorder(BevelBorder.RAISED));
    }
  
    public void updateUI()
    {
      setUI(new BasicButtonUI());
    }
  }
}
