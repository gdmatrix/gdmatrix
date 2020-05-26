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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author lopezrj
 */
public class BiometricFormParametersEditor extends JPanel implements NodeEditor
{
  private FormNode formNode;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel selectModeLabel = new JLabel();
  private JLabel xmlLabel = new JLabel();
  private JLabel pdfLabel = new JLabel();
  private JLabel sigIdLabel = new JLabel();
  private JLabel pdfDocIdLabel = new JLabel();
  private JLabel xslLabel = new JLabel();  
  private JLabel commonFieldsLabel = new JLabel();
  private JLabel messageLabel = new JLabel();
  private JLabel deviceNameLabel = new JLabel();
  private JLabel signerNameLabel = new JLabel();
  private JLabel signerIdentLabel = new JLabel();  
  private JLabel signerIdentTypeLabel = new JLabel(); 
  private JLabel apiSubscriptionLabel = new JLabel();
  private JLabel apiBaseUrlLabel = new JLabel();
  private JLabel apiUsernameLabel = new JLabel();
  private JLabel apiPasswordLabel = new JLabel();
  private JLabel signLabel = new JLabel();
  //private JLabel signSizeLabel = new JLabel();
  private JLabel signSizeXLabel = new JLabel();
  private JLabel signSizeYLabel = new JLabel();
  //private JLabel signPosLabel = new JLabel();
  private JLabel signPosXLabel = new JLabel();
  private JLabel signPosYLabel = new JLabel();
  private JLabel signPosPageLabel = new JLabel();
  private JLabel signPosAnchorLabel = new JLabel();
  
  private JTextField sigIdTextField = new JTextField();
  private JTextField pdfDocIdTextField = new JTextField();  
  private JTextField xslTextField = new JTextField();  
  private JTextArea messageTextArea = new JTextArea();
  private JTextField deviceNameTextField = new JTextField();
  private JTextField signerNameTextField = new JTextField();
  private JTextField signerIdentTextField = new JTextField();  
  private JTextField signerIdentTypeTextField = new JTextField(); 
  private JTextField apiBaseUrlTextField = new JTextField();   
  private JTextField apiUsernameTextField = new JTextField();   
  private JTextField apiPasswordTextField = new JTextField(); 
  private JTextField signSizeXTextField = new JTextField();
  private JTextField signSizeYTextField = new JTextField();
  private JTextField signPosXTextField = new JTextField();
  private JTextField signPosYTextField = new JTextField();
  private JTextField signPosPageTextField = new JTextField();
  private JTextField signPosAnchorTextField = new JTextField();  
  
  private JScrollPane scrollPane = new JScrollPane();

  public BiometricFormParametersEditor()
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

    Object sigId = parameters.get("sigId");
    if (sigId != null && sigId.toString().trim().length() > 0)
    {
      sigIdTextField.setText(sigId.toString());
    }      
    Object xsl = parameters.get("xsl");
    if (xsl != null && xsl.toString().trim().length() > 0)
    {
      xslTextField.setText(xsl.toString());
    }
    Object deviceName = parameters.get("deviceName");
    if (deviceName != null && deviceName.toString().trim().length() > 0)
    {
      deviceNameTextField.setText(deviceName.toString());
    }      
    Object signerName = parameters.get("signerName");
    if (signerName != null && signerName.toString().trim().length() > 0)
    {
      signerNameTextField.setText(signerName.toString());
    }      
    Object signerIdent = parameters.get("signerIdent");
    if (signerIdent != null && signerIdent.toString().trim().length() > 0)
    {
      signerIdentTextField.setText(signerIdent.toString());
    }      
    Object signerIdentType = parameters.get("signerIdentType");
    if (signerIdentType != null && signerIdentType.toString().trim().length() > 0)
    {
      signerIdentTypeTextField.setText(signerIdentType.toString());
    }
    Object message = parameters.get("message");
    if (message != null)
    {
      messageTextArea.setText(String.valueOf(message));
      messageTextArea.setCaretPosition(0);
    }
    Object pdfDocId = parameters.get("pdfDocId");
    if (pdfDocId != null && pdfDocId.toString().trim().length() > 0)
    {
      pdfDocIdTextField.setText(pdfDocId.toString());
    }
    Object apiBaseUrl = parameters.get("apiBaseUrl");
    if (apiBaseUrl != null && apiBaseUrl.toString().trim().length() > 0)
    {
      apiBaseUrlTextField.setText(apiBaseUrl.toString());
    }
    Object apiUsername = parameters.get("apiUsername");
    if (apiUsername != null && apiUsername.toString().trim().length() > 0)
    {
      apiUsernameTextField.setText(apiUsername.toString());
    }
    Object apiPassword = parameters.get("apiPassword");
    if (apiPassword != null && apiPassword.toString().trim().length() > 0)
    {
      apiPasswordTextField.setText(apiPassword.toString());
    }
    Object signSizeX = parameters.get("signSizeX");
    if (signSizeX != null && signSizeX.toString().trim().length() > 0)
    {
      signSizeXTextField.setText(signSizeX.toString());
    }
    Object signSizeY = parameters.get("signSizeY");
    if (signSizeY != null && signSizeY.toString().trim().length() > 0)
    {
      signSizeYTextField.setText(signSizeY.toString());
    }
    Object signPosX = parameters.get("signPosX");
    if (signPosX != null && signPosX.toString().trim().length() > 0)
    {
      signPosXTextField.setText(signPosX.toString());
    }
    Object signPosY = parameters.get("signPosY");
    if (signPosY != null && signPosY.toString().trim().length() > 0)
    {
      signPosYTextField.setText(signPosY.toString());
    }
    Object signPosPage = parameters.get("signPosPage");
    if (signPosPage != null && signPosPage.toString().trim().length() > 0)
    {
      signPosPageTextField.setText(signPosPage.toString());
    }
    Object signPosAnchor = parameters.get("signPosAnchor");
    if (signPosAnchor != null && signPosAnchor.toString().trim().length() > 0)
    {
      signPosAnchorTextField.setText(signPosAnchor.toString());
    }
    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
  }

  @Override
  public void stopEditing() throws Exception
  {
    Properties parameters = new Properties();
    String sigId = sigIdTextField.getText();
    if (sigId != null && sigId.trim().length() > 0)
    {
      parameters.setProperty("sigId", sigId);
    }
    String xsl = xslTextField.getText();
    if (xsl != null && xsl.trim().length() > 0)
    {
      parameters.setProperty("xsl", xsl);
    }
    String deviceName = deviceNameTextField.getText();
    if (deviceName != null && deviceName.trim().length() > 0)
    {
      parameters.setProperty("deviceName", deviceName);
    }
    String signerName = signerNameTextField.getText();
    if (signerName != null && signerName.trim().length() > 0)
    {
      parameters.setProperty("signerName", signerName);
    }
    String signerIdent = signerIdentTextField.getText();
    if (signerIdent != null && signerIdent.trim().length() > 0)
    {
      parameters.setProperty("signerIdent", signerIdent);
    }
    String signerIdentType = signerIdentTypeTextField.getText();
    if (signerIdentType != null && signerIdentType.trim().length() > 0)
    {
      parameters.setProperty("signerIdentType", signerIdentType);
    }
    String message = messageTextArea.getText();
    if (message != null && message.trim().length() > 0)
    {
      parameters.setProperty("message", message);
    }
    String pdfDocId = pdfDocIdTextField.getText();
    if (pdfDocId != null && pdfDocId.trim().length() > 0)
    {
      parameters.setProperty("pdfDocId", pdfDocId);
    }
    String apiBaseUrl = apiBaseUrlTextField.getText();
    if (apiBaseUrl != null && apiBaseUrl.trim().length() > 0)
    {
      parameters.setProperty("apiBaseUrl", apiBaseUrl);
    }
    String apiUsername = apiUsernameTextField.getText();
    if (apiUsername != null && apiUsername.trim().length() > 0)
    {
      parameters.setProperty("apiUsername", apiUsername);
    }
    String apiPassword = apiPasswordTextField.getText();
    if (apiPassword != null && apiPassword.trim().length() > 0)
    {
      parameters.setProperty("apiPassword", apiPassword);
    }
    String signSizeX = signSizeXTextField.getText();
    if (signSizeX != null && signSizeX.trim().length() > 0)
    {
      parameters.setProperty("signSizeX", signSizeX);
    }
    String signSizeY = signSizeYTextField.getText();
    if (signSizeY != null && signSizeY.trim().length() > 0)
    {
      parameters.setProperty("signSizeY", signSizeY);
    }
    String signPosX = signPosXTextField.getText();
    if (signPosX != null && signPosX.trim().length() > 0)
    {
      parameters.setProperty("signPosX", signPosX);
    }
    String signPosY = signPosYTextField.getText();
    if (signPosY != null && signPosY.trim().length() > 0)
    {
      parameters.setProperty("signPosY", signPosY);
    }
    String signPosPage = signPosPageTextField.getText();
    if (signPosPage != null && signPosPage.trim().length() > 0)
    {
      parameters.setProperty("signPosPage", signPosPage);
    }
    String signPosAnchor = signPosAnchorTextField.getText();
    if (signPosAnchor != null && signPosAnchor.trim().length() > 0)
    {
      parameters.setProperty("signPosAnchor", signPosAnchor);
    }    
    formNode.setParameters(parameters);
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents() throws Exception
  {
    this.setLayout(gridBagLayout1);
    Border bottomBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK);
    selectModeLabel.setText("Select signature file id or PDF document id");
    selectModeLabel.setBorder(bottomBorder);
    xmlLabel.setText("XML");
    Font font = xmlLabel.getFont();
    font = new Font(font.getFontName(), Font.BOLD, font.getSize());
    xmlLabel.setFont(font);
    pdfLabel.setText("PDF");
    pdfLabel.setFont(font);
    sigIdLabel.setText("Signature Id.:");
    pdfDocIdLabel.setText("PDF DocId:");
    xslLabel.setText("XSL:");
    commonFieldsLabel.setText("Common fields");
    commonFieldsLabel.setBorder(bottomBorder);
    messageLabel.setText("Message:");
    deviceNameLabel.setText("Device name:");
    signerNameLabel.setText("Signer name:");
    signerIdentLabel.setText("Signer ident:");
    signerIdentTypeLabel.setText("Signer ident. type:");
    apiSubscriptionLabel.setText("VIDSIGNER SUBSCRIPTION");
    apiSubscriptionLabel.setFont(font);
    apiBaseUrlLabel.setText("URL:");
    apiUsernameLabel.setText("Username:");
    apiPasswordLabel.setText("Password:");
    signLabel.setText("VISIBLE SIGNATURE");
    signLabel.setFont(font);
    //signSizeLabel.setText("SIZE");
    signSizeXLabel.setText("Size X:");
    signSizeYLabel.setText("Size Y:");
    //signPosLabel.setText("POSITION");
    signPosXLabel.setText("Position X:");
    signPosYLabel.setText("Position Y:");
    signPosPageLabel.setText("Position Page:");
    signPosAnchorLabel.setText("Position Anchor:");

    sigIdTextField.setPreferredSize(new Dimension(140, 24));
    sigIdTextField.setMinimumSize(new Dimension(140, 24));
    xslTextField.setPreferredSize(new Dimension(140, 24));
    xslTextField.setMinimumSize(new Dimension(140, 24));
    pdfDocIdTextField.setPreferredSize(new Dimension(140, 24));
    pdfDocIdTextField.setMinimumSize(new Dimension(140, 24));
    messageTextArea.setFont(new Font("Dialog", 0, 14));
    messageTextArea.setLineWrap(true);
    messageTextArea.setWrapStyleWord(true);
    deviceNameTextField.setPreferredSize(new Dimension(140, 24));
    deviceNameTextField.setMinimumSize(new Dimension(140, 24));
    signerNameTextField.setPreferredSize(new Dimension(140, 24));
    signerNameTextField.setMinimumSize(new Dimension(140, 24));
    signerIdentTextField.setPreferredSize(new Dimension(140, 24));
    signerIdentTextField.setMinimumSize(new Dimension(140, 24));
    signerIdentTypeTextField.setPreferredSize(new Dimension(140, 24));
    signerIdentTypeTextField.setMinimumSize(new Dimension(140, 24));
    signSizeXTextField.setPreferredSize(new Dimension(140, 24));
    signSizeXTextField.setMinimumSize(new Dimension(140, 24));
    signSizeYTextField.setPreferredSize(new Dimension(140, 24));
    signSizeYTextField.setMinimumSize(new Dimension(140, 24));
    signPosXTextField.setPreferredSize(new Dimension(140, 24));
    signPosXTextField.setMinimumSize(new Dimension(140, 24));
    signPosYTextField.setPreferredSize(new Dimension(140, 24));
    signPosYTextField.setMinimumSize(new Dimension(140, 24));
    signPosPageTextField.setPreferredSize(new Dimension(140, 24));
    signPosPageTextField.setMinimumSize(new Dimension(140, 24));
    signPosAnchorTextField.setPreferredSize(new Dimension(140, 24));
    signPosAnchorTextField.setMinimumSize(new Dimension(140, 24));

    this.add(selectModeLabel,
      new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(4, 0, 2, 4), 0, 0));

    this.add(xmlLabel,
      new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(pdfLabel,
      new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(sigIdLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(sigIdTextField,
      new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(pdfDocIdLabel,
      new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(pdfDocIdTextField,
      new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(xslLabel,
      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(xslTextField,
      new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(commonFieldsLabel,
      new GridBagConstraints(0, 4, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(6, 0, 2, 4), 0, 0));

    this.add(messageLabel,
      new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(scrollPane,
      new GridBagConstraints(1, 5, 3, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
        new Insets(2, 4, 2, 4), 0, 0));
    scrollPane.getViewport().add(messageTextArea);

    this.add(deviceNameLabel,
      new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(deviceNameTextField,
      new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(signerNameLabel,
      new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(signerNameTextField,
      new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(signerIdentLabel,
      new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(signerIdentTextField,
      new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(signerIdentTypeLabel,
      new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(signerIdentTypeTextField,
      new GridBagConstraints(3, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(apiSubscriptionLabel,
      new GridBagConstraints(0, 8, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(4, 0, 2, 4), 0, 0));

    this.add(apiBaseUrlLabel,
      new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(apiBaseUrlTextField,
      new GridBagConstraints(1, 9, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(apiUsernameLabel,
      new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(apiUsernameTextField,
      new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(apiPasswordLabel,
      new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(apiPasswordTextField,
      new GridBagConstraints(3, 10, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(signLabel,
      new GridBagConstraints(0, 11, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(4, 0, 2, 4), 0, 0));

    /*    
    this.add(signPosLabel, 
             new GridBagConstraints(0, 12, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(signSizeLabel, 
             new GridBagConstraints(2, 12, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(2, 4, 2, 4), 0, 0));
     */
    this.add(signPosXLabel,
      new GridBagConstraints(0, 12, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(signPosXTextField,
      new GridBagConstraints(1, 12, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(signSizeXLabel,
      new GridBagConstraints(2, 12, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(signSizeXTextField,
      new GridBagConstraints(3, 12, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(signPosYLabel,
      new GridBagConstraints(0, 13, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(signPosYTextField,
      new GridBagConstraints(1, 13, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(signSizeYLabel,
      new GridBagConstraints(2, 13, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(signSizeYTextField,
      new GridBagConstraints(3, 13, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(signPosPageLabel,
      new GridBagConstraints(0, 14, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(signPosPageTextField,
      new GridBagConstraints(1, 14, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

    this.add(signPosAnchorLabel,
      new GridBagConstraints(0, 15, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(signPosAnchorTextField,
      new GridBagConstraints(1, 15, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));

  }
}
