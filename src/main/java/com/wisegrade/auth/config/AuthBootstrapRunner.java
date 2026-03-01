package com.wisegrade.auth.config;

import com.wisegrade.auth.service.AuthBootstrapService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AuthBootstrapRunner implements ApplicationRunner {

    private final AuthBootstrapService authBootstrapService;

    public AuthBootstrapRunner(AuthBootstrapService authBootstrapService) {
        this.authBootstrapService = authBootstrapService;
    }

    @Override
    public void run(ApplicationArguments args) {
        authBootstrapService.ensureAdminUser();
    }
}
