package edu.uclm.esi.tys2122.sinkthefleet;

import edu.uclm.esi.tys2122.model.Board;


public class SinkTheFleetBoard extends Board {
	private int[][] squares;
	
	public SinkTheFleetBoard() {
		this.squares = new int[10][10];
		for (int i=0; i<10; i++)
			for (int j=0; j<10; j++)
				this.squares[i][j] = 0;
		this.squares[5][5]=1;
	}
	
	public int[][] getSquares() {
		return squares;
	}
}
