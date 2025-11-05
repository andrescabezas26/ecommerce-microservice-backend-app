package com.selimhorri.app.business.orderItem.controller;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selimhorri.app.business.auth.enums.ResourceType;
import com.selimhorri.app.business.auth.util.AuthUtil;
import com.selimhorri.app.business.orderItem.model.OrderItemDto;
import com.selimhorri.app.business.orderItem.model.response.OrderItemOrderItemServiceDtoCollectionResponse;
import com.selimhorri.app.business.orderItem.service.OrderItemClientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shippings")
@RequiredArgsConstructor
public class OrderItemController {

	@Autowired
	private AuthUtil authUtil;
	private final OrderItemClientService orderItemClientService;

	@GetMapping
	public ResponseEntity<OrderItemOrderItemServiceDtoCollectionResponse> findAll() {
		return ResponseEntity.ok(this.orderItemClientService.findAll().getBody());
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderItemDto> findById(
			@PathVariable("orderId") final String orderId, HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(orderId, ResourceType.ORDERS);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.orderItemClientService.findById(orderId).getBody());
	}

	@PostMapping
	public ResponseEntity<OrderItemDto> save(@RequestBody final OrderItemDto orderItemDto, HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(orderItemDto.getOrderId().toString(), ResourceType.ORDERS);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.orderItemClientService.save(orderItemDto).getBody());
	}

	@DeleteMapping("/{orderId}")
	public ResponseEntity<Boolean> deleteById(
			@PathVariable("orderId") final String orderId, HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(orderId, ResourceType.ORDERS);
		authUtil.canActivate(request, userId, userDetails);
		this.orderItemClientService.deleteById(orderId).getBody();
		return ResponseEntity.ok(true);
	}

}
