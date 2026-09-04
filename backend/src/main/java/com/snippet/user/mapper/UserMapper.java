package com.snippet.user.mapper;

import com.snippet.user.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    UserProfile selectById(@Param("userId") Long userId);

    UserProfile selectByUsername(@Param("username") String username);

    UserProfile selectByEmail(@Param("email") String email);

    int updateProfile(
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("email") String email,
            @Param("nickname") String nickname,
            @Param("avatarAssetId") Long avatarAssetId,
            @Param("backgroundAssetId") Long backgroundAssetId
    );
}
