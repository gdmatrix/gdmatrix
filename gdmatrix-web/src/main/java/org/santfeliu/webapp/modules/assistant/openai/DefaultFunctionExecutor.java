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
package org.santfeliu.webapp.modules.assistant.openai;

/**
 *
 * @author realor
 */
public class DefaultFunctionExecutor implements FunctionExecutor
{
  @Override
  public String execute(Function function)
  {
    String functionName = function.getName();
    String arguments = function.getArguments();
    System.out.println("CALL " + functionName + " " + arguments);
    switch (functionName)
    {
      case "get_question_status":
        return "Hem procedit a substituir el fanal trencat tal com es havia demanat.";

      case "get_questions_by_nif":
        return "[\"question_id:\": \"42351\", \"subject\": \"Fanal trencat.\"]";
    }
    return "Funció no reconeguda";
  }
}