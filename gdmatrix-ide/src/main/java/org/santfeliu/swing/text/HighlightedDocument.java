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
package org.santfeliu.swing.text;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;

public abstract class HighlightedDocument extends DefaultStyledDocument
{
  protected long repaintInterval = 500;
  protected long nextRepaint;
  protected ArrayList tokens = new ArrayList();

  public HighlightedDocument()
  {
    addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        parse();
        forceRepaint();
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        parse();
        forceRepaint();
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        parse();
        forceRepaint();
      }
    });
  }

  public long getRepaintInterval()
  {
    return repaintInterval;
  }

  public void setRepaintInterval(long repaintInterval)
  {
    this.repaintInterval = repaintInterval;
  }

  public ArrayList getTokens()
  {
    return tokens;
  }

  public void repaintAll(Component component)
  {
    if (nextRepaint != -1 && System.currentTimeMillis() > nextRepaint)
    {
      component.repaint();
      nextRepaint = -1;
    }
  }

  protected void forceRepaint()
  {
    if (System.currentTimeMillis() > nextRepaint)
    {
      this.nextRepaint = System.currentTimeMillis() + repaintInterval;
    }
  }

  protected Token newToken(int index, int type)
  {
    Token token = new Token();
    tokens.add(token);
    token.position = index;
    token.type = type;
    return token;
  }

  protected Token newToken(int index, int type, char ch)
  {
    Token token = newToken(index, type);
    token.append(ch);
    return token;
  }

  protected abstract void parse();
}
