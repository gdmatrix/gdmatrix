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
package org.santfeliu.doc.swing;

import java.awt.Dimension;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.swing.ErrorMessagePanel;

public abstract class DocumentBasePanel extends JPanel implements DocumentPanel
{
  protected String username;
  protected String password;
  protected String wsdlLocation;
  protected URL wsDirectoryURL;
  protected boolean documentLockedByUser;
  protected boolean documentExists;
  protected boolean documentLocked;
  
  protected ResourceBundle resourceBundle = 
    loadResourceBundle(Locale.getDefault());
  
  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getUsername()
  {
    return username;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getPassword()
  {
    return password;
  }
  
  public boolean isDocumentLockedByUser()
  {
    return documentLockedByUser;
  }
  
  public boolean isDocumentLocked()
  {
    return documentLocked;
  }
  
  public boolean existsDocument()
  {
    return documentExists;
  }
  
  public DocumentManagerClient getClient()
    throws Exception
  {
    DocumentManagerClient client;
    if (wsDirectoryURL != null)
      client = new DocumentManagerClient(this.wsDirectoryURL, username, password);
    else
      client = new DocumentManagerClient(wsdlLocation, username, password);

    return client;
  }
  
  public void showError(Exception ex)
  {
    ErrorMessagePanel.showErrorMessage(
      SwingUtilities.getWindowAncestor(this), new Dimension(500, 400), ex);    
  }

  public void setDocumentLocked(boolean documentLocked)
  {
    this.documentLocked = documentLocked;
  }
  
  protected ResourceBundle loadResourceBundle(Locale locale) 
  {    
    return ResourceBundle.getBundle("org.santfeliu.doc.swing.resources.DocumentPanelBundle", locale);
  }
  
  public String getLocalizedText(String text) 
  {
    String result = null;
    try
    {
      result = resourceBundle.getString(text);
    }
    catch (MissingResourceException ex)
    {
      result = "{" + text + "}";
    }
    return result;
  }

  public URL getWsDirectoryURL()
  {
    return wsDirectoryURL;
  }

  public void setWsDirectoryURL(URL wsDirectoryURL)
  {
    this.wsDirectoryURL = wsDirectoryURL;
  }

  @Deprecated
  public void setWsdlLocation(String wsdlLocation)
  {
    this.wsdlLocation = wsdlLocation;
  }

  @Deprecated
  public String getWsdlLocation()
  {
    return wsdlLocation;
  }
}
