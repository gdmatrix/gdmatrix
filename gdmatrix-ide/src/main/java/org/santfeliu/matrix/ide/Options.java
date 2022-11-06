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
package org.santfeliu.matrix.ide;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Enumeration;
import java.util.Properties;
import org.santfeliu.matrix.MatrixInfo;

/**
 *
 * @author realor
 */
public class Options
{
  private static final Properties properties;
  private static Font editorFont = new Font("Monospaced", Font.PLAIN, 14);
  private static File lastDirectory = new File(".");
  private static int indentSpaces = 2;

  static
  {
    properties = new Properties();
  }

  public static String get(String key)
  {
    return properties.getProperty(key);
  }

  public static void set(String key, String newValue)
  {
    if (newValue != null)
    {
      properties.setProperty(key, newValue);
    }
    else
    {
      properties.remove(key);
    }
  }

  public static Font getEditorFont()
  {
    return Options.editorFont;
  }

  public static void setEditorFont(Font editorFont)
  {
    Options.editorFont = editorFont;
  }

  public static File getLastDirectory()
  {
    return Options.lastDirectory;
  }

  public static void setLastDirectory(File lastDirectory)
  {
    Options.lastDirectory = lastDirectory;
  }

  public static int getIndentSpaces()
  {
    return indentSpaces;
  }

  public static void setIndentSpaces(int indentSpaces)
  {
    Options.indentSpaces = indentSpaces;
  }

  public static void load(InputStream is) throws IOException
  {
    // read properties file
    try
    {
      properties.load(is);
    }
    finally
    {
      is.close();
    }

    // read special properties
    String dir = properties.getProperty("dir");
    if (dir != null) Options.lastDirectory = new File(dir);

    String fontString = properties.getProperty("font");
    if (fontString != null) editorFont = Font.decode(fontString);

    try
    {
      indentSpaces = Integer.parseInt(properties.getProperty("indentSpaces"));
    }
    catch (NumberFormatException ex)
    {
      indentSpaces = 2;
    }
  }

  public static void save(OutputStream os) throws IOException
  {
    // write special properties
    if (lastDirectory != null)
      properties.setProperty("dir", lastDirectory.getAbsolutePath());

    properties.setProperty("revision", MatrixInfo.getRevision());

    if (editorFont != null)
    {
      String fontString = editorFont.getName() + "-" +
       (editorFont.isBold() ?
         (editorFont.isItalic() ? "bolditalic" : "bold") :
         (editorFont.isItalic() ? "italic" : "plain")) +
         "-" + editorFont.getSize();
      properties.setProperty("font", fontString);
    }

    properties.setProperty("indentSpaces", String.valueOf(indentSpaces));

    // store properties file
    try
    {
      properties.store(os, "MatrixIDE options");
    }
    finally
    {
      os.close();
    }
  }

  public static void clear()
  {
    properties.clear();
  }

  public static Enumeration listKeys()
  {
    return properties.keys();
  }
}