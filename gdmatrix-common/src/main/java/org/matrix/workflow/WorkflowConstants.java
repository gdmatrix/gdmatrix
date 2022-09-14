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
package org.matrix.workflow;

/**
 *
 * @author realor
 */
public class WorkflowConstants
{
  // variable types
  public static final String TEXT_TYPE = "T";
  public static final String NUMBER_TYPE = "N";
  public static final String BOOLEAN_TYPE = "B";

  // special instance variables
  public static final String WORKFLOW_NAME = "WORKFLOW_NAME";
  public static final String WORKFLOW_VERSION = "WORKFLOW_VERSION";
  public static final String INSTANCE_ID = "INSTANCE_ID";
  public static final String ACTIVE_NODES = "ACTIVE_NODES";
  public static final String AGENT_NAME = "AGENT_NAME";
  public static final String DESCRIPTION = "DESCRIPTION";
  public static final String STATE = "STATE";
  public static final String ERRORS = "ERRORS";
  public static final String START_DATE_TIME = "START_DATE_TIME";
  public static final String SIMULATION = "SIMULATION";
  public static final String EXIT_BUTTON_ENABLED = "EXIT_BUTTON_ENABLED";
  public static final String DESTROY_BUTTON_ENABLED = "DESTROY_BUTTON_ENABLED";
  public static final String TRANSLATION_ENABLED = "TRANSLATION_ENABLED";
  public static final String HELP_BUTTON_URL = "HELP_BUTTON_URL";
  public static final String HEADER_FORM = "HEADER_FORM";
  public static final String TERMINATION_MESSAGE = "TERMINATION_MESSAGE";
  public static final String TERMINATION_ICON = "TERMINATION_ICON";
  public static final String FAIL_MESSAGE = "FAIL_MESSAGE";
  public static final String FAIL_ICON = "FAIL_ICON";
  public static final String TERMINATION_FORM = "TERMINATION_FORM";
  public static final String FAIL_FORM = "FAIL_FORM";
  public static final String INVOKER_INSTANCE_ID = "INVOKER_INSTANCE_ID";
  public static final String INVOKER_NODE_ID = "INVOKER_NODE_ID";
  public static final String FORM_RENDERERS = "FORM_RENDERERS";

  // variable prefixes
  public static final String FORM_PREFIX = "FORM_";
  public static final String INVOCATION_PREFIX = "INVOKE_";
  public static final String WAIT_PREFIX = "WAIT_";
  public static final String ERROR_PREFIX = "ERROR_";
  public static final String READ_ACCESS_PREFIX = "ACCESS_READ_";
  public static final String WRITE_ACCESS_PREFIX = "ACCESS_WRITE_";
  public static final String UPDATE_ONLY_PREFIX = "UPDATE_ONLY_";

  // variable values
  public static final String ANY_AGENT = "ANY_AGENT";

  // Form specific contants
  public static final String SHOW_STATE = "show";
  public static final String FORWARD_STATE = "forward";
  public static final String BACKWARD_STATE = "backward";
  public static final String FORWARD_BUTTON_ENABLED = "F";
  public static final String BACKWARD_BUTTON_ENABLED = "B";
  public static final String FORM_PARAMETERS_SEPARATOR = " :: ";

  // roles
  public static final String WORKFLOW_ADMIN_ROLE = "WF_ADMIN";
  public static final String WORKFLOW_AGENT_ROLE = "WF_AGENT";
}
