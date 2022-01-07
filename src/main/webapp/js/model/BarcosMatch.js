class BarcosMatch extends Match  {
	constructor(nombre, response) {
		super(nombre, response)
		this.nombre = nombre
		if(response.players.length ==2){
			this.board = response.boardOponente
			this.boardOponente = response.board
		}else{
			this.board = response.board
			this.boardOponente = response.boardOponente
		}
	}
	
	
	
}