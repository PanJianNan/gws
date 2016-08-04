package com.enation.app.base.core.service.solution.impl;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;

import com.enation.app.base.core.service.dbsolution.DBSolutionFactory;
import com.enation.app.base.core.service.dbsolution.IDBSolution;
import com.enation.app.base.core.service.solution.IInstaller;
import com.enation.app.base.core.service.solution.InstallUtil;
import com.enation.eop.resource.model.EopSite;
import com.enation.eop.sdk.context.EopContext;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.framework.database.ISqlFileExecutor;
import com.enation.framework.util.FileUtil;


/**
 * 示例数据安装器
 *
 * @author kingapex
 */
public class ExampleDataInstaller implements IInstaller {

    private ISqlFileExecutor sqlFileExecutor;

    protected final Logger logger = Logger.getLogger(getClass());
    protected String eopsiteSQL;

    public String parseConst(String content) {
        eopsiteSQL = "";
        Map<String, String> constMap = new HashMap<String, String>();
        char buf[] = new char[content.length()];
        content.getChars(0, content.length(), buf, 0);
        CharArrayReader in = new CharArrayReader(buf);
        BufferedReader reader = new BufferedReader(in);
        while (true) {
            try {
                String line = reader.readLine();
                if (line == null)
                    break;
                else if (line.startsWith("CONST")) {
                    String[] value = line.substring(line.indexOf('!')).split("=");
                    constMap.put(value[0], value[1]);
                    content = content.replaceFirst(line + "\n", "");
                } else if (line.startsWith("EOPSITE")) {
                    eopsiteSQL = "\n" + line.replaceFirst("EOPSITE", "update eop_site");
                    content = content.replaceFirst(line + "\n", "");
                } else
                    break;
            } catch (Exception e) {
                break;
            }
        }
        Set<String> keys = constMap.keySet();
        for (Iterator it = keys.iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            String value = constMap.get(key);
            content = content.replaceAll(key, value);
            eopsiteSQL = eopsiteSQL.replaceAll(key, value);
        }
        return content;
    }

    protected String parseUserSiteID(String content) {
        if ("2".equals(EopSetting.RUNMODE)) {
            EopSite site = EopContext.getContext().getCurrentSite();
            content = content.replaceAll("<userid>", String.valueOf(site.getUserid()));
            content = content.replaceAll("<siteid>", String.valueOf(site.getId()));
        } else {
            content = content.replaceAll("_<userid>", "");
            content = content.replaceAll("_<siteid>", "");
            content = content.replaceAll("/user/<userid>/<siteid>", "");
            content = content.replaceAll("<userid>", "1");
            content = content.replaceAll("<siteid>", "1");
        }
        return content;
    }

    /**
     * 使用新的数据库中间件来导入数据
     *
     * @param dataSqlPath
     * @return
     */
    private void anyDBInstall(String dataSqlPath) throws Exception {
        IDBSolution dbsolution = DBSolutionFactory.getDBSolution();
        dbsolution.setPrefix("es_");
        dbsolution.dbImport(dataSqlPath);
    }

    /**
     * 返回产品示例数据sql的路径
     *
     * @param productId
     * @return
     */
    private String getDataSqlPath(String productId) {
        String dataSqlPath = "";
        if (EopSetting.DBTYPE.equals("1")) {
//            dataSqlPath = EopSetting.PRODUCTS_STORAGE_PATH + "/" + productId + "/example_data_mysql.sql";
            dataSqlPath = EopSetting.PRODUCTS_DB_INIT_PATH + "/" + productId + "/example_data_mysql.sql";
        } else if (EopSetting.DBTYPE.equals("2")) {
//            dataSqlPath = EopSetting.PRODUCTS_STORAGE_PATH + "/" + productId + "/example_data_oracle.sql";
            dataSqlPath = EopSetting.PRODUCTS_DB_INIT_PATH + "/" + productId + "/example_data_oracle.sql";
        } else if (EopSetting.DBTYPE.equals("3")) {
//            dataSqlPath = EopSetting.PRODUCTS_STORAGE_PATH + "/" + productId + "/example_data_mssql.sql";
            dataSqlPath = EopSetting.PRODUCTS_DB_INIT_PATH + "/" + productId + "/example_data_mssql.sql";
        }
        return dataSqlPath;
    }

    private void loggerPrint(String text) {
        if (logger.isDebugEnabled()) {
            this.logger.debug(text);
        }
    }

    /**
     * 安装产品相应的示例数据
     *
     * @param productId
     * @param fragment
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void install(String productId, Node fragment) {
        boolean xmlData = true;
//        String dataSqlPath = EopSetting.PRODUCTS_STORAGE_PATH + "/" + productId + "/example_data.xml";
        String dataSqlPath = EopSetting.PRODUCTS_DB_INIT_PATH + "/" + productId + "/example_data.xml";
        File xmlFile = FileUtil.getFile(dataSqlPath);
        if (!xmlFile.exists()) {        //	如果example_data.xml不存在，执行旧版本(即直接执行产品示例数据的sql)
            xmlData = false;
            dataSqlPath = getDataSqlPath(productId);
        }
        this.loggerPrint("安装datasqlPath:" + dataSqlPath);

        try {
            if (!"base".equals(productId)) {
                InstallUtil.putMessaage("正在安装示例数据，可能要花费较长时间，请稍后 <img src='resource/com/enation/app/saas/widget/product/loading.gif'");
            } else {
                InstallUtil.putMessaage("正在安装基础数据...");
            }

            if (xmlData) {
                anyDBInstall(dataSqlPath);//直接使用example_data.xml初始化
            } else {
                if (FileUtil.getFile(dataSqlPath).exists()) {
                    //安装示例数据
                    String content = FileUtil.read(dataSqlPath, "UTF-8");
                    //解析文件头部CONST和EOPSITE命令，CONST用于替换下面的文本内容，EOPSITE用于更新eop_site表
                    content = parseConst(content);

                    content = parseUserSiteID(content);
                    content = this.filter(content);
                    content = content + parseUserSiteID(eopsiteSQL);

                    sqlFileExecutor.execute(content);
                } else {
                    this.loggerPrint("示例数据安装失败：找不到" + dataSqlPath);
                }
            }
            this.loggerPrint("示例数据安装完毕");

            //若静态资源分离，会将示例静态资源复制到安装时指定的服务器
            FileUtil.copyFolder(
                    EopSetting.PRODUCTS_STORAGE_PATH + "/" + productId + "/attachment/",
                    EopSetting.IMG_SERVER_PATH + EopContext.getContext().getContextPath() + "/attachment/");

            //非base应用copy init.sql
            if (!"base".equals(productId)) {
//                FileUtil.copyFile(EopSetting.PRODUCTS_STORAGE_PATH + "/" + productId + "/init.sql", EopSetting.EOP_PATH + EopContext.getContext().getContextPath() + "/init.sql");
                File initSqlFile = FileUtil.getFile(EopSetting.PRODUCTS_DB_INIT_PATH + "/" + productId + "/init.sql");
                File rootSqlFile = new File(EopSetting.EOP_PATH + EopContext.getContext().getContextPath());
                //将该类产品的init.sql copy 到根目录
                FileUtil.copyFileToDirectory(initSqlFile, rootSqlFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
            this.logger.debug(e.getMessage(), e);
            throw new RuntimeException("安装示例数据出错...");
        }
    }

    /**
     * 过滤drop/truncate/delete语句/以及eop_开头的语句
     *
     * @param input
     * @return
     */
    private String filter(String input) {
        Pattern pattern = Pattern.compile("delete\\s?.*?\\s?;", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        input = matcher.replaceAll("");
        pattern = Pattern.compile("truncate\\s?.*?\\s?;", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(input);
        input = matcher.replaceAll("");
        pattern = Pattern.compile("drop\\s?.*?\\s?;", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(input);
        input = matcher.replaceAll("");
        pattern = Pattern.compile("(delete|drop|truncate|insert|update)\\s?eop_.*?;", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(input);
        input = matcher.replaceAll("");

        return input;
    }

    public void setSqlFileExecutor(ISqlFileExecutor sqlFileExecutor) {
        this.sqlFileExecutor = sqlFileExecutor;
    }

}
