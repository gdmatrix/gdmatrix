function saveScroll()
{
  var sScroll;
  if (document.documentElement && document.documentElement.scrollTop)
    sScroll = document.documentElement.scrollTop;
  else if (document.body)
    sScroll = document.body.scrollTop;
  else
  {
    sScroll = 0;
  }
  document.getElementById('__SAVESCROLL').value = "" + Math.round(sScroll);
}

function restoreScroll()
{
  var sScroll = document.getElementById('__SAVESCROLL').value;
  if (sScroll > 0)
  {
    if (document.documentElement && document.documentElement.scrollTop)
    {
      document.documentElement.scrollTop = sScroll;
    }
    else
    {
      window.scroll(0, sScroll);
    }
  }
}

if (window.addEventListener)
{
  window.addEventListener("load", restoreScroll, false);
  window.addEventListener("scroll", saveScroll, false);
  window.addEventListener("resize", saveScroll, false);
}
else
{
  window.attachEvent("onload", restoreScroll);
  window.attachEvent("onscroll", saveScroll);
  window.attachEvent("onresize", saveScroll);  
}
  
