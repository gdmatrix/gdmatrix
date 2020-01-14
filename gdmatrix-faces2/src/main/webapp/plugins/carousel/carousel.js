var newsCarouselIntervalId = 0;
var newsCarouselIndex = 1;

function newsCarouselGoPrevious()
{
  newsCarouselStop();
  var oldLayerIndex = newsCarouselIndex;
  var newLayerIndex = oldLayerIndex - 1;
  if (newLayerIndex == 0)
  {
    newLayerIndex = newsCarouselNewCount;
  }
  var oldLayer = document.getElementById('newsCarouselNewLayer' + oldLayerIndex);
  var newLayer = document.getElementById('newsCarouselNewLayer' + newLayerIndex);
  oldLayer.style.visibility = 'hidden';
  assignImage(newLayerIndex - 1);
  newLayer.style.visibility = 'visible';
  newsCarouselIndex = newLayerIndex;
}

function newsCarouselGoNext()
{
  newsCarouselStop();
  newsCarouselPass();
}

function newsCarouselPass()
{
  var oldLayerIndex = newsCarouselIndex;
  var newLayerIndex = oldLayerIndex + 1;
  if (newLayerIndex > newsCarouselNewCount)
  {
    newLayerIndex = 1;
  }
  var oldLayer = document.getElementById('newsCarouselNewLayer' + oldLayerIndex);
  var newLayer = document.getElementById('newsCarouselNewLayer' + newLayerIndex);
  oldLayer.style.visibility = 'hidden';
  assignImage(newLayerIndex + 1);
  newLayer.style.visibility = 'visible';
  newsCarouselIndex = newLayerIndex;
}

function newsCarouselStart()
{
  if (newsCarouselNewCount > 0)
  {
    newsCarouselIntervalId = setInterval("newsCarouselPass()", newsCarouselTransitionTime);
  }
}

function newsCarouselStop()
{
  clearInterval(newsCarouselIntervalId);
}

function assignImage(layerIndex)
{
  if (layerIndex >= 1 && layerIndex <= newsCarouselNewCount)
  {
    var layerImage = document.getElementById('newsCarouselNewImage' + layerIndex);
    if (layerImage)
    {
      var docURL = imageURLArray[layerIndex - 1];
      layerImage.setAttribute('src', docURL);    
    }
  }
}

