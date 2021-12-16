package linksame.com.Ftrl;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.LogisticRegressionTrainBatchOp;
import com.alibaba.alink.operator.batch.source.RandomTableSourceBatchOp;
import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.onlinelearning.FtrlPredictStreamOp;
import com.alibaba.alink.operator.stream.onlinelearning.FtrlTrainStreamOp;
import com.alibaba.alink.operator.stream.source.RandomTableSourceStreamOp;
import org.junit.Test;

/**
 * @Author: menghuan
 * @Date: 2021/12/10 9:37
 */
public class FTRLOnlineTrain {

    @Test
    public void fTRLTrainOnline() throws Exception {

        StreamOperator.setParallelism(2);

        BatchOperator<?> batchData = new RandomTableSourceBatchOp()
                .setNumCols(5)              // 随机构建数据的列数
                .setNumRows(20L)            // 随机构建数据的max行数
                .setOutputCols(new String[]{"f0", "f1", "f2", "f3", "label"})   // 输出列名数组(字符串数组，当参数不设置时，算法自动生成)
                .setOutputColConfs("label:weight_set(1.0,1.0,2.0,5.0)");        // 列配置信息(表示每一列的数据分布配置信息)

        // 逻辑回归训练 (批租件-分类)
        LogisticRegressionTrainBatchOp model = new LogisticRegressionTrainBatchOp()
                .setFeatureCols(new String[]{"f0", "f1", "f2", "f3"})           // 特征列名数组(默认全选)
                .setLabelCol("label")       // 标签列名(输入表中的标签列名)
                .setMaxIter(10)             // 最大迭代步数(默认为 100)
                .linkFrom(batchData);

        StreamOperator<?> streamData = new RandomTableSourceStreamOp()
                .setNumCols(5)              // 随机构建数据的列数
                .setMaxRows(100L)           // 随机构建数据的max行数
                .setOutputCols(new String[]{"f0", "f1", "f2", "f3", "label"})   // 输出列名数组(字符串数组，当参数不设置时，算法自动生成)
                .setOutputColConfs("label:weight_set(1.0,1.0,2.0,5.0)")         // 列配置信息(表示每一列的数据分布配置信息)
                .setTimePerSample(0.1);     // 每条样本流过的时间(每两条样本间的时间间隔，单位秒)

        // streamData.print();

        // Ftrl在线训练
        // FtrlTrainStreamOp sModel = new FtrlTrainStreamOp(model) 替换也OK
        StreamOperator<?> sModel = new FtrlTrainStreamOp(model)
                .setFeatureCols(new String[]{"f0", "f1", "f2", "f3"})   // 特征向量名
                .setLabelCol("label")                                   // 标签列名
                .setWithIntercept(true)     // 有常数项
                .setAlpha(0.1)              // 参数α的值
                .setBeta(0.1)               // 参数β的值
                .setL1(0.1)                 // L1 正则化系数
                .setL2(0.1)                 // L2 正则化系数
                .setTimeInterval(10)        // 数据流流动过程中时间的间隔（窗口大小）
                .setVectorSize(4)           // 向量长度
                .linkFrom(streamData);      // 模型 连接 流式向量训练数据

        // Ftrl在线预测
        new FtrlPredictStreamOp(model)
                .setPredictionCol("pred")   // 预测结果列名
                .setReservedCols(new String[]{"label"})     // 算法保留列名
                .setPredictionDetailCol("details")          // 预测详细信息列名
                .linkFrom(sModel, streamData)
                .print();

        StreamOperator.execute();
    }

}
