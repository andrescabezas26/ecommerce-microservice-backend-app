package com.selimhorri.app.config.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.selimhorri.app.jwt.service.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

	private final UserDetailsService userDetailsService;
	private final JwtService jwtService;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		String method = request.getMethod();

		log.debug("shouldNotFilter check for: {} {}", method, path);

		// ✅ No filtrar OPTIONS (CORS preflight)
		if ("OPTIONS".equalsIgnoreCase(method)) {
			log.debug("Skipping filter for OPTIONS request");
			return true;
		}

		// ✅ No filtrar autenticación (con prefijo /app desde API Gateway)
		if (path.startsWith("/app/api/authenticate")) {
			log.debug("Skipping filter for authenticate endpoint");
			return true;
		}

		// ✅ No filtrar health checks
		if (path.startsWith("/actuator/health") || path.startsWith("/actuator/info")) {
			log.debug("Skipping filter for actuator endpoint");
			return true;
		}

		// ✅ No filtrar registro de usuarios (POST /app/api/users desde API Gateway)
		if (path.equals("/app/api/users") && "POST".equalsIgnoreCase(method)) {
			log.debug("Skipping filter for user registration");
			return true;
		}

		// ✅ No filtrar POST de credenciales (POST /app/api/credentials desde API
		// Gateway)
		if (path.equals("/app/api/credentials") && "POST".equalsIgnoreCase(method)) {
			log.debug("Skipping filter for credentials registration");
			return true;
		}

		// ✅ No filtrar GET de usuarios (GET /app/api/users desde API Gateway)
		if (path.startsWith("/app/api/users") && "GET".equalsIgnoreCase(method)) {
			log.debug("Skipping filter for user GET requests");
			return true;
		}

		// ✅ No filtrar GET de productos y categorías (con prefijo /app desde API
		// Gateway)
		if ("GET".equalsIgnoreCase(method)) {
			if (path.startsWith("/app/api/products") || path.startsWith("/app/api/categories")) {
				log.debug("Skipping filter for public GET endpoint");
				return true;
			}
		}

		// ✅ No filtrar recursos estáticos
		if (path.equals("/") || path.equals("/index") || path.equals("/index.html") ||
				path.contains("/css/") || path.contains("/js/") || path.contains("/images/")) {
			log.debug("Skipping filter for static resource");
			return true;
		}

		// ✅ Filtrar todo lo demás
		log.debug("Applying JWT filter to this request");
		return false;
	}

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain filterChain) throws ServletException, IOException {

		log.debug("**JwtRequestFilter processing: {} {}", request.getMethod(), request.getRequestURI());

		final String authorizationHeader = request.getHeader("Authorization");

		String username = null;
		String jwt = null;

		// Extraer token del header Authorization
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			jwt = authorizationHeader.substring(7);
			try {
				username = jwtService.extractUsername(jwt);
			} catch (Exception e) {
				log.error("Error extracting username from JWT: {}", e.getMessage());
			}
		}

		// Si hay username y no hay autenticación en el contexto
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

				// Validar token
				if (this.jwtService.validateToken(jwt, userDetails)) {
					final String userId = jwtService.extractUserId(jwt);

					// Crear autenticación
					final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
							userDetails,
							null,
							userDetails.getAuthorities());

					authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					// Agregar userId al request
					request.setAttribute("userId", userId);

					// Establecer autenticación en el contexto
					SecurityContextHolder.getContext().setAuthentication(authenticationToken);

					log.debug("User '{}' authenticated with userId: {}", username, userId);
				} else {
					log.warn("Invalid JWT token for user: {}", username);
				}
			} catch (Exception e) {
				log.error("Error during JWT authentication: {}", e.getMessage());
			}
		}

		filterChain.doFilter(request, response);
		log.debug("**Jwt request filtered!");
	}
}