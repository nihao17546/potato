package cn.thecover.potato.controller;

import cn.thecover.potato.generate.boot.BootResult;
import cn.thecover.potato.generate.boot.GenerateBoot;
import cn.thecover.potato.generate.constant.BootConstant;
import cn.thecover.potato.meta.conf.Config;
import cn.thecover.potato.model.param.GenerateParam;
import cn.thecover.potato.model.param.MetaParam;
import cn.thecover.potato.model.vo.*;
import cn.thecover.potato.service.IGenerateService;
import cn.thecover.potato.service.IMetaService;
import cn.thecover.potato.util.CommonUtil;
import cn.thecover.potato.util.GenerateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * @author nihao 2021/06/27
 */
@Slf4j
@RequestMapping("/meta")
public class MetaController {
    @Autowired
    private IMetaService metaService;
    @Autowired
    private IGenerateService generateService;
    @Autowired
    private GenerateBoot generateBoot;

    @PostMapping("/add")
    @ResponseBody
    public String add(@RequestBody MetaParam param) {
        metaService.add(param);
        return HttpResult.success().json();
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody MetaParam param) {
        metaService.updateInfo(param);
        return HttpResult.success().json();
    }

    @GetMapping("/list")
    @ResponseBody
    public String list(@RequestParam Integer page,
                       @RequestParam(name = "page_size") Integer pageSize,
                       @RequestParam(required = false) String name,
                       HttpServletRequest request) {
        ListVO<MetaVO> listVO = metaService.getPagination(page, pageSize, name, request);
        return HttpResult.success().pull(listVO).json();
    }

    @GetMapping("/delete")
    @ResponseBody
    public String delete(@RequestParam Integer id) {
        metaService.delete(id);
        return HttpResult.success().json();
    }

    @GetMapping("/boot")
    @ResponseBody
    public String boot(@RequestParam Integer id) {
        Config config = metaService.getConfig(id);
        BootResult bootResult = generateService.boot(id, config);
        generateBoot.boot(bootResult);
        return HttpResult.success().json();
    }

    @GetMapping("/unBoot")
    @ResponseBody
    public String unBoot(@RequestParam Integer id) {
        generateService.unBoot(id);
        return HttpResult.success().json();
    }

    @GetMapping("/getBootCode")
    @ResponseBody
    public String getBootCode(@RequestParam Integer id) {
        BootResult bootResult = generateBoot.getLoaded(id);
        if (bootResult == null) {
            return HttpResult.build(HttpStatus.NOT_FOUND).json();
        }

        BootCodeVo vo = new BootCodeVo();

        BootCodeVo.Label backend = new BootCodeVo.Label("backend");
        vo.addLabel(backend);
        BootCodeVo.Label src = new BootCodeVo.Label("src");
        backend.addChild(src);
        BootCodeVo.Label main = new BootCodeVo.Label("main");
        src.addChild(main);
        BootCodeVo.Label java = new BootCodeVo.Label("java");
        main.addChild(java);
        BootCodeVo.Label resources = new BootCodeVo.Label("resources");
        main.addChild(resources);

        for (BootResult.Mapper mapper : bootResult.getMappers()) {
            String simpleName = CommonUtil.getSimpleClassName(mapper.getMapperId());
            BootCodeVo.Label la = new BootCodeVo.Label(simpleName + "Mapper.xml", mapper.getSource());
            resources.addChild(la);
        }

        BootCodeVo.Label front = new BootCodeVo.Label("front");
        vo.addLabel(front);

        BootCodeVo.Label htmls = new BootCodeVo.Label("htmls");
        front.addChild(htmls);
        for (String key : bootResult.getHtml().keySet()) {
            BootResult.Html html = bootResult.getHtml().get(key);
            BootCodeVo.Label h  = new BootCodeVo.Label(
                    key.substring(key.lastIndexOf("/") + 1), html.getSource());
            htmls.addChild(h);
        }

        String basePackage = GenerateUtil.getWord(BootConstant.bootPackage + ".", id);
        BootCodeVo.Label basePackageLabel = new BootCodeVo.Label(basePackage);
        java.addChild(basePackageLabel);

        List<BootResult.Java> javas = bootResult.getAllJava();
        for (BootResult.Java javaSource : javas) {
            String packageName = CommonUtil.getPackageName(javaSource.getClassName());
            String pn = packageName.replaceFirst(basePackage + ".", "");
            String simpleClassName = CommonUtil.getSimpleClassName(javaSource.getClassName());
            if (pn.contains(".")) {
                String[] strings = pn.split("\\.");
                BootCodeVo.Label parent = null;
                for (String s : strings) {
                    if (parent == null) {
                        BootCodeVo.Label p = basePackageLabel.getChild(s);
                        if (p == null) {
                            p = new BootCodeVo.Label(s);
                            basePackageLabel.addChild(p);
                        }
                        parent = p;
                    } else {
                        BootCodeVo.Label p = parent.getChild(s);
                        if (p == null) {
                            p = new BootCodeVo.Label(s);
                            parent.addChild(p);
                        }
                        parent = p;
                    }
                }
                BootCodeVo.Label c = new BootCodeVo.Label(simpleClassName, javaSource.getSource());
                parent.addChild(c);
            } else {
                BootCodeVo.Label p = basePackageLabel.getChild(pn);
                if (p == null) {
                    p = new BootCodeVo.Label(pn);
                    basePackageLabel.addChild(p);
                }
                BootCodeVo.Label c = new BootCodeVo.Label(simpleClassName, javaSource.getSource());
                p.addChild(c);
            }
        }

        return HttpResult.success().pull(vo).json();
    }

    @PostMapping("/generate")
    @ResponseBody
    public void generate(@RequestBody GenerateParam param, HttpServletResponse response, HttpServletRequest request) {
        if (param.getPackageName() == null || param.getPackageName().isEmpty()
                || param.getEntityNames() == null || param.getEntityNames().isEmpty()) {
            CommonUtil.responseOutWithJson(response, HttpResult.build(HttpStatus.PARAM_ERROR));
            return;
        }
        Set<String> strings = new HashSet<>();
        for (GenerateParam.Entity entity : param.getEntityNames()) {
            if (strings.contains(entity.getClazz())) {
                CommonUtil.responseOutWithJson(response, HttpResult.build(HttpStatus.PARAM_ERROR));
                return;
            }
            strings.add(entity.getClazz());
        }
        Config config = metaService.getConfig(param.getId());
        Map<String,String> map = generateService.generate(param, config);
        Set<Map.Entry<String,String>> entries = map.entrySet();
        String fileName = config.getBasic().getName() + "-" + config.getBasic().getVersion() + ".rar";
        ServletOutputStream servletOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        try {
            // 先判断是什么浏览器
            String agent = request.getHeader("USER-AGENT");
            // 判断当前是什么浏览器
            if(agent.contains("Firefox")){
                // 采用BASE64编码
                fileName = base64EncodeFileName(fileName);
            }
            else{
                // IE GOOGLE
                fileName = URLEncoder.encode(fileName, "UTF-8");
                // URL编码会空格编成+
                fileName = fileName.replace("+", " ");
            }
            String mimeType = request.getSession().getServletContext().getMimeType(".rar");
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition","attachment;filename=" + fileName);
            servletOutputStream = response.getOutputStream();
            zipOutputStream = new ZipOutputStream(servletOutputStream);
            for (Map.Entry<String,String> entry : entries) {
                zipOutputStream.putNextEntry(new ZipEntry(entry.getKey()));
                zipOutputStream.write(entry.getValue().getBytes());
            }
        } catch (Exception e) {
            log.warn("{}", e);
        } finally {
            if (zipOutputStream != null) {
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    log.warn("{}", e);
                }
            }
            if (servletOutputStream != null) {
                try {
                    servletOutputStream.close();
                } catch (IOException e) {
                    log.warn("{}", e);
                }
            }
        }
    }

    private String base64EncodeFileName(String fileName) {
        try {
            return "=?UTF-8?B?"
                    + new String(Base64.encodeBase64String(fileName.getBytes("UTF-8"))) + "?=";
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
