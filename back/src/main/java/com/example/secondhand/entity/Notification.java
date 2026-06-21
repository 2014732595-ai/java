package com.example.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String type;

    private String title;

    private String content;

    private Integer isRead;

    private Long relatedId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
