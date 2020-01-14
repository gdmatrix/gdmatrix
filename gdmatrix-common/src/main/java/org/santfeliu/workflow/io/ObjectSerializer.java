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
package org.santfeliu.workflow.io;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;

import org.santfeliu.util.Properties;

/**
 *
 * @author unknown
 */
public class ObjectSerializer
{
  public static final String JAVA_SER_B64 = "java_ser_b64:";

  public ObjectSerializer()
  {
  }

  public String serialize(Object value, Class valueClass) throws Exception
  {
    String str;

    if (value == null)
    {
      str = "";
    }
    else if (valueClass == String.class)
    {
      str = "<![CDATA[" + value.toString() + "]]>";
    }
    else if (valueClass == int.class || valueClass == Integer.class ||
             valueClass == long.class || valueClass == Long.class ||
             valueClass == short.class || valueClass == Short.class ||
             valueClass == float.class || valueClass == Float.class ||
             valueClass == double.class || valueClass == Double.class)
    {
      str = value.toString();
    }
    else if (valueClass == boolean.class || valueClass == Boolean.class)
    {
      str = value.toString();
    }
    else if (valueClass == org.santfeliu.util.Properties.class)
    {
      Properties properties = (Properties)value;
      str = "<![CDATA[" + properties.saveToString() + "]]>";
    }
    else if (value instanceof Serializable)
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream os = new ObjectOutputStream(bos);
      os.writeObject(value);
      os.close();
      byte[] data = bos.toByteArray();
      str = JAVA_SER_B64 + new String(Base64.encodeBase64(data));
    }
    else
    {
      throw new Exception("Invalid property class");
    }
    return str;
  }
}
