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
package org.santfeliu.misc.sendmail.web;

import java.io.Serializable;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MailSender;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author unknown
 */
@CMSManagedBean
public class SendMailBean extends WebBean implements Serializable
{
  private String url;
  private Map values = new TreeMap();
  private boolean mailSent;
  
  @CMSProperty(mandatory=true)
  public static final String RECIPIENT_PROPERTY = "mail.to";
  @CMSProperty
  public static final String DEFAULT_FROM_PROPERTY = "mail.from";
  @CMSProperty
  public static final String DEFAULT_SUBJECT_PROPERTY = "mail.subject";
  @CMSProperty
  public static final String DATAFORM_DOCCOD_PROPERTY = "form.data.doccod";
  @CMSProperty
  public static final String MESSAGEFORM_DOCCOD_PROPERTY =
    "form.message.doccod";
  @CMSProperty
  public static final String URL_PROPERTY = "url";

  public static final String SMTP_HOST = "mail.smtp.host";
  
  public static final String FROM_FORM_PARAMETER = "_from";
  public static final String SUBJECT_FORM_PARAMETER = "_subject";
  
  public static final String ERROR_PROPERTY_NOT_FOUND = "PROPERTY_NOT_FOUND: ";
  public static final String HOST_NOT_FOUND = "HOST_NOT_FOUND";

  public SendMailBean()
  {
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getUrl()
  {
    return url;
  }

  public void setValues(Map values)
  {
    this.values = values;
  }

  public Map getValues()
  {
    return values;
  }

  // action methods
  @CMSAction
  public String loadForm()
  {
    try
    {
      mailSent = false;
      MenuItemCursor cursor = UserSessionBean.getCurrentInstance().
        getMenuModel().getSelectedMenuItem();

      url = (String)cursor.getProperties().get(URL_PROPERTY);
      if (url == null)
        url = getDocumentServletUrl(cursor, DATAFORM_DOCCOD_PROPERTY);
      if (values != null)
        values.clear();
      return url == null ? "blank" : "send_mail";
    }
    catch (Exception ex)
    {
      error(ex.getLocalizedMessage());
      ex.printStackTrace();
    }
    return null;
  }

  public String sendMail()
  {
    try
    {
      MenuItemCursor cursor = UserSessionBean.getCurrentInstance().
        getMenuModel().getSelectedMenuItem();

      String from = (String)values.get(FROM_FORM_PARAMETER);
      if (from == null)
        from = cursor.getProperty(DEFAULT_FROM_PROPERTY);
        if (from == null)
          throw new Exception(ERROR_PROPERTY_NOT_FOUND + DEFAULT_FROM_PROPERTY);
          
      String subject = (String)values.get(SUBJECT_FORM_PARAMETER);      
      if (subject == null)
        subject = cursor.getProperty(DEFAULT_SUBJECT_PROPERTY);
        if (subject == null)
          subject = "";
          
      String host = MatrixConfig.getProperty(SMTP_HOST);
      
      if (host == null)
        throw new Exception(HOST_NOT_FOUND);

      String text = getFormContent(values);
      
      Locale locale = getExternalContext().getRequestLocale();
      String[] recArray = getRecipients(cursor, locale.getLanguage());
      MailSender.sendMail(host, from, recArray, subject, text, true);        
      mailSent = true;      
      url = getDocumentServletUrl(cursor, MESSAGEFORM_DOCCOD_PROPERTY);
      return url == null ? "blank" : "send_mail";
      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();    
      error(ex.getMessage());
    }
    return null;
  }
  
  private String[] getRecipients(MenuItemCursor cursor, String language)
    throws Exception
  {
    List<String> recipients = cursor.getMultiValuedProperty(RECIPIENT_PROPERTY);
    if (recipients == null || recipients.isEmpty())
      throw new Exception(ERROR_PROPERTY_NOT_FOUND + RECIPIENT_PROPERTY);
          
    String[] recArray = 
      (String[])recipients.toArray(new String[recipients.size()]);
    
    return recArray;
  }

  private String getDocumentServletUrl(MenuItemCursor cursor, String property)
  {
    String url = null;
    String docid = (String)cursor.getProperties().get(property);
    if (docid != null)
    {
      url = getContextURL() + "/documents/" + docid;
    }
    
    return url;
  }
  
  private String getFormContent(Map values)
  {
    values.remove(SUBJECT_FORM_PARAMETER);
    values.remove(FROM_FORM_PARAMETER);
    
    StringBuilder sb = new StringBuilder();
    Set set = values.entrySet();
    Iterator it = set.iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry)it.next();
      sb.append(entry.getKey());
      sb.append(" = ");
      sb.append(entry.getValue());
      sb.append("\n");
    }
    
    return sb.toString();
  }

  public void setMailSent(boolean mailSent)
  {
    this.mailSent = mailSent;
  }

  public boolean isMailSent()
  {
    return mailSent;
  }
}
