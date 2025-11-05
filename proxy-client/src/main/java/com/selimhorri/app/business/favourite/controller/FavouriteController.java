package com.selimhorri.app.business.favourite.controller;

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

import com.selimhorri.app.business.auth.util.AuthUtil;
import com.selimhorri.app.business.favourite.model.FavouriteDto;
import com.selimhorri.app.business.favourite.model.FavouriteId;
import com.selimhorri.app.business.favourite.model.response.FavouriteFavouriteServiceCollectionDtoResponse;
import com.selimhorri.app.business.favourite.service.FavouriteClientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/favourites")
@RequiredArgsConstructor
public class FavouriteController {

	@Autowired
	private AuthUtil authUtil;
	private final FavouriteClientService favouriteClientService;

	@GetMapping
	public ResponseEntity<FavouriteFavouriteServiceCollectionDtoResponse> findAll() {
		return ResponseEntity.ok(this.favouriteClientService.findAll().getBody());
	}

	@GetMapping("/{userId}/{productId}")
	public ResponseEntity<FavouriteDto> findById(
			@PathVariable("userId") final String userId,
			@PathVariable("productId") final String productId,
			HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.favouriteClientService.findById(userId, productId).getBody());
	}

	@PostMapping
	public ResponseEntity<FavouriteDto> save(@RequestBody final FavouriteDto favouriteDto, HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		authUtil.canActivate(request, favouriteDto.getUserId().toString(), userDetails);
		return ResponseEntity.ok(this.favouriteClientService.save(favouriteDto).getBody());
	}

	@DeleteMapping("/{userId}/{productId}")
	public ResponseEntity<Boolean> deleteById(
			@PathVariable("userId") final String userId,
			@PathVariable("productId") final String productId, HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		authUtil.canActivate(request, userId, userDetails);
		this.favouriteClientService.deleteById(userId, productId).getBody();
		return ResponseEntity.ok(true);
	}

}
