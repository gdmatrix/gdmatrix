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
package org.santfeliu.util.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import org.santfeliu.util.script.WebScriptableBase;

/**
 *
 * @author realor
 */
public class WebTemplate extends Template
{
  public WebTemplate(Reader reader) throws IOException
  {
    super(reader);
  }

  public WebTemplate(String s)
  {
    super(s);
  }

  public static WebTemplate create(String s)
  {
    return new WebTemplate(s);
  }

  public static WebTemplate create(Reader reader) throws IOException
  {
    return new WebTemplate(reader);
  }

  public static WebTemplate create(File file) throws IOException
  {
    return new WebTemplate(new InputStreamReader(new FileInputStream(file)));
  }

  @Override
  protected Scriptable createScriptable(Context cx, Map variables)
  {
    Scriptable scriptable = new WebScriptableBase(cx, variables);
    return scriptable;
  }
  
}
