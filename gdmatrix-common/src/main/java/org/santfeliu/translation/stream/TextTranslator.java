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
package org.santfeliu.translation.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import org.santfeliu.translation.StreamTranslator;

/**
 *
 * @author realor
 */
public abstract class TextTranslator extends StreamTranslator
{
  public abstract void translate(Reader reader, Writer writer,
    String language, String group) throws IOException;

  public Reader translate(Reader reader, String language, String group)
    throws IOException
  {
    // TODO: use circular buffer to improve performance
    StringWriter sw = new StringWriter();
    translate(reader, sw, language, group);
    return new StringReader(sw.toString());
  }

  public void translate(InputStream in, OutputStream out,
   String language, String group) throws IOException
  {
    // use default charset
    InputStreamReader reader = new InputStreamReader(in);
    OutputStreamWriter writer = new OutputStreamWriter(out);
    translate(reader, writer, language, group);
  }

  public String translate(String text, String language, String group)
  {
    String translation;
    try
    {
      StringReader reader = new StringReader(text);
      StringWriter writer = new StringWriter();
      translate(reader, writer, language, group);
      translation = writer.getBuffer().toString();
    }
    catch (IOException ex)
    {
      translation = text;
    }
    return translation;
  }
}
