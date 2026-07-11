package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentShortDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CommentDto addComment(@Valid @RequestBody NewCommentDto commentNewDto,
                                 @PathVariable Long userId,
                                 @PathVariable Long eventId) {

        return commentService.addComment(userId, eventId, commentNewDto);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(value = HttpStatus.OK)
    public CommentDto updateComment(@Valid @RequestBody NewCommentDto commentNewDto,
                                    @PathVariable Long userId,
                                    @PathVariable Long commentId) {

        return commentService.updateComment(userId, commentId, commentNewDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {

        commentService.deletePrivateComment(userId, commentId);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public List<CommentShortDto> getCommentsByUserId(@PathVariable Long userId,
                                                     @RequestParam(required = false) String rangeStart,
                                                     @RequestParam(required = false) String rangeEnd,
                                                     @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                     @Positive @RequestParam(defaultValue = "10") Integer size) {

        return commentService.getCommentsByUserId(rangeStart, rangeEnd, userId, from, size);
    }
}