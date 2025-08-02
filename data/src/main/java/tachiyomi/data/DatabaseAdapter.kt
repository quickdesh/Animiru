package tachiyomi.data

import app.cash.sqldelight.ColumnAdapter
import eu.kanade.tachiyomi.animesource.model.AnimeUpdateStrategy
import java.util.Date

object DateColumnAdapter : ColumnAdapter<Date, Long> {
    override fun decode(databaseValue: Long): Date = Date(databaseValue)
    override fun encode(value: Date): Long = value.time
}

private const val LIST_OF_STRINGS_SEPARATOR = ", "
object StringListColumnAdapter : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String) = if (databaseValue.isEmpty()) {
        emptyList()
    } else {
        databaseValue.split(LIST_OF_STRINGS_SEPARATOR)
    }
    override fun encode(value: List<String>) = value.joinToString(
        separator = LIST_OF_STRINGS_SEPARATOR,
    )
}

object UpdateStrategyColumnAdapter : ColumnAdapter<AnimeUpdateStrategy, Long> {
    override fun decode(databaseValue: Long): AnimeUpdateStrategy =
        AnimeUpdateStrategy.entries.getOrElse(databaseValue.toInt()) { AnimeUpdateStrategy.ALWAYS_UPDATE }

    override fun encode(value: AnimeUpdateStrategy): Long = value.ordinal.toLong()
}
