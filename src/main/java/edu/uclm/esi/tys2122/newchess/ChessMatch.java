package edu.uclm.esi.tys2122.newchess;

import java.io.IOException;

import javax.persistence.Entity;

import org.json.JSONObject;

import edu.uclm.esi.tys2122.model.Board;
import edu.uclm.esi.tys2122.model.Match;
import edu.uclm.esi.tys2122.model.User;

@Entity
public class ChessMatch extends Match {
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
	public void move(String userId, JSONObject jso) {
		// TODO Auto-generated method stub
		
	}

	@Override
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

	@Override
	public void colocarPiezas() {
		// TODO Auto-generated method stub
		
	}
}
