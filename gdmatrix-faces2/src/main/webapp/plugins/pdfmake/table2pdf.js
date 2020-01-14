function exportTable2Pdf(tableId)
{
  var data = [];
  var table = document.getElementById(tableId);
  var rows = table.getElementsByTagName("tr");
  for (var r = 0; r < rows.length; r++)
  {
    var row = rows[r];
    var columns = row.getElementsByTagName(r === 0 ? "th" : "td");
    var dataRow = [];
    data.push(dataRow);
    for (var c = 0; c < columns.length; c++)
    {
      var column = columns[c];
      var value = column.textContent;
      value = value.replace(/;/g, ",");
      value = value.replace(/[\n\r]/g, " ");
      dataRow.push({text: value, style: "cell"});
    }
  }
  if (data.length > 0)
  {
    var columnCount = data[0].length;
    var pageSize;
    var pageOrientation;
    if (columnCount >= 20)
    {
      pageSize = 'A1';
      pageOrientation = 'landscape';      
    }
    else if (columnCount >= 16)
    {
      pageSize = 'A2';
      pageOrientation = 'landscape';      
    }
    else if (columnCount >= 12)
    {
      pageSize = 'A3';
      pageOrientation = 'landscape';
    }
    else if (columnCount >= 8)
    {
      pageSize = 'A4';
      pageOrientation = 'landscape';
    }
    else
    {
      pageSize = 'A4';
      pageOrientation = 'portrait';      
    }

    var docDefinition = {
      pageSize: pageSize,
      pageOrientation: pageOrientation,
      content: [
      {
        table: {
          headerRows: 1,
          body: data
        }
      }],
      styles:
      {
        cell: {fontSize: 8},
        footer: {fontSize: 8, alignment: 'center'}
      },
      footer: function(currentPage, pageCount)
        { return {text: currentPage.toString() + ' / ' + pageCount, style: "footer"};}  
    };
    pdfMake.createPdf(docDefinition).open();
  }
}

function exportLabels2Pdf(tableId)
{
  var data = [];
  var table = document.getElementById(tableId);
  var rows = table.getElementsByTagName("tr");
  for (var r = 0; r < rows.length; r++)
  {
    var row = rows[r];
    var columns = row.getElementsByTagName(r === 0 ? "th" : "td");
    var dataRow = [];
    data.push(dataRow);
    for (var c = 0; c < columns.length; c++)
    {
      var column = columns[c];
      var value = column.textContent;
      value = value.replace(/;/g, ",");
      value = value.replace(/[\n\r]/g, " ");
      dataRow.push(value);
    }
  }
  if (data.length > 0)
  {
    var pageSize = 'A4';
    var pageOrientation = 'portrait';

    var pages = [];
    var column = null;
    var page = null;
    var labelsPerPage = 0;

    for (var r = 1; r < data.length; r++)
    {
      if (page === null)
      {
        page = {columns: []};
        if (data.length - r > 16)
        {
          page.pageBreak = "after";
        }
        pages.push(page);
      }
      if (column === null)
      {
        column = [];
        page.columns.push(column);
      }
      
      var row = data[r];
      var linesText = Math.min(row.length - 1, 8);
      var linesTop = Math.floor((8 - linesText) / 2);
      var linesBottom = 8 - (linesText + linesTop);
      for (var k = 0; k < linesTop; k++)
      {
        column.push({text: " ", style: "cell"});
      }
      for (var c = 1; c <= linesText; c++)
      {
        var value = row[c];
        if (value.length > 50) value = value.substring(0, 50);
        column.push({text: value, style: "cell"});
      }
      for (var k = 0; k < linesBottom; k++)
      {
        column.push({text: " ", style: "cell"});
      }
      labelsPerPage++;
      if (labelsPerPage === 8)
      {
        column = null;
      }
      else if (labelsPerPage === 16)
      {
        labelsPerPage = 0;
        column = null;
        page = null;
      }
    }

    var docDefinition = {
      pageSize: pageSize,
      pageOrientation: pageOrientation,
      pageMargins: [ 0, 10, 0, 0 ],
      content: pages,
      styles:
      {
        cell: {fontSize: 11, margin: [20, 0, 0, 0]}
      }
    };
    pdfMake.createPdf(docDefinition).open();
  }
}

