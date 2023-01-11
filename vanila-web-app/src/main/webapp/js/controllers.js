angular.module('starter.controllers', [])

.controller('ContentController', function($scope) {
	$scope.toggleLeft = function() {
		$ionicSideMenuDelegate.toggleLeft();
	};
})

.controller('MenuController', function ($scope, $ionicSideMenuDelegate, MenuService, $location) {
	// "MenuService" is a service returning mock data (services.js)
	$scope.list = MenuService.all();

	$scope.toggleLeft = function() {
		$ionicSideMenuDelegate.toggleLeft();
	};

	$scope.goTo = function(page) {
		console.log('Going to ' + page);
		$ionicSideMenuDelegate.toggleLeft();
		$location.url('/' + page);
	};

	$scope.myMessages = 2;
})
	
.controller('LoginController', function ($scope, $location) {
    $scope.nextStep = function() {
		$location.url('/social-screen');
	}
})

.controller('SocialScreenController', function ($scope, $location) {
    $scope.nextStep = function() {
		$location.url('/profile');
	}
})

.controller('ProfileController', function ($scope, $ionicSideMenuDelegate, $location) {
    $scope.save = function() {
		$location.url('/main');
	}
	$scope.showMenu = function() {
		$ionicSideMenuDelegate.toggleLeft();
	}
})

.controller('MainController', function ($scope, $ionicSideMenuDelegate, $location) {
	$scope.showMenu = function() {
		$ionicSideMenuDelegate.toggleLeft();
	}
})

.controller('PeoplesController', function ($scope, $ionicSideMenuDelegate, $location, Peoples) {
	$scope.showMenu = function() {
		$ionicSideMenuDelegate.toggleLeft();
	}

	$scope.peoples = Peoples.all();
	$scope.peoplesRecent = Peoples.recent();
	$scope.peoplesFavorite = Peoples.favorite();

	$scope.showProfile = function(id) {
		$location.url('/show-profile');
	}
})

.controller('ShowProfileController', function ($scope, Peoples, $stateParams, $location) {
	$scope.profile = Peoples.get($stateParams.profileId);

	$scope.favorite = function() {
		alert("set " + $stateParams.profileId + " favorite");
	}

	$scope.sendMessage = function() {
		$location.url('/messages/' + $stateParams.profileId);
	}
})

.controller('PlacesController', function ($scope, $ionicSideMenuDelegate, $location, Places) {
	$scope.showMenu = function() {
		$ionicSideMenuDelegate.toggleLeft();
	}

	$scope.places = Places.all();
})

.controller('ShowPlaceController', function ($scope, Places, $stateParams) {
	$scope.place = Places.get($stateParams.placeId);

	$scope.favorite = function() {
		alert("set " + $stateParams.placeId + " favorite");
	}
})

.controller('MessagesController', function ($scope, Messages, Peoples, $stateParams, $http, $location) {
	/*
	$scope.messages = Messages.recipient($stateParams.recipientId);
	$scope.recipient = Peoples.get($stateParams.recipientId);
	*/

	$scope.user = {};
	$scope.messages = [];

	extendScopeWithChat($scope, $http);
	$scope.reciever = $stateParams.id;


	var url = '/api?action=get_profile&id=' + encodeURI($stateParams.id);
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
	                              + '&reciever='  + encodeURI($stateParams.id)
	                              + '&latitude='  + encodeURI($scope.latitude)
	                              + '&longitude=' + encodeURI($scope.longitude);
	$http.get(url).
	  success(function(data) {
	  $scope.messages =  $scope.messages.concat(data);
	  if ($scope.messages.length > 0)
	      $scope.lastTimestamp = $scope.messages[$scope.messages.length - 1].message.timestamp;
	    window.scrollTo(0, document.body.scrollHeight);
	    $timeout($scope.getDialog, $scope.interval);
	  }).
	  error(function(data, status) {
	    $timeout($scope.getDialog, $scope.interval);
	  });
	}
	$scope.getDialog();
})

.controller('MyProfileController', function ($scope) {
})

.controller('GeneralChatController', function ($scope, Messages, Peoples, $stateParams, $http, $location, $timeout) {
	//$scope.messages = Messages.all();
	$scope.messages = [];

	extendScopeWithChat($scope, $http);

	$scope.showMenu = function() {
		$ionicSideMenuDelegate.toggleLeft();
	}

	$scope.recipients = Messages.recipients();
	$scope.peoples = Peoples.arr($scope.recipients);

	$scope.lastTimestamp = 0;
  	$scope.interval = 300;
  	$scope.getMessages = function() {
    	if (!startsWith($location.$$path, '/general-chat')) {
    		return;
    	}

	    var url = 'http://localhost:8080/api?action=get_messages&since='     + encodeURI($scope.lastTimestamp)
	                                    + '&latitude='  + encodeURI($scope.latitude)
	                                    + '&longitude=' + encodeURI($scope.longitude);
		//console.log(url);
		$http.get(url).success(function(data) {
      		console.log(data);
    	});
	    $http.get(url).success(function(data) {
	    	console.log(data);
			$scope.messages =  $scope.messages.concat(data);
			if ($scope.messages.length > 0)
				$scope.lastTimestamp = $scope.messages[$scope.messages.length - 1].message.timestamp;
			window.scrollTo(0, document.body.scrollHeight);
	        $timeout($scope.getMessages, $scope.interval);
		}).
	    error(function(data, status) {
	    	//console.log(status);
			$timeout($scope.getMessages, $scope.interval);
		});
  	}
	$scope.getMessages();

	$scope.dialogs_count = 0;
	$scope.getDialogsCount = function(){
		if (!startsWith($location.$$path, '/general-chat')) {
			return;
		}

		var url = 'http://localhost:8080/api?action=get_dialogs_count';
		$http.get(url).success(function(data) {
			$scope.dialogs_count = data.count;
			$timeout($scope.getDialogsCount, $scope.interval);
		}).
		error(function(data, status) {
			$timeout($scope.getDialogsCount, $scope.interval);
		});
	}
	$scope.getDialogsCount();
})

.controller('MyChatsController', function ($scope, $ionicSideMenuDelegate, $location, Peoples, Messages) {
	$scope.showMenu = function() {
		$ionicSideMenuDelegate.toggleLeft();
	}
	/*
	$scope.recipients = Messages.recipients();
	$scope.peoples = Peoples.arr($scope.recipients);
	*/

	$scope.messages = [];
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
})
;

function extendScopeWithChat($scope, $http) {
  $scope.text = '';
  $scope.reciever = '2';

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(SetLocation);
  }

  function SetLocation(location) {
    $scope.latitude  = location.coords.latitude;
    $scope.longitude = location.coords.longitude;
  }

  $scope.send = function() {
    if ($scope.message.text.length == 0)
      return;

    var url = 'http://localhost:8080/api?action=send_message&text=' + encodeURI($scope.message.text)
                                    + '&latitude='  + encodeURI($scope.latitude)
                                    + '&longitude=' + encodeURI($scope.longitude)
                                    + '&reciever='  + encodeURI($scope.reciever);
    console.log(url);
    $http.get(url).success(function(data) {
      $scope.text = '';
    });
  }
}

function startsWith(str1, str2) {
  return str1.slice(0, str2.length) == str2;
}