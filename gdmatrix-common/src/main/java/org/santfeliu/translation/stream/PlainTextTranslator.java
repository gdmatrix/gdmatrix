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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.santfeliu.translation.StringTranslator;
import org.santfeliu.translation.string.DebugStringTranslator;
import org.santfeliu.translation.util.Normalizer;

/**
 *
 * @author realor
 */
public class PlainTextTranslator extends TextTranslator
{
  @Override
  public void translate(Reader reader, Writer writer,
    String language, String group) throws IOException
  {
    Normalizer normalizer = new Normalizer();
    int ch = reader.read();
    while (ch != -1)
    {
      char chr =(char)ch;
      boolean newLineChar = isNewLineCharacter(chr);
      boolean punctuationChar = isPunctuationCharacter(chr);
      if ((newLineChar || punctuationChar) && !normalizer.insideMark())
      {
        // break can be performed
        if (punctuationChar)
        {
          normalizer.append(chr);
        }
        writer.write(translateNormalizer(normalizer, language, group));
        if (newLineChar)
        {
          writer.write(chr);
        }
      }
      else normalizer.append(chr);
      ch = reader.read();
    }
    writer.write(translateNormalizer(normalizer, language, group));
    writer.flush();
  }

  protected String translateNormalizer(Normalizer normalizer, 
    String language, String group)
  {
    String normalizedText = normalizer.end();
    if (normalizedText.length() > 0)
    {
      String normalizedTranslation =
        stringTranslator.translate(normalizedText, language, group);
      if (normalizedTranslation == null)
        normalizedTranslation = normalizedText;
      normalizer.append(normalizedTranslation);
      return normalizer.end();
    }
    else normalizer.reset();
    return "";
  }

  protected boolean isNewLineCharacter(char ch)
  {
    return ch == '\n' || ch == '\r';
  }

  protected boolean isPunctuationCharacter(char ch)
  {
    return ch == '?' || ch == '!' || ch == '.';
  }

  public static void main(String[] args)
  {
    try
    {
      StringTranslator st;      
      st = new DebugStringTranslator();

      PlainTextTranslator translator = new PlainTextTranslator();
      translator.setStringTranslator(st);
      translator.translate(
        new File("c:/in.txt"),
        new File("c:/out.txt"),
        "en", "test");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
