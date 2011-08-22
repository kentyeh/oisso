<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="meta.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>    
<c:if test="${not empty userInfo}">
    <c:redirect url="/userinfo"/>
</c:if>
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
        <form action="${cp}/login" method="post">
            <table align="center">
                <thead><tr><th><fmt:message key="identifier.caption"/></th></tr>
                <tr><td><fmt:message key="identifier.Input"/> &quot;http://localhost:8080/oisso/admin&quot; <fmt:message key="identifier.or"/> &quot;http://localhost:8080/oisso/user&quot;</td></tr></thead>
                <tbody><tr><td><input type="text" placeholder="<fmt:message key="identifier.idenifier.placeholder"/>" name="identifier" id="identifier" required aria-required="true" style="width:100%" 
                                      value="http&#58;&#47;&#47;localhost:8080&#47;oisso&#47;admin"/></td></tr></tbody>
                <tfoot><tr><td align="center"><input type="submit"/></td></tr></tfoot>
            </table>
        </form>
        ${firstInputFocus}
    </body>
</html>