<%@ page import="java.util.List, java.util.Map" %>
<html>
<head>
  <title>
    Web Search Engines
  </title>
</head>
<body>
<form method="GET" action="/wse/search.do">
  <label for="search">Input your search term here:
    <input id="search" type="text" val="" placeholder="Search term here" name="q"/>
  </label>
  <button type="submit">Search</button>
</form>
<% List<Map<String, String>> results = (List<Map<String, String>>) request.getAttribute("results");
    if(results == null){ %>
      <div>Results is null</div>
    <%} %>
  <% for(int i=0; results != null && i < results.size(); i++){
    Map<String, String> res = results.get(i);
  %>
<div>
  <h3><%= res.get("title") %></h3>
  <span><%= res.get("url") %></span>
</div>
<% } %>
</body>
</html>
