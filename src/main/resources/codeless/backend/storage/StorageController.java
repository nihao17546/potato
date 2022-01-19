package ${basePackageName}.controller;

import ${basePackageName}.pojo.vo.TokenResultVo;
import ${basePackageName}.service.IUploadService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author codeless ${now}
 * @version ${version}
 */
@Controller
public class StorageController {
    @Autowired
    private IUploadService uploadService;

    @RequestMapping(value = ${requestMapping}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public TokenResultVo token(@RequestParam(value = "file_name") String fileName) {
        return uploadService.getToken(fileName);
    }

}
