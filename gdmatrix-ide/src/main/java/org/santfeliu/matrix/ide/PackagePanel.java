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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

/**
 *
 * @author realor
 */
public class PackagePanel extends DocumentPanel
{
  private JToolBar toolBar = new JToolBar();
  private JButton installButton = new JButton();
  private JButton uninstallButton = new JButton();
  private JButton addButton = new JButton();
  private JButton removeButton = new JButton();
  private JScrollPane scrollPane = new JScrollPane();

  private JTable table = new JTable();
  private BorderLayout layout = new BorderLayout();

  private Path unpackDir;
  private ChangeMonitor changeMonitor;

  public PackagePanel()
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

  public File getUnpackDir()
  {
    return unpackDir.toFile();
  }

  @Override
  public void create() throws Exception
  {
    unpackDir = Files.createTempDirectory("unpack-");

    try (InputStream is = getClass().getResourceAsStream(
      "/org/santfeliu/matrix/ide/resources/install.js"))
    {
      Files.copy(is, Paths.get(unpackDir.toString(), "install.js"));
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
    updateTable();
    startMonitor();
  }

  @Override
  public void close()
  {
    stopMonitor();
  }

  @Override
  public void open(InputStream is) throws Exception
  {
    unpackDir = Files.createTempDirectory("unpack-");

    try (ZipInputStream zis = new ZipInputStream(is))
    {
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null)
      {
        String name = zipEntry.getName();
        Path path = Paths.get(unpackDir.toString(), name);

        if (zipEntry.isDirectory())
        {
          Files.createDirectories(path);
        }
        else
        {
          Path parentPath = path.getParent();
          if (!Files.exists(parentPath))
          {
            Files.createDirectories(parentPath);
          }
          Files.copy(zis, path, StandardCopyOption.REPLACE_EXISTING);
        }
        zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();
    }

    updateTable();

    startMonitor();
  }

  @Override
  public void save(OutputStream os) throws Exception
  {
    String unpackDirName = unpackDir.toString();

    try (ZipOutputStream zos = new ZipOutputStream(os))
    {
      exploreFiles(unpackDir, subPath ->
      {
        try
        {
          String fileName = subPath.toString();
          Path fullPath = Paths.get(unpackDirName, fileName);

          ZipEntry ze = new ZipEntry(fileName);
          zos.putNextEntry(ze);
          Files.copy(fullPath, zos);
          zos.closeEntry();
        }
        catch (Exception ex)
        {
          MatrixIDE.log(ex);
        }
      });
    }
  }

  public void updateTable()
  {
    int selectedRow = table.getSelectedRow();
    try
    {
      String unpackDirName = unpackDir.toString();
      DefaultTableModel tableModel = new DefaultTableModel()
      {
        @Override
        public boolean isCellEditable(int row, int column)
        {
          return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
          switch (columnIndex)
          {
            case 0: return Path.class;
            case 1: return Long.class;
            case 2: return Date.class;
          }
          return String.class;
        }
      };
      tableModel.addColumn("Path");
      tableModel.addColumn("Size");
      tableModel.addColumn("Last update");

      exploreFiles(unpackDir, subPath ->
      {
        try
        {
          Path fullPath = Paths.get(unpackDirName, subPath.toString());
          long size = Files.size(fullPath);
          FileTime time = Files.getLastModifiedTime(fullPath);

          tableModel.addRow(new Object[]
          {
            subPath,
            size,
            new Date(time.toMillis())
          });
        }
        catch (IOException ex)
        {
          MatrixIDE.log(ex);
        }
      });

      table.setModel(tableModel);
      if (selectedRow >= 0 && selectedRow < tableModel.getRowCount())
      {
        table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
      }

      TableColumn column = table.getColumnModel().getColumn(0);
      column.setPreferredWidth(1000);
      column.setMinWidth(100);

      column = table.getColumnModel().getColumn(1);
      column.setPreferredWidth(100);
      column.setMinWidth(100);

      column = table.getColumnModel().getColumn(2);
      column.setPreferredWidth(200);
      column.setMinWidth(100);
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  protected void install()
  {
    File installFile = Paths.get(unpackDir.toString(), "install.js").toFile();
    executeScript(installFile, "Install package " + getDisplayName(),
      "if (install) { install(); } else " +
      "{ output.println('Install not implemented.'); }");
  }

  protected void uninstall()
  {
    File installFile = Paths.get(unpackDir.toString(), "install.js").toFile();
    executeScript(installFile, "Uninstall  package " + getDisplayName(),
      "if (uninstall) { uninstall(); } else " +
      "{ output.println('Uninstall not implemented.'); }");
  }

  protected void executeScript(File script, String title, String functionCall)
  {
    MainPanel mainPanel = getMainPanel();
    MatrixIDE ide = mainPanel.getIDE();
    File installFile = Paths.get(unpackDir.toString(), "install.js").toFile();
    if (installFile.exists())
    {
      ConnectionParameters connectionParameters =
        mainPanel.getConnectionPanel().getSelectedConnection();
      if (connectionParameters != null)
      {
        PackageRunnerDialog dialog = new PackageRunnerDialog(ide);
        dialog.setTitle(title);
        dialog.setScript(script);
        dialog.setFunctionCall(functionCall);
        dialog.setConnectionParameters(connectionParameters);
        dialog.setLocationRelativeTo(ide);
        dialog.setVisible(true);
      }
      else
      {
        JOptionPane.showMessageDialog(this, "No server connection defined!");
      }
    }
    else
    {
      JOptionPane.showMessageDialog(this,
        "This package does not provide an install/uninstall script!");
    }
  }

  protected void openDocument()
  {
    try
    {
      getMainPanel().openDocument(getSelectedDocument());
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(
        getMainPanel(),
        ex.getMessage(),
        "Open file from package", JOptionPane.ERROR_MESSAGE);
    }
  }

  protected void addDocumentFromDisk()
  {
    try
    {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

      File dir = Options.getLastDirectory();
      chooser.setCurrentDirectory(dir);

      int result = chooser.showOpenDialog(getMainPanel());
      if (result == JFileChooser.APPROVE_OPTION)
      {
        File file = chooser.getSelectedFile();
        dir = file.getParentFile();
        Options.setLastDirectory(dir);

        try (FileInputStream is = new FileInputStream(file))
        {
          Path path = Paths.get(unpackDir.toString(), file.getName());
          Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(
        getMainPanel(),
        ex.getMessage(),
        "Open file", JOptionPane.ERROR_MESSAGE);
    }
  }

  protected void removeDocument()
  {
    try
    {
      File file = getSelectedDocument();
      file.delete();
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(
        getMainPanel(),
        ex.getMessage(),
        "Remove file from package", JOptionPane.ERROR_MESSAGE);
    }
  }

  protected File getSelectedDocument()
  {
    int row = table.getSelectedRow();
    Path subPath = (Path)table.getModel().getValueAt(row, 0);
    Path fullPath = Paths.get(unpackDir.toString(), subPath.toString());
    return fullPath.toFile();
  }

  protected void exploreFiles(Path dir, Consumer<Path> consumer)
  {
    int nameCount = unpackDir.getNameCount();

    try
    {
      Files.list(dir).forEach(path ->
      {
        if (Files.isDirectory(path))
        {
          exploreFiles(path, consumer);
        }
        else
        {
          Path subPath = path.subpath(nameCount, path.getNameCount());
          consumer.accept(subPath);
        }
      });
    }
    catch (IOException ex)
    {
      MatrixIDE.log(ex);
    }
  }

  protected void startMonitor()
  {
    if (changeMonitor == null)
    {
      changeMonitor = new ChangeMonitor(unpackDir);
      changeMonitor.start();
    }
  }

  protected void stopMonitor()
  {
    if (changeMonitor != null)
    {
      changeMonitor.interrupt();
      changeMonitor = null;
    }
  }

  public class DateCellRenderer extends DefaultTableCellRenderer
  {
    private final SimpleDateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column)
    {
      super.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);

      if (value instanceof Date)
      {
        setText(dateFormat.format((Date)value));
      }
      else setText("");

      return this;
    }
  }

  public class PathCellRenderer extends DefaultTableCellRenderer
  {
    Map<String, DocumentType> documentTypeMap;
    ImageIcon defaultIcon;

    public PathCellRenderer()
    {
      defaultIcon = new ImageIcon(getClass().getResource(
        "/org/santfeliu/matrix/ide/resources/images/file.gif"));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column)
    {
      Path path = (Path)value;

      super.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);

      if (documentTypeMap == null)
      {
        documentTypeMap = new HashMap<>();

        ArrayList<DocumentType> documentTypes =
          getMainPanel().getDocumentTypes();

        documentTypes.forEach(documentType ->
          documentTypeMap.put(documentType.getExtension(), documentType));
      }

      String fileName = path.getFileName().toString();
      int index = fileName.lastIndexOf(".");
      String extension = index == -1 ? "" : fileName.substring(index + 1);

      DocumentType documentType = documentTypeMap.get(extension);
      if (documentType != null)
      {
        setIcon(documentType.getIcon());
      }
      else
      {
        setIcon(defaultIcon);
      }

      return this;
    }
  }

  private void initComponents()
  {
    setLayout(layout);
    add(toolBar, BorderLayout.NORTH);
    toolBar.setFloatable(false);

    add(scrollPane, BorderLayout.CENTER);

    scrollPane.getViewport().add(table, null);

    installButton.setText("Install");
    installButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/install.gif")));
    installButton.addActionListener(event -> install());
    toolBar.add(installButton);

    uninstallButton.setText("Uninstall");
    uninstallButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/uninstall.gif")));
    uninstallButton.addActionListener(event -> uninstall());
    toolBar.add(uninstallButton);

    toolBar.addSeparator();

    addButton.setText("Add");
    addButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/add.gif")));
    addButton.addActionListener(event -> addDocumentFromDisk());
    toolBar.add(addButton);

    removeButton.setText("Remove");
    removeButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/images/delete.gif")));
    removeButton.addActionListener(event -> removeDocument());
    toolBar.add(removeButton);

    table.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() > 1)
        {
          openDocument();
        }
      }
    });

    table.getTableHeader().setReorderingAllowed(false);
    table.setSelectionMode(SINGLE_SELECTION);

    table.setDefaultRenderer(Path.class, new PathCellRenderer());
    table.setDefaultRenderer(Date.class, new DateCellRenderer());

    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "open");
    table.getActionMap().put("open", new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        openDocument();
      }
    });
  }

  class ChangeMonitor extends Thread
  {
    final Path dir;

    ChangeMonitor(Path dir)
    {
      this.dir = dir;
    }

    @Override
    public void run()
    {
      try
      {
        MatrixIDE.log(Level.INFO, "Start change monitor for " + dir);

        WatchService watcher = FileSystems.getDefault().newWatchService();

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>()
        {
          @Override
          public FileVisitResult preVisitDirectory(
            Path subDir, BasicFileAttributes attrs) throws IOException
          {
            subDir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            return FileVisitResult.CONTINUE;
          }
        });

        while (!Thread.currentThread().isInterrupted())
        {
          WatchKey key = watcher.take();
          key.pollEvents();

          SwingUtilities.invokeLater(() -> updateTable());
          setModified(true);

          boolean valid = key.reset();
          if (!valid)
          {
            break;
          }
        }
      }
      catch (InterruptedException ex)
      {
        // ignore
      }
      catch (Exception ex)
      {
        MatrixIDE.log(ex);
      }
      finally
      {
        MatrixIDE.log(Level.INFO, "End change monitor for " + dir);
      }
    }
  }
}
