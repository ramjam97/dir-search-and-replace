package utils

// for logging
class Logger {

    String processId
    File path
    File logFile
    private String fileSeparator = System.getProperty('file.separator')

    Logger(String processId, String path) {
        this.processId = processId
        this.path = new File(path)
        this.logFile = new File("${path}${fileSeparator}${processId}.log")
    }

    // create log folder if not exist
    private void checkFilePath() {
        if (!path.exists()) {
            path.mkdirs()
        }
    }

    // log message
    void msg(String message) {
        checkFilePath()
        println message
        logFile << "${message}\n"
    }

    // new line
    void br() {
        msg ''
    }

    // log success message
    void success(String message) {
        msg "[success] : ${message}"
    }

    // log info message
    void info(String message) {
        msg "[info]    : ${message}"
    }

    // log error message
    void error(String message) {
        msg "[failed]  : ${message}"
    }

}
