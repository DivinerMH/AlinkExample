package linksame.com.LinearRegTrain;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.regression.LinearRegPredictBatchOp;
import com.alibaba.alink.operator.batch.source.AkSourceBatchOp;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
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
public class LinearRegTrainLoadAKModel {

    @Test
    public void linearRegTrainBatchOpTest() throws Exception {

        // 模型文件路径
        String modelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/model/LinearRegTrainAKModel.ak";

        // 训练文件路径 = 静态资源路径+文件目录路径
        String trainPath_2 = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/LinearRegTrain2.txt";

        System.out.println("加载预测数据 =============================================================================");

        // 预测数据源
        BatchOperator <?> predictorSource = new CsvSourceBatchOp()
                .setFilePath(trainPath_2)
                .setFieldDelimiter("|")
                .setSchemaStr("f0 int,f1 int,f2 int,f3 int")
                .setIgnoreFirstLine(true);
        predictorSource.print();

        // 加载模型文件
        /*CsvSinkBatchOp linearRegPredictModel = new CsvSinkBatchOp()
                .setFilePath(modelPath);*/

        /*CsvSourceBatchOp model = new CsvSourceBatchOp()
                .setFilePath(modelPath)
                .setSchemaStr("f0 int,f1 int,f2 int,f3 int,label int");*/

        AkSourceBatchOp model = new AkSourceBatchOp()
                .setFilePath(modelPath);

        System.out.println("加载模型 - 执行线性回归预测 ===============================================================");

        // 线性回归 预测初始化
        BatchOperator <?> predictor = new LinearRegPredictBatchOp()
                .setPredictionCol("pred");

        // 线性回归 预测
        BatchOperator <?> result = predictor.linkFrom(model, predictorSource);
        result.print();
    }

}
