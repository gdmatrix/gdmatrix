/* assistant.js */

function onAssistantKeyUp(event)
{
  var text = PF("assistantTextarea").getJQ().val().trim();
  if (event.key === "Enter" && !event.shiftKey && text.length > 0)
  {
    let inProgress = isInProgress();
    if (!inProgress)
    {
      var sendButton = PF("assistantSendButton");
      sendButton.jq.click();
    }
  }
  else
  {
    updateAssistantButtons();
  }
}

function updateAssistantButtons()
{
  let text = PF("assistantTextarea").getJQ().val().trim();
  let sendButton = PF("assistantSendButton");
  let interruptButton = PF("assistantInterruptButton");
  let uploadButton = PF("assistantUploadButton");

  let inProgress = isInProgress();

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

function isInProgress()
{
  let dotsElem = document.querySelector(".message_list .dot-typing");
  let inProgress = dotsElem && !dotsElem.classList.contains("invisible");
  return inProgress;
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
  /* If the bar is in floating mode, open it half so the conversation remains
  visible. */
  if (document.body.classList.contains("asst-min"))
  {
    changeWindowSize("half");
  }
  
  let listElem = getMessageList();
  let textarea = PF("assistantTextarea").getJQ();
  let text = textarea.val().trim();

  let fileName = document.querySelector(".attached_filename").textContent;
  let docId = document.querySelector(".attached_docid").textContent;
  let contentId = document.querySelector(".attached_contentid").textContent;
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

  let sendButton = PF("assistantSendButton");
  sendButton.jq.hide();

  let interruptButton = PF("assistantInterruptButton");
  interruptButton.jq.show();

  let uploadButton = PF("assistantUploadButton");
  uploadButton.disable();

  let itemElem = createMessage("USER", text);
  listElem.appendChild(itemElem);

  showDots();

  scrollMessages();
  setTimeout(() => textarea.val(""), 0);
}

async function showResponse(threadId)
{
  const listElem = getMessageList();
  if (!listElem) return;

  let response = await fetch("/stream/" + threadId);
  let queue = await response.json();
  let end = false;

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
    if (item === 0) // end streaming
    {
      end = true;
      hideDots();
      updateAssistantButtons();
    }
    else if (item === 1) // clear messages
    {
      listElem.innerHTML = "";
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
      htmlElem.innerHTML = markdownToHtml(markdownElem.textContent, true);
      setLinkTarget(htmlElem);
      scrollMessages();
    }
    else if (typeof item === "object") // message
    {
      console.info(item);
      if (htmlElem && htmlElem.textContent?.length === 0)
      {
        // previous AI message is empty, remove it.
        let itemElem = aiMessageElem.parentElement;
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
      if (!aiDebugEnabled) 
      {
        // show only last word
        index = think.lastIndexOf(" ");
        think = index !== -1 ? think.substring(index + 1) : think;
      }
      html = `<p class='think'><b>${thinkingLabel}:</b> ${think}</p>`;
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
  let threadsPanel = PF("threadsPanel");
  threadsPanel.show();
  threadsPanel.getJQ().css("display", "flex");
  threadsPanel.getJQ().removeClass("first_time");

  let threadsPanelButton = PF("threadsPanelButton");
  threadsPanelButton.getJQ().css("display", "none");
  threadsPanelButton.getJQ().removeClass("first_time");

  return false;
}

function closeThreadsPanel()
{
  let threadsPanel = PF("threadsPanel");
  threadsPanel.close();

  let threadsPanelButton = PF("threadsPanelButton");
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
  let messageList = document.querySelector(".message_list");
  if (!messageList) return;
  let listElem = messageList.firstElementChild;
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
  let elem = document.querySelector(".message_list");
  if (elem)
  {
    elem.scrollTop = elem.scrollHeight;
  }
}

/*
 * Window size menu (wsm-*, mirar assistant_bar.xhtml)
 * Estas funciones las he añadido/movido desde header, porque assistant_bar.xhtml
 * se usa tanto en pf_web (nouweb) como pf_app (actual) donde
 * maximize/minimize/hideAssistantPanel no existen. Los "guards typeof" estan para
 * evitar romper la web actual/intra (pf_app)
 */
function changeWindowSize(size)
{
  var fk = { preventDefault: function() {} };
  
  if (size === "full" && typeof maximizeAssistantPanel === "function")
  {
    maximizeAssistantPanel(fk);
  }
  else if (size === "half" && typeof minimizeAssistantPanel === "function")
  {
    minimizeAssistantPanel(fk);
  }
  else if (size === "min" && typeof hideAssistantPanel === "function")
  {
    hideAssistantPanel(fk);
  }
  
  updateWsmSelection(size);
  
  if (window.PF && PrimeFaces.widgets["wsmOverlay"])
  {
    PF("wsmOverlay").hide();
  }
}

function updateWsmSelection(size)
{
  document.querySelectorAll(".wsm-item").forEach(function(item)
  {
    item.classList.toggle("wsm-selected", item.dataset.size === size);
  });
}