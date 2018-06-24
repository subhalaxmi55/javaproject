package com.sudhansu.demo.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.sudhansu.demo.service.LoginService;

@RestController
@RequestMapping("/login")
public class LoginController {
	@Autowired
	LoginService loginService;
	static Logger logger = Logger.getLogger(LoginController.class)
	
	@RequestMapping(value = "/validateLogin", method = RequestMethod.GET)
	public String validateLogin(@RequestParam(value="user_name") String user_name,@RequestParam(value="password") String password) {
		loginService.validateLogin(user_name,password);
}
