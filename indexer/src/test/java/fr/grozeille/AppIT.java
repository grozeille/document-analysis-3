package fr.grozeille;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class AppIT {

    @Test
    public void test() throws IOException {
        //String path = "hdfs://192.168.0.151/user/root/systems.csv";
        String inputPath = "hdfs://192.168.0.151/user/root/wikipedia.txt";
        String outputPath = "hdfs://192.168.0.151/user/root/wikipedia.en.txt";
        SparkConf conf = new SparkConf().setAppName("demo-spark-test").setMaster("local[3]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        Configuration hadoopConf = new Configuration();
        FileSystem fs = FileSystem.get(hadoopConf);
        if(fs.exists(new Path(outputPath))) {
            fs.delete(new Path(outputPath), true);
        }

        log.info("Trying to open: " + inputPath);

        JavaRDD<String> lines = sc.textFile(inputPath);

        JavaRDD<String> result = App.wordsCount(lines);

        result.saveAsTextFile(outputPath);
    }
}
