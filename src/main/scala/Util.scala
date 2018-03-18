object Util {

    def loadProperties(): Map[String, String] =
        scala.io.Source.fromFile("config.properties").getLines()
        .map(_.split("=", 2))
        .map(xs => (xs(0), xs(1)))
        .toMap

    def writeFile(file: String)(p: java.io.PrintWriter => Unit): Unit = {
        val pw = new java.io.PrintWriter(file, "UTF-8")
        try
            p(pw)
        finally
            pw.close()
    }

    def httpCall(url: String, body: String, timeout: Int = 0): String = {
        import java.io._
        import java.net._

        val connection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]

        try {
            connection.setRequestMethod("POST")
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
            connection.setRequestProperty("Content-Length", body.getBytes.length.toString)
            connection.setUseCaches(false)
            connection.setDoOutput(true)
            connection.setConnectTimeout(timeout)
            connection.setReadTimeout(timeout)

            val wr = new DataOutputStream(connection.getOutputStream)
            wr.writeBytes(body)
            wr.close()

            scala.io.Source.fromInputStream(connection.getInputStream).mkString
        } finally
            if (connection != null)
                connection.disconnect()
    }
}
