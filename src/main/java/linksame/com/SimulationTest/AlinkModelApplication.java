package linksame.com.SimulationTest;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.pipeline.PipelineModel;


/**
 * alink：批式训练和保存模型，流式消费和分类文本
 * https://blog.csdn.net/asdf1368822590/article/details/118370000
 * @Author: menghuan
 * @Date: 2021/9/3 17:47
 */
public class AlinkModelApplication {

    public static void main(String[] args) throws Exception {
        // 模型文件路径
        String modelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/model.csv";
        // 加载模型文件
        PipelineModel model = PipelineModel.load(modelPath);
        // 预测文件路径
        String predictorPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/train3.txt";

        CsvSourceBatchOp predictorResource = new CsvSourceBatchOp()
                .setFilePath(predictorPath)
                .setFieldDelimiter("|")
                .setSchemaStr("review string")
                .setIgnoreFirstLine(true);

        model.transform(predictorResource)
                .select(new String[] {"pred","review"})
                .firstN(5)
                .print();

        // BatchOperator.execute();

    }

}
