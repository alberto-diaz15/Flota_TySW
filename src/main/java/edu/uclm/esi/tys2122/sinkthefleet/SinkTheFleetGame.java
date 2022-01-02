package edu.uclm.esi.tys2122.sinkthefleet;

import edu.uclm.esi.tys2122.model.Game;
import edu.uclm.esi.tys2122.model.Match;

public class SinkTheFleetGame extends Game{

	public SinkTheFleetGame() {
		super();
	}

	@Override
	public Match newMatch() {
		return new SinkTheFleetMatch();
	}
}
