package ${basePackageName}.controller;

import ${basePackageName}.pojo.vo.TokenResultVo;
import ${basePackageName}.service.IUploadService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author codeless ${now}
 * @version ${version}
 */
@RestController
@RequestMapping(${requestMapping})
public class StorageController {
    @Autowired
    private IUploadService uploadService;

    @RequestMapping(value = "/token", produces = "application/json;charset=UTF-8")
    public TokenResultVo token(@RequestParam(value = "file_name") String fileName) {
        return uploadService.getToken(fileName);
    }

}
