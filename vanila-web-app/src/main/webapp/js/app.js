angular.module('lunch2meet', []).
  config(['$routeProvider', function($routeProvider) {
  $routeProvider.
    when('/my_profile',  {templateUrl: 'partials/my_profile.html',  controller: MyProfileCtrl}).
    when('/common_chat', {templateUrl: 'partials/common_chat.html', controller: CommonChatCtrl}).
    when('/profile/:id', {templateUrl: 'partials/profile.html',     controller: ProfileCtrl}).
    when('/messages',    {templateUrl: 'partials/messages.html',    controller: MessagesCtrl}).
    when('/menu',        {templateUrl: 'partials/menu.html',        controller: MenuCtrl}).
    otherwise({redirectTo: '/my_profile'});
}]);