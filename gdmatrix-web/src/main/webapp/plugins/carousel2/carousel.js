function newsCarouselPass()
{  
  var newIndex = newsCarouselGetNextNewIndex(newsCarouselIndex); 
  newsCarouselChange(newIndex);
}

function newsCarouselMouseOverItem(newIndex)
{
  newsCarouselChange(newIndex);
  newsCarouselStop();
}

function newsCarouselMouseOutItem()
{  
  newsCarouselStart();
}

function newsCarouselChange(newIndex)
{
  var oldIndex = newsCarouselIndex;
  var oldImageLayer = document.getElementById('newsCarouselImageLayer' + oldIndex);
  oldImageLayer.style.visibility = 'hidden';
  var newImageLayer = document.getElementById('newsCarouselImageLayer' + newIndex);
  newImageLayer.style.visibility = 'visible';  
  newsCarouselUpdateInfoLayer(newIndex);
  newsCarouselIndex = newIndex;  
}

function newsCarouselGoNext()
{
  var currentBlock = newsCarouselGetBlock(newsCarouselIndex);
  var nextBlock = newsCarouselGetNextBlock(currentBlock);
  var newIndex = newsCarouselGetBlockFirstNewIndex(nextBlock);
  newsCarouselChange(newIndex);
  newsCarouselStop();
  newsCarouselStart();
}

function newsCarouselGoPrevious()
{
  var currentBlock = newsCarouselGetBlock(newsCarouselIndex);
  var previousBlock = newsCarouselGetPreviousBlock(currentBlock);
  var newIndex = newsCarouselGetBlockFirstNewIndex(previousBlock);
  newsCarouselChange(newIndex);
  newsCarouselStop();
  newsCarouselStart();
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

function newsCarouselUpdateInfoLayer(newIndex)
{
  var blockIndex = newsCarouselGetBlock(newIndex);
  var minVisibleIndex = newsCarouselGetBlockFirstNewIndex(blockIndex);
  var maxVisibleIndex = newsCarouselGetBlockLastNewIndex(blockIndex);
  for (i = 1; i <= newsCarouselNewCount; i++)
  {
    var newInfoLayer = document.getElementById('newsCarouselInfoLayer' + i);
    if (i >= minVisibleIndex && i <= maxVisibleIndex)
    {
      newInfoLayer.style.display = 'block';
    }
    else
    {
      newInfoLayer.style.display = 'none';
    }
    if (i == newIndex) newInfoLayer.className = 'newInfo selected';
    else newInfoLayer.className = 'newInfo';
  }
  var j = 0;
  do
  {
    j++;
    var goToPageDiv = document.getElementById('newsCarouselPageSelect' + j);    
    if (j == blockIndex) 
    {  
      goToPageDiv.className = 'goToPage selected';
    }    
    else 
    {
      goToPageDiv.className = 'goToPage';
    }      
  }
  while (!newsCarouselIsLastBlock(j));
}

//Help methods

//Blocks

function newsCarouselGetBlock(newIndex)
{
  return Math.floor((newIndex - 1) / newsCarouselBlockSize) + 1;
}

function newsCarouselIsFirstBlock(blockIndex)
{  
  return (blockIndex == 1);  
}

function newsCarouselIsLastBlock(blockIndex)
{  
  return (blockIndex == newsCarouselGetBlock(newsCarouselNewCount));
}

function newsCarouselGetNextBlock(blockIndex)
{
  if (newsCarouselIsLastBlock(blockIndex)) return 1;
  else return (blockIndex + 1);
}

function newsCarouselGetPreviousBlock(blockIndex)
{
  if (newsCarouselIsFirstBlock(blockIndex)) return newsCarouselGetBlock(newsCarouselNewCount);
  else return (blockIndex - 1);
}

//News

function newsCarouselGetBlockFirstNewIndex(blockIndex)
{
  return 1 + (blockIndex * newsCarouselBlockSize) - newsCarouselBlockSize;
}

function newsCarouselGetBlockLastNewIndex(blockIndex)
{
  return newsCarouselGetBlockFirstNewIndex(blockIndex) + newsCarouselBlockSize - 1;
}

function newsCarouselGetNextNewIndex(newIndex) //next new to pass
{
  var nextNewIndex;
  if (newIndex == newsCarouselNewCount) nextNewIndex = 1;
  else nextNewIndex = newIndex + 1;
  if (newsCarouselGetBlock(newIndex) != newsCarouselGetBlock(nextNewIndex))
  {
    nextNewIndex = newsCarouselGetBlockFirstNewIndex(newsCarouselGetBlock(newIndex)); 
  }
  return nextNewIndex;
}

//Other

function newsCarouselMakeNavPanelVisible()
{
  var navPanel = document.getElementById('newsCarouselNavPanel');
  if (navPanel) navPanel.style.visibility = 'visible';  
}

