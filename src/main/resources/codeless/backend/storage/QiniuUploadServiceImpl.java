package ${basePackageName}.service.impl;

import ${basePackageName}.pojo.vo.TokenResultVo;
import ${basePackageName}.service.IUploadService;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.stereotype.Service;

/**
 * @author codelee ${now}
 * @version ${version}
 */
@Service
public class QiniuUploadServiceImpl extends IUploadService {
    private final String ak = "${ak}";
    private final String sk = "${sk}";
    private final String bucket = "${bucket}";
    private final String host = "${host}";

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
        result.setProvider("QI_NIU");
        return result;
    }
}
