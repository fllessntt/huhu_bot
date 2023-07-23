package tech.chowyijiu.huhu_bot.plugins.resource_search;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

/**
 * @author elastic chow
 * @date 22/7/2023
 */
public class OpenwrtReq {

    public static final String baseUrl = "http://192.168.5.1/cgi-bin/luci";

    public static boolean invalidateCache() {
        HttpResponse response = HttpRequest.get(baseUrl + "/admin/services/aliyundrive-webdav/invalidate-cache")
                .header("Cookie", login("root", "261806"))
                .execute();
        Boolean ok = JSONObject.parseObject(response.body()).getBoolean("ok");
        return ok != null && ok;
    }

    //todo expire time
    public static String login(String username, String password) {
        Map<String, Object> map = Map.of("luci_username", username, "luci_password", password);
        HttpResponse response = HttpRequest.post(baseUrl)
                .form(map)
                .execute();
        return response.header("Set-Cookie");
    }
}
