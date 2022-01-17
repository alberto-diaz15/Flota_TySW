package edu.uclm.esi.tys2122.http;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.openqa.selenium.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tys2122.dao.TokenRepository;
import edu.uclm.esi.tys2122.dao.UserRepository;
import edu.uclm.esi.tys2122.model.Email;
import edu.uclm.esi.tys2122.model.Token;
import edu.uclm.esi.tys2122.model.User;
import edu.uclm.esi.tys2122.services.UserService;

@RestController
@RequestMapping("user")
public class UserController extends CookiesController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRepository userDAO;
	
	@Autowired
	private TokenRepository tokenRepo;
	
	@GetMapping("/heLlegado")
	public User heLlegado(HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = super.findCookie(request.getCookies());
		if (cookie!=null) {
			super.incrementarContador(request, response);
			User user = userDAO.findByCookie(cookie.getValue());
			if (user!=null) {
				userService.insertLogin(user, request.getRemoteAddr(), cookie);
				userService.doLogin(user.getName(), user.getPwd(), request.getRemoteAddr());
				request.getSession().setAttribute("userId", user.getId());
				request.getSession().setAttribute("user", user);
				
				return user;
			}
		}
		return null;
	}
	@PostMapping(value = "/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> credenciales) throws NoSuchAlgorithmException {
		JSONObject jso = new JSONObject(credenciales);
		Cookie cookie = super.findCookie(request.getCookies());
		if (cookie!=null) {
			User user = userDAO.findByCookie(cookie.getValue());
			if (user!=null) {
				user.setCookie(COOKIE_NAME);
				userService.doLogout(jso.optString("userName"));
				request.getSession().removeAttribute("userId");
				request.getSession().removeAttribute("user");
				return "login";
			}
		}
		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No has iniciado sesion");
	}
	
	@PostMapping(value = "/login")
	public User login(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> credenciales) throws NoSuchAlgorithmException {
		JSONObject jso = new JSONObject(credenciales);
		if(jso.optString("origen").length()>0) {
			return loginDeOtro(request,response,jso,credenciales);
		}else {
			return loginClasico(request, response, jso);

		}
	}

	private User loginDeOtro(HttpServletRequest request, HttpServletResponse response, JSONObject jso,@RequestBody Map<String, Object> credenciales) {
		String name = jso.getString("name");
		String email = jso.getString("email");
		String googleId = jso.getString("googleId");
		String ip = request.getRemoteAddr();

		User user = userService.doLoginGoogle(googleId);
		if(user != null) {
			Cookie[] cookies = readOrCreateCookie(request, response);
			Cookie cookie = findCookie(cookies);
			user.setCookie(cookie.getValue());
			userDAO.save(user);
			userService.insertLogin(user, ip, cookies[0]);
			request.getSession().setAttribute("userId", user.getId());
			request.getSession().setAttribute("user", user);
		}else {
			registerGoogle(credenciales);
			user = userService.doLoginGoogle(googleId);
			Cookie[] cookies = readOrCreateCookie(request, response);
			Cookie cookie = findCookie(cookies);
			user.setCookie(cookie.getValue());
			userDAO.save(user);
			userService.insertLogin(user, ip, cookies[0]);
			request.getSession().setAttribute("userId", user.getId());
			request.getSession().setAttribute("user", user);
		}
		return user;
	}

	private User loginClasico(HttpServletRequest request, HttpServletResponse response, JSONObject jso) throws NoSuchAlgorithmException {
		String name = jso.getString("name");
		String pwd = jso.getString("pwd");
		String ip = request.getRemoteAddr();
		User user = userService.doLogin(name, pwd, ip);
		Cookie[] cookies = readOrCreateCookie(request, response);
		for(int i=0;cookies!=null&&i<cookies.length;i++) {
		Cookie cookie = findCookie(cookies);
		if(cookie.getValue()!=null)
			user.setCookie(cookie.getValue());
		}
		userDAO.save(user);
		userService.insertLogin(user, ip, cookies[0]);
		request.getSession().setAttribute("userId", user.getId());
		request.getSession().setAttribute("user", user);
		return user;
	}

	@PostMapping(value = "/sendRestorePwd")	
	public void sendRestorePwd(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> credenciales) throws NoSuchAlgorithmException {
		JSONObject jso = new JSONObject(credenciales);
		String userName = jso.optString("name");
		Optional<User> optUser = userDAO.findByName(userName);
		if(optUser.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: usuario no encontrado");
		}
		User user = optUser.get(); 
		String ip = request.getRemoteAddr();
		Token token = new Token(user.getEmail());
		tokenRepo.save(token);
		Email smtp=new Email();
		smtp.sendRestore(user.getEmail(),token.getId()); 
		//Comprobando que metodo usar para asegurarme que es el usuario el que pide recuperar contraseña, dudo si usar la IP o el token
		//userService.doRestore(userName, user.getEmail(), ip);
	}
	
	@PostMapping(value = "/restorePwd")	
	public void restorePwd(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> credenciales) throws NoSuchAlgorithmException {
		JSONObject jso = new JSONObject(credenciales);
		String email = jso.optString("email");
		String pwd1 = jso.optString("pwd1");
		String pwd2 = jso.optString("pwd2");
		String idToken = jso.optString("token");
		User user = userDAO.findByEmail(email);
		if(user == null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: usuario no encontrado");
		}
		
		if(pwd1.isEmpty() || pwd2.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: inserte una contraseña");
		}
		if (!pwd1.equals(pwd2))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: the passwords do not match");
		if (pwd1.length()<4)
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: la contraseña debe tener al menos cuatro caracteres");
		
		String ip = request.getRemoteAddr();
		userService.validateToken(idToken);

		user.setPwd(org.apache.commons.codec.digest.DigestUtils.sha512Hex(pwd1));
		userService.save(user);
		//userService.doRestore(userName, user.getEmail(), ip);
	}
	
	@PutMapping("/register")
	@ResponseBody
	public String register(@RequestBody Map<String, Object> credenciales) {
		JSONObject jso = new JSONObject(credenciales);
		String userName = jso.optString("userName");
		String email = jso.optString("email");
		String pwd1 = jso.optString("pwd1");
		String pwd2 = jso.optString("pwd2");
		String picture = jso.optString("picture");
		if(pwd1.isEmpty() || pwd2.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: inserte una contraseña");
		}
		if (!pwd1.equals(pwd2))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: the passwords do not match");
		if (pwd1.length()<4)
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: la contraseña debe tener al menos cuatro caracteres");
		
		User user = new User();
		user.setName(userName);
		user.setEmail(email);
		user.setPwd(org.apache.commons.codec.digest.DigestUtils.sha512Hex(pwd1));
		user.setPicture(picture);
		
		try{
			userService.save(user);
		}
		catch(Exception e) {
		    if(e.getCause() != null && e.getCause().getCause() instanceof SQLIntegrityConstraintViolationException) {
		        SQLIntegrityConstraintViolationException sql_violation_exception = (SQLIntegrityConstraintViolationException) e.getCause().getCause() ;
				System.err.println("Error, ese usuario ya existe: " + sql_violation_exception.getMessage());
		    } else {
		    	System.err.println("Error, ese usuario ya existe: " +e.getMessage());
		    }
		    return "Error, ese usuario ya existe";
		}

		return "Cuenta creada correctamente";
	}
	
	public String registerGoogle(@RequestBody Map<String, Object> credenciales) {
		JSONObject jso = new JSONObject(credenciales);
		String userName = jso.optString("name");
		String email = jso.optString("email");
		String googleId = jso.optString("googleId");
		User user = new User();
		user.setName(userName);
		user.setEmail(email);
		user.setId(googleId);
		
		try{
			userService.save(user);
		}
		catch(Exception e) {
		    if(e.getCause() != null && e.getCause().getCause() instanceof SQLIntegrityConstraintViolationException) {
		        SQLIntegrityConstraintViolationException sql_violation_exception = (SQLIntegrityConstraintViolationException) e.getCause().getCause() ;
				System.err.println("Error, ese usuario ya existe: " + sql_violation_exception.getMessage());
		    } else {
		    	System.err.println("Error, ese usuario ya existe: " +e.getMessage());
		    }
		    return "Error, ese usuario ya existe";
		}
		return "Cuenta creada correctamente";
	}	
	@DeleteMapping("/remove/{userId}")
	public void remove(@PathVariable String userId) {
		System.out.println("Borrar el usuario con id " + userId);		
	}
	
	@GetMapping("/validateAccount/{tokenId}")
	public void validateAccount(HttpServletRequest request, HttpServletResponse response, @PathVariable String tokenId) {
		userService.validateToken(tokenId);
		// Ir a la base de datos, buscar el token con ese tokenId en la tabla, ver que no ha caducado
		// y actualizar la confirmationDate del user
		System.out.println(tokenId);
		try {
			response.sendRedirect(Manager.get().getConfiguration().getString("home"));
		} catch (IOException e) {}
	}
}
