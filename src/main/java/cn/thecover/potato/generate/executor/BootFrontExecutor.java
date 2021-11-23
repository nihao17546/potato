package cn.thecover.potato.generate.executor;

import cn.thecover.potato.generate.constant.BootConstant;
import cn.thecover.potato.generate.context.FrontContext;
import cn.thecover.potato.generate.context.FrontSearchElementContext;
import cn.thecover.potato.generate.context.RemoteContext;
import cn.thecover.potato.meta.conf.form.operate.Rule;
import cn.thecover.potato.meta.conf.form.operate.elements.OperateElement;
import cn.thecover.potato.meta.conf.form.search.element.DateTimeRangeSearchElement;
import cn.thecover.potato.meta.conf.table.UIColumn;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
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
//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> map = new HashMap<>();
        result.put(context.getPath(), map);
//        map.put("contextPath", request.getContextPath());
        map.put("title", context.getTitle());

        StringBuilder searchBuilder = new StringBuilder();
        StringBuilder mainColumnsBuilder = new StringBuilder();
        StringBuilder datasBuilder = new StringBuilder();
        StringBuilder methodsBuilder = new StringBuilder();
        StringBuilder paginationBuilder = new StringBuilder();
        StringBuilder optionColumnBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        StringBuilder extraHtmlBuilder = new StringBuilder();

        StringBuilder searchParamsBuilder = new StringBuilder();

        if (context.getForeignKeyProp() != null) {
            // 是从表页面，需获取外键请求参数
            datasBuilder.append("                ").append(context.getForeignKeyProp()).append(": null,\n");
            searchParamsBuilder.append("                        ")
                    .append(context.getForeignKeyProp()).append(": this.").append(context.getForeignKeyProp()).append(",\n");
            createBuilder
                    .append("            let ").append(context.getForeignKeyProp()).append(" = getParam(\"").append(context.getForeignKeyProp()).append("\");\n")
                    .append("            if (").append(context.getForeignKeyProp()).append(" != '' && ").append(context.getForeignKeyProp()).append(" != null && typeof (").append(context.getForeignKeyProp()).append(") != 'undefined') {\n")
                    .append("                this.").append(context.getForeignKeyProp()).append(" = ").append(context.getForeignKeyProp()).append(";\n")
                    .append("            } else {\n")
                    .append("                alert('链接错误');\n")
                    .append("                return;\n")
                    .append("            }\n");
            createBuilder
                    .append("            window.setInterval(function () {\n")
                    .append("                let height = document.body.scrollHeight;\n")
                    .append("                let dialogs = document.getElementsByClassName('el-dialog')\n")
                    .append("                if (dialogs && dialogs.length > 0) {\n")
                    .append("                    for (let i = 0; i < dialogs.length; i++) {\n")
                    .append("                        if (dialogs[i].getAttribute('role') == 'dialog') {\n")
                    .append("                            let h = dialogs[i].parentElement.scrollHeight\n")
                    .append("                            if (height < h) {\n")
                    .append("                                height = h;\n")
                    .append("                            }\n")
                    .append("                        }\n")
                    .append("                    }\n")
                    .append("                }\n")
                    .append("                window.parent.document.getElementById(\"ifa\").height = height;\n")
                    .append("            }, 100)\n");
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
            for (FrontSearchElementContext searchElement : context.getSearchElements()) {
                if (context.getForeignKeyProp() != null && context.getForeignKeyProp().equals(searchElement.getField())) {
                    // 与外键冲突，忽略
                    continue;
                }
                searchBuilder.append(searchElement.getElement().getOptions(searchElement.getField()));
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
            methodsBuilder
                    .append("            search() {\n")
                    .append("                if (typeof this.curPage != 'undefined') {\n")
                    .append("                    this.curPage = 1;\n")
                    .append("                }\n")
                    .append("                this.getList()\n")
                    .append("            },\n");
        }

        for (UIColumn uiColumn : context.getUiTable().getColumns()) {
            String prop = context.getProp(uiColumn);
            String label = uiColumn.getLabel();
            Integer width = uiColumn.getWidth();
            Boolean sortable = uiColumn.getSortable();
            mainColumnsBuilder
                    .append("        <el-table-column\n")
                    .append("                label=\"").append(label).append("\"\n");
            if (width != null) {
                mainColumnsBuilder.append("                width=\"").append(width).append("\"\n");
            }
            if (sortable != null) {
                mainColumnsBuilder.append("                sortable\n");
            }
            mainColumnsBuilder.append("                prop=\"").append(prop).append("\">\n");
            if (uiColumn.getFormatter() != null && !uiColumn.getFormatter().isEmpty()) {
                String formatterMethodName = prop + "Formatter";

                mainColumnsBuilder.append("            <template slot-scope=\"props\">\n");
                mainColumnsBuilder.append("                <span v-html=\"").append(formatterMethodName).append("(props.row, '").append(prop).append("')\"></span>\n");
                mainColumnsBuilder.append("            </template>\n");

                methodsBuilder
                        .append("            ").append(formatterMethodName).append("(row, prop) {\n")
                        .append("                ").append(uiColumn.getFormatter()).append("\n")
                        .append("            },\n");
            }
            mainColumnsBuilder.append("        </el-table-column>\n");
        }

        if (context.getOperateContext() != null) {
            datasBuilder.append("                form: {},\n");
            datasBuilder.append("                formVisible: false,\n");
            datasBuilder.append("                formTitle: '',\n");
            optionColumnBuilder.append("                    ")
                    .append("<el-button type=\"primary\" size=\"mini\" :disabled=\"loading\" @click=\"showInfo(props.row,[");
            int index = 0;
            for (String pk : context.getOperateContext().getPrimaryKeys()) {
                if (index ++ > 0) {
                    optionColumnBuilder.append(",");
                }
                optionColumnBuilder.append("'").append(pk).append("'");
            }
            optionColumnBuilder.append("])\">编辑")
                    .append("</el-button>\n");
            methodsBuilder
                    .append("            showInfo(row,pks) {\n")
                    .append("                let param = {}\n")
                    .append("                for(let i = 0; i < pks.length; i ++) {\n")
                    .append("                    param[pks[i]] = row[pks[i]]\n")
                    .append("                }\n")
                    .append("                axios.get('").append("${contextPath}").append(context.getInfoRequest()).append("',{\n")
                    .append("                    params: param\n")
                    .append("                }).then(res => {\n")
                    .append("                    if (res.data) {\n")
                    .append("                        this.form = res.data\n")
                    .append("                        this.formVisible = true\n")
                    .append("                        this.formTitle = '编辑'\n")
                    .append("                    } else {\n")
                    .append("                        this.$message.error('没有查询到数据');\n")
                    .append("                    }\n")
                    .append("                }).catch(res => {\n")
                    .append("                    console.error(res)\n")
                    .append("                    this.loading = false;\n")
                    .append("                })\n")
                    .append("            },\n");
            methodsBuilder
                    .append("            closeInfo() {\n")
                    .append("                this.formVisible = false\n")
                    .append("                this.formTitle = ''\n")
                    .append("                this.form = {}\n")
                    .append("                this.$refs.form.resetFields()\n")
                    .append("            },\n");
            extraHtmlBuilder
                    .append("    <el-dialog :title=\"formTitle\" :visible.sync=\"formVisible\" class=\"group-dialog\" :before-close=\"closeInfo\">\n")
                    .append("        <el-form :model=\"form\" :rules=\"rules\" ref=\"form\" size=\"small\">\n");
            StringBuilder formBuilder = new StringBuilder();
            StringBuilder ruleBuilder = new StringBuilder();
            for (OperateElement element : context.getOperateContext().getElements()) {
                formBuilder.append(element.getHtml());
                Rule rule = element.getRule();
                if (rule != null) {
                    ruleBuilder
                            .append("                    ").append(element.getFieldName())
                            .append(": [{required : ").append(rule.getRequired())
                            .append(", trigger: 'change', ");
                    if (rule.getRegular() == null) {
                        ruleBuilder.append("message: '").append(rule.getMessage()).append("'");
                    } else {
                        ruleBuilder.append("validator: function (rule, value, callback) {\n")
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
                    .append("                <el-button @click=\"closeInfo\" size=\"small\" :disabled=\"loading\">取 消</el-button>\n")
                    .append("                <el-button type=\"primary\" @click=\"submit('form')\" size=\"small\" :disabled=\"loading\">确 定</el-button>\n")
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
                    .append("                        let url = '").append("${contextPath}").append(context.getSaveRequest()).append("';\n")
                    .append("                        if ( this.formTitle == '编辑') {\n")
                    .append("                            url = '").append("${contextPath}").append(context.getUpdateRequest()).append("'\n")
                    .append("                        }\n")
                    .append("                        axios.post(url, this.form).then(res => {\n")
                    .append("                            this.loading = false;\n")
                    .append("                            if (res.data.code != 0) {\n")
                    .append("                                this.$message.error(res.data.message);\n")
                    .append("                            } else {\n")
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
        }

        if (!CollectionUtils.isEmpty(context.getFollows())) {
            for (FrontContext follow : context.getFollows()) {
                optionColumnBuilder.append("                    ")
                        .append("<el-button type=\"primary\" size=\"mini\" :disabled=\"loading\" @click=\"showFollow(props.row, ")
                        .append("'").append(follow.getForeignKeyProp()).append("'").append(", '")
                        .append(follow.getParentKeyProp(context.getPropMap())).append("', '")
                        .append("${potatoPath}").append(follow.getPath()).append("', '").append(follow.getTitle()).append("')\">")
                        .append(follow.getTitle())
                        .append("</el-button>\n");
            }
            datasBuilder.append("                followTitle: null,\n");
            datasBuilder.append("                followVisible: false,\n");
            datasBuilder.append("                followPath: null,\n");
            methodsBuilder
                    .append("            showFollow(row, fk, pk, path, title) {\n")
                    .append("                let v = row[pk];\n")
                    .append("                this.followVisible = true;\n")
                    .append("                this.followTitle = title;\n")
                    .append("                this.followPath = '" + "${contextPath}" + "' + path + '?' + fk + '=' + v;\n")
                    .append("            },\n");
            methodsBuilder
                    .append("            followClose() {\n")
                    .append("                this.followVisible = false;\n")
                    .append("                this.followTitle = null;\n")
                    .append("                this.followPath = null;\n")
                    .append("            },\n");
            extraHtmlBuilder
                    .append("    <el-dialog\n")
                    .append("            :before-close=\"followClose\"\n")
                    .append("            :title=\"followTitle\"\n")
                    .append("            :visible.sync=\"followVisible\"\n")
                    .append("            :fullscreen=\"true\">\n")
                    .append("        <iframe id=\"ifa\" :src=\"followPath\" style=\"width: 100%;\" frameborder=\"0\" scrolling=\"no\"></iframe>\n")
                    .append("    </el-dialog>");
        }
        if (optionColumnBuilder.length() > 0) {
            mainColumnsBuilder
                    .append("        <el-table-column\n");
            if (context.getUiTable().getOptionColumnWidth() != null) {
                mainColumnsBuilder.append("                width=\"").append(context.getUiTable().getOptionColumnWidth()).append("\"\n");
            }
            mainColumnsBuilder.append("                label=\"").append("操作").append("\">\n");
            mainColumnsBuilder.append("            <template slot-scope=\"props\">\n");
            mainColumnsBuilder.append("                <el-button-group>\n");
            mainColumnsBuilder.append(optionColumnBuilder.toString());
            mainColumnsBuilder.append("                </el-button-group>\n");
            mainColumnsBuilder.append("            </template>\n");
            mainColumnsBuilder.append("        </el-table-column>\n");
        }


        String getListUrl = "${contextPath}" + context.getListRequest();

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
                    .append("                axios.get('").append(getListUrl).append("',{\n" )
                    .append("                    params: {\n")
                    .append("                        sort: this.sort,\n")
                    .append("                        order: this.order,\n")
                    .append("                        curPage: this.curPage,\n")
                    .append("                        pageSize: this.pageSize,\n")
                    .append(searchParamsBuilder.toString())
                    .append("                    }\n")
                    .append("                }).then(res => {\n")
                    .append("                    this.list = res.data.list;\n")
                    .append("                    this.totalCount = res.data.count;\n")
                    .append("                    this.loading = false;\n")
                    .append("                }).catch(res => {\n")
                    .append("                    console.error(res)\n")
                    .append("                    this.loading = false;\n")
                    .append("                })\n")
                    .append("            },\n");


            paginationBuilder
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
                    .append("                axios.get('").append(getListUrl).append("',{\n" )
                    .append("                    params: {\n")
                    .append("                        sort: this.sort,\n")
                    .append("                        order: this.order,\n")
                    .append(searchParamsBuilder.toString())
                    .append("                    }\n")
                    .append("                }).then(res => {\n")
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

        map.put("mainTableColumns", mainColumnsBuilder.toString());
        map.put("datas", datasBuilder.toString());
        map.put("methods", methodsBuilder.toString());
        map.put("mainPagination", paginationBuilder.toString());
        map.put("extraHtml", extraHtmlBuilder.toString());

        StringBuilder topBuilder = new StringBuilder();
        topBuilder
                .append("    <div style=\"padding-left: 5px;\">\n")
                .append("        <el-form @submit.native.prevent :inline=\"true\">\n")
                .append(searchBuilder.toString());
        if (searchBuilder.length() > 0) {
            topBuilder
                    .append("        <el-form-item>\n")
                    .append("            <el-button type=\"primary\" @click=\"search\">查询</el-button>\n")
                    .append("        </el-form-item>\n");
        }
        topBuilder
                .append("        </el-form>\n")
                .append("    </div>\n");
        map.put("top", topBuilder.toString());
        map.put("created", createBuilder.toString());

        return result;
    }

    @Override
    protected String getFile() {
        return FILE_PATH;
    }
}
