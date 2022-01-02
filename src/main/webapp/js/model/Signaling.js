class Signaling {
	constructor(viewModel) {
		this.viewModel = viewModel;
		
		this.ws = new WebSocket("wss://" + window.location.host + "/wsSignaling")
		this._asignarEventHandlers();
	}
	
	_asignarEventHandlers() {	
		let self = this;
		
		this.ws.onopen = function() {
			self.viewModel.conectado(true)
		}
		
		this.ws.onclose = function() {
			self.viewModel.conectado(false)
		}
					
		self.ws.onmessage = function(event) {
			let msg = JSON.parse(event.data)
			console.log(JSON.stringify(msg))
			
			if (msg.type=="ROOM ID") {
				self.viewModel.llamar(msg.roomId, msg.destinatario)
			} else if (msg.type=="LLEGADA DE USUARIO") {
				self.viewModel.llegadaDeUsuario(msg.userName);
			}  else if (msg.type=="MARCHA DE USUARIO") {
				self.viewModel.marchaDeUsuario(msg.userName);
			} else if (msg.type=="LISTA DE USUARIOS") {
				self.viewModel.setUsuarios(msg.usuarios);
			} else if (msg.type=="TE LLAMAN") {
				self.viewModel.atenderLlamada(msg)
			} else if (msg.type=="LLAMADA ACEPTADA") {
				self.viewModel.iniciarConversacion(msg)
			} else if (msg.type=="LLAMADA RECHAZADA") {
				self.viewModel.rechazarLlamada(msg)
			} else if (msg.type=="CANDIDATE" && msg.candidate && msg.candidate.ice) {
				try {
					self.viewModel.addIceCandidate(msg)
					console.log("iceCandidate bien añadido")
				} catch(error) {
					console.log("Error en addIceCandidate: " + error)
				}
			}
		}
	}
}