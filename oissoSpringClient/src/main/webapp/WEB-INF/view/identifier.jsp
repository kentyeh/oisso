<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="meta.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<spring:eval expression="@appProperies['httpPort']" var="httpPort"/>
<sec:authorize access="authenticated">
    <c:redirect url="/userinfo"/>
</sec:authorize>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8"/>
        ${Modernizr}
        ${jQuery}
        ${ie4Html5}
        <title><fmt:message key="identifier.title"/></title>
        <script>
            window.onload=function(){
                if(!Modernizr.input.required) {
                    $("form:first").submit(function(){
                        return $("identifier").val().length>0;
                    });
                }
            };
        </script>
    </head>
    <body>
        <section data="page">
            <header style="text-align: center"><fmt:message key="identifier.caption"/></header>
                <c:if test="${not empty param.cause or not empty param.authfailed or not empty requestScope.errorMessage}">
                    <div align="center" style="color:red;font-weight:bold;text-align: center" id="msgArea">
                        <c:if test="${'expired' eq param.cause}"><fmt:message key="identifier.expired"/></c:if>
                        <c:if test="${'sessionExceed' eq param.cause}"><fmt:message key="identifier.sessionExceed"/></c:if>
                        <c:if test="${not empty param.authfailed}"><fmt:message key="error.exception"/> : ${SPRING_SECURITY_LAST_EXCEPTION.message}</c:if>
                        <c:if test="${not empty requestScope.errorMessage}">${requestScope.errorMessage}</c:if>                                        
                    </div>
                </c:if>
        <form action="${cp}/j_spring_openid_security_check" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <table align="center">
                <tr><td><fmt:message key="identifier.Input"/> &quot;http://localhost:${httpPort}/oisso/admin&quot; <fmt:message key="identifier.or"/> &quot;http://localhost:${httpPort}/oisso/user&quot;</td></tr></thead>
                <tbody><tr><td><input type="text" placeholder="<fmt:message key="identifier.idenifier.placeholder"/>" name="openid_identifier" id="identifier" required aria-required="true" style="width:100%" 
                                      value="http&#58;&#47;&#47;localhost:${httpPort}&#47;oisso&#47;admin"/></td></tr>
                </tbody>
                <tfoot><tr><td align="center"><input type="submit"/></td></tr></tfoot>
            </table>
        </form>
        </section>
        ${firstInputFocus}
    </body>
</html>