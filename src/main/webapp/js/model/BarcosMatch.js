class BarcosMatch extends Match  {
	constructor(nombre, response) {
		super(nombre, response)
		this.boardLocal = response.board
		this.boardOponente = response.boardOponente
	}
	
}