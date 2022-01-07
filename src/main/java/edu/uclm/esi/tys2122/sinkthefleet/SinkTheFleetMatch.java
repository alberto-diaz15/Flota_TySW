package edu.uclm.esi.tys2122.sinkthefleet;

import java.io.IOException;
import java.security.SecureRandom;

import org.json.JSONObject;

import edu.uclm.esi.tys2122.model.Board;
import edu.uclm.esi.tys2122.model.Match;
import edu.uclm.esi.tys2122.model.User;

public class SinkTheFleetMatch extends Match {

	public SinkTheFleetMatch() {
		super("Hundir la flota");
	}
	private User winner, looser;
	private boolean draw;
	
	@Override
	protected Board newBoard() {
		return new SinkTheFleetBoard();
	}
	
	@Override
	protected Board newBoardOponente() {
		return new SinkTheFleetBoard();
	}
	
	@Override
	protected void checkReady() {
		this.ready = this.players.size()==2;
		if (this.ready)
			this.playerWithTurn = new SecureRandom().nextBoolean() ? this.players.get(0) : this.players.get(1);
	}
	
	public int getSquare(Integer x, Integer y) {
		SinkTheFleetBoard board = (SinkTheFleetBoard) this.getBoard();
		return board.getSquares()[x][y];
	}
	
	public void setSquare(Integer x, Integer y, int value) {
		SinkTheFleetBoard board = (SinkTheFleetBoard) this.getBoard();
		board.getSquares()[x][y]=value;
	}
	
	@Override
	public void move(String userId, JSONObject jsoMovimiento) throws Exception {
		if (this.filled())
			throw new Exception("La partida ya terminó");
		
		if (!this.getPlayerWithTurn().getId().equals(userId))
			throw new Exception("No es tu turno");
		
		Integer x = jsoMovimiento.getInt("x");
		Integer y = jsoMovimiento.getInt("y");
		
		if (this.getSquare(x, y)!=0)
			throw new Exception("Casilla ocupada");
		
		//int value = this.getPlayerWithTurn()==this.getPlayers().get(0) ? 1 : 2;
		int value = 1;
		this.setSquare(x, y, value);
		notifyMove(x,y);
		checkWinner();
		
		if (this.filled() && this.winner==null)
			this.draw = true;
		else {
			this.playerWithTurn = this.getPlayerWithTurn()==this.getPlayers().get(0) ?
				this.getPlayers().get(1) : this.getPlayers().get(0);
		}
	}
	
	private boolean filled() {
		SinkTheFleetBoard board = (SinkTheFleetBoard) this.getBoard();
		int[][] squares = board.getSquares();
		for (int i=0; i<3; i++)
			for (int j=0; j<3; j++)
				if (squares[i][j]==0)
					return false;
		return true;
	}
	private void checkHit(int x, int y) {
		SinkTheFleetBoard board = (SinkTheFleetBoard) this.getBoard();
		int[][] squares = board.getSquares();
	}
	private void checkWinner() {
		SinkTheFleetBoard board = (SinkTheFleetBoard) this.getBoard();
		int[][] squares = board.getSquares();
		
		if (squares[0][0]!=0 && squares[0][0]==squares[0][1] && squares[0][1]==squares[0][2] ||
				squares[1][0]!=0 && squares[1][0]==squares[1][1] && squares[1][1]==squares[1][2] ||
				squares[2][0]!=0 && squares[2][0]==squares[2][1] && squares[2][1]==squares[2][2]) {
			this.winner = this.getPlayerWithTurn();
		} else if (squares[0][0]!=0 && squares[0][0]==squares[1][0] && squares[1][0]==squares[2][0] ||
				squares[0][1]!=0 && squares[0][1]==squares[1][1] && squares[1][1]==squares[2][1] ||
				squares[0][2]!=0 && squares[0][2]==squares[1][2] && squares[2][1]==squares[2][2]) {
			this.winner = this.getPlayerWithTurn();
		} else if (squares[0][0]!=0 && squares[0][0]==squares[1][1] && squares[1][1]==squares[2][2] ||
				squares[0][2]!=0 && squares[0][2]==squares[1][1] && squares[1][1]==squares[2][0]) {
			this.winner = this.getPlayerWithTurn();
		}
		if (this.winner!=null) {
			this.looser = this.winner==this.players.get(0) ? this.players.get(1) : this.players.get(0);
		}
	}
	
	public User getWinner() {
		return winner;
	}
	
	public User getLooser() {
		return looser;
	}
	
	public boolean isDraw() {
		return draw;
	}
	
	public void notifyMove(int x, int y) {
		JSONObject jso = new JSONObject();
		// jso.put("board", this.board.toJSON());
		jso.put("row", x);
		jso.put("col", y);
		for (User player : this.players) {
			User user = getPlayerWithTurn();
			if (!player.getId().equals(user.getId()))
				try {
					player.sendMessage(jso);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}		
	}
	
	public void notifyNewState(String userId) {
		JSONObject jso = new JSONObject();
		jso.put("type", "BOARD");
		// jso.put("board", this.board.toJSON());
		
		for (User player : this.players) {
			if (!player.getId().equals(userId))
				try {
					player.sendMessage(jso);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}		
	}

	public void colocarPiezas() {
		
		for(int barcos = 0; barcos <8; barcos++){
			boolean added = false;
			int fila = getRandomInt(0,5);
			int columna =getRandomInt(0,5);
			if(this.getBoard().squares[fila][columna] == 0){
				this.getBoard().squares[fila][columna] = 1;
				added = true;
			}else{
				while(!added){
					fila = getRandomInt(0,5);
					columna =getRandomInt(0,5);
					if(this.getBoard().squares[fila][columna] == 0){
						this.getBoard().squares[fila][columna] = 1;
						added= true;
					}
				}
			}
		}
		//this.getBoard().setSquares(this.getBoard().squares);
	}
	public int getRandomInt(int min, int max) {
		return (int) (Math.floor(Math.random() * (max - min)) + min);
	}
}
