package tech.chowyijiu.huhubot.core.entity.response;

import lombok.Data;

/**
 * @author elastic chow
 * @date 14/5/2023
 */
@Data
public class DownloadFileResp {
    // gocq下载文件的绝对路径
    private String file;
}