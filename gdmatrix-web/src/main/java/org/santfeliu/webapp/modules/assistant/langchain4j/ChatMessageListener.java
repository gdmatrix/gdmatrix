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
package org.santfeliu.webapp.modules.assistant.langchain4j;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.model.output.FinishReason;

/**
 *
 * @author realor
 */
public interface ChatMessageListener
{
  /**
   * Called when new tokens are received
   * 
   * @param tokens - tokens returned by the model.
   * @param handle - handle to cancel streaming or null if this is not 
   * a streaming response.
   */
  default void onNext(String tokens, StreamingHandle handle) {}  
  
  /**
   * Called when a completed message is received
   * 
   * @param message - the AI message
   */
  default void onMessage(ChatMessage message) {}

  /**
   * Called when the model asks to execute a tool
   * 
   * @param toolRequest - the tool execution request
   * @return the tool result
   */
  default String onExecute(ToolExecutionRequest toolRequest)
  {
    return "Not implemented.";
  }

  /**
   * Called on error
   * @param t - the error produced
   */
  default void onError(Throwable t) {}

  /**
   * Called when the model finish the response
   * 
   * @param reason - the finish reason
   */
  default void onComplete(FinishReason reason) {}
}

