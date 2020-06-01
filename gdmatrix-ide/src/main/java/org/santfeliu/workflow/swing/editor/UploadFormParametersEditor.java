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
package org.santfeliu.workflow.swing.editor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.matrix.ide.Options;
import org.santfeliu.swing.text.RestrictedDocument;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author realor
 */
public class UploadFormParametersEditor extends JPanel implements NodeEditor
{
  private FormNode formNode;
  private GridBagLayout gridBagLayout = new GridBagLayout();
  private JLabel referenceLabel = new JLabel();
  private JLabel referenceHelpLabel = new JLabel();
  private JTextField referenceTextField = new JTextField()
  {
    @Override
    public Dimension getPreferredSize()
    {
      Dimension size = super.getPreferredSize();
      return new Dimension(3 * size.height, size.height);
    }

    @Override
    public Dimension getMinimumSize()
    {
      return getPreferredSize();
    }
  };
  private JLabel maxFileSizeLabel = new JLabel();
  private JLabel maxFileSizeHelpLabel = new JLabel();
  private JTextField maxFileSizeTextField = new JTextField();
  private JLabel validExtensionsLabel = new JLabel();
  private JLabel validExtensionsHelpLabel = new JLabel();
  private JTextField validExtensionsTextField = new JTextField();
  private JLabel messageLabel = new JLabel();
  private JScrollPane messageScrollPane = new JScrollPane();
  private JTextArea messageTextArea = new JTextArea();
  private JLabel propertiesLabel = new JLabel();
  private JScrollPane propertiesScrollPane = new JScrollPane();
  private JTextArea propertiesTextArea = new JTextArea();

  public UploadFormParametersEditor()
  {
    try
    {
      initComponents();
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  @Override
  public Component getEditingComponent(NodeEditorDialog dialog,
    WorkflowNode node)
  {
    this.formNode = (FormNode)node;
    Properties parameters = formNode.getParameters();

    Object value = parameters.get("reference");
    if (value == null || String.valueOf(value).trim().length() == 0)
    {
      value = "ref";
    }
    referenceTextField.setText(value.toString());

    Object message = parameters.get("message");
    if (message != null)
    {
      messageTextArea.setText(message.toString());
      messageTextArea.setCaretPosition(0);
    }

    Object maxFileSize = parameters.get("maxFileSize");
    if (maxFileSize != null)
    {
      maxFileSizeTextField.setText(maxFileSize.toString());
      maxFileSizeTextField.setCaretPosition(0);
    }

    Object validExtensions = parameters.get("validExtensions");
    if (validExtensions != null)
    {
      validExtensionsTextField.setText(validExtensions.toString());
      validExtensionsTextField.setCaretPosition(0);
    }

    Properties properties = new Properties();
    properties.putAll(parameters);
    properties.remove("message");
    properties.remove("reference");
    properties.remove("maxFileSize");
    properties.remove("validExtensions");
    propertiesTextArea.setText(properties.saveToString());

    updateHelpLabel();
    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
  }

  @Override
  public void stopEditing() throws Exception
  {
    checkValues();
    Properties parameters = new Properties();
    parameters.loadFromString(propertiesTextArea.getText());

    String prefix = referenceTextField.getText();
    if (prefix != null && prefix.trim().length() > 0)
    {
      parameters.setProperty("reference", prefix);
    }
    String message = messageTextArea.getText();
    if (message != null && message.trim().length() > 0)
    {
      parameters.setProperty("message", message);
    }
    String maxFileSize = maxFileSizeTextField.getText();
    if (maxFileSize != null && maxFileSize.trim().length() > 0)
    {
      parameters.setProperty("maxFileSize", maxFileSize);
    }
    String validExtensions = validExtensionsTextField.getText();
    if (validExtensions != null && validExtensions.trim().length() > 0)
    {
      parameters.setProperty("validExtensions", validExtensions);
    }
    formNode.setParameters(parameters);
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents() throws Exception
  {
    this.setLayout(gridBagLayout);
    referenceLabel.setText("Reference:");
    referenceHelpLabel.setText("()");

    maxFileSizeLabel.setText("Max. file size:");
    maxFileSizeHelpLabel.setText("(ex: 512, 50kb, 4Mb)");

    validExtensionsLabel.setText("Valid extensions:");
    validExtensionsHelpLabel.setText("(ex: doc, pdf, xls)");

    messageLabel.setText("Message:");
    messageTextArea.setLineWrap(true);
    messageTextArea.setWrapStyleWord(true);

    propertiesLabel.setText("Properties:");
    this.add(referenceLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(referenceTextField,
      new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 4, 2, 4), 100, 0));
    this.add(referenceHelpLabel,
      new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));

    this.add(maxFileSizeLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(maxFileSizeTextField,
      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(maxFileSizeHelpLabel,
      new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));

    this.add(validExtensionsLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(validExtensionsTextField,
      new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(validExtensionsHelpLabel,
      new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));

    this.add(messageLabel,
      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(messageScrollPane,
      new GridBagConstraints(1, 3, 2, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(propertiesLabel,
      new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(propertiesScrollPane,
      new GridBagConstraints(1, 4, 2, 1, 0.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 4, 2, 4), 0, 0));

    messageScrollPane.getViewport().add(messageTextArea);
    propertiesScrollPane.getViewport().add(propertiesTextArea, null);
    propertiesTextArea.setFont(Options.getEditorFont());

    this.referenceTextField.setDocument(new RestrictedDocument("[a-zA-Z_][a-zA-Z0-9_]*"));
    referenceTextField.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        updateHelpLabel();
      }

      @Override
      public void keyReleased(KeyEvent e)
      {
        updateHelpLabel();
      }

      @Override
      public void keyTyped(KeyEvent e)
      {
        updateHelpLabel();
      }
    });
  }

  private void updateHelpLabel()
  {
    String varPrefix = referenceTextField.getText();
    referenceHelpLabel.setText(
      "(" + varPrefix + "docid_0, "
      + varPrefix + "uuid_0, "
      + varPrefix + "desc_0, "
      + varPrefix + "count, etc.)");
  }
}
