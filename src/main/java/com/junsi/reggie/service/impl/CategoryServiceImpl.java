package com.junsi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junsi.reggie.common.CustomException;
import com.junsi.reggie.entity.Category;
import com.junsi.reggie.entity.Dish;
import com.junsi.reggie.entity.Setmeal;
import com.junsi.reggie.mapper.CategoryMapper;
import com.junsi.reggie.service.CategoryService;
import com.junsi.reggie.service.DishService;
import com.junsi.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除前查询当前分类关联情况
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 查询当前分类是否关联菜品
        // 创建条件构造器
        LambdaQueryWrapper<Dish> queryWrapperDish = new LambdaQueryWrapper<>();
        // 查询条件
        queryWrapperDish.eq(Dish::getCategoryId, id);
        int countDish = dishService.count(queryWrapperDish);
        // 若已关联，则抛出业务异常
        if(countDish > 0) {
            throw new CustomException("当前分类下已关联菜品，不能删除");
        }

        // 查询当前分类是否关联套餐
        // 创建条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapperSetmeal = new LambdaQueryWrapper<>();
        // 查询条件
        queryWrapperSetmeal.eq(Setmeal::getCategoryId, id);
        int countSetmeal = setmealService.count(queryWrapperSetmeal);
        // 若已关联，则抛出业务异常
        if(countSetmeal > 0) {
            throw new CustomException("当前分类下已关联套餐，不能删除");
        }

        // 若无，则删除
        super.removeById(id);
    }
}
