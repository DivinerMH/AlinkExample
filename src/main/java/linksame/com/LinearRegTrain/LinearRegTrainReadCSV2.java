package linksame.com.LinearRegTrain;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.regression.LinearRegPredictBatchOp;
import com.alibaba.alink.operator.batch.regression.LinearRegTrainBatchOp;
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
public class LinearRegTrainReadCSV2 {

    @Test
    public void LinearRegTrainBatchOpTest() throws Exception {

        // 模型文件路径
        String modelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/LinearRegTrainModel.csv";

        // 训练文件路径 = 静态资源路径+文件目录路径
        String trainPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/LinearRegTrain.txt";

        // 格式
        String schema = "f0 int,f1 int,f2 int,f3 int,label int";

        // 特征值
        String[] featureCols = new String[]{"f0", "f1", "f2", "f3"};

        // 训练资源
        BatchOperator <?> trainSource = new CsvSourceBatchOp()
                .setFilePath(trainPath)
                .setFieldDelimiter("|")
                .setSchemaStr(schema)
                .setIgnoreFirstLine(true);

        System.out.println("数据源配置构建完成 ========================================================================");

        // 打印前5天数据源信息
        trainSource.firstN(5).print();

        // 线性回归算法 初始化
        BatchOperator <?> lr = new LinearRegTrainBatchOp()
                .setFeatureCols(featureCols)
                .setLabelCol("label")
                .linkFrom(trainSource);

        System.out.println("开始执行线性回归预测 ======================================================================");

        // 线性回归预测 初始化
        BatchOperator <?> predictor = new LinearRegPredictBatchOp()
                .setPredictionCol("pred");
        // 线性回归预测
        predictor.linkFrom(lr, trainSource)
                .print();
    }

}
