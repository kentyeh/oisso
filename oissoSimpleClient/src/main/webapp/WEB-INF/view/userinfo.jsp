<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="meta.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>
<s:eval expression="@appProperies['httpPort']" var="httpPort"/>--%>
<c:set var="index">http://${pageContext.request.serverName}:${httpPort}${pageContext.request.contextPath}</c:set>
<c:url var="openIdLogoutUrl" value="${applicationScope.providerLogoutUrl}">
    <c:param name="spring-security-redirect" value="${fp}/logout"/>
</c:url>

<!doctype html>
<html>
    <head>
        <meta charset="utf-8"/>
        ${ie4Html5}
        <title><fmt:message key="userinfo.title"/>**${fp}</title>
    </head>
    <body>
    <sec:authorize access="authenticated">
        <section role="page">
            <table style="width: 100%">
                <tr>
                    <td style="text-align: left"><a href="${cp}/"><fmt:message key="index.index"/></a></td>
                    <td style="text-align: right"><span style="color:blue">${userInfo.nickname}</span>:
                        <a href="${cp}/logout" role="button"><fmt:message key="userinfo.logout"/></a>&nbsp;|&nbsp;
                        <a href="${openIdLogoutUrl}" role="button"><fmt:message key="userinfo.provider.logout"/></a></td>
                </tr>
            </table>
            <table border="1" align="center"><caption style="white-space: nowrap"><fmt:message key="userinfo.caption"/></caption>
                <colgroup><col align="right"/><col style="white-space: nowrap"/></colgroup>
                <tbody>
                    <tr><td><fmt:message key="userinfo.identifier"/></td><td>${userInfo.idenifier}</td></tr>
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