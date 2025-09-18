package com.sanisidro.restaurante.core.config;

import com.sanisidro.restaurante.core.security.model.User;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws BadCredentialsException {

        if (userDetails instanceof User user) {
            if (user.isGoogleUser()) {
                throw new BadCredentialsException("Google users cannot login with password");
            }
        }

        super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
