package com.example.secondhand.controller.user;

import com.example.secondhand.common.Result;
import com.example.secondhand.entity.Comment;
import com.example.secondhand.entity.User;
import com.example.secondhand.service.CommentService;
import com.example.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @GetMapping("/{productId}")
    public Result<List<Comment>> listComments(@PathVariable Long productId) {
        List<Comment> comments = commentService.getComments(productId);
        return Result.success(comments);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<Void> createComment(@RequestBody Map<String, Object> params) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserInfo(username);
        Long productId = Long.valueOf(params.get("productId").toString());
        String content = params.get("content").toString();
        commentService.createComment(user.getId(), productId, content);
        return Result.success();
    }
}
