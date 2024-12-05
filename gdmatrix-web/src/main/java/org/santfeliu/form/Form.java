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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author realor
 */
public interface Form
{
  // Description
  String getId();
  String getTitle();
  String getLanguage();
  Object getProperty(String name);
  Collection<String> getPropertyNames();

  // Model
  Collection<Field> getFields(); // input and output fields (read only)
  Field getField(String reference);

  // View
  View getRootView();
  View getView(String reference);

  // Validation
  boolean validate(String reference, Object value, List errors, Locale locale);
  boolean validate(Map data, List errors, Locale locale);

  // Evaluation
  Form evaluate(Map context) throws Exception;
  boolean isEvaluated();
  boolean isContextDependant();

  // Change control
  boolean isOutdated();
  String getLastModified();
  void setLastModified(String date);

  // submit
  void submit(Map data);

  // Persistence
  Map read(InputStream is) throws IOException;
  void write(OutputStream os, Map data) throws IOException;
}
