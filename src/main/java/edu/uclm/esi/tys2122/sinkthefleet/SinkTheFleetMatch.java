package edu.uclm.esi.tys2122.sinkthefleet;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.InputMismatchException;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uclm.esi.tys2122.dao.UserRepository;
import edu.uclm.esi.tys2122.model.Board;
import edu.uclm.esi.tys2122.model.Match;
import edu.uclm.esi.tys2122.model.User;
import net.bytebuddy.asm.Advice.This;
import net.bytebuddy.asm.Advice.This;

public class SinkTheFleetMatch extends Match {

	public SinkTheFleetMatch() {
		super("Hundir la flota");
	}
	

	
	private User winner = null, looser= null;
	private boolean draw;
	private int barcosOponente = 8;
	private int barcos = 8;
	
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
	
	public int getSquare(Integer x, Integer y, int player) {
		if(player == 0){
			SinkTheFleetBoard board = (SinkTheFleetBoard) this.getBoard();
			return board.getSquares()[x][y];
		}else {
			SinkTheFleetBoard board = (SinkTheFleetBoard) this.getBoardOponente();
			return board.getSquares()[x][y];
		}

	}
	
	public void setSquare(Integer x, Integer y, int value, int player) {
		if(player == 0){
			SinkTheFleetBoard board = (SinkTheFleetBoard) this.getBoard();
			board.getSquares()[x][y]=value;
		}else {
			SinkTheFleetBoard board = (SinkTheFleetBoard) this.getBoardOponente();
			board.getSquares()[x][y]=value;
		}
	}
	
	@Override
	public User move(String userId, JSONObject jsoMovimiento) throws Exception {
		if (this.winner!=null)
			throw new Exception("La partida ya terminó");
		
		if (!this.getPlayerWithTurn().getId().equals(userId))
			throw new Exception("No es tu turno");
		Integer x;
		Integer y;
		try {
			x = jsoMovimiento.getInt("x");
			y = jsoMovimiento.getInt("y");
			if((x <0 || x>5) || (y<0 || y>5)) {
				throw new Exception("Casilla no válida. Solo valores entre 0 y 5");
			}
		}catch(InputMismatchException e){
			throw new Exception("Casilla no válida. Solo valores entre 0 y 5");
		}

		int player = checkPlayer();
		if (this.getSquare(x, y,player)==3 || this.getSquare(x, y,player)==2)
			throw new Exception("Casilla ocupada");
		

		int value = 0;
		boolean hit = checkHit(x,y, player);
		if(hit) {
			if(player == 0) {
				barcos--;
				value = 2;
			}else {
				barcosOponente--;
				value = 2;
			}
		}else {
			value = 3;
		}
		this.setSquare(x, y, value, player);
		notifyMove(x,y,hit,this);
		checkWinner();
		
		
		if (this.winner==null) {
			this.playerWithTurn = this.getPlayerWithTurn()==this.getPlayers().get(0) ?
				this.getPlayers().get(1) : this.getPlayers().get(0);
		}
		return winner;
	}
	
	private int checkPlayer() {
		if (this.getPlayerWithTurn().getId() == this.getPlayers().get(0).getId()) {
			return 1; // si mueve el jugador 0, me fijo en el tablero del jugador 1
		}else {
			return 0;
		}
	}
	private boolean checkHit(int x, int y, int player) {
		SinkTheFleetBoard board;
		if(player == 0) {
			board = (SinkTheFleetBoard) this.getBoard();
		}else {
			board = (SinkTheFleetBoard) this.getBoardOponente();
		}
		int[][] squares = board.getSquares();
		if(squares[x][y] == 1) {
			return true;
		}else {
			return false;
		}
	}
	private void checkWinner() {
		
		if(barcos == 0) {
			this.winner = this.getPlayers().get(1);
		}
		if(barcosOponente == 0) {
			this.winner = this.getPlayers().get(0);
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
	
	public void notifyMove(int x, int y, boolean hit, SinkTheFleetMatch match) {
		JSONObject jso = new JSONObject();
		//jso.put("board", this.board.toJSON());
		jso.put("type", "move");
		jso.put("row", x);
		jso.put("col", y);
		jso.put("hit", hit);
		
		jso.put("match", match.toJSON());
		
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
	
	public void notifyNewState(User user, Match match) {
		JSONObject jso = new JSONObject();
		jso.put("type", "connected");	
		jso.put("match", match.toJSON());
		
		for (User player : this.players) {
			if (!player.getId().equals(user.getId()))
				try {
					player.sendMessage(jso);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}		
	}

	public int[][] colocarPiezas(int[][] squares) {
		
		for(int barcos = 0; barcos <8; barcos++){
			boolean added = false;
			int fila = getRandomInt(0,5);
			int columna =getRandomInt(0,5);
			if(squares[fila][columna] == 0){
				squares[fila][columna] = 1;
				added = true;
			}else{
				while(!added){
					fila = getRandomInt(0,5);
					columna =getRandomInt(0,5);
					if(squares[fila][columna] == 0){
						squares[fila][columna] = 1;
						added= true;
					}
				}
			}
		}
		return squares;
	}
	public int getRandomInt(int min, int max) {
		return (int) (Math.floor(Math.random() * (max - min)) + min);
	}

	public void notifyNewMessage(User user, String msg) {
		JSONObject jso = new JSONObject();
		jso.put("type", "msg");
		jso.put("msg", user.getName()+": "+msg);
		
		for (User player : this.players) {
			if (!player.getId().equals(user.getId()))
				try {
					player.sendMessage(jso);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}		
	}
	
	public User notifyDisconnected(User user, String msg) {
		JSONObject jso = new JSONObject();
		jso.put("type", "disconnected");
		jso.put("msg", user.getName()+": "+msg);
		for (User player : this.players) {
			if (!player.getId().equals(user.getId()))
				try {
					if(winner ==null) {
						player.sendMessage(jso);
						winner = player;
						looser = user;
						return winner;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return winner;		
	}
	
	public JSONObject toJSON() {
		JSONObject jso = new JSONObject();
		JSONArray players = new JSONArray();
		for(int i = 0; i <this.getPlayers().size();i++) {
			players.put(this.getPlayers().elementAt(i));
		}
		jso.put("id", this.getId());
		jso.put("board", this.getBoard());
		jso.put("boardOponente", this.getBoardOponente());
		jso.put("nombre", this.getNombre());
		jso.put("looser", looser);
		jso.put("winner", winner);
		jso.put("owner", this.getOwner());
		jso.put("players", players);
		jso.put("playerWithTurn", playerWithTurn);
		jso.put("ready", this.ready);
		jso.put("draw", draw);
		return jso;
	}
	

}
