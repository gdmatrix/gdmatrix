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
package org.santfeliu.ant.gui;

import javax.swing.JOptionPane;
import org.apache.tools.ant.Task;

/**
 *
 * @author real
 */
public class AlertTask extends Task
{
  private String title;
  private String message;
  private String type = "info";

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }


  @Override
  public void execute()
  {
    int messageType;
    if ("error".equalsIgnoreCase(type))
    {
      messageType = JOptionPane.ERROR_MESSAGE;
    }
    else if ("warning".equalsIgnoreCase(type))
    {
      messageType = JOptionPane.WARNING_MESSAGE;
    }
    else
    {
      messageType = JOptionPane.INFORMATION_MESSAGE;
    }
    String text = null;
    if (message != null) text = getProject().replaceProperties(message);
    JOptionPane.showMessageDialog(null, text, title, messageType);
  }

  public void addText(String text)
  {
    this.message = text;
  }
}
