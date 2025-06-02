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
package org.santfeliu.webapp.modules.assistant.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 *
 * @author realor
 */
public class CharsetInputStream extends InputStream
{
  private final Reader reader;
  private Charset outCharset;
  private byte bytes[] = new byte[0];
  private int index = 0;

  public CharsetInputStream(InputStream is, Charset inCharset)
  {
    this(is, inCharset, Charset.defaultCharset());
  }

  public CharsetInputStream(InputStream is, Charset inCharset, Charset outCharset)
  {
    reader = new InputStreamReader(is, inCharset);
    this.outCharset = outCharset;
  }

  @Override
  public int read() throws IOException
  {
    if (index >= bytes.length)
    {
      int ch = reader.read();
      if (ch == -1) return -1;
      String sch = String.valueOf((char)ch);
      this.bytes = sch.getBytes(outCharset);
      index = 0;
    }
    return (int)bytes[index++];
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {
    return super.read(b, off, Math.min(b.length - off, 64));
  }

  @Override
  public void close() throws IOException
  {
    reader.close();
  }
}
