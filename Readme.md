# POTATO 低代码开发插件

当前最新版本： 1.0.0（发布日期：2022-02-10）

[![AUR](https://img.shields.io/badge/license-Springboot%202.x-blue.svg)](https://github.com/nihao17546/potato)
[![](https://img.shields.io/badge/Author-neil-orange.svg)](https://www.appcnd.com)
[![](https://img.shields.io/badge/version-1.0.0-brightgreen.svg)](https://github.com/nihao17546/potato)

项目介绍：
-----------------------------------
Potato 是一款基于Java的`低代码插件`！通过该插件可以不用写任何前后端代码实现常规`后台管理系统`的开发，并且对原有项目代码无侵入。
可在线生成前后端代码、代码可直接发布生效或导出代码。
支持对单表、一对一、一对多从表操作，支持tinymce富文本、mardown，支持七牛云、华为云对象存储、支持图片裁剪。
帮助解决Java项目70%的重复工作，节省研发成本！

适用于
-----------------------------------
Potato 适用于企业信息管理系统（MIS）、内部办公系统（OA）、企业资源计划系统（ERP）、客户关系管理系统（CRM）、内容管理系统（CMS）

技术选型
-----------------------------------
- Springboot2.x
- Mybatis-plus
- Vue2.0
- Element-ui

快速开始
-----------------------------------

- 1.拉取项目代码
```bash
git clone https://github.com/nihao17546/potato.git
cd  potato
```

- 2.安装打包
```bash
mvn clean -Dmaven.test.skip=true install
```

- 3.引入依赖
```xml
<dependency>
    <groupId>com.appcnd</groupId>
    <artifactId>potato</artifactId>
    <version>1.0.0</version>
</dependency>
```

- 4.项目配置
```properties
# 指定访问路径前缀（必须配置）
spring.potato.path=/potato
# 指定生成的class存放目录（可选）
spring.potato.class-path=/tmp
# 指定管理配置登录账号
spring.potato.loginname=root
# 指定管理配置登录密码
spring.potato.password=123456
# 是否是集群模式（可选）
spring.potato.cluster=false
# spring.potato.db.** 配置数据库相关，不配置使用项目原有自带的
```

- 5.启动项目
![POTATO](https://s.wqisland.com/ops/img/ef642b51059c4a2ea6c22210046cfb88.png "Potato低代码开发插件")
> 如项目访问地址为127.0.0.1:9000/ppp，以上步配置为例，管理配置页面路径为：127.0.0.1:9000/ppp/potato/index.html


待完善项
-----------------------------------
* bean交由spring管理后，spring移除了bean，class无法卸载，classloader没有被gc
* tinymce富文本视频上传进度条
* 表单分类显示
* path配置校验
* 远程下拉菜单sql合法性校验
* 本地对象存储

交流互动
-----------------------------------
[https://www.appcnd.com](https://www.appcnd.com)