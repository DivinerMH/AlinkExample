package linksame.com.Ftrl;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.LogisticRegressionTrainBatchOp;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.operator.batch.source.TextSourceBatchOp;
import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.dataproc.JsonValueStreamOp;
import com.alibaba.alink.operator.stream.dataproc.SplitStreamOp;
import com.alibaba.alink.operator.stream.evaluation.EvalBinaryClassStreamOp;
import com.alibaba.alink.operator.stream.onlinelearning.FtrlPredictStreamOp;
import com.alibaba.alink.operator.stream.onlinelearning.FtrlTrainStreamOp;
import com.alibaba.alink.operator.stream.source.CsvSourceStreamOp;
import com.alibaba.alink.pipeline.Pipeline;
import com.alibaba.alink.pipeline.PipelineModel;
import com.alibaba.alink.pipeline.dataproc.StandardScaler;
import com.alibaba.alink.pipeline.feature.FeatureHasher;
import org.junit.Test;

/**
 * 参考地址：https://www.freesion.com/article/53071116558/   ALINK 在线学习的 6 个 JAVA 示例
 *          https://github.com/alibaba/Alink/blob/master/examples/src/main/java/com/alibaba/alink/FTRLExample.java
 *
 * @Author: menghuan
 * @Date: 2021/12/10 14:33
 */
public class FTRLExampleSecond {

    // 训练文件路径 = 静态资源路径+文件目录路径
    private static final String trainPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/avazu-small.csv";

    // 该管道模型可以作用在批式数据，也可以应用在流式数据，生成特征向量。我们先把这个特征工程处理模型保存到本地，设置文件路径
    // fit and save feature pipeline model
    private static final String FEATURE_PIPELINE_MODEL_FILE =  "G:/Idea-Workspaces/AlinkExample/src/main/resources/model/feature_pipe_model.csv";

    // 定义 SchemaStr ( 根据数据设置列 )
    private static final String schemaStr
            = "id string, click string, dt string, C1 string, banner_pos int, site_id string, site_domain string, "
            + "site_category string, app_id string, app_domain string, app_category string, device_id string, "
            + "device_ip string, device_model string, device_type string, device_conn_type string, C14 int, C15 int, "
            + "C16 int, C17 int, C18 int, C19 int, C20 int, C21 int";

    // 设置标签列 - click 列标明了是否被点击，是分类问题的标签列
    private static final String labelColName = "click";
    // 设置选择的属性
    private static final String[] selectedColNames = new String[]{
            "C1", "banner_pos", "site_category", "app_domain",
            "app_category", "device_type", "device_conn_type",
            "C14", "C15", "C16", "C17", "C18", "C19", "C20", "C21",
            "site_id", "site_domain", "device_id", "device_model"};

    // 数值型特征
    private static final String[] categoryColNames = new String[]{
            "C1", "banner_pos", "site_category", "app_domain",
            "app_category", "device_type", "device_conn_type",
            "site_id", "site_domain", "device_id", "device_model"};

    //（枚举）类别型特征
    private static final String[] numericalColNames = new String[]{
            "C14", "C15", "C16", "C17", "C18", "C19", "C20", "C21"};

    // 标准化后的结果向量名称 - 特性工程的结果列名
    private static final String vecColName = "vec";

    // 向量长度
    // FeatureHash 操作会将这些特征通过 hash 的方式，映射到一个稀疏向量中，向量的维度可以设置，我们这里设置为30000
    private static final int numHashFeatures = 30000;


    /**
     * 使用 Kaggle 比赛的 CTR 数据 ， 链接为：https://www.kaggle.com/c/avazu-ctr-prediction/data
     * 使用 TextSourceBatchOp 整行读取打印部分数据
     *      1、url 数据加载缓慢
     *      2、加载本地数据
     * @throws Exception
     */
    @Test
    public void testRemoteData() throws Exception {
        new TextSourceBatchOp()
                // .setFilePath("http://alink-release.oss-cn-beijing.aliyuncs.com/data-files/avazu-small.csv")
                .setFilePath(trainPath)
                .firstN(10)
                .print();
    }

    @Test
    public void testRemoteData2() throws Exception {
        // 定义 SchemaStr 后，可以通过 CsvSourceBatchOp 读取显示数据
        CsvSourceBatchOp trainBatchData = new CsvSourceBatchOp()
                //.setFilePath("http://alink-release.oss-cn-beijing.aliyuncs.com/data-files/avazu-small.csv")
                .setFilePath(trainPath)
                .setSchemaStr(schemaStr);
        // 打印前10条数据
        trainBatchData.firstN(10).print();
    }

    @Test
    public void testRemoteData3() throws Exception {
        // 批式原始训练数据
        CsvSourceBatchOp trainBatchData = new CsvSourceBatchOp()
                //.setFilePath("http://alink-release.oss-cn-beijing.aliyuncs.com/data-files/avazu-small.csv")
                .setFilePath(trainPath)
                .setSchemaStr(schemaStr);

        // 定义 特征工程 处理 pipeline(管道)
        // setup feature engineering pipeline
        Pipeline featurePipeline = new Pipeline()
                .add(
                        // 标准缩放 ( 数值特征标准化 )
                        new StandardScaler()
                                .setSelectedCols(numericalColNames)
                )
                .add(
                        // 特征哈希 ( 将多个特征组合成一个特征向量 )
                        new FeatureHasher()
                                .setSelectedCols(selectedColNames)
                                .setCategoricalCols(categoryColNames)
                                .setOutputCol(vecColName)
                                .setNumFeatures(numHashFeatures)
                );

        // 对批式训练数据 trainBatchData 执行 fit 方法，及进行训练，得到 PipelineModel(管道模型)
        // 构建特征工程流水线
        // fit feature pipeline model
        PipelineModel fit = featurePipeline
                .fit(trainBatchData);
        // 初始模型允许覆盖重写
        fit.save(FEATURE_PIPELINE_MODEL_FILE,true);

        // 批处理执行
        BatchOperator.execute();

    }

    @Test
    public void testRemoteData4() throws Exception {

        // 批式原始训练数据
        CsvSourceBatchOp trainBatchData = new CsvSourceBatchOp()
                //.setFilePath("http://alink-release.oss-cn-beijing.aliyuncs.com/data-files/avazu-small.csv")
                .setFilePath(trainPath)
                .setSchemaStr(schemaStr);

        // 准备流式训练数据
        CsvSourceStreamOp data = new CsvSourceStreamOp()
                .setFilePath("http://alink-release.oss-cn-beijing.aliyuncs.com/data-files/avazu-ctr-train-8M.csv")
                .setSchemaStr(schemaStr)
                .setIgnoreFirstLine(true);

        // 对于流数据源进行实时切分获得原始训练数据和原始预测数据
        // split stream to train and eval data
        // 通过定义一个流式数据源，并按1:1的比例实时切分数据，从而得到 : 流式原始训练数据、流式原始预测数据
        SplitStreamOp spliter = new SplitStreamOp()
                .setFraction(0.5)
                .linkFrom(data);
        StreamOperator<?> trainStreamData = spliter;
        StreamOperator<?> testStreamData = spliter.getSideOutput(0);

        // 通过PipelineModel.load()方法，可以载入前面保存的特征工程处理模型
        PipelineModel featurePipelineModel = PipelineModel.load(FEATURE_PIPELINE_MODEL_FILE);

        /* 分别利用模型生成批式训练数据，流式训练数据，流式测试数据 */

        // 训练出一个逻辑回归模型做为FTRL算法的初始模型，这是为了系统冷启动的须要。
        // 定义逻辑回归分类器 lr
        LogisticRegressionTrainBatchOp lr = new LogisticRegressionTrainBatchOp()
                .setVectorCol(vecColName)       // 向量列名
                .setLabelCol(labelColName)      // 标签列名
                .setWithIntercept(true)         // 是否有常数项
                .setMaxIter(10);                // 最大迭代步数

        // 连接批处理数据
        BatchOperator<?> initModel = featurePipelineModel.transform(trainBatchData).link(lr);

        /* FTRL模型训练及预测评估 */

        // ftrl train ( 在初始模型基础上进行 FTRL 流式在线训练 )
        FtrlTrainStreamOp model = new FtrlTrainStreamOp(initModel)
                .setVectorCol(vecColName)       // 特征向量名
                .setLabelCol(labelColName)      // 标签列名
                .setWithIntercept(true)         // 有常数项
                .setAlpha(0.1)                  // 参数α的值
                .setBeta(0.1)                   // 参数β的值
                .setL1(0.01)                    // L1 正则化系数
                .setL2(0.01)                    // L2 正则化系数
                .setTimeInterval(10)            // 数据流流动过程中时间的间隔（窗口大小）
                .setVectorSize(numHashFeatures) // 向量长度
                // 模型 连接 流式向量训练数据
                .linkFrom(featurePipelineModel.transform(trainStreamData));

        // ftrl predict ( 在FTRL在线模型的基础上，链接预测数据进行预测 )
        FtrlPredictStreamOp predResult = new FtrlPredictStreamOp(initModel)
                .setVectorCol(vecColName)       // 向量列名
                .setPredictionCol("pred")       // 预测结果列名
                .setReservedCols(new String[] {labelColName})       // 算法保留列名
                .setPredictionDetailCol("details")                  // 预测详细信息列名
                .linkFrom(model, featurePipelineModel.transform(testStreamData));   // 模型 连接 模型流 和 流式测试数据
        // 取样输出
        predResult.sample(0.0001).print();

        // 对于流式的任务，print()方法不能触发流式任务的执行，必须调用StreamOperator.execute()方法，才能开始执行
        StreamOperator.execute();

        /* 模型流式二分类评估 */
        predResult
                .link(
                        new EvalBinaryClassStreamOp()
                                .setLabelCol(labelColName)
                                .setPredictionCol("pred")
                                .setPredictionDetailCol("details")
                                .setTimeInterval(10)
                )
                .link(
                        new JsonValueStreamOp()
                                .setSelectedCol("Data")
                                .setReservedCols(new String[] {"Statistics"})
                                .setOutputCols(new String[] {"Accuracy", "AUC", "ConfusionMatrix"})
                                .setJsonPath(new String[] {"$.Accuracy", "$.AUC", "$.ConfusionMatrix"})
                )
                .print();
        StreamOperator.execute();

    }

}
