<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	String callback = request.getParameter("callback");
	if(callback == null || "".equals(callback)){
	    out.print("{\"abc\":123}");
	}else{
	    out.print(callback+"({\"abc\":123})");
	}
	
%>