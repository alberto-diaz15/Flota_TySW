package edu.uclm.esi.tys2122.http;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tys2122.dao.UserRepository;
import edu.uclm.esi.tys2122.model.Game;
import edu.uclm.esi.tys2122.model.Match;
import edu.uclm.esi.tys2122.model.User;
import edu.uclm.esi.tys2122.services.GamesService;
import edu.uclm.esi.tys2122.services.UserService;

@RestController
@RequestMapping("games")
public class GamesController extends CookiesController {
	@Autowired
	private GamesService gamesService;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private UserService userService;
	
	@GetMapping("/getGames")
	public List<Game> getGames(HttpSession session) throws Exception {
		return gamesService.getGames();
	}

	@GetMapping("/joinGame/{gameName}")
	public Match joinGame(HttpSession session, @PathVariable String gameName) throws Exception {
		User user;
		if (session.getAttribute("user")!=null) {
			user = (User) session.getAttribute("user");
		} else {
			user = new User();
			user.setName("anonimo" + new SecureRandom().nextInt(1000));
			session.setAttribute("user", user);
		}

		Manager.get().add(session);
		
		Game game = Manager.get().findGame(gameName);
		if (game==null)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No se encuentra el juego " + gameName);
		
		Match match = getMatch(game, user);
		match.setNombre(gameName);
		match.addPlayer(user);
		
		if(match.getOwner() == null) {
			match.setOwner(user);
		}
		if(gameName.equals("Hundir la flota")) {
			if(match.getBoard().getPlayer() == null && match.getOwner().getId().equals(user.getId())) {
				match.getBoard().setPlayer(user);
			}else if(match.getBoardOponente().getPlayer() == null){
				match.getBoardOponente().setPlayer(user);
			}
		}

		if (match.isReady()) {
			game.getPendingMatches().remove(match);
			game.getPlayingMatches().add(match);
		}
		//match.notifyNewState(user, match);
		gamesService.put(match);
		return match;
	}
	
	@PostMapping(value = "/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> credenciales) throws NoSuchAlgorithmException {
		JSONObject jso = new JSONObject(credenciales);
		Cookie cookie = super.findCookie(request.getCookies());
		if (cookie!=null) {
			User user = userRepo.findByCookie(cookie.getValue());
			if (user!=null) {
				String cookieAux = COOKIE_NAME;
				while(true) {
					try {
						cookieAux+= cookieAux+" ";
						user.setCookie(cookieAux);
						userRepo.save(user);
						userService.doLogout(jso.optString("userName"));
						request.getSession().removeAttribute("userId");
						request.getSession().removeAttribute("user");
						return "login";	
					}catch(Exception e) {
						
						
					}	
				}
			}
		}
		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No has iniciado sesion");
	}
	
	@PostMapping("/move")
	public Match move(HttpSession session, @RequestBody Map<String, Object> movement)  {
		User user = (User) session.getAttribute("user");
		JSONObject jso = new JSONObject(movement);
		Match match = gamesService.getMatch(jso.getString("matchId"));
		User winner = null;
		try {
			winner = match.move(user.getId(), jso);
			match.notifyNewState(user,match);
			if(winner != null) {
				addGameWon(winner);
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
		}
		gamesService.put(match);
		return match;
	}
	
	@PostMapping("/sendMsg")
	public Match sendMsg(HttpSession session, @RequestBody Map<String, Object> msg)  {
		User user = (User) session.getAttribute("user");
		JSONObject jso = new JSONObject(msg);
		Match match = gamesService.getMatch(jso.getString("matchId"));
		try {
			match.notifyNewMessage(user,jso.getString("msg"));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
		}
		gamesService.put(match);
		return match;
	}
	
	@PostMapping("/sendConnected")
	public Match sendConnected(HttpSession session, @RequestBody Map<String, Object> msg)  {
		User user = (User) session.getAttribute("user");
		JSONObject jso = new JSONObject(msg);
		Match match = gamesService.getMatch(jso.getString("matchId"));
		try {
			match.notifyNewState(user, match);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
		}
		gamesService.put(match);
		return match;
	}
	@PostMapping("/disconnected")
	public Match disconnected(HttpSession session, @RequestBody Map<String, Object> msg)  {
		User user = (User) session.getAttribute("user");
		JSONObject jso = new JSONObject(msg);
		Match match = gamesService.getMatch(jso.getString("matchId"));
		User winner = null;
		try {
			winner = match.notifyDisconnected(user,jso.getString("msg"));
			addGameWon(winner);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
		}
		
		gamesService.put(match);
		return match;
	}
	
	public void addGameWon(User user) {
		Optional<User> winnerAux = userRepo.findById(user.getId());
		if (!winnerAux.isEmpty()) {
			winnerAux.get().addGamesWon();
			userRepo.save(winnerAux.get());
		}	
	}
	
	@GetMapping("/findMatch/{matchId}")
	public Match findMatch(@PathVariable String matchId) {
		Match match=gamesService.getMatch(matchId);
		return match;
	}

	private Match getMatch(Game game,User user) {
		Match match;
		if (game.getPendingMatches().isEmpty()) {
			match = game.newMatch();
			game.getPendingMatches().add(match);
		} else {
			match = game.getPendingMatches().get(0);
		}
		return match;
	}
}
