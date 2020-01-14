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
package org.santfeliu.util;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 *
 * @author unknown
 */
public class MailSender
{
  public static String CHARSET = "iso-8859-1";
  
  public MailSender()
  {
  }
  
  public static boolean sendMail(String host, String from, String recipient, 
    String subject, String text, boolean debug)
    throws MessagingException
  {
    String[] recipients = new String[]{recipient};
    return sendMail(host, from, recipients, subject, text, debug);
  }
  
  public static boolean sendMail(String host, String from, String[] recipients,
    String subject, String text, boolean debug)
    throws MessagingException
  {
    return sendMail(host, from, recipients, null, null, subject, text, debug);
  }

  public static boolean sendMail(String host, String from, String[] recipients,
    String[] ccs, String[] bccs, String subject, String text, boolean debug)
    throws MessagingException
  {
    MimeMessage msg = createMessage(host, from, subject, text, debug, false);
    //to
    setRecipients(msg, recipients, MimeMessage.RecipientType.TO);
    //cc
    if (ccs != null)
      setRecipients(msg, recipients, MimeMessage.RecipientType.CC);    
    //bcc
    if (bccs != null)
      setRecipients(msg, recipients, MimeMessage.RecipientType.BCC);    
    // send the message
    send(msg);

    return true;
  }
  
  public static boolean sendHtmlMail(String host, String from, String recipient, 
    String subject, String text, boolean debug)
    throws MessagingException
  {
    String[] recipients = new String[]{recipient};
    return sendHtmlMail(host, from, recipients, subject, text, debug);
  }
  
  public static boolean sendHtmlMail(String host, String from, String[] recipients,
    String subject, String text, boolean debug)
    throws MessagingException
  {
    return sendHtmlMail(host, from, recipients, null, null, subject, text, debug);
  }  
  
  public static boolean sendHtmlMail(String host, String from, String[] recipients,
    String[] ccs, String[] bccs, String subject, String text, boolean debug)
    throws MessagingException
  {
    MimeMessage msg = createMessage(host, from, subject, text, debug, true);
    //to
    setRecipients(msg, recipients, MimeMessage.RecipientType.TO);
    //cc
    if (ccs != null)
      setRecipients(msg, recipients, MimeMessage.RecipientType.CC);    
    //bcc
    if (bccs != null)
      setRecipients(msg, recipients, MimeMessage.RecipientType.BCC);    
    // send the message
    send(msg);

    return true;    
  }

  private static MimeMessage createMessage(String host, String from, String subject,
    String text, boolean debug, boolean html)
    throws MessagingException
  {
    // create some properties and get default Session
    Properties props = System.getProperties();
    props.put("mail.smtp.host", host);
    Session session = Session.getDefaultInstance(props, null);
    session.setDebug(debug);

    // create a message
    MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(from));
    msg.setSubject(subject, CHARSET);
    if (html)
      msg.setContent(text, "text/html;charset=" + CHARSET);
    else
      msg.setText(text, CHARSET);
    
    return msg;
  }
  
  private static MimeMessage setRecipients(MimeMessage msg, String[] recipients, 
    Message.RecipientType recType) throws MessagingException
  {
    InternetAddress[] address = new InternetAddress[recipients.length];
    for (int i = 0; i < recipients.length; i++)
    {
      address[i] = new InternetAddress(recipients[i]);
    }
    msg.setRecipients(recType, address);
    return msg;
  }
  
  private static void send(MimeMessage msg) throws MessagingException
  {
    // set the Date: header
    msg.setSentDate(new Date());
    // send the message
    Transport.send(msg);
  }
}
