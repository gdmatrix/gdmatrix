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
package org.santfeliu.workflow.processor;

import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.WorkflowActor;
import org.santfeliu.workflow.WorkflowInstance;


/**
 *
 * @author realor
 */
public class SendMailNode extends org.santfeliu.workflow.node.SendMailNode 
  implements NodeProcessor
{
  @Override
  public String process(WorkflowInstance instance, WorkflowActor actor)
    throws Exception
  {
    Properties props = System.getProperties();
    props.put("mail.smtp.host", 
      Template.create(host).merge(instance));

    Session session;
    if (username != null && password != null &&
        username.length() > 0 && password.length() > 0)
    {
      System.out.println("STMP authetication as user " + username);
      // set authenticator
      session = Session.getDefaultInstance(props, new MailAuthenticator());
    }
    else
    {
      session = Session.getDefaultInstance(props);
    }
    session.setDebug(true);

    // create a message
    MimeMessage msg = new MimeMessage(session);
    
    // Prepare sender
    msg.setFrom(new InternetAddress(
      Template.create(sender).merge(instance)));

    // Prepare TO recipients
    InternetAddress[] addressListTO = 
      getAddressList(Template.create(recipientsTO).merge(instance));
    if (addressListTO != null)
      msg.setRecipients(MimeMessage.RecipientType.TO, addressListTO);

    // Prepare CC recipients
    InternetAddress[] addressListCC = 
      getAddressList(Template.create(recipientsCC).merge(instance));
    if (addressListCC != null)   
      msg.setRecipients(MimeMessage.RecipientType.CC, addressListCC);

    // Prepare BCC recipients
    InternetAddress[] addressListBCC = 
      getAddressList(Template.create(recipientsBCC).merge(instance));
    if (addressListBCC != null)
      msg.setRecipients(MimeMessage.RecipientType.BCC, addressListBCC);

    // Set subject
    msg.setSubject(Template.create(subject).merge(instance), charset);
    
    // create and fill the first message part
    if (StringUtils.isBlank(contentType) || 
      contentType.startsWith("text/plain"))
    {
      msg.setText(Template.create(message).merge(instance), charset);
    }
    else
    {
      msg.setContent(Template.create(message).merge(instance), contentType);      
    }
    
    // set the Date: header
    msg.setSentDate(new Date());

    // send the message
    Transport.send(msg);

    return CONTINUE_OUTCOME;
  }
  
  private InternetAddress[] getAddressList(String recipients)
    throws Exception
  {
    StringTokenizer tokenizer = new StringTokenizer(recipients, " ,;");
    
    int count = tokenizer.countTokens();
    if (count == 0) return null;
    
    InternetAddress[] addresses = new InternetAddress[count];
    int i = 0;
    while (tokenizer.hasMoreTokens())
    {
      String address = tokenizer.nextToken();
      addresses[i++] = new InternetAddress(address);
    }
    return addresses;
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
}
