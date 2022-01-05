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
	private Board board;
	
	@Transient
	private Board boardOponente;
	
	@Transient
	protected Vector<User> players;
	
	@Transient
	protected User playerWithTurn; 
	
	@Transient
	protected boolean ready;
	
	public Match() {
		this.id = UUID.randomUUID().toString();
		this.players = new Vector<>();
		this.board = newBoard();
		this.boardOponente = newOponentBoard();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Board getBoard() {
		return board;
	}
	
	public Board getOponentBoard() {
		return boardOponente;
	}
	public void setBoard(Board board) {
		this.board = board;
	}
	
	public void setOponentBoard(Board board) {
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
	
	@Transient
	public Vector<User> getPlayers() {
		return players;
	}

	protected abstract void checkReady();

	protected abstract Board newBoard();
	
	protected abstract Board newOponentBoard();

	public abstract void move(String userId, JSONObject jso) throws Exception;

	public abstract void notifyNewState(String userId);

}
