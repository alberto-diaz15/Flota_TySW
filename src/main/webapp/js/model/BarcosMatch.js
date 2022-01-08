class BarcosMatch extends Match  {
	constructor(nombre, response) {
		super(nombre, response)
		this.nombre = nombre
		if(response.players[0].id == sessionStorage.getItem("User") && this.board.player.id == sessionStorage.getItem("User")){
			this.board = response.board
			this.boardOponente = response.boardOponente
		}else{
			this.board = response.boardOponente
			this.boardOponente = response.board
		}	
	}
	
	checkBoard(response){
		if(this.nombre == "Hundir la flota"){
			if(response.players[0].id == sessionStorage.getItem("User") && this.board.player.id == sessionStorage.getItem("User")){
				this.board = response.board
				this.boardOponente = response.boardOponente
			}else{
				this.board = response.boardOponente
				this.boardOponente = response.board
			}			
		}	
	}

}