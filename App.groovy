import utils.Logger
import utils.ModifiedFile
import java.util.regex.Pattern

/*
* Application entry point
*/
class App {

    static String processId = new Date().format('yyMMddHHmmss')
    static String pattern
    static String replacement
    static Logger log
    static String backupFolderName = processId
    static Set<String> processedFiles = new HashSet<>()

    static Boolean isShallow = false
    static Boolean isIgnoreCase = false
    static String fileSeparator = System.getProperty('file.separator')

    static void main(String[] args) {
        try {
            String targetDir = getArgument(args, 0)
            pattern = getArgument(args, 1)
            replacement = getArgument(args, 2)

            String logPath = getArgument(args, 3, "${targetDir}${fileSeparator}.log")

            // get flags from arguments to update config
            List<String> flags = args.findAll { it.startsWith('--') }

            // switch to shallow mode
            isShallow = flags.indexOf('--shallow') > -1
            isIgnoreCase = flags.indexOf('--ignore-case') > -1

            // switch to form mode
            if (flags.indexOf('--form') > -1) {
                Scanner scan = new Scanner(System.in)
                println 'Enter target directory: '
                targetDir = scan.nextLine()
                println 'Enter pattern: '
                pattern = scan.nextLine()
                println 'Enter replacement: '
                replacement = scan.nextLine()
                println 'Enter log path (optional): '
                logPath = scan.nextLine()
                if (logPath.length() == 0) {
                    logPath = "${targetDir}${fileSeparator}.log"
                }
                scan.close()
            }

            File dir = new File(targetDir)

            // guard for directory
            if (!dir.exists()) {
                throw new Exception("'${targetDir}' directory does not exist")
            }

            // guard for directory
            if (!dir.isDirectory()) {
                throw new Exception("'${targetDir}' is not a directory")
            }

            log = new Logger(processId, logPath)

            Date startTime = new Date()

            log.br()
            log.msg "[START]     : ${getDateTime()}"
            log.msg "[PROCESS ID]: ${processId}"

            log.br()
            log.msg '----------[Input]----------->'
            log.msg "TARGET DIR    : ${targetDir}"
            log.msg "PATTERN       : ${pattern}"
            log.msg "REPLACEMENT   : ${replacement}"
            log.msg "LOG PATH      : ${logPath}"
            log.msg "FLAG          : ${flags}"
            log.msg '----------[Config]---------->'
            log.msg "SHALLOW       : ${isShallow}"
            log.msg "CASE SENSITIVE: ${!isIgnoreCase}"
            log.msg '---------------------------->'
            log.br()

            log.msg 'Scanning directory...'
            log.br()

            // start directory scan
            dirScanner(dir)

            log.msg 'Process completed!'
            log.br()
            log.msg "[MODIFIED]  : ${processedFiles.size()} file(s)"
            log.msg "[LOG FILE]  : ${log.logFile.absolutePath}"
            log.br()

            Date endTime = new Date()
            log.msg "[END]       : ${getDateTime()}"
            log.msg "[TIME SPENT]: ${endTime.getTime() - startTime.getTime()} ms"
        }catch (Exception e) {
            println "[failed]    : main() error: ${e.getMessage()}"
        }
    }

    // get argument
    static String getArgument(String[] args, Integer index, String defaultValue = '') {
        if (args.length > index) {
            return args[index]
        }
        return defaultValue
    }

    // get current date and time
    static String getDateTime() {
        return new Date().format('yyyy-MM-dd HH:mm:ss') as String
    }

    // process for scanning directory
    static void dirScanner(File currentDir) {
        try {
            if (!currentDir.exists()) {
                throw new Exception("'${currentDir.absolutePath}' does not exist")
            }

            // get list of text files and sub folders
            List<File> textFiles = currentDir.listFiles().collect { it }.findAll { it.isFile() && it.getName().endsWith('.txt') }

            // process text files
            textFiles.eachWithIndex { file, index -> textFileScanner(file) }

            // process sub folders recursively if not in shallow mode
            if (!isShallow) {
                // get list of sub folders
                List<File> subFolders = currentDir.listFiles().collect { it }.findAll { it.isDirectory() && !it.getName().startsWith('.') }

                // Recursion for sub folders
                subFolders.eachWithIndex { dir, index -> dirScanner(dir) }
            }
        }catch (Exception e) {
            log.error "dirScanner() error: ${e.getMessage()}"
            log.br()
        }
    }

    // get index of needle in haystack starting from startIndex
    // with or without case sensitivity
    static int getIndexOf(String haystack, String needle, Integer startIndex = 0) {
        return isIgnoreCase ? haystack.toLowerCase().indexOf(needle.toLowerCase(), startIndex)
                : haystack.indexOf(needle, startIndex)
    }

    // process text file line by line
    static void textFileScanner(File targetFile) {
        try {
            String fileName = targetFile.getName()

            // guard for text file
            if (!targetFile.getName().endsWith('.txt')) {
                throw new Exception("'${fileName}' is not a text file")
            }

            // get original file lines
            List<String> origLines = targetFile.readLines()

            // container for modified files
            List<ModifiedFile> modifiedFiles = new ArrayList<>()

            // iterate over file lines to find pattern
            List<String> newLines = origLines.withIndex().collect  { line, lnIndex ->
                // get line number
                int lineNumber = lnIndex + 1

                // quick check if line contains pattern
                int foundIndex = getIndexOf(line, pattern)

                // if pattern is found proceed further
                if (foundIndex > -1) {
                    // continue getting column numbers of matched pattern(s)
                    ArrayList<Integer> columnNumbers = new ArrayList<>([])
                    while (foundIndex > -1) {
                        columnNumbers << foundIndex + 1
                        foundIndex = getIndexOf(line, pattern, foundIndex + 1)
                    }

                    // store previous line
                    String prevLine = line

                    // Replace matched pattern
                    // with ignore case option
                    if (isIgnoreCase) {
                        // to ignore case sensitivity
                        String regex = "(?i)${Pattern.quote(pattern)}"
                        line = line.replaceAll(~regex, replacement)
                    }else {
                        // to preserve case sensitivity
                        line = line.replaceAll(pattern, replacement)
                    }

                    // add file details to modified file
                    modifiedFiles << new ModifiedFile(
                        fileName: fileName,
                        lineNumber: lineNumber,
                        content: prevLine,
                        replacement: line,
                        columnNumbers: columnNumbers
                    )

                    // add file name to processed files
                    processedFiles << fileName
                }
                return line
            }

            // check if file's lines found matches
            if (modifiedFiles.size() > 0) {
                // create backup file in file's folder
                String backupPath = copyFile(targetFile, origLines.join(System.lineSeparator()))

                // commit changes and overwrite original file
                targetFile.write(newLines.join(System.lineSeparator()))

                log.msg "<File ${processedFiles.size()}>"
                log.msg "[Source] : ${targetFile.absolutePath}"
                log.msg "[Backup] : ${backupPath}"
                log.msg "[Found]  : ${modifiedFiles.size()} line(s)"

                // display modified file details
                modifiedFiles.eachWithIndex { modifiedFile, index ->
                    int itemNo = index + 1
                    log.msg "($itemNo): Line ${modifiedFile.lineNumber} at column(s) ${modifiedFile.columnNumbers} from '${modifiedFile.content}' to '${modifiedFile.replacement}'"
                }
                log.br()
            }
        }catch (Exception e) {
            log.error "textFileScanner() error: ${e.getMessage()}"
            log.br()
        }
    }

    // for backup file and return the path
    static String copyFile(File sourceFile, String content) {
        // create backup folder if not exist
        File backupDir = new File("${sourceFile.parent}${fileSeparator}.backup${fileSeparator}${backupFolderName}")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }

        // create backup file in backup folder
        File backupFile = new File("${backupDir.absolutePath}${fileSeparator}${sourceFile.getName()}")
        backupFile << content

        return backupFile.absolutePath
    }

}
