function WidgetContainerPaginator(id) 
{
  this.id = id;
  this.enableSwipe = false;
  this.pageCount = 0;
  this.pageWidth = 0;
  this.selectedPage = 0;  
  this.lastSwipeClientX = null;   
  this.startSwipeClientX = null;  
}

window.onresize = function()
{
  for (var i = 0; i < document.getElementsByClassName("widget_container").length; i++)
  {
    var container = document.getElementsByClassName("widget_container")[i];
    if (container)
    {
      var containerWindow = document.getElementById(container.id + "_window");
      if (containerWindow)
      {
        var paginator = containerWindow.paginator;
        if (paginator)
        {          
          paginator.setComponentsWidth();
          paginator.swipeToPoint(-paginator.selectedPage * paginator.pageWidth, true);    
          paginator.refresh();          
        }
      }
    }
  }  
}

WidgetContainerPaginator.prototype.init = function()
{
  this.setComponentsWidth();
  if (this.enableSwipe === true)
  {
    this.getWindowDiv().addEventListener('touchstart', this.startSwipe, false); 
    this.getWindowDiv().addEventListener('touchmove', this.swipe, false);
    this.getWindowDiv().addEventListener('touchend', this.endSwipe, false);    
    this.getWindowDiv().addEventListener('touchcancel', this.cancelSwipe, false);     
  }
  this.getWindowDiv().paginator = this;
  this.refresh();
}

WidgetContainerPaginator.prototype.setComponentsWidth = function()
{
  this.pageWidth = this.getPageWidth();
  this.getWindowDiv().style.width = this.pageWidth + 'px'; 
  this.getPagesDiv().style.width = (this.pageCount * this.pageWidth) + 'px';
  this.getPagesDiv().style.left = this.getLeftStyleValue() + 'px';
  for (var i = 0; i < this.pageCount; i++)
  {
    this.getPageDiv(i).style.width = this.pageWidth + 'px';    
  }  
}

//PAGE SELECTION

WidgetContainerPaginator.prototype.prevPageSelect = function()
{  
  if (this.selectedPage > 0) 
  {
    this.selectedPage = this.selectedPage - 1;
    this.swipeToPoint(-this.selectedPage * this.pageWidth, true);    
    this.refresh();
  }      
}

WidgetContainerPaginator.prototype.nextPageSelect = function()
{  
  if (this.selectedPage < this.pageCount - 1)
  {
    this.selectedPage = this.selectedPage + 1;
    this.swipeToPoint(-this.selectedPage * this.pageWidth, true);
    this.refresh();
  }
}

WidgetContainerPaginator.prototype.selectPage = function(idx)
{  
  if (idx >= 0 && idx < this.pageCount)
  {
    this.selectedPage = idx;
    this.swipeToPoint(-this.selectedPage * this.pageWidth, true);
    this.refresh();
  }
}

WidgetContainerPaginator.prototype.refresh = function()
{
  if (this.selectedPage === 0)
  {
    this.getPrevArrow().className = 'pagePrev hide';
    this.getNextArrow().className = 'pageNext show';
  }
  else if (this.selectedPage === this.pageCount - 1)
  {
    this.getPrevArrow().className = 'pagePrev show';
    this.getNextArrow().className = 'pageNext hide';
  }
  else
  {
    this.getPrevArrow().className = 'pagePrev show';
    this.getNextArrow().className = 'pageNext show';
  }

  for (i = 0; i < this.pageCount; i++)
  {    
    if (i === this.selectedPage)
    {
      this.getPoint(i).className = 'point on';      
    }
    else
    {
      this.getPoint(i).className = 'point off';      
    }
  }
}

WidgetContainerPaginator.prototype.getPrevArrow = function()
{
  return document.getElementById(this.id + "_prev");
}

WidgetContainerPaginator.prototype.getPoint = function(index)
{
  return document.getElementById(this.id + "_point_" + index);
}

WidgetContainerPaginator.prototype.getNextArrow = function()
{
  return document.getElementById(this.id + "_next");
}

//SWIPE

WidgetContainerPaginator.prototype.startSwipe = function(event)
{
  var paginator = this.paginator; //this = div affected by swipe  
  paginator.lastSwipeClientX = event.touches[0].clientX;
  paginator.lastSwipeClientY = event.touches[0].clientY;  
  paginator.startSwipeClientX = event.touches[0].clientX;
}

WidgetContainerPaginator.prototype.swipe = function(event)
{    
  //event.preventDefault(); //only here, for compatibility with Android default browser
  var paginator = this.paginator; //this = div affected by swipe
  var deltaX = event.touches[0].clientX - paginator.lastSwipeClientX;
  var deltaY = event.touches[0].clientY - paginator.lastSwipeClientY;
  if (Math.abs(deltaX) > Math.abs(deltaY))
  {
    event.preventDefault();
    paginator.swipeAmount(deltaX);
    if (paginator.selectedPage !== paginator.calcSelectedPage())
    {
      paginator.selectedPage = paginator.calcSelectedPage();
      paginator.refresh();
    }
    paginator.lastSwipeClientX = event.touches[0].clientX;
    paginator.lastSwipeClientY = event.touches[0].clientY;
  }
}
 
WidgetContainerPaginator.prototype.endSwipe = function(event)
{
  var paginator = this.paginator; //this = div affected by swipe  
  var swipePercent = paginator.calcSwipePercent();
  if (swipePercent >= 0.25 && swipePercent < 0.50) //change selected page
  {
    if (paginator.lastSwipeClientX > paginator.startSwipeClientX) //swipe right
    {
      paginator.prevPageSelect();
    }
    else //swipe left
    {
      paginator.nextPageSelect();      
    }
  }
  else //maintain selected page
  {
    paginator.selectPage(paginator.selectedPage);
  }  
  paginator.lastSwipeClientX = null;
  paginator.lastSwipeClientY = null;  
  paginator.startSwipeClientX = null;
}

WidgetContainerPaginator.prototype.cancelSwipe = function(event)
{
  //console.log("touchcancel");  
}

WidgetContainerPaginator.prototype.swipeAmount = function(amount)
{  
  var leftValue = this.getLeftStyleValue();
  if (leftValue <= 0 && leftValue >= -this.getMaxSwipe())
  {
    if (amount > 0) //swipe right
    {
      if (leftValue - (-amount) > 0) 
        this.swipeToPoint(0, false);        
      else
        this.swipeToPoint(leftValue - (-amount), false);
    }
    else //swipe left
    {
      if (leftValue - (-amount) < -this.getMaxSwipe()) 
        this.swipeToPoint(-this.getMaxSwipe(), false);
      else    
        this.swipeToPoint(leftValue - (-amount), false);
    }
  }  
}  
  
WidgetContainerPaginator.prototype.swipeToPoint = function(pointX, direct)
{  
  var pagesDiv = this.getPagesDiv(); 
  if (direct === true)
  {
    pagesDiv.className = "widgetContainerPages pageChange";
    pagesDiv.style.left = pointX + 'px';    
  }
  else if (direct === false)
  {
    pagesDiv.className = "widgetContainerPages";
    pagesDiv.style.left = pointX + 'px';
  }
}
  
WidgetContainerPaginator.prototype.getLeftStyleValue = function()
{  
  var value = this.getPagesDiv().style.left;
  if (value == null || value == '') 
    return 0;
  else if (value.indexOf('px') == -1) 
    return value;
  else 
    return value.substring(0, value.indexOf('px'));  
}

WidgetContainerPaginator.prototype.calcSelectedPage = function()
{          
  var leftValue = this.getLeftStyleValue();  
  return Math.round(Math.abs(leftValue / this.pageWidth));
}

WidgetContainerPaginator.prototype.calcSwipePercent = function()
{  
  return Math.abs((this.lastSwipeClientX - this.startSwipeClientX) / this.pageWidth);
}

WidgetContainerPaginator.prototype.getPageWidth = function()
{ 
  return this.getContainerDiv().offsetWidth;
}

WidgetContainerPaginator.prototype.getMaxSwipe = function()
{
  return this.pageWidth * (this.pageCount - 1);
}

WidgetContainerPaginator.prototype.getContainerDiv = function()
{
  return document.getElementById(this.id);
}

WidgetContainerPaginator.prototype.getWindowDiv = function()
{
  return document.getElementById(this.id + "_window");
}

WidgetContainerPaginator.prototype.getPagesDiv = function()
{
  return document.getElementById(this.id + "_pages");  
}

WidgetContainerPaginator.prototype.getPageDiv = function(index)
{
  return document.getElementById(this.id + "_page_" + index);
}
