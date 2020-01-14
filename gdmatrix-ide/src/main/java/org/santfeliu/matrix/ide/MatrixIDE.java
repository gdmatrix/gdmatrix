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
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author unknown
 */
public class MatrixIDE extends JFrame
{
  private static final Logger logger = Logger.getLogger("matrix-ide");
  private MainPanel mainPanel = new MainPanel();  

  public MatrixIDE()
  {
    try
    {
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
    catch (Exception ex)
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
    return new File(
      System.getProperty("user.home") + "/matrix_ide.properties");
  }

  private void initComponents() throws Exception
  {  
    setTitle("Matrix IDE");
    setSize(new Dimension(760, 600));
    
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
      catch (Exception ex)
      {
        MatrixIDE.log(ex);
      }
    }

    // center screen
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height)
    {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width)
    {
      frameSize.width = screenSize.width;
    }
    setLocation((screenSize.width - frameSize.width) / 2, 
                (screenSize.height - frameSize.height) / 2);
  }

  public MainPanel getMainPanel()
  {
    return mainPanel;
  }
 
  public void exit()
  {
    try
    {
      System.out.println("Quit application.");
      // save window location
      Rectangle rect = getBounds();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      Options.set("x", String.valueOf(Math.max(rect.x, 0)));
      Options.set("y", String.valueOf(Math.max(rect.y, 0)));
      Options.set("width",
        String.valueOf(Math.min(rect.width, screenSize.width)));
      Options.set("height",
        String.valueOf(Math.min(rect.height, screenSize.height)));

      // save window state
      Options.set("dividers", mainPanel.getDividersLocation());

      // save connections in environment
      mainPanel.saveConnections();

      // save environment
      MatrixIDE.saveOptions();
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
    System.exit(0);
  }

  public static void log(Exception ex)
  {
    logger.log(Level.SEVERE, ex.toString());
  }

  public static void log(Level level, String message)
  {
    logger.log(level, message);
  }

  public static void main(String[] args)
  {
    // load environment variables
    MatrixIDE.loadOptions();

    // start frame
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        try
        {
          // setup LookAndFeel
          String lafClassName = Options.get("lafClassName");
          if (lafClassName == null)
            lafClassName = UIManager.getSystemLookAndFeelClassName();
          UIManager.setLookAndFeel(lafClassName);

          // setup locale
          String language = Options.get("language");
          if (language != null)
          {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
          }

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