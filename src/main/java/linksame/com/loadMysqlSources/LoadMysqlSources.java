package linksame.com.loadMysqlSources;

import com.alibaba.alink.common.AlinkGlobalConfiguration;
import com.alibaba.alink.common.io.catalog.MySqlCatalog;
import org.junit.Test;

/**
 * @Author: menghuan
 * @Date: 2021/10/15 16:35
 * Catalog描述了数据库的属性和数据库的位置, 支持 Mysql, Derby, Sqlite, Hive.
 * 在使用时，需要先下载插件，详情请看 https://www.yuque.com/pinshu/alink_guide/czg4cx
 * 定义分成三步：
 *      第一步，定义Catalog
 *      第二步， 定义CatalogObject
 *      第三步，定义Source和Sink
 */
public class LoadMysqlSources {

    @Test
    public void loadMysqlSources(){

        // 指定插件路径
        AlinkGlobalConfiguration.setPluginDir("G:/Idea-Workspaces/AlinkPlugIn");

        /**
         * 第一步，定义 Catalog 包括 :
         *      catalogName：目录名，自定义，当为null时会自动生成，"catalog_" + UUID.randomUUID().toString().replaceAll("-", "_");
         *      defaultDatabase：默认数据库名，在数据库中一定要有，如果为null，会使用 default 库
         *      MySQL版本
         *      url
         *      port
         *      userName
         *      password
         */
        // MySqlCatalog mysqlCatalog = new MySqlCatalog();



    }
}
