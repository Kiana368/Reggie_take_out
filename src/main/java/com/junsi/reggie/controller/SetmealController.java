package com.junsi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.junsi.reggie.common.R;
import com.junsi.reggie.entity.Category;
import com.junsi.reggie.entity.Setmeal;
import com.junsi.reggie.service.CategoryService;
import com.junsi.reggie.service.SetmealDishService;
import com.junsi.reggie.service.SetmealService;
import com.junsi.reggie.dto.SetmealDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setMealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true) // 删除这个分类下所有数据
    public R<String> save(@RequestBody SetmealDto setMealDto) {
        setmealService.saveWithDish(setMealDto);
        return R.success("新增菜品成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtopageInfo = new Page<>(page, pageSize);

        // 构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        // 查询
        setmealService.page(pageInfo, queryWrapper);

        // 加上分类名称
        BeanUtils.copyProperties(pageInfo, setmealDtopageInfo, "record");
        List<Setmeal> setmeals = pageInfo.getRecords();
        List<SetmealDto> list = setmeals.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);

            // 根据分类id加上分类名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        // 把结果设置进分页查询器
        setmealDtopageInfo.setRecords(list);

        return R.success(setmealDtopageInfo);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true) // 删除这个分类下所有数据
    public R<String> delete(@RequestParam List<Long> ids) { // 加上@RequestParam注解来说明才能接收到
        setmealService.deleteWithDish(ids);
        return R.success("删除套餐成功");
    }

    /**
     * 更改套餐售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> changeStatus(HttpServletRequest request, @PathVariable int status, @RequestParam List<Long> ids) {
        for(Long id: ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            setmeal.setUpdateTime(LocalDateTime.now());
            setmeal.setUpdateUser((Long) request.getSession().getAttribute("employee"));

            setmealService.updateById(setmeal);
        }

        return R.success("修改套餐售卖状态成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, 1);
        // 添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        // 查询
        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

}
