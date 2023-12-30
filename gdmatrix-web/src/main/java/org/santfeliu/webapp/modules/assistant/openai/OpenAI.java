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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author realor
 */
public class OpenAI
{

  static final String CHARSET = "utf-8";
  static final FunctionExecutor DEFAULT_FUNCTION_EXECUTOR =
    new DefaultFunctionExecutor();
  static final String BOUNDARY = UUID.randomUUID().toString();
  static final String EOL = "\r\n";

  String apiUrl = "https://api.openai.com";
  String apiKey;
  FunctionExecutor functionExecutor = DEFAULT_FUNCTION_EXECUTOR;
  long pollInterval = 1000;

  Gson gson = new Gson();

  public String getApiUrl()
  {
    return apiUrl;
  }

  public void setApiUrl(String apiUrl)
  {
    this.apiUrl = apiUrl;
  }

  public String getApiKey()
  {
    return apiKey;
  }

  public void setApiKey(String apiKey)
  {
    this.apiKey = apiKey;
  }

  public FunctionExecutor getFunctionExecutor()
  {
    return functionExecutor;
  }

  public void setFunctionExecutor(FunctionExecutor functionExecutor)
  {
    if (functionExecutor == null)
    {
      functionExecutor = DEFAULT_FUNCTION_EXECUTOR;
    }
    this.functionExecutor = functionExecutor;
  }

  public long getPollInterval()
  {
    return pollInterval;
  }

  public void setPollInterval(long pollInterval)
  {
    this.pollInterval = pollInterval;
  }

  public AssistantList listAssistants() throws Exception
  {
    return fetch("GET", "/v1/assistants", AssistantList.class);
  }

  public Assistant retrieveAssistant(String assistantId) throws Exception
  {
    return fetch("GET", "/v1/assistants/" + assistantId, Assistant.class);
  }

  public Assistant createAssistant(Assistant assistant) throws Exception
  {
    JsonObject request = new JsonObject();
    request.addProperty("model", assistant.getModel());
    request.addProperty("name", assistant.getName());
    request.addProperty("description", assistant.getDescription());
    request.addProperty("instructions", assistant.getInstructions());
    if (assistant.getTools() != null)
    {
      request.add("tools", gson.toJsonTree(assistant.getTools()));
    }
    if (assistant.getFileIds() != null)
    {
      request.add("file_ids", gson.toJsonTree(assistant.getFileIds()));
    }
    if (assistant.getMetadata() != null)
    {
      request.add("metadata", gson.toJsonTree(assistant.getMetadata()));
    }
    return fetch("POST", "/v1/assistants", request, Assistant.class);
  }

  public Assistant modifyAssistant(Assistant assistant) throws Exception
  {
    JsonObject request = new JsonObject();
    request.addProperty("model", assistant.getModel());
    request.addProperty("name", assistant.getName());
    request.addProperty("description", assistant.getDescription());
    request.addProperty("instructions", assistant.getInstructions());
    if (assistant.getTools() != null)
    {
      request.add("tools", gson.toJsonTree(assistant.getTools()));
    }
    if (assistant.getFileIds() != null)
    {
      request.add("file_ids", gson.toJsonTree(assistant.getFileIds()));
    }
    if (assistant.getMetadata() != null)
    {
      request.add("metadata", gson.toJsonTree(assistant.getMetadata()));
    }
    return fetch("POST", "/v1/assistants/" + assistant.getId(),
      request, Assistant.class);
  }

  public JsonObject deleteAssistant(String assistantId) throws Exception
  {
    return fetch("DELETE", "/v1/assistants/" + assistantId, JsonObject.class);
  }

  public Thread createThread() throws Exception
  {
    return fetch("POST", "/v1/threads", Thread.class);
  }

  public Thread retrieveThread(String threadId) throws Exception
  {
    return fetch("GET", "/v1/threads/" + threadId, Thread.class);
  }

  public JsonObject deleteThread(String threadId) throws Exception
  {
    return fetch("DELETE", "/v1/threads/" + threadId, JsonObject.class);
  }

  public Message createMessage(String threadId, String role, String content)
    throws Exception
  {
    JsonObject request = new JsonObject();
    request.addProperty("role", role);
    request.addProperty("content", content);
    return fetch("POST", "/v1/threads/" + threadId + "/messages",
      request, Message.class);
  }

  public MessageList listMessages(String threadId) throws Exception
  {
    return fetch("GET", "/v1/threads/" + threadId + "/messages",
      MessageList.class);
  }

  public ModelList listModels() throws Exception
  {
    return fetch("GET", "/v1/models", ModelList.class);
  }

  public Run createRun(String threadId, String assistantId)
    throws Exception
  {
    JsonObject request = new JsonObject();
    request.addProperty("assistant_id", assistantId);
    return fetch("POST", "/v1/threads/" + threadId
      + "/runs", request, Run.class);
  }

  public Run retrieveRun(Run run) throws Exception
  {
    return fetch("GET", "/v1/threads/" + run.getThreadId()
      + "/runs/" + run.getId(), Run.class);
  }

  public Run executeToolCall(Run run, ToolCall toolCall) throws Exception
  {
    String output = functionExecutor.execute(toolCall.getFunction());
    return submitToolOutputs(run, toolCall, output);
  }

  public Run submitToolOutputs(Run run,
    ToolCall toolCall, String output) throws Exception
  {
    JsonObject request = new JsonObject();
    JsonArray toolOutputs = new JsonArray();
    JsonObject toolOutput = new JsonObject();
    toolOutput.addProperty("tool_call_id", toolCall.getId());
    toolOutput.addProperty("output", output);
    toolOutputs.add(toolOutput);
    request.add("tool_outputs", toolOutputs);

    return fetch("POST", "/v1/threads/" + run.getThreadId() + "/runs/" +
      run.getId() + "/submit_tool_outputs", request, Run.class);
  }

  public FileList listFiles(String purpose) throws Exception
  {
    String params = purpose == null ? "" : "?purpose=" + purpose;
    return fetch("GET", "/v1/files" + params, FileList.class);
  }

  public File uploadFile(java.io.File file, String filename,
    String contentType, String purpose) throws Exception
  {
    HttpURLConnection conn = prepareConnection("POST", "/v1/files", true);

    try (OutputStream outputStream = conn.getOutputStream())
    {
      PrintWriter writer =
        new PrintWriter(new OutputStreamWriter(outputStream, CHARSET));

      sendFormField("purpose", purpose, writer);
      if (contentType == null)
      {
        contentType = URLConnection.guessContentTypeFromName(file.getName());
      }
      sendFilePart("file", file, filename, contentType, writer, outputStream);

      writer.append("--" + BOUNDARY + "--").append(EOL);
      writer.flush();
    }

    return readResponse(conn, File.class);
  }

  public File retrieveFile(String fileId) throws Exception
  {
    return fetch("GET", "/v1/files/" + fileId, File.class);
  }

  public java.io.File retrieveFileContent(String fileId) throws Exception
  {
    HttpURLConnection conn = prepareConnection("GET",
      "/v1/files/" + fileId + "/content");

    java.io.File file = java.io.File.createTempFile("openAI", ".bin");
    try (InputStream inputStream = conn.getInputStream())
    {
      try (FileOutputStream outputStream = new FileOutputStream(file))
      {
        IOUtils.copy(inputStream, outputStream);
      }
    }
    catch (Exception ex)
    {
      throw new IOException(readErrorMessage(conn));
    }
    return file;
  }

  public JsonObject deleteFile(String fileId) throws Exception
  {
    return fetch("DELETE", "/v1/files/" + fileId, JsonObject.class);
  }

  public Run updateRun(Run run) throws Exception
  {
    run = retrieveRun(run);
    if (run.isRequiresAction())
    {
      List<ToolCall> toolCalls =
        run.getRequiredAction().getSubmitToolOutputs().toolCalls;

      for (int i = 0; i < toolCalls.size(); i++)
      {
        ToolCall toolCall = toolCalls.get(i);
        run = executeToolCall(run, toolCall);
      }
    }
    return run;
  }

  public Run assist(String threadId, String assistantId) throws Exception
  {
    Run run = createRun(threadId, assistantId);
    while (run.isPending() || run.isRequiresAction())
    {
      if (run.isPending())
      {
        java.lang.Thread.sleep(pollInterval);
        run = retrieveRun(run);
      }
      else // requires action
      {
        List<ToolCall> toolCalls =
          run.getRequiredAction().getSubmitToolOutputs().getToolCalls();

        for (int i = 0; i < toolCalls.size(); i++)
        {
          ToolCall toolCall = toolCalls.get(i);
          run = executeToolCall(run, toolCall);
        }
      }
    }
    return run;
  }

  // private methods

  private <T> T fetch(String method, String uri, Class<T> returnClass)
    throws Exception
  {
    return fetch(method, uri, null, returnClass);
  }

  private <T> T fetch(String method, String uri, Object body,
    Class<T> returnClass) throws Exception
  {
    HttpURLConnection conn = prepareConnection(method, uri);

    T result = null;

    try
    {
      sendBody(conn, body);

      result = readResponse(conn, returnClass);
    }
    finally
    {
      conn.disconnect();
    }
    return result;
  }


  private HttpURLConnection prepareConnection(String method, String uri)
    throws Exception
  {
    return prepareConnection(method, uri, false);
  }

  private HttpURLConnection prepareConnection(String method, String uri,
    boolean multiPart) throws Exception
  {
    URL url = new URL(apiUrl + uri);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setUseCaches(false);
    conn.setRequestMethod(method);
    if (multiPart)
    {
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type",
        "multipart/form-data; boundary=" + BOUNDARY);
    }
    else
    {
      conn.setRequestProperty("Content-Type", "application/json");
    }
    conn.setRequestProperty("OpenAI-Beta", "assistants=v1");
    if (!StringUtils.isBlank(apiKey))
    {
      conn.setRequestProperty("Authorization", "Bearer " + apiKey);
    }
    return conn;
  }

  private void sendBody(HttpURLConnection conn, Object body) throws Exception
  {
    if (body != null)
    {
      conn.setDoOutput(true);

      String json = gson.toJson(body);

      try (OutputStream os = conn.getOutputStream())
      {
        byte[] bytes = json.getBytes(CHARSET);
        os.write(bytes);
      }
    }
  }

  private <T> T readResponse(HttpURLConnection conn, Class<T> returnClass)
    throws Exception
  {
    T result;
    try (InputStreamReader reader =
         new InputStreamReader(conn.getInputStream(), CHARSET))
    {
      result = gson.fromJson(reader, returnClass);
    }
    catch (Exception ex)
    {
      throw new IOException(readErrorMessage(conn));
    }
    return result;
  }

  private String readErrorMessage(HttpURLConnection conn) throws Exception
  {
    String message;
    try (InputStreamReader reader =
         new InputStreamReader(conn.getErrorStream(), CHARSET))
    {
      Error error = gson.fromJson(reader, ErrorResponse.class).getError();

      message = error.getMessage();
      if (error.getCode() != null)
      {
        message = "[ " + error.getCode() + " ] " + message;
      }
      return message;
    }
    catch (Exception ex)
    {
      message = "HTTP " + conn.getResponseCode();
      String responseMessage = conn.getResponseMessage();
      if (responseMessage != null)
      {
        message = message + " " + responseMessage;
      }
    }
    return message;
  }

  private void sendFormField(String name, String value, PrintWriter writer)
  {
    writer.append("--" + BOUNDARY).append(EOL)
      .append("Content-Disposition: form-data; name=\"")
      .append(name)
      .append('"').append(EOL)
      .append("Content-Type: text/plain; charset=" + CHARSET).append(EOL)
      .append(EOL)
      .append(value).append(EOL)
      .flush();
  }

  private void sendFilePart(String name, java.io.File uploadFile,
    String filename, String contentType, PrintWriter writer,
    OutputStream outputStream) throws IOException
  {
    writer.append("--" + BOUNDARY).append(EOL)
      .append("Content-Disposition: form-data; name=\"")
      .append(name)
      .append("\"; filename=\"")
      .append(filename)
      .append('"').append(EOL)
      .append("Content-Type: ").append(contentType).append(EOL)
      .append("Content-Transfer-Encoding: binary").append(EOL)
      .append(EOL)
      .flush();

    try (FileInputStream inputStream = new FileInputStream(uploadFile))
    {
      IOUtils.copy(inputStream, outputStream);
      outputStream.flush();
    }
    writer.append(EOL);
    writer.flush();
  }
}
