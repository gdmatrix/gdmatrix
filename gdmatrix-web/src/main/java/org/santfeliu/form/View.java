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
package org.santfeliu.form;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author realor
 */
public interface View
{
  // view types
  static final String UNKNOWN = "UNKNOWN";
  static final String GROUP = "GROUP";
  static final String TABLE = "TABLE";
  static final String STYLE = "STYLE";
  static final String TEXT = "TEXT";
  static final String LABEL = "LABEL";
  static final String TEXTFIELD = "TEXTFIELD";
  static final String PASSWORDFIELD = "PASSWORDFIELD";
  static final String RADIO = "RADIO";
  static final String CHECKBOX = "CHECKBOX";
  static final String LIST = "LIST";
  static final String ITEM = "ITEM";
  static final String SELECT = "SELECT";
  static final String SLIDER = "SLIDER";
  static final String CALENDAR = "CALENDAR";
  static final String BUTTON = "BUTTON";

  String getId();
  String getReference();
  String getViewType();
  View getParent();
  List<View> getChildren();
  Object getProperty(String name);
  Collection<String> getPropertyNames();
  public String getNativeViewType();
}
