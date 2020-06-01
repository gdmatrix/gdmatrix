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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.matrix.ide.Options;
import org.santfeliu.swing.text.XMLEditorKit;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.SendMailNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author realor
 */
public class SendMailNodeEditor extends JPanel
  implements NodeEditor
{
  private SendMailNode sendMailNode;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel northPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel hostLabel = new JLabel();
  private JTextField hostTextField = new JTextField();
  private JLabel recipientsTOLabel = new JLabel();
  private JTextField recipientsTOTextField = new JTextField();
  private JLabel recipientsCCLabel = new JLabel();
  private JTextField recipientsCCTextField = new JTextField();
  private JLabel recipientsBCCLabel = new JLabel();
  private JTextField recipientsBCCTextField = new JTextField();
  private JLabel subjectLabel = new JLabel();
  private JTextField subjectTextField = new JTextField();
  private JLabel senderLabel = new JLabel();
  private JTextField senderTextField = new JTextField();
  private JPanel centerPanel = new JPanel();
  private JScrollPane scrollPane = new JScrollPane();
  private JTextPane textArea = new JTextPane();
  private BorderLayout borderLayout2 = new BorderLayout();

  public SendMailNodeEditor()
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
    sendMailNode = (SendMailNode)node;
    hostTextField.setText(sendMailNode.getHost());
    senderTextField.setText(sendMailNode.getSender());
    recipientsTOTextField.setText(sendMailNode.getRecipientsTO());
    recipientsCCTextField.setText(sendMailNode.getRecipientsCC());
    recipientsBCCTextField.setText(sendMailNode.getRecipientsBCC());
    subjectTextField.setText(sendMailNode.getSubject());
    String contentType = sendMailNode.getContentType();
    if (contentType != null && contentType.startsWith("text/html"))
    {
      textArea.setEditorKitForContentType(
        "text/xml", new XMLEditorKit());
      textArea.setContentType("text/xml");
    }
    else
    {
      textArea.setContentType("text/plain");
    }
    textArea.setFont(Options.getEditorFont());
    textArea.setSelectionColor(new Color(198, 198, 198));
    textArea.setText(sendMailNode.getMessage());
    textArea.setCaretPosition(0);
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
    sendMailNode.setHost(hostTextField.getText());
    sendMailNode.setSender(senderTextField.getText());
    sendMailNode.setRecipientsTO(recipientsTOTextField.getText());
    sendMailNode.setRecipientsCC(recipientsCCTextField.getText());
    sendMailNode.setRecipientsBCC(recipientsBCCTextField.getText());
    sendMailNode.setSubject(subjectTextField.getText());
    sendMailNode.setMessage(textArea.getText());
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents() throws Exception
  {
    this.setLayout(borderLayout1);
    northPanel.setLayout(gridBagLayout1);
    hostLabel.setText("Host:");
    recipientsTOLabel.setText("TO:");
    recipientsCCLabel.setText("CC:");
    recipientsBCCLabel.setText("BCC:");
    subjectLabel.setText("Subject:");
    senderLabel.setText("Sender:");
    centerPanel.setLayout(borderLayout2);
    centerPanel.setBorder(BorderFactory.createTitledBorder("Message"));

    northPanel.add(hostLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(hostTextField,
      new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));
    northPanel.add(recipientsTOLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(recipientsTOTextField,
      new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));
    northPanel.add(recipientsCCLabel,
      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(recipientsCCTextField,
      new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));
    northPanel.add(recipientsBCCLabel,
      new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(recipientsBCCTextField,
      new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));
    northPanel.add(subjectLabel,
      new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(subjectTextField,
      new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));
    northPanel.add(senderLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(senderTextField,
      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));
    this.add(northPanel, BorderLayout.NORTH);
    scrollPane.getViewport().add(textArea, null);
    centerPanel.add(scrollPane, BorderLayout.CENTER);
    this.add(centerPanel, BorderLayout.CENTER);
  }
}
