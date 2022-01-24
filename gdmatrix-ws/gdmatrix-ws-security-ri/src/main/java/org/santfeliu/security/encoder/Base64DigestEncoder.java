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
import java.util.Base64;

/**
 *
 * @author realor
 */
public class Base64DigestEncoder implements DigestEncoder
{
  public static final String DEFAULT_ALGORITHM = "MD5";

  public Base64DigestEncoder()
  {
  }

  @Override
  public String encode(String password, String parameters)
    throws Exception
  {
    //parameters contains only the digest algorithm
    byte[] message = calcHash(password, parameters);
    return encodeHash(message);
  }

  private byte[] calcHash(String password, String algorithm)
    throws Exception
  {
    if (algorithm == null)
      algorithm = DEFAULT_ALGORITHM;
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
    byte[] digest = messageDigest.digest(password.getBytes());

    return digest;
  }

  private String encodeHash(byte[] message) throws Exception
  {
    return Base64.getMimeEncoder().encodeToString(message);
  }
}
