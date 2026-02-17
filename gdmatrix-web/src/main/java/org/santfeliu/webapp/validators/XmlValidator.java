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
package org.santfeliu.webapp.validators;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author granadogj
 */
public class XmlValidator
{  
  public static List<String> validateXML(String xmlSource)
  {
    List<String> errors = new ArrayList<>();
    
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setValidating(false); // No DTD/XSD, only basic sintax
      //factory.setSchema(schema); // Add DTD/XSD if necessary
      
      DocumentBuilder builder = factory.newDocumentBuilder();
      
      // Capture parsing errors according to SAX (abstract methods implementation)
      builder.setErrorHandler(new ErrorHandler(){
        @Override
        public void warning(SAXParseException ex)
        {
          errors.add(String.format("Warning (line %d): %s", ex.getLineNumber(), ex.getMessage()));
        }

        @Override
        public void error(SAXParseException ex)
        {
          errors.add(String.format("Error (line %d): %s", ex.getLineNumber(), ex.getMessage()));
        }

        @Override
        public void fatalError(SAXParseException ex)
        {
          errors.add(String.format("FatalError (line %d): %s", ex.getLineNumber(), ex.getMessage()));
        }
      });
      
      // Parsing XML from String
      builder.parse(new InputSource(new StringReader(xmlSource)));
    }
    catch(Exception ex)
    {
      errors.add("Unexpected error: " +ex.getMessage());
    }
    
    return errors;
  }
}
