package edu.uclm.esi.tys2122.sinkthefleet;

import edu.uclm.esi.tys2122.model.Board;


public class SinkTheFleetBoard extends Board {
	private int[][] squares;
	
	public SinkTheFleetBoard() {
		this.squares = new int[6][6];
		for (int i=0; i<6; i++)
			for (int j=0; j<6; j++)
				this.squares[i][j] = 0;
		this.squares[3][3]=1;
	}
	
	public int[][] getSquares() {
		return squares;
	}
}
