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
package org.santfeliu.workflow.node;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import org.santfeliu.workflow.WorkflowNode;


/**
 *
 * @author unknown
 */
public class SendMailNode extends WorkflowNode
{
  public static final String CHARSET = "ISO-8859-1";

  protected String host;
  protected String sender;
  protected String recipientsTO;
  protected String recipientsCC;
  protected String recipientsBCC;
  protected String subject;
  protected String message;
  protected String charset = CHARSET;
  protected String contentType = "text/plain";
  protected String username;
  protected String password;
  
  public SendMailNode()
  {
  }
  
  @Override
  public String getType()
  {
    return "SendMail";
  }

  public void setRecipientsTO(String recipientsTO)
  {
    this.recipientsTO = recipientsTO;
  }

  public String getRecipientsTO()
  {
    return recipientsTO;
  }

  public void setRecipientsCC(String recipientsCC)
  {
    this.recipientsCC = recipientsCC;
  }

  public String getRecipientsCC()
  {
    return recipientsCC;
  }

  public void setRecipientsBCC(String recipientsBCC)
  {
    this.recipientsBCC = recipientsBCC;
  }

  public String getRecipientsBCC()
  {
    return recipientsBCC;
  }

  public void setHost(String host)
  {
    this.host = host;
  }

  public String getHost()
  {
    return host;
  }

  public void setSender(String sender)
  {
    this.sender = sender;
  }

  public String getSender()
  {
    return sender;
  }

  public void setSubject(String subject)
  {
    this.subject = subject;
  }

  public String getSubject()
  {
    return subject;
  }

  public String getContentType()
  {
    return contentType;
  }

  public void setContentType(String contentType)
  {
    this.contentType = contentType;
  }
  
  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public String getCharset()
  {
    return charset;
  }

  public void setCharset(String charset)
  {
    this.charset = charset;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }


  class MailAuthenticator extends Authenticator
  {
    public MailAuthenticator()
    {
    }
    
    @Override
    protected PasswordAuthentication getPasswordAuthentication()
    {
      return new PasswordAuthentication(username, password);
    }
  }

  @Override
  public boolean containsText(String text)
  {
    if (super.containsText(text)) return true;
    if (message != null && message.contains(text)) return true;
    if (recipientsTO != null && recipientsTO.contains(text)) return true;
    if (recipientsCC != null && recipientsCC.contains(text)) return true;
    if (recipientsBCC != null && recipientsBCC.contains(text)) return true;
    return false;
  }
  
 
}
