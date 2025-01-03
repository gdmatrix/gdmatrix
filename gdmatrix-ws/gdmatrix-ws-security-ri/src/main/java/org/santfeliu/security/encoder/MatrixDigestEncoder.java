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

import java.security.MessageDigest;


/**
 *
 * @author realor
 */
public class MatrixDigestEncoder implements DigestEncoder
{
  public static final int DIGEST_LENGTH = 8;
  public static final String DIGEST_ALGORITHM = "MD5";

  public MatrixDigestEncoder()
  {
  }

  @Override
  public String encode(String password, String parameters) throws Exception
  {
    byte[] message = calcHash(password);
    String result =  encodeHash(message);
    System.out.println(result);
    return result;
  }

  private byte[] calcHash(String password)
    throws Exception
  {
    MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
    byte[] digest = messageDigest.digest(password.getBytes());

    return digest;
  }

  private String encodeHash(byte[] message) throws Exception
  {
    String strMessage = new String(message, 0, DIGEST_LENGTH);
    StringBuilder buffer = new StringBuilder();
    for (int index = 0; index < DIGEST_LENGTH; index++)
    {
      char ch = strMessage.charAt(index);
      if (ch < 32 || ch > 128) ch = (char) (32 + ch % 91);
      buffer.append(ch);
    }
    return buffer.toString();
  }

  public static void main(String args[])
  {
    try
    {
      MatrixDigestEncoder encoder = new MatrixDigestEncoder();
      encoder.encode("", null);
    }
    catch (Exception ex)
    {
    }
  }
}
