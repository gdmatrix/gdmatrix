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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.XMLPrinter;

/**
 *
 * @author realor
 */
public class SOAPDebugHandler implements SOAPHandler<SOAPMessageContext>
{
  protected static final Logger logger = Logger.getLogger("SOAPDebugHandler");
  protected boolean printInboundMessage = true;
  protected boolean printOutboundMessage = false;
  protected boolean printFaultMessage = true;

  public SOAPDebugHandler()
  {
  }

  public boolean isPrintFaultMessage()
  {
    return printFaultMessage;
  }

  public void setPrintFaultMessage(boolean printFaultMessage)
  {
    this.printFaultMessage = printFaultMessage;
  }

  public boolean isPrintInboundMessage()
  {
    return printInboundMessage;
  }

  public void setPrintInboundMessage(boolean printInboundMessage)
  {
    this.printInboundMessage = printInboundMessage;
  }

  public boolean isPrintOutboundMessage()
  {
    return printOutboundMessage;
  }

  public void setPrintOutboundMessage(boolean printOutboundMessage)
  {
    this.printOutboundMessage = printOutboundMessage;
  }

  public Set<QName> getHeaders()
  {
    return null;
  }

  public boolean handleMessage(SOAPMessageContext context)
  {
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(bos);
      XMLPrinter xmlPrinter = new XMLPrinter();
      Boolean outboundProperty = (Boolean)
        context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      SOAPMessage message = context.getMessage();
      String operation = getOperation(message);
      if (outboundProperty)
      {
        out.println("<<<================,OUT," + operation + ",-,-");
        if (printOutboundMessage)
          xmlPrinter.print(message.getSOAPBody(), out);
      }
      else
      {
        HttpServletRequest request = (HttpServletRequest)
          context.get(MessageContext.SERVLET_REQUEST);
        Credentials credentials = SecurityUtils.getCredentials(request);
        String userId = credentials.getUserId();
        out.println("================>>>,IN," + operation + "," + userId + ",");
        if (printInboundMessage)
          xmlPrinter.print(message.getSOAPBody(), out);
      }
      out.flush();
      logger.log(Level.INFO, bos.toString());
    }
    catch (Exception ex)
    {
    }
    return true;
  }

  public boolean handleFault(SOAPMessageContext context)
  {
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(bos);
      XMLPrinter xmlPrinter = new XMLPrinter();
      out.println("<<<================,FAULT,-,-,");
      SOAPMessage message = context.getMessage();
      if (printFaultMessage)
        xmlPrinter.print(message.getSOAPBody(), out);
      out.flush();
      logger.log(Level.INFO, bos.toString());
    }
    catch (Exception ex)
    {
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