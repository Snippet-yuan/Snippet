package com.snippet.auth.mapper;

import com.snippet.auth.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAccountMapper {
    UserAccount selectByUsername(@Param("username") String username);

    int insert(UserAccount user);
}
