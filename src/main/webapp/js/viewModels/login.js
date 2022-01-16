define([ 'knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils',
		'jquery' ], function(ko, app, moduleUtils, accUtils, $) {

	class LoginViewModel {
		constructor() {
			var self = this;
			
			self.userName = ko.observable("andres2");
			self.pwd = ko.observable("pepe");
			self.message = ko.observable();
			self.error = ko.observable();
			
			// Header Config
			self.headerConfig = ko.observable({
				'view' : [],
				'viewModel' : null
			});
			moduleUtils.createView({
				'viewPath' : 'views/header.html'
			}).then(function(view) {
				self.headerConfig({
					'view' : view,
					'viewModel' : app.getHeaderModel()
				})
			})
			
			$.ajax({
				url : "/user/heLlegado",
				type : "get",
				success : function(response) {
					if(response != ''){
						app.router.go( { path : "games"} )
						sessionStorage.setItem("userName", response.name)
						sessionStorage.setItem("userId",response.id)	
					}
				}
			});
		}

		login(googleUser) {
			var self = this;
			var info;
			
			if (googleUser){
				info = {
					name : googleUser.getBasicProfile().getName(),
					email : googleUser.getBasicProfile().getEmail(),
					googleId : googleUser.getBasicProfile().getId(),
					origen : "Google"
				}
			}else{
				info = {
					name : this.userName(),
					pwd : this.pwd()
				};
			}
			 
			var data = {
				data : JSON.stringify(info),
				url : "user/login",
				type : "post",
				contentType : 'application/json',
				success : function(response, a, b) {
					sessionStorage.setItem("userName", response.name)
					app.router.go( { path : "games"} );
				},
				error : function(response) {
					self.error(response.responseJSON.message);
				}
			};
			$.ajax(data);
		}
		
		register() {
			app.router.go( { path : "register" } );
		}
		
		restore() {
			var self = this;
			var info = {
				name : this.userName()
			};
			var data = {
				data : JSON.stringify(info),
				url : "user/sendRestorePwd",
				type : "post",
				contentType : 'application/json',
				success : function(response) {
					app.router.go( { path : "login"} );
				},
				error : function(response) {
					self.error(response.responseJSON.message);
				}
			};
			$.ajax(data);		}

		connected() {
			accUtils.announce('Login page loaded.')
			document.title = "Login"
			let self = this
			let divGoogle = document.createElement("div")
			divGoogle.setAttribute("id", "my-signin2")
			document.getElementById("zonaGoogle").appendChild(divGoogle)
			gapi.signin2.render('my-signin2', {
		        'scope': 'profile email',
		        'width':150,
		        'height': 50,
		        'longtitle': false,
		        'theme': 'dark',
		        'onsuccess': function(googleUser) {
	        		self.login(googleUser);
				},
		        'onfailure': function(error) {
		        	alert(error);
		        	console.log(error);
		        }
		      });
		};

		disconnected() {
			 if(!gapi.auth2){
			    gapi.load('auth2', function() {
			        gapi.auth2.init();
			    });
			 }
		    var auth2 = gapi.auth2.getAuthInstance();
		    auth2.signOut().then(function () {
		        auth2.disconnect();
		    });


		};

		transitionCompleted() {
			// Implement if needed
		};
	}

	return LoginViewModel;
});
