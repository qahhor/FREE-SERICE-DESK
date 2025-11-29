package com.servicedesk.monolith.ticket.mapper;

import com.servicedesk.monolith.ticket.dto.TicketCommentDto;
import com.servicedesk.monolith.ticket.entity.TicketComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TicketAttachmentMapper.class})
public interface TicketCommentMapper {

    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", expression = "java(comment.getAuthor().getFullName())")
    @Mapping(target = "authorEmail", source = "author.email")
    @Mapping(target = "authorAvatarUrl", source = "author.avatarUrl")
    TicketCommentDto toDto(TicketComment comment);

    List<TicketCommentDto> toDtoList(List<TicketComment> comments);
}
