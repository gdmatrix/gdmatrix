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
package org.santfeliu.webapp.modules.ide;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author granadogj
 */
public class AiRepairUtils
{

  private static final Logger LOGGER = Logger.getLogger(AiRepairUtils.class.getName());

  // Inicialization of the AI Model
  private static final OllamaChatModel model = OllamaChatModel.builder()
    .baseUrl("http://10.30.30.1:11434/")
    .modelName("qwen3.5:27b")
    .timeout(Duration.ofMinutes(2)) // 2 min max timeout
    .temperature(0.1)
    .build();

  public static String processForm(String oldForm, String systemPrompt, String userPrompt)
  {
    // Build messages
    List<ChatMessage> messages = buildMessages(oldForm, systemPrompt, userPrompt);
    
    // Create request
    ChatRequest chatRequest = ChatRequest.builder()
      .messages(messages)
      .temperature(0.1)
      .build();

    // Call the model
    try
    {
      ChatResponse chatResponse = model.chat(chatRequest);
      String aiMessage = chatResponse.aiMessage().text();
      LOGGER.info("AI Response received");

      return cleanOutput(aiMessage, oldForm);
    }
    catch (Exception e)
    {
      LOGGER.log(Level.SEVERE, "Error connecting to AI Model", e);
      return oldForm; // Fallback
    }
  }

  private static String buildSystemPrompt(String systemPrompt, String userPrompt)
  {
    String prompt = "/nothink " + systemPrompt;
    if (userPrompt != null && !userPrompt.trim().isEmpty())
    {
      prompt += "\nAdditional instructions:\n" + userPrompt.trim();
    }
    return prompt;
  }
  
  private static List<ChatMessage> buildMessages (String oldForm, String systemPrompt, String userPrompt)
  {
    List<ChatMessage> messages = new ArrayList<>();
    // System prompt
    messages.add(SystemMessage.from(buildSystemPrompt(systemPrompt, userPrompt)));
    // User prompt
    messages.add(UserMessage.from("### INPUT LEGACY CODE:\n" +oldForm));
    
    return messages;
  }
  // Clean the ai response, we only want the code
  private static String cleanOutput(String aiMessage, String fallback)
  {
    if (aiMessage == null || aiMessage.isBlank()) return fallback;
    
    aiMessage = aiMessage.trim();

    // Pattern to get only the markdown content
    Pattern markdownPattern = Pattern.compile("```(?:html)?\\s*(.*?)\\s*```", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    Matcher markdownMatcher = markdownPattern.matcher(aiMessage);

    if (markdownMatcher.find())
    {
      // If it has a block of code, we extract group 1 (what’s inside)
      return markdownMatcher.group(1).trim();
    }
    else
    {
      // If no Markdown search for DOCTYPE
      Pattern htmlPattern = Pattern.compile("<!DOCTYPE[\\s\\S]*</html>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
      Matcher htmlMatcher = htmlPattern.matcher(aiMessage);

      if (htmlMatcher.find())
      {
        return htmlMatcher.group(0).trim();
      }
    }
    // If doesn't find anything, return oldForm
    return aiMessage.length() > 0 ? aiMessage : fallback;
  }
}
