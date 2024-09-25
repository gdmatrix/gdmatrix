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
package org.santfeliu.doc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.santfeliu.util.script.ScriptClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

/**
 *
 * @author realor
 */
public class HtmlFixer
{
  private final String scriptName;

  public HtmlFixer(String scriptName)
  {
    this.scriptName = scriptName;
  }

  public String fixCode(String s)
  {
    try
    {
      if (s != null && !s.trim().isEmpty())
      {
        ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        fixCode(in, out);
        return out.toString();
      }
    }
    catch (Exception ex)
    {
      //Return the same string
    }
    return s;
  }

  public void fixCode(InputStream in, OutputStream out) throws Exception
  {
    Tidy tidy = new Tidy();
    tidy.setOnlyErrors(true);
    tidy.setTidyMark(false);
    org.w3c.dom.Document documentDOM = tidy.parseDOM(in, null);
    if (scriptName != null)
    {
      fixNode(documentDOM);
    }
    tidy.setSpaces(2);
    tidy.setTabsize(2);
    tidy.setIndentContent(true);
    tidy.pprint(documentDOM, out);
  }

  public void fixNode(Node node) throws Exception
  {
    applyChanges(node);
    Node child = node.getFirstChild();
    while (child != null)
    {
      fixNode(child);
      child = child.getNextSibling();
    }
  }

  private void applyChanges(Node node) throws Exception
  {
    if (node instanceof Element)
    {
      Element element = (Element)node;
      ScriptClient scriptClient = new ScriptClient();
      scriptClient.put("element", element);
      scriptClient.executeScript(scriptName);
    }
  }

  public static void main(String[] args)
  {
    try
    {
      HtmlFixer fixer = new HtmlFixer("html_fixer");

      FileInputStream in = new FileInputStream(new File("test.html"));
      FileOutputStream out = new FileOutputStream(new File("test_out.html"));
      Tidy tidy = new Tidy();
      tidy.setOnlyErrors(true);
      tidy.setTidyMark(false);
      org.w3c.dom.Document documentDOM = tidy.parseDOM(in, null);
      fixer.fixNode(documentDOM);
      tidy.pprint(documentDOM, out);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}