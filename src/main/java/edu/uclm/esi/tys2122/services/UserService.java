package edu.uclm.esi.tys2122.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tys2122.dao.LoginRepository;
import edu.uclm.esi.tys2122.dao.TokenRepository;
import edu.uclm.esi.tys2122.dao.UserRepository;
import edu.uclm.esi.tys2122.model.Login;
import edu.uclm.esi.tys2122.model.Token;
import edu.uclm.esi.tys2122.model.User;

@Service
public class UserService {
	@Autowired
	private LoginRepository loginDAO;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private TokenRepository tokenRepo;
	
	private ConcurrentHashMap<String, User> connectedUsers;
	
	public UserService() {
		this.connectedUsers = new ConcurrentHashMap<>();
	}

	public User doLogin(String name, String pwd, String ip) {
		if(pwd.length() < 128) {
			pwd = org.apache.commons.codec.digest.DigestUtils.sha512Hex(pwd);
		}
		User user = userRepo.findByNameAndPwd(name, pwd);
		if (user==null) //  || user.getConfirmationDate()==null)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
		
		this.connectedUsers.put(user.getId(), user);
		return user;
	}
	
	public User doLogout(String name) {		
		Optional<User> user = userRepo.findByName(name);
		if (user.isEmpty()) //  || user.getConfirmationDate()==null)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No has iniciado sesion");
		User userLogout;
		if((userLogout =this.connectedUsers.remove(user.get().getId())) == null)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No has iniciado sesion");
		String cookie = " ";
		while(true) {
			try {
				cookie += " ";
				userLogout.setCookie(cookie);
				userRepo.save(userLogout);
				return userLogout;
			}catch(Exception e) {
				
			}
		}

	}
	
	public User doLoginGoogle(String userId) {		
		Optional<User> user = userRepo.findById(userId);
		if (user.isEmpty()) //  || user.getConfirmationDate()==null)
			return null;
		
		this.connectedUsers.put(user.get().getId(), user.get());
		return user.get();
	}
	
	public User doRestore(String name, String email, String ip) throws NoSuchAlgorithmException {
		//pwd = org.apache.commons.codec.digest.DigestUtils.sha512Hex(pwd);
		
		User user = userRepo.findByEmail(email);
		if (user==null) //  || user.getConfirmationDate()==null)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
		
		this.connectedUsers.put(user.getId(), user);
		return user;
	}

	public void save(User user) {
		userRepo.save(user);
		
		Token token = new Token(user.getEmail());
		tokenRepo.save(token);
		/*Email smtp=new Email();
		smtp.send(user.getEmail(), "Bienvenido al sistema", 
			"Para confirmar, pulse aquí: " +
			"http://localhost/user/validateAccount/" + token.getId());*/

	}

	public void validateToken(String tokenId) {
		Optional<Token> optToken = tokenRepo.findById(tokenId);
		if (optToken.isPresent()) {
			Token token = optToken.get();
			long date = token.getDate();
			long now = System.currentTimeMillis();
			if (now>date+24*60*60*1000)
				throw new ResponseStatusException(HttpStatus.GONE, "Token caducado");
			String email = token.getEmail();
			User user = userRepo.findByEmail(email);
			if (user!=null) {
				user.setConfirmationDate(now);
				userRepo.save(user);
				tokenRepo.delete(token);
			} else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
		} else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token " + tokenId + " no encontrado");
	}

	public void insertLogin(User user, String ip, Cookie cookie) {
		Login login;
		try {
			 login = loginDAO.findByEmail(user.getEmail());
		}catch(Exception e){
			login = new Login();
		}
		if(login == null) {
            login = new Login();
        }
		login.setEmail(user.getEmail());
		login.setDate(System.currentTimeMillis());
		login.setIp(ip);
		login.setCookieValue(cookie.getValue());
		loginDAO.save(login);
	}

	public User findUser(String userId) {
		return this.connectedUsers.get(userId);
	}

}
