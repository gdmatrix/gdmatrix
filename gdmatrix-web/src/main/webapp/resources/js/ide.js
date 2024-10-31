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
