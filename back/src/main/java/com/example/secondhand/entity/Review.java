package com.example.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long productId;

    private Long userId;

    private Integer rating;

    private String content;

    private String images;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
