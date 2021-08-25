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
package org.santfeliu.form.type.html;

import org.santfeliu.form.Field;

/**
 *
 * @author realor
 */
public class HtmlField implements Field, Cloneable
{
  private String reference;
  private String label;
  private String type;
  private boolean readOnly;
  private int minOccurs;
  private int maxOccurs;

  @Override
  public String getReference()
  {
    return reference;
  }

  public void setReference(String reference)
  {
    this.reference = reference;
  }

  @Override
  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  @Override
  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  @Override
  public boolean isReadOnly()
  {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly)
  {
    this.readOnly = readOnly;
  }

  @Override
  public int getMinOccurs()
  {
    return minOccurs;
  }

  public void setMinOccurs(int minOccurs)
  {
    this.minOccurs = minOccurs;
  }

  @Override
  public int getMaxOccurs()
  {
    return maxOccurs;
  }

  public void setMaxOccurs(int maxOccurs)
  {
    this.maxOccurs = maxOccurs;
  }

  @Override
  public Object getNativeField()
  {
    return null;
  }

  public boolean isRequired()
  {
    return minOccurs > 0;
  }

  @Override
  public String toString()
  {
    return "HtmlField[" + reference + "] " + label;
  }

  @Override
  public HtmlField clone()
  {
    HtmlField newField = new HtmlField();
    newField.reference = reference;
    newField.label = label;
    newField.type = type;
    newField.minOccurs = minOccurs;
    newField.maxOccurs = maxOccurs;
    newField.readOnly = readOnly;
    return newField;
  }
}
