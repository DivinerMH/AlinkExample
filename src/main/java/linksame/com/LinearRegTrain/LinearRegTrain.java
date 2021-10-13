package linksame.com.LinearRegTrain;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.regression.LinearRegPredictBatchOp;
import com.alibaba.alink.operator.batch.regression.LinearRegTrainBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * 测试线性回归三元一次方程
 *      5X + 2Y + Z + t = O
 *          三元：X，Y，Z
 *          常量：t
 *          结果：O
 * @Author: menghuan
 * @Date: 2021/10/12 10:36
 */
public class LinearRegTrain {

    @Test
    public void LinearRegTrainBatchOpTest() throws Exception {

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
        // TypeInformation<?>[] colTypes = new TypeInformation<?>[]{};

        // 数据处理
        // BatchOperator<?> batchData = new MemSourceBatchOp(dataSource, "f0 int, f1 int, label int");
        BatchOperator <?> batchSource = new MemSourceBatchOp(dataSource,schema);

        // 线性回归算法配置
        BatchOperator <?> lr = new LinearRegTrainBatchOp()
                .setFeatureCols("f0", "f1", "f2", "f3")
                .setLabelCol("label");
        BatchOperator model = batchSource.link(lr);

        // 线性回归预测初始化
        BatchOperator <?> predictor = new LinearRegPredictBatchOp()
                .setPredictionCol("pred");
        // 线性回归预测
        predictor.linkFrom(model, batchSource).print();
    }

}
