package ${basePackageName}.service;

import ${basePackageName}.pojo.vo.TokenResultVo;

import java.util.UUID;

/**
 * @author codelee ${now}
 * @version ${version}
 */
public abstract class IUploadService {
    public abstract TokenResultVo getToken(String fileName);

    protected String getSaveKey(String string) {
        String fileName = "potato/" + UUID.randomUUID().toString().replaceAll("-","");
        if (string != null) {
            if (string.contains(".")) {
                fileName = fileName + string.substring(string.lastIndexOf("."));
            } else {
                fileName = fileName + string;
            }
        }
        return fileName;
    }
}
