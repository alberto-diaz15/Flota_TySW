class ChatRoom {
	constructor(ko, viewModel, usuario) {
		this.viewModel = viewModel

		this.videoOn = ko.observable(false)
		this.conexionOn = ko.observable(false)
		this.llamadaEnCurso = ko.observable(false)
		
		this.mensajes = ko.observableArray([])
		
		this.usuario = usuario
		this.constraints = {
			video : true,
			audio : false
		}
		let dado = Math.random()
		this.idLocal = dado + "a"
		this.idRemoto = dado + "b"
	}
	
	encenderVideo() {
		let self = this;
		navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

		navigator.getUserMedia(
			self.constraints, 
			(stream) => {
				let widget = document.getElementById(self.idLocal)
				widget.srcObject = stream;
				self.localStream = stream;
				self.videoOn(true)
				self.mensajes.push("Vídeo local encendido")
			}, 
			(error) => {
				alert(error)
			}
		)			
	}
		
	crearConexion() {
		let servers = { 
			iceServers : [ 
				//{ "url" : "stun:stun.l.google.com:19302" },
				{ 
					urls : "turn:localhost",
					username : "webrtc",
					credential : "turnserver"
				}
			]
		};
		this.conexion = new RTCPeerConnection(servers);
		this.conexionOn(true)
		
		let localTracks = this.localStream.getTracks();
		localTracks.forEach(track =>
			{
				this.conexion.addTrack(track, this.localStream);
			}
		);
		this._addEventHandlers()
		this.mensajes.push("Conexión creada")
	}
	
	crearSala() {
		let msg = {
			type : "CREAR SALA",
			destinatario : this.usuario
		}
		this.viewModel.signaling.ws.send(JSON.stringify(msg))		
	}
	
	llamar() {
		let self = this;
		let sdpConstraints = {}
		this.conexion.createOffer(
			(localSessionDescription) => {
				self.llamadaEnCurso(true)
				self.conexion.setLocalDescription(localSessionDescription)
				let msg = {
					type : "LLAMAR",
					roomId : self.id,
					destinatario : self.usuario,
					sessionDescription : localSessionDescription
				}
				self.viewModel.signaling.ws.send(JSON.stringify(msg))
				self.mensajes.push("Enviado LLAMAR")
			},
			(error) => {
				self.llamadaEnCurso(false)
				alert(error)
			},
			sdpConstraints
		)
	}
	
	atenderLlamada(msg) {
		let aceptar = confirm("Te llama " + msg.remitente)
		if (aceptar) {
			this._aceptarLlamada(msg.sessionDescription)
		} else {
			this._rechazarLlamada()
		}
	}
	
	_aceptarLlamada(remoteSessionDescription) {
		let self = this
		if (!this.videoOn()) 
			this.encenderVideo()
		
		if (!this.conexion)
			this.crearConexion()	

		let rtcRemoteSessionDescription = new RTCSessionDescription(remoteSessionDescription)
		self.conexion.setRemoteDescription(rtcRemoteSessionDescription)
		this.mensajes.push("Asignada la remoteSessionDescription a la conexión")

		let sdpConstraints = {}
		this.conexion.createAnswer(
			( (localSessionDescription) => {
				self.llamadaEnCurso(true)
				self.conexion.setLocalDescription(localSessionDescription).then( () => {
					let msg = {
						type : "ACEPTAR LLAMADA",
						roomId : self.id,
						sessionDescription : localSessionDescription
					}
					self.viewModel.signaling.ws.send(JSON.stringify(msg))
					self.mensajes.push("Enviado ACEPTAR LLAMADA")
				})
			}),
			( (error) => {
				self.llamadaEnCurso(true)
				alert(error)
			}),
			sdpConstraints
		)
	}
	
	_rechazarLlamada() {
		let msg = {
			type : "RECHAZAR LLAMADA",
			roomId : this.id
		}
		this.viewModel.signaling.ws.send(JSON.stringify(msg))
		this.mensajes.push("Enviado RECHAZAR LLAMADA")
	}
	
	iniciarConversacion(remoteSessionDescription) {
		let rtcRemoteSessionDescription = new RTCSessionDescription(remoteSessionDescription);
		this.conexion.setRemoteDescription(rtcRemoteSessionDescription);
		this.mensajes.push("Asignada la remoteSessionDescription a la conexión")
	}
	
	llamadaRechazada() {
		alert(this.usuario + " rechazó tu llamada")
		this.llamadaEnCurso(false)
		this.mensajes.push("Te han rechazado la llamada")
	}
	
	addIceCandidate(candidate) {
		try {
			this.conexion.addIceCandidate(candidate)
			this.mensajes.push("Añadido un candidate desde signaling")
		} catch(error) {
			console.error("Error añadiendo candidate")
		}
	}
	
	_addEventHandlers() {
		let self = this;
		this.conexion.onicecandidate = function(event) {
			if (event.candidate) {
				self.mensajes.push("Recibido candidate: " + event.candidate.candidate)
				let msg = {
					type : "CANDIDATE",
					candidate : event.candidate,
					roomId : self.id		
				};
				self.viewModel.signaling.ws.send(JSON.stringify(msg))
				self.mensajes.push("Enviado candidate")
			} else {
				alert("Todos los candidates")
				self.mensajes.push("Recibidos todos los candidates")
			}
		}
		
		this.conexion.oniceconnectionstatechange = function(event) {
			self.mensajes.push("oniceconnectionstatechange")
		}
			
		this.conexion.onicegatheringstatechange = function(event) {
			self.mensajes.push("onicegatheringstatechange")
		}
		
		this.conexion.onsignalingstatechange = function(event) {
			self.mensajes.push("onsignalingstatechange")
		}
	
		this.conexion.onnegotiationneeded = function(event) {
			self.mensajes.push("onnegotiationneeded")
		}
			
		this.conexion.ontrack = function(event) {
			let widget = document.getElementById(self.idRemoto)
			widget.srcObject = event.streams[0];
			self.mensajes.push("ontrack (de vídeo remoto)")
		}
		
		this.conexion.onremovetrack = function(event) {
			self.mensajes.push("onremovetrack")
		}		
	}
	
	clear() {
		this.mensajes([])
	}
}