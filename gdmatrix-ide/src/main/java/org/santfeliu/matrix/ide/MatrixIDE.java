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

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author realor
 */
public class MatrixIDE extends JFrame
{
  private static final Logger LOGGER = Logger.getLogger("MatrixIDE");
  private MainPanel mainPanel = new MainPanel();

  public MatrixIDE()
  {
    try
    {
      initLogger();
      log(Level.INFO, "Start MatrixIDE");
      initComponents();
    }
    catch (Exception ex)
    {
      log(ex);
    }
  }

  public static void loadOptions()
  {
    try
    {
      File optionsFile = getOptionsFile();
      if (optionsFile.exists())
      {
        Options.load(new FileInputStream(optionsFile));
      }
    }
    catch (IOException ex)
    {
      log(ex);
    }
  }

  public static void saveOptions()
  {
    try
    {
      File optionsFile = getOptionsFile();
      Options.save(new FileOutputStream(optionsFile));
    }
    catch (Exception ex)
    {
      log(ex);
    }
  }

  private static File getOptionsFile()
  {
    return new File(getBaseDir(), "ide.properties");
  }

  private static File getBaseDir()
  {
    String userHome = System.getProperty("user.home");
    File baseDir = new File(userHome + "/.matrix");
    if (!baseDir.exists())
    {
      baseDir.mkdirs();
    }
    return baseDir;
  }

  private void initLogger()
  {
    try
    {
      File baseDir = getBaseDir();
      File logDir = new File(baseDir, "logs");
      if (!logDir.exists())
        logDir.mkdir();
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
      String today = df.format(new Date());
      String logFile =
        logDir.getAbsolutePath() + "/ide-" + today + ".log";
      Handler handler = new FileHandler(logFile, true);
      handler.setFormatter(new SimpleFormatter());
      Logger logger = Logger.getLogger("");
      logger.addHandler(handler);
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  private void initComponents() throws Exception
  {
    setTitle("Matrix IDE");

    setIconImages(loadIcons(
      "icon_blue_16.png", "icon_blue_32.png", "icon_blue_64.png",
      "icon_blue_128.png", "icon_blue_256.png"));

    mainPanel.setIDE(this);
    mainPanel.setupLocale(Locale.getDefault());
    mainPanel.setupDocumentTypes();
    mainPanel.setupActions();
    mainPanel.loadConnections();
    mainPanel.setupToolBar();
    mainPanel.setupPalette();

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    mainPanel.setupMenu(menuBar);

    getContentPane().add(mainPanel);

    // adjust window location
    String xString = Options.get("x");
    String yString = Options.get("y");
    String widthString = Options.get("width");
    String heightString = Options.get("height");
    String dividers = Options.get("dividers");

    if (xString != null && yString != null &&
        widthString != null && heightString != null && dividers != null)
    {
      try
      {
        int x = Integer.parseInt(xString);
        int y = Integer.parseInt(yString);
        int width = Integer.parseInt(widthString);
        int height = Integer.parseInt(heightString);
        setBounds(x, y, width, height);
        mainPanel.setDividersLocation(dividers);
        return;
      }
      catch (NumberFormatException ex)
      {
        MatrixIDE.log(ex);
      }
    }

    // first time, full size window
    Rectangle maxSize = GraphicsEnvironment.getLocalGraphicsEnvironment().
      getMaximumWindowBounds();
    Dimension size = new Dimension(maxSize.width, maxSize.height);
    setSize(size);
    mainPanel.adjustDividersLocation(size);
    setLocationRelativeTo(null);
  }

  private List<Image> loadIcons(String... names)
  {
    List<Image> icons = new ArrayList<>();
    for (String name : names)
    {
      try
      {
        String resource = "resources/images/gdmatrix/" + name;
        icons.add(ImageIO.read(getClass().getResourceAsStream(resource)));
      }
      catch (Exception ex)
      {
        // ignore
      }
    }
    return icons;
  }

  public MainPanel getMainPanel()
  {
    return mainPanel;
  }

  public void exit()
  {
    try
    {
      // save window location
      Rectangle rect = getBounds();
      Rectangle maxSize = GraphicsEnvironment.getLocalGraphicsEnvironment().
        getMaximumWindowBounds();
      Dimension size = new Dimension(maxSize.width, maxSize.height);

      Options.set("x", String.valueOf(Math.max(rect.x, 0)));
      Options.set("y", String.valueOf(Math.max(rect.y, 0)));
      Options.set("width",
        String.valueOf(Math.min(rect.width, size.width)));
      Options.set("height",
        String.valueOf(Math.min(rect.height, size.height)));

      // save window state
      Options.set("dividers", mainPanel.getDividersLocation());

      // save connections in environment
      mainPanel.saveConnections();

      // save environment
      saveOptions();
      log(Level.INFO, "Exit MatrixIDE");
    }
    catch (Exception ex)
    {
      log(ex);
    }
    System.exit(0);
  }

  public static void log(Exception ex)
  {
    LOGGER.log(Level.SEVERE, ex.toString(), ex);
  }

  public static void log(Level level, String message)
  {
    LOGGER.log(level, message);
  }

  public static void main(String[] args)
  {
    MatrixIDE.loadOptions();

    // setup LookAndFeel
    try
    {
      UIManager.put("Popup.dropShadowPainted", false);
      UIManager.installLookAndFeel(
        "FlatLaf light", "com.formdev.flatlaf.FlatLightLaf");
      UIManager.installLookAndFeel(
        "FlatLaf dark", "com.formdev.flatlaf.FlatDarkLaf");

      String lafClassName = Options.get("lafClassName");
      if (lafClassName == null)
        lafClassName = "com.formdev.flatlaf.FlatLightLaf";
      UIManager.setLookAndFeel(lafClassName);
    }
    catch (Exception ex)
    {
    }

    // setup locale
    String language = Options.get("language");
    if (language != null)
    {
      Locale locale = new Locale(language);
      Locale.setDefault(locale);
    }

    // start frame
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          // create IDE instance
          final MatrixIDE ide = new MatrixIDE();
          ide.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
          ide.addWindowListener(new WindowAdapter()
          {
            @Override
            public void windowClosing(WindowEvent event)
            {
              if (ide.getMainPanel().confirmExit())
              {
                ide.exit();
              }
            }
          });

          ide.setVisible(true);
        }
        catch (Exception ex)
        {
          MatrixIDE.log(ex);
        }
      }
    });
  }
}