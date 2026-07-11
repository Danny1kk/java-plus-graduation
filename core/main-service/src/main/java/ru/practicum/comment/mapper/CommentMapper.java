package ru.practicum.comment.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentShortDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    public static Comment returnComment(NewCommentDto dto, Long userId, Long eventId) {
        return Comment.builder()
                .authorId(userId)
                .eventId(eventId)
                .text(dto.getText())
                .status(CommentStatus.PENDING)
                .build();
    }

    public static CommentDto returnCommentDto(Comment comment, String authorName) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventId(comment.getEventId())
                .authorName(authorName != null ? authorName : "User #" + comment.getAuthorId())
                .created(comment.getCreated())
                .status(comment.getStatus().name())
                .build();
    }

    public static CommentShortDto toCommentShortDto(Comment comment, String userName, String eventTitle) {
        return CommentShortDto.builder()
                .userName(userName)
                .eventTitle(eventTitle)
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }
}