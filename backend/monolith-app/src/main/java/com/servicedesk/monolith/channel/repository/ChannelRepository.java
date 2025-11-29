package com.servicedesk.monolith.channel.repository;

import com.servicedesk.monolith.channel.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, String> {

    List<Channel> findByProjectId(String projectId);

    List<Channel> findByType(Channel.ChannelType type);

    List<Channel> findByEnabledTrue();

    List<Channel> findByProjectIdAndType(String projectId, Channel.ChannelType type);

    List<Channel> findByProjectIdAndEnabledTrue(String projectId);

    Optional<Channel> findByIdAndProjectId(String id, String projectId);

    @Query("SELECT c FROM Channel c WHERE c.type = :type AND c.enabled = true")
    List<Channel> findActiveChannelsByType(Channel.ChannelType type);

    boolean existsByNameAndProjectId(String name, String projectId);
}
