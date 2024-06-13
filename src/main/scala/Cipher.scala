package Encryption

import scala.io.Source
import scala.util.{Try, Success, Failure, Random}
import java.nio.file.{Files, Paths, Path, StandardOpenOption}
import java.nio.charset.StandardCharsets
import java.io.IOException
import java.util.logging.{Logger, Level}

val logger: Logger = Logger.getLogger("EncryptionLogger")
val ASCIISIZE = 128

def getNumber(input: Char): Int = {
    val value = input.toInt
    val x = value match {
        case v if v >= 48 && v <= 57 => -48
        case v if v >= 65 && v <= 90 => -55
        case v if v >= 97 && v <= 122 => -61
        case v if v >= 0 && v <= 47 => 62
        case v if v >= 58 && v <= 64 => 52
        case v if v >= 91 && v <= 96 => 26
        case _ => 0
    }
    value + x
}

def getChar(input: Int): Char = {
    val value = input
    val x = value match {
        case v if v >= 0 && v <= 9 => 48
        case v if v >= 10 && v <= 35 => 55
        case v if v >= 36 && v <= 61 => 61
        case v if v >= 62 && v <= 109 => -62
        case v if v >= 110 && v <= 116 => -52
        case v if v >= 117 && v <= 122 => -26
        case _ => 0
    }
    (value + x).toChar
}

def readFile(filePath: String): String = {
    val path: Path = Paths.get(filePath).toAbsolutePath
    Try {
        Source.fromFile(path.toString).mkString
    } match {
        case Success(content) => content
        case Failure(exception) =>
            logger.log(Level.SEVERE, s"Error reading file: ${exception.getMessage}")
            s"Error reading file: ${exception.getMessage}"
            throw new IOException(s"Error reading file: ${exception.getMessage}")
    }
}

def saveFile(absolutePath: String, filename: String, content: String): Unit = {
    val fullPath = Paths.get(absolutePath.replace("\\", "/"), filename)
    if (!Files.exists(fullPath.getParent)) {
        Files.createDirectories(fullPath.getParent)
    }
    Files.write(fullPath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
}

def getData(filePath: String): (String, String, String) = {
    val separator = if (filePath.contains("/")) "/" else "\\"
    val lastSeparatorIndex = filePath.lastIndexOf(separator)
    (
        if (lastSeparatorIndex != -1) filePath.substring(0, lastSeparatorIndex) else "",
        if (lastSeparatorIndex != -1) filePath.substring(lastSeparatorIndex + 1) else filePath,
        separator
    )
}

class Shuffle(seed: Long) {
    private def createRandom(): Random = new Random(seed)

    def shuffleString(input: String): String = {
        val random = createRandom()
        val array = input.toCharArray
        for (i <- array.indices.reverse) {
            val j = random.nextInt(i + 1)
            val tmp = array(i)
            array(i) = array(j)
            array(j) = tmp
        }
        new String(array)
    }

    def reshuffleString(input: String): String = {
        val random = createRandom()
        val array = input.toCharArray
        val swaps = array.indices.reverse.map(i => (i, random.nextInt(i + 1))).toArray
        for ((i, j) <- swaps.reverse) {
            val tmp = array(i)
            array(i) = array(j)
            array(j) = tmp
        }
        new String(array)
    }
}

class Permutation(seed: Long = 0) {
    var permutation: Array[Int] = (0 until ASCIISIZE).toArray
    var revPermutation: Array[Int] = (0 until ASCIISIZE).toArray

    def generatePermutation(): Array[Int] = {
        val random = new Random(seed)
        permutation = (0 until ASCIISIZE).toArray
        for (i <- 0 until ASCIISIZE) {
            val position = i + random.nextInt(ASCIISIZE - i)
            val tmp = permutation(position)
            permutation(position) = permutation(i)
            permutation(i) = tmp
        }
        for (i <- 0 until ASCIISIZE) {
            revPermutation(permutation(i)) = i
        }
        permutation
    }

    def generateShuffler(): Char => Char = {
        (n: Char) => {
            if (getNumber(n) < 0 || getNumber(n) >= (1 << 16))
                throw new IllegalArgumentException("Input must be in the range 0 to 65535")
            getChar(permutation(getNumber(n)))
        }
    }

    def generateReshuffler(): Char => Char = {
        (n: Char) => {
            if (getNumber(n) < 0 || getNumber(n) >= (1 << 16))
                throw new IllegalArgumentException("Input must be in the range 0 to 65535")
            getChar(revPermutation(getNumber(n)))
        }
    }
}

def stringToSeed(input: String): Long = {
    var output: Long = 0
    input.foreach { char =>
        output += getNumber(char)
    }
    return output
}

def encrypt(fileContent: String, dirname: String, filename: String, extension: String, permutation: Char => Char, shuffler: String => String): String = {
    var content = fileContent + '\n' + extension
    content = shuffler(content)
    val outputBuilder = new StringBuilder(content.length)
    content.foreach { char =>
        outputBuilder.append(permutation(char))
    }
    try {
        saveFile(absolutePath = dirname, filename = filename + ".ssenc", content = outputBuilder.toString)
        "Encrypted " + filename + "." + extension + " successfully"
    } catch {
        case e: IOException => "Failure during saving " + filename + ".ssenc: " + e.getMessage
    }
}

def decrypt(fileContent: String, dirname: String, filename: String, permutation: Char => Char, reshuffler: String => String): String = {
    val content = fileContent.map(permutation)
    val reshuffledContent = reshuffler(content)
    val (originalContent, extension) = reshuffledContent.splitAt(reshuffledContent.lastIndexOf('\n'))
    try {
        saveFile(absolutePath = dirname, filename = filename + "_decrypted." + extension.trim, content = originalContent)
        "Decrypted " + filename + ".ssenc successfully"
    } catch {
        case e: IOException => "Failure during saving " + filename + "." + extension.trim + ": " + e.getMessage
    }
}

def run(input: String, key: String, cipher: Boolean): String = {
    logger.log(Level.INFO, s"Input: $input")
    val (dirname, filenameExt, separator) = getData(input)
    val filename = filenameExt.split("\\.")(0)
    val extension = filenameExt.split("\\.")(1)
    logger.log(Level.INFO, s"Directory: $dirname")
    logger.log(Level.INFO, s"Filename: $filename")
    logger.log(Level.INFO, s"Extension: $extension")
    logger.log(Level.INFO, s"Separator: $separator")

    var content = ""
    try {
        content = readFile(input)
    } catch {
        case e: IOException => return content
    }
    logger.log(Level.INFO, "File Content:")
    logger.log(Level.INFO, content)
    logger.log(Level.INFO, s"${content.length}")

    val seed = stringToSeed(key)
    val permutation = new Permutation(seed)
    val shuffler = new Shuffle(seed)
    permutation.generatePermutation()

    if (cipher) {
        return encrypt(content, dirname, filename, extension, permutation.generateShuffler(), shuffler.shuffleString)
    } 
    if (extension == "ssenc") {
        return decrypt(content, dirname, filename, permutation.generateReshuffler(), shuffler.reshuffleString)
    }

    return "Cannot decrypt files that are not .ssenc"

}

@main def main(): Unit = {
    val seed = "iubhf"

    val filename = "Cipher2.scala"

    logger.log(Level.INFO, run("/home/pprus/Main/Studia/SEM_4/Scala/simple_encryptor_scala/src/main/scala/" + filename, key = seed, cipher = true))

    logger.log(Level.INFO, "=================")

    logger.log(Level.INFO, run("/home/pprus/Main/Studia/SEM_4/Scala/simple_encryptor_scala/src/main/scala/" + (filename.split("\\."))(0) + ".ssenc", key = seed, cipher = false))

    // logger.log(Level.INFO, "On standby of sorts")

}
