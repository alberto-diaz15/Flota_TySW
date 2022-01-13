package edu.uclm.esi.tys2122.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uclm.esi.tys2122.model.Login;
import edu.uclm.esi.tys2122.model.Token;

public interface LoginRepository extends JpaRepository <Login, String> {
	public Login findByEmail(String email);

}
