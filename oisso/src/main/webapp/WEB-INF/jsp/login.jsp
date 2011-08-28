<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="meta.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:if test="${not empty account}"><c:redirect url="/userinfo" /></c:if>
<c:if test="${empty account}">
    <!doctype html>
    <html>
        <head>
            <meta charset="utf-8"/>
            <title><fmt:message key="login.title"/></title>
            ${Modernizr}
            ${jQuery}
            ${ie4Html5}
            <script>
                window.onload=function(){
                    if(!Modernizr.input.required) {
                        $("form:first").submit(function(){
                            if($("userid").val().length<0){
                                $("msgArea").val("<fmt:message key="login.account"/><fmt:message key="login.notAllowEmpty"/>");
                                return false;
                            }
                            if($("password").val().length<0){
                                $("msgArea").val("<fmt:message key="login.password"/><fmt:message key="login.notAllowEmpty"/>");
                                return false;
                            }
                        });
                    }
                };
            </script>
        </head>
        <body>
            <section data="page">
                <header style="text-align: center"><fmt:message key="login.caption"/></header>
                <c:if test="${not empty param.cause or not empty param.authfailed or not empty requestScope.errorMessage}">
                    <div align="center" style="color:red;font-weight:bold;text-align: center" id="msgArea">
                        <c:if test="${'expired' eq param.cause}"><fmt:message key="login.expired"/></c:if>
                        <c:if test="${'sessionExceed' eq param.cause}"><fmt:message key="login.sessionExceed"/></c:if>
                        <c:if test="${not empty param.authfailed}"><fmt:message key="error.exception"/> : ${SPRING_SECURITY_LAST_EXCEPTION.message}</c:if>
                        <c:if test="${not empty requestScope.errorMessage}">${requestScope.errorMessage}</c:if>                                        
                    </div>
                </c:if>
                <form action="${cp}/j_spring_security_check" method="post">
                    <table border="0" align="center"><tbody>
                            <tr><td rowSpan="2">${logo}</td>
                            	  <td align="right"><label for="userid"><fmt:message key="login.account"/></label></td><td>
                                    <input type="text" required aria-required="true" placeholder="<fmt:message key="login.account.placeholder"/>" aria-required="true" id="userid" name="j_username" value="${sessionScope.loginId}"/></td></tr>
                            <tr><td align="right"><label for="password"><fmt:message key="login.password"/></label></td><td>
                                    <input type="password" required aria-required="true" id="password" name="j_password"/></td></tr>
                            <tr><td d colSpan="2">&nbsp;</td><td><input id="rememberMe" name="_spring_security_remember_me" type="checkbox" value="true"/>
                                    <label for="rememberMe"><fmt:message key="login.remeberMe"/></label></td>
                            <tr><td colSpan="3" align="center"><input type="submit"/><span style="width:300px">&nbsp;</span>
                                    <input type="reset"/></td></tr>
                        </tbody></table>
                </form>
                <table align="center" border="1">
                    <tr><th colSpan="2"><fmt:message key="login.trialAccount"/></th></tr>
                    <tr><th><fmt:message key="login.account"/></th><th><fmt:message key="login.password"/></th></tr>
                    <tr><td>admin</td><td>admin</td></tr>
                    <tr><td>user</td><td>user</td></tr>
                </table>
            </section>
            ${firstInputFocus}
        </body>
    </html>
</c:if>