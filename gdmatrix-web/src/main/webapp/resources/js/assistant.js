/* assistant.js */

function updateAssistantButtons()
{
  var text = PF("assistantTextarea").getJQ().val().trim();
  var sendButton = PF("assistantSendButton");
  var interruptButton = PF("assistantInterruptButton");
  var uploadButton = PF("assistantUploadButton");

  var dotsElem = document.querySelector(".message_list .dot-typing");
  var inProgress = dotsElem && !dotsElem.classList.contains("invisible");

  if (inProgress)
  {
    sendButton.disable();
    uploadButton.disable();
  }
  else
  {
    if (text.length === 0)
    {
      sendButton.disable();
    }
    else
    {
      sendButton.enable();
    }
    uploadButton.enable();
    sendButton.jq.show();
    interruptButton.jq.hide();
  }
}

function createMessage(role, markdown = "")
{
  let icon;
  switch (role)
  {
    case "USER": icon = "pi pi-user"; break;
    case "AI": icon = "mi-outlined mi-smart-toy"; break;
    case "TOOL_EXECUTION_RESULT": icon = "pi pi-cog"; break;
    default: icon = "pi pi-cog"; break;
  }

  let roleLabel = role;
  if (role === "AI") roleLabel = aiLabel || role;
  else if (role === "USER") roleLabel = userLabel || role;

  const itemElem = document.createElement("li");
  itemElem.innerHTML =
  `
    <div class="flex m-2 mb-4 message ${role}">
      <div class="flex-grow-0">
        <div class="avatar">
          <span class="${icon}" />
        </div>
      </div>
      <div class="flex-grow-1 flex flex-column ml-2 overflow-x-hidden">
        <div>
          <span class="role">${roleLabel}</span>
        </div>
        <div class="content mt-1">
          <div class="markdown hidden">${markdown}</div>
          <div class="html">${markdownToHtml(markdown, false)}</div>
        </div>
      </div>
    </div>
  `;
  return itemElem;
}

function showDots()
{
  const dotsElem = document.querySelector(".message_list .dot-typing");
  if (dotsElem) dotsElem.classList.remove("invisible");
}

function hideDots()
{
  const dotsElem = document.querySelector(".message_list .dot-typing");
  if (dotsElem) dotsElem.classList.add("invisible");
}

function setLinkTarget(htmlElem)
{
  const links = htmlElem.querySelectorAll("a");
  for (let link of links)
  {
    link.target = "_blank";
  }
}

function fileToBase64(file) 
{
  return new Promise((resolve, reject) => 
  {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
    reader.readAsDataURL(file);
  });
}

async function assistantPaste(event)
{
  console.info(event);
  const items = (event.clipboardData || event.originalEvent.clipboardData).items;

  for (const item of items) 
  {
    if (item.type.indexOf("image") === 0)
    {
      const pastedFile = item.getAsFile();
      console.info(pastedFile);
      let base64 = await fileToBase64(pastedFile);
      uploadImage([{ name: 'image', value: base64 }]);
      break;
    }
  }
}

function sendMessage()
{
  var listElem = getMessageList();
  var textarea = PF("assistantTextarea").getJQ();
  var text = textarea.val().trim();

  var fileName = document.querySelector(".attached_filename").textContent;
  var docId = document.querySelector(".attached_docid").textContent;
  var contentId = document.querySelector(".attached_contentid").textContent;
  if (docId && contentId)
  {
    const origin = document.location.origin;
    const docUrl = origin + "/documents/" + contentId;
    if (fileName.endsWith(".png") || fileName.endsWith(".jpg"))
    {
      text += "\n[![image " + docId + "](" + docUrl + ")](" + docUrl + ")";      
    }
    else
    {
      text += "\n(docId: [" + docId + "](" + docUrl + "))";
    }
  }

  textarea.val(text);

  var sendButton = PF("assistantSendButton");
  sendButton.jq.hide();

  var interruptButton = PF("assistantInterruptButton");
  interruptButton.jq.show();

  var uploadButton = PF("assistantUploadButton");
  uploadButton.disable();

  var itemElem = createMessage("USER", text);
  listElem.appendChild(itemElem);

  showDots();

  scrollMessages();
  setTimeout(() => textarea.val(""), 0);
}

async function showResponse(threadId)
{
  const listElem = getMessageList();
  if (!listElem) return;

  var response = await fetch("/stream/" + threadId);
  var queue = await response.json();
  var end = false;

  let markdownElem;
  let htmlElem;

  let aiMessageElem = listElem.querySelector(":scope > li:last-child .message.AI");
  if (aiMessageElem)
  {
    markdownElem = aiMessageElem.querySelector(".markdown");
    htmlElem = aiMessageElem.querySelector(".html");
  }

  for (var item of queue)
  {
    if (item === 0)
    {
      end = true;
      hideDots();
      updateAssistantButtons();
    }
    else if (typeof item === "string") // tokens from streaming
    {
      if (!aiMessageElem)
      {
        var itemElem = createMessage("AI");
        listElem.appendChild(itemElem);
        aiMessageElem = itemElem.querySelector(".message");
        markdownElem = itemElem.querySelector(".markdown");
        htmlElem = itemElem.querySelector(".html");
      }
      markdownElem.textContent += item;
      htmlElem.innerHTML = markdownToHtml(markdownElem.textContent);
      setLinkTarget(htmlElem);
      scrollMessages();
    }
    else if (typeof item === "object") // message
    {
      console.info(item);
      if (htmlElem && htmlElem.textContent?.length === 0)
      {
        // previous AI message is empty, remove it.
        var itemElem = aiMessageElem.parentElement;
        if (itemElem?.parentElement) itemElem.parentElement.removeChild(itemElem);
      }
      const type = item.type;
      let text = item.text;
      let toolExecutionRequests = item.toolExecutionRequests;

      if (toolExecutionRequests && toolExecutionRequests.length > 0)
      {
        let json = JSON.stringify(
        {
          type: "AI", 
          toolExecutionRequests: toolExecutionRequests 
        }, null, 2); 
        var itemElem = createMessage(type, "```json\n" + json + "\n```");
        listElem.appendChild(itemElem);
      }
      
      if (type === "TOOL_EXECUTION_RESULT")
      {
        text = "```json\n" + JSON.stringify(item, null, 2) + "\n```";
      }
      else if (type === "ACTION")
      {
        console.info("ACTION", item.text);
        eval(item.text);
        if (!aiDebugEnabled) continue;
        text = "```json\n" + item.text + "\n```";
      }
      var itemElem = createMessage(type, text);
      listElem.appendChild(itemElem);
      if (type === "AI")
      {
        aiMessageElem = itemElem.querySelector(".message");
        markdownElem = itemElem.querySelector(".markdown");
        htmlElem = itemElem.querySelector(".html");
        setLinkTarget(htmlElem);
      }
      else if (type === "USER")
      {
        htmlElem = itemElem.querySelector(".html");
        setLinkTarget(htmlElem);        
      }
      scrollMessages();
    }
  }

  if (!end)
  {
    setTimeout(() => showResponse(threadId), 0);
  }
}

function markdownToHtml(text, showThinking = true)
{
  let html = "";
  let index = text.lastIndexOf("<think>");
  if (index !== -1)
  {
    let think = "";
    let message = "";

    let index2 = text.lastIndexOf("</think>");
    if (index2 !== -1 && index2 > index)
    {
      think = text.substring(index + 7, index2);
      message = text.substring(index2 + 8);
    }
    else
    {
      think = text.substring(index + 7);
    }
    if (showThinking && think.trim().length > 0 && message.trim().length === 0)
    {
      html = "<p class='think'><b>Thinking:</b> " + think + "</p>";
    }
    else
    {
      html = markdown.render(message);
    }
  }
  else
  {
    html = markdown.render(text);
  }
  return html;
}

function showThreadsPanel()
{
  var threadsPanel = PF("threadsPanel");
  threadsPanel.show();
  threadsPanel.getJQ().css("display", "flex");
  threadsPanel.getJQ().removeClass("first_time");

  var threadsPanelButton = PF("threadsPanelButton");
  threadsPanelButton.getJQ().css("display", "none");
  threadsPanelButton.getJQ().removeClass("first_time");

  return false;
}

function closeThreadsPanel()
{
  var threadsPanel = PF("threadsPanel");
  threadsPanel.close();

  var threadsPanelButton = PF("threadsPanelButton");
  threadsPanelButton.getJQ().css("display", "inline");

  return false;
}

function endThreadsPanel()
{
  if (700 > window.innerWidth)
  {
    closeThreadsPanel();
  }
}

function getMessageList()
{
  var messageList = document.querySelector(".message_list");
  if (!messageList) return;
  var listElem = messageList.firstElementChild;
  if (!listElem)
  {
    listElem = document.createElement("ul");
    listElem.className = "list-none pl-0";
    messageList.appendChild(listElem);

    const dotsElem = document.createElement("div");
    dotsElem.className = "dot-typing invisible";
    messageList.appendChild(dotsElem);
  }
  return listElem;
}

function scrollMessages()
{
  var elem = document.querySelector(".message_list");
  if (elem)
  {
    elem.scrollTop = elem.scrollHeight;
  }
}
