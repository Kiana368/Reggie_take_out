package com.junsi.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junsi.reggie.dto.SetmealDto;
import com.junsi.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    // 保存套餐基本信息以及保存套餐内菜品关系信息
    public void saveWithDish(SetmealDto setmealDto);

    // 删除套餐基本信息以及相应套餐内菜品关系信息
    public void deleteWithDish(List<Long> ids);
}
