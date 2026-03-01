package com.wisegrade.auth.api;

import com.wisegrade.auth.api.dto.AuthBulkEstudiantesRequest;
import com.wisegrade.auth.api.dto.AuthBulkEstudiantesResponse;
import com.wisegrade.auth.api.dto.AuthLoginRequest;
import com.wisegrade.auth.api.dto.AuthMeResponse;
import com.wisegrade.auth.api.dto.AuthUserCreateRequest;
import com.wisegrade.auth.security.AuthPrincipal;
import com.wisegrade.auth.service.AuthMeService;
import com.wisegrade.auth.service.AuthUserService;
import com.wisegrade.common.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AuthMeService authMeService;
    private final AuthUserService authUserService;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            AuthMeService authMeService,
            AuthUserService authUserService) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.authMeService = authMeService;
        this.authUserService = authUserService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthMeResponse login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.documento(), request.clave()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        Object principal = auth.getPrincipal();
        if (!(principal instanceof AuthPrincipal ap)) {
            throw new BadRequestException("Principal inválido");
        }
        return authMeService.toMe(ap);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public AuthMeResponse me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal ap)) {
            throw new BadRequestException("No autenticado");
        }
        return authMeService.toMe(ap);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@Valid @RequestBody AuthUserCreateRequest req) {
        authUserService.createUser(req);
    }

    @PostMapping("/users/bulk/estudiantes")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public AuthBulkEstudiantesResponse bulkCreateEstudianteUsers(
            @RequestBody(required = false) AuthBulkEstudiantesRequest req) {
        return authUserService.bulkCreateEstudianteUsers(req);
    }
}
