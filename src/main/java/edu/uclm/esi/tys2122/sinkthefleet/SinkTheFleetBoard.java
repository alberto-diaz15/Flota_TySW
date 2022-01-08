package edu.uclm.esi.tys2122.sinkthefleet;

import edu.uclm.esi.tys2122.model.Board;
import edu.uclm.esi.tys2122.model.User;


public class SinkTheFleetBoard extends Board {
	private int[][] squares;
	private User owner;
	public SinkTheFleetBoard() {
		this.squares = new int[6][6];
		for (int i=0; i<6; i++)
			for (int j=0; j<6; j++)
				this.squares[i][j] = 0;
				this.setSquares(squares);
	}
	public User getPlayer() {
		return owner;
	}

	@Override
	public void setPlayer(User user) {
		this.owner=user;
	}

}
