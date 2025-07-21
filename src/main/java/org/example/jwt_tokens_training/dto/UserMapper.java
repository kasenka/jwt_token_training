package org.example.jwt_tokens_training.dto;

import org.example.jwt_tokens_training.model.User;
import org.mapstruct.*;

@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {
    @Mapping(target = "encryptedPassword", source = "password")
    public abstract User map(UserRegisterDTO dto);

    @Mapping(target = "encryptedPassword", source = "password")
    public abstract User map(UserLoginDTO dto);
    public abstract UserDTO map(User model);
}
