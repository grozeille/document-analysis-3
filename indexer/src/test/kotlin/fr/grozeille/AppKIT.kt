package fr.grozeille

import lombok.extern.slf4j.Slf4j
import mu.KotlinLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaSparkContext
import fr.grozeille.AppK.wordsCount
import org.junit.Test
import java.io.IOException


@Slf4j
class AppITK {

    private val log = KotlinLogging.logger {}

    @Test
    @Throws(IOException::class)
    fun test() {
        val conf = SparkConf().setAppName("demo-spark-test").setMaster("local[3]")
        val sc = JavaSparkContext(conf)
        val inputPath = "hdfs://192.168.0.151/user/root/wikipedia.sv.txt"
        val outputPath = "hdfs://192.168.0.151/user/root/wikipedia.fr.txt"

        val hadoopConf = Configuration()
        val fs = FileSystem.get(hadoopConf)
        if (fs.exists(Path(outputPath))) {
            fs.delete(Path(outputPath), true)
        }

        log.info("Trying to open: $inputPath")

        val lines = sc.textFile(inputPath)

        val result = lines.wordsCount()

        result.saveAsTextFile(outputPath)
    }
}