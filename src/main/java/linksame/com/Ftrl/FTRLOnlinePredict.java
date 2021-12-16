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
 * @Date: 2021/12/10 9:39
 */
public class FTRLOnlinePredict {

    @Test
    public void fTRLPredictOnline() throws Exception {
        StreamOperator.setParallelism(2);

        BatchOperator<?> trainData0 = new RandomTableSourceBatchOp()
                .setNumCols(5)
                .setNumRows(100L)
                .setOutputCols(new String[]{"f0", "f1", "f2", "f3", "label"})
                .setOutputColConfs("label:weight_set(1.0,1.0,2.0,5.0)");

        BatchOperator<?> model = new LogisticRegressionTrainBatchOp()
                .setFeatureCols(new String[]{"f0", "f1", "f2", "f3"})
                .setLabelCol("label")
                .setMaxIter(10).linkFrom(trainData0);

        StreamOperator<?> trainData1 = new RandomTableSourceStreamOp()
                .setNumCols(5)
                .setMaxRows(1000L)
                .setOutputCols(new String[]{"f0", "f1", "f2", "f3", "label"})
                .setOutputColConfs("label:weight_set(1.0,1.0,2.0,5.0)")
                .setTimePerSample(0.1);

        StreamOperator<?> sModel = new FtrlTrainStreamOp(model)
                .setFeatureCols(new String[]{"f0", "f1", "f2", "f3"})
                .setLabelCol("label")
                .setTimeInterval(10)
                .setAlpha(0.1)
                .setBeta(0.1)
                .setL1(0.1)
                .setL2(0.1)
                .setVectorSize(4)
                .setWithIntercept(true)
                .linkFrom(trainData1);

        new FtrlPredictStreamOp(model)
                .setPredictionCol("pred")
                .setReservedCols(new String[]{"label"})
                .setPredictionDetailCol("details")
                .linkFrom(sModel, trainData1)
                .print();

        StreamOperator.execute();
    }

}
