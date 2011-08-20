<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="meta.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8"/>
        ${ie4Html5}
        <title><fmt:message key="userinfo.title"/></title>
    </head>
    <body>
        <sec:authorize access="authenticated">
            <section role="page">
                <header style="text-align:right"><span style="color:blue">${userInfo.nickname}</span>:<a href="${cp}/logout" role="button"><fmt:message key="userinfo.logout"/></a></header>
                <table border="1" align="center"><caption style="white-space: nowrap"><fmt:message key="userinfo.caption"/></caption>
                    <colgroup><col align="right"/><col style="white-space: nowrap"/></colgroup>
                    <tbody>
                        <tr><td><fmt:message key="userinfo.identifier"/></td><td>${userInfo.idenifier}</td></tr>
                    <tr><td><fmt:message key="userinfo.fullname"/></td><td>${userInfo.fullname}</td></tr>
                    <%--Extension Exchange Attribute--%>
                    <tr><td><fmt:message key="city"/></td><td>${userInfo.city}</td></tr>
                    <tr><td><fmt:message key="roles"/></td><td>${userInfo.roles}</td></tr>
                    </tbody>
                </table>
            </section>
        </sec:authorize>
    </body>
</html>