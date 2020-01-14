<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<HTML>
  <HEAD>
    <STYLE type="text/css">
      body, td, input, select
      {
        font-family: Arial, Verdana;
        font-size:14px;
      }

      input
      {
        background: #F0F0F0;
      }

      h1
      {
        font-size:18px;
      }

      .col1
      {
        width:100px;
        color: #404040;
      }

      .parametersHeader, .optionsHeader
      {
        font-family: Arial, Verdana;
        font-size:14px;
        font-weight: bold;
        margin-top:20px;
      }

      .execute
      {
        background:#C0C0C0;
      }
    </STYLE>
    <SCRIPT src="${pageContext.request.contextPath}/plugins/working/work.js"
      type="text/javascript"></SCRIPT>
    <SCRIPT type="text/javascript">
      function executeReport()
      {
        var format = document.forms[0]['format'].value;
        if (format === 'csv' || format === 'rtf')
        {
          document.forms[0].submit();
        }
        else
        {
          submitWork('${pageContext.request.contextPath}');
        }
        return false;
      }
    </SCRIPT>
  </HEAD>
  <BODY>
    <DIV id="layer1">
      <H1>${report.title}</H1>
      <TABLE class="properties">
        <TR>
          <TD class="col1">reportId:</TD>
          <TD class="col2">${report.reportId}</TD>
        </TR>
        <TR>
          <TD class="col1">docId:</TD>
          <TD class="col2">${report.docId}</TD>
        </TR>
        <TR>
          <TD class="col1">Technology:</TD>
          <TD class="col2">${report.technology}</TD>
        </TR>
        <TR>
          <TD class="col1">Last update:</TD>
          <TD class="col2">${changeDate}</TD>
        </TR>
        <TR>
          <TD class="col1">Modified by:</TD>
          <TD class="col2">${changeUserId}</TD>
        </TR>
      </TABLE>

      <FORM method="post" action="${pageContext.request.contextPath}/reports/${report.reportId}?exec">
        <DIV class="parametersHeader">Parameters:</DIV>
        <TABLE class="parameters">
          <c:forEach var="par" items="${parameters}">
            <c:if test="${par.forPrompting and fn:toUpperCase(par.name) ne 'USERID'}">
              <TR>
                <TD class="col1">${par.description == null ? par.name : par.description}:</TD>
                <TD class="col2">
                  <INPUT type="text" name="${par.name}" value="${values[par.name]}" />
                </TD>
              </TR>
            </c:if>
          </c:forEach>
        </TABLE>

        <DIV class="optionsHeader">Options:</DIV>
        <TABLE class="options">
          <TR>
            <TD class="col1">Username:</TD>
            <TD class="col2">
              <INPUT type="text" name="userid" value="${userid}"/>
            </TD>
          </TR>
          <TR>
            <TD class="col1">Password:</TD>
            <TD class="col2">
              <INPUT type="password" name="password" value="${password}"/>
            </TD>
          </TR>
          <TR>
            <TD class="col1">Connection:</TD>
            <TD class="col2">
              <INPUT type="text" name="connectionName"
                value="${report.defaultConnectionName}"/>
            </TD>
          </TR>
          <TR>
            <TD class="col1">Format:</TD>
            <TD class="col2">
              <SELECT name="format">
                <OPTION value="pdf">PDF</OPTION>
                <OPTION value="html">HTML</OPTION>
                <OPTION value="csv">CSV</OPTION>
                <OPTION value="rtf">RTF</OPTION>
              </SELECT>
              <INPUT type="submit" value="Execute" class="execute"
                onclick="return executeReport();" />
            </TD>
          </TR>
        </TABLE>

        <INPUT type="hidden" name="username" value="${userid}"/>

        <c:forEach var="par" items="${parameters}">
          <c:if test="${!par.forPrompting}">
            <INPUT type="hidden" name="${par.name}" value="${par.defaultValue}" />
          </c:if>
        </c:forEach>

      </FORM>
    </DIV>
  </BODY>
</HTML>

