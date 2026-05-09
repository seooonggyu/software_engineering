package com.project.software_engineering.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ChatRoom extends AuditingFields {

    @ManyToOne
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id")
    private User user2;

    protected ChatRoom() {}

    private ChatRoom(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    public static ChatRoom of(User user1, User user2) {
        return new ChatRoom(user1, user2);
    }
}
