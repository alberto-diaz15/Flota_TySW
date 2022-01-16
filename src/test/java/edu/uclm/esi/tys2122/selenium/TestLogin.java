package edu.uclm.esi.tys2122.selenium;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.uclm.esi.tys2122.dao.UserRepository;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TestLogin {
	private static WebDriver jugador;
		
	@Autowired
	private static UserRepository userDao;
	
	@BeforeAll
	public static void setUp() throws Exception {
		String userHome = System.getProperty("user.home");
		userHome = userHome.replace('\\', '/');
		if (!userHome.endsWith("/"))
			userHome = userHome + "/";
		
		System.setProperty("webdriver.chrome.driver", userHome + "chromedriver/chromedriver.exe");
		
		jugador= new ChromeDriver();
		jugador.manage().window().maximize();
		jugador.get("http://localhost:8080");
	}
	
	@Test
	@Order(1)
	public void testLogin() {
		jugador.findElement(By.id("ui-id-3")).click();
		
		jugador.findElement(By.id("tbUsuario")).clear();
		jugador.findElement(By.id("tbPasswd")).clear();
		jugador.findElement(By.id("entrar")).click();
		pausa(1000);
		jugador.findElement(By.id("tbUsuario")).sendKeys("inexistente");
		jugador.findElement(By.id("tbPasswd")).sendKeys("nofunciona");
		jugador.findElement(By.id("entrar")).click();
		pausa(1000);
		jugador.findElement(By.id("tbUsuario")).sendKeys("usuarioextremadamentelargooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
		jugador.findElement(By.id("tbPasswd")).sendKeys("contraseñaextremadamentelargaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		jugador.findElement(By.id("entrar")).click();
		pausa(1000);
		jugador.findElement(By.id("tbUsuario")).clear();
		jugador.findElement(By.id("tbUsuario")).sendKeys("pepe");
		jugador.findElement(By.id("tbPasswd")).clear();
		jugador.findElement(By.id("tbPasswd")).sendKeys("pepe123");
		jugador.findElement(By.id("entrar")).click();
		pausa(1000);
	}

	@AfterAll
	public static void tearDown() {
		jugador.quit();
	}

	private void pausa(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}

