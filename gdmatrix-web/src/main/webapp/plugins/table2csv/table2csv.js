/* table2csv */

function exportTable2CSV(tableId)
{
  var data = "";
  var table = document.getElementById(tableId);
  var rows = table.getElementsByTagName("tr");
  for (var r = 0; r < rows.length; r++)
  {
    var row = rows[r];
    var columns = row.getElementsByTagName(r === 0 ? "th" : "td");
    for (var c = 0; c < columns.length; c++)
    {
      if (c > 0) data += ";";
      var column = columns[c];
      var value = column.textContent;
      value = value.replace(/;/g, ",");
      value = value.replace(/[\n\r]/g, " ");
      data += value;
    }
    data += "\n";
  }
  data = new Blob(["\ufeff", data], {type: 'application/csv;charset=UTF-8', encoding: "UTF-8"});
  var url = window.URL.createObjectURL(data);
  var link = document.createElement("a");
  document.body.appendChild(link);
  link.href = url;
  link.download="export.csv";
  link.click();
}

