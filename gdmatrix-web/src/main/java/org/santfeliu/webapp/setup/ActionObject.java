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
package org.santfeliu.webapp.setup;

import java.util.ArrayList;
import java.util.List;
import org.santfeliu.webapp.setup.ActionObject.Message.Severity;

/**
 *
 * @author blanquepa
 */
public class ActionObject
{
  private Object object;
  private boolean refresh = false;
  private final List<Message> messages = new ArrayList();
  
  public ActionObject(Object object)
  {
    this.object = object;
  }

  public Object getObject()
  {
    return object;
  }

  public void setObject(Object object)
  {
    this.object = object;
  }

  public boolean isRefresh()
  {
    return refresh;
  }

  public void setRefresh(boolean refresh)
  {
    this.refresh = refresh;
  }

  public void addInfo(String message, String... params)
  {
    messages.add(new Message(message, params, Severity.INFO));    
  }  
  
  public void addWarn(String message, String... params)
  {
    messages.add(new Message(message, params, Severity.WARN));    
  }  
  
  public void addError(String message, String... params)
  {
    messages.add(new Message(message, params, Severity.ERROR));     
  }

  public List<Message> getMessages()
  {
    return messages;
  }
  
  public static class Message
  {
    private String text;
    private String[] params;
    private Severity severity;
    
    public enum Severity
    {
      INFO,
      WARN,
      ERROR
    }    

    public Message(String text, String[] params, Severity severity)
    {
      this.text = text;
      this.params = params;
      this.severity = severity;
    }
    
    public String getText()
    {
      return text;
    }

    public void setText(String text)
    {
      this.text = text;
    }

    public String[] getParams()
    {
      return params;
    }

    public void setParams(String[] params)
    {
      this.params = params;
    }

    public Severity getSeverity()
    {
      return severity;
    }

    public void setSeverity(Severity severity)
    {
      this.severity = severity;
    }
    
  }
  
}
