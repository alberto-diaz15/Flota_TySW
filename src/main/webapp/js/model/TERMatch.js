class TERMatch extends Match {
	constructor(nombre, response) {
		super(nombre, response)
		this.nombre = nombre
		this.boardLocal = response.board

	}
	
}