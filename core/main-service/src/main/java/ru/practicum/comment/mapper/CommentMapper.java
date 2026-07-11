package ru.practicum.comment.mapper;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;
import ru.practicum.comment.dto.*;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.user.UserClient;

import java.util.ArrayList;
import java.util.List;

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

    public static CommentDto returnCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventId(comment.getEventId())
                .authorName(authorName != null ? authorName : "User #" + comment.getAuthorId())
                .created(comment.getCreated())
                .status(comment.getStatus().name())
                .build();
    }

    public static List<CommentDto> returnCommentDtoList(Iterable<Comment> comments) {
        List<CommentDto> result = new ArrayList<>();
        for (Comment comment : comments) {
            result.add(returnCommentDto(comment));
        }
        return result;
    }
}