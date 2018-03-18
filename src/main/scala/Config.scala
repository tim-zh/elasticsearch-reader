import java.sql.Timestamp

object Config {

    def load(): Config = {
        val map = Util.loadProperties()
        Config(
            map("url"),
            Integer.valueOf(map("results.limit")),
            Integer.valueOf(map("results.page.size")),
            Integer.valueOf(map("query.timeout")),
            map("query.field"),
            map("query.string"),
            Timestamp.valueOf(map("query.start")).getTime,
            Timestamp.valueOf(map("query.end")).getTime,
            map("output.file"),
            map("count") == "true"
        )
    }
}

case class Config(
                         url: String,
                         resultsLimit: Int,
                         pageSize: Int,
                         queryTimeout: Int,
                         field: String,
                         queryStr: String,
                         start: Long,
                         end: Long,
                         outputFile: String,
                         countEntries: Boolean
                 )
