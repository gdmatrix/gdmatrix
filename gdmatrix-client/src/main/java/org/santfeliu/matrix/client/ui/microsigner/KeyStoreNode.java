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

import java.security.KeyStore;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author realor
 */
public class KeyStoreNode extends DefaultMutableTreeNode
{
  private KeyStore keyStore;
  private String keyStorePath;
  private String keyStorePassword;
  private boolean askForPIN;

  public KeyStoreNode()
  {
  }

  public void setKeyStore(KeyStore keyStore)
  {
    this.keyStore = keyStore;
  }

  public KeyStore getKeyStore()
  {
    return keyStore;
  }

  public void setKeyStorePath(String keyStorePath)
  {
    this.keyStorePath = keyStorePath;
  }

  public String getKeyStorePath()
  {
    return keyStorePath;
  }

  public void setKeyStorePassword(String keyStorePassword)
  {
    this.keyStorePassword = keyStorePassword;
  }

  public String getKeyStorePassword()
  {
    return keyStorePassword;
  }

  public void setAskForPIN(boolean askForPIN)
  {
    this.askForPIN = askForPIN;
  }

  public boolean isAskForPIN()
  {
    return askForPIN;
  }
  
  public String getProviderClassName()
  {
    return keyStore.getProvider().getClass().getName();
  }
  
  public String getKeyStoreType()
  {
    return keyStore.getType();
  }
  
  public String toString()
  {
    if (keyStore == null) return null;
    return keyStore.getType() + " [" + keyStore.getProvider().toString() + "]";
  }
}
