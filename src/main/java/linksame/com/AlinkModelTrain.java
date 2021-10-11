package linksame.com;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.pipeline.Pipeline;
import com.alibaba.alink.pipeline.PipelineModel;
import com.alibaba.alink.pipeline.classification.LogisticRegression;
import com.alibaba.alink.pipeline.dataproc.Imputer;
import com.alibaba.alink.pipeline.nlp.DocCountVectorizer;
import com.alibaba.alink.pipeline.nlp.Segment;
import com.alibaba.alink.pipeline.nlp.StopWordsRemover;

/**
 * 示例：alink：批式训练和保存模型，流式消费和分类文本
 *      1、https://blog.csdn.net/asdf1368822590/article/details/118370000
 * 示例：情感分析
 *      1、https://blog.csdn.net/Alink1024/article/details/107811435
 *      2、方法解释说明
 *
 * @Author: menghuan
 * @Date: 2021/9/3 17:47
 */

public class AlinkModelTrain {

    public static void main(String[] args) throws Exception {
        System.out.println("开始构建离线批处理训练执行环境 =========================================================");

        // 模型文件路径
        String modelPath = "G:/Idea-Workspaces/AlinkExample/src/main/resources/model.csv";
        // 训练文件路径 = 静态资源路径+文件目录路径
        String train_path = "G:/Idea-Workspaces/AlinkExample/src/main/resources/static/train.txt";

        // 训练资源
        CsvSourceBatchOp trainSource = new CsvSourceBatchOp()
                .setFilePath(train_path)
                .setFieldDelimiter("|")
                .setSchemaStr("label int , review string")
                .setIgnoreFirstLine(true);

        // 选择5条数据打印显示出来
        // trainSource.firstN(5).print();

        // 设置 Pipeline，将整个处理和模型过程封装在里面
        Pipeline pipeline = new Pipeline(
                /*各个算法组件的作用*/
                // 对“review”列进行缺失值填充，方式是填充字符串值“null”，结果写到“featureText“列。
                new Imputer()
                        .setSelectedCols("review")
                        .setOutputCols("featureText")
                        .setStrategy("value")
                        .setFillValue("null"),
                // 是进行分词操作，即将原句子分解为单词，之间用空格分隔。由于没有输入结果列，分词结果会直接替换掉输入列的值。
                new Segment()
                        .setSelectedCol("featureText"),
                // 是将分词结果中的停用词去掉
                new StopWordsRemover()
                        .setSelectedCol("featureText"),
                // 对“featureText“列出现的单词进行统计，并根据计算出的TF值，将句子映射为向量，向量长度为单词个数，并保存在"featureVector"列
                new DocCountVectorizer()
                        .setFeatureType("TF")
                        .setSelectedCol("featureText")
                        .setOutputCol("featureVector"),
                // 是使用LogisticRegression分类模型。分类预测放在“pred” 列
                new LogisticRegression()
                        .setVectorCol("featureVector")
                        .setLabelCol("label")
                        .setPredictionCol("pred")
        );
        // 进入模型训练阶段
        // 通过 Pipeline 的 fit()方法，可以得到整个流程的模型（PipelineModel），记作变量 model
        PipelineModel model = pipeline.fit(trainSource);

        // 保存训练的模型文件
        // model.save(modelPath);
        // 覆写训练的模型文件
        model.save(modelPath,true);

        System.out.println("离线批处理训练开始执行 ================================================================");
        BatchOperator.execute();

    }

}
