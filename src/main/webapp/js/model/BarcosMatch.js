class BarcosMatch extends Match  {
	constructor(nombre, response) {
		super(nombre, response)
		this.nombre = nombre
		this.boardOponente = response.boardOponente	
	}
	
	
	
}