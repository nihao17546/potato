package ${basePackageName}.service.impl;

import ${basePackageName}.pojo.vo.TokenResultVo;
import ${basePackageName}.service.IUploadService;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author codelee ${now}
 * @version ${version}
 */
@Service
public class QiniuUploadServiceImpl extends IUploadService {
    @Value("${ak}")
    private String ak;
    @Value("${sk}")
    private String sk;
    @Value("${bucket}")
    private String bucket;
    @Value("${host}")
    private String host;

    private Auth auth;
    private Auth getAuth() {
        if (auth == null) {
            synchronized (this) {
                if (auth == null) {
                    auth = Auth.create(ak, sk);
                }
            }
        }
        return auth;
    }

    @Override
    public TokenResultVo getToken(String fileName) {
        String saveKey = getSaveKey(fileName);
        String token = getAuth().uploadToken(bucket, null, 3600, new StringMap().putNotEmpty("saveKey", saveKey));
        TokenResultVo result = new TokenResultVo();
        result.setToken(token);
        result.setHost(host);
        result.setKey(saveKey);
        return result;
    }
}
