class BarcosMatch extends Match  {
	constructor(nombre, response) {
		super(nombre, response)
		this.boardLocal = response.board
		this.boardOponente = response.oponentBoard
		this.boardLocal = colocarPiezas(this.boardLocal)
		
		function colocarPiezas(boardLocal) {
			var getRandomInt = function getRandomInt(min, max) {
			  return Math.floor(Math.random() * (max - min)) + min;
			}
			let barcos = 8;
			while(barcos >0){
				barcos--;
				var fila = getRandomInt(0,5);
				var columna =getRandomInt(0,5);
				if(boardLocal.squares[fila][columna] == 0){
					boardLocal.squares[fila][columna] =1;
				}
			}
			return boardLocal
		}		
	}
	
	
	
}