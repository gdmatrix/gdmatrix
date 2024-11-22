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
package org.santfeliu.form.builder;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;

/**
 *
 * @author realor
 */
public class PathFormBuilder extends FileFormBuilder
{
  public static final String PREFIX = "path";

  public List<FormDescriptor> findForms(String selector)
  {
    List<FormDescriptor> formDescriptors;
    File file = getFile(selector);
    if (file == null)
    {
      // return empty list
      formDescriptors = Collections.EMPTY_LIST;
    }
    else if (file.isDirectory())
    {
      formDescriptors = new ArrayList();
      File files[] = file.listFiles();
      for (File cfile : files)
      {
        if (getFormClass(cfile.getName()) != null)
        {
          FormDescriptor descriptor = new FormDescriptor();
          descriptor.setSelector(PREFIX + ":" + cfile.getAbsolutePath());
          descriptor.setTitle(getTitle(cfile.getName()));
          formDescriptors.add(descriptor);
        }
      }
    }
    else if (file.isFile())
    {
      FormDescriptor descriptor = new FormDescriptor();
      descriptor.setSelector(PREFIX + ":" + file.getAbsolutePath());
      descriptor.setTitle(getTitle(file.getName()));
      formDescriptors = Collections.singletonList(descriptor);
    }
    else
    {
      formDescriptors = Collections.EMPTY_LIST;
    }
    return formDescriptors;
  }

  public Form getForm(String selector)
  {
    Form form = null;
    File file = getFile(selector);
    if (file != null && file.isFile())
    {
      Class formClass = getFormClass(file.getName());
      if (formClass != null)
      {
        try
        {
          form = (Form)formClass.getConstructor().newInstance();
          form.read(new FileInputStream(file));
          setup(form);
        }
        catch (Exception ex)
        {
          throw new RuntimeException(ex);
        }
      }
    }
    return form;
  }

  protected File getFile(String selector)
  {
    File file = null;
    if (selector.startsWith(PREFIX + ":"))
    {
      String path = selector.substring(PREFIX.length() + 1);
      file = new File(path);
      if (!file.exists()) file = null;
    }
    return file;
  }
}
