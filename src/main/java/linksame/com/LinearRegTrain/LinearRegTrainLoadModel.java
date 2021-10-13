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
        String modelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/LinearRegTrainModel2.csv";

        // 训练文件路径 = 静态资源路径+文件目录路径
        String trainPath_2 = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/LinearRegTrain2.txt";

        // 预测数据源
        BatchOperator <?> predictorSource = new CsvSourceBatchOp()
                .setFilePath(trainPath_2)
                .setFieldDelimiter("|")
                .setSchemaStr("f0 int,f1 int,f2 int,f3 int")
                .setIgnoreFirstLine(true);

        // 加载模型文件
        /*CsvSinkBatchOp linearRegPredictModel = new CsvSinkBatchOp()
                .setFilePath(modelPath);*/

        CsvSourceBatchOp linearRegPredictModel = new CsvSourceBatchOp()
                .setFilePath(modelPath)
                .setSchemaStr("f0 int,f1 int,f2 int,f3 int,label int");


        System.out.println("开始执行线性回归预测 ======================================================================");

        // 线性回归 预测初始化
        BatchOperator <?> predictor = new LinearRegPredictBatchOp()
                .setPredictionCol("pred");

        // 线性回归 预测
        BatchOperator <?> result = predictor
                .linkFrom(linearRegPredictModel, predictorSource);

        System.out.println("预测结果数据 =============================================================================");

        result.print();
    }

}
