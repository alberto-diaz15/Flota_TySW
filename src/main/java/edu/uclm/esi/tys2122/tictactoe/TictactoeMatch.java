package edu.uclm.esi.tys2122.tictactoe;

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

public class TictactoeMatch extends Match {
	
	public TictactoeMatch() {
		super("Tres en raya");
	}

	private User winner, looser;
	private boolean draw;
	
	@Autowired
	private UserRepository userRepo;
	
	@Override
	protected Board newBoard() {
		return new TictactoeBoard();
	}
	
	@Override
	protected Board newBoardOponente() {
		return null;
	}

	@Override
	protected void checkReady() {
		this.ready = this.players.size()==2;
		if (this.ready)
			this.playerWithTurn = new SecureRandom().nextBoolean() ? this.players.get(0) : this.players.get(1);
	}

	public int getSquare(Integer x, Integer y) {
		TictactoeBoard board = (TictactoeBoard) this.getBoard();
		return board.getSquares()[x][y];
	}

	public void setSquare(Integer x, Integer y, int value) {
		TictactoeBoard board = (TictactoeBoard) this.getBoard();
		board.getSquares()[x][y]=value;
	}

	@Override
	public User move(String userId, JSONObject jsoMovimiento) throws Exception {
		if (this.filled() || winner != null)
			throw new Exception("La partida ya terminó");
		
		if (!this.getPlayerWithTurn().getId().equals(userId))
			throw new Exception("No es tu turno");
		Integer x;
		Integer y;
		try {
			x = jsoMovimiento.getInt("x");
			y = jsoMovimiento.getInt("y");
			if((x <0 || x>2) || (y<0 || y>2)) {
				throw new Exception("Casilla no válida. Solo valores entre 0 y 2");
			}
		}catch(InputMismatchException e){
			throw new Exception("Casilla no válida. Solo valores entre 0 y 2");
		}
		if (this.getSquare(x, y)!=0)
			throw new Exception("Casilla ocupada");
		
		int value = this.getPlayerWithTurn()==this.getPlayers().get(0) ? 1 : 2;
		this.setSquare(x, y, value);
		
		checkWinner();
		
		if (this.filled() && this.winner==null)
			this.draw = true;
		else {
			this.playerWithTurn = this.getPlayerWithTurn()==this.getPlayers().get(0) ?
				this.getPlayers().get(1) : this.getPlayers().get(0);
		}
		return winner;
	}

	private boolean filled() {
		TictactoeBoard board = (TictactoeBoard) this.getBoard();
		int[][] squares = board.getSquares();
		for (int i=0; i<3; i++)
			for (int j=0; j<3; j++)
				if (squares[i][j]==0)
					return false;
		return true;
	}

	private void checkWinner() {
		TictactoeBoard board = (TictactoeBoard) this.getBoard();
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

	@Override
	public int[][] colocarPiezas(int[][] squares) {
		return squares;
		// TODO Auto-generated method stub
		
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
