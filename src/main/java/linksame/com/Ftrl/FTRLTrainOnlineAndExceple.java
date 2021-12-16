package linksame.com.Ftrl;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.LogisticRegressionTrainBatchOp;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.operator.batch.source.TextSourceBatchOp;
import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.dataproc.SplitStreamOp;
import com.alibaba.alink.operator.stream.onlinelearning.FtrlPredictStreamOp;
import com.alibaba.alink.operator.stream.onlinelearning.FtrlTrainStreamOp;
import com.alibaba.alink.operator.stream.source.CsvSourceStreamOp;
import com.alibaba.alink.pipeline.Pipeline;
import com.alibaba.alink.pipeline.PipelineModel;
import com.alibaba.alink.pipeline.dataproc.StandardScaler;
import com.alibaba.alink.pipeline.feature.FeatureHasher;
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
public class FTRLTrainOnlineAndExceple {

    // 初始化 模型文件路径
    private static final String initModelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/model/FTRLLinearRegTrainModel.ak";
    // 初始化 数据文件路径
    private static final String initTrainPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/FTRLInitTrain.txt";
    // 在线训练 数据文件路径
    private static final String onlineTrainPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/FTRLOnlineTrain.txt";


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
                .setSchemaStr("f0 int,f1 int,f2 int,f3 int,label int")
                .setIgnoreFirstLine(true);

        // 打印前10条数据
        trainBatchData.firstN(10).print();
    }

    @Test
    public void fTRLInitModel() throws Exception {

        // 批式原始训练数据
        CsvSourceBatchOp trainBatchData = new CsvSourceBatchOp()
                .setFilePath("G:/Idea-Workspaces/AlinkExample/src/main/resources/static/FTRLInitTrain.txt")
                .setFieldDelimiter("|")
                .setSchemaStr("f0 int,f1 int,f2 int,f3 int,label int")
                .setIgnoreFirstLine(true);

        // 定义 特征工程 处理 pipeline(管道)
        Pipeline featurePipeline = new Pipeline()
                .add(
                        // 标准缩放 ( 数值特征标准化 )
                        new StandardScaler()
                                .setSelectedCols("f0", "f1", "f2", "f3", "label")
                )
                .add(
                        // 特征哈希 ( 将多个特征组合成一个特征向量 )
                        new FeatureHasher()
                                .setSelectedCols(new String[]{"f0,f1,f2,f3,label"})
                                //.setCategoricalCols(new String[]{"f0,f1,f2,f3,label"})
                                .setOutputCol("vec")
                                .setNumFeatures(30000)
                );

        // 构建特征工程流水线 - 对批式训练数据 trainBatchData 执行 fit 方法，及进行训练，得到 PipelineModel(管道模型)

        // 初始模型允许覆盖重写
        featurePipeline.fit(trainBatchData).save(initModelPath,true);

        // 批处理执行
        BatchOperator.execute();

        /* ------------------------------------------------------------------------------------- */

        // 准备流式训练数据
        CsvSourceStreamOp data = new CsvSourceStreamOp()
                //.setFilePath("http://alink-release.oss-cn-beijing.aliyuncs.com/data-files/avazu-ctr-train-8M.csv")
                .setFilePath("G:/Idea-Workspaces/AlinkExample/src/main/resources/static/FTRLOnlineTrain.txt")
                .setFieldDelimiter("|")
                .setSchemaStr("f0 int,f1 int,f2 int,f3 int,label int")
                .setIgnoreFirstLine(true);

        // 定义一个流式数据源，并按1:1的比例实时切分数据，从而得到 : 流式原始训练数据、流式原始预测数据
        SplitStreamOp split = new SplitStreamOp()
                .setFraction(0.5)
                .linkFrom(data);

        // 流式原始训练数据
        StreamOperator<?> trainStreamData = split;
        // 流式原始预测数据
        StreamOperator<?> testStreamData = split.getSideOutput(0);

        // 通过PipelineModel.load()方法，可以载入前面保存的特征工程处理模型
        PipelineModel featurePipelineModel = PipelineModel.load(initModelPath);

        /* ------------ 分别利用模型生成批式训练数据，流式训练数据，流式测试数据--------------------- */

        // 训练出一个逻辑回归模型做为FTRL算法的初始模型，这是为了系统冷启动的须要。
        // 定义逻辑回归分类器 lr
        LogisticRegressionTrainBatchOp lr = new LogisticRegressionTrainBatchOp()
                .setVectorCol("vec")            // 向量列名
                .setLabelCol("label")           // 标签列名
                .setWithIntercept(true)         // 是否有常数项
                .setMaxIter(10);                // 最大迭代步数

        // 连接批处理数据
        BatchOperator<?> initModel = featurePipelineModel.transform(trainBatchData).link(lr);

        /* ------------------------------ FTRL模型训练及预测评估 ------------------------------ */

        // ftrl train ( 在初始模型基础上进行 FTRL 流式在线训练 )
        /*FtrlTrainStreamOp model = new FtrlTrainStreamOp(initModel)
                .setVectorCol("vec")            // 特征向量名
                .setLabelCol("label")           // 标签列名
                .setWithIntercept(true)         // 有常数项
                .setAlpha(0.1)                  // 参数α的值
                .setBeta(0.1)                   // 参数β的值
                .setL1(0.01)                    // L1 正则化系数
                .setL2(0.01)                    // L2 正则化系数
                .setTimeInterval(10)            // 数据流流动过程中时间的间隔（窗口大小）
                .setVectorSize(30000)           // 向量长度
                // 模型 连接 流式向量训练数据
                .linkFrom(featurePipelineModel.transform(trainStreamData));*/

        // ftrl predict ( 在 FTRL 在线模型的基础上，链接预测数据进行预测 )
        /*FtrlPredictStreamOp predResult = new FtrlPredictStreamOp(initModel)
                .setVectorCol("vec")            // 向量列名
                .setPredictionCol("pred")       // 预测结果列名
                .setReservedCols(new String[] {"label"})            // 算法保留列名
                .setPredictionDetailCol("details")                  // 预测详细信息列名
                .linkFrom(model, featurePipelineModel.transform(testStreamData));   // 模型 连接 模型流 和 流式测试数据
        // 取样输出
        predResult.sample(0.0001).print();*/

        // 对于流式的任务，print()方法不能触发流式任务的执行，必须调用StreamOperator.execute()方法，才能开始执行
        // StreamOperator.execute();
    }

}
