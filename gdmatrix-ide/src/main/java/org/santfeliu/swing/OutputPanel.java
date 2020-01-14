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
import java.awt.Color;
import java.awt.Font;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 *
 * @author unknown
 */
public class OutputPanel extends JPanel implements Runnable
{
  private JScrollPane scrollPane = new JScrollPane();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JTextArea textArea = new JTextArea();
  private StringBuffer buffer = new StringBuffer();
  private int openStreams = 0;

  public OutputPanel()
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

  public synchronized void clear()
  {
    buffer.setLength(0);
    textArea.setText("");
  }

  public synchronized OutputStream getOutputStream()
  {
    if (openStreams == 0)
    {
      Thread thread = new Thread(this);
      openStreams++;
      thread.start();
    }
    return new OutputStream();
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(borderLayout1);
    textArea.setFont(new Font("Monospaced", 0, 14));
    scrollPane.getViewport().add(textArea, null);
    this.add(scrollPane, BorderLayout.CENTER);
    textArea.setEditable(false);
    textArea.setForeground(Color.black);
  }

  public void run()
  {
    while (openStreams > 0)
    {
      try
      {
        Thread.sleep(100);
      }
      catch (Exception ex)
      {
      }
      textArea.append(buffer.toString()); // Thread safe
      buffer.setLength(0);
      textArea.setCaretPosition(textArea.getText().length());
    }
  }

  class OutputStream extends java.io.OutputStream
  {
    public void write(int b) throws IOException
    {
      char ch = (char)b;
      buffer.append(ch);
    }
    
    public void close() throws IOException
    {
      synchronized (OutputPanel.this)
      {
        openStreams--;
      }
    }
  }

  public static void main(String[] args)
  {
    try
    {
      JFrame frame = new JFrame();
      OutputPanel op = new OutputPanel();
      frame.getContentPane().add(op);
      frame.setSize(500, 300);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
      
      OutputStream os = op.getOutputStream();
      for (int i = 0; i < 10000; i++)
      {
        os.write(("HOLA-" + i + "\n").getBytes());
        try
        {
          Thread.sleep(1);
        }
        catch (Exception ex)
        {
        }
      }
      os.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
