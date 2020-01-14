/* --- Swazz Javascript Calendar ---
/* --- v 1.0 3rd November 2006
Created by Oliver Bryant (http://calendar.swazz.org)
Adapted by Ricard Real (realor@santfeliu.cat)
*/

function writeCalendar()
{

  var languageAux = language;
  if (language != 'ca' && language != 'es' && language != 'en')
  {
    languageAux = 'en';
  }

  var elem = document.createElement('div');
  document.body.appendChild(elem);
  elem.setAttribute("id", "calendar_div");
  elem.setAttribute("style", "display:none");

  var html = '<table class="calendar">';
  html += '<tr class="header"><td id="prev_month_cal" style="cursor:pointer;font-size:8px;" onclick="csubm()">&lt;&lt;</td><td colspan=5 id="mns" align="center" style="font:bold 13px Arial"></td><td id="next_month_cal" align="right" style="cursor:pointer;font-size:8px;" onclick="caddm()">&gt;&gt;</td></tr>';

  html += '<tr class="dow">';
  for (var tt=0;tt<7;tt++)
  {
    html += '<td id="dow' + tt + '">' +
      dayOfWeek[languageAux][(tt + fdok) % 7] + '</td>';
  }
  html += '</tr>';

  for (var kk=1;kk<=6;kk++)
  {
    html += '<tr class="days">';
    for(var tt=1;tt<=7;tt++)
    {
      num = 7 * (kk-1) - (-tt);
      html += '<td id="v' + num + '">&nbsp;</td>';
    }
    html += '</tr>';
  }
  html += '</table>';
  elem.innerHTML = html;
  document.all?document.attachEvent('onclick',checkClick):document.addEventListener('click',checkClick,false);
}

function prepareCalendar(hd, cm, cy)
{
  now=new Date();
  sd=now.getDate();
  td=new Date();
  td.setDate(1);
  td.setFullYear(cy);
  td.setMonth(cm);
  cd=td.getDay() - fdok;

  var languageAux = language;
  if (language != 'ca' && language != 'es' && language != 'en')
  {
    languageAux = 'en';
  }

  if (cd < 0) cd = td.getDay() + 7 - fdok;

  // bar month - year
  getObj('mns').innerHTML = months[languageAux][cm]+ ' ' + cy;

  // paint days of week
  for (var tt=0; tt<7; tt++)
  {
    getObj('dow' + tt).innerHTML = dayOfWeek[languageAux][(tt + fdok) % 7];
  }
  // paints days of month
  marr = ((cy % 4) == 0)? mnl : mnn; // leap year
  for (var d=1; d<=42; d++)
  {
    var currCell = getObj('v'+parseInt(d));
    if ((d >= cd + 1) && (d <= cd + parseInt(marr[cm])))
    {
      if (d-cd == hd)
        currCell.className = 'selectedCell';
      else if ((d+fdok) % 7 == 0 || (d+fdok) % 7 == 1)
        currCell.className = 'weekendCell';
      else
        currCell.className = 'normalCell';

      currCell.innerHTML=d-cd;
      currCell.onmouseover = onMouseOverCell;
      currCell.onmouseout = onMouseOutCell;
      currCell.onclick = onClickCell;
      calvalarr[d]=''+(d-cd)+'/'+(cm-(-1))+'/'+cy;
    }
    else
    {
      currCell.className = 'normalCell';
      currCell.innerHTML='&nbsp;';
      currCell.onmouseover=null;
      currCell.onmouseout=null;
      currCell.onclick=null;
    }
  }
}

function showCalendar(ielem)
{
  inputTextObj = ielem;  
  var rect = ielem.getBoundingClientRect();  
  var layleft = (window.pageXOffset || document.documentElement.scrollLeft) + rect.left;
  var laytop = (window.pageYOffset || document.documentElement.scrollTop) + rect.bottom;

  var layer = getObj('calendar_div');
  layer.style.position='absolute';
  layer.style.left = layleft + "px";
  layer.style.top = laytop + "px";
  layer.style.display = 'block';

  // First check date is valid
  curdt = ielem.value;
  curdtarr = curdt.split('/');
  isdt = true;
  for(var k = 0; k<curdtarr.length; k++)
  {
    if (isNaN(curdtarr[k])) isdt=false;
  }
  if (isdt&(curdtarr.length == 3))
  {
    ccm = curdtarr[1]-1;
    ccy = curdtarr[2];
    prepareCalendar(curdtarr[0], curdtarr[1]-1, curdtarr[2]);
  }
  else
  {
    prepareCalendar('', ccm, ccy);
  }
}

function showCalendarId(id, lang)
{
  language = lang;
  showCalendar(getObj(id));
}

function getEventTarget(e)
{
  var el;
  if (e.target) el = e.target;
  else if(e.srcElement) el = e.srcElement;
  if (el.nodeType == 3) el = el.parentNode; // defeat Safari bug
  return el;
}

function getEventObject(e)
{
  if (!e) e = window.event;
  return e;
}

function getObj(objID)
{
  if (document.getElementById) {return document.getElementById(objID);}
  else if (document.all) {return document.all[objID];}
  else if (document.layers) {return document.layers[objID];}
}

function checkClick(e)
{
  e? evt = e : evt = event;
  CSE = evt.target? evt.target : evt.srcElement;
  if (CSE.id)
  {
    if (CSE.id.indexOf("_calendar") != -1 ||
      CSE.id == 'prev_month_cal' || CSE.id == 'next_month_cal') return;
  }
  var layer = getObj('calendar_div');
  layer.style.display = "none";
}

function isChild(s, d)
{
  while (s)
  {
    if (s == d) return true;
    s = s.parentNode;
  }
  return false;
}

function onMouseOverCell(e)
{
  getEventTarget(getEventObject(e)).style.fontWeight = 'bold';
}

function onMouseOutCell(e)
{
  getEventTarget(getEventObject(e)).style.fontWeight = 'normal';
}

function onClickCell(e)
{
  var eventObj = getEventObject(e);
  var eventTarget = getEventTarget(eventObj);
  inputTextObj.value = 
    calvalarr[eventTarget.id.substring(1, eventTarget.id.length)];

  var layer = getObj('calendar_div');
  layer.style.display = "none";
}

function caddm()
{
  marr=((ccy%4)==0)? mnl:mnn;
  ccm+=1;
  if (ccm>=12)
  {
    ccm=0;
    ccy++;
  }
  cdayf();
  prepareCalendar('', ccm, ccy);
}

function csubm()
{
  marr=((ccy%4)==0)? mnl:mnn;
  ccm-=1;
  if (ccm<0)
  {
    ccm=11;
    ccy--;
  }
  cdayf();
  prepareCalendar('', ccm, ccy);
}

function cdayf()
{
}

// *** Calendar script ***

var language = 'ca'; // default language
var mnn = new Array('31','28','31','30','31','30','31','31','30','31','30','31');
var mnl = new Array('31','29','31','30','31','30','31','31','30','31','30','31');

var months = new Array();
months['ca'] = new Array('GEN','FEB','MAR','ABR','MAI','JUN','JUL','AGO','SET','OCT','NOV','DES');
months['es'] = new Array('ENE','FEB','MAR','ABR','MAY','JUN','JUL','AGO','SEP','OCT','NOV','DIC');
months['en'] = new Array('JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC');

var dayOfWeek = new Array();
dayOfWeek['ca'] = new Array('DM','DL','DM','DM','DJ','DV','DS');
dayOfWeek['es'] = new Array('D','L','M','M','J','V','S');
dayOfWeek['en'] = new Array('S','M','T','W','T','F','S');

var fdok = 1; // first day of week (0: sunday)
var calvalarr = new Array(42);

writeCalendar();

var now = new Date;
var sccm = now.getMonth();
var sccy = now.getFullYear();
var ccm = now.getMonth();
var ccy = now.getFullYear();
var inputTextObj;

