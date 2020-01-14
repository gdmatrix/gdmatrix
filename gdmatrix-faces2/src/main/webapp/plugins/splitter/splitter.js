/* Splitter v1.0 */

splitterMap = {};
splitterArray = new Array();

function updateSplitters()
{
  var splitter_i;
  for (i = 0; i < splitterArray.length; i++)
  {
    splitter_i = splitterArray[i];
    if (splitter_i.widthExpanded) splitter_i.expandWidth();
    if (splitter_i.heightExpanded) splitter_i.expandHeight();
  }
  window.status = "update splitters...";
}

function isIE()
{
  return /msie/i.test(navigator.userAgent) &&
    !/opera/i.test(navigator.userAgent);
}

function Splitter(elementId, position, widthExpanded, heightExpanded)
{
  this.widthExpanded = widthExpanded;
  this.heightExpanded = heightExpanded;
  this.position = position;
  this.drag = false;
  this.container = document.getElementById(elementId);
  this.first = document.getElementById(elementId + "_first");
  this.last = document.getElementById(elementId + "_last");
  this.inputPosition = document.getElementById(elementId + "_pos");
  this.sliderWidth =
    this.first.offsetWidth - this.first.clientWidth - this.first.clientLeft;

  splitterMap[this.container.id] = this;
  splitterMap[this.first.id] = this;
  splitterMap[this.last.id] = this;
  splitterArray.push(this);

  this.getX = function(element, event)
  {
    var curleft = 0;
    if (element.offsetParent)
    {
      while (1)
      {
        curleft += element.offsetLeft + element.clientLeft;
        if (!element.offsetParent) break;
        element = element.offsetParent;
      }
    }
    else if (element.x)
    {
      curleft += element.x;
    }
    if (isIE()) curleft += 2;

    var scrollLeft = 0;
    var parent = this.container.parentNode;
    if (parent)
    {
      scrollLeft = parent.scrollLeft;
    }

    var x;
    if (window.event)
    {
      x = window.event.x - curleft + scrollLeft;
    }
    else
    {
      x = event.clientX - curleft + scrollLeft;
    }
    elem = document.getElementById("xposition");
    if (elem != null) elem.value = "" + x + " " + curleft + " " + scrollLeft;

    return x;
  };

  this.getContainerWidth = function()
  {
    return width = this.container.clientWidth - this.sliderWidth;
  }

  this.getWindowHeight = function()
  {
    var height = 0;
    if (typeof(window.innerWidth) == 'number')
    {
      //Non-IE
      height = window.innerHeight;
    }
    else if (document.documentElement &&
      (document.documentElement.clientWidth ||
       document.documentElement.clientHeight))
    {
      //IE 6+ in 'standards compliant mode'
      height = document.documentElement.clientHeight;
    }
    else if (document.body &&
      (document.body.clientWidth || document.body.clientHeight))
    {
      //IE 4 compatible
      height = document.body.clientHeight;
    }
    return height;
  }

  this.startDrag = function()
  {
    this.drag = true;
    disableSelection(this.first);
    disableSelection(this.last);
    this.first.style.borderRightColor = "#8080FF";
  }

  this.stopDrag = function()
  {
    this.drag = false;
    enableSelection(this.first);
    enableSelection(this.last);
    this.first.style.borderRightColor = "black";
  }

  this.resize = function()
  {
    var width = this.getContainerWidth();
    this.first.style.width = "" + this.position + "px";
    var firstWidth = this.first.offsetWidth;
    var lastWidth = (width - firstWidth) + this.sliderWidth;
    
    if (lastWidth < 0) lastWidth = 0;
    this.last.style.width = "" + lastWidth + "px";
    this.inputPosition.value = this.position;

    if (isIE())
    {
      if (this.last.childNodes.length > 0)
      {
        var child = this.last.childNodes[0];
        elem = document.getElementById("cwidth");
        if (elem != null) elem.value = child.tagName;
        child.style.display = "inline-block";
      }
    }

//    elem = document.getElementById("cwidth");
//    if (elem != null) elem.value = "" + width + " " + firstWidth + " " + lastWidth;
//    elem = document.getElementById("position");
//    if (elem != null) elem.value = this.position;
  };

  this.expandWidth = function()
  {
    var parent = this.container.parentNode;
    if (parent)
    {
      var style = this.getRealStyle(this.container);
      var width = parent.clientWidth - 
        parseInt(style.borderLeftWidth) -
        parseInt(style.borderRightWidth);
      if (width > 0)
      {
        this.container.style.width = "" + width + "px";
        parent.style.overflow = "hidden";
        this.resize();
      }
    }
  }

  this.expandHeight = function()
  {
    var parent = this.container.parentNode;
    if (parent)
    {
      var style = this.getRealStyle(this.container);
      var height;
      if (parent == document.body)
      {
        var curtop = 0;
        var element = this.container;
        if (element.offsetParent)
        {
          while (1)
          {
            curtop += element.offsetTop;
            if (!element.offsetParent) break;
            element = element.offsetParent;
          }
        }
        else if (element.x)
        {
          curtop += element.y;
        }

        height = this.getWindowHeight() -
          curtop -
          parseInt(style.borderTopWidth) -
          parseInt(style.borderBottomWidth);
      }
      else
      {
        height = parent.clientHeight -
          parseInt(style.borderTopWidth) -
          parseInt(style.borderBottomWidth);
      }
      if (height > 0)
      {
        this.container.style.height = "" + height + "px";
        parent.style.overflow = "hidden";
        this.resize();
      }
    }
  }

  this.getRealStyle = function(element)
  {
    var style;
    if (window.getComputedStyle)
    {
      style = window.getComputedStyle(element, null)
    }
    else
    {
      style = element.currentStyle;
    }
    return style;
  }

  function disableSelection(target)
  {
    if (typeof target.onselectstart != "undefined") //IE route
      target.onselectstart = function(){return false}
    else if (typeof target.style.MozUserSelect != "undefined") //Firefox route
      target.style.MozUserSelect = "none"
    target.style.cursor = "default"
  }

  function enableSelection(target)
  {
    if (typeof target.onselectstart != "undefined") //IE route
      target.onselectstart = null;
    else if (typeof target.style.MozUserSelect != "undefined") //Firefox route
      target.style.MozUserSelect = "inherit"
    target.style.cursor = "default"
  }

  function mouseMoveHandler(event)
  {
    var element = this;
    var splitter = splitterMap[element.id];
    var width = splitter.getContainerWidth();
    var x = splitter.getX(element, event);

    // set cursor style
    var cursorStyle;
    if (splitter.drag)
    {
      cursorStyle = "w-resize";
    }
    else
    {
      if (element == splitter.first)
      {
        if (parseInt(x) >= splitter.position)
        {
          cursorStyle = "w-resize";
        }
        else
        {
          cursorStyle = "auto";
        }
      }
      else cursorStyle = "auto";
    }
    element.style.cursor = cursorStyle;

    if (splitter.drag)
    {
      var margin = 24;
      if (element == splitter.first) splitter.position = x;
      else splitter.position = x + splitter.position;

      // limits
      if (splitter.position < margin)
        splitter.position = margin;
      else if (splitter.position >= width - margin)
        splitter.position = width - margin;

      splitter.resize();
      updateSplitters();
    }
  }

  function mouseDownHandler(event)
  {
    var element = this;
    var splitter = splitterMap[element.id];
    var x = splitter.getX(element, event);
    if (element == splitter.first)
    {
      if (parseInt(x) > splitter.position - splitter.sliderWidth + 3)
      {
        splitter.startDrag();
      }
    }
  }

  document.body.onmouseup = function()
  {
    for (i = 0; i < splitterArray.length; i++)
    {
      splitter = splitterArray[i];
      splitter.stopDrag();
    }
  };

  this.first.onmousemove = mouseMoveHandler;
  this.first.onmousedown = mouseDownHandler;

  this.last.onmousemove = mouseMoveHandler;
  this.last.onmousedown = mouseDownHandler;

  this.resize();
}

if (window.addEventListener)
{
  window.addEventListener("load", updateSplitters, false);
  window.addEventListener("resize", updateSplitters, false);
}
else if (window.attachEvent)
{
  window.attachEvent("onload", updateSplitters);
  window.attachEvent("onresize", updateSplitters);
}



