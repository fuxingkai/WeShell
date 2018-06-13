package cn.jufuns.ws.utils;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * JSON解析相关工具类
 *
 * @author zch 2016-7-2
 */
public class JsonUtils {

    // 采取单例模式
    private static Gson gson = new Gson();

    private JsonUtils() {
    }

    /**
     * 将对象转为JSON串
     *
     * @param src 将要被转化的对象
     * @return 转化后的JSON串
     */
    public static String toJson(Object src) {
        if (null == src) {
            return gson.toJson(JsonNull.INSTANCE);
        }
        try {
            return gson.toJson(src);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用来将JSON串转为对象，但此方法不可用来转带泛型的集合
     *
     * @param json     JSON串
     * @param classOfT 对象的类型
     * @return
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return gson.fromJson(json, (Type) classOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用来将JSON串转为对象，此方法可用来转带泛型的集合，如：Type为 new TypeToken<List<T>>(){}.getType()
     * ，其它类也可以用此方法调用，就是将List<T>替换为你想要转成的类
     *
     * @param json    JSON串
     * @param typeOfT new TypeToken<List<T>>(){}.getType()
     * @return
     */
    public static Object fromJson(String json, Type typeOfT) {
        try {
            return gson.fromJson(json, typeOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取json中的某个值，若不存在，返回 ""
     *
     * @param json JSON串
     * @param key
     * @return
     */
    public static String getValue(String json, String key) {
        try {
            JSONObject object = new JSONObject(json);
            return object.optString(key);// 若不存在，返回 ""
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

}
