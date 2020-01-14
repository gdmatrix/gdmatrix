<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<HTML>
  <HEAD>
    <STYLE>
      .text {
        font-family:arial;
        font-size:12px;
        font-weight:normal}
      .info
      {
        font-family:arial;
        font-size:12px;
        font-weight:normal;
        display:block;
      }
      H3.text {font-size:12px; font-weight:bold}
      .table {
        border-collapse:collapse;
        border-style:solid;
        border-width:1px;
        border-color:blue;
        font-size:11px;
        width:300px}
      .table TR TD {
        border-style:solid;
        border-width:1px 1px 1px 1px;
        border-color:#D0D0DF;
        padding:2px;}
      .table TR.row1 {
        background:#FEFEFE;
      }
      .table TR.row2 {
        background:#F0F0F0;
      }
      .metadata, .code
      {
        font-family:"courier new";
        font-size:12px;
      }

    </STYLE>
  </HEAD>
<BODY class="text">
<H3 class="text">Variables predefinides</H3>
<UL>
  <LI><SPAN class="code">d</SPAN>: document (Document)</LI>
  <LI><SPAN class="code">c</SPAN>: expedient (Case)</LI>
  <LI><SPAN class="code">o</SPAN>: document o expedient si no hi ha document</LI>
</UL>

<H3 class="text">Funcions</H3>
<UL>
  <LI>
    <SPAN class="code">addDate(dateTime, increment, unit)</SPAN>
    <SPAN class="info">Incrementa la data <SPAN class="code">dateTime</SPAN>, <SPAN class="code">increment</SPAN>
    unitats de tipus <SPAN class="code">unit</SPAN>.</SPAN>
  </LI>

  <LI>
    <SPAN class="code">parseDate(text, pattern)</SPAN>
    <SPAN class="info">Converteix <SPAN class="code">text</SPAN> a data segons el patr&oacute;
    <SPAN class="code">pattern</SPAN>.</SPAN>
  </LI>

  <LI>
    <SPAN class="code">formatDate(dateTime, pattern)</SPAN>
    <SPAN class="info">Dona format a la data <SPAN class="code">dateTime</SPAN> segons el patr&oacute; <SPAN class="code">pattern</SPAN>.</SPAN>
  </LI>
</UL>

<SPAN class="text">Els valors acceptars pels par&agrave;metres 
  <SPAN class="code">unit</SPAN> i <SPAN class="code">pattern</SPAN> són:</SPAN>
<TABLE class="table">
  <TR class="row1">
    <TD class="metadata">y</TD>
    <TD>Anys</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">M</TD>
    <TD>Mesos</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">d</TD>
    <TD>Dies</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">H</TD>
    <TD>Hores</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">m</TD>
    <TD>Minuts</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">s</TD>
    <TD>Segons</TD>
  </TR>
</TABLE>

<H3 class="text">Metadades b&agrave;siques de document</H3>
<TABLE class="table">
  <TR class="row1">
    <TD class="metadata">docId</TD>
    <TD>Identificador del document</TD>
  </TR>
  <TR class="row2">
    <TD class="metadata">version</TD>
    <TD>Versi&oacute; del document</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">title</TD>
    <TD>T&iacute;tol del document</TD>
  </TR>
  <TR class="row2">
    <TD class="metadata">description</TD>
    <TD>Descripci&oacute;</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">docTypeId</TD>
    <TD>Tipus del document</TD>
  </TR>
  <TR class="row2">
    <TD class="metadata">state</TD>
    <TD>Estat del document</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">language</TD>
    <TD>Idioma del document</TD>
  </TR>
  <TR class="row2">
    <TD class="metadata">classId</TD>
    <TD>S&egrave;rie del document (mulitvalor)</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">captureDateTime</TD>
    <TD>Data/hora de captura</TD>
  </TR>
  <TR class="row2">
    <TD class="metadata">captureUserId</TD>
    <TD>Usuari que ha capturat el document</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">changeDateTime</TD>
    <TD>Data de la darrera modificaci&oacute;</TD>
  </TR>
  <TR class="row2">
    <TD class="metadata">changeUserId</TD>
    <TD>Usuari que ha fet la darrera modifcaci&oacute;</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">creationDate</TD>
    <TD>Data de creaci&oacute;</TD>
  </TR>
</TABLE>

<H3 class="text">Metadades b&agrave;siques d'expedient</H3>
<TABLE class="table">
  <TR class="row1">
    <TD class="metadata">caseId</TD>
    <TD>Identificador de l'expedient</TD>
  </TR>
  <TR class="row2">
    <TD class="metadata">title</TD>
    <TD>T&iacute;tol del document</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">description</TD>
    <TD>Descripci&oacute;</TD>
  </TR>
  <TR class="row2">
    <TD class="metadata">caseTypeId</TD>
    <TD>Tipus de l'expedient</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">classId</TD>
    <TD>S&egrave;rie de l'expedient (mulitvalor)</TD>
  </TR>
  <TR class="row2">
    <TD class="metadata">startDate</TD>
    <TD>Data d'obertura de l'expedient</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">endDate</TD>
    <TD>Data de tancament de l'expedient</TD>
  </TR>
  <TR class="row2">
    <TD class="metadata">creationDateTime</TD>
    <TD>Data de creaci&oacute; de l'expedient.</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">creationUserId</TD>
    <TD>Usuari que va crear l'expedient.</TD>
  </TR>
  <TR class="row2">
    <TD class="metadata">changeDateTime</TD>
    <TD>Data de la darrera modificaci&oacute;</TD>
  </TR>
  <TR class="row1">
    <TD class="metadata">changeUserId</TD>
    <TD>Usuari que ha fet la darrera modifcaci&oacute;</TD>
  </TR>
</TABLE>
</BODY>
</HTML>