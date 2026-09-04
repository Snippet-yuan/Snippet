package com.snippet.auth.mapper;

import com.snippet.auth.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAccountMapper {

    UserAccount selectByUsername(@Param("username") String username);

    UserAccount selectById(@Param("userId") Long userId);

    UserAccount selectTokenStateById(@Param("userId") Long userId);

    int insert(UserAccount user);

    int updatePassword(
            @Param("userId") Long userId,
            @Param("passwordHash") String passwordHash
    );
}
