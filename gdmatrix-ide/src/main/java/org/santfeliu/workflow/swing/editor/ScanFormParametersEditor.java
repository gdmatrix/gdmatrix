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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.matrix.ide.Options;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author blanquepa
 */
public class ScanFormParametersEditor extends JPanel implements NodeEditor
{
  private FormNode formNode;
  private GridBagLayout gridBagLayout = new GridBagLayout();

  private JLabel messageLabel = new JLabel();
  private JScrollPane messageScrollPane = new JScrollPane();
  private JTextArea messageTextArea = new JTextArea();

  private JLabel resultVarLabel = new JLabel();
  private JTextField resultVarTextField = new JTextField();

  private JLabel pdfAuthorLabel = new JLabel();
  private JTextField pdfAuthorTextField = new JTextField();

  private JLabel pdfSubjectLabel = new JLabel();
  private JTextField pdfSubjectTextField = new JTextField();

  private JLabel pdfTitleLabel = new JLabel();
  private JTextField pdfTitleTextField = new JTextField();

  private JLabel propertiesLabel = new JLabel();
  private JScrollPane propertiesScrollPane = new JScrollPane();
  private JTextArea propertiesTextArea = new JTextArea();

  public ScanFormParametersEditor()
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

    Object message = parameters.get("message");
    if (message != null)
    {
      messageTextArea.setText(message.toString());
      messageTextArea.setCaretPosition(0);
    }

    Object resultVar = parameters.get("resultVar");
    if (resultVar != null)
      resultVarTextField.setText(resultVar.toString());

    Object pdfAuthor = parameters.get("_pdfAuthor");
    if (pdfAuthor != null)
      pdfAuthorTextField.setText(pdfAuthor.toString());

    Object pdfSubject = parameters.get("_pdfSubject");
    if (pdfSubject != null)
      pdfSubjectTextField.setText(pdfSubject.toString());

    Object pdfTitle = parameters.get("_pdfTitle");
    if (pdfTitle != null)
      pdfTitleTextField.setText(pdfTitle.toString());

    Properties properties = new Properties();
    properties.putAll(parameters);
    properties.remove("message");
    properties.remove("resultVar");
    properties.remove("_pdfAuthor");
    properties.remove("_pdfSubject");
    properties.remove("_pdfTitle");
    propertiesTextArea.setText(properties.saveToString());

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

    String message = messageTextArea.getText();
    if (message != null && message.trim().length() > 0)
    {
      parameters.setProperty("message", message);
    }

    String resultVar = resultVarTextField.getText();
    parameters.setProperty("resultVar", resultVar);

    String pdfAuthor = pdfAuthorTextField.getText();
    parameters.setProperty("_pdfAuthor", pdfAuthor);

    String pdfSubject = pdfSubjectTextField.getText();
    parameters.setProperty("_pdfSubject", pdfSubject);

    String pdfTitle = pdfTitleTextField.getText();
    parameters.setProperty("_pdfTitle", pdfTitle);

    formNode.setParameters(parameters);
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents() throws Exception
  {
    this.setLayout(gridBagLayout);

    messageLabel.setText("Message:");
    messageTextArea.setLineWrap(true);
    messageTextArea.setWrapStyleWord(true);

    propertiesTextArea.setFont(Options.getEditorFont());

    resultVarLabel.setText("Result variable:");
    pdfAuthorLabel.setText("PDF Author:");
    pdfSubjectLabel.setText("PDF Subject:");
    pdfTitleLabel.setText("PDF Title:");
    propertiesLabel.setText("Doc. properties:");

    Dimension dim = resultVarTextField.getPreferredSize();
    dim.width = 5 * dim.height;
    resultVarTextField.setPreferredSize(dim);
    resultVarTextField.setMinimumSize(dim);

    this.add(messageLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(messageScrollPane,
      new GridBagConstraints(1, 0, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(resultVarLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(resultVarTextField,
      new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(propertiesLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(propertiesScrollPane,
      new GridBagConstraints(1, 2, 1, 1, 0.0, 0.5, GridBagConstraints.CENTER,
        GridBagConstraints.BOTH,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(pdfAuthorLabel,
      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(pdfAuthorTextField,
      new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(pdfSubjectLabel,
      new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(pdfSubjectTextField,
      new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(pdfTitleLabel,
      new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(pdfTitleTextField,
      new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    messageScrollPane.getViewport().add(messageTextArea);
    propertiesScrollPane.getViewport().add(propertiesTextArea, null);
  }
}
