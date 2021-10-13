package linksame.com.LinearRegTrain;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.LogisticRegressionTrainBatchOp;
import com.alibaba.alink.operator.batch.regression.LinearRegPredictBatchOp;
import com.alibaba.alink.operator.batch.regression.LinearRegTrainBatchOp;
import com.alibaba.alink.operator.batch.sink.CsvSinkBatchOp;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.pipeline.PipelineModel;
import org.junit.Test;

/**
 * 测试线性回归三元一次方程
 *      5X + 2Y + Z + t = O
 *          三元：X，Y，Z
 *          常量：t
 *          结果：O
 * @Author: menghuan
 * @Date: 2021/10/12 10:36
 */
public class LinearRegTrainLoadModel {

    @Test
    public void LinearRegTrainBatchOpTest() throws Exception {

        // 模型文件路径
        String modelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/LinearRegTrainModel.csv";

        // 训练文件路径 = 静态资源路径+文件目录路径
        String trainPath_2 = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/LinearRegTrain3.txt";

        // 格式
        String schema = "f0 int,f1 int,f2 int,f3 int,label int";

        // 预测资源
        BatchOperator <?> trainSource = new CsvSourceBatchOp()
                .setFilePath(trainPath_2)
                .setFieldDelimiter("|")
                .setSchemaStr(schema)
                .setIgnoreFirstLine(true);

        // 加载模型文件
        CsvSinkBatchOp linearRegPredictModel = new CsvSinkBatchOp()
                .setFilePath(modelPath);

        System.out.println("开始执行线性回归预测 ======================================================================");

        // 线性回归预测初始化
        BatchOperator <?> predictor = new LinearRegPredictBatchOp()
                .setPredictionCol("pred");
        // 线性回归预测
        BatchOperator <?> o = predictor.linkFrom(linearRegPredictModel, trainSource);
        System.out.println("预测结果数据 =============================================================================");
        o.print();
    }

}
