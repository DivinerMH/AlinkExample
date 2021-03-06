package linksame.com.LinearRegTrain;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.regression.LinearRegTrainBatchOp;
import com.alibaba.alink.operator.batch.sink.CsvSinkBatchOp;
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
public class LinearRegTrainSaveCSVModel {

    @Test
    public void linearRegTrainBatchOpTest() throws Exception {

        /* --------------- 警告：模型文件以 csv 格式存储，加载会出现异常，请用 ak 格式存储 --------------- */
        // 模型文件路径
        String modelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/model/LinearRegTrainMode.csv";

        // 训练文件路径 = 静态资源路径+文件目录路径
        String trainPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/LinearRegTrain.txt";

        // 格式
        String schema = "f0 int,f1 int,f2 int,f3 int,label int";

        // 训练资源
        BatchOperator <?> trainSource = new CsvSourceBatchOp()
                .setFilePath(trainPath)
                .setFieldDelimiter("|")
                .setSchemaStr(schema)
                //.setSchemaStr("label int , review string")
                .setIgnoreFirstLine(true);

        System.out.println("数据源配置构建完成 ========================================================================");

        // 打印前5天数据源信息 [trainSource.print().firstN(5) 限制条件会在生效前完成打印;]
        trainSource.firstN(5).print();

        // 线性回归算法 初始化
        BatchOperator <?> lr = new LinearRegTrainBatchOp()
                .setFeatureCols("f0","f1","f2","f3")
                .setLabelCol("label");

        // 批处理操作( 数据 link 算法)
        BatchOperator model = trainSource.link(lr);

        // 保存模型：训练模型写入CSV文件【允许重写】
        CsvSinkBatchOp csvSink = new CsvSinkBatchOp()
                .setFilePath(modelPath)
                .setOverwriteSink(true);

        model.link(csvSink);

        // 执行批处理
        BatchOperator.execute();
    }

}
