package com.selimhorri.app.business.auth.service.impl;

import org.slf4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.business.user.model.CredentialDto;
import com.selimhorri.app.business.user.model.UserDetailsImpl;
import com.selimhorri.app.constant.AppConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private static final String API_URL = AppConstant.DiscoveredDomainsApi.USER_SERVICE_HOST + "/api/credentials";
	private final RestTemplate restTemplate;

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		log.info("**UserDetails, load user by username*\n");
		try {
			CredentialDto credentialDto = this.restTemplate.getForObject(API_URL + "/username/" + username,
					CredentialDto.class);
			if (credentialDto == null) {
				log.error("User not found with username: {}", username);
				throw new UsernameNotFoundException("User not found with username: " + username);
			}
			return new UserDetailsImpl(credentialDto);
		} catch (RestClientException e) {
			log.error("Failed to load user with username: " + username, e);
			throw new UsernameNotFoundException("Failed to load user with username: " + username, e);
		}
	}

}
