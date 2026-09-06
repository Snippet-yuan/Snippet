package com.snippet.social.mapper;

import com.snippet.social.entity.FollowRelation;
import com.snippet.social.entity.RelationUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowMapper {

    RelationUser selectActiveUserById(@Param("userId") Long userId);

    List<RelationUser> selectFollowingUsers(
            @Param("followerId") Long followerId,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    boolean existsFollow(
            @Param("followerId") Long followerId,
            @Param("followingId") Long followingId
    );

    int insertFollow(
            @Param("followerId") Long followerId,
            @Param("followingId") Long followingId
    );

    int deleteFollow(
            @Param("followerId") Long followerId,
            @Param("followingId") Long followingId
    );
}
