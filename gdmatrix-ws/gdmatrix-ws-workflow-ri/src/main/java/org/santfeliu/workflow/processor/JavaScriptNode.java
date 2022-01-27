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
package org.santfeliu.workflow.processor;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.script.ScriptableBase;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.WorkflowActor;
import org.santfeliu.workflow.WorkflowInstance;


/**
 *
 * @author realor
 */
public class JavaScriptNode extends org.santfeliu.workflow.node.JavaScriptNode
  implements NodeProcessor
{
  @Override
  public String process(WorkflowInstance instance, WorkflowActor actor)
    throws Exception
  {
    Thread thread = Thread.currentThread();
    String threadName = thread.getName();

    Context cx = ContextFactory.getGlobal().enterContext();
    Object result = null;
    try
    {
      thread.setName("workflow:" + instance.getWorkflowName()
        + ":" + getId() + ":" + instance.getInstanceId());

      String mcode = Template.create(code).merge(instance);
      Scriptable scope = new ScriptableBase(cx, instance);
      scope.put("actor", scope, actor);
      result = cx.evaluateString(scope, mcode, "<code>", 1, null);
    }
    finally
    {
      thread.setName(threadName);
      Context.exit();
    }
    return String.valueOf(result);
  }
}