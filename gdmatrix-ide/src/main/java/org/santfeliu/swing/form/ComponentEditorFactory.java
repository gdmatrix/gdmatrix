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
package org.santfeliu.swing.form;

import org.santfeliu.swing.form.editor.InputTextEditor;
import org.santfeliu.swing.form.editor.LabelEditor;
import org.santfeliu.swing.form.editor.OutputTextAreaEditor;
import org.santfeliu.swing.form.editor.OutputTextEditor;
import org.santfeliu.swing.form.editor.SelectBoxEditor;
import org.santfeliu.swing.form.view.InputTextView;
import org.santfeliu.swing.form.view.LabelView;
import org.santfeliu.swing.form.view.OutputTextAreaView;
import org.santfeliu.swing.form.view.OutputTextView;
import org.santfeliu.swing.form.view.SelectBoxView;

/**
 *
 * @author realor
 */
public class ComponentEditorFactory
{
  public static ComponentEditor getComponentEditor(ComponentView view)
  {
    ComponentEditor editor = null;
    if (view instanceof LabelView)
    {
      editor = new LabelEditor();
    }
    else if (view instanceof OutputTextView)
    {
      editor = new OutputTextEditor();
    }
    else if (view instanceof OutputTextAreaView)
    {
      editor = new OutputTextAreaEditor();
    }
    else if (view instanceof InputTextView)
    {
      editor = new InputTextEditor();
    }
    else if (view instanceof SelectBoxView)
    {
      editor = new SelectBoxEditor();
    }
    return editor;
  }
}
