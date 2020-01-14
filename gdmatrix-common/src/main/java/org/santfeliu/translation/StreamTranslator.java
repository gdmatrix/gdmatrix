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
package org.santfeliu.translation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author realor
 */
public abstract class StreamTranslator
{
  protected StringTranslator stringTranslator =
    TranslatorFactory.getDefaultStringTranslator();

  public StringTranslator getStringTranslator()
  {
    return stringTranslator;
  }

  public void setStringTranslator(StringTranslator stringTranslator)
  {
    this.stringTranslator = stringTranslator;
  }

  /**
   *
   * @param in: input stream to translateString
   * @param out: translated output stream
   * @param language: output language
   * @param group: translation group
   * @throws java.io.IOException
   */
  public abstract void translate(InputStream in, OutputStream out,
     String language, String group) throws IOException;

  public void translate(File in, File out, String language, String group)
    throws IOException
  {
    translate(new FileInputStream(in), new FileOutputStream(out),
      language, group);
  }

  public InputStream translate(InputStream in, String language, String group)
    throws IOException
  {
    // TODO: use a circular buffer to improve performance
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    translate(in, out, language, group);
    return new ByteArrayInputStream(out.toByteArray());
  }
}
