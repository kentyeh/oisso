<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="meta.jsp" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8"/>
        <title><fmt:message key="index.title"/></title>
    </head>
    <body>
        <section>
            <div align="center"><a href="${cp}/identifier1"><fmt:message key="index.link.identifier"/></a></div>
            <div align="center"><a href="${cp}/userinfo"><fmt:message key="index.link.userinfo"/></a></div>
        </section>
    </body>
</html>