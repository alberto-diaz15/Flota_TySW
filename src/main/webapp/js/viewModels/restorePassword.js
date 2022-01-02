define([ 'knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils',
		'jquery' ], function(ko, app, moduleUtils, accUtils, $) {

	class RestorePasswordViewModel {
		constructor() {
			var self = this;
			
			self.email = ko.observable("pepe@pepe.com");
			self.pwd1 = ko.observable("pepe123");
			self.pwd2 = ko.observable("pepe123");
			
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
		}
		
		restore() {
			var self = this;
			var info = {
				email : this.email(),
				pwd1 : self.pwd1(),
				pwd2 : self.pwd2()
			};
			var data = {
				data : JSON.stringify(info),
				url : "user/restorePwd",
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
			accUtils.announce('Restore Password page loaded.');
			document.title = "Restore Password";
		};

		disconnected() {
			// Implement if needed
		};

		transitionCompleted() {
			// Implement if needed
		};
	}

	return RestorePasswordViewModel;
});