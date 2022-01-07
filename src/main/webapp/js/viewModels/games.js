define([ 'knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils',
		'jquery' ], function(ko, app, moduleUtils, accUtils, $) {

	class MenuViewModel {
		constructor() {
			let self = this;

			self.games = ko.observableArray([]);
			self.matches = ko.observableArray([]);

			self.x = ko.observable(null);
			self.y = ko.observable(null);

			self.error = ko.observable(null);
						
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
			});
		}

		connected() {
			accUtils.announce('Juegos.');
			document.title = "Juegos";

			let self = this;

			let data = {
				type : "get",
				url : "/games/getGames",
				success : function(response) {
					self.games(response);
				},
				error : function(response) {
					console.error(response);
					self.error(response);
				}
			}
			$.ajax(data);
		};

		mover(match) {
			let self = this;

			let info = {
				matchId : match.id,
				x : this.x(),
				y : this.y()
			};

			let data = {
				type : "post",
				url : "/games/move",
				data : JSON.stringify(info),
				contentType : "application/json",
				success : function(response) {
					console.log(JSON.stringify(response));
				},
				error : function(response) {
					console.error(response);
					self.error(response.responseJSON.message);
				}
			}
			$.ajax(data);
		}
		
		actualizarTablero(x,y){
			//Preguntar si en barcosMatch puedo quitar el boardLocal ya que no necesito que los board viajen,
			//aunque quizas en el momento de crear el board si que tiene que ir en la response, en ese caso,
			//tengo que crear 2 tableros, uno para cada usuario, y luego 2 tableros vacios, esos vacíos, donde los creo
			//Preguntar tambien, el error en el register cuando no meto ninguna pwd, ni la 1 ni la 2
			// como usar el validateAccount del userController
			// Como usar la date de un Token
			var tablero = document.getElementById("idTabla");
			var celdas = tablero.getElementsByTagName("td");
			var i;
			if(x==0){
				i=0;
			}else if(x==1){
				i=3;
			}else{
				i=6;
			}
			i =+y;
			celdas[i].textContent = 1 //en caso de jugador 1 o poner un 0 en caso de jugador 2
		}

		conectarAWebSocket() {
			let ws = new WebSocket("ws://localhost:8080/wsGenerico");
			ws.onopen = function(event) {
				alert("Conexión establecida");
			}
			ws.onmessage = function(event) {
				let msg = JSON.parse(event.data);
			}
		}

		joinGame(game) {
			let self = this;

			let data = {
				type : "get",
				url : "/games/joinGame/" + game.name,
				success : function(response) {
					let match
					if (game.name=="Tres en raya")
						match = new TERMatch("Tres en raya", response)
					else
						match = new BarcosMatch("Hundir la flota", response)
					self.matches.push(match);
					self.conectarAWebSocket();
					//self.createMap(game);
					console.log(JSON.stringify(response));
				},
				error : function(response) {
					console.error(response.responseJSON.message);
					self.error(response.responseJSON.message);
				}
			};
			
			$.ajax(data);
		}
		
		createMap(game){
			var create = function(){
				var name = game.name;
				var tablero = document.getElementById("idTabla");
				if(name == "Tres en raya"){
					for(var i = 0; i<3;i++){
						var fila = document.createElement("tr");
						for(var j = 0; j<3; j++){
							var celda = document.createElement("td");
							var textoCelda = document.createTextNode("-");
							celda.style.background = "black";
							celda.appendChild(textoCelda);
							fila.appendChild(celda);
							
						}
						tablero.appendChild(fila);
					}
				}else if(name == "Ajedrez"){
					for(var i = 0; i<8;i++){
						var fila = document.createElement("tr");						
						for(var j = 0; j<8; j++){
							var celda = document.createElement("td");
							var textoCelda = document.createTextNode("F");
							celda.appendChild(textoCelda);
							celda.setAttribute("border","1px solid black");
							fila.appendChild(celda);
						}
						tablero.appendChild(fila);
					}
				}else{
					for(var i = 0; i<10;i++){
						var fila = document.createElement("tr");
						for(var j = 0; j<10; j++){
							var celda = document.createElement("td");
							var textoCelda = document.createTextNode("-");
							celda.appendChild(textoCelda);
							fila.appendChild(celda);
						}
						tablero.appendChild(fila);
					}
					//colocarPiezas()
				}
			}
			create();
		}


		reload(match) {
			let self = this;

			let data = {
				type : "get",
				url : "/games/findMatch/" + match.id,
				success : function(response) {
					for (let i=0; i<self.matches().length; i++)
						if (self.matches()[i].id==match.id) {
							self.matches.splice(i, 1, response);
							break;
						}
					console.log(JSON.stringify(response));
				},
				error : function(response) {
					console.error(response.responseJSON.message);
					self.error(response.responseJSON.message);
				}
			};
			$.ajax(data);
		}

		disconnected() {
			// Implement if needed
		};

		transitionCompleted() {
			// Implement if needed
		};
	}

	return MenuViewModel;
});
