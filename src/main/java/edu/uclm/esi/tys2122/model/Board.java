package edu.uclm.esi.tys2122.model;

public abstract class Board {

	
	public int[][] squares;

	public int[][] getSquares() {
		return squares;
	}
	
	public void setSquares(int[][] squares) {
		this.squares = squares;
	}

	public abstract Object getPlayer();

	public abstract void setPlayer(User user);
}
