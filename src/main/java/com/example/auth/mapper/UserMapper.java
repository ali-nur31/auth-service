package com.example.auth.mapper;

import com.example.auth.dto.LoginResponse;
import com.example.auth.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "ExpiresInSeconds", ignore = true)
    LoginResponse toLoginResponse(User user);
}