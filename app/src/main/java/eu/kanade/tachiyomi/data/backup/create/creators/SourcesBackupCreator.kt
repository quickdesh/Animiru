package eu.kanade.tachiyomi.data.backup.create.creators

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.data.backup.models.BackupAnime
import eu.kanade.tachiyomi.data.backup.models.BackupSource
import tachiyomi.domain.source.service.SourceManager

@Inject
class SourcesBackupCreator(
    private val sourceManager: SourceManager,
) {

    operator fun invoke(animes: List<BackupAnime>): List<BackupSource> {
        return animes
            .asSequence()
            .map(BackupAnime::source)
            .distinct()
            .map(sourceManager::getOrStub)
            .map { it.toBackupSource() }
            .toList()
    }
}

private fun AnimeSource.toBackupSource() =
    BackupSource(
        name = this.name,
        sourceId = this.id,
    )
