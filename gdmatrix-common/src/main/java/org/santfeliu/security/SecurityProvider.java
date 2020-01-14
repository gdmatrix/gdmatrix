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
package org.santfeliu.security;

import java.io.OutputStream;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 * @author unknown
 */
public interface SecurityProvider
{
  public static final String COMMON_NAME = 
    "urn:oasis:names:tc:dss:1.0:profiles:XSS:certificateAttributes:SubjectDistinguishedName:commonName";
  public static final String GIVEN_NAME = 
    "urn:oasis:names:tc:dss:1.0:profiles:XSS:certificateAttributes:SubjectDistinguishedName:givenName";
  public static final String SURNAME = 
    "urn:oasis:names:tc:dss:1.0:profiles:XSS:certificateAttributes:SubjectDistinguishedName:surname";
  public static final String NIF = 
    "urn:catcert:psis:certificateAttributes:KeyOwnerNIF";
  public static final String CIF = 
    "urn:catcert:psis:certificateAttributes:LegalEntityCIF";
  public static final String TITLE = 
    "urn:catcert:psis:certificateAttributes:Title";
  public static final String DEPARTMENT = 
    "urn:catcert:psis:certificateAttributes:Department";
  public static final String ORGANIZATION_NAME =
    "urn:oasis:names:tc:dss:1.0:profiles:XSS:certificateAttributes:SubjectDistinguishedName:organizationName";
  public static final String COUNTRY = 
    "urn:oasis:names:tc:dss:1.0:profiles:XSS:certificateAttributes:SubjectDistinguishedName:countryName";
  public static final String EMAIL = 
    "urn:oasis:names:tc:dss:1.0:profiles:XSS:certificateAttributes:SubjectEmail";
  public static final String ALT_NAME =
    "urn:oasis:names:tc:dss:1.0:profiles:XSS:certificateAttributes:Extension:subjectAltName";

  public String getName();

  public boolean validateCertificate(byte[] certEncoded, Map attributes);
  
  public boolean validateSignatureCMS(byte[] cmsEncoded, OutputStream out);
  
  public boolean validateSignatureXML(Document signature, OutputStream out);
  
  public byte[] createCMSTimeStamp(byte[] digest, String digestMethod, byte[] certEncoded);
  
  public Element createXMLTimeStamp(byte[] digest, String digestMethod, byte[] certEncoded);
}
