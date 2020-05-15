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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 * @author blanquepa
 */
public class Utils {

	/**
	 * Prints an XMLObject.
	 * @param xmlObj XMLObject to print
	 */
	public static void printXmlObject(XmlObject xmlObj){	
		//print request message
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSaveOuter();
		System.out.println(xmlObj);
	}
	
	
	/** 
	 * Returns the contents of the file in a byte array.
	 */
    public static byte[] getBytesFromFile(String path) throws IOException {
    	
    	InputStream is = ClassLoader.getSystemResourceAsStream(path);
        byte[] bytes = getBytes(is);
        is.close();

        return bytes;
    }
    
    
    /**
     * Gets a byte array from an InputStream.
     * @param is InputStream
     * @return byte[]
     * @throws IOException
     */
    public static byte[] getBytes(InputStream is) throws IOException {
    
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	
    	int bytee;
    	while (-1!=(bytee=is.read()))
    	{
    	   baos.write(bytee);
    	}
    	baos.close();
    	byte[] bytes = baos.toByteArray();

        is.close();
        return bytes;
    }
	
    
    public static void writeToFile(String path, byte[] data) throws IOException{
    	File fOut = new File(path);
		FileOutputStream fos = new FileOutputStream(fOut);
		fos.write(data);
		fos.close();
    }
	
}
