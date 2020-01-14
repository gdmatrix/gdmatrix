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
package org.santfeliu.security.util;

import org.apache.commons.codec.binary.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 *
 * @author realor
 */
public class StringCipher
{
  private Cipher ecipher;
  private Cipher dcipher;

  public StringCipher(byte[] secret)
  {
    init(secret);
  }

  public StringCipher(String secret)
  {
    init(secret.getBytes());
  }

  public String encrypt(String str)
  {
    try
    {
      byte[] utf8 = str.getBytes("UTF8");
      byte[] enc = ecipher.doFinal(utf8);
      return new String(Base64.encodeBase64(enc));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public String decrypt(String str)
  {
    try
    {
      byte[] dec = Base64.decodeBase64(str.getBytes());
      byte[] utf8 = dcipher.doFinal(dec);
      return new String(utf8, "UTF8");
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private void init(byte[] secret)
  {
    try
    {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
      SecretKey key = factory.generateSecret(new DESKeySpec(secret));
      ecipher = Cipher.getInstance("DES");
      dcipher = Cipher.getInstance("DES");
      ecipher.init(Cipher.ENCRYPT_MODE, key);
      dcipher.init(Cipher.DECRYPT_MODE, key);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args)
  {
    try
    {
      StringCipher cipher = new StringCipher("34fsh256");
      String s1 = cipher.encrypt("Això és una prova");
      String s2 = cipher.decrypt(s1);
      System.out.println(s1);
      System.out.println(s2);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
