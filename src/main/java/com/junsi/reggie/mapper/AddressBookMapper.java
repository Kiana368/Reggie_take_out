package com.junsi.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.junsi.reggie.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {

}
