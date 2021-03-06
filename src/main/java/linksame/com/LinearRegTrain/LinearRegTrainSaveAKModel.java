package linksame.com.LinearRegTrain;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.regression.LinearRegTrainBatchOp;
import com.alibaba.alink.operator.batch.sink.AkSinkBatchOp;
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
public class LinearRegTrainSaveAKModel {

    @Test
    public void linearRegTrainBatchOpTest() throws Exception {

        // 模型文件路径
        String modelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/model/LinearRegTrainAKModel.ak";

        // 训练文件路径 = 静态资源路径+文件目录路径
        String trainPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/LinearRegTrain.txt";
        // String trainPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/LinearRegTrainAKModel2.ak";

        // 格式
        String schema = "f0 int,f1 int,f2 int,f3 int,label int";

        // 训练资源
        BatchOperator <?> trainSource = new CsvSourceBatchOp()
                .setFilePath(trainPath)
                .setFieldDelimiter("|")
                .setSchemaStr(schema)
                .setIgnoreFirstLine(true);

        System.out.println("数据源配置构建完成 ========================================================================");

        // 打印前5天数据源信息 [trainSource.print().firstN(5) 限制条件会在生效前完成打印;]
        trainSource.firstN(5).print();

        // 线性回归算法 初始化
        BatchOperator <?> lr = new LinearRegTrainBatchOp()
                .setFeatureCols("f0", "f1", "f2", "f3")
                .setLabelCol("label")
                .linkFrom(trainSource);

        // 保存模型：训练模型写入ak文件【允许重写】
        AkSinkBatchOp akSinkBatchOp = new AkSinkBatchOp()
                .setFilePath(modelPath)
                .setOverwriteSink(true)
                .linkFrom(lr);

        // 执行批处理
        BatchOperator.execute();
    }

    /**
     * 加载 | 预览 AK 模型文件
     * @throws Exception
     */
    @Test
    public void previewAKModel() throws Exception {
        String trainPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/LinearRegTrainAKModel2.ak";
        AkSourceBatchOp akSourceBatchOp = new AkSourceBatchOp().setFilePath(trainPath);
        akSourceBatchOp.print();
    }

}
