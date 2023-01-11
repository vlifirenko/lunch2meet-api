<%@page import="com.lunch2meet.daos.impl.UserDaoGAE"%>
<%@page import="com.lunch2meet.daos.UserDao"%>
<%@page import="com.lunch2meet.dto.User"%>
<%@page import="com.google.appengine.api.datastore.Email"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%
  String id = (String) session.getAttribute("id");
  if (id == null) {
    response.sendRedirect("/index.jsp");
  }
%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7" ng-app="lunch2meet"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8" ng-app="lunch2meet"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9" ng-app="lunch2meet"> <![endif]-->
<!--[if gt IE 8]><!-->
<html class="no-js" ng-app="lunch2meet"> <!--<![endif]-->
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
    <body>
        <!--[if lt IE 7]>
            <p class="chromeframe">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> or <a href="http://www.google.com/chromeframe/?redirect=true">activate Google Chrome Frame</a> to improve your experience.</p>
        <![endif]-->

        <script type="text/javascript">
        <%UserDao userDao = new UserDaoGAE();
          if (id != null) {
            User user = userDao.getById(id);

            out.println("var userInfo = ['" + user.name + "',");
            out.println("'" + user.pictureURL + "',");
            out.println("'" + user.profileURL + "'];");
          }%>
        </script>

        <div id="layout-div" ng-view></div>

        <!-- <script src="http://code.jquery.com/jquery-1.10.1.min.js"></script>
        <script src="js/plugins.js"></script>
        <script src="js/main.js"></script> -->
        <script src="js/angular.min.js"></script>
        <script src="js/app.js"></script>
        <script src="js/controllers.js"></script>
    </body>
</html>
