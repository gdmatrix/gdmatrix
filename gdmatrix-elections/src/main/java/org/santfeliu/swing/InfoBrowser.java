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
package org.santfeliu.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;


import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


/**
 *
 * @author unknown
 */
public class InfoBrowser extends JDialog
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel infoBrowserLabel = new JLabel();
  private JScrollPane scrollPane = new JScrollPane();
  private JTextPane textPane = new JTextPane();

  public InfoBrowser()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setSize(new Dimension(435, 423));
    this.setLayout(borderLayout1);
    this.add(infoBrowserLabel, BorderLayout.NORTH);
    scrollPane.getViewport().add(textPane, null);
    this.add(scrollPane, BorderLayout.CENTER);
    textPane.setEditable(false);
    textPane.addHyperlinkListener(new HyperlinkListener()
    {
      public void hyperlinkUpdate(HyperlinkEvent e)
      {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
          URL url = e.getURL();
          if (url != null)
          {
            if (url.toString().startsWith("http://info/"))
            {
              try
              {
                String file = url.getFile();
                URL infoURL = new URL("http://www.esantfeliu.org/info" + file);
                System.out.println(infoURL);
                setURL(infoURL);
              }
              catch (Exception ex)
              {
              }
            }
            else
            {
              setURL(url);
            }
          }
        }
      }
    });
  }

  public void setText(String mimeType, String text)
  {
    textPane.setContentType(mimeType);
    text = text.replaceAll("https://", "http://");
    textPane.setText(text);
    textPane.setCaretPosition(0);
  }
  
  public void setURL(URL url)
  {
    try
    {
      textPane.setPage(url);
    }
    catch (Exception ex)
    {
      textPane.setText("ERROR: " + ex);
    }
  }
}
