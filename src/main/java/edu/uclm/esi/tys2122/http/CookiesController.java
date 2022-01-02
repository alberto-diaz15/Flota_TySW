package edu.uclm.esi.tys2122.http;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class CookiesController {
	public final String COOKIE_NAME = "cookieJuegos";
	public final String COOKIE_PATH = "/";

	protected Cookie[] readOrCreateCookie(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		if (cookies==null || cookies.length<= 1)
			return createCookie(response);
		Cookie cookie = findCookie(cookies);
		
		//Esto esta chapucero no se si va a funcionar. Pan para hoy y hambre para mañana
		/*Cookie[] newCookies = null;
		if (cookie==null)
			newCookies = createCookie(response);*/
		return cookies;
	}

	protected Cookie findCookie(Cookie[] cookies) {
		if(cookies==null) {
			return null;
		}
		for (Cookie cookie : cookies)
			if (cookie.getName().equals(COOKIE_NAME))
				return cookie;
		return null;
	}
	//Me estoy cargando la cookie de JSESSIONID
	private Cookie[] createCookie(HttpServletResponse response) {
		Cookie[] cookies = new Cookie[2];
		Cookie cookie = new Cookie(COOKIE_NAME, UUID.randomUUID().toString());
		Cookie cookieContador = new Cookie("contadorJuegos", "0");
		cookieContador.setPath(COOKIE_PATH);
		cookie.setPath(COOKIE_PATH);
		cookie.setMaxAge(30*24*60*60);
		cookieContador.setMaxAge(30*24*60*60);
		response.addCookie(cookieContador);
		response.addCookie(cookie);
		cookies[0] = cookie;
		cookies[1] = cookieContador;
		return cookies;
	}

	public void incrementarContador(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		if(cookies==null) {
			return;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("contadorJuegos")) {
				String value = cookie.getValue();
				int n  = Integer.parseInt(value);
				n++;
				cookie.setValue(""+n);
			}
			response.addCookie(cookie);
		}
	}
}
