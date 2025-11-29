package com.servicedesk.monolith.ticket.mapper;

import com.servicedesk.monolith.ticket.dto.TicketAttachmentDto;
import com.servicedesk.monolith.ticket.entity.TicketAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketAttachmentMapper {

    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "uploadedById", source = "uploadedBy.id")
    @Mapping(target = "uploadedByName", expression = "java(attachment.getUploadedBy().getFullName())")
    @Mapping(target = "downloadUrl", expression = "java(\"/api/v1/attachments/\" + attachment.getId() + \"/download\")")
    @Mapping(target = "thumbnailUrl", expression = "java(attachment.getThumbnailPath() != null ? \"/api/v1/attachments/\" + attachment.getId() + \"/thumbnail\" : null)")
    TicketAttachmentDto toDto(TicketAttachment attachment);

    List<TicketAttachmentDto> toDtoList(List<TicketAttachment> attachments);
}
