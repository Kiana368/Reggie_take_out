package com.junsi.reggie.dto;


import com.junsi.reggie.entity.Setmeal;
import com.junsi.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
