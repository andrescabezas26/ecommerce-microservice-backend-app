package com.selimhorri.app.business.auth.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.business.auth.model.request.AuthenticationRequest;
import com.selimhorri.app.business.auth.model.response.AuthenticationResponse;
import com.selimhorri.app.business.auth.service.AuthenticationService;
import com.selimhorri.app.business.user.model.CredentialDto;
import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.exception.wrapper.IllegalAuthenticationCredentialsException;
import com.selimhorri.app.jwt.service.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

	private static final String API_URL = AppConstant.DiscoveredDomainsApi.USER_SERVICE_HOST + "/api/credentials";

	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final JwtService jwtService;
	private final RestTemplate restTemplate;

	@Override
	public AuthenticationResponse authenticate(final AuthenticationRequest authenticationRequest) {

		log.info("** AuthenticationResponse, authenticate user service*\n");

		try {
			this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					authenticationRequest.getUsername(), authenticationRequest.getPassword()));
		} catch (BadCredentialsException e) {
			throw new IllegalAuthenticationCredentialsException("#### Bad credentials! ####");
		}
		CredentialDto credentialDto = this.restTemplate.getForObject(API_URL + "/username/" + authenticationRequest.getUsername(),
				CredentialDto.class);
				System.out.println(credentialDto.getPassword());
				System.out.println(credentialDto.getUsername());
				System.out.println("++++++++++++++");

		return new AuthenticationResponse(this.jwtService.generateToken(this.userDetailsService
				.loadUserByUsername(authenticationRequest.getUsername()), credentialDto.getUserDto().getUserId().toString()));
	}

}
