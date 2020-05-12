function expand_collapse(index, collapseIcon, expandIcon)
{
  var state = $("#icon-collapse-" + index).attr('src');
  if (state === collapseIcon)
    $("#icon-collapse-" + index).attr('src', expandIcon);
  else
    $("#icon-collapse-" + index).attr('src', collapseIcon);
}

function highlightEffect(rowid) 
{
  var row = $( "#" + rowid )
  row.effect( "highlight", {color: "yellow"}, 1000, function(){} );
};
