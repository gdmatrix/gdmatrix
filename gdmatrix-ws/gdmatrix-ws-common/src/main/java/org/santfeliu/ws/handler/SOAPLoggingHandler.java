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
package org.santfeliu.ws.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.XMLPrinter;
import org.santfeliu.util.log.CSVLogger;

/**
 *
 * @author realor
 */
public class SOAPLoggingHandler implements SOAPHandler<SOAPMessageContext>
{
  public static final String LOG_CONFIG = "org.santfeliu.ws.logConfig";

  protected static CSVLogger logger;

  static
  {
    String logConfig = MatrixConfig.getPathProperty(LOG_CONFIG);
    if (logConfig != null)
    {
      logger = CSVLogger.getInstance(logConfig);
    }
  }

  public Set<QName> getHeaders()
  {
    return null;
  }

  public boolean handleMessage(SOAPMessageContext context)
  {
    try
    {
      if (logger != null)
      {
        SOAPMessage message = context.getMessage();
        String operation = getOperation(message);

        Boolean outboundProperty = (Boolean)
          context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        String messageType = Boolean.TRUE.equals(outboundProperty) ?
          "OUT" : "IN";

        XMLPrinter xmlPrinter = new XMLPrinter();
        xmlPrinter.setTruncateLongTexts(true);
        xmlPrinter.setIndentSize(0);
        xmlPrinter.setSingleLine(true);
        xmlPrinter.setPrintNamespaces(false);
        xmlPrinter.setPrintAttributes(false);
        xmlPrinter.setMaxOutputSize(128);
        String messageString = xmlPrinter.format(message.getSOAPBody());

        // TODO: CHECK URL
        HttpServletRequest request = (HttpServletRequest)
          context.get(MessageContext.SERVLET_REQUEST);
        String url = request.getRequestURL().toString();
        String ip = request.getRemoteAddr();

        Credentials credentials = SecurityUtils.getCredentials(request);
        String userId = credentials.getUserId();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        String dateTime = df.format(new Date());

        logger.log(dateTime, userId, ip, url,
          operation, messageType, messageString);
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    return true;
  }

  public boolean handleFault(SOAPMessageContext context)
  {
    try
    {
      if (logger != null)
      {
        SOAPMessage message = context.getMessage();
        String operation = getOperation(message);

        String messageType = "FAULT";

        XMLPrinter xmlPrinter = new XMLPrinter();
        xmlPrinter.setTruncateLongTexts(true);
        xmlPrinter.setIndentSize(0);
        xmlPrinter.setSingleLine(true);
        xmlPrinter.setPrintNamespaces(false);
        xmlPrinter.setPrintAttributes(false);
        String messageString = xmlPrinter.format(message.getSOAPBody());

        HttpServletRequest request = (HttpServletRequest)
          context.get(MessageContext.SERVLET_REQUEST);
        String url = request.getRequestURL().toString();
        String ip = request.getRemoteAddr();

        Credentials credentials = SecurityUtils.getCredentials(request);
        String userId = credentials.getUserId();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        String dateTime = df.format(new Date());

        logger.log(dateTime, userId, ip, url,
          operation, messageType, messageString);
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    return true;
  }

  public void close(MessageContext context)
  {
  }

  private String getOperation(SOAPMessage message)
  {
    String operation;
    try
    {
      operation = message.getSOAPBody().getFirstChild().getLocalName();
    }
    catch (Exception ex)
    {
      operation = "-";
    }
    return operation;
  }
}
