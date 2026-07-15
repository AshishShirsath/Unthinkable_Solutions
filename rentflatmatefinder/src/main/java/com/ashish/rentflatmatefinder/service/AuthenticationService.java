package com.ashish.rentflatmatefinder.service;

import com.ashish.rentflatmatefinder.dto.request.LoginRequest;
import com.ashish.rentflatmatefinder.dto.request.RegisterRequest;
import com.ashish.rentflatmatefinder.dto.response.AuthenticationResponse;

public interface AuthenticationService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

}