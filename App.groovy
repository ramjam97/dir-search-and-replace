import java.io.File

/*
* Application entry point
*/
class App {

    static String pattern
    static String replacement

    static void main(String[] args) {
        printLog "Args: ${args}"

        String targetDir = 'C:/Ramjam/Accenture/Core Engineer/dir-search-and-replace/target'
        // String targetDir = args[0]
        this.pattern = args[1]
        this.replacement = args[2]

        printLog "Pattern: ${this.pattern}"
        printLog "Replacement: ${this.replacement}"
        printLog ''
        dirScanner(targetDir)
    }

    static void printLog(String message) {
        println message
    }

    static void dirScanner(String targetDir) {
        try {
            File currentDir = new File(targetDir)

            printLog "Current Directory: ${currentDir}"

            List<File> files = currentDir.listFiles().collect { it }.findAll { it.isFile() }
            List<File> subDirs = currentDir.listFiles().collect { it }.findAll { it.isDirectory() }

            printLog "Sub Folders: ${subDirs.size()}"
            printLog "Files: ${files.size()}"
            printLog ''

            files.eachWithIndex { file, index ->
                printLog "(${index + 1}) : ${file}"
                textFileScanner(file)
                printLog ''
            }

            printLog '-------------------------------------------------'
            printLog ''

            /*
            * Recursion for sub folders
            */
            for (element in subDirs) {
                dirScanner(element.getAbsolutePath())
            }
        }catch (Exception e) {
            printLog "dirScanner Error: ${e.getMessage()}"
        }
    }

    static void textFileScanner(File targetFile) {
        try {
            String fileName = targetFile.getName()

            if (!targetFile.getName().endsWith('.txt')) {
                throw new Exception("'${fileName}' is not a text file")
            }

            List<Integer> modifiedLines = []
            List<String> lines = targetFile.readLines()
            List<String> newLines = lines.withIndex().collect  { line, lnIndex ->
                int colIndex = line.indexOf(this.pattern)

                int lineNumber = lnIndex + 1
                if (colIndex > -1) {
                    String prevLine = line
                    line = line.replace(this.pattern, this.replacement)
                    printLog "[line $lineNumber] : '${prevLine}' -> '${line}'"
                    modifiedLines.add(lineNumber)
                }
                return line
            }

            printLog "[modified] : ${modifiedLines.size()}"

            if (modifiedLines.size() > 0) {
                // add backup file here
                targetFile.write(newLines.join(System.lineSeparator()))
            }
        }catch (Exception e) {
            printLog "textFileScanner Error: ${e.getMessage()}"
        }
    }

}
