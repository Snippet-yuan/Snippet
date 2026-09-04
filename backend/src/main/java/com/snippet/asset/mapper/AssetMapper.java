package com.snippet.asset.mapper;

import com.snippet.asset.entity.Asset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssetMapper {

    int insert(Asset asset);

    boolean existsOwnedReadyById(
            @Param("assetId") Long assetId,
            @Param("ownerId") Long ownerId
    );
}
