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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 *
 * @author realor
 */
public class URLCredentialsCipher
{
  public static final String CREDENTIALS_PARAMETER = "credentials";
  private StringCipher cipher;

  public URLCredentialsCipher(byte[] secret)
  {
    cipher = new StringCipher(secret);
  }

  public URLCredentialsCipher(String secret)
  {
    cipher = new StringCipher(secret);
  }

  /* url must be url encoded */
  public String putCredentials(String url, Credentials credentials) 
  {
    String curl = null;
    try
    {
      String digest = digestURL(url);
      String word = credentials.getUserId() + ":" +
        credentials.getPassword() + ":" + digest;
      String cword = cipher.encrypt(word);
      cword = URLEncoder.encode(cword, "UTF-8");
      if (url.indexOf("?") == -1)
      {
        curl = url + "?";
      }
      else
      {
        curl = url + "&";
      }
      curl += CREDENTIALS_PARAMETER + "=" + cword;
    }
    catch (Exception ex)
    {
      curl = url;
    }
    return curl;
  }

  /* curl must be url encoded */
  public Credentials getCredentials(String curl)
  {
    Credentials credentials = null;
    int index = curl.indexOf(CREDENTIALS_PARAMETER + "=");
    if (index != -1)
    {
      try
      {
        String url = curl.substring(0, index - 1);
        String cword =
          curl.substring(index + CREDENTIALS_PARAMETER.length() + 1);
        cword = URLDecoder.decode(cword, "UTF-8");
        String word = cipher.decrypt(cword);
        String digest = digestURL(url);
        String[] tokens = word.split(":");
        if (tokens[2].equals(digest))
        {
          credentials = new Credentials(tokens[0], tokens[1]);
        }
      }
      catch (Exception ex)
      {
      }
    }
    return credentials;
  }

  private String digestURL(String url)
  {
    String normalizedUrl;
    try
    {
      URL u = new URL(url);
      normalizedUrl = u.getFile();
    }
    catch (MalformedURLException ex)
    {
      normalizedUrl = url;
    }
    return SecurityUtils.calculateDigestBase64(normalizedUrl);
  }

  public static void main(String args[])
  {
    try
    {
      URLCredentialsCipher urlcipher = new URLCredentialsCipher("12345678");
      Credentials credentials = new Credentials("scott", "tiger");
      String url = urlcipher.putCredentials("http://www.santfeliu.cat/demo?a=7",
        credentials);
      System.out.println(url);
      credentials = urlcipher.getCredentials(url);
      System.out.println(credentials.getUserId());
      System.out.println(credentials.getPassword());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
