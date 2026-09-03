package com.snippet.asset.mapper;

import com.snippet.asset.entity.Asset;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssetMapper {

    int insert(Asset asset);
}
