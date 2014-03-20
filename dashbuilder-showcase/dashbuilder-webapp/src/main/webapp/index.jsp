<%
  final String queryString = request.getQueryString();
  final String redirectURL = "org.dashbuilder.DashbuilderShowcase/Dashbuilder.html" + (queryString == null ? "" : "?" + queryString);
  response.sendRedirect(redirectURL);
%>
