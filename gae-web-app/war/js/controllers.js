function startsWith(str1, str2) {
  return str1.slice(0, str2.length) == str2;
}

function LoginCtrl($scope, $location) {

  $scope.login = function() {
    $location.path('/my_profile');
  }

}

function MyProfileCtrl($scope, $http, $location) {

  $scope.user = {
    'name':       userInfo[0],
    'pictureURL': userInfo[1],
    'profileURL': userInfo[2],
    'status':     '',
  }

  $scope.save = function() {
    $http.get('/api?action=set_status&status=' + encodeURI($scope.user.status)).success(function(data) {
      $location.path('/common_chat');
    });
  }

}

function extendScopeWithChat($scope, $http) {
  $scope.text = '';
  $scope.reciever = '';

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(SetLocation);
  }

  function SetLocation(location) {
    $scope.latitude  = location.coords.latitude;
    $scope.longitude = location.coords.longitude;
  }

  $scope.send = function() {
    if ($scope.text.length == 0)
      return;

    var url = '/api?action=send_message&text='      + encodeURI($scope.text)
                                    + '&latitude='  + encodeURI($scope.latitude)
                                    + '&longitude=' + encodeURI($scope.longitude)
                                    + '&reciever='  + encodeURI($scope.reciever);
    $http.get(url).success(function(data) {
      $scope.text = '';
    });
  }
}

function CommonChatCtrl($scope, $http, $location, $timeout) {

  $scope.messages = [];
  /*
  [
    {
      'user':    {'id':1, 'pic':'/img/bear.jpg', 'nick':'Мишка Г.', 'about':'гурман, ресторатор'},
      'message': {'text':'Знаю отличное место где много протеиновой еды.', 'date':'12:45 08.06.2013', 'distance': 7}
    }
    , {
      'user':    {'id':2, 'pic':'/img/sasha.jpg', 'nick':'Александра С.', 'about':'музыкант, актер'},
      'message': {'text':'Я тоже... правда надоела уже такая. Но я Мишка с тобой!', 'date':'12:50 08.06.2013', 'distance': 6}
    }
    , {
      'user':    {'id':3, 'pic':'/img/timati.jpg', 'nick':'Тимофей B. S,', 'about':'летчик, вертолетчик'},
      'message': {'text':'Кто-нибудь хочет отзавтракать со мной и моим братом? Я уже лечу!', 'date':'13:00 08.06.2013', 'distance': 10}
    }
    , {
      'user':    {'id':4, 'pic':'/img/proger.jpg', 'nick':'Сергей Б', 'about':'программист'},
      'message': {'text':'Yoyoyo!!1 Горячие чики пойдем со мной в столовку за борщем!', 'date':'13:12 08.06.2013', 'distance': 1}
    }
  ];*/

  extendScopeWithChat($scope, $http);

  $scope.lastTimestamp = 0;
  $scope.interval = 300;
  $scope.getMessages = function() {
    if (!startsWith($location.$$path, '/common_chat')) {
      return;
    }

    var url = '/api?action=get_messages&since='     + encodeURI($scope.lastTimestamp)
                                    + '&latitude='  + encodeURI($scope.latitude)
                                    + '&longitude=' + encodeURI($scope.longitude);
    $http.get(url).
      success(function(data) {
      $scope.messages =  $scope.messages.concat(data);
      if ($scope.messages.length > 0)
          $scope.lastTimestamp = $scope.messages[$scope.messages.length - 1].message.datetime;
        window.scrollTo(0, document.body.scrollHeight);
        $timeout($scope.getMessages, $scope.interval);
      }).
      error(function(data, status) {
        $timeout($scope.getMessages, $scope.interval);
      });
  }
  $scope.getMessages();


  $scope.dialogs_count = 0;
  $scope.getDialogsCount = function(){
    if (!startsWith($location.$$path, '/common_chat')) {
      return;
    }

    var url = '/api?action=get_dialogs_count';
  $http.get(url).
      success(function(data) {
        $scope.dialogs_count = data.count;
        $timeout($scope.getDialogsCount, $scope.interval);
      }).
      error(function(data, status) {
        $timeout($scope.getDialogsCount, $scope.interval);
      });
  }
  $scope.getDialogsCount();
}

function ProfileCtrl($scope, $http, $routeParams, $timeout, $location) {
  $scope.user = {}; //{'id':1, 'pic':'/img/bear.jpg', 'nick':'Мишка Г.', 'about':'гурман, ресторатор', 'distance': 7}
  $scope.messages = [];
  /*[
    {
      'user':    {'id':2, 'pic':'/img/sasha.jpg', 'nick':'Александра С.', 'about':'музыкант, актер', 'distance': 6},
      'message': {'text':'Мишка-Мишка ну куда мы пойдем?', 'date':'12:50 08.06.2013'}
    }, {
      'user':    {'id':1, 'pic':'/img/bear.jpg', 'nick':'Мишка Г.', 'about':'гурман, ресторатор', 'distance': 7},
      'message': {'text':'Давай в 14 у зоопарка встретимся... там близко', 'date':'12:45 08.06.2013'}
    }
  ]*/

  extendScopeWithChat($scope, $http);
  $scope.reciever = $routeParams.id;


  var url = '/api?action=get_profile&id=' + encodeURI($routeParams.id);
  $http.get(url).success(function(data) {
    $scope.user = data;
  });


  $scope.lastTimestamp = 0;
  $scope.interval = 500;
  $scope.getDialog = function() {
    if (!startsWith($location.$$path, '/profile')) {
      return;
    }

    var url = '/api?action=get_dialog&since='     + encodeURI($scope.lastTimestamp)
                                  + '&reciever='  + encodeURI($routeParams.id)
                                  + '&latitude='  + encodeURI($scope.latitude)
                                  + '&longitude=' + encodeURI($scope.longitude);
    $http.get(url).
      success(function(data) {
      $scope.messages =  $scope.messages.concat(data);
      if ($scope.messages.length > 0)
          $scope.lastTimestamp = $scope.messages[$scope.messages.length - 1].message.datetime;
        window.scrollTo(0, document.body.scrollHeight);
        $timeout($scope.getDialog, $scope.interval);
      }).
      error(function(data, status) {
        $timeout($scope.getDialog, $scope.interval);
      });
  }
  $scope.getDialog();
}

function MessagesCtrl($scope, $location, $http, $timeout) {
  $scope.messages = [];
  /*[
    {
      'user':    {'id':1, 'pic':'/img/bear.jpg', 'nick':'Мишка Г.', 'about':'гурман, ресторатор', 'distance': 7},
      'message': {'text':'Давай в 14 у зоопарка встретимся... там близко', 'date':'12:55 08.06.2013'}
    }
  ];*/


  $scope.interval = 500;
  $scope.getDialogs = function() {
    if (!startsWith($location.$$path, '/messages')) {
      return;
    }

    var url = '/api?action=get_dialogs';
    $http.get(url).
      success(function(data) {
      $scope.messages = data;
        $timeout($scope.getDialogs, $scope.interval);
      }).
      error(function(data, status) {
        $timeout($scope.getDialogs, $scope.interval);
      });
  }
  $scope.getDialogs();
}

function MenuCtrl($scope) {}
