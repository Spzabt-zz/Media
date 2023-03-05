package com.monitor.media.repo;

import com.monitor.media.domain.Message;
import com.monitor.media.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Long> {
    //@QueryHints(value = {@QueryHint(name = "org.hibernate.fetchSize", value = "0")})
    /*@EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            value = "message_entity-graph")*/
    /*@Query("select m from Message m join fetch m.comments")
    Page<Message> findAll(Pageable pageable);*/

    @Query("select m.id from Message m")
    List<Long> getAllIds(Pageable page);

    @Query(value = "select m from Message m left join fetch m.comments where m.id IN (:ids)")
    List<Message> findAllMessages(@Param("ids") List<Long> ids);

    @Query(value = "SELECT m FROM Message m WHERE m IN :messages AND m.author IN :users")
    List<Message> findMessagesByUser(@Param("users") List<User> users, @Param("messages") List<Message> messages);

    //List<Message> findByAuthorIn(List<User> users, List<Message> messages);
}