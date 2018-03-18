import Json._
import Util._

class ElasticsearchClient(config: Config) {

    private def body(from: Int, size: Int, queryStr: String, minDate: Long, maxDate: Long) =
        s"""{
  "query": {
    "filtered": {
      "query": {
        "query_string": {
          "analyze_wildcard": true,
          "query": "$queryStr"
        }
      },
      "filter": {
        "bool": {
          "must": [
            {
              "range": {
                "@timestamp": {
                  "gte": $minDate,
                  "lte": $maxDate
                }
              }
            }
          ]
        }
      }
    }
  },
  "from": $from,
  "size": $size,
  "fields": [
    "${config.field}"
  ]
}"""

    private def getHitsTotal() = {
        val response = httpCall(config.url, body(0, 0, config.queryStr, config.start, config.end))
        (Wrapper(new Parser().parse(response)) / "hits" / "total").asDouble.get.toInt
    }

    private def getHits(body: String, timeout: Int = config.queryTimeout) = {
        val response = httpCall(config.url, body, timeout)
        (Wrapper(new Parser().parse(response)) / "hits" / "hits").asWrappedArray.get
    }

    private def queryPaging[T](doQuery: (Int, Int) => T, limit: Int, pageSize: Int, start: Int = 0)
                              (processResult: T => Unit) =
        for (i <- 0 until ((limit - start) / pageSize + 1)) {
            val time = System.currentTimeMillis()
            val result = doQuery(i * pageSize + start, pageSize)
            val diff = System.currentTimeMillis() - time
            println(diff + " ms")
            processResult(result)
            println(s"page ${i + 1} from ${(limit - start) / pageSize + 1}")
        }

    def processMessages(processResult: String => Unit): Unit = {
        val q = (from: Int, size: Int) => getHits(body(from, size, config.queryStr, config.start, config.end))
        queryPaging(q, resultsLimit, config.pageSize)(_.foreach { x =>
            try {
                val str = (x / "fields" / config.field).asArray.get.head.asInstanceOf[String]
                processResult(str)
            } catch {
                case _: Throwable =>
                    println("no message found")
                    println(x.parsed)
            }
        })
    }

    def resultsLimit: Int =
        if (config.resultsLimit == 0)
            getHitsTotal()
        else
            config.resultsLimit

}
