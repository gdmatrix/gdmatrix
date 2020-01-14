/* print.js */

var gAutoPrint = true;
function printGroup(printname)
{
  if (document.getElementById !== null)
  {
    var head = '';
    if (document.getElementsByTagName !== null)
    {
      var headTags = document.getElementsByTagName("head");
      if (headTags.length > 0) head += headTags[0].innerHTML;
    }
    var body = '';
    var printReadyElem = document.getElementById(printname);
    if (printReadyElem !== null)
    {
      body += printReadyElem.innerHTML;
    }
    else
    {
      alert("Could not find the printname tag");
      return;
    }
    var printWin = window.open("", "printGroup");
    printWin.document.open();
    printWin.document.write("<html><head></head><body></body></html>");
    var links = document.head.getElementsByTagName("link");
    var host = window.location.protocol + "//" + window.location.host;
    for (var i = 0; i < links.length; i++)
    {
      var link = links[i];
      console.info(link);
      var printLink = printWin.document.createElement("link");
      
      var href = link.getAttribute("href");
      if (href)
      {
        if (href.charAt(0) === '/') href = host + href;
        printLink.setAttribute("href", href);
      };
      
      var type = link.getAttribute("type");
      if (type) printLink.setAttribute("type", type);
      
      var rel = link.getAttribute("rel");
      if (rel) printLink.setAttribute("rel", rel);
      
      printWin.document.head.appendChild(printLink);
    }
    var printDiv = printWin.document.createElement("div");
    printDiv.setAttribute("class", printname + " print");
    printDiv.innerHTML = body;
    printWin.document.body.appendChild(printDiv);
    printWin.document.close();
    if (gAutoPrint) setTimeout(function(){printWin.print();}, 1000);
  }
  else
  {
    alert('Can not print.');
  }
}
