

import com.kanade.mipaaudio.config.SpringConfig;
import com.kanade.mipaaudio.config.WebConfig;
import com.kanade.mipaaudio.config.MyBatisFlexConfig;
import com.kanade.mipaaudio.entity.Audio;
import com.kanade.mipaaudio.mapper.AudioMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

/**
 * ✅ Web 环境专用测试
 * ✅ 解决 No ServletContext set 报错
 * ✅ 支持 MockMvc 接口测试 + 数据库测试
 */
@Slf4j
// 核心注解：开启 Spring Web 测试环境（替代你原来的注解）
@SpringJUnitWebConfig(classes = {SpringConfig.class, WebConfig.class, MyBatisFlexConfig.class})
public class MipaAudioWebTest {

    // Web 上下文
    @Autowired
    private WebApplicationContext wac;

    // 模拟浏览器请求
    private MockMvc mockMvc;

    // 注入 Mapper
    @Autowired
    private AudioMapper audioMapper;

    // 初始化 Web 环境
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // ====================== 1. 数据库测试 ======================
    @Test
    public void testAudioList() {
        List<Audio> list = audioMapper.selectListByQuery(QueryWrapper.create().eq("isDelete", 0));
        log.info("✅ 查询音频总数：{}", list.size());
        list.forEach(System.out::println);
    }

    // ====================== 2. 接口测试（Web） ======================
    @Test
    public void testApiList() throws Exception {
        // 模拟请求 GET /audio/list
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/audio/list")
        ).andReturn();
        log.info("✅ Web 接口测试完成！");
    }

    @Test
    public void testInsert() {
        Audio audio = new Audio();
        audio.setUploadUser("测试用户");
        audio.setAudioName("测试音频.mp3");
        audio.setFileSize(2048000L);
        audio.setCategory("测试");
        audio.setSavePath("/upload/audio/test.mp3");
        audio.setIsDelete(0);
        // ✅ 修复：必须赋值 fileMd5（数据库非空字段）
        audio.setFileMd5("TEST_MD5_" + System.currentTimeMillis());

        audioMapper.insert(audio);
        log.info("✅ 插入成功！");
    }
}