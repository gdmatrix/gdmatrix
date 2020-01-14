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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import org.santfeliu.translation.StringTranslator;
import org.santfeliu.translation.string.DebugStringTranslator;
import org.santfeliu.translation.util.Normalizer;
import org.santfeliu.util.enc.HtmlDecoder;
import org.santfeliu.util.enc.HtmlEncoder;

/**
 *
 * @author Administrador
 */
public class HtmlTranslator extends TextTranslator
{
  public final HashSet nonBreakingTags = new HashSet();
  public final HashSet nonTranslatingTags = new HashSet();
  public final HashSet translatingAttributes = new HashSet();

  public HtmlTranslator()
  {
    nonBreakingTags.add("b");
    nonBreakingTags.add("u");
    nonBreakingTags.add("i");
    nonBreakingTags.add("span");
    nonBreakingTags.add("font");
    nonBreakingTags.add("strong");
    nonBreakingTags.add("a");

    nonTranslatingTags.add("script");
    nonTranslatingTags.add("style");
    nonTranslatingTags.add("textarea");
    
    translatingAttributes.add("alt");
    translatingAttributes.add("title");
    translatingAttributes.add("aria-label");
  }

  public Set getNonBreakingTags()
  {
    return nonBreakingTags;
  }

  public void setNonBreakingTags(Set tags)
  {
    nonBreakingTags.clear();
    nonBreakingTags.addAll(tags);
  }

  public HashSet getNonTranslatingTags()
  {
    return nonTranslatingTags;
  }

  public void setNonTranslatingTags(Set tags)
  {
    nonTranslatingTags.clear();
    nonTranslatingTags.addAll(tags);
  }

  public void translate(Reader reader, Writer writer,
    String language, String group) throws IOException
  {
    StringBuilder symbolBuffer = new StringBuilder();
    StringBuilder tagBuffer = new StringBuilder();
    boolean insideAttribute = false;
    char attrChr = '"';
    Normalizer normalizer = new Normalizer();
    String insideNonTranslatingTag = null;
    
    int ch = reader.read();
    while (ch != -1)
    {
      char chr = (char)ch;
      if (tagBuffer.length() > 0) // inside tag
      {
        tagBuffer.append(chr);
        if (chr == '>' && !insideAttribute)
        {
          String tag = tagBuffer.toString();
          boolean closeTag = tag.startsWith("</") || tag.endsWith("/>");
          boolean openTag = !closeTag;
          String tagName = getTagName(tag);
          if (insideNonTranslatingTag != null)
          {
            // inside insideNonTranslatingTag: script, textarea, style
            writer.write(tag);
            if (closeTag && insideNonTranslatingTag.equals(tagName))
            {
              insideNonTranslatingTag = null;
            }
          }
          else // not inside insideNonTranslatingTag: script, textarea, style
          {
            if (isNonTranslatingTag(tagName))
            {
              writer.write(translateNormalizer(normalizer, language, group));
              writer.write(tag);
              if (openTag)
              {
                // enter in non translating tag
                insideNonTranslatingTag = tagName;
              }
            }
            else if (isNonBreakingTag(tagName))
            {
              normalizer.append(Normalizer.OPEN_TAG);
              normalizer.append(translateTag(tag, language, group));
              normalizer.append(Normalizer.CLOSE_TAG);
            }
            else // breaking tag
            {
              writer.write(translateNormalizer(normalizer, language, group));
              writer.write(translateTag(tag, language, group));
            }
          }
          tagBuffer.setLength(0);
        }
        else if (chr == 34 || chr == 39)
        {
          if (!insideAttribute)
          {
            attrChr = chr;
            insideAttribute = true;
          }
          else
          {
            if ((chr == 34 && attrChr == 34) || (chr == 39 && attrChr == 39))
            {
              insideAttribute = false;
            }
          }
        }
      }
      else // outside tag
      {
        if (chr == '<')
        {
          tagBuffer.append(chr);
          symbolBuffer.setLength(0);
        }
        else if (chr == '&')
        {
          symbolBuffer.append(chr);
        }
        else if (symbolBuffer.length() > 0)
        {
          if (chr == ';')
          {
            symbolBuffer.append(chr);
            String symbol = symbolBuffer.toString();
            if (insideNonTranslatingTag == null) // translate
            {
              //char schr = HtmlDecoder.decode(symbol).charAt(0);
              normalizer.append(HtmlDecoder.decode(symbol));
            }
            else writer.write(symbol); // do not translate
            symbolBuffer.setLength(0);
          }
          else symbolBuffer.append(chr);
        }
        else if (insideNonTranslatingTag == null) // translate
        {
          // replace newLine characters with whitespace
          if (chr == '\r' || chr == '\n') chr = ' ';
          normalizer.append(chr);
        }
        else // inside non translating tag
        {
          writer.write(chr);
        }
      }
      ch = reader.read();
    }
    if (normalizer.length() > 0)
    {
      writer.write(translateNormalizer(normalizer, language, group));
    }
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
      normalizedTranslation = HtmlEncoder.encode(normalizedTranslation);
      normalizer.append(normalizedTranslation);
      return normalizer.end();
    }
    else normalizer.reset();
    return "";
  }
  
  protected String translateTag(String tag, String language, String group)
    throws IOException
  {
    char terminator = '"';
    int index = tag.indexOf(" ");
    if (index == -1)
    {
      return tag;
    }
    StringBuilder output = new StringBuilder();
    StringBuilder buffer = new StringBuilder();
    Normalizer normalizer = new Normalizer();
    String attrName = null;
    String attrValue;
    output.append(tag.substring(0, index));
    int state = 0;
    while (index < tag.length())
    {
      char ch = (char)tag.charAt(index);
      switch (state)
      {
        case 0: // skip blanks and other symbols
          if (ch == ' ' || ch == '/' || ch == '>')
          {
            output.append(ch);
          }
          else
          {
            buffer.append(ch);
            state = 1;
          }
          break;
        case 1: // read attribute name
          if (ch == '=')
          {
            attrName = buffer.toString().toLowerCase().trim();
            buffer.setLength(0);
            state = 2;
          }
          else if (Character.isLetter(ch) || ch == '_' || ch == '-')
          {
            buffer.append(ch);
          }
          else // ignore attribute
          {
            buffer.append(ch);
            output.append(buffer.toString());
            buffer.setLength(0);
            state = 0;
          }
          break;
        case 2: // look for starting " or '
          if (ch == ' ')
          {
            // skip
          }
          else if (ch == '"' || ch == '\'')
          {
            terminator = ch;
            state = 3;
          }
          break;   
        case 3: // look for ending " or '
          if (ch == terminator)
          {
            attrValue = buffer.toString();
            buffer.setLength(0);
            output.append(attrName).append("=\"");
            if (translatingAttributes.contains(attrName))
            {
              System.out.println(">>>> TRANSLATING ATTR: [" + 
                attrName + "], [" + attrValue + "]");
              normalizer.append(attrValue);
              output.append(translateNormalizer(normalizer, language, group));
            }
            else
            {
              output.append(attrValue);
            }
            output.append('"');
            state = 0;
          }
          else
          {
            buffer.append((char)ch);
          }
          break;
      }
      index++;
    }
    return output.toString();
  }

  protected String getTagName(String tag)
  {
    String tagName = tag.toLowerCase();
    tagName = tagName.substring(1); // skip <
    if (tagName.startsWith("/"))
      tagName = tagName.substring(1); // skip slash /
    int index = tagName.indexOf(' ');
    if (index != -1)
    {
      tagName = tagName.substring(0, index);
    }
    else if (tagName.endsWith("/>"))
    {
      tagName = tagName.substring(0, tagName.length() - 2);
    }
    else
    {
      tagName = tagName.substring(0, tagName.length() - 1);
    }
    return tagName;
  }

  protected boolean isNonBreakingTag(String tagName)
  {
    return nonBreakingTags.contains(tagName);
  }

  protected boolean isNonTranslatingTag(String tagName)
  {
    return nonTranslatingTags.contains(tagName);
  }

  public static void main(String[] args) throws FileNotFoundException
  {
    try
    {
      HtmlTranslator tr = new HtmlTranslator();
      StringTranslator st;

      st = new DebugStringTranslator();
      //st = new GoogleStringTranslator();
      
      tr.setStringTranslator(st);
      tr.translate(
        new File("c:/Users/realor/Desktop/test.html"),
        new File("c:/Users/realor/Desktop/test_out.html"), "zh", "test");

//      Tidy tidy = new Tidy();
//      tidy.setOnlyErrors(true);
//      // parse document with Tidy
//      tidy.parseDOM(new FileInputStream(new File("c:/out.htm")), null);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
