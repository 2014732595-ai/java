package com.example.secondhand.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class OrderDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "地址ID不能为空")
    private Long addressId;
}
