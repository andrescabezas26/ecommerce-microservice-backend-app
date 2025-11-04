package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.exception.wrapper.UsernameAlreadyExistsException;
import com.selimhorri.app.helper.CredentialMappingHelper;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.CredentialService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {

	private final CredentialRepository credentialRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public List<CredentialDto> findAll() {
		log.info("*** CredentialDto List, service; fetch all credentials *");
		return this.credentialRepository.findAll()
				.stream()
				.map(CredentialMappingHelper::map)
				.distinct()
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public CredentialDto findById(final Integer credentialId) {
		log.info("*** CredentialDto, service; fetch credential by ids *");
		return this.credentialRepository.findById(credentialId)
				.map(CredentialMappingHelper::map)
				.orElseThrow(() -> new CredentialNotFoundException(
						String.format("#### Credential with id: %d not found! ####", credentialId)));
	}

	@Override
	public CredentialDto findByUsername(final String username) {
		return CredentialMappingHelper.map(this.credentialRepository.findByUsername(username)
				.orElseThrow(() -> new UserObjectNotFoundException(
						String.format("#### Credential with username: %s not found! ####", username))));
	}

	@Override
	public CredentialDto save(final CredentialDto credentialDto) {
		log.info("*** CredentialDto, service; save credential *");
		credentialDto.setCredentialId(null);
		if (credentialRepository.existsByUsername(credentialDto.getUsername())) {
			throw new UsernameAlreadyExistsException("Username already exists: " + credentialDto.getUsername());
		}

		Integer userId = credentialDto.getUserDto().getUserId();
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserObjectNotFoundException("User not found with id: " + userId));

		if (credentialRepository.existsByUserUserId(userId)) {
			throw new IllegalArgumentException(
					"User with ID " + userId + " already has credentials. You may update them instead.");
		}

		String rawPassword = credentialDto.getPassword();
		String encodedPassword = passwordEncoder.encode(rawPassword);
		credentialDto.setPassword(encodedPassword);

		Credential credential = CredentialMappingHelper.map(credentialDto);
		credential.setUser(user);

		Credential saved = credentialRepository.save(credential);
		return CredentialMappingHelper.map(saved);
	}

	@Override
	public CredentialDto update(final CredentialDto credentialDto) {
		log.info("*** CredentialDto, service; update credential *");

		Credential existingCredential = credentialRepository.findById(credentialDto.getCredentialId())
				.orElseThrow(() -> new CredentialNotFoundException(
						"Credential not found with id: " + credentialDto.getCredentialId()));

		existingCredential.setUsername(credentialDto.getUsername());

		// Codifica la nueva contraseña
		String encodedPassword = passwordEncoder.encode(credentialDto.getPassword());
		existingCredential.setPassword(encodedPassword);

		existingCredential.setRoleBasedAuthority(credentialDto.getRoleBasedAuthority());
		existingCredential.setIsEnabled(credentialDto.getIsEnabled());
		existingCredential.setIsAccountNonExpired(credentialDto.getIsAccountNonExpired());
		existingCredential.setIsAccountNonLocked(credentialDto.getIsAccountNonLocked());
		existingCredential.setIsCredentialsNonExpired(credentialDto.getIsCredentialsNonExpired());

		Credential updatedCredential = credentialRepository.save(existingCredential);

		return CredentialMappingHelper.map(updatedCredential);
	}

	@Override
	public CredentialDto update(final Integer credentialId, final CredentialDto credentialDto) {
		log.info("*** CredentialDto, service; update credential with credentialId *");

		Credential existingCredential = credentialRepository.findById(credentialId)
				.orElseThrow(() -> new CredentialNotFoundException(
						"Credential not found with id: " + credentialDto.getCredentialId()));

		existingCredential.setUsername(credentialDto.getUsername());

		// Codifica la nueva contraseña
		String encodedPassword = passwordEncoder.encode(credentialDto.getPassword());
		existingCredential.setPassword(encodedPassword);

		existingCredential.setRoleBasedAuthority(credentialDto.getRoleBasedAuthority());
		existingCredential.setIsEnabled(credentialDto.getIsEnabled());
		existingCredential.setIsAccountNonExpired(credentialDto.getIsAccountNonExpired());
		existingCredential.setIsAccountNonLocked(credentialDto.getIsAccountNonLocked());
		existingCredential.setIsCredentialsNonExpired(credentialDto.getIsCredentialsNonExpired());

		Credential updatedCredential = this.credentialRepository.save(existingCredential);

		return CredentialMappingHelper.map(updatedCredential);
	}

	@Transactional
	@Override
	public void deleteById(final Integer credentialId) {
		log.info("*** Void, service; delete credential by id *");

		boolean exists = credentialRepository.existsById(credentialId);
		if (!exists) {
			throw new CredentialNotFoundException("Credential with id: "+credentialId+" not found");
		}

		this.credentialRepository.deleteByCredentialId(credentialId);
	}

}
