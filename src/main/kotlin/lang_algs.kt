import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.jakewharton.fliptables.FlipTable
import java.io.File
import kotlin.math.round

fun main() {
    val phraseFile = File("D:\\langs\\lang-labs\\src\\main\\kotlin\\phraseTable.csv")
    val textFile = File("D:\\langs\\lang-labs\\src\\main\\kotlin\\textTable.csv")

    val phraseMatrix = csvReader().readAll(phraseFile)
    val textMatrix = csvReader().readAll(textFile)

    println("Матрица текста")
    printMatrix(textMatrix)

    println("Матрица фразы")
    printMatrix(phraseMatrix)

    println("Относительная матрица текста")
    val textRelativeMatrix = getRelativeMatrix(textMatrix)
    printMatrix(textRelativeMatrix)

    println("Относительная матрица фразы")
    val phraseRelativeMatrix = getRelativeMatrix(phraseMatrix)
    printMatrix(phraseRelativeMatrix)

    println("Viterbi матрица")
    printMatrix(getViterbi(textRelativeMatrix, phraseRelativeMatrix))

    println("Forward матрица")
    printMatrix(getForward(textRelativeMatrix, phraseRelativeMatrix))

    println("Backward матрица")
    printMatrix(getBackward(textRelativeMatrix, phraseRelativeMatrix))
}

private fun getRelativeMatrix(matrix: List<List<String>>): List<List<String>> {
    val relativeMatrix = matrix.map { it.toMutableList() }.toMutableList()

    val rowCount = matrix.size
    val columnCount = matrix[0].size

    for (i in 1 until rowCount) {
        var sum = 0.0

        for (j in 1 until columnCount - 1) {
            val value = (matrix[i][j]).toDouble() / (matrix[i][columnCount - 1]).toDouble()

            sum += value
            relativeMatrix[i][j] = value.toString()
        }

        relativeMatrix[i][columnCount - 1] = sum.toString()

    }

    return relativeMatrix
}

private fun getViterbi(
    textRelativeMatrix: List<List<String>>,
    phraseRelativeMatrix: List<List<String>>
): List<List<String>> {
    val headers = mutableListOf<String>("viterbi", "start")

    val phraseRowCount = phraseRelativeMatrix.size

    for (i in 1 until phraseRowCount) {
        headers.add(phraseRelativeMatrix[i][0])
    }

    val partOfSpeechMatrix = mutableListOf<MutableList<String>>()
    partOfSpeechMatrix.add(mutableListOf())
    partOfSpeechMatrix.add(mutableListOf())
    val textRowCount = textRelativeMatrix.size

    for (i in 1 until textRowCount) {
        val partOfSpeech = textRelativeMatrix[i][0]
        partOfSpeechMatrix[0].add(partOfSpeech)
        partOfSpeechMatrix[1].add("0.0")
    }

    partOfSpeechMatrix[1][textRowCount - 2] = "1.0"


    val partOfSpeechCount = textRowCount - 1
    val phraseTransposeMatrix = transpose(phraseRelativeMatrix)

    for (wordInPhrase in 1 until phraseRowCount) {
        partOfSpeechMatrix.add(mutableListOf())

        for (partOfSpeech in 0 until partOfSpeechCount) {
            partOfSpeechMatrix[wordInPhrase + 1].add("")

            val partOfSpeechValues = mutableListOf<Double>()

            for (i in 0 until partOfSpeechCount) {
                val first = partOfSpeechMatrix[wordInPhrase][i]
                val second = textRelativeMatrix[i + 1][partOfSpeech + 1]
                val third = phraseTransposeMatrix[partOfSpeech + 1][wordInPhrase]

                partOfSpeechValues.add(
                    first.toDouble() * second.toDouble() * third.toDouble()
                )
            }

            partOfSpeechMatrix[wordInPhrase + 1][partOfSpeech] = partOfSpeechValues.max().toString()
        }
    }

    val transposedPartOfSpeech = transpose(partOfSpeechMatrix)

    return mutableListOf(headers) + transposedPartOfSpeech
}

private fun getForward(
    textRelativeMatrix: List<List<String>>,
    phraseRelativeMatrix: List<List<String>>
): List<List<String>> {
    val headers = mutableListOf<String>("forward", "start")

    val phraseRowCount = phraseRelativeMatrix.size

    for (i in 1 until phraseRowCount) {
        headers.add(phraseRelativeMatrix[i][0])
    }

    val partOfSpeechMatrix = mutableListOf<MutableList<String>>()
    partOfSpeechMatrix.add(mutableListOf())
    partOfSpeechMatrix.add(mutableListOf())
    val textRowCount = textRelativeMatrix.size

    for (i in 1 until textRowCount) {
        val partOfSpeech = textRelativeMatrix[i][0]
        partOfSpeechMatrix[0].add(partOfSpeech)
        partOfSpeechMatrix[1].add("0.0")
    }
    partOfSpeechMatrix[1][textRowCount - 2] = "1.0"


    val partOfSpeechCount = textRowCount - 1
    val textTransposeMatrix = transpose(textRelativeMatrix)
    val phraseTransposeMatrix = transpose(phraseRelativeMatrix)

    for (wordInPhrase in 1 until phraseRowCount) {
        partOfSpeechMatrix.add(mutableListOf())

        for (partOfSpeech in 0 until partOfSpeechCount) {
            partOfSpeechMatrix[wordInPhrase + 1].add("")

            var sum = 0.0
            val multiplier = phraseTransposeMatrix[partOfSpeech + 1][wordInPhrase]

            for (i in 0 until partOfSpeechCount) {
                val first = partOfSpeechMatrix[wordInPhrase][i]
                val second = textTransposeMatrix[partOfSpeech + 1][i + 1]

                sum += first.toDouble() * second.toDouble()
            }

            partOfSpeechMatrix[wordInPhrase + 1][partOfSpeech] = (multiplier.toDouble() * sum).toString()
        }
    }

    val transposedPartOfSpeech = transpose(partOfSpeechMatrix)

    return mutableListOf(headers) + transposedPartOfSpeech
}

private fun getBackward(
    textRelativeMatrix: List<List<String>>,
    phraseRelativeMatrix: List<List<String>>
): List<List<String>> {
    val headers = mutableListOf<String>("backward", "start")

    val phraseRowCount = phraseRelativeMatrix.size

    for (i in 1 until phraseRowCount) {
        headers.add(phraseRelativeMatrix[i][0])
    }

    val partOfSpeechMatrix = mutableListOf<MutableList<String>>()
    partOfSpeechMatrix.add(mutableListOf())
    partOfSpeechMatrix.add(mutableListOf())
    val textRowCount = textRelativeMatrix.size

    for (i in 1 until textRowCount) {
        val partOfSpeech = textRelativeMatrix[i][0]
        partOfSpeechMatrix[0].add(partOfSpeech)
        partOfSpeechMatrix[1].add("0.0")
    }


    for (i in 1 until phraseRowCount) {
        partOfSpeechMatrix.add(mutableListOf())

        for (j in 1 until textRowCount) {
            partOfSpeechMatrix[i + 1].add("0.0")
        }
    }

    partOfSpeechMatrix[partOfSpeechMatrix.size - 1][partOfSpeechMatrix[0].size - 1] = "1.0"

    val partOfSpeechCount = textRowCount - 1
    val phraseTransposeMatrix = transpose(phraseRelativeMatrix)

    for (wordInPhrase in (phraseRowCount - 1) downTo 1) {
        for (partOfSpeech in 0 until partOfSpeechCount) {
            var sum = 0.0

            for (i in 0 until partOfSpeechCount) {
                val first = partOfSpeechMatrix[wordInPhrase + 1][i].toDouble()
                val second = textRelativeMatrix[partOfSpeech + 1][i + 1].toDouble()
                val third = phraseTransposeMatrix[i + 1][wordInPhrase].toDouble()

                sum += first * second * third
            }

            partOfSpeechMatrix[wordInPhrase][partOfSpeech] = sum.toString()
        }
    }

    val transposedPartOfSpeech = transpose(partOfSpeechMatrix)

    return mutableListOf(headers) + transposedPartOfSpeech
}

private fun transpose(matrix: List<List<String>>): List<List<String>> {
    val row = matrix.size
    val column = matrix[0].size
    val transpose = MutableList(column) { MutableList(row) { "" } }
    for (i in 0 until row) {
        for (j in 0 until column) {
            transpose[j][i] = matrix[i][j]
        }
    }

    return transpose
}
/*Выводит матрицу в консоль*/
fun printMatrix(matrix: List<List<String>>) {
    val dataArr = matrix.subList(1, matrix.size)
    println(FlipTable.of(matrix[0].toTypedArray(), dataArr.map { it.toTypedArray() }.toTypedArray()))
}