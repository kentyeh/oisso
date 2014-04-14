<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="meta.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8"/>
        <title><fmt:message key="userinfo.title"/> - <sec:authorize access="authenticated"><sec:authentication property="principal.username"/></sec:authorize></title>
        </head>
        <body>
        <sec:authorize access="authenticated">
            <section role="page">
                <header style="text-align:right"><span style="color:blue"><sec:authentication property="principal.username"/></span>:<a href="${cp}/j_spring_security_logout" role="button"><fmt:message key="userinfo.logout"/></a></header>
                <table border="1" align="center"><caption style="white-space: nowrap"><fmt:message key="userinfo.title"/></caption>
                    <colgroup><col align="right"/><col style="white-space: nowrap"/></colgroup>
                    <tbody>
                        <tr><td><fmt:message key="login.account"/></td><td><sec:authentication property="principal.username"/></td></tr>
                        <tr><td><fmt:message key="userinfo.fullname"/></td><td>${userInfo.fullname}</td></tr>
                        <%--Extension Exchange Attribute--%>
                        <tr><td><fmt:message key="userinfo.city"/></td><td>${userInfo.city}</td></tr>
                        <tr><td><fmt:message key="userinfo.roles"/></td><td>${userInfo.roles}</td></tr>
                    </tbody>
                </table>
            </section>
        </sec:authorize>
    </body>
</html>