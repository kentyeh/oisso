<%@ page pageEncoding="UTF-8" contentType="application/xrds+xml"%><?xml version="1.0" encoding="UTF-8"?>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="ep">${pageContext.request.scheme}://${pageContext.request.serverName}<c:if test="${80 ne pageContext.request.localPort}">:${pageContext.request.localPort}</c:if>${pageContext.request.contextPath}/${empty account?"anonymous":account}</c:set>
<xrds:XRDS xmlns:xrds="xri://$xrds">
    <XRD xmlns="xri://$xrd*($v*2.0)">
            <Service priority="10">
                <Type>http://specs.openid.net/auth/2.0/server</Type>
                <URI>${ep}</URI>
            </Service>
            <Service priority="20">
                <Type>http://specs.openid.net/auth/2.0/signon</Type>
                <URI>${ep}</URI>
            </Service>
            <Service priority="30">
                <Type>http://openid.net/srv/ax/1.0</Type>
                <URI>${ep}</URI>
            </Service>
            <Service priority="40">
                <Type>http://openid.net/sreg/1.0</Type>
                <URI>${ep}</URI>
            </Service>
        </XRD>
</xrds:XRDS>