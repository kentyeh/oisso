<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="meta.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:url value="${applicationScope.providerLogoutUrl}" var="openIdLogoutUrl">
    <c:param name="spring-security-redirect" value="${fp}/j_spring_security_logout"/>
    <c:param name="${_csrf.parameterName}" value="${_csrf.token}"/>
</c:url>
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
                <table style="width: 100%">
                    <tr>
                        <td style="text-align: left">
                            <a href="${cp}/"><fmt:message key="index.index"/></a>
                        </td>
                        <td style="text-align: right">
                            <span style="color:blue"><sec:authentication property="principal.username"/></span>:
                            <form action="${cp}/j_spring_security_logout" method="post" style="display: inline">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <input type="submit" value="<fmt:message key="userinfo.logout"/>"/>
                            </form>&nbsp;|&nbsp;
                            <a href="${openIdLogoutUrl}" role="button"><fmt:message key="userinfo.provider.logout"/></a>    
                        </td>
                    </tr>
                </table>
                <table border="1" align="center"><caption style="white-space: nowrap"><fmt:message key="userinfo.caption"/></caption>
                    <colgroup><col align="right"/><col style="white-space: nowrap"/></colgroup>
                    <tbody>
                        <tr><td><fmt:message key="userinfo.identifier"/></td><td>${userinfo.userid}</td></tr>
                        <tr><td><fmt:message key="userinfo.fullname"/></td><td>${userinfo.fullname}</td></tr>
                        <%--Extension Exchange Attribute--%>
                        <tr><td><fmt:message key="userinfo.city"/></td><td>${userinfo.city}</td></tr>
                        <tr><td><fmt:message key="userinfo.roles"/></td><td><sec:authentication property="principal.authorities" /></td></tr>
                    </tbody>
                </table>
            </section>
        </sec:authorize>
    </body>
</html>