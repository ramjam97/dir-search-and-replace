import java.io.File

/*
* Application entry point
*/
class App {

    static String processId = new Date().format('yyMMddHHmmss')
    static String pattern
    static String replacement
    static String backupFolderName = "${processId}"
    static Logger log

    static void main(String[] args) {
        String targetDir = 'C:/Ramjam/Accenture/Core Engineer/dir-search-and-replace/target'
        // String targetDir = args[0]
        pattern = args[1]
        replacement = args[2]

        log = new Logger(processId, "${targetDir}/.log")
        log.msg "[START] : ${getDateTime()}"
        log.msg "[PROCESS ID]: ${processId}"
        log.msg ''
        log.msg "Args: ${args}"
        log.msg "TARGER PATH: ${targetDir}"
        log.msg "PATTERN: ${pattern}"
        log.msg "REPLACEMENT: ${replacement}"
        log.msg ''
        dirScanner(targetDir)
        log.msg "[END] : ${getDateTime()}"
    }

    static String getDateTime() {
        return new Date().format('yyyy-MM-dd HH:mm:ss') as String
    }

    static void dirScanner(String targetDir, Integer level = 0) {
        try {
            File currentDir = new File(targetDir)

            log.msg "($level) Current Directory: ${currentDir}"

            List<File> files = currentDir.listFiles().collect { it }.findAll { it.isFile() }
            List<File> subDirs = currentDir.listFiles().collect { it }.findAll { it.isDirectory() && !it.getName().startsWith('.') }

            log.msg "Sub Folders: ${subDirs.size()}"
            log.msg "Files: ${files.size()}"
            log.msg ''

            files.eachWithIndex { file, index ->
                log.msg "(${level}.${index + 1}) : ${file}"
                textFileScanner(file)
                log.msg ''
            }

            /*
            * Recursion for sub folders
            */
            for (element in subDirs) {
                dirScanner(element.getAbsolutePath(), level + 1)
            }
        }catch (Exception e) {
            log.error "dirScanner() error: ${e.getMessage()}"
        }
    }

    static void textFileScanner(File targetFile) {
        try {
            String fileName = targetFile.getName()

            if (!targetFile.getName().endsWith('.txt')) {
                throw new Exception("'${fileName}' is not a text file")
            }

            List<String> origLines = targetFile.readLines()
            List<ModifiedFile> modifiedFiles = new ArrayList<>()

            List<String> newLines = origLines.withIndex().collect  { line, lnIndex ->
                int colIndex = line.indexOf(pattern)

                int lineNumber = lnIndex + 1
                if (colIndex > -1) {
                    String prevLine = line
                    line = line.replace(pattern, replacement)
                    log.msg "[line $lineNumber] : '${prevLine}' -> '${line}'"

                    modifiedFiles << new ModifiedFile(
                        fileName: fileName,
                        lineNumber: lineNumber
                    )
                }
                return line
            }

            if (modifiedFiles.size() > 0) {
                String backupPath = copyFile(targetFile, origLines.join(System.lineSeparator()))
                targetFile.write(newLines.join(System.lineSeparator()))

                log.msg "[modified] : ${modifiedFiles.size()} line(s)"
                log.msg "[backup_path] : ${backupPath}"
            }
        }catch (Exception e) {
            log.error "textFileScanner() error: ${e.getMessage()}"
        }
    }

    static String copyFile(File sourceFile, String content) {
        try {
            File backupDir = new File("${sourceFile.parent}/.backup/${backupFolderName}")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            File backupFile = new File("${backupDir.getAbsolutePath()}/${sourceFile.getName()}")
            backupFile << content

            return backupFile.getAbsolutePath()
        }catch (Exception e) {
            log.error "copyFile() error: ${e.getMessage()}"
        }
    }

}
