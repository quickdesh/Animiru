package tachiyomi.domain.source.model

import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.SAnime

class StubSource(
    override val id: Long,
    override val lang: String,
    override val name: String,
) : AnimeSource {

    private val isInvalid: Boolean = name.isBlank() || lang.isBlank()

    override suspend fun getAnimeDetails(anime: SAnime): SAnime =
        throw SourceNotInstalledException()

    override suspend fun getEpisodeList(anime: SAnime): List<SEpisode> =
        throw SourceNotInstalledException()
    override suspend fun getVideoList(episode: SEpisode): List<Video> =
        throw SourceNotInstalledException()

    override fun toString(): String =
        if (!isInvalid) "$name (${lang.uppercase()})" else id.toString()

    companion object {
        fun from(source: AnimeSource): StubSource {
            return StubSource(id = source.id, lang = source.lang, name = source.name)
        }
    }
}

class SourceNotInstalledException : Exception()
