package edu.uclm.esi.tys2122.newchess;

import java.io.IOException;
import java.util.Optional;

import javax.persistence.Entity;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uclm.esi.tys2122.dao.UserRepository;
import edu.uclm.esi.tys2122.model.Board;
import edu.uclm.esi.tys2122.model.Match;
import edu.uclm.esi.tys2122.model.User;

@Entity
public class ChessMatch extends Match {
	//private User winner, looser;
	//private boolean draw;
	/*@Autowired
	private UserRepository userRepo;*/
	
	public ChessMatch(String gameName) {
		super(gameName);
	}

	@Override
	protected Board newBoard() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected Board newBoardOponente() {
		return null;
	}

	@Override
	protected void checkReady() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public User move(String userId, JSONObject jso) {
		// TODO Auto-generated method stub
		return null;
		
	}

	@Override
	public void notifyNewState(User user, Match match) {
		JSONObject jso = new JSONObject();
		jso.put("type", "connected");	
		jso.put("match", match);
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
		return user;
		/*JSONObject jso = new JSONObject();
		jso.put("type", "disconnected");
		jso.put("msg", user.getName()+": "+msg);
		
		for (User player : this.players) {
			if (!player.getId().equals(user.getId()))
				try {
					player.sendMessage(jso);
					winner = player;
					looser = user;
					addGameWon(player);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}*/		
	}
	
	public void addGameWon(User user) {
		/*Optional<User> winnerAux = userRepo.findById(user.getId());
		if (!winnerAux.isEmpty()) {
			winnerAux.get().addGamesWon();
			userRepo.save(winnerAux.get());
		}*/
	}
	
	public JSONObject toJSON() {
		return null;
	}
}
