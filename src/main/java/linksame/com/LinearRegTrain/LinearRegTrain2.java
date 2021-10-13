package linksame.com.LinearRegTrain;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.regression.LinearRegPredictBatchOp;
import com.alibaba.alink.operator.batch.regression.LinearRegTrainBatchOp;
import com.alibaba.alink.operator.batch.sink.CsvSinkBatchOp;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 测试线性回归三元一次方程
 *      5X + 2Y + Z + t = O
 *          三元：X，Y，Z
 *          常量：t
 *          结果：O
 * @Author: menghuan
 * @Date: 2021/10/12 10:36
 */
public class LinearRegTrain2 {

    @Test
    public void LinearRegTrainModelTrain() throws Exception {

        String trainFilePath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/train/" + UUID.randomUUID().toString() + ".csv";
        String trainModelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/model/" + UUID.randomUUID().toString() + ".csv";

        // 构建训练数据
        List<Row> dataSource = Arrays.asList(
                Row.of(2, 1, 1, 9, 22),
                Row.of(3, 2, 1, 9, 29),
                Row.of(4, 3, 2, 9, 37),
                Row.of(2, 4, 1, 9, 28),
                Row.of(2, 2, 1, 9, 24),
                Row.of(4, 3, 2, 9, 37),
                Row.of(1, 2, 1, 9, 19),
                Row.of(5, 3, 3, 9, 43)
        );
        // String schema = "f0 int, f1 int, f2 int, f3 int, label int";
        String[] schema = new String[]{"f0", "f1", "f2", "f3", "label"};
        // String[] schema = new String[]{"f0 int","f1 int","f2 int","f3 int","label int"};

        // 数据处理【内存数据】
        // BatchOperator<?> batchData = new MemSourceBatchOp(dataSource, "f0 int, f1 int, label int");
        BatchOperator <?> batchSource = new MemSourceBatchOp(dataSource,schema);

        // 线性回归 训练初始化
        BatchOperator <?> lr = new LinearRegTrainBatchOp()
                .setFeatureCols("f0", "f1", "f2", "f3")
                .setLabelCol("label");
        // 线性回归 训练
        BatchOperator model = batchSource.link(lr);

        // 线性回归 预测初始化
        BatchOperator <?> predictor = new LinearRegPredictBatchOp()
                .setPredictionCol("pred");
        // 线性回归 预测
        predictor.linkFrom(model, batchSource)
                // .select(new String[] {"f0", "f1", "f2", "f3", "pred"})
                // .firstN(5)
                .print();

        // 结果集保存至Csv文件
        CsvSinkBatchOp csvSink = new CsvSinkBatchOp()
                .setFilePath(trainFilePath);
                // .linkFrom(predictor);
        predictor.link(csvSink);

        // 保存模型
        CsvSinkBatchOp csvSinkModel = new CsvSinkBatchOp()
                .setFilePath(trainModelPath);
                //.linkFrom(model);
        model.link(csvSinkModel);

        // 批处理执行（不加此行代码，当前情景，保存文件步骤会无法执行...）
        BatchOperator.execute();
    }

    @Test
    public void LinearRegTrainModelPredictor() throws Exception {
        // 模型文件路径
        String trainModelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/model/e2068056-b512-499f-bca7-45d6c6e47157.csv";

        String schemaStr = "f0 int,f1 int,f2 int,f3 int";

        // 加载模型
        CsvSourceBatchOp csvSourceModel = new CsvSourceBatchOp()
                .setFilePath(trainModelPath)
                .setSchemaStr(schemaStr);

        // 测试数据
        List<Row> dataSource = Arrays.asList(
                Row.of(7, 1, 5, 9),
                Row.of(3, 8, 1, 9)
        );
        String[] schema = new String[]{"f0", "f1", "f2", "f3"};
        BatchOperator <?> batchSource = new MemSourceBatchOp(dataSource,schema);

        // 预测初始化
        BatchOperator <?> predictor = new LinearRegPredictBatchOp()
                .setPredictionCol("pred");

        // 预测
        BatchOperator<?> resultOp = predictor.linkFrom(csvSourceModel, batchSource);

        // 预测结果 打印
        resultOp.print();

        // BatchOperator.execute();
    }

}
