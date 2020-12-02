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
package org.santfeliu.matrix.client.ui.microsigner;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.cert.X509Certificate;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.santfeliu.matrix.client.ui.microsigner.action.*;

/**
 *
 * @author realor
 */
public class MainPanel extends JPanel
{
  private MicroSigner microSigner;
  private CardLayout cardLayout = new CardLayout();
  private JLabel headerLabel = new JLabel();
  private BorderLayout borderLayout = new BorderLayout();
  private JPanel certificatesPanel = new JPanel();
  private JScrollPane scrollPane = new JScrollPane();
  private JTree certificatesTree = new JTree();
  private JLabel messageLabel = new JLabel();
  private SignaturePanel signaturePanel = new SignaturePanel();
  private DefaultTreeModel treeModel;

  private AddKeyStoreAction addKeyStoreAction = new AddKeyStoreAction(this);
  private AddPKCS12Action addPKCS12Action = new AddPKCS12Action(this);
  private EditKeyStoreAction editKeyStoreAction = new EditKeyStoreAction(this);
  private ReloadKeyStoreAction reloadKeyStoreAction = new ReloadKeyStoreAction(this);
  private RemoveKeyStoreAction removeKeyStoreAction = new RemoveKeyStoreAction(this);
  private ViewCertificateAction viewCertificateAction = new ViewCertificateAction(this);
  private SaveConfigAction saveConfigAction = new SaveConfigAction(this);
  private AboutAction aboutAction = new AboutAction(this);

  public MainPanel(MicroSigner microSigner)
  {
    this.microSigner = microSigner;
    try
    {
      initComponents();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public MicroSigner getMicroSigner()
  {
    return microSigner;
  }

  public void setEnableInputs(final boolean enable)
  {
    certificatesTree.setEnabled(enable);
  }

  public void setInfo(String text)
  {
    headerLabel.setText(text);
  }

  private void initComponents() throws Exception
  {
    DefaultMutableTreeNode rootNode = 
      new DefaultMutableTreeNode(MicroSigner.getLocalizedText("KeyStores"));
    treeModel = new DefaultTreeModel(rootNode);
    certificatesTree.setModel(treeModel);

    setLayout(cardLayout);
    scrollPane.getViewport().add(certificatesTree, null);
    certificatesPanel.setLayout(borderLayout);
    certificatesPanel.add(headerLabel, BorderLayout.NORTH);
    certificatesPanel.add(scrollPane, BorderLayout.CENTER);
    headerLabel.setText(MicroSigner.getLocalizedText("SelectCertificate"));
    headerLabel.setBorder(new EmptyBorder(4, 4, 4, 4));

    this.add(certificatesPanel, "CERTIFICATES");
    this.add(messageLabel, "MESSAGE");
    this.add(signaturePanel, "SIGNATURE");

    messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    messageLabel.setBackground(Color.white);
    messageLabel.setOpaque(true);
    messageLabel.setBorder(new LineBorder(Color.gray, 1));
    messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    messageLabel.setVerticalAlignment(SwingConstants.CENTER);
    
    certificatesTree.setCellRenderer(new TreeCellRenderer());
    certificatesTree.setToggleClickCount(0);
    certificatesTree.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent event)
      {
        TreePath selPath = 
          certificatesTree.getPathForLocation(event.getX(), event.getY());
        certificatesTree.setSelectionPath(selPath);

        if (event.getButton() == MouseEvent.BUTTON1 &&
            event.getClickCount() == 2)
        {
          fireSelectedNodeAction();
        }
        else if (event.getButton() == MouseEvent.BUTTON2 ||
            event.getButton() == MouseEvent.BUTTON3)
        {
          JMenu menu = new JMenu();
          if (selPath != null)
          {
            Object node = selPath.getLastPathComponent();
            if (node instanceof KeyStoreNode)
            {
              menu.add(editKeyStoreAction);
              menu.add(removeKeyStoreAction);
              menu.add(reloadKeyStoreAction);
              menu.addSeparator();
            }
            else if (node instanceof CertificateNode)
            {
              menu.add(viewCertificateAction);
              menu.addSeparator();
            }
          }
          menu.add(addPKCS12Action);
          menu.add(addKeyStoreAction);
          menu.add(saveConfigAction);
          menu.addSeparator();
          menu.add(aboutAction);
          JPopupMenu popup = menu.getPopupMenu();
          popup.pack();
          popup.show(certificatesTree, event.getX(), event.getY());          
        }
      }
    });
    certificatesTree.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyReleased(KeyEvent event)
      {        
        if (event.getKeyCode() == KeyEvent.VK_ENTER)
        {
          fireSelectedNodeAction();
        }
      }
    });
  }

  public void showMessage(String message)
  {
    messageLabel.setText(message);
    cardLayout.show(this, "MESSAGE");
    signaturePanel.stopAnimation();
  }

  public void showCertificates()
  {
    cardLayout.show(this, "CERTIFICATES");
    signaturePanel.stopAnimation();
  }

  public void showSignature(X509Certificate certificate, byte[] signatureData)
  {
    cardLayout.show(this, "SIGNATURE");
    signaturePanel.setCertificate(certificate, signatureData);
    signaturePanel.startAnimation();
  }

  public void stopSignature()
  {
    signaturePanel.stopAnimation();
  }

  public DefaultMutableTreeNode getRoot()
  {
    return (DefaultMutableTreeNode)treeModel.getRoot();
  }

  public KeyStoreNode getSelectedKeyStoreNode()
  {
    TreePath path = certificatesTree.getSelectionPath();
    if (path == null) return null;
    Object node = path.getLastPathComponent();
    return (node instanceof KeyStoreNode) ? (KeyStoreNode)node : null;
  }

  public CertificateNode getSelectedCertificateNode()
  {
    TreePath path = certificatesTree.getSelectionPath();
    if (path == null) return null;
    Object node = path.getLastPathComponent();
    return (node instanceof CertificateNode) ? (CertificateNode)node : null;
  }

  public void addKeyStoreNode(KeyStoreNode ksNode)
  {
    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
    int index = rootNode.getChildCount();
    rootNode.add(ksNode);
    treeModel.nodesWereInserted(rootNode, new int[]{index});
    TreePath path;
    if (ksNode.getChildCount() > 0)
    {
      CertificateNode child = (CertificateNode)ksNode.getChildAt(0);
      path = new TreePath(child.getPath());
    }
    else
    {    
      path = new TreePath(ksNode.getPath());
    }
    certificatesTree.expandPath(path);
    certificatesTree.setSelectionPath(path);
  }
  
  public void removeKeyStoreNode(KeyStoreNode ksNode)
  {
    DefaultMutableTreeNode parentNode = 
      (DefaultMutableTreeNode)ksNode.getParent();
    int index = parentNode.getIndex(ksNode);
    parentNode.remove(index);
    treeModel.nodesWereRemoved(parentNode, new int[]{index}, 
                               new Object[]{ksNode});    
  }

  public void refreshKeyStoreNode(KeyStoreNode ksNode)
  {
    treeModel.nodeStructureChanged(ksNode);
    certificatesTree.expandPath(new TreePath(ksNode.getPath()));
  }

  public Frame getFrame()
  {
    Window window = SwingUtilities.getWindowAncestor(this);
    return (window instanceof Frame) ? (Frame)window : null;
  }

  public void fireSelectedNodeAction()
  {
    TreePath selPath = certificatesTree.getSelectionPath();
    if (selPath != null)
    {
      Object node = selPath.getLastPathComponent();
      if (node instanceof KeyStoreNode)
      {
        editKeyStoreAction.actionPerformed(
          new ActionEvent(this, 10, "editKs"));
      }
      else if (node instanceof CertificateNode)
      {
        viewCertificateAction.actionPerformed(
          new ActionEvent(this, 11, "viewCert"));
      }
    }
  }  
}
