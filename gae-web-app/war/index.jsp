<%@page import="com.lunch2meet.daos.impl.UserDaoGAE"%>
<%@page import="com.lunch2meet.utils.JSONUtils"%>
<%@page import="com.lunch2meet.dto.User"%>
<%@page import="com.lunch2meet.utils.GoogleAuthHelper"%>
<%@page import="com.google.appengine.api.datastore.Email"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%@page import="com.google.appengine.labs.repackaged.org.json.JSONObject" %>
<%
  UserDaoGAE userDao = new UserDaoGAE();
  String id = (String) session.getAttribute("id");
  if (id != null) {
    User user = userDao.getById(id);
    if (user != null) {
      response.sendRedirect("/app.jsp");
    }
  }


  final GoogleAuthHelper helper = new GoogleAuthHelper();
  if (request.getParameter("code") != null && request.getParameter("state").equals("google")) {
    JSONObject json = null;
    try {
      json = helper.getUserInfoJson(request.getParameter("code"));

      User user = userDao.getByEmail((String) json.get("email"));
      if (user == null) {
        user = userDao.createUser(helper.deserializeUser(json));
      }

      session.setAttribute("id", user.id);
      response.sendRedirect("/app.jsp");
    } catch (Exception e) {
      e.printStackTrace();
      response.sendRedirect("/logout.jsp");
    }
  }
%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7" ng-app="lunch2meet"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8" ng-app="lunch2meet"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9" ng-app="lunch2meet"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js" ng-app="lunch2meet"> <!--<![endif]-->
  <head>
      <meta charset="utf-8">
      <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
      <title></title>
      <meta name="description" content="">
      <meta name="viewport" content="width=device-width">

      <!-- Place favicon.ico and apple-touch-icon.png in the root directory -->

      <link rel="stylesheet" href="css/normalize.css">
      <link rel="stylesheet" href="css/main.css">
      <link rel="stylesheet" href="css/bootstrap.min.css">
      <link rel="stylesheet" href="css/bootstrap-responsive.min.css">
      <link rel="stylesheet" href="css/custom.css">
      <script src="js/vendor/modernizr-2.6.2.min.js"></script>
  </head>
  <body id="front">
    <center>
      <!--[if lt IE 7]>
          <p class="chromeframe">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> or <a href="http://www.google.com/chromeframe/?redirect=true">activate Google Chrome Frame</a> to improve your experience.</p>
      <![endif]-->

      <h1>lunch&meet</h1>
      <div id="locAlert" class="alert">
        <strong>Внимание!</strong> Для использования сервиса разрешите определить ваше местоположение.
      </div>
      <!-- <div id="slogan">Обед &ndash; время для встреч</div> -->
      <a href="<% out.println(helper.buildLoginUrl()); %>" id="enterBtn" class="btn btn-primary hidden">Войти через<br/>Google Account</a>

      <div id="logos">
        <a href="https://appengine.google.com/" title="Google App Engine"><img src="/img/googleappengine.png" /></a>
        <a href="http://oauth.net/2/" title="OAuth 2.0"><img src="/img/oauth.png" /></a>
        <a href="http://angularjs.org/" title="AngularJS"><img src="/img/angularjs.png" /></a>
        <a href="http://twitter.github.io/bootstrap/" title="Twitter Bootstrap"><img src="/img/bootstrap.png" /></a>
      </div>

      <script src="http://code.jquery.com/jquery-1.10.1.min.js"></script>
      <script type="text/javascript">
        if (navigator.geolocation) {
          navigator.geolocation.getCurrentPosition(SetLocation);
        }

        function SetLocation(location) {
          $('#locAlert').hide();
          $('#enterBtn').removeClass('hidden');
        }
      </script>

    </center>
  </body>
</html>
