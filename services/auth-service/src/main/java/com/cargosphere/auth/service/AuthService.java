package com.cargosphere.auth.service;

import com.cargosphere.auth.dto.LoginRequest;
import com.cargosphere.auth.dto.LoginResponse;
import com.cargosphere.auth.dto.RegisterRequest;
import com.cargosphere.auth.dto.RegisterResponse;
import com.cargosphere.auth.dto.UserResponse;

import java.util.List;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);
}