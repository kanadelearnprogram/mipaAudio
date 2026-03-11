package com.kanade.mipaaudio.mapper;

import com.kanade.mipaaudio.entity.Audio;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 米帕音频表-驼峰+秒传+逻辑删除 映射层。
 *
 * @author Lenovo
 * @since 2026-03-11
 */
@Mapper
public interface AudioMapper extends BaseMapper<Audio> {

}
