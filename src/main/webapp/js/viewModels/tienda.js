define([ 'knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils',
		'jquery' ], function(ko, app, moduleUtils, accUtils, $) {

	class TiendaViewModel {
		constructor() {
			var self = this;
			
			this.productos = ko.observable([]);

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

		

		connected() {
			accUtils.announce('Tienda page loaded.');
			document.title = "Tienda";
			
			var self= this;
			var data = {
				data: JSON.stringify(info),
				url: "/tienda/getProductos",
				type: "get",
				contentType: "application/json",
				success : function(response) {
					self.productos(response);
				},
				error : function(response) {
					self.error(response.responseJSON.message);
				}
			}
			$.ajax(data);
		};

		disconnected() {
			// Implement if needed
		};

		transitionCompleted() {
			// Implement if needed
		};
	}

	return TiendaViewModel;
});
