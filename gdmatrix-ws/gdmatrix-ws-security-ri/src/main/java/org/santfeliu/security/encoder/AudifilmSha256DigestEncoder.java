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
package org.santfeliu.security.encoder;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Encode the password using SHA-256 with a SALT
 * <br>
 * <br>
 * <pre>
 * <context-param>
 * <param-name>org.santfeliu.security.service.SecurityManager.digestEncoder</param-name>
 * <param-value>org.santfeliu.security.encoder.AudifilmSha256DigestEncoder</param-value>
 * </context-param><br>
 * <context-param>
 * <param-name>org.santfeliu.security.service.SecurityManager.digestParameters</param-name>
 * <param-value>SALT_DIGEST_VALUE</param-value>
 * </context-param>
 * </pre>
 *
 * @author xserrats
 *
 */
public class AudifilmSha256DigestEncoder implements DigestEncoder
{

  @Override
  public String encode(String password, String parameters) throws Exception
  {
    String wHashConcatenar = "_hash_";
    if (parameters != null && parameters.length() > 0)
    {
      wHashConcatenar = parameters.trim();
    }

    return sha256(wHashConcatenar + password.trim()).toUpperCase();
  }

  private static String sha256(String missatge) throws Exception
  {
    MessageDigest md;
    try
    {
      md = MessageDigest.getInstance("SHA-256");
      md.update(missatge.getBytes());
      byte[] mdbytes = md.digest();

      BigInteger intNumber = new BigInteger(1, mdbytes);
      String strHashCode = intNumber.toString(16);

      while (strHashCode.length() < 64)
      {
        strHashCode = "0" + strHashCode;
      }

      return strHashCode;
    }
    catch (Exception e)
    {
      e.printStackTrace(System.out);
      throw e;
    }
  }

}
