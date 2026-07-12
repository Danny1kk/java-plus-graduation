package ru.practicum.comment.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false)
    String text;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "author_id")
    private Long authorId;

    @CreationTimestamp
    @Column(name = "created")
    LocalDateTime created;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    CommentStatus status;
}