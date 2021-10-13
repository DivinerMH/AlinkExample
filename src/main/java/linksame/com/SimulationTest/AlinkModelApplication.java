package linksame.com.SimulationTest;

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




/*        KafkaSourceStreamOp kafkaSourceStreamOp = new KafkaSourceStreamOp()
                .setBootstrapServers("127.0.0.1:9092")
                .setStartupMode("latest")
                .setGroupId("test")
                .setTopic("sentiment");

        StreamOperator data = kafkaSourceStreamOp
                .link(
                        new JsonValueStreamOp()
                                .setSelectedCol("message")
                                .setOutputCols(new String[]{"review","user_id", "role_name", "role_id"})
                                .setJsonPath(new String[]{"chat_content", "user_id", "role_name", "role_id"})
                );

        PipelineModel pipelineModel = PipelineModel.load(modelPath);

        pipelineModel.transform(data)
                .select(new String[]{"review", "user_id", "role_name", "role_id","pred"})
                .print();
        StreamOperator.execute();*/
    }


}
