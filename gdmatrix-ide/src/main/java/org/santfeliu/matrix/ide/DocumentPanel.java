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
package org.santfeliu.matrix.ide;

import com.l2fprod.common.propertysheet.Property;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.undo.UndoManager;
import org.matrix.translation.TranslationConstants;
import org.santfeliu.swing.palette.Palette;

public abstract class DocumentPanel extends JPanel
{
  private MainPanel mainPanel;
  private DocumentType documentType;
  private String displayName = "new";
  private String description = "";
  private String language = TranslationConstants.UNIVERSAL_LANGUAGE;
  private boolean modified = false;
  private String connectionUrl;
  private File directory;
  private String docId;
  private int version;

  public void setMainPanel(MainPanel mainPanel)
  {
    this.mainPanel = mainPanel;
  }

  public MainPanel getMainPanel()
  {
    return mainPanel;
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public int getVersion()
  {
    return version;
  }

  public void setVersion(int version)
  {
    this.version = version;
  }
  
  public void setDocumentType(DocumentType documentType)
  {
    this.documentType = documentType;
  }

  public DocumentType getDocumentType()
  {
    return documentType;
  }

  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
    updateDisplayName();
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public String getConnectionUrl()
  {
    return connectionUrl;
  }

  public void setConnectionUrl(String connectionUrl)
  {
    this.connectionUrl = connectionUrl;
  }

  public void updateDisplayName()
  {
    // update tab title
    Component comp = getParent();
    if (comp instanceof JTabbedPane)
    {
      JTabbedPane tabbedPane = (JTabbedPane)comp;
      int count = tabbedPane.getComponentCount();
      boolean done = false;
      int index = 0;
      while (!done && index < count)
      {
        Component tab = tabbedPane.getComponentAt(index);
        if (tab == this)
        {
          tabbedPane.setTitleAt(index,
            displayName + (isModified() ? "*" : ""));
          done = true;
        }
        index++;
      }
    }
  }

  public boolean isModified()
  {
    return modified;
  }

  public void setModified(boolean modified)
  {
    if (this.modified != modified)
    {
      this.modified = modified;
      updateDisplayName();
      getMainPanel().updateActions();
    }
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getDescription()
  {
    return description;
  }

  public void setLanguage(String language)
  {
    this.language = language;
  }

  public String getLanguage()
  {
    return language;
  }

  public void setDirectory(File dir)
  {
    this.directory = dir;
  }

  public File getDirectory()
  {
    return directory;
  }

  public UndoManager getUndoManager()
  {
    return null;
  }

  public Font getEditorFont()
  {
    return Options.getEditorFont();
  }

  public void objectPropertyChanged(Object editObject, Property property)
  {
  }

  // ************ operations **************

  public void activate()
  {
    mainPanel.setRightPanelVisible(false);
    mainPanel.getPalette().setSelectedCategory(Palette.EMPTY);
    mainPanel.setEditObject(null);
  }

  public void create() throws Exception
  {
  }

  public void open(InputStream is) throws Exception
  {
  }

  public void save(OutputStream os) throws Exception
  {
  }

  public void print()
  {
    JOptionPane.showMessageDialog(mainPanel, "Not available.",
      "Printing", JOptionPane.WARNING_MESSAGE);
  }

  public void copy()
  {
  }

  public void paste()
  {
  }

  public void delete()
  {
  }

  public void find()
  {
  }

  public void setZoom(double size)
  {
  }

  public double getZoom()
  {
    return 100.0;
  }

  public boolean isFindEnabled()
  {
    return false;
  }
}
