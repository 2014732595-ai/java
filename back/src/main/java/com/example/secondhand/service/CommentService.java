package com.example.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    List<Comment> getComments(Long productId);

    Long createComment(Long userId, Long productId, String content);

    void deleteComment(Long commentId, Long userId);
}
