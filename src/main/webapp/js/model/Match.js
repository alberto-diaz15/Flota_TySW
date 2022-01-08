class Match {
	constructor(nombre, response) {
		this.nombre = nombre
		this.response = response
		this.id = response.id;
		this.ready = response.ready;
		this.players = response.players;
		this.playerWithTurn = response.playerWithTurn;
		this.winner = response.winner;
		this.looser = response.looser;
		this.draw = response.draw;
		this.board = response.board;
		this.game = response.game;
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