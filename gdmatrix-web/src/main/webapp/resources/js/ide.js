/* ide.js */

function showPanel()
{
  var panel = PF("docsPanel");
  panel.show();
  panel.getJQ().css("display", "flex");
  panel.getJQ().removeClass("first_time");

  var threadsPanelButton = PF("docsPanelButton");
  threadsPanelButton.getJQ().css("display", "none");
  threadsPanelButton.getJQ().removeClass("first_time");

  return false;
}

function closePanel()
{
  var panel = PF("docsPanel");
  panel.close();

  var threadsPanelButton = PF("docsPanelButton");
  threadsPanelButton.getJQ().css("display", "inline");

  return false;
}

function endPanel()
{
  if (700 > window.innerWidth)
  {
    closePanel();
  }
}

var readingConsole = false;

async function readConsole(consoleId, clear = false)
{
  console.info("read " + consoleId);
  readingConsole = true;

  const consoleElem = document.getElementById("js_console");
  if (!consoleElem) return;

  if (clear)
  {
    consoleElem.innerHTML = "";
  }

  const END_TAG = 0;
  const response = await fetch("/stream/" + consoleId);
  const queue = await response.json();  
  
  for (var item of queue)
  {
    if (item === END_TAG)
    { 
      readingConsole = false;
      if (updateToolbar) updateToolbar();
    }
    else
    {
      const msgElem = document.createElement("div");
      msgElem.className = item.level;
      for (let value of item.values)
      {
        const itemElem = document.createElement("div");
        itemElem.className = typeof value;
        itemElem.textContent = value;
        msgElem.appendChild(itemElem);
      }
      consoleElem.appendChild(msgElem);
      consoleElem.scrollTop = consoleElem.scrollHeight;
    }
  }

  if (readingConsole)
  {
    setTimeout(() => readConsole(consoleId), 0);
  }
}