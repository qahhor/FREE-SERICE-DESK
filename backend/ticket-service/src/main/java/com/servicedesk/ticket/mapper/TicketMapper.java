package com.servicedesk.ticket.mapper;

import com.servicedesk.ticket.dto.TicketDto;
import com.servicedesk.ticket.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "projectKey", source = "project.key")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "requesterId", source = "requester.id")
    @Mapping(target = "requesterName", expression = "java(ticket.getRequester().getFullName())")
    @Mapping(target = "requesterEmail", source = "requester.email")
    @Mapping(target = "requesterAvatarUrl", source = "requester.avatarUrl")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "assigneeName", expression = "java(ticket.getAssignee() != null ? ticket.getAssignee().getFullName() : null)")
    @Mapping(target = "assigneeEmail", source = "assignee.email")
    @Mapping(target = "assigneeAvatarUrl", source = "assignee.avatarUrl")
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.name")
    @Mapping(target = "commentCount", expression = "java(ticket.getComments() != null ? ticket.getComments().size() : 0)")
    @Mapping(target = "attachmentCount", expression = "java(ticket.getAttachments() != null ? ticket.getAttachments().size() : 0)")
    TicketDto toDto(Ticket ticket);

    List<TicketDto> toDtoList(List<Ticket> tickets);
}
