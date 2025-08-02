package tachiyomi.domain.episode.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import tachiyomi.domain.episode.model.Episode

@Execution(ExecutionMode.CONCURRENT)
class MissingChaptersTest {

    @Test
    fun `missingChaptersCount returns 0 when empty list`() {
        emptyList<Double>().missingEpisodesCount() shouldBe 0
    }

    @Test
    fun `missingChaptersCount returns 0 when all unknown chapter numbers`() {
        listOf(-1.0, -1.0, -1.0).missingEpisodesCount() shouldBe 0
    }

    @Test
    fun `missingChaptersCount handles repeated base chapter numbers`() {
        listOf(1.0, 1.0, 1.1, 1.5, 1.6, 1.99).missingEpisodesCount() shouldBe 0
    }

    @Test
    fun `missingChaptersCount returns number of missing chapters`() {
        listOf(-1.0, 1.0, 2.0, 2.2, 4.0, 6.0, 10.0, 11.0).missingEpisodesCount() shouldBe 5
    }

    @Test
    fun `calculateChapterGap returns difference`() {
        calculateEpisodeGap(chapter(10.0), chapter(9.0)) shouldBe 0f
        calculateEpisodeGap(chapter(10.0), chapter(8.0)) shouldBe 1f
        calculateEpisodeGap(chapter(10.0), chapter(8.5)) shouldBe 1f
        calculateEpisodeGap(chapter(10.0), chapter(1.1)) shouldBe 8f

        calculateEpisodeGap(10.0, 9.0) shouldBe 0f
        calculateEpisodeGap(10.0, 8.0) shouldBe 1f
        calculateEpisodeGap(10.0, 8.5) shouldBe 1f
        calculateEpisodeGap(10.0, 1.1) shouldBe 8f
    }

    @Test
    fun `calculateChapterGap returns 0 if either are not valid chapter numbers`() {
        calculateEpisodeGap(chapter(-1.0), chapter(10.0)) shouldBe 0
        calculateEpisodeGap(chapter(99.0), chapter(-1.0)) shouldBe 0

        calculateEpisodeGap(-1.0, 10.0) shouldBe 0
        calculateEpisodeGap(99.0, -1.0) shouldBe 0
    }

    private fun chapter(number: Double) = Episode.create().copy(
        episodeNumber = number,
    )
}
