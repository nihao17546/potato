package com.appcnd.potato.generate.executor;

import com.appcnd.potato.exception.ExceptionAssert;
import com.appcnd.potato.generate.constant.BootConstant;
import com.appcnd.potato.generate.context.FrontContext;
import com.appcnd.potato.generate.context.FrontSearchElementContext;
import com.appcnd.potato.generate.context.RemoteContext;
import com.appcnd.potato.meta.conf.form.operate.Rule;
import com.appcnd.potato.meta.conf.form.operate.elements.ImageElement;
import com.appcnd.potato.meta.conf.form.operate.elements.MarkdownElement;
import com.appcnd.potato.meta.conf.form.operate.elements.OperateElement;
import com.appcnd.potato.meta.conf.form.operate.elements.RichTinymceElement;
import com.appcnd.potato.meta.conf.form.search.element.DateTimeRangeSearchElement;
import com.appcnd.potato.meta.conf.form.storage.HuaweiStorage;
import com.appcnd.potato.meta.conf.form.storage.QiniuStorage;
import com.appcnd.potato.meta.conf.table.UIColumn;
import com.appcnd.potato.model.vo.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nihao 2021/07/24
 */
public class BootFrontExecutor extends FrontExecutor {
    private final String FILE_PATH = "codeless/front/boot.html";

    public BootFrontExecutor(FrontContext context) {
        super(context);
    }

    @Override
    protected Map<String, Map<String, String>> analysis() {
        Map<String, Map<String, String>> result = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        result.put(context.getPath(), map);
        map.put("title", context.getTitle());

        String formString = null;
        if (context.getForeignKey() != null) {
            String foreignKey = context.getPropMap().get(context.getTable() + "_" + context.getForeignKey());
            formString = "{" + foreignKey + ": this." + foreignKey + "}";
        } else {
            formString = "{}";
        }


        StringBuilder vueUseBuilder = new StringBuilder();
        StringBuilder cssBuilder = new StringBuilder();
        StringBuilder jsBuilder = new StringBuilder();
        StringBuilder datasBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        StringBuilder methodsBuilder = new StringBuilder();
        StringBuilder searchHtmlBuilder = new StringBuilder();
        StringBuilder tableHtmlBuilder = new StringBuilder();
        StringBuilder paginationHtmlBuilder = new StringBuilder();
        StringBuilder extraHtmlBuilder = new StringBuilder();
        StringBuilder searchParamsBuilder = new StringBuilder();
        StringBuilder optionButtonHtmlBuilder = new StringBuilder();
        StringBuilder backHtmlBuidler = new StringBuilder();

        boolean hasUploadImage = false;
        boolean hasCutImage = false;
        boolean hasMarkdown = false;
        boolean hasMarkdownUseStorage = false;
        boolean hasTinymceElement = false;
        List<String> tinymceInitMethods = new ArrayList<>();
        List<String> tinymceEditorNames = new ArrayList<>();


        if (context.getForeignKeyProp() != null) {
            // ????????????????????????????????????????????????
            datasBuilder.append("                ").append(context.getForeignKeyProp()).append(": null,\n");
            searchParamsBuilder.append("                        ")
                    .append(context.getForeignKeyProp()).append(": this.").append(context.getForeignKeyProp()).append(",\n");
            createBuilder
                    .append("            let ").append(context.getForeignKeyProp()).append(" = getParam(\"").append(context.getForeignKeyProp()).append("\");\n")
                    .append("            if (").append(context.getForeignKeyProp()).append(" != '' && ").append(context.getForeignKeyProp()).append(" != null && typeof (").append(context.getForeignKeyProp()).append(") != 'undefined') {\n")
                    .append("                this.").append(context.getForeignKeyProp()).append(" = ").append(context.getForeignKeyProp()).append(";\n")
                    .append("            } else {\n")
                    .append("                this.loading = true;\n")
                    .append("                alert('????????????');\n")
                    .append("                return;\n")
                    .append("            }\n");
            backHtmlBuidler
                    .append("            <el-link href=\"").append("${contextPath}").append("${potatoPath}").append(context.getParentPath()).append("\"><i class=\"el-icon-back\"></i></el-link>\n")
                    .append("            <el-divider direction=\"vertical\"></el-divider>");
        }

        if (context.getRemoteContexts() != null) {
            for (RemoteContext remoteContext : context.getRemoteContexts()) {
                String methodName = "getRemote" + remoteContext.hashCode();
                methodName = methodName.replaceAll("-", "A");
                String optionsName = "data" + remoteContext.hashCode();
                optionsName = optionsName.replaceAll("-", "A");
                datasBuilder.append("                ").append(optionsName).append(": [],\n");
                String requestUrl = BootConstant.requestPrefix + methodName;
                methodsBuilder
                        .append("            ").append(methodName).append("() {\n")
                        .append("                axios.get('").append("${contextPath}").append(requestUrl).append("',{\n")
                        .append("                    params: {}\n")
                        .append("                }).then(res => {\n")
                        .append("                    this.").append(optionsName).append(" = res.data\n")
                        .append("                }).catch(res => {\n")
                        .append("                    console.error(res)\n")
                        .append("                })\n")
                        .append("            },\n");
                createBuilder.append("            this.").append(methodName).append("();\n");
            }
        }

        if (context.getSearchElements() != null) {
            searchHtmlBuilder
                    .append("            <el-col :span=\"24\">\n")
                    .append("                <el-card shadow=\"never\" class=\"c-card\">\n");
            searchHtmlBuilder
                    .append("                <div slot=\"header\">\n")
                    .append("                    <span>????????????</span>\n")
                    .append("                </div>\n");
            searchHtmlBuilder.append("                <el-form @submit.native.prevent :inline=\"true\">\n");
            for (FrontSearchElementContext searchElement : context.getSearchElements()) {
                if (context.getForeignKeyProp() != null && context.getForeignKeyProp().equals(searchElement.getField())) {
                    // ????????????????????????
                    continue;
                }
                searchHtmlBuilder.append(searchElement.getElement().getOptions(searchElement.getField()));
                if (searchElement.getField() != null) {
                    if (searchElement.getStartField() == null && searchElement.getEndField() == null) {
                        searchParamsBuilder.append("                        ")
                                .append(searchElement.getField()).append(": this.").append(searchElement.getField()).append(",\n");
                    }
                    datasBuilder.append("                ").append(searchElement.getField()).append(": null,\n");
                }
                if (searchElement.getStartField() != null) {
                    searchParamsBuilder.append("                        ")
                            .append(searchElement.getStartField()).append(": this.").append(searchElement.getStartField()).append(",\n");
                    datasBuilder.append("                ").append(searchElement.getStartField()).append(": null,\n");
                }
                if (searchElement.getEndField() != null) {
                    searchParamsBuilder.append("                        ")
                            .append(searchElement.getEndField()).append(": this.").append(searchElement.getEndField()).append(",\n");
                    datasBuilder.append("                ").append(searchElement.getEndField()).append(": null,\n");
                }

                if (searchElement.getElement() instanceof DateTimeRangeSearchElement) {
                    String methodName = searchElement.getField() + "Change";
                    methodsBuilder
                            .append("            ").append(methodName).append("() {\n")
                            .append("                this.").append(searchElement.getStartField()).append(" = null;\n")
                            .append("                this.").append(searchElement.getEndField()).append(" = null;\n")
                            .append("                if (this.").append(searchElement.getField()).append(" && Array.isArray(this.").append(searchElement.getField()).append(")) {\n")
                            .append("                    if (this.").append(searchElement.getField()).append(".length == 1) {\n")
                            .append("                        this.").append(searchElement.getStartField()).append(" = this.").append(searchElement.getField()).append("[0];\n")
                            .append("                    } else if (this.").append(searchElement.getField()).append(".length > 1) {\n")
                            .append("                        this.").append(searchElement.getStartField()).append(" = this.").append(searchElement.getField()).append("[0];\n")
                            .append("                        this.").append(searchElement.getEndField()).append(" = this.").append(searchElement.getField()).append("[1];\n")
                            .append("                    }\n")
                            .append("                }\n")
                            .append("                this.search();\n")
                            .append("            },\n");
                }
            }
            searchHtmlBuilder
                    .append("                    <el-form-item>\n")
                    .append("                        <el-button type=\"text\" @click=\"search\">??????</el-button>\n")
                    .append("                    </el-form-item>\n")
                    .append("                </el-form>\n");
            searchHtmlBuilder
                    .append("                </el-card>\n")
                    .append("            </el-col>\n");
            methodsBuilder
                    .append("            search() {\n")
                    .append("                if (typeof this.curPage != 'undefined') {\n")
                    .append("                    this.curPage = 1;\n")
                    .append("                }\n")
                    .append("                this.getList()\n")
                    .append("            },\n");
        }

        tableHtmlBuilder.append("                <el-table\n")
                .append("                        :data=\"list\"\n")
                .append("                        border\n")
                .append("                        @sort-change=\"sortChange\"\n")
                .append("                        style=\"width: 100%; margin-top: 3px;\">\n");
        for (UIColumn uiColumn : context.getUiTable().getColumns()) {
            String prop = context.getProp(uiColumn);
            String label = uiColumn.getLabel();
            Integer width = uiColumn.getWidth();
            Boolean sortable = uiColumn.getSortable();
            tableHtmlBuilder
                    .append("        <el-table-column\n")
                    .append("                label=\"").append(label).append("\"\n");
            if (width != null) {
                tableHtmlBuilder.append("                width=\"").append(width).append("\"\n");
            }
            if (Boolean.TRUE.equals(sortable)) {
                tableHtmlBuilder.append("                sortable\n");
            }
            tableHtmlBuilder.append("                prop=\"").append(prop).append("\">\n");
            if (uiColumn.getFormatter() != null && !uiColumn.getFormatter().isEmpty()) {
                String formatterMethodName = prop + "Formatter";

                tableHtmlBuilder.append("            <template slot-scope=\"props\">\n");
                tableHtmlBuilder.append("                <span v-html=\"").append(formatterMethodName).append("(props.row, '").append(prop).append("')\"></span>\n");
                tableHtmlBuilder.append("            </template>\n");

                methodsBuilder
                        .append("            ").append(formatterMethodName).append("(row, prop) {\n")
                        .append("                ").append(uiColumn.getFormatter()).append("\n")
                        .append("            },\n");
            }
            tableHtmlBuilder.append("        </el-table-column>\n");
        }

        StringBuilder optionColumnBuilder = new StringBuilder();
        if (context.getOperateContext() != null) {
            datasBuilder.append("                form: ").append(formString).append(",\n");
            datasBuilder.append("                formVisible: false,\n");
            datasBuilder.append("                formTitle: '',\n");

            StringBuilder pks = new StringBuilder();
            for (String pk : context.getOperateContext().getPrimaryKeys()) {
                if (pks.length() > 0) {
                    pks.append(",");
                }
                pks.append("'").append(pk).append("'");
            }

            extraHtmlBuilder
                    .append("    <el-dialog :title=\"formTitle\" :visible.sync=\"formVisible\" class=\"group-dialog\" :close-on-press-escape=\"false\" :close-on-click-modal=\"false\" :before-close=\"closeInfo\">\n")
                    .append("        <el-form :model=\"form\" :rules=\"rules\" ref=\"form\" size=\"small\">\n");
            StringBuilder formBuilder = new StringBuilder();
            StringBuilder ruleBuilder = new StringBuilder();
            for (OperateElement element : context.getOperateContext().getElements()) {
                if (element instanceof ImageElement) {
                    hasUploadImage = true;
                    ImageElement imageElement = (ImageElement) element;
                    if (Boolean.TRUE.equals(imageElement.getCut())) {
                        hasCutImage = true;
                    }
                } else if (element instanceof MarkdownElement) {
                    hasMarkdown = true;
                    MarkdownElement markdownElement = (MarkdownElement) element;
                    if (Boolean.TRUE.equals(markdownElement.getUploadImage())) {
                        if (context.getStorage() == null) {
                            ExceptionAssert.isTrue(context.getStorage() == null)
                                    .throwException(HttpStatus.PARAM_ERROR.getCode(), "Markdown?????????????????????????????????");
                        }
                        hasMarkdownUseStorage = true;
                    }
                } else if (element instanceof RichTinymceElement) {
                    RichTinymceElement richTinymceElement = (RichTinymceElement) element;
                    hasTinymceElement = true;
                    String initMethod = "initTinymce" + element.getFieldName();
                    tinymceInitMethods.add(initMethod + "(this.form." + element.getFieldName() + ");");
                    String editorName = "tinymceEditor" + element.getFieldName();
                    tinymceEditorNames.add(editorName);
                    datasBuilder.append("                ").append(editorName).append(": null,\n");
                    methodsBuilder
                            .append("            ").append(initMethod).append("(val) {\n")
                            .append("                let _this = this;\n")
                            .append("                tinymce.init({\n")
                            .append("                    selector: '#tinymce").append(element.getFieldName()).append("',\n")
                            .append("                    language: 'zh_CN',\n")
                            .append("                    height: 350,\n")
                            .append("                    base_url: '${contextPath}${potatoPath}/static/tinymce',\n")
                            .append("                    branding: false,\n")
                            .append("                    plugins: [\n")
                            .append("                        'table advlist autolink lists link charmap print preview hr anchor pagebreak',\n")
                            .append("                        'searchreplace wordcount visualblocks visualchars code fullscreen',\n")
                            .append("                        'insertdatetime nonbreaking save table contextmenu directionality',\n")
                            .append("                        'emoticons textcolor colorpicker textpattern image code codesample toc pagebreak media'\n")
                            .append("                    ],\n")
                            .append("                    toolbar1: 'undo redo | table | insert | styleselect | bold italic | alignleft aligncenter alignright alignjustify lineheight| bullist numlist outdent indent | link image media | fontsizeselect | fontselect | forecolor backcolor emoticons | codesample | pagebreak | toc | print preview | code fullscreen',\n")
                            .append("                    fontsize_formats: '8pt 10pt 12pt 14pt 18pt 24pt 36pt',\n")
                            .append("                    image_advtab: true,\n")
                            .append("                    file_picker_types: 'media',\n")
                            .append("                    paste_data_images: true,\n")
                            .append("                    menubar: false,//???????????????\n")
                            .append("                    automatic_uploads: true,\n")
                            .append("                    media_live_embeds: true,//?????????????????????\n");
                    if (Boolean.FALSE.equals(richTinymceElement.getCanEdit())) {
                        methodsBuilder.append("                    readonly: true,\n");
                    }
                    if (richTinymceElement.getPlaceholder() != null) {
                        methodsBuilder.append("                    placeholder: \"").append(richTinymceElement.getPlaceholder()).append("\",\n");
                    }
                    // ????????????
                    if (context.getStorage() != null) {
                        methodsBuilder
                                .append("                    images_upload_handler: (blobInfo, succFun, failFun) => {\n")
                                .append("                        let file = blobInfo.blob();\n")
                                .append("                        axios.get('").append("${contextPath}").append(context.getHttpRequest()).append(context.getTokenRequest()).append("',{\n")
                                .append("                            params: {\n")
                                .append("                                file_name: file.name\n")
                                .append("                            }\n")
                                .append("                        }).then(res => {\n")
                                .append("                            if (typeof res.data.token != 'undefined') {\n");
                        if (context.getStorage() instanceof QiniuStorage) {
                            methodsBuilder
                                    .append("                                let observable = qiniu.upload(file, res.data.key, res.data.token, {}, {\n")
                                    .append("                                    useCdnDomain: true\n")
                                    .append("                                });\n")
                                    .append("                                observable.subscribe({\n")
                                    .append("                                    next: (result) => {\n")
                                    .append("                                        console.log(result);\n")
                                    .append("                                    },\n")
                                    .append("                                    error: () => {\n")
                                    .append("                                        failFun('??????????????????');\n")
                                    .append("                                    },\n")
                                    .append("                                    complete: (successRes) => {\n")
                                    .append("                                        let url = res.data.host + '/' + successRes.key;\n")
                                    .append("                                        succFun(url)\n")
                                    .append("                                    }\n")
                                    .append("                                })\n");
                        } else if (context.getStorage() instanceof HuaweiStorage) {
                            methodsBuilder
                                    .append("                                let obsClient = new ObsClient({\n")
                                    .append("                                    access_key_id: res.data.access,\n")
                                    .append("                                    secret_access_key: res.data.secret,\n")
                                    .append("                                    server : res.data.endpoint,\n")
                                    .append("                                    security_token: res.data.token,\n")
                                    .append("                                    timeout: 24 * 60 * 60\n")
                                    .append("                                });\n")
                                    .append("                                obsClient.putObject({\n")
                                    .append("                                    Bucket: res.data.bucket,\n")
                                    .append("                                    Key: res.data.key,\n")
                                    .append("                                    SourceFile: file,\n")
                                    .append("                                    ProgressCallback: (transferredAmount, totalAmount, totalSeconds) => {\n")
                                    .append("                                        console.log('????????????: ' + transferredAmount * 1.0 / totalSeconds / 1024 + ' KB/S');\n")
                                    .append("                                        console.log(transferredAmount * 100.0 / totalAmount);\n")
                                    .append("                                    }\n")
                                    .append("                                }, (err, result) => {\n")
                                    .append("                                    console.log(result)\n")
                                    .append("                                    if (result && result.CommonMsg && result.CommonMsg.Status == 200) {\n")
                                    .append("                                        let url = res.data.host + '/' + res.data.key;\n")
                                    .append("                                        succFun(url)\n")
                                    .append("                                    } else {\n")
                                    .append("                                        failFun('??????????????????');\n")
                                    .append("                                        console.error(err);\n")
                                    .append("                                    }\n")
                                    .append("                                });\n");
                        }
                        methodsBuilder
                                .append("                            } else {\n")
                                .append("                                console.error(res)\n")
                                .append("                                failFun('??????token??????');\n")
                                .append("                            }\n")
                                .append("                        }).catch(res => {\n")
                                .append("                            console.error(res)\n")
                                .append("                            failFun('??????token??????');\n")
                                .append("                        })\n")
                                .append("                    },\n");
                    }
                    // ????????????
                    if (context.getStorage() != null) {
                        methodsBuilder
                                .append("                    file_picker_callback: (callback, value, meta) => {\n")
                                .append("                        if (meta.filetype == 'media') {\n")
                                .append("                            let input = document.createElement('input');\n")
                                .append("                            input.setAttribute('type', 'file');\n")
                                .append("                            input.setAttribute('accept', 'video/*');\n")
                                .append("                            input.click();\n")
                                .append("                            input.onchange = function() {\n")
                                .append("                                let file = this.files[0];\n")
                                .append("                                if (file) {\n")
                                .append("                                    axios.get('").append("${contextPath}").append(context.getHttpRequest()).append(context.getTokenRequest()).append("',{\n")
                                .append("                                        params: {\n")
                                .append("                                            file_name: file.name\n")
                                .append("                                        }\n")
                                .append("                                    }).then(res => {\n")
                                .append("                                        if (typeof res.data.token != 'undefined') {\n");
                        if (context.getStorage() instanceof QiniuStorage) {
                            methodsBuilder
                                    .append("                                            let observable = qiniu.upload(file, res.data.key, res.data.token, {}, {\n")
                                    .append("                                                useCdnDomain: true\n")
                                    .append("                                            });\n")
                                    .append("                                            observable.subscribe({\n")
                                    .append("                                                next: (result) => {\n")
                                    .append("                                                    console.log(result);\n")
                                    .append("                                                },\n")
                                    .append("                                                error: () => {\n")
                                    .append("                                                    _this.$message.error('??????????????????');\n")
                                    .append("                                                },\n")
                                    .append("                                                complete: (successRes) => {\n")
                                    .append("                                                    let url = res.data.host + '/' + successRes.key;\n")
                                    .append("                                                    callback(url, { title: file.name });\n")
                                    .append("                                                }\n")
                                    .append("                                            })\n");
                        } else if (context.getStorage() instanceof HuaweiStorage) {
                            methodsBuilder
                                    .append("                                            let obsClient = new ObsClient({\n")
                                    .append("                                                access_key_id: res.data.access,\n")
                                    .append("                                                secret_access_key: res.data.secret,\n")
                                    .append("                                                server : res.data.endpoint,\n")
                                    .append("                                                security_token: res.data.token,\n")
                                    .append("                                                timeout: 24 * 60 * 60\n")
                                    .append("                                            });\n")
                                    .append("                                            obsClient.putObject({\n")
                                    .append("                                                Bucket: res.data.bucket,\n")
                                    .append("                                                Key: res.data.key,\n")
                                    .append("                                                SourceFile: file,\n")
                                    .append("                                                ProgressCallback: (transferredAmount, totalAmount, totalSeconds) => {\n")
                                    .append("                                                    console.log('????????????: ' + transferredAmount * 1.0 / totalSeconds / 1024 + ' KB/S');\n")
                                    .append("                                                    console.log(transferredAmount * 100.0 / totalAmount);\n")
                                    .append("                                                }\n")
                                    .append("                                            }, (err, result) => {\n")
                                    .append("                                                console.log(result)\n")
                                    .append("                                                if (result && result.CommonMsg && result.CommonMsg.Status == 200) {\n")
                                    .append("                                                    let url = res.data.host + '/' + res.data.key;\n")
                                    .append("                                                    callback(url, { title: file.name });\n")
                                    .append("                                                } else {\n")
                                    .append("                                                    _this.$message.error('??????????????????');\n")
                                    .append("                                                    console.error(err);\n")
                                    .append("                                                }\n")
                                    .append("                                            });\n");
                        }
                        methodsBuilder
                                .append("                                        } else {\n")
                                .append("                                            console.error(res)\n")
                                .append("                                            _this.$message.error('??????token??????');\n")
                                .append("                                        }\n")
                                .append("                                    }).catch(res => {\n")
                                .append("                                        console.error(res)\n")
                                .append("                                        _this.$message.error('??????token??????');\n")
                                .append("                                    })\n")
                                .append("                                }\n");
                        methodsBuilder
                                .append("                            }\n")
                                .append("                        }\n")
                                .append("                    },\n");
                    }
                    methodsBuilder
                            .append("                    setup: function(editor) {\n")
                            .append("                        editor.on('change', (e) => {\n")
                            .append("                            _this.$set(_this.form, '").append(element.getFieldName()).append("', e.level.content);\n")
                            .append("                        });\n")
                            .append("                    },\n");
                    methodsBuilder
                            .append("                    init_instance_callback: editor => {\n")
                            .append("                        if (typeof val != 'undefined' && val != null) {\n")
                            .append("                            editor.setContent(val);\n")
                            .append("                        }\n")
                            .append("                        _this.").append(editorName).append(" = editor\n")
                            .append("                    }\n")
                            .append("                })\n")
                            .append("            },\n");
                }
                formBuilder.append(element.getHtml());
                Rule rule = element.getRule();
                if (rule != null) {
                    ruleBuilder
                            .append("                    ")
                            .append(element.getFieldName()).append(": [{required : ").append(rule.getRequired())
                            .append(", trigger: 'change', message: '");
                    if (rule.getMessage() != null && !rule.getMessage().isEmpty()) {
                        ruleBuilder.append(rule.getMessage());
                    } else {
                        if (rule.getRequired()) {
                            ruleBuilder.append("?????????");
                        } else {
                            ruleBuilder.append("????????????");
                        }
                    }
                    ruleBuilder.append("'");
                    if (rule.getRegular() != null) {
                        ruleBuilder.append(", validator: function (rule, value, callback) {\n")
                                .append("                            if (typeof value == 'undefined' || value == null || value == '') {\n")
                                .append("                                if (rule.required) {\n")
                                .append("                                    callback(new Error(rule.message));\n")
                                .append("                                } else {\n")
                                .append("                                    callback();\n")
                                .append("                                }\n")
                                .append("                            } else {\n")
                                .append("                                if((/").append(rule.getRegular()).append("/.test(value))){\n")
                                .append("                                    callback();\n")
                                .append("                                } else {\n")
                                .append("                                    callback(new Error(rule.message));\n")
                                .append("                                }\n")
                                .append("                            }\n")
                                .append("                        }");
                    }
                    ruleBuilder.append(" }],\n");
                }
            }
            if (ruleBuilder.length() > 0) {
                datasBuilder
                        .append("                rules: {\n")
                        .append(ruleBuilder.toString())
                        .append("                },\n");
            } else {
                datasBuilder.append("                rules: {},");
            }
            formBuilder
                    .append("            <el-form-item style=\"text-align: right\">\n")
                    .append("                <el-button @click=\"closeInfo\" size=\"small\" :disabled=\"loading\">??? ???</el-button>\n")
                    .append("                <el-button type=\"primary\" @click=\"submit('form')\" size=\"small\" :disabled=\"loading\">??? ???</el-button>\n")
                    .append("            </el-form-item>\n");
            extraHtmlBuilder.append(formBuilder.toString());
            extraHtmlBuilder
                    .append("        </el-form>\n")
                    .append("    </el-dialog>\n");
            methodsBuilder
                    .append("            submit(formName) {\n")
                    .append("                this.loading = true;\n")
                    .append("                this.$refs[formName].validate((valid) => {\n")
                    .append("                    if (valid) {\n")
                    .append("                        let url = '").append("${contextPath}").append(context.getHttpRequest()).append(context.getSaveRequest()).append("';\n")
                    .append("                        if ( this.formTitle == '??????') {\n")
                    .append("                            url = '").append("${contextPath}").append(context.getHttpRequest()).append(context.getUpdateRequest()).append("'\n")
                    .append("                        }\n")
                    .append("                        axios.post(url, this.form).then(res => {\n")
                    .append("                            this.loading = false;\n")
                    .append("                            if (res.data.").append(context.getResponseVoSetting().getStatusKey()).append(" != ").append(context.getResponseVoSetting().getSuccessValue()).append(") {\n")
                    .append("                                this.$message.error(res.data.").append(context.getResponseVoSetting().getMessageKey()).append(");\n")
                    .append("                            } else {\n")
                    .append("                                this.$message.success('????????????');\n")
                    .append("                                this.closeInfo()\n")
                    .append("                                this.getList()\n")
                    .append("                            }\n")
                    .append("                        }).catch(res => {\n")
                    .append("                            console.error(res)\n")
                    .append("                            this.loading = false;\n")
                    .append("                        })\n")
                    .append("                    } else {\n")
                    .append("                        this.loading = false;\n")
                    .append("                        return false;\n")
                    .append("                    }\n")
                    .append("                });\n")
                    .append("            },\n");

            if (Boolean.TRUE.equals(context.getOperateContext().getInsert())) {
                optionButtonHtmlBuilder
                        .append("                <div slot=\"header\">\n")
                        .append("                    <el-button size=\"medium\" type=\"primary\" @click=\"showAdd\">??????</el-button>\n")
                        .append("                </div>\n");
                methodsBuilder
                        .append("            showAdd() {\n")
                        .append("                this.form = ").append(formString).append("\n")
                        .append("                this.formVisible = true\n")
                        .append("                this.formTitle = '??????'\n");
                if (!tinymceInitMethods.isEmpty()) {
                    methodsBuilder.append("                this.$nextTick(() => {\n");
                    for (String tinymceInitMethod : tinymceInitMethods) {
                        methodsBuilder.append("                    this.").append(tinymceInitMethod).append("\n");
                    }
                    methodsBuilder.append("                })\n");
                }
                methodsBuilder
                        .append("            },\n");
            }

            if (Boolean.TRUE.equals(context.getOperateContext().getUpdate())) {
                optionColumnBuilder.append("                    ")
                        .append("<el-button type=\"primary\" size=\"mini\" :disabled=\"loading\" @click=\"showInfo(props.row,[");
                optionColumnBuilder.append(pks.toString());
                optionColumnBuilder.append("])\">??????")
                        .append("</el-button>\n");
                methodsBuilder
                        .append("            showInfo(row, pks) {\n")
                        .append("                let param = {}\n")
                        .append("                for(let i = 0; i < pks.length; i ++) {\n")
                        .append("                    param[pks[i]] = row[pks[i]]\n")
                        .append("                }\n")
                        .append("                axios.get('").append("${contextPath}").append(context.getHttpRequest()).append(context.getInfoRequest()).append("',{\n")
                        .append("                    params: param\n")
                        .append("                }).then(res => {\n")
                        .append("                    if (res.data.").append(context.getResponseVoSetting().getStatusKey()).append(" == ").append(context.getResponseVoSetting().getSuccessValue()).append(") {\n")
                        .append("                        this.form = res.data.").append(context.getResponseVoSetting().getContentKey()).append("\n")
                        .append("                        this.formVisible = true\n")
                        .append("                        this.formTitle = '??????'\n");
                if (!tinymceInitMethods.isEmpty()) {
                    methodsBuilder.append("                        this.$nextTick(() => {\n");
                    for (String tinymceInitMethod : tinymceInitMethods) {
                        methodsBuilder.append("                            this.").append(tinymceInitMethod).append("\n");
                    }
                    methodsBuilder.append("                        })\n");
                }
                methodsBuilder
                        .append("                    } else {\n")
                        .append("                        if (res.data.").append(context.getResponseVoSetting().getMessageKey()).append(") {\n")
                        .append("                            this.$message.error(res.data.").append(context.getResponseVoSetting().getMessageKey()).append(");\n")
                        .append("                        } else {\n")
                        .append("                            this.$message.error('????????????');\n")
                        .append("                        }\n")
                        .append("                    }\n")
                        .append("                }).catch(res => {\n")
                        .append("                    console.error(res)\n")
                        .append("                    this.loading = false;\n")
                        .append("                    this.$message.error('????????????');\n")
                        .append("                })\n")
                        .append("            },\n");
            }

            if (Boolean.TRUE.equals(context.getOperateContext().getDelete())) {
                optionColumnBuilder.append("                    ")
                        .append("<el-button type=\"danger\" size=\"mini\" :disabled=\"loading\" @click=\"del(props.row,[");
                optionColumnBuilder.append(pks.toString());
                optionColumnBuilder.append("])\">??????")
                        .append("</el-button>\n");
                methodsBuilder
                        .append("            del(row,pks) {\n")
                        .append("                this.$confirm('????????????????', '??????', {\n")
                        .append("                    confirmButtonText: '??????',\n")
                        .append("                    cancelButtonText: '??????',\n")
                        .append("                    type: 'warning'\n")
                        .append("                }).then(() => {\n")
                        .append("                    let param = {}\n")
                        .append("                    for(let i = 0; i < pks.length; i ++) {\n")
                        .append("                        param[pks[i]] = row[pks[i]]\n")
                        .append("                    }\n")
                        .append("                    this.loading = true;\n")
                        .append("                    axios.get('").append("${contextPath}").append(context.getHttpRequest()).append(context.getDeleteRequest()).append("',{\n")
                        .append("                        params: param\n")
                        .append("                    }).then(res => {\n")
                        .append("                        this.loading = false;\n")
                        .append("                        if (res.data.").append(context.getResponseVoSetting().getStatusKey()).append(" != ").append(context.getResponseVoSetting().getSuccessValue()).append(") {\n")
                        .append("                            this.$message.error(res.data.").append(context.getResponseVoSetting().getMessageKey()).append(");\n")
                        .append("                        } else {\n")
                        .append("                            this.$message.success('????????????');\n")
                        .append("                            this.getList()\n")
                        .append("                        }\n")
                        .append("                    }).catch(res => {\n")
                        .append("                        console.error(res)\n")
                        .append("                        this.loading = false;\n")
                        .append("                        this.$message.error('????????????');\n")
                        .append("                    })\n")
                        .append("                }).catch(() => {\n")
                        .append("                });\n")
                        .append("            },\n");
            }

            methodsBuilder
                    .append("            closeInfo() {\n");
            if (!tinymceEditorNames.isEmpty()) {
                for (String editor : tinymceEditorNames) {
                    methodsBuilder
                            .append("                if (this.").append(editor).append(" != null) {\n")
                            .append("                    this.").append(editor).append(".destroy();\n")
                            .append("                    this.").append(editor).append(" = null;\n")
                            .append("                }\n");
                }
            }
            methodsBuilder
                    .append("                this.formVisible = false\n")
                    .append("                this.formTitle = ''\n")
                    .append("                this.form = ").append(formString).append("\n")
                    .append("                this.$refs.form.resetFields()\n")
                    .append("            },\n");
        }

        if (!CollectionUtils.isEmpty(context.getFollows())) {
            for (FrontContext follow : context.getFollows()) {
                optionColumnBuilder.append("                    ")
                        .append("<el-button type=\"primary\" size=\"mini\" :disabled=\"loading\" @click=\"showFollow(props.row, ")
                        .append("'").append(follow.getForeignKeyProp()).append("'").append(", '")
                        .append(follow.getParentKeyProp(context.getPropMap())).append("', '")
                        .append("${potatoPath}").append(follow.getPath()).append("')\">")
                        .append(follow.getTitle())
                        .append("</el-button>\n");
            }
            methodsBuilder
                    .append("            showFollow(row, fk, pk, path) {\n")
                    .append("                let v = row[pk];\n")
                    .append("                window.location.href = '" + "${contextPath}" + "' + path + '?' + fk + '=' + v;\n")
                    .append("            },\n");
        }

        if (optionColumnBuilder.length() > 0) {
            tableHtmlBuilder
                    .append("        <el-table-column\n");
            if (context.getUiTable().getOptionColumnWidth() != null) {
                tableHtmlBuilder.append("                width=\"").append(context.getUiTable().getOptionColumnWidth()).append("\"\n");
            }
            tableHtmlBuilder.append("                label=\"").append("??????").append("\">\n");
            tableHtmlBuilder.append("            <template slot-scope=\"props\">\n");
            tableHtmlBuilder.append("                <el-button-group>\n");
            tableHtmlBuilder.append(optionColumnBuilder.toString());
            tableHtmlBuilder.append("                </el-button-group>\n");
            tableHtmlBuilder.append("            </template>\n");
            tableHtmlBuilder.append("        </el-table-column>\n");
        }
        tableHtmlBuilder.append("                </el-table>\n");


        String getListUrl = "${contextPath}" + context.getHttpRequest() + context.getListRequest();

        datasBuilder.append("                list: [],\n");
        datasBuilder.append("                sort: null,\n");
        datasBuilder.append("                order: null,\n");
        if (context.getUiTable().getPagination()) {
            datasBuilder
                    .append("                totalCount: 0,\n")
                    .append("                pageSize: 10,\n")
                    .append("                curPage: 1,");

            methodsBuilder
                    .append("            sizeChange(size) {\n")
                    .append("                this.pageSize = size;\n")
                    .append("                this.curPage = 1;\n")
                    .append("                this.getList()\n")
                    .append("            },\n")
                    .append("            currentChange(currentPage) {\n")
                    .append("                this.curPage = currentPage;\n")
                    .append("                this.getList()\n")
                    .append("            },\n")
                    .append("            getList() {\n")
                    .append("                this.loading = true;\n")
                    .append("                let param = {\n")
                    .append("                    sort: this.sort,\n")
                    .append("                    order: this.order,\n")
                    .append("                    curPage: this.curPage,\n")
                    .append("                    pageSize: this.pageSize,\n")
                    .append(searchParamsBuilder.toString())
                    .append("                }\n")
                    .append("                let url = '").append(getListUrl).append("?' + parseParam(param)\n")
                    .append("                axios.get(url).then(res => {\n")
                    .append("                    this.list = res.data.list;\n")
                    .append("                    this.totalCount = res.data.count;\n")
                    .append("                    this.loading = false;\n")
                    .append("                }).catch(res => {\n")
                    .append("                    console.error(res)\n")
                    .append("                    this.loading = false;\n")
                    .append("                })\n")
                    .append("            },\n");


            paginationHtmlBuilder
                    .append("    <div style=\"text-align: right;\">\n")
                    .append("        <el-pagination\n")
                    .append("                small\n")
                    .append("                background\n")
                    .append("                layout=\"total, sizes, prev, pager, next, jumper\"\n")
                    .append("                :total=\"totalCount\"\n")
                    .append("                :page-size=\"pageSize\"\n")
                    .append("                :current-page=\"curPage\"\n")
                    .append("                @current-change=\"currentChange\"\n")
                    .append("                @size-change=\"sizeChange\">\n")
                    .append("        </el-pagination>\n")
                    .append("    </div>\n");
        } else {
            methodsBuilder
                    .append("            getList() {\n")
                    .append("                this.loading = true;\n")
                    .append("                let param = {\n")
                    .append("                    sort: this.sort,\n")
                    .append("                    order: this.order,\n")
                    .append("                    curPage: this.curPage,\n")
                    .append("                    pageSize: this.pageSize,\n")
                    .append(searchParamsBuilder.toString())
                    .append("                }\n")
                    .append("                let url = '").append(getListUrl).append("?' + parseParam(param)\n")
                    .append("                axios.get(url).then(res => {\n")
                    .append("                    this.list = res.data;\n")
                    .append("                    this.loading = false;\n")
                    .append("                }).catch(res => {\n")
                    .append("                    console.error(res)\n")
                    .append("                    this.loading = false;\n")
                    .append("                })\n")
                    .append("            },\n");
        }
        methodsBuilder
                .append("            sortChange(opt) {\n")
                .append("                if (typeof opt.prop != 'undefined' && opt.prop != null) {\n")
                .append("                    this.sort = opt.prop;\n")
                .append("                } else {\n")
                .append("                    this.sort = null;\n")
                .append("                }\n")
                .append("                if (typeof opt.order != 'undefined' && opt.order != null) {\n")
                .append("                    this.order = opt.order == 'descending' ? 'desc' : 'asc';\n")
                .append("                } else {\n")
                .append("                    this.order = null;\n")
                .append("                }\n")
                .append("                this.getList();\n")
                .append("            },\n");

        createBuilder.append("            this.getList();\n");

        if (context.getStorage() != null) {
            if (context.getStorage() instanceof QiniuStorage) {
                jsBuilder.append("    <script src=\"${contextPath}${potatoPath}/static/qiniu.min.js\"></script>\n");
            } else if (context.getStorage() instanceof HuaweiStorage) {
                jsBuilder.append("    <script src=\"${contextPath}${potatoPath}/static/esdk-obs-browserjs-without-polyfill-3.19.9.min.js\"></script>\n");
            }
        }

        if (hasTinymceElement) {
            jsBuilder.append("    <script src=\"${contextPath}${potatoPath}/static/tinymce/tinymce.min.js\"></script>\n");

        }

        if (hasMarkdown) {
            cssBuilder.append("    <link rel=\"stylesheet\" href=\"${contextPath}${potatoPath}/static/mavon-editor/css/index.css\">\n");
            jsBuilder.append("    <script src=\"${contextPath}${potatoPath}/static/mavon-editor/mavon-editor.js\"></script>\n");
            vueUseBuilder.append("    Vue.use(MavonEditor)\n");
            if (hasMarkdownUseStorage) {
                methodsBuilder
                        .append("            mdUploadImage(prop) {\n")
                        .append("                const $vm = this.$refs['MD'+prop];\n")
                        .append("                if ($vm.editable == true) {\n")
                        .append("                    this.$refs['mdImage'+prop].click();\n")
                        .append("                }\n")
                        .append("            },\n");
                methodsBuilder
                        .append("            mdUploadImgChange(e) {\n")
                        .append("                let target = e.target;\n")
                        .append("                try {\n")
                        .append("                    let key = target.id;\n")
                        .append("                    key = key.replace('mdImage', '')\n")
                        .append("                    const file = e.target.files[0];\n")
                        .append("                    this.mdUploadFile(file, key);\n")
                        .append("                } catch (e) {\n")
                        .append("                    console.log(e)\n")
                        .append("                    this.$message.error('??????????????????');\n")
                        .append("                } finally {\n")
                        .append("                    target.value = '';\n")
                        .append("                }\n")
                        .append("            },\n");
                methodsBuilder
                        .append("            mdUploadFile(file, prop){\n")
                        .append("                const $vm = this.$refs['MD'+prop];\n")
                        .append("                axios.get('").append("${contextPath}").append(context.getHttpRequest()).append(context.getTokenRequest()).append("',{\n")
                        .append("                    params: {\n")
                        .append("                        file_name: file.name\n")
                        .append("                    }\n")
                        .append("                }).then(res => {\n")
                        .append("                    if (typeof res.data.token != 'undefined') {\n");
                if (context.getStorage() instanceof QiniuStorage) {
                    methodsBuilder
                            .append("                        let observable = qiniu.upload(file, res.data.key, res.data.token, {}, {\n")
                            .append("                            useCdnDomain: true\n")
                            .append("                        });\n")
                            .append("                        observable.subscribe({\n")
                            .append("                            next: (result) => {\n")
                            .append("                                console.log(result);\n")
                            .append("                            },\n")
                            .append("                            error: () => {\n")
                            .append("                                this.$message.error('??????????????????');\n")
                            .append("                            },\n")
                            .append("                            complete: (successRes) => {\n")
                            .append("                                let url = res.data.host + '/' + successRes.key;\n")
                            .append("                                $vm.insertText($vm.getTextareaDom(),{\n")
                            .append("                                    prefix: `![${file.name}](${url})`,\n")
                            .append("                                    subfix: '',\n")
                            .append("                                    str: ''\n")
                            .append("                                })\n")
                            .append("                            }\n")
                            .append("                        })\n");
                } else if (context.getStorage() instanceof HuaweiStorage) {
                    methodsBuilder
                            .append("                        let obsClient = new ObsClient({\n")
                            .append("                            access_key_id: res.data.access,\n")
                            .append("                            secret_access_key: res.data.secret,\n")
                            .append("                            server : res.data.endpoint,\n")
                            .append("                            security_token: res.data.token,\n")
                            .append("                            timeout: 24 * 60 * 60\n")
                            .append("                        });\n")
                            .append("                        obsClient.putObject({\n")
                            .append("                            Bucket: res.data.bucket,\n")
                            .append("                            Key: res.data.key,\n")
                            .append("                            SourceFile: file,\n")
                            .append("                            ProgressCallback: (transferredAmount, totalAmount, totalSeconds) => {\n")
                            .append("                                console.log('????????????: ' + transferredAmount * 1.0 / totalSeconds / 1024 + ' KB/S');\n")
                            .append("                                console.log(transferredAmount * 100.0 / totalAmount);\n")
                            .append("                            }\n")
                            .append("                        }, (err, result) => {\n")
                            .append("                            console.log(result)\n")
                            .append("                            if (result && result.CommonMsg && result.CommonMsg.Status == 200) {\n")
                            .append("                                let url = res.data.host + '/' + res.data.key;\n")
                            .append("                                $vm.insertText($vm.getTextareaDom(),{\n")
                            .append("                                    prefix: `![${file.name}](${url})`,\n")
                            .append("                                    subfix: '',\n")
                            .append("                                    str: ''\n")
                            .append("                                })\n")
                            .append("                            } else {\n")
                            .append("                                this.$message.error('??????????????????');\n")
                            .append("                                console.error(err);\n")
                            .append("                            }\n")
                            .append("                        });\n");
                }
                methodsBuilder
                        .append("                    } else {\n")
                        .append("                        console.error(res)\n")
                        .append("                        this.$message.error('??????token??????');\n")
                        .append("                    }\n")
                        .append("                }).catch(res => {\n")
                        .append("                    console.error(res)\n")
                        .append("                    this.$message.error('??????token??????');\n")
                        .append("                })\n")
                        .append("            },\n");
            }
        }

        if (hasUploadImage) {
            ExceptionAssert.isNull(context.getStorage()).throwException(HttpStatus.PARAM_ERROR.getCode(), "?????????????????????????????????");
            if (hasCutImage) {
                cssBuilder.append("    <link rel=\"stylesheet\" href=\"${contextPath}${potatoPath}/static/cropper/cropper.css\">\n");
                jsBuilder.append("    <script src=\"${contextPath}${potatoPath}/static/jquery.min.js\"></script>\n");
                jsBuilder.append("    <script src=\"${contextPath}${potatoPath}/static/cropper/cropper.js\"></script>\n");

                datasBuilder
                        .append("                jcrop: {\n")
                        .append("                    visible: false\n")
                        .append("                },\n");

                extraHtmlBuilder
                        .append("    <el-dialog class=\"jcrop_dialog\" :close-on-press-escape=\"false\" :close-on-click-modal=\"false\" title=\"????????????\" width=\"70%\" :visible.sync=\"jcrop.visible\" class=\"group-dialog\" :before-close=\"closeJcrop\">\n")
                        .append("        <div v-loading=\"loading\">\n")
                        .append("            <div>\n")
                        .append("                <img id=\"jcrop_img\" width=\"80%\" :src=\"jcrop.img\"/>\n")
                        .append("            </div>\n")
                        .append("            <el-row :gutter=\"24\" style=\"margin-top: 8px;\">\n")
                        .append("                <el-col :span=\"6\">\n")
                        .append("                    <el-input size=\"mini\" readonly=\"true\" v-model=\"jcrop.width\">\n")
                        .append("                        <template slot=\"prepend\">???</template>\n")
                        .append("                        <template slot=\"append\">px</template>\n")
                        .append("                    </el-input>\n")
                        .append("                </el-col>\n")
                        .append("                <el-col :span=\"6\">\n")
                        .append("                    <el-input size=\"mini\" readonly=\"true\" v-model=\"jcrop.height\">\n")
                        .append("                        <template slot=\"prepend\">???</template>\n")
                        .append("                        <template slot=\"append\">px</template>\n")
                        .append("                    </el-input>\n")
                        .append("                </el-col>\n")
                        .append("                <el-col :span=\"12\" style=\"text-align: right;\">\n")
                        .append("                    <el-button @click=\"closeJcrop\" size=\"small\">??? ???</el-button>\n")
                        .append("                    <el-button type=\"primary\" @click=\"confirmJcrop\" size=\"small\">??? ???</el-button>\n")
                        .append("                </el-col>\n")
                        .append("            </el-row>\n")
                        .append("        </div>\n")
                        .append("    </el-dialog>\n");

                methodsBuilder
                        .append("            closeJcrop() {\n")
                        .append("                this.jcrop = {\n")
                        .append("                    visible: false\n")
                        .append("                }\n")
                        .append("            },\n");
                methodsBuilder
                        .append("            confirmJcrop() {\n")
                        .append("                try {\n")
                        .append("                    $(\"#jcrop_img\").cropper('getCroppedCanvas').toBlob((blob) => {\n")
                        .append("                        let file = new File([blob], this.jcrop.filename, {type: this.jcrop.imageType, lastModified: Date.now()});\n")
                        .append("                        let key = this.jcrop.key\n")
                        .append("                        this.closeJcrop()\n")
                        .append("                        // ??????\n")
                        .append("                        this.uploadImage(file, key)\n")
                        .append("                    })\n")
                        .append("                } catch (e) {\n")
                        .append("                    console.log(e)\n")
                        .append("                    this.$message.error('????????????????????????');\n")
                        .append("                    this.closeJcrop()\n")
                        .append("                }\n")
                        .append("            },\n");

                methodsBuilder
                        .append("            uploadChange(file, fileList, key) {\n")
                        .append("                this.$refs[key].clearFiles()\n")
                        .append("                this.$confirm('?????????????', '??????', {\n")
                        .append("                    confirmButtonText: '??????',\n")
                        .append("                    cancelButtonText: '????????????',\n")
                        .append("                    showClose: false,\n")
                        .append("                    type: 'warning'\n")
                        .append("                }).then(() => {\n")
                        .append("                    try {\n")
                        .append("                        window.URL = window.URL || window.webkitURL;\n")
                        .append("                        let blobURL = window.URL.createObjectURL(file.raw);\n")
                        .append("                        this.jcrop = {\n")
                        .append("                            visible: true,\n")
                        .append("                            img: blobURL,\n")
                        .append("                            filename: file.raw.name,\n")
                        .append("                            imageType: file.raw.type,\n")
                        .append("                            key: key\n")
                        .append("                        }\n")
                        .append("                        this.$nextTick(() => {\n")
                        .append("                            $('#jcrop_img').cropper('destroy')\n")
                        .append("                                .cropper({\n")
                        .append("                                    viewMode: 2,        //???????????????????????????????????????\n")
                        .append("                                    dragMode: 'move',   //??????????????????\n")
                        .append("                                    crop: (data) => {\n")
                        .append("                                        this.jcrop.width = Math.round(data.width)\n")
                        .append("                                        this.jcrop.height = Math.round(data.height)\n")
                        .append("                                        this.jcrop = JSON.parse(JSON.stringify(this.jcrop))\n")
                        .append("                                    }\n")
                        .append("                                });\n")
                        .append("                        })\n")
                        .append("                    } catch (e) {\n")
                        .append("                        console.log(e)\n")
                        .append("                        this.$message.error('??????????????????');\n")
                        .append("                        this.closeJcrop()\n")
                        .append("                    }\n")
                        .append("                }).catch((e) => {\n")
                        .append("                    // ????????????\n")
                        .append("                    this.uploadImage(file.raw, key)\n")
                        .append("                });\n")
                        .append("            },\n");
            } else {
                methodsBuilder
                        .append("            uploadChange(file, fileList, key) {\n")
                        .append("                this.$refs[key].clearFiles()\n")
                        .append("                this.uploadImage(file.raw, key)\n")
                        .append("            },\n");
            }
            datasBuilder
                    .append("                uploadImageProgress: {},\n");
            methodsBuilder
                    .append("            removeImage(key) {\n")
                    .append("                this.$set(this.form, key, null)\n")
                    .append("            },\n");
            methodsBuilder
                    .append("            uploadImage(file, key) {\n")
                    .append("                console.log(file)\n")
                    .append("                this.uploadImageProgress[key] = 0\n")
                    .append("                this.uploadImageProgress = JSON.parse(JSON.stringify(this.uploadImageProgress))\n")
                    .append("                axios.get('").append("${contextPath}").append(context.getHttpRequest()).append(context.getTokenRequest()).append("',{\n")
                    .append("                    params: {\n")
                    .append("                        file_name: file.name\n")
                    .append("                    }\n")
                    .append("                }).then(res => {\n")
                    .append("                    if (typeof res.data.token != 'undefined') {\n");
            if (context.getStorage() instanceof QiniuStorage) {
                methodsBuilder
                        .append("                        let observable = qiniu.upload(file, res.data.key, res.data.token, {}, {\n")
                        .append("                            useCdnDomain: true\n")
                        .append("                        });\n")
                        .append("                        observable.subscribe({\n")
                        .append("                            next: (result) => {\n")
                        .append("                                this.uploadImageProgress[key] = Math.floor(result.total.percent * 100) / 100\n")
                        .append("                                this.uploadImageProgress = JSON.parse(JSON.stringify(this.uploadImageProgress))\n")
                        .append("                            },\n")
                        .append("                            error: () => {\n")
                        .append("                                this.$message.error('??????????????????');\n")
                        .append("                                delete this.uploadImageProgress[key];\n")
                        .append("                                this.uploadImageProgress = JSON.parse(JSON.stringify(this.uploadImageProgress))\n")
                        .append("                            },\n")
                        .append("                            complete: (successRes) => {\n")
                        .append("                                this.uploadImageProgress[key] = 100;\n")
                        .append("                                this.uploadImageProgress = JSON.parse(JSON.stringify(this.uploadImageProgress))\n")
                        .append("                                setTimeout(() => {\n")
                        .append("                                    this.$set(this.form, key, res.data.host + '/' + successRes.key)\n")
                        .append("                                    delete this.uploadImageProgress[key]\n")
                        .append("                                    this.uploadImageProgress = JSON.parse(JSON.stringify(this.uploadImageProgress))\n")
                        .append("                                }, 500)\n")
                        .append("                            }\n")
                        .append("                        })\n");
            } else if (context.getStorage() instanceof HuaweiStorage) {
                methodsBuilder
                        .append("                        let obsClient = new ObsClient({\n")
                        .append("                            access_key_id: res.data.access,\n")
                        .append("                            secret_access_key: res.data.secret,\n")
                        .append("                            server : res.data.endpoint,\n")
                        .append("                            security_token: res.data.token,\n")
                        .append("                            timeout: 24 * 60 * 60\n")
                        .append("                        });\n")
                        .append("                        obsClient.putObject({\n")
                        .append("                            Bucket: res.data.bucket,\n")
                        .append("                            Key: res.data.key,\n")
                        .append("                            SourceFile: file,\n")
                        .append("                            ProgressCallback: (transferredAmount, totalAmount, totalSeconds) => {\n")
                        .append("                                console.log('????????????: ' + transferredAmount * 1.0 / totalSeconds / 1024 + ' KB/S');\n")
                        .append("                                console.log(transferredAmount * 100.0 / totalAmount);\n")
                        .append("                                this.uploadImageProgress[key] = Math.floor((transferredAmount * 100.0 / totalAmount) * 100) / 100\n")
                        .append("                                this.uploadImageProgress = JSON.parse(JSON.stringify(this.uploadImageProgress))\n")
                        .append("                            }\n")
                        .append("                        }, (err, result) => {\n")
                        .append("                            console.log(result)\n")
                        .append("                            if (result && result.CommonMsg && result.CommonMsg.Status == 200) {\n")
                        .append("                                this.uploadImageProgress[key] = 100;\n")
                        .append("                                this.uploadImageProgress = JSON.parse(JSON.stringify(this.uploadImageProgress))\n")
                        .append("                                setTimeout(() => {\n")
                        .append("                                    this.$set(this.form, key, res.data.host + '/' + res.data.key)\n")
                        .append("                                    delete this.uploadImageProgress[key]\n")
                        .append("                                    this.uploadImageProgress = JSON.parse(JSON.stringify(this.uploadImageProgress))\n")
                        .append("                                }, 500)\n")
                        .append("                            } else {\n")
                        .append("                                this.$message.error('??????????????????');\n")
                        .append("                                delete this.uploadImageProgress[key];\n")
                        .append("                                this.uploadImageProgress = JSON.parse(JSON.stringify(this.uploadImageProgress))\n")
                        .append("                                console.error(err);\n")
                        .append("                            }\n")
                        .append("                        });\n");
            }
            methodsBuilder
                    .append("                    } else {\n")
                    .append("                        console.error(res)\n")
                    .append("                        delete this.uploadImageProgress[key]\n")
                    .append("                        this.uploadImageProgress = JSON.parse(JSON.stringify(this.uploadImageProgress))\n")
                    .append("                        this.$message.error('??????token??????');\n")
                    .append("                    }\n")
                    .append("                }).catch(res => {\n")
                    .append("                    console.error(res)\n")
                    .append("                    delete this.uploadImageProgress[key]\n")
                    .append("                    this.uploadImageProgress = JSON.parse(JSON.stringify(this.uploadImageProgress))\n")
                    .append("                    this.$message.error('??????token??????');\n")
                    .append("                })\n")
                    .append("            },\n");
        }

        map.put("css", cssBuilder.toString());
        map.put("js", jsBuilder.toString());
        map.put("vueUse", vueUseBuilder.toString());
        map.put("backHtml", backHtmlBuidler.toString());
        map.put("searchHtml", searchHtmlBuilder.toString());
        map.put("optionButtonHtml", optionButtonHtmlBuilder.toString());
        map.put("tableHtml", tableHtmlBuilder.toString());
        map.put("paginationHtml", paginationHtmlBuilder.toString());
        map.put("extraHtml", extraHtmlBuilder.toString());
        map.put("datas", datasBuilder.toString());
        map.put("methods", methodsBuilder.toString());
        map.put("created", createBuilder.toString());

        return result;
    }

    @Override
    protected String getFile() {
        return FILE_PATH;
    }
}
