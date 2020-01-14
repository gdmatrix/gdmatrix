var autoComplete =
{
  theResults: null,
  theInput: null,
  
  initResults : function()
  {
    if (this.theResults == null)
    {
      var theBody = document.getElementsByTagName("body").item(0);
      this.theResults = document.createElement("div");
      this.theResults.setAttribute("id", "theResults");
      this.theResults.style.left = "0px";
      this.theResults.style.top = "0px";
      this.theResults.style.position = "absolute";
      this.theResults.style.background = "white";
      this.theResults.style.zIndex = "99";
      this.theResults.style.border = "1px solid #AAAAAA";
      this.theResults.style.display = "none";
      this.theResults.style.boxShadow = "3px 3px 3px #CCCCCC";
      theBody.appendChild(this.theResults);
      document.onclick = function(){autoComplete.hideResults();};      
    }
  },

  showResults: function()
  {
    this.theResults.style.left = x + "px";
    this.theResults.style.top = (y + h) + "px";
    this.theResults.style.display = "block";
    if (this.theResults.childNodes.length > 8)
    {
      this.theResults.style.height = "180px";
      this.theResults.style.overflowY = "scroll";
    }
    else
    {
      this.theResults.style.height = null;
      this.theResults.style.overflowY = "hidden";
    }
  },

  hideResults: function()
  {
    this.theResults.innerHTML = "";
    this.theResults.style.display = "none";
  },

  fillResults: function(items)
  {
    var options = "";
    for (i = 0; i < items.length; i++)
    {
      var item = items[i];
      var label;
      var code;
      if (item instanceof Array)
      {
        code = items[i][0];
        label = items[i][1];
      }
      else
      {
        code = items[i];
        label = items[i];
      }
      options += "<a class=\"acItem\" " + 
        "onkeyup=\"autoComplete.processKey(event);\" " +
        "onmouseover=\"this.focus();\"";
      options += "id=\"acItem-" + i + "\"";
      options += "href=\"javascript:autoComplete.setValue('" + code + "');\">";
      options += label;
      options += "</a>";
    }
    this.theResults.innerHTML = options;  
  },

  filter: function(elem, items, event)
  {
    this.theInput = elem;
    this.initResults();

    var keyCode = event.keyCode;
    var value = this.theInput.value;
    var index = value.lastIndexOf(",");
    if (index >= 0)
    {
      value = value.substring(index + 1);
    }
    //alert(keyCode);
    if (value == "" && keyCode != 40 || 
        keyCode == 9 || keyCode == 13 || keyCode == 27)
    {
      this.hideResults();
    }
    else
    {
      var obj = this.theInput;
      if (obj.offsetParent)
      {
        x = obj.offsetLeft;
        y = obj.offsetTop;
        h = obj.offsetHeight;
        w = obj.offsetWidth;
        while (obj = obj.offsetParent)
        {
          x += obj.offsetLeft;
          y += obj.offsetTop;
        }
      }
      var filteredItems;
      if (items instanceof Array)
      {
        filteredItems = this.localFilterItems(items, value);
      }
      else
      {
        filteredItems = this.remoteFilterItems(items, value);
      }
      if (filteredItems.length > 0)
      {
        this.fillResults(filteredItems);
        this.showResults();
        if (event.keyCode == 40)
        {
          this.focusItem(0);
        }
      }
      else
      {
        this.hideResults();
      }
    }
  },

  localFilterItems: function(items, value)
  {
    value = value.toUpperCase();
    var filteredItems = new Array();
    for (i = 0; i < items.length; i++)
    {
      var item = items[i];
      var code;
      var label;
      if (item instanceof Array)
      {
        code = items[i][0];
        label = items[i][1];
      }
      else
      {
        code = items[i];
        label = items[i];
      }
      if (label.toUpperCase().indexOf(value) >= 0)
      {
        filteredItems.push([code, label]); 
      };
    }
    return filteredItems;
  },

  remoteFilterItems: function(items, value)
  {
    return items;
  },

  processKey: function(event)
  {
    if (typeof event.stopPropagation != "undefined")
    {
      event.stopPropagation();
    }
    else
    {
      event.cancelBubble = true;
    }
    var keyCode = event.keyCode;
    if (keyCode == 27 || keyCode == 8)
    {
      this.hideResults();
      this.theInput.focus();
    }
    else if (keyCode == 38 || keyCode == 40)
    {
      var elem = document.activeElement;    
      if (elem != null && elem.id != null)
      {
        var itemId = elem.id;
        var pos = itemId.indexOf("-");
        var acItemIndex = parseInt(itemId.substring(pos + 1));
        if (keyCode == 40)
        {
          this.focusItem(acItemIndex + 1);
        }
        else if (keyCode == 38)
        {
          this.focusItem(acItemIndex - 1);
        }
      }
    }
  },

  setValue: function(value)
  {    
    var oldValue = this.theInput.value;
    var index = oldValue.lastIndexOf(",");
    if (index >= 0)
    {
      value = oldValue.substring(0, index + 1) + value;
    }
    this.theInput.value = value;
    this.theInput.focus();
    this.hideResults();
  },

  focusItem: function(acItemIndex)
  {
    var item = document.getElementById("acItem-" + acItemIndex);
    if (item != null) item.focus();
  }
};

// helper methods

function autoCompleteLayer(elem, event)
{
  var serviceName = document.getElementById("serviceName").value;
  var layerList = serviceList[serviceName];
  autoComplete.filter(elem, layerList, event);
}

function disableAutoComplete(elem)
{
  try
  {
    //var elem = document.getElementById(elemId);
    elem["autocomplete"] = "off";
  }
  catch (e)
  {
  }
}

function showCQLAssistant(typeFieldName, element)
{
  var url;
  if (serviceUrl !== null)
  {
    url = baseUrl + "/proxy?url=" + serviceUrl;
  }
  else
  {
    var serviceElem = document.getElementById('serviceName');
    if (!serviceElem) return;
    url = baseUrl + "/proxy?url=" + serviceArray[serviceElem.selectedIndex];
  }
  console.info("URL: " + url);
  var typeFieldElem = document.getElementById(typeFieldName);
  if (typeFieldElem)
  {
    var typeName = typeFieldElem.value;
    OpenLayers.CQLAssistant.show(url, typeName, element);
  }
}

function hideCQLAssistant()
{
  OpenLayers.CQLAssistant.hide();
}