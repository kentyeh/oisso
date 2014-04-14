<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@include file="meta.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8"/>
        <title><fmt:message key="error.prone"/></title>
    </head>
    <%
        Exception ex = (Exception) request.getAttribute("exception");
        if (ex != null) {
            pageContext.setAttribute("exClassName", ex.getClass().getSimpleName());
        }
    %>
    <body>
        <section role="page">
            <c:if test="${'404' eq param.errorCode}"><fmt:message key="error.resourceNotfound"/></c:if>
            <c:if test="${not empty exception}">
                <header style="text-align:center"><fmt:message key="error.exception"/> : ${exClassName}</header>
                <div style="text-align:center;color:red;font-weight:bold">${requestScope.exception.message}</div>
            </c:if>
        </section>
    </body>
</html>