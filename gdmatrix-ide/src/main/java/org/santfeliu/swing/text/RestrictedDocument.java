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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author realor
 */
public class RestrictedDocument extends PlainDocument
{
  private int maxLength = 100;
  private String pattern = ".*";

  public RestrictedDocument()
  {
  }

  public RestrictedDocument(String pattern)
  {
    this.pattern = pattern;
  }

  public RestrictedDocument(String pattern, int maxLength)
  {
    this.pattern = pattern;
    this.maxLength = maxLength;
  }

  public void insertString(int offset, String part2, AttributeSet set)
    throws BadLocationException
  {
    String part1 = getText(0, offset);
    String part3 = getText(offset, getLength() - offset);
    String newValue = part1 + part2 + part3;
    if (isValid(newValue))
    {
      super.insertString(offset, part2, set);
    }
  }

  public void setMaxLength(int maxLength)
  {
    this.maxLength = maxLength;
  }

  public int getMaxLength()
  {
    return maxLength;
  }

  public void setPattern(String pattern)
  {
    this.pattern = pattern;
  }

  public String getPattern()
  {
    return pattern;
  }

  protected boolean isValid(String newValue)
  {
    return newValue.matches(pattern) && newValue.length() < maxLength;
  }
}
