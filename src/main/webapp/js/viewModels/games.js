define([ 'knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils',
		'jquery' ], function(ko, app, moduleUtils, accUtils, $) {

	class MenuViewModel {
		constructor() {
			let self = this;
			
			self.userName = "";
			self.games = ko.observableArray([]);
			self.matches = ko.observableArray([]);

			self.msg = ko.observable();

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
			let self = this;
			accUtils.announce('Juegos.');
			document.title = "Juegos";
			if(sessionStorage.getItem("userName") != null){
				self.userName = sessionStorage.getItem("userName");
			}else{
				self.userName = "No has iniciado sesion"
			}
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
					self.reload(match)
					self.error("")
				},
				error : function(response) {
					console.error(response);
					self.error(response.responseJSON.message);
				}
			}
			$.ajax(data);
		}
		
			//Preguntar si en barcosMatch puedo quitar el boardLocal ya que no necesito que los board viajen,
			//aunque quizas en el momento de crear el board si que tiene que ir en la response, en ese caso,
			//tengo que crear 2 tableros, uno para cada usuario, y luego 2 tableros vacios, esos vacíos, donde los creo
			//Preguntar tambien, el error en el register cuando no meto ninguna pwd, ni la 1 ni la 2
			// como usar el validateAccount del userController
			// Como usar la date de un Token

		conectarAWebSocket() {
			let self = this
			let ws = new WebSocket("ws://localhost:8080/wsGenerico");
			ws.onopen = function(event) {
			}
			ws.onmessage = function(event) {
				let msg = JSON.parse(event.data);
				if(msg.type == "move"){
					if(msg.hit == true){
						alert("Barco golpeado!");
					}
					self.reload(msg.match)
				}else if(msg.type == "msg"){
					alert("Me llega el mensaje del chat");
					var message = document.createElement("span")
					message.textContent = msg.msg
					var chat = document.getElementById("chatShow")
					chat.appendChild(message);
					chat.appendChild(document.createElement("br"))
				}else if( msg.type == "disconnected"){
					var message = document.createElement("span")
					message.textContent = msg.msg
					var chat = document.getElementById("chatShow")
					chat.appendChild(message);
					chat.appendChild(document.createElement("br"))
				}else if( msg.type == "connected"){
					self.reload(msg.match)

				}

			}
		}

		sendMsg(match){
			let self = this
			
			let info = {
				matchId : match.id,
				msg : this.msg()
			};

			let data = {
				type : "post",
				url : "/games/sendMsg",
				data : JSON.stringify(info),
				contentType : "application/json",
				success : function(response) {
					
					var message = document.createElement("span")
					var nombre = ""
					for (var i=0; i<response.players.length; i++){
						if(response.players[i].id==sessionStorage.User){
							nombre = response.players[i].name + ": " + info.msg
							break;	
						}
					}
					message.textContent = nombre
					var chat = document.getElementById("chatShow")
					chat.appendChild(message);
					chat.appendChild(document.createElement("br"))
					console.log(JSON.stringify(response));
					self.error("")
				},
				error : function(response) {
					console.error(response);
					self.error(response.responseJSON.message);
				}
			}
			$.ajax(data);
		}

		joinGame(game) {
			let self = this;
			
			let data = {
				type : "get",
				url : "/games/joinGame/" + game.name,
				success : function(response) {
					let match
					
					if(response.players.length == 1){
						self.user= response.owner.id
						self.userName = response.owner.name
						sessionStorage.setItem("User", self.user)
						sessionStorage.setItem("userName", self.user)
					}else{
						for(var i = 0; i<response.players.length;i++ ){
							if(response.players[i] != response.owner.id){
								self.user = response.players[i].id
								sessionStorage.setItem("User", self.user)
							}
						}
					}
					if (game.name=="Tres en raya")
						match = new TERMatch("Tres en raya", response)
					else
						match = new BarcosMatch("Hundir la flota", response)
					self.matches.push(match);
					self.conectarAWebSocket();
					self.sendConnected(match);
					self.error("")
					console.log(self.user);
					console.log(JSON.stringify(response));
				},
				error : function(response) {
					console.error(response.responseJSON.message);
					self.error(response.responseJSON.message);
				}
			};
			$.ajax(data);
		}
		
		sendConnected(match){
			let self = this
			
			let info = {
				matchId : match.id
			};

			let data = {
				type : "post",
				url : "/games/sendConnected",
				data : JSON.stringify(info),
				contentType : "application/json",
				success : function(response) {
					console.log(JSON.stringify(response));
					self.error("")
				},
				error : function(response) {
					console.error(response);
					self.error(response.responseJSON.message);
				}
			}
			$.ajax(data);
		}
		
		logout(){
			let self = this
			
			let info = {
				userName: self.userName
			};

			let data = {
				type : "post",
				url : "/games/logout",
				data : JSON.stringify(info),
				contentType : "application/json",
				success : function(response) {
					console.log(JSON.stringify(response));
					//depende de lo que devuelva, hace el app.router o no
					app.router.go( { path : "login"} );
					self.error("")
				},
				error : function(response) {
					console.error(response);
					self.error(response.responseJSON.message);
				}
			}
			$.ajax(data);
		}

		reload(match) {
			let self = this;

			let data = {
				type : "get",
				url : "/games/findMatch/" + match.id,
				success : function(response) {
					for (let i=0; i<self.matches().length; i++)
						if (self.matches()[i].id==match.id) {
							response.id = match.id
							if(response.nombre == "Hundir la flota"){
								match = new BarcosMatch("Hundir la flota",response);
							}else{
								match = new TERMatch("Tres en raya", response);
							}
							match.checkBoard(response);
							self.matches.splice(i, 1, match);
							break;
						}
					console.log(JSON.stringify(response));
					self.error("")
				},
				error : function(response) {
					console.error(response.responseJSON.message);
					self.error(response.responseJSON.message);
				}
			};
			$.ajax(data);
		}

		disconnected() {
			let self = this
		    if(self.matches._latestValue.length =! 0){
				for(var i = 0 ; i<self.matches._latestValue.length;i++){
					var match = self.matches._latestValue[i]
					if(match != undefined){
										let info = {
						matchId : match.id, 
						msg : "Se ha desconectado, Has ganado"
					};
		
					let data = {
						type : "post",
						url : "/games/disconnected",
						data : JSON.stringify(info),
						contentType : "application/json",
						success : function(response) {
							console.log(JSON.stringify(response));
							self.error("")
						},
						error : function(response) {
							console.error(response);
							self.error(response.responseJSON.message);
						}
					}
					$.ajax(data);	
					}
				}					
			}	

		};

		transitionCompleted() {
			// Implement if needed
		};
	}

	return MenuViewModel;
});
