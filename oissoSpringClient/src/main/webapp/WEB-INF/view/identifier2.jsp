<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="meta.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
                    <c:set var="hasInfo" value="true"/>
                    <div align="center" style="color:red;font-weight:bold;text-align: center" id="msgArea">
                        <c:if test="${'expired' eq param.cause}"><fmt:message key="identifier.expired"/></c:if>
                        <c:if test="${'sessionExceed' eq param.cause}"><fmt:message key="identifier.sessionExceed"/></c:if>
                        <c:if test="${not empty param.authfailed}"><fmt:message key="error.exception"/> : ${SPRING_SECURITY_LAST_EXCEPTION.message}</c:if>
                        <c:if test="${not empty requestScope.errorMessage}">${requestScope.errorMessage}</c:if>                                        
                    </div>
                </c:if>
                <c:if test="${not empty hasInfo}">
                    <form action="${cp}/j_spring_openid_security_check" method="post" style="text-align:center">
                        <input type="hidden" name="openid_identifier" value="${applicationScope.directLoginUrl}"/>
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <input type="submit" value="<fmt:message key="identifier.Login"/>"/>
                    </form>
                </c:if>
                <c:if test="${empty hasInfo}">
                    <c:redirect url="/j_spring_openid_security_check?openid_identifier=${applicationScope.directLoginUrl}"/>
                </c:if>
        </section>
    </body>
</html>