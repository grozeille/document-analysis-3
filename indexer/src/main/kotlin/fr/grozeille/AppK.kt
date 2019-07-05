package fr.grozeille


import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import lombok.extern.slf4j.Slf4j
import mu.KotlinLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.util.AttributeFactory
import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaRDD
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.api.java.function.FlatMapFunction
import org.apache.spark.api.java.function.Function

import java.io.IOException
import java.io.StringReader
import java.util.ArrayList
import java.util.Arrays

object AppK {
    private val log = KotlinLogging.logger {}

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val conf = SparkConf().setAppName("demo-spark")
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
        /*
        List<String> collect = translated.collect();
        String stringTranslated = String.join("\n", collect);
        FSDataOutputStream fsDataOutputStream = fs.create(new Path(outputPath), true);
        fsDataOutputStream.writeUTF(stringTranslated);
        fsDataOutputStream.flush();
        fsDataOutputStream.close();*/

        sc.stop()
    }

    fun JavaRDD<String>.translate(): JavaRDD<String> {
        // TODO https://github.com/GoogleCloudPlatform/cloud-dataproc/tree/master/spark-translate

        return this.map { s : String ->
            val translate = TranslateOptions.getDefaultInstance().service
            val translation = translate.translate(
                    s,
                    Translate.TranslateOption.sourceLanguage("sv"),
                    Translate.TranslateOption.targetLanguage("fr"),
                    // Use "base" for standard edition, "nmt" for the premium model.
                    Translate.TranslateOption.model("base"))
            translation.translatedText
        }
    }

    fun JavaRDD<String>.wordsCount(): JavaRDD<String> {
        val tokenRdd = this.map { s:String ->
            val factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY

            val tokenizer = StandardTokenizer(factory)
            tokenizer.setReader(StringReader(s))
            tokenizer.reset()

            val result = ArrayList<String>()
            val attr = tokenizer.addAttribute(CharTermAttribute::class.java)
            while (tokenizer.incrementToken()) {
                val term = attr.toString()

                result.add(term)
            }
            result.toTypedArray()
        }

        val words = tokenRdd.flatMap { strings: Array<String> -> Arrays.asList<String>(*strings).iterator() }

        return words.distinct()
    }
}