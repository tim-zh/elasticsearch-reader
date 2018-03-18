import Util._

import scala.collection.mutable

object Main {

    def main(args: Array[String]): Unit = {
        def groupCountSort[T](xs: Array[T]) = xs.toSeq.groupBy(identity).mapValues(_.size).toArray.sortBy(_._2).reverse

        val config = Config.load()
        val client = new ElasticsearchClient(config)
        println(s"querying ${client.resultsLimit} entries in 5 sec")
        Thread.sleep(5000)

        writeFile(config.outputFile) { p =>
            if (config.countEntries) {
                val xs = new mutable.ArrayBuilder.ofRef[String]
                client.processMessages(xs += _)
                groupCountSort(xs.result()).foreach { x =>
                    p.println(s"${x._2} ${x._1}")
                }
            } else
                client.processMessages { x =>
                    p.println(x)
                }
        }
    }

}
