package com.selimhorri.app.business.user.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.business.auth.enums.ResourceType;
import com.selimhorri.app.business.auth.util.AuthUtil;
import com.selimhorri.app.business.user.model.CredentialDto;
import com.selimhorri.app.business.user.model.response.CredentialUserServiceCollectionDtoResponse;
import com.selimhorri.app.business.user.service.CredentialClientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/credentials")
@RequiredArgsConstructor
public class CredentialController {

	@Autowired
	private AuthUtil authUtil;
	private final CredentialClientService credentialClientService;

	@GetMapping
	public ResponseEntity<CredentialUserServiceCollectionDtoResponse> findAll() {
		return ResponseEntity.ok(this.credentialClientService.findAll().getBody());
	}

	@GetMapping("/{credentialId}")
	public ResponseEntity<CredentialDto> findById(@PathVariable("credentialId") final String credentialId,
			HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(credentialId, ResourceType.CREDENTIALS);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.credentialClientService.findById(credentialId).getBody());
	}

	@GetMapping("/username/{username}")
	public ResponseEntity<CredentialDto> findByCredentialname(@PathVariable("username") final String username) {
		return ResponseEntity.ok(this.credentialClientService.findByUsername(username).getBody());
	}

	@PostMapping
	public ResponseEntity<CredentialDto> save(@RequestBody final CredentialDto credentialDto,
			HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		// ✅ Comentado para permitir creación de credenciales sin autenticación
		// authUtil.canActivate(request,
		// credentialDto.getUserDto().getUserId().toString(), userDetails);
		return ResponseEntity.ok(this.credentialClientService.save(credentialDto).getBody());
	}

	@DeleteMapping("/{credentialId}")
	public ResponseEntity<Boolean> deleteById(@PathVariable("credentialId") final String credentialId,
			HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(credentialId, ResourceType.CREDENTIALS);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.credentialClientService.deleteById(credentialId).getBody());
	}

}
