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
					self.error(response);
				}
			}
			$.ajax(data);
		}
		
		actualizarTablero(x,y){
			//Preguntar  si es mejor en vez de actualizar la tabla con html, usar el board
			//Preguntar como usar los squares para combinarlos con el tablero
			// Tambien averiguar qué usuario tiene el turno para poner una X o una O en las Tres en raya
			// Preguntar tambien si creo el tablero de forma optima
			//duda para añadir mas .js para vincular unos a otros sin ponerlo explicitamente en el html
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
			let ws = new WebSocket("ws://localhost/wsGenerico");
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
						match = new TERMatch("TER", response)
					else
						match = new BarcosMatch("Barcos", response)
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

		colocarPiezas() {
			//INCOMPLETO 
	        let color = "brown";
	        for (let i=0; i<this.board.squares.length; i++) {
	            for (let j=0; j<this.board.squares.length; j++) {
	                let square = this.board.squares[i][j];
	
	                this.board.squares[i][j] = {
	                    color : color,
	                    valor : square,
	                    imagen : null
	                };
	
	                color = color=="white" ? "brown" : "white";
	                switch (this.board.squares[i][j].valor) {
	                    case 1 : 
	                        this.board.squares[i][j].imagen = "css/images/tb.png";
	                        break;
	                    case 2 : 
	                        this.board.squares[i][j].imagen = "css/images/cb.png";
	                        break;
	                    case -1 : 
	                        this.board.squares[i][j].imagen = "css/images/tn.png";
	                        break;
	                    case -2 : 
	                        this.board.squares[i][j].imagen = "css/images/cn.png";
	                    default:
	                        this.board.squares[i][j].imagen = null;
	                }
	            }
	        }
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
