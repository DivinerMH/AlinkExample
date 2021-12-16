package linksame.com.Ftrl;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.LogisticRegressionTrainBatchOp;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.operator.batch.source.RandomTableSourceBatchOp;
import com.alibaba.alink.operator.batch.source.TextSourceBatchOp;
import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.onlinelearning.FtrlPredictStreamOp;
import com.alibaba.alink.operator.stream.onlinelearning.FtrlTrainStreamOp;
import com.alibaba.alink.operator.stream.source.CsvSourceStreamOp;
import com.alibaba.alink.operator.stream.source.RandomTableSourceStreamOp;
import org.junit.Test;

/**
 *  * 测试线性回归三元一次方程
 *  *      5X + 2Y + Z + t = O
 *  *          三元：X，Y，Z
 *  *          常量：t
 *  *          结果：O
 * @Author: menghuan
 * @Date: 2021/12/10 9:37
 */
public class FTRLOnlineTrainSecond {

    // 初始化 模型文件路径
    private static final String initModelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/model/FTRLLinearRegModel.ak";
    // 初始化 数据文件路径
    private static final String initTrainPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/FTRLInitTrain.txt";
    // 在线训练 数据文件路径
    private static final String onlineTrainPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/FTRLOnlineTrain.txt";
    // 在线训练 数据文件路径
    private static final String onlineTrainAllPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/FTRLOnlineTrainAll.txt";


    // 特性工程的结果列名
    private static final String vecColName = "vec";

    // 设置标签列 - click 列标明了是否被点击，是分类问题的标签列
    private static final String labelColName = "label";

    // FeatureHash 操作会将这些特征通过 hash 的方式，映射到一个稀疏向量中，向量的维度可以设置，我们这里设置为30000
    private static final int numHashFeatures = 30000;

    // 格式 Schema
    private static final String schemaStr = "f0 int,f1 int,f2 int,f3 int,label int";
    // 特征值
    String[] featureCols = new String[]{"f0", "f1", "f2", "f3"};
    // 设置选择的属性
    private static final String[] selectedColNames = new String[]{"f0,f1,f2,f3,label"};
    // 数值型特征
    private static final String[] categoryColNames = new String[]{"f0,f1,f2"};
    //（枚举）类别型特征
    private static final String[] numericalColNames = new String[]{"f3"};

    @Test
    public void initData() throws Exception {
        new TextSourceBatchOp()
                .setFilePath(initTrainPath)
                .firstN(10)
                .print();
    }

    @Test
    public void loadInitData() throws Exception {
        // 定义 schema 后，可以通过 CsvSourceBatchOp 读取显示数据
        CsvSourceBatchOp trainBatchData = new CsvSourceBatchOp()
                .setFilePath("G:/Idea-Workspaces/AlinkExample/src/main/resources/static/FTRLInitTrain.txt")
                .setFieldDelimiter("|")
                .setSchemaStr("f0 string,f1 string,f2 string,f3 string,label string")
                .setIgnoreFirstLine(false);
        // 打印前10条数据
        trainBatchData.firstN(5).print();
    }

    @Test
    public void randomInitData() throws Exception {
        // 随机构建数据源
        BatchOperator<?> batchData = new RandomTableSourceBatchOp()
                .setNumCols(5)              // 随机构建数据的列数
                .setNumRows(20L)            // 随机构建数据的max行数
                .setOutputCols(new String[]{"f0", "f1", "f2", "f3", "label"})
                .setOutputColConfs("label:weight_set(1.0,1.0,2.0,5.0)");

        // 打印前10条数据
        batchData.firstN(20).print();
    }

    @Test
    public void randomInitDataSecond() throws Exception {
        // 随机构建数据源
        BatchOperator<?> batchData = new RandomTableSourceBatchOp()
                .setNumCols(5)              // 随机构建数据的列数
                .setNumRows(20L)            // 随机构建数据的max行数
                .setOutputCols(new String[]{"f0", "f1", "f2", "f3", "label"})
                .setOutputColConfs("label:weight_set(1.0,1.0,2.0,5.0)");

        // 打印前10条数据
        batchData.firstN(20).print();

        System.out.println("============================= 流式数据开始打印 =============================");

        StreamOperator<?> streamData = new RandomTableSourceStreamOp()
                .setNumCols(5)              // 输出表列数目
                .setMaxRows(30L)            // 输出表行数目最大值
                .setOutputCols(new String[]{"f0", "f1", "f2", "f3", "label"})   // 输出列名数组(字符串数组，当参数不设置时，算法自动生成)
                .setOutputColConfs("label:weight_set(1.0,1.0,2.0,5.0)")         // 列配置信息(表示每一列的数据分布配置信息)
                .setTimePerSample(0.1);     // 每条样本流过的时间(每两条样本间的时间间隔，单位秒)

        // 打印前10条数据
        streamData.print();
        // 提示：流式操作需要execute触发
        StreamOperator.execute();
    }

    @Test
    public void fTRLInitModel() throws Exception {

        StreamOperator.setParallelism(2);

        // 批式原始训练数据
        BatchOperator<?> batchData = new CsvSourceBatchOp()
                .setFilePath(initTrainPath)
                .setFieldDelimiter("|")         // 字段分隔符
                .setSchemaStr("f0 int,f1 int,f2 int,f3 int,label int")
                .setIgnoreFirstLine(true)       // 忽略第一行
                .setLenient(true);              // 开启容错
        // trainBatchData.print();

        BatchOperator<?> batchData2 = new RandomTableSourceBatchOp()
                .setNumCols(5)          // 随机构建数据的列数
                .setNumRows(20L)        // 随机构建数据的max行数
                .setOutputCols(new String[]{"f0", "f1", "f2", "f3", "label"})
                .setOutputColConfs("label:weight_set(1.0,1.0,2.0,5.0)");

        LogisticRegressionTrainBatchOp model = new LogisticRegressionTrainBatchOp()
                .setFeatureCols(new String[]{"f0", "f1", "f2", "f3"})
                .setWithIntercept(true)         // 是否有常数项
                .setLabelCol("label")           // 标签列名
                .setMaxIter(10)                 // 最大迭代步数
                .linkFrom(batchData);

        StreamOperator.execute();

        // 准备流式训练数据
        /*StreamOperator<?> streamData = new CsvSourceStreamOp()
                .setFilePath(onlineTrainAllPath)
                .setFieldDelimiter("|")
                .setSchemaStr("f0 int,f1 int,f2 int,f3 int,label int")
                .setIgnoreFirstLine(true);*/

        StreamOperator<?> streamData = new RandomTableSourceStreamOp()
                .setNumCols(5)
                .setMaxRows(100L)
                .setOutputCols(new String[]{"f0", "f1", "f2", "f3", "label"})
                .setOutputColConfs("label:weight_set(1.0,1.0,2.0,5.0)")
                .setTimePerSample(0.1);

        // streamData.print();

        // StreamOperator<?> sModel = new FtrlTrainStreamOp(model)
        FtrlTrainStreamOp sModel = new FtrlTrainStreamOp(model)
                .setFeatureCols(new String[]{"f0", "f1", "f2", "f3","label"})   // 特征向量名
                .setLabelCol("label")       // 标签列名
                .setWithIntercept(true)     // 有常数项
                .setAlpha(0.1)              // 参数α的值
                .setBeta(0.1)               // 参数β的值
                .setL1(0.1)                 // L1 正则化系数
                .setL2(0.1)                 // L2 正则化系数
                .setTimeInterval(10)        // 数据流流动过程中时间的间隔（窗口大小）
                .setVectorSize(4)           // 向量长度
                .linkFrom(streamData);      // 模型 连接 流式向量训练数据

        new FtrlPredictStreamOp(model)
                .setPredictionCol("pred")
                .setReservedCols(new String[]{"label"})
                .setPredictionDetailCol("details")
                .linkFrom(sModel, streamData)
                .print();

        StreamOperator.execute();
    }

}
