package com.selimhorri.app.business.auth.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.business.auth.enums.ResourceType;
import com.selimhorri.app.business.order.model.CartDto;
import com.selimhorri.app.business.order.model.OrderDto;
import com.selimhorri.app.business.payment.model.PaymentDto;
import com.selimhorri.app.business.user.model.AddressDto;
import com.selimhorri.app.business.user.model.CredentialDto;
import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.exception.wrapper.UnauthorizedException;

@Component
public class AuthUtil {

    private final RestTemplate restTemplate;

    public AuthUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void canActivate(HttpServletRequest request, String userId, UserDetails userDetails) {
        if (userId != null) {
            String authUserId = (String) request.getAttribute("userId");
            boolean isAdmin = getIsAdmin(userDetails);
            if (!isAdmin && !authUserId.equals(userId)) {
                throw new UnauthorizedException("You can access to resources of your own");
            }
        }
    }

    public String getOwner(String id, ResourceType resourceType) {
        String apiUrl = "";
        try {
            switch (resourceType) {
                case CREDENTIALS:
                    apiUrl = AppConstant.DiscoveredDomainsApi.USER_SERVICE_HOST + "/api/credentials/" + id;
                    CredentialDto credentialDto = restTemplate.getForObject(apiUrl, CredentialDto.class);
                    return credentialDto.getUserDto().getUserId().toString();
                case ADDRESSES:
                    apiUrl = AppConstant.DiscoveredDomainsApi.USER_SERVICE_HOST + "/api/address/" + id;
                    AddressDto addressDto = restTemplate.getForObject(apiUrl, AddressDto.class);
                    return addressDto.getUserDto().getUserId().toString();
                case CARTS:
                    apiUrl = AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_HOST + "/api/carts/" + id;
                    CartDto cartDto = restTemplate.getForObject(apiUrl, CartDto.class);
                    return cartDto.getUserDto().getUserId().toString();
                case ORDERS:
                    apiUrl = AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_HOST + "/api/orders/" + id;
                    OrderDto orderDto = restTemplate.getForObject(apiUrl, OrderDto.class);
                    apiUrl = AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_HOST + "/api/carts/"
                            + orderDto.getCartDto().getCartId();
                    CartDto orderCartDto = restTemplate.getForObject(apiUrl, CartDto.class);
                    return orderCartDto.getUserDto().getUserId().toString();
                case PAYMENTS:
                    apiUrl = AppConstant.DiscoveredDomainsApi.PAYMENT_SERVICE_HOST + "/api/payments/" + id;
                    PaymentDto paymentDto = restTemplate.getForObject(apiUrl, PaymentDto.class);
                    apiUrl = AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_HOST + "/api/orders/" + paymentDto.getOrderDto().getOrderId();
                    OrderDto paymentOrderDto = restTemplate.getForObject(apiUrl, OrderDto.class);
                    apiUrl = AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_HOST + "/api/carts/"
                            + paymentOrderDto.getCartDto().getCartId();
                    CartDto paymentCartDto = restTemplate.getForObject(apiUrl, CartDto.class);
                    return paymentCartDto.getUserDto().getUserId().toString();
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private boolean getIsAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}
