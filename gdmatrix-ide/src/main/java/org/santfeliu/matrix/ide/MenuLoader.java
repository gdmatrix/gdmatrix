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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.santfeliu.matrix.ide.action.WrapperAction;

/**
 *
 * @author realor
 */
public class MenuLoader 
{
  protected HashMap actions;
  protected ResourceBundle resources;
  protected String line;
  
  public MenuLoader(HashMap actions, ResourceBundle resources)
  {
    this.actions = actions;
    this.resources = resources;
  }
 
  public JMenuBar loadMenuBar(JMenuBar menuBar, InputStream is)
    throws IOException
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    try
    {
      line = reader.readLine();
      while (line != null)
      {
        line = line.trim();
        if (line.length() > 0)
        {
          if (line.charAt(0) == '#')
          {
            // comment
          }
          else if (line.startsWith("MENU"))
          {
            JMenu menu = loadMenu(reader);
            menuBar.add(menu);
          }
          else
          {
            throw new IOException("invalid menu file");
          }
        }
        line = reader.readLine();
      }
    }
    finally
    {
      reader.close();
    }
    return menuBar;
  }

  public JMenu loadMenu(InputStream is) throws IOException
  {
    JMenu menu = null;

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    try
    {
      line = reader.readLine();
      while (line != null && menu == null)
      {
        line = line.trim();
        if (line.length() > 0)
        {
          if (line.charAt(0) == '#')
          {
            // comment
          }
          else if (line.startsWith("MENU"))
          {
            menu = loadMenu(reader);
          }
          else
          {
            throw new IOException("invalid menu file");
          }
        }
        line = reader.readLine();
      }
    }
    finally
    {
      reader.close();
    }
    return menu;
  }
  
  private JMenu loadMenu(BufferedReader reader)
  throws IOException
  {
    boolean end = false;
    JMenu menu = new JMenu();
    StringTokenizer tokenizer = new StringTokenizer(line);
    if (tokenizer.countTokens() > 1)
    {
      tokenizer.nextToken(); // skip keyword
      String name = tokenizer.nextToken(); // read menu name
      String localeName;
      try
      {
        localeName = resources.getString(name);
      }
      catch (Exception e)
      {
        localeName = name;
      }
      menu.setText(localeName);
    }
    line = reader.readLine();
    while (line != null && !end)
    {
      line = line.trim();
      if (line.length() > 0)
      {
        if (line.charAt(0) != '#')
        {
          tokenizer = new StringTokenizer(line);
          String type = tokenizer.nextToken();

          if (type.equals("MENUITEM"))
          {
            if (tokenizer.countTokens() > 0)
            {
              try
              {
                String actionId = tokenizer.nextToken();
                JMenuItem item = new JMenuItem(
                  new WrapperAction((Action)actions.get(actionId), true, false));

                if (tokenizer.hasMoreElements())
                {
                  String accelerator = tokenizer.nextToken();
                  accelerator = accelerator.replace("+", " ");
                  KeyStroke keyStroke = KeyStroke.getKeyStroke(accelerator);
                  item.setAccelerator(keyStroke);
                }
                menu.add(item);
              }
              catch (Exception ex)
              {
                MatrixIDE.log(ex);
              }
            }
          }
          else if (type.equals("SEPARATOR"))
          {
            menu.addSeparator();
          }
          else if (type.equals("MENU"))
          {
            JMenu childMenu = loadMenu(reader);
            menu.add(childMenu);
          }
          else if (type.equals("ENDMENU"))
          {
            end = true;
          }
          else throw new IOException("invalid menu file");
        }
      }
      if (!end) line = reader.readLine();
    }
    return menu;
  }
}