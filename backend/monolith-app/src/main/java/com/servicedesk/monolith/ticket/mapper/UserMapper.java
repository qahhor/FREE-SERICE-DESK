package com.servicedesk.monolith.ticket.mapper;

import com.servicedesk.monolith.ticket.dto.UserDto;
import com.servicedesk.monolith.ticket.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.name")
    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);
}
