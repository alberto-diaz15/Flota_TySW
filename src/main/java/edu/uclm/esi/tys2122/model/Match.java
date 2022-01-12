package edu.uclm.esi.tys2122.model;

import java.util.UUID;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.json.JSONObject;

@Entity
@Table(name = "partida")
public abstract class Match {
	@Id
	@Column(length = 36)
	private String id;
	
	@Transient
	private String nombre;
	
	@Transient
	private User owner;
	
	@Transient
	private Board board;
	
	@Transient
	private Board boardOponente;
	
	@Transient
	protected Vector<User> players;
	
	@Transient
	protected User playerWithTurn; 
	
	@Transient
	protected boolean ready;
	
	public Match(String gameName) {
		this.id = UUID.randomUUID().toString();
		this.players = new Vector<>();
		this.board = getBoard();
		this.boardOponente = getBoardOponente();
		if(gameName.equals("Hundir la flota")) {
			if(players.isEmpty()) {
				board.setSquares(colocarPiezas(board.getSquares()));
				boardOponente.setSquares(colocarPiezas(boardOponente.getSquares()));
			}
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public String getNombre() {
		return nombre;
	}

	public Board getBoard() {
		if(board == null) {
			board = newBoard();
		}		
		return board;
	}
	
	public Board getBoardOponente() {
		if (boardOponente == null) {
			boardOponente = newBoardOponente();
		}
		return boardOponente;
	}
	public void setBoard(Board board) {
		this.board = board;
	}
	
	public void setBoardOponente(Board board) {
		this.boardOponente = board;
	}

	// TODO : no se puede añadir dos veces el mismo jugador
	public void addPlayer(User user) {
		this.players.add(user);
		checkReady();
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public User getPlayerWithTurn() {
		return playerWithTurn;
	}
	

	public  User getOwner() {
		return owner;
	}

	public  void setOwner(User user) {
		this.owner = user;

	}

	
	@Transient
	public Vector<User> getPlayers() {
		return players;
	}

	protected abstract void checkReady();

	protected abstract Board newBoard();
	
	protected abstract Board newBoardOponente();

	public abstract User move(String userId, JSONObject jso) throws Exception;

	public abstract void notifyNewState(User user, Match match);

	public abstract int[][] colocarPiezas(int[][] squares);

	public abstract void notifyNewMessage(User userOrigen, String msg);

	public abstract User notifyDisconnected(User user, String string);
	
	public abstract JSONObject toJSON();
}
