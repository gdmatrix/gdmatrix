<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>print_setup</title>
    <style type="text/css">
      .title {font-size:12px;}
    </style>
  </head>
  <body style="font-family:verdana;font-size:11px">
    <form action="/pdfprint/print.pdf" method="get">
      <table style="margin-left:auto;margin-right:auto;background-color:#F0F0F0;border-style:outset;border-width:2px;">
        <tr>
          <td colspan="3" class="title" height="30px">Opcions d'impressi&oacute;</td>
        </tr>
        <tr style="vertical-align:top">
          <td>
            <table style="width:120px; width:120px;border-style:solid; border-color:gray; border-width:1px">
              <tr>
                <td style="font-weight:bold">
                  Escala:
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="scale" value="100" />
                  1/100
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="scale" value="250"/>
                  1/250
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="scale" checked="checked" value="500"/>
                  1/500
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="scale" value="1000"/>
                  1/1000
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="scale" value="2000"/>
                  1/2000
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="scale" value="5000"/>
                  1/5000
                </td>
              </tr>
            </table>
          </td>
          <td>
            <table style="width:120px; border-style:solid; border-color:gray; border-width:1px">
              <tr>
                <td style="font-weight:bold">
                  Paper:
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="paper" value="A4"
                         checked="checked"/>
                  DIN-A4
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="paper" value="A3"/>
                  DIN-A3
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="paper" value="A2"/>
                  DIN-A2
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="paper" value="A1"/>
                  DIN-A1
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="paper" value="A0"/>
                  DIN-A0
                </td>
              </tr>
            </table>
          </td>

          <td>
            <table style="width:120px; border-style:solid; border-color:gray; border-width:1px">
              <tr>
                <td style="font-weight:bold">
                  Orientaci&oacute;:
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="or" value="p" checked="checked" />
                  Vertical
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="or" value="l" />
                  Apaisat
                </td>
              </tr>
            </table>
          </td>          
        </tr>
        <tr>
          <td colspan="3" style="text-align:center">
            <input type="button" name="print" value="Imprimir" 
              onclick="this.disabled=true;document.getElementById('pmessage').style.visibility='visible';submit();"
              style="background-color:#F0F0F0; border-style:outset;font-family:verdana;font-size:12px;" />
          </td>
        </tr>
        <tr>
          <td colspan="3" style="text-align:center">
            <div id="pmessage" style="visibility:hidden">
              <p>Imprimint document, esperi si us plau...</p> 
            </div>
          </td>
        </tr>
      </table>
      
    </form></body>
</html>