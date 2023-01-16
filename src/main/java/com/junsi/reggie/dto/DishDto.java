package com.junsi.reggie.dto;

import com.junsi.reggie.entity.Dish;
import com.junsi.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于接受含有DIsh类数据并且还有其他数据的一个封装类
 */
@Data
public class DishDto extends Dish {

    // 还可以接收其他数据
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
