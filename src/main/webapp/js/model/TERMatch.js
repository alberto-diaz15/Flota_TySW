class TERMatch extends Match {
	constructor(nombre, response) {
		super(nombre, response)
		this.boardLocal = response.board

	}
	
}