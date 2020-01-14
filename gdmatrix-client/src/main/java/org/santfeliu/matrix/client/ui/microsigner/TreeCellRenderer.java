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

import java.awt.Color;
import java.awt.Component;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 *
 * @author unknown
 */
public class TreeCellRenderer extends DefaultTreeCellRenderer
{
  private Icon keyStoreIcon;
  private Icon certificateIcon;

  public TreeCellRenderer()
  {
    try
    {
      URL url;
      url = getClass().getClassLoader().getResource(
        "org/santfeliu/matrix/client/ui/microsigner/resources/keystore.gif");    
      keyStoreIcon = new ImageIcon(url);    
      url = getClass().getClassLoader().getResource(
        "org/santfeliu/matrix/client/ui/microsigner/resources/certificate.gif");
      certificateIcon = new ImageIcon(url);
    }
    catch (Exception ex)
    {
    }
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree,
    Object value, boolean sel, boolean expanded, boolean leaf,
    int row, boolean hasFocus)
  {  
    String stringValue = tree.convertValueToText(value, sel,
                                                 expanded, leaf, row, hasFocus);
    this.hasFocus = hasFocus;
    setText(stringValue);
    if (sel)
      setForeground(getTextSelectionColor());
    else
      setForeground(getTextNonSelectionColor());
    // There needs to be a way to specify disabled icons.
    if (!tree.isEnabled())
    {
      setEnabled(false);
    }
    else 
    {
      setEnabled(true);
    }
    
    if (value instanceof KeyStoreNode)
    {
      setIcon(keyStoreIcon);
    }
    else if (value instanceof CertificateNode)
    {
      CertificateNode certificateNode = (CertificateNode)value;
      if (!certificateNode.isValid())
      {
        setForeground(Color.RED);
      }
      setIcon(certificateIcon);
    }
    else
    {
      setIcon(null);
    }
    setComponentOrientation(tree.getComponentOrientation());        
    selected = sel;
    
    return this;
  }
}
