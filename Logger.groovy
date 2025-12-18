class Logger {

    String processId
    File path
    File logFile

    Logger(String processId, String path) {
        this.processId = processId
        this.path = new File(path)
        this.logFile = new File("${path}/${processId}.log")
    }

    private void checkFilePath() {
        if (!path.exists()) {
            path.mkdirs()
        }
    }

    void msg(String message) {
        checkFilePath()
        println message
        logFile << "${message}\n"
    }

    void success(String message) {
        msg "[success] : ${message}"
    }

    void info(String message) {
        msg "[info]    : ${message}"
    }

    void error(String message) {
        msg "[failed]  : ${message}"
    }

}
