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
package org.santfeliu.webapp.modules.assistant.test;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.FinishReason;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.santfeliu.webapp.modules.assistant.langchain4j.Assistant;
import static org.santfeliu.webapp.modules.assistant.langchain4j.Assistant.OLLAMA_PROVIDER;
import org.santfeliu.webapp.modules.assistant.langchain4j.ChatMessageListener;

/**
 *
 * @author realor
 */
public class AssistantTest
{

  public static void main(String[] args) throws Exception
  {
    Assistant assistant = new Assistant();
    assistant.setProvider(OLLAMA_PROVIDER);
    assistant.setName("Rosa");
    assistant.setBaseUrl("http://sia.santfeliu.local:11434");
    assistant.setModelName("llama3");

    System.out.println(">");

    List<ChatMessage> messages = new ArrayList<>();

    try (Scanner scanner = new Scanner(System.in))
    {
      while (true)
      {
        String userQuery = scanner.nextLine();

        if ("exit".equalsIgnoreCase(userQuery))
        {
          break;
        }

        messages.add(UserMessage.from(userQuery));
        System.out.println(messages);

        assistant.generate(messages, new ChatMessageListener()
        {
          @Override
          public void onNext(String token)
          {
            System.out.println(token);
          }

          @Override
          public void onMessage(ChatMessage message)
          {
            messages.add(message);
            System.out.println("\n>>>" + message);
          }

          @Override
          public void onComplete(FinishReason reason)
          {
            System.out.println("\n>>>END");
          }

          @Override
          public void onError(Throwable t)
          {
            System.out.println("\nERROR:" + t);
          }
        });
      }
    }
  }
}
