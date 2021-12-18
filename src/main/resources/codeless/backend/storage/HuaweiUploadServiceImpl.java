package ${basePackageName}.service.impl;

import ${basePackageName}.pojo.vo.TokenResultVo;
import ${basePackageName}.service.IUploadService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.sdk.DefaultRequest;
import com.cloud.sdk.Request;
import com.cloud.sdk.auth.credentials.BasicCredentials;
import com.cloud.sdk.auth.signer.Signer;
import com.cloud.sdk.auth.signer.SignerFactory;
import com.cloud.sdk.http.HttpMethodName;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author codelee ${now}
 * @version ${version}
 */
@Service
public class HuaweiUploadServiceImpl extends IUploadService {
    private final String ak = "${ak}";
    private final String sk = "${sk}";
    private final String bucket = "${bucket}";
    private final String host = "${host}";
    private final String region = "${region}";

    private final int durationSeconds = 3600;
    private static final Map<String,String> defaultHeaders = new HashMap<>();
    private final String tokenContent = "{" +
            "	\"auth\":{" +
            "		\"identity\":{" +
            "			\"methods\":[\"token\"],\"token\": {\n" +
            "                \"duration-seconds\": \"" + durationSeconds + "\"\n" +
            "            }" +
            "		}" +
            "	}" +
            "}";

    static {
        HuaweiUploadServiceImpl.defaultHeaders.put("Content-Type","application/json");
    }

    @Override
    public TokenResultVo getToken(String fileName) {
        String saveKey = getSaveKey(fileName);
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpEntity httpEntity = null;
        try {
            String urlStr = "https://iam." + region + ".myhuaweicloud.com/v3.0/OS-CREDENTIAL/securitytokens";
            URL url = new URL(urlStr);
            Request request = new DefaultRequest();
            request.setEndpoint(url.toURI());
            request.setHttpMethod(HttpMethodName.POST);
            request.setHeaders(defaultHeaders);
            request.setContent(new ByteArrayInputStream(tokenContent.getBytes()));
            Signer signer = SignerFactory.getSigner();
            signer.sign(request, new BasicCredentials(ak, sk));

            HttpPost httpPost = new HttpPost(url.toString());
            InputStreamEntity entity = new InputStreamEntity(request.getContent());
            httpPost.setEntity(entity);
            Map<String, String> requestHeaders = request.getHeaders();
            for (String key : requestHeaders.keySet()) {
                httpPost.addHeader(key, requestHeaders.get(key));
            }

            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .useTLS().build();
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext, new AllowAllHostnameVerifier());

            httpClient = HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();
            response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() < 200
                    || response.getStatusLine().getStatusCode() > 300) {
                throw new RuntimeException(response.getStatusLine().toString());
            }
            httpEntity = response.getEntity();
            String resultStr = EntityUtils.toString(httpEntity, "utf-8");
            JSONObject re = JSON.parseObject(resultStr);
            JSONObject credential = re.getJSONObject("credential");
            TokenResultVo result = new TokenResultVo();
            result.setAccess(credential.getString("access"));
            result.setSecret(credential.getString("secret"));
            result.setEndpoint("https://obs." + region + ".myhuaweicloud.com");
            result.setBucket(bucket);
            result.setHost(host);
            result.setToken(credential.getString("securitytoken"));
            result.setKey(saveKey);
            return result;
        } catch (Exception e) {
            throw  new RuntimeException(e);
        } finally {
            if (httpEntity != null) {
                try {
                    EntityUtils.consume(httpEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
