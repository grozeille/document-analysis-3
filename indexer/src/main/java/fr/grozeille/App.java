package fr.grozeille;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class App 
{
    public static void main( String[] args ) throws IOException {
        SparkConf conf = new SparkConf().setAppName("demo-spark");
        JavaSparkContext sc = new JavaSparkContext(conf);
        String inputPath = "hdfs://192.168.0.151/user/root/wikipedia.sv.txt";
        String outputPath = "hdfs://192.168.0.151/user/root/wikipedia.fr.txt";

        Configuration hadoopConf = new Configuration();
        FileSystem fs = FileSystem.get(hadoopConf);
        if(fs.exists(new Path(outputPath))) {
            fs.delete(new Path(outputPath), true);
        }

        log.info("Trying to open: " + inputPath);

        JavaRDD<String> lines = sc.textFile(inputPath);

        JavaRDD<String> result = App.wordsCount(lines);

        result.saveAsTextFile(outputPath);
        /*
        List<String> collect = translated.collect();
        String stringTranslated = String.join("\n", collect);
        FSDataOutputStream fsDataOutputStream = fs.create(new Path(outputPath), true);
        fsDataOutputStream.writeUTF(stringTranslated);
        fsDataOutputStream.flush();
        fsDataOutputStream.close();*/

        sc.stop();
    }

    public static JavaRDD<String> translate(JavaRDD<String> lines) {
        // TODO https://github.com/GoogleCloudPlatform/cloud-dataproc/tree/master/spark-translate

        JavaRDD<String> translated = lines.map((Function<String, String>) s -> {
            Translate translate = TranslateOptions.getDefaultInstance().getService();
            Translation translation =
                    translate.translate(
                            s,
                            Translate.TranslateOption.sourceLanguage("sv"),
                            Translate.TranslateOption.targetLanguage("fr"),
                            // Use "base" for standard edition, "nmt" for the premium model.
                            Translate.TranslateOption.model("base"));
            return translation.getTranslatedText();
        });

        return translated;
    }

    public static JavaRDD<String> wordsCount(JavaRDD<String> lines) {
        JavaRDD<String[]> tokenRdd = lines.map((Function<String, String[]>) s -> {
            AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;

            StandardTokenizer tokenizer = new StandardTokenizer(factory);
            tokenizer.setReader(new StringReader(s));
            tokenizer.reset();

            List<String> result = new ArrayList<>();
            CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
            while (tokenizer.incrementToken()) {
                String term = attr.toString();

                result.add(term);
            }
            return result.toArray(new String[0]);
        });

        JavaRDD<String> words = tokenRdd.flatMap((FlatMapFunction<String[], String>) strings -> Arrays.asList(strings).iterator());

        return words.distinct();
    }
}
