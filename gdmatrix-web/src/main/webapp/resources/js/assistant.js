/* assistant.js */

if (typeof markdownConverter === "undefined")
{
  markdownConverter = new showdown.Converter();
}

function updateSendButton()
{
  var text = PF("textarea").getJQ().val().trim();
  var sendButton = PF("sendButton");

  var waiting = document.querySelector(".dot-typing") ? true : false;  

  if (text.length === 0 || waiting)
  {
    sendButton.disable();
  } 
  else
  {
    sendButton.enable();
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
          <div class="html">${markdownToHtml(markdown)}</div>
        </div>
      </div>
    </div>
  `;
  return itemElem;
}

function createDots()
{
  const itemElem = document.createElement("li");
  itemElem.className = "dot-typing";
  const dotsElem = document.createElement("div");
  itemElem.appendChild(dotsElem);
  return itemElem;
}

function sendMessage()
{
  var listElem = getMessageList();
  var textarea = PF("textarea").getJQ();
  var text = textarea.val().trim();
  
  var docId = document.querySelector(".attached_docid").textContent;
  if (docId)
  {
    text += "\n(docId: [" + docId + "](/documents/" + docId + "))";    
  }
  
  textarea.val(text);

  var sendButton = PF("sendButton");
  sendButton.disable();

  var itemElem = createMessage("USER", text);
  listElem.appendChild(itemElem);
  
  var dotsElem = createDots();
  listElem.appendChild(dotsElem);
  
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

  if (queue.length > 0)
  {
    var dotsElem = listElem.querySelector(".dot-typing"); // remove dots
    if (dotsElem)
    {
      dotsElem.parentElement.removeChild(dotsElem);
    }
  }

  const lastItemElem = listElem.querySelector(":scope > li:last-child");
  if (lastItemElem)
  {
    markdownElem = lastItemElem.querySelector(".markdown");  
    htmlElem = lastItemElem.querySelector(".html");
  }

  for (var item of queue)
  {
    if (item === 0)
    {
      updateSendButton();      
      end = true;
    }
    else if (item === 1) // start AI response
    {
      var itemElem = createMessage("AI");  
      listElem.appendChild(itemElem);
      markdownElem = itemElem.querySelector(".markdown");  
      htmlElem = itemElem.querySelector(".html");
    }
    else if (typeof item === "string")
    {
      markdownElem.textContent += item;
      htmlElem.innerHTML = markdownToHtml(markdownElem.textContent);
      scrollMessages();
    }
    else if (typeof item === "object") // message
    {
      const type = item.type;
      let text = item.text;
      if (!text || type === "TOOL_EXECUTION_RESULT")
      {
        text = "```json\n" + JSON.stringify(item, null, 2) + "\n```";
      }
      var itemElem = createMessage(type, text);
      listElem.appendChild(itemElem);
      scrollMessages();
    }
  }

  if (!end)
  {
    setTimeout(() => showResponse(threadId), 0);
  }
}

function markdownToHtml(text)
{
  let html = "";
  let index = text.indexOf("<think>");
  if (index !== -1)
  {
    html = "<p class='think'><b>Thinking:</b> ";

    let index2 = text.indexOf("</think>");
    if (index2 !== -1)
    {
      html += text.substring(index + 7, index2) + "</p>" +
              markdownConverter.makeHtml(text.substring(index2 + 9));    
    }
    else
    {
      html += text.substring(index + 8) + "</p>";
    }    
  }
  else
  {
    html = markdownConverter.makeHtml(text);    
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
