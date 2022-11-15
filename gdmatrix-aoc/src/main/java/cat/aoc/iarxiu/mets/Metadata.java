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
package cat.aoc.iarxiu.mets;

import java.util.List;
import org.w3c.dom.Element;

/**
 *
 * @author blanquepa
 */

public abstract class Metadata
{
  protected String id;
  protected String type;

  protected void setId(String id)
  {
    this.id = id;
  }

  protected String getId()
  {
    return id;
  }

  protected void setType(String type)
  {
    this.type = type;
  }

  protected String getType()
  {
    return type;
  }

  @Override
  public String toString()
  {
    return id + "(" + type + ")";
  }

  public abstract void setProperty(String name, String value) throws Exception;

  public abstract String getPropertyValue(String name) throws Exception;

  public abstract List<String> getPropertyNames();

  protected abstract void load(Element mdWrapElement, Mets mets) throws Exception;

  protected abstract void write(Element mdWrapElement, Mets mets) throws Exception;
}
