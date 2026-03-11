package com.kanade.mipaaudio.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.kanade.mipaaudio.entity.Audio;
import com.kanade.mipaaudio.mapper.AudioMapper;
import com.kanade.mipaaudio.service.AudioService;
import org.springframework.stereotype.Service;

/**
 * 米帕音频表 - 驼峰 + 秒传 + 逻辑删除 服务层实现。
 *
 * @author Lenovo
 * @since 2026-03-11
 */
@Service
public class AudioServiceImpl extends ServiceImpl<AudioMapper, Audio> implements AudioService {

}
