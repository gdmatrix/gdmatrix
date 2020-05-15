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
package org.santfeliu.util.iarxiu.client;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.oxm.xmlbeans.XmlBeansMarshaller;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapHeaderException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import x0Assertion.oasisNamesTcSAML2.AssertionDocument;
import x0Assertion.oasisNamesTcSAML2.AssertionType;
import x0Assertion.oasisNamesTcSAML2.AttributeStatementType;
import x0Assertion.oasisNamesTcSAML2.AttributeType;
import x0Assertion.oasisNamesTcSAML2.NameIDType;
import x0Assertion.oasisNamesTcSAML2.SubjectConfirmationType;
import x0Assertion.oasisNamesTcSAML2.SubjectType;

/**
 *
 * @author blanquepa
 */
public class SamlInterceptor implements ClientInterceptor
{
  private String userName;
  private String organizationAlias;
  private String fonsAlias;
  private String memberOf;

	public boolean handleFault(MessageContext arg0)
    throws WebServiceClientException
  {
		return false;
	}

	public boolean handleRequest(MessageContext messageContext)
    throws WebServiceClientException
  {
		QName qn = new QName("http://soap.iarxiu/headers","Context","ish");
		
		SoapMessage soapMessage = ((SoapMessage)messageContext.getRequest());
		
		SoapHeaderElement wsh = soapMessage.getSoapHeader().addHeaderElement(qn);
		wsh.setMustUnderstand(true);
		
		AssertionDocument assertion = createAssertion();

		Map<String,String> ns = new HashMap<String,String>();
		ns.put("urn:oasis:names:tc:SAML:2.0:assertion", "saml2");
		
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSaveSuggestedPrefixes(ns);
		xmlOptions.setSaveAggressiveNamespaces();
		
		XmlBeansMarshaller marshaller = new XmlBeansMarshaller();
		marshaller.setXmlOptions(xmlOptions);
		
		try
    {
			marshaller.marshal(assertion, wsh.getResult());
		} 
    catch(IOException e)
    {
			throw new SaajSoapHeaderException("Error creating SAML Header", e);
		}

		return true;
	}

	public boolean handleResponse(MessageContext messageContext)
    throws WebServiceClientException
  {
		return false;
	}
	
	protected AssertionDocument createAssertion()
  {
		AssertionDocument assertionDocument = AssertionDocument.Factory.newInstance();
		AssertionType assertion;
		
		assertion = assertionDocument.addNewAssertion();
		
		assertion.setVersion("2.0");
		assertion.setID("AssertId-" + System.currentTimeMillis());
		assertion.setIssueInstant( Calendar.getInstance() );
		
		NameIDType issuerName = assertion.addNewIssuer();
		issuerName.setStringValue("iArxiuClient");
		
		SubjectType subject = assertion.addNewSubject();
		
		SubjectConfirmationType subjectConfirmation =
      subject.addNewSubjectConfirmation();
		subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:sender-vouches");
		
		NameIDType subjectName = subjectConfirmation.addNewNameID();
		subjectName.setStringValue(this.userName);
		
		AttributeStatementType attributeStatement =
      assertion.addNewAttributeStatement();
		
		addAttribute(attributeStatement,
      "urn:iarxiu:2.0:names:organizationAlias", this.organizationAlias);
		addAttribute(attributeStatement,
      "urn:iarxiu:2.0:names:fondsAlias", this.fonsAlias);
		addAttribute(attributeStatement,
      "urn:iarxiu:2.0:names:member-of", this.memberOf);
		
		return assertionDocument;
	}
	
	private void addAttribute(
			final AttributeStatementType attributeStatement,
			final String name,
			final String value )
  {
		AttributeType attribute = attributeStatement.addNewAttribute();
		attribute.setName(name);
		XmlObject xmlNode = attribute.addNewAttributeValue();
		Node node = xmlNode.getDomNode();
		Text text = node.getOwnerDocument().createTextNode(value);
		node.appendChild(text);
	}

  public String getFonsAlias()
  {
    return fonsAlias;
  }

  public void setFonsAlias(String fonsAlias)
  {
    this.fonsAlias = fonsAlias;
  }

  public String getMemberOf()
  {
    return memberOf;
  }

  public void setMemberOf(String memberOf)
  {
    this.memberOf = memberOf;
  }

  public String getOrganizationAlias()
  {
    return organizationAlias;
  }

  public void setOrganizationAlias(String organizationAlias)
  {
    this.organizationAlias = organizationAlias;
  }

  public String getUserName()
  {
    return userName;
  }

  public void setUserName(String userName)
  {
    this.userName = userName;
  }
}
