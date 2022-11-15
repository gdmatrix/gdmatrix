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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoManager;
import org.matrix.doc.Document;
import org.matrix.security.SecurityManagerPort;
import org.matrix.security.SecurityManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.swing.palette.Palette;
import org.santfeliu.swing.undo.BeanUndoableEdit;
import org.santfeliu.matrix.ide.action.BaseAction;
import org.santfeliu.matrix.ide.action.CloseAction;
import org.santfeliu.matrix.ide.action.WrapperAction;
import org.santfeliu.swing.FlatSplitPane;
import org.santfeliu.swing.TabbedPane;
import org.santfeliu.swing.layout.WrapLayout;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.util.function.Predicate;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;


/**
 *
 * @author realor
 */
public class MainPanel extends JPanel
{
  private MatrixIDE ide;
  private ResourceBundle resources;
  private HashMap<String, Action> actions = new HashMap();
  private ArrayList<DocumentType> documentTypes = new ArrayList();
  private ArrayList connections = new ArrayList();

  private JPanel northPanel = new JPanel();
  private ToolBar toolBar = new ToolBar();
  private ZoomPanel zoomPanel = new ZoomPanel();
  private ConnectionPanel connPanel = new ConnectionPanel();
  private WrapLayout northLayout = new WrapLayout();
  private BorderLayout mainLayout = new BorderLayout();
  private JSplitPane splitPane = new FlatSplitPane();
  private JSplitPane rightSplitPane = new FlatSplitPane();
  private JLabel statusBar = new JLabel();
  private TabbedPane tabbedPane = new TabbedPane();
  private PropertySheetPanel sheet = new PropertySheetPanel();
  private Palette palette = new Palette();
  private boolean loadingObjectProperties = false;
  private Object editObject = null;
  private int dividerLocation;

  public MainPanel()
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

  public String getDividersLocation()
  {
    return dividerLocation + " " + rightSplitPane.getDividerLocation();
  }

  public void setDividersLocation(String dividers)
  {
    try
    {
      if (dividers == null) return;

      StringTokenizer tokenizer = new StringTokenizer(dividers);
      if (tokenizer.countTokens() == 2)
      {
        String s1 = tokenizer.nextToken();
        String s2 = tokenizer.nextToken();
        int d1 = Integer.parseInt(s1);
        int d2 = Integer.parseInt(s2);
        if (d1 > 0)
        {
          splitPane.setDividerLocation(d1);
        }
        else
        {
          splitPane.setDividerLocation(0.7);
        }
        rightSplitPane.setDividerLocation(d2);
      }
    }
    catch (NumberFormatException ex)
    {
      MatrixIDE.log(ex);
    }
  }

  public void adjustDividersLocation(Dimension size)
  {
    int rightWidth = 5 * palette.getPreferredSize().width / 3;
    splitPane.setDividerLocation(size.width - rightWidth);
    rightSplitPane.setDividerLocation(size.height / 2);
  }

  public void setEditObject(Object object)
  {
    editObject = object;
    updateEditObject();
  }

  public Object getEditObject()
  {
    return editObject;
  }

  public void updateEditObject()
  {
    try
    {
      if (editObject == null)
      {
        sheet.setProperties(new Property[0]);
      }
      else
      {
        loadingObjectProperties = true;
        BeanInfo bi = Introspector.getBeanInfo(editObject.getClass());
        sheet.setBeanInfo(bi);
        sheet.readFromObject(editObject);
        loadingObjectProperties = false;
      }
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  public void updateZoomPanel()
  {
    zoomPanel.updateZoomValue();
  }

  public void updateActions()
  {
    Iterator iter = actions.values().iterator();
    while (iter.hasNext())
    {
      BaseAction action = (BaseAction)iter.next();
      action.updateEnabled();
    }
    toolBar.repaint();
  }

  public void setupLocale(Locale locale)
  {
    try
    {
      resources = ResourceBundle.getBundle(
        "org.santfeliu.matrix.ide.resources.MatrixIDE", locale);
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  public void setupDocumentTypes()
  {
    try
    {
      documentTypes.clear();
      InputStream is = getClass().getResourceAsStream(
        "/org/santfeliu/matrix/ide/resources/doctypes.json");
      JSONArray array =
        (JSONArray)new JSONParser().parse(new InputStreamReader(is));
      for (int i = 0; i < array.size(); i++)
      {
        Map type = (Map)array.get(i);
        String displayName = ((String)type.get("displayName")).trim();
        String extension = ((String)type.get("extension")).trim();
        String className = ((String)type.get("className")).trim();
        String propertyName = ((String)type.get("propertyName")).trim();
        String mimeType = ((String)type.get("mimeType")).trim();
        String icon = ((String)type.get("icon")).trim();
        String docTypeId = ((String)type.get("docTypeId")).trim();
        Map propMap = (Map)type.get("properties");

        DocumentType docType = new DocumentType();
        docType.setDisplayName(displayName);
        docType.setExtension(extension);
        docType.setDocumentPanelClassName(className);
        docType.setPropertyName(propertyName);
        docType.setMimeType(mimeType);
        docType.setDocTypeId(docTypeId);
        try
        {
          ImageIcon imageIcon =
            new ImageIcon(getClass().getResource(
              "/org/santfeliu/matrix/ide/resources/images/" + icon));
            docType.setIcon(imageIcon);
        }
        catch (Exception ex)
        {
        }
        if (propMap != null)
          docType.setFixedProperties(propMap);
        documentTypes.add(docType);
      }
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  public void setupActions()
  {
    try
    {
      actions.clear();
      InputStream is = getClass().getResourceAsStream(
        "/org/santfeliu/matrix/ide/resources/actions.properties");
      Properties actionsDef = new Properties();
      actionsDef.load(is);
      Enumeration enu = actionsDef.keys();
      while (enu.hasMoreElements())
      {
        try
        {
          String actionId = (String)enu.nextElement();
          String className = (String)actionsDef.get(actionId);
          String actionName = getLocalizedString(actionId + ".name", actionId);
          String actionTooltip =
            getLocalizedString(actionId + ".tooltip", actionName);

          Class actionClass = Class.forName(className);
          BaseAction action = (BaseAction)actionClass.newInstance();
          action.setIDE(ide);
          action.putValue(Action.NAME, actionName);
          action.putValue(Action.SHORT_DESCRIPTION, actionTooltip);
          actions.put(actionId, action);
          if (actionClass == CloseAction.class)
            tabbedPane.setCloseAction(action);
        }
        catch (Exception ex)
        {
          MatrixIDE.log(ex);
        }
      }
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  public void setupToolBar()
  {
    try
    {
      toolBar.removeAll();
      InputStream is = getClass().getResourceAsStream(
        "/org/santfeliu/matrix/ide/resources/toolbar.properties");
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      String actionId = reader.readLine();
      while (actionId != null)
      {
        actionId = actionId.trim();
        if (actionId.length() > 0)
        {
          if (actionId.startsWith("-"))
          {
            toolBar.addSeparator(new Dimension(8, 20));
          }
          else
          {
            toolBar.add(new WrapperAction(
              (Action)actions.get(actionId), false, true));
          }
        }
        actionId = reader.readLine();
      }
      updateActions();
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  public void setupMenu(JMenuBar menuBar)
  {
    try
    {
      InputStream is = getClass().getResourceAsStream(
        "/org/santfeliu/matrix/ide/resources/menu.properties");
      MenuLoader menuLoader = new MenuLoader(actions, resources);
      menuLoader.loadMenuBar(menuBar, is);
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  public void loadConnections()
  {
    try
    {
      boolean end = false;
      int i = 0;
      connections.clear();
      while (!end)
      {
        String connString = Options.get("conn_" + i);
        if (connString != null)
        {
          String tokens[] = connString.split(";");
          ConnectionParameters connectionParameters =
            new ConnectionParameters();
          connectionParameters.setName(tokens[0].trim());
          connectionParameters.setURL(tokens[1].trim());
          String username = tokens[2].trim();
          String password = tokens[3].trim();
          if ("null".equals(username)) username = null;
          if ("null".equals(password)) password = null;
          connectionParameters.setUsername(username);
          connectionParameters.setPassword(password);
          connections.add(connectionParameters);
          i++;
        }
        else end = true;
      }
      connPanel.setConnections(connections);
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  public void saveConnections()
  {
    Enumeration enu = Options.listKeys();
    while (enu.hasMoreElements())
    {
      String key = String.valueOf(enu.nextElement());
      if (key.startsWith("conn_")) Options.set(key, null);
    }
    for (int i = 0; i < connections.size(); i++)
    {
      ConnectionParameters connectionParameters =
       (ConnectionParameters)connections.get(i);
      String username = connectionParameters.getUsername();
      String password = connectionParameters.getPassword();
      if (username == null || username.trim().length() == 0) username = "null";
      if (password == null || password.trim().length() == 0) password = "null";

      Options.set("conn_" + i,
        connectionParameters.getName() + ";" +
        connectionParameters.getURL() + ";" +
        username + ";" + password);
    }
  }

  public void setupPalette()
  {
    try
    {
      InputStream is = getClass().getResourceAsStream(
        "/org/santfeliu/matrix/ide/resources/palette.xml");
      try
      {
        palette.read(is);
      }
      finally
      {
        is.close();
      }
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  public List getConnections()
  {
    return connections;
  }

  public ArrayList<DocumentType> getDocumentTypes()
  {
    return documentTypes;
  }

  public DocumentType getDocumentType(String extension)
  {
    DocumentType documentType = null;
    boolean found = false;
    Iterator iter = documentTypes.iterator();
    while (iter.hasNext() && !found)
    {
      documentType = (DocumentType)iter.next();
      if (documentType.getExtension().equalsIgnoreCase(extension))
      {
        found = true;
      }
    }
    return found ? documentType : null;
  }

  public String getLocalizedString(String key)
  {
    return getLocalizedString(key, null);
  }

  public String getLocalizedString(String key, String defaultValue)
  {
    try
    {
      return resources.getString(key);
    }
    catch (MissingResourceException ex)
    {
      return defaultValue;
    }
  }

  public DocumentPanel createPanel(DocumentType documentType)
    throws Exception
  {
    String className = documentType.getDocumentPanelClassName();
    Class cls = Class.forName(className);
    DocumentPanel panel = (DocumentPanel)cls.newInstance();
    panel.setDocumentType(documentType);
    panel.setMainPanel(this);
    return panel;
  }

  public void addPanel(DocumentPanel documentPanel)
  {
    ImageIcon icon = null;
    documentPanel.setMainPanel(this);
    DocumentType documentType = documentPanel.getDocumentType();
    if (documentType != null)
    {
      icon = documentType.getIcon();
    }
    tabbedPane.addTab(documentPanel.getDisplayName(), icon, documentPanel);
    tabbedPane.setSelectedComponent(documentPanel);
    documentPanel.updateDisplayName();
    if (tabbedPane.getTabCount() == 1) // first tab
    {
      splitPane.setVisible(true);
      revalidate();
    }
  }

  public void closePanel(DocumentPanel documentPanel)
  {
    if (documentPanel.isModified())
    {
      int option = JOptionPane.showConfirmDialog(this,
        "Discard changes in \"" + documentPanel.getDisplayName() + "\"?",
        "Close document",
        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (option == JOptionPane.YES_OPTION)
      {
        documentPanel.close();
        tabbedPane.remove(documentPanel);
      }
    }
    else
    {
      documentPanel.close();
      tabbedPane.remove(documentPanel);
    }
    if (tabbedPane.getTabCount() == 0) // no tabs
    {
      splitPane.setVisible(false);
    }
    updateActions();
  }

  public void closeActivePanel()
  {
    DocumentPanel documentPanel =
      (DocumentPanel)tabbedPane.getSelectedComponent();
    closePanel(documentPanel);
  }

  public List<DocumentPanel> getPanels()
  {
    return getPanels(panel -> true);
  }

  public List<DocumentPanel> getPanels(Predicate<DocumentPanel> predicate)
  {
    List<DocumentPanel> panels = new ArrayList<>();
    int count = tabbedPane.getComponentCount();
    for (int i = 0; i < count; i++)
    {
      Component component = tabbedPane.getComponent(i);
      if (component instanceof DocumentPanel)
      {
        DocumentPanel panel = (DocumentPanel)component;
        if (predicate.test(panel))
        {
          panels.add(panel);
        }
      }
    }
    return panels;
  }

  public void openDocument(File file) throws Exception
  {
    String filename = file.getName();
    int index = filename.lastIndexOf(".");
    if (index != -1)
    {
      String name = filename.substring(0, index);
      String extension = filename.substring(index + 1);
      DocumentType documentType =
        ide.getMainPanel().getDocumentType(extension);
      if (documentType != null)
      {
        DocumentPanel panel =
          ide.getMainPanel().createPanel(documentType);
        try (FileInputStream is = new FileInputStream(file))
        {
          panel.setDisplayName(name);
          panel.setDirectory(file.getParentFile());
          panel.open(is); // read from file
          panel.setConnectionUrl(null);
          ide.getMainPanel().addPanel(panel); // add panel to framework
          panel.setModified(false);
        }
      }
      else if (Desktop.isDesktopSupported())
      {
        Desktop.getDesktop().open(file);
      }
      else
      {
        throw new Exception("Unsupported file type");
      }
    }
    else throw new Exception("Unknow file type");
  }

  public void openDocumentFromDM(DocumentType documentType,
    String docId, String language, int version) throws Exception
  {
    DocumentManagerClient client = getDocumentManagerClient();
    Document document = client.loadDocument(docId, version);

    String displayName = null;
    DocumentPanel panel = createPanel(documentType);
    for (org.matrix.dic.Property property : document.getProperty())
    {
      if (property.getName().equals(documentType.getPropertyName()))
      {
        displayName = property.getValue().get(0);
        break;
      }
    }
    String description = document.getTitle();

    int index = description.indexOf(":");
    if (index != -1)
    {
      description = description.substring(index + 1);
    }
    panel.setDocId(docId);
    panel.setVersion(version);
    panel.setDisplayName(displayName);
    panel.setDescription(description);
    panel.setLanguage(language);

    DataHandler dh = document.getContent().getData();
    InputStream is = dh.getInputStream();
    panel.open(is); // read from file
    panel.setConnectionUrl(getConnectionPanel().
      getSelectedConnection().getURL());
    addPanel(panel); // add panel to framework
    panel.setModified(false);
  }

  public DocumentPanel getActivePanel()
  {
    return (DocumentPanel)tabbedPane.getSelectedComponent();
  }

  public ConnectionPanel getConnectionPanel()
  {
    return connPanel;
  }

  public DocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    ConnectionParameters connectionParameters =
      connPanel.getSelectedConnection();
    return getDocumentManagerClient(connectionParameters);
  }

  public DocumentManagerClient getDocumentManagerClient(
    ConnectionParameters connectionParameters) throws Exception
  {
    if (connectionParameters == null) return null;

    URL wsDirectoryURL = new URL(connectionParameters.getURL());
    String userId = connectionParameters.getUsername();
    String password = connectionParameters.getPassword();
    return new DocumentManagerClient(wsDirectoryURL, userId, password);
  }

  public SecurityManagerPort getSecurityManagerPort() throws Exception
  {
    ConnectionParameters connectionParameters =
      connPanel.getSelectedConnection();

    if (connectionParameters == null) return null;

    URL wsDirectoryURL = new URL(connectionParameters.getURL());
    WSDirectory directory = WSDirectory.getInstance(wsDirectoryURL);

    WSEndpoint endpoint = directory.getEndpoint(SecurityManagerService.class);
    return endpoint.getPort(SecurityManagerPort.class,
      connectionParameters.getUsername(),
      connectionParameters.getPassword());
  }

  public PropertySheetPanel getPropertySheetPanel()
  {
    return sheet;
  }

  public Palette getPalette()
  {
    return palette;
  }

  public void setRightPanelVisible(boolean visible)
  {
    if (visible)
    {
      if (!splitPane.getRightComponent().isVisible())
      {
        splitPane.getRightComponent().setVisible(true);
        if (dividerLocation > splitPane.getLeftComponent().getWidth())
        {
          splitPane.setDividerLocation(0.5);
        }
        else
        {
          splitPane.setDividerLocation(dividerLocation);
        }
      }
    }
    else // hide
    {
      splitPane.getRightComponent().setVisible(false);
    }
  }

  public void setIDE(MatrixIDE builder)
  {
    this.ide = builder;
    zoomPanel.setIDE(builder);
  }

  public MatrixIDE getIDE()
  {
    return ide;
  }

  public void showStatus(DocumentPanel panel)
  {
    if (panel.getConnectionUrl() != null)
    {
      statusBar.setText("Location: " + panel.getConnectionUrl() +
        " - docId / version: " + panel.getDocId() + " / " + panel.getVersion());
    }
    else if (panel.getDirectory() != null)
    {
      String extension = panel.getDocumentType().getExtension();
     String separator = System.getProperty("file.separator");
      statusBar.setText("Location: " + panel.getDirectory() + separator +
        panel.getDisplayName() + "." + extension);
    }
    else
    {
      statusBar.setText("Not yet saved.");
    }
  }

  public boolean confirmExit()
  {
    boolean exit = true;
    int modifiedDocuments = 0;
    int count = tabbedPane.getComponentCount();
    for (int i = 0; i < count; i++)
    {
      Component component = tabbedPane.getComponent(i);
      if (component instanceof DocumentPanel)
      {
        DocumentPanel panel = (DocumentPanel)component;
        if (panel.isModified()) modifiedDocuments++;
      }
    }
    if (modifiedDocuments > 0)
    {
      String message = null;
      if (modifiedDocuments == 1)
      {
        message = "1 document was not saved.";
      }
      else
      {
        message = modifiedDocuments + " documents were not saved.";
      }
      int option = JOptionPane.showConfirmDialog(this,
        message + " Exit without saving?",
        "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      exit = (option == JOptionPane.YES_OPTION);
    }
    return exit;
  }

  /* internal methods */

  private void initComponents() throws Exception
  {
    setLayout(mainLayout);

    Color borderColor = UIManager.getColor("Panel.background").darker();
    northLayout.setAlignment(FlowLayout.LEFT);
    northLayout.setVgap(1);
    northPanel.setLayout(northLayout);
    northPanel.setBorder(
      BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));

    add(northPanel, BorderLayout.NORTH);
    add(splitPane, BorderLayout.CENTER);
    add(statusBar, BorderLayout.SOUTH);

    statusBar.setBorder(new EmptyBorder(2, 2, 2, 2));

    northPanel.add(toolBar);
    northPanel.add(zoomPanel);
    northPanel.add(connPanel);

    splitPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
    splitPane.add(tabbedPane, JSplitPane.LEFT);
    splitPane.add(rightSplitPane, JSplitPane.RIGHT);
    splitPane.setContinuousLayout(true);
    splitPane.setDividerLocation(540);
    splitPane.setResizeWeight(1.0);
    splitPane.setDividerSize(2);
    splitPane.setVisible(false);
    splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
      (PropertyChangeEvent pce) ->
      {
        if (splitPane.getRightComponent().isVisible())
        {
          dividerLocation = splitPane.getDividerLocation();
        }
      });

    rightSplitPane.setBorder(BorderFactory.createEmptyBorder());
    rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    rightSplitPane.add(palette, JSplitPane.TOP);
    rightSplitPane.add(sheet, JSplitPane.BOTTOM);
    rightSplitPane.setDividerLocation(200);
    rightSplitPane.setContinuousLayout(true);

    toolBar.setBorderPainted(false);

    PropertyChangeListener listener = (PropertyChangeEvent evt) ->
    {
      DocumentPanel panel = getActivePanel();
      try
      {
        if (editObject != null)
        {
          Property prop = (Property)evt.getSource();
          prop.writeToObject(editObject);
          panel.objectPropertyChanged(editObject, prop);

          if (!loadingObjectProperties)
          {
            panel.setModified(true);
            UndoManager undoManager = panel.getUndoManager();
            if (undoManager != null)
            {
              BeanUndoableEdit edit = new BeanUndoableEdit(editObject);
              edit.propertyChange(prop.getName(),
                evt.getOldValue(), evt.getNewValue());
              undoManager.addEdit(edit);
            }
          }
        }
      }
      catch (Exception ex)
      {
      }
      updateActions();
      panel.repaint();
    };

    sheet.addPropertySheetChangeListener(listener);
    sheet.setSorting(true);
    sheet.setMode(PropertySheetPanel.VIEW_AS_CATEGORIES);
    sheet.setDescriptionVisible(true);
    sheet.setBorder(BorderFactory.createEmptyBorder());
    sheet.getTable().setBorder(BorderFactory.createLineBorder(borderColor, 1));
    sheet.getTable().addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyReleased(KeyEvent event)
      {
        if (event.getKeyCode() == KeyEvent.VK_DELETE)
        {
          try
          {
            int index = sheet.getTable().getSelectedRow();

            PropertySheetTableModel.Item item =
              sheet.getTable().getSheetModel().getPropertySheetElement(index);
            Class cls = item.getProperty().getType();
            if (cls != int.class && cls != long.class &&
              cls != boolean.class && cls != byte.class && cls != char.class &&
              cls != float.class && cls != double.class)
            {
              // set property to null
              sheet.getTable().getSheetModel().setValueAt(null, index, 1);
            }
          }
          catch (Exception ex)
          {
          }
        }
      }
    });

    tabbedPane.setShowCloseButton(true);
    tabbedPane.addChangeListener((ChangeEvent e) ->
    {
      DocumentPanel panel = getActivePanel();
      if (panel != null)
      {
        panel.activate();
        showStatus(panel);
      }
      updateActions();
      updateZoomPanel();
    });
  }
}
