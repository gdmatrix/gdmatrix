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
package org.santfeliu.workflow.node;

import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;

/**
 *
 * @author realor
 */
public class FormNode extends WorkflowNode
{
  public static final String FORWARD_OUTCOME = "forward";
  public static final String BACKWARD_OUTCOME = "backward";
  public static final String FORM_FORWARD = "forward";

  protected String formType = "";
  protected String group = "0";
  protected boolean forwardEnabled = true;
  protected boolean backwardEnabled = true;
  protected Properties parameters = new Properties();
  protected String viewRoles;
  protected String editRoles;
  protected String readVariables;
  protected String writeVariables;
  protected String checkExpression;
  protected String cancelExpression;
  protected String outcomeExpression;
  protected String actorVariable;

  public FormNode()
  {
  }

  @Override
  public String getType()
  {
    return "Form";
  }

  public void setFormType(String formType)
  {
    this.formType = formType;
  }

  public String getFormType()
  {
    return formType;
  }

  public void setGroup(String group)
  {
    this.group = group;
  }

  public String getGroup()
  {
    return group;
  }

  public void setForwardEnabled(boolean forwardEnabled)
  {
    this.forwardEnabled = forwardEnabled;
  }

  public boolean isForwardEnabled()
  {
    return forwardEnabled;
  }

  public void setBackwardEnabled(boolean backwardEnabled)
  {
    this.backwardEnabled = backwardEnabled;
  }

  public boolean isBackwardEnabled()
  {
    return backwardEnabled;
  }

  public void setParameters(Properties parameters)
  {
    this.parameters = parameters;
  }

  public Properties getParameters()
  {
    return parameters;
  }

  public void setReadVariables(String readVariables)
  {
    this.readVariables = readVariables;
  }

  public String getReadVariables()
  {
    return readVariables;
  }

  public void setWriteVariables(String writeVariables)
  {
    this.writeVariables = writeVariables;
  }

  public String getWriteVariables()
  {
    return writeVariables;
  }

  public String getActorVariable()
  {
    return actorVariable;
  }

  public void setActorVariable(String actorVariable)
  {
    this.actorVariable = actorVariable;
  }

  public String getViewRoles()
  {
    return viewRoles;
  }

  public void setViewRoles(String viewRoles)
  {
    this.viewRoles = viewRoles;
  }

  public String getEditRoles()
  {
    return editRoles;
  }

  public void setEditRoles(String editRoles)
  {
    this.editRoles = editRoles;
  }

  public void setCheckExpression(String checkExpression)
  {
    this.checkExpression = checkExpression;
  }

  public String getCheckExpression()
  {
    return checkExpression;
  }

  public void setCancelExpression(String cancelExpression)
  {
    this.cancelExpression = cancelExpression;
  }

  public String getCancelExpression()
  {
    return cancelExpression;
  }

  public void setOutcomeExpression(String outcomeExpression)
  {
    this.outcomeExpression = outcomeExpression;
  }

  public String getOutcomeExpression()
  {
    return outcomeExpression;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    FormNode newNode = (FormNode)super.clone();
    newNode.parameters = (Properties)this.parameters.clone();
    return newNode;
  }

}
