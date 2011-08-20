<%@ page pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
    response.setHeader("Expires", "-1");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Content-Type", "text/html; charset=UTF-8");
%>

<%--Context path--%>
<c:set var="cp" value="${pageContext.request.contextPath}"/>

<%--Full path--%>
<c:set var="fp">${pageContext.request.scheme}://${pageContext.request.serverName}<c:if test="${80 ne pageContext.request.localPort}">:${pageContext.request.localPort}</c:if>${cp}</c:set>

<%--JQuery--%>
<c:set var="jQueryVersion" value="1.6.2"/>
<c:set var="jQuery"><script src="http://ajax.googleapis.com/ajax/libs/jquery/${jQueryVersion}/jquery.min.js"></script></c:set>
<%--Modernizr 2--%>
<c:set var="Modernizr"><script src="http://www.modernizr.com/downloads/modernizr-latest.js"></script></c:set>
<%--Focus on first input element--%>
<c:set var="firstInputFocus">
    <script><!--
        $(function() {
            $("input:text:visible:first").focus();
        });
        --></script></c:set>

<%--Html5 compatible with IE9 before version--%>
<c:set var="ie4Html5">
    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
</c:set>