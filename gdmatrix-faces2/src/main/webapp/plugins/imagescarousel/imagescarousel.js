function ImagesCarousel(id) 
{
  this.id = id;
  this.intervalId = 0;
  this.index = 0;
  this.thumbnailCount = 0;
  this.thumbnailWindow = 0;
  this.thumbnailFirstIndex = 0;
  this.thumbnailLastIndex = 0;
  this.transitionTime = 0;
  this.continueTime = 0;
  this.thumbnailShiftMode = "";
  this.thumbnailHoverMode = "";
  this.thumbnailClickMode = "";
  this.renderMainImage = false;
  this.renderThumbnails = false;
  this.mainImageURLArray = [];
  this.externalImageURLArray = [];
  this.running = false;
}

ImagesCarousel.prototype.pass = function()
{  
  var newIndex = this.getNextThumbnailIndex(this.index); 
  if (this.renderThumbnails)
  {    
    if (newIndex == 1)
    {
      this.updateThumbnailsVisibility(1, this.thumbnailWindow);  
    }
    else if (newIndex > this.thumbnailLastIndex)
    {
      this.updateThumbnailsVisibility(this.thumbnailFirstIndex + 1, this.thumbnailLastIndex + 1);  
    }      
  }
  this.changeImage(newIndex);
};

ImagesCarousel.prototype.back = function()
{    
  var newIndex = this.getPreviousThumbnailIndex(this.index); 
  if (this.renderThumbnails)
  {
    if (newIndex == this.thumbnailCount)
    {
      this.updateThumbnailsVisibility(this.thumbnailCount - this.thumbnailWindow + 1, this.thumbnailCount);  
    }
    else if (newIndex < this.thumbnailFirstIndex)
    {
      this.updateThumbnailsVisibility(this.thumbnailFirstIndex - 1, this.thumbnailLastIndex - 1);  
    }
  }
  this.changeImage(newIndex);
};

ImagesCarousel.prototype.thumbnailMouseOver = function(index)
{
  if (this.thumbnailHoverMode == 'select')
  {
    this.changeImage(index);    
    this.stop();    
  }
};

ImagesCarousel.prototype.thumbnailMouseOut = function()
{
  if (this.thumbnailHoverMode == 'select')
  {
    if (this.continueTime > 0)
    {
      this.intervalId = setTimeout('imagesCarousel' + '_' + this.id + '.go()', this.continueTime);
      this.running = true;
    }
  }    
};

ImagesCarousel.prototype.thumbnailMouseClick = function(index)
{
  if (this.thumbnailClickMode == 'select' || this.thumbnailClickMode == 'selectAndOpen')
  {
    this.changeImage(index);
    this.stopAndGo();    
  }
};

ImagesCarousel.prototype.goLeft = function()
{
  if (this.renderThumbnails)
  {
    if (this.thumbnailShiftMode == 'block')
    {  
      if (this.thumbnailFirstIndex > 1)
      {  
        this.updateThumbnailsVisibility(this.thumbnailFirstIndex - 1, this.thumbnailLastIndex - 1);
      }
    }
    else if (this.thumbnailShiftMode == 'thumbnail')
    {
      if (this.index > 1)
      {
        this.back();
      }      
    }      
  }
  else
  {
    if (this.index > 1)
    {
      this.back();
    }      
  }
  this.stopAndGo();
};

ImagesCarousel.prototype.goRight = function()
{
  if (this.renderThumbnails)
  {
    if (this.thumbnailShiftMode == 'block')
    {
      if (this.thumbnailLastIndex < this.thumbnailCount)
      {
        this.updateThumbnailsVisibility(this.thumbnailFirstIndex + 1, this.thumbnailLastIndex + 1);
      }      
    }
    else if (this.thumbnailShiftMode == 'thumbnail')
    {
      if (this.index < this.thumbnailCount)
      {
        this.pass();
      }
    }          
  }
  else
  {
    if (this.index < this.thumbnailCount)
    {
      this.pass();
    }
  }
  this.stopAndGo();
};

ImagesCarousel.prototype.changeImage = function(index)
{
  if (this.renderMainImage)
  {
    var mainImage = document.getElementById('imagesCarouselMainImage' + '_' + this.id);
    if (mainImage)
    {
      var docURL = this.mainImageURLArray[index - 1];
      mainImage.setAttribute('src', docURL);    
    }  
    var mainLink = document.getElementById('imagesCarouselMainLink' + '_' + this.id);
    if (mainLink)
    {
      var externalURL = this.externalImageURLArray[index - 1];
      mainLink.setAttribute('href', externalURL);    
    }      
  }
  if (this.renderThumbnails)
  {
    var oldThumbnail = document.getElementById('imagesCarouselThumbnailDiv' + this.index + '_' + this.id);
    if (oldThumbnail)
    {
      oldThumbnail.className = 'thumbnail';         
    }    
    var newThumbnail = document.getElementById('imagesCarouselThumbnailDiv' + index + '_' + this.id);
    if (newThumbnail)
    {
      newThumbnail.className = 'thumbnail selected';              
    }    
  }
  this.index = index;
};

ImagesCarousel.prototype.updateThumbnailsVisibility = function(newFirstIndex, newLastIndex)
{
  for (var i = 1; i <= this.thumbnailCount; i++)
  {
    var thumbnail = document.getElementById('imagesCarouselThumbnailDiv' + i + '_' + this.id);
    if (thumbnail)
    {
      if (i >= newFirstIndex && i <= newLastIndex)
      {
        thumbnail.style.display = 'inline-block';
      }
      else
      {
        thumbnail.style.display = 'none';
      }
    }
  }  
  this.thumbnailFirstIndex = newFirstIndex;
  this.thumbnailLastIndex = newLastIndex;
};

ImagesCarousel.prototype.go = function()
{  
  if (this.transitionTime > 0)
  {
    this.intervalId = setInterval('imagesCarousel' + '_' + this.id + '.pass()', this.transitionTime);        
    this.running = true;        
  }
};

ImagesCarousel.prototype.stop = function()
{
  if (this.transitionTime > 0)
  {
    clearInterval(this.intervalId);
    this.running = false;
  }
};

ImagesCarousel.prototype.stopAndGo = function()
{
  if (this.transitionTime > 0)
  {
    if (this.running)
    {
      clearInterval(this.intervalId);
      this.running = false;    
      if (this.continueTime > 0)
      {
        this.intervalId = setTimeout('imagesCarousel' + '_' + this.id + '.go()', this.continueTime);
        this.running = true;
      }            
    }
  }
};

ImagesCarousel.prototype.getNextThumbnailIndex = function(thumbnailIndex) //next item to pass
{
  if (thumbnailIndex == this.thumbnailCount) return 1;
  else return (thumbnailIndex + 1);  
};

ImagesCarousel.prototype.getPreviousThumbnailIndex = function(thumbnailIndex) //previous item to pass
{
  if (thumbnailIndex == 1) return this.thumbnailCount;
  else return (thumbnailIndex - 1);
};
