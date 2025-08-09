package tachiyomi.domain.episode.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.CONCURRENT)
class EpisodeRecognitionTest {

    @Test
    fun `Basic Ep prefix`() {
        val animeTitle = "Bleach"

        assertEpisode(animeTitle, "Bleach Season 2 Ep.4", 4.0)
    }

    @Test
    fun `Basic Ep prefix with space after period`() {
        val animeTitle = "Bleach"

        assertEpisode(animeTitle, "Bleach Season 1 Ep. 4: And Your Bird Can Speak", 4.0)
    }

    @Test
    fun `Basic Ep prefix with decimal`() {
        val animeTitle = "Boku no Hero Academia"

        assertEpisode(animeTitle, "Boku no Hero Academia (S2) - 13.5: Hero Notebook ", 13.5)
    }

    @Test
    fun `Name containing one number`() {
        val animeTitle = "Bleach"

        assertEpisode(animeTitle, "Bleach 134 The Beautiful Patissier, Yumichika!", 134.0)
    }

    @Test
    fun `Name containing one number and decimal`() {
        val animeTitle = "Bleach"

        assertEpisode(animeTitle, "Bleach 134.1 The Beautiful Patissier, Yumichika!", 134.1)
        assertEpisode(animeTitle, "Bleach 134.4 The Beautiful Patissier, Yumichika!", 134.4)
    }

    @Test
    fun `Episode containing anime title and number`() {
        val animeTitle = "One Piece"

        assertEpisode(animeTitle, "One Piece 0006 Season 2", 6.0)
    }

    @Test
    fun `Episode containing anime title and number decimal`() {
        val animeTitle = "One Piece"

        assertEpisode(animeTitle, "One Piece 0006.1 Vol. 2", 6.1)
        assertEpisode(animeTitle, "One Piece 0006.4 Vol. 2", 6.4)
    }

    @Test
    fun `Extreme case`() {
        val animeTitle = "Onepunch-Man"

        assertEpisode(animeTitle, "Onepunch-Man Punch Ver002 028", 28.0)
    }

    @Test
    fun `Extreme case with decimal`() {
        val animeTitle = "Onepunch-Man"

        assertEpisode(animeTitle, "Onepunch-Man Punch Ver002 028.1", 28.1)
        assertEpisode(animeTitle, "Onepunch-Man Punch Ver002 028.4", 28.4)
    }

    @Test
    fun `Episode containing dot v2`() {
        val animeTitle = "random"

        assertEpisode(animeTitle, "Season.1 Ep.5v.2: Alones", 5.0)
    }

    @Test
    fun `Number in anime title`() {
        val animeTitle = "11 Eyes"

        assertEpisode(animeTitle, "11 Eyes 3 - Lonely Pride", 3.0)
    }

    @Test
    fun `Space between ep x`() {
        val animeTitle = "Bleach"

        assertEpisode(animeTitle, "Bleach Season 1 Ep. 12: A Gentle Right Arm", 12.0)
    }

    @Test
    fun `Episode title with ep substring`() {
        val animeTitle = "Bleach"

        assertEpisode(animeTitle, "Season 1 Ep.3: Step 1 to becoming a shinigami", 3.0)
    }

    @Test
    fun `Episode containing multiple zeros`() {
        val animeTitle = "One piece"

        assertEpisode(animeTitle, "Season.001 Ep.0008: Who Is the Victor? Devil Fruit Power Showdown! ", 8.0)
    }

    @Test
    fun `Version attached to episode number`() {
        val animeTitle = "Shingeki no Kyojin"

        assertEpisode(animeTitle, "Shingeki no Kyojin - 03v2", 3.0)
    }

    @Test
    fun `Unparseable episode`() {
        val animeTitle = "random"

        assertEpisode(animeTitle, "Foo", -1.0)
    }

    @Test
    fun `Episode with time in title`() {
        val animeTitle = "random"

        assertEpisode(
            animeTitle,
            "Jigoku Sensei Nube 23: Mystery of 00:00:00!? Parallel Mirrors of the Seven Mysteries",
            23.0,
        )
    }

    @Test
    fun `Episode title containing commas`() {
        val animeTitle = "One Piece"

        assertEpisode(animeTitle, "One Piece the sunny, goes swimming 024,005", 24.005)
    }

    @Test
    fun `Episode title containing hyphens`() {
        val animeTitle = "Solo Leveling"

        assertEpisode(animeTitle, "ep 122-a", 122.1)
        assertEpisode(animeTitle, "Solo Leveling Ep.123-extra", 123.99)
        assertEpisode(animeTitle, "Solo Leveling, 024-005", 24.005)
        assertEpisode(animeTitle, "Ep.191-200 Read Online", 191.200)
    }

    @Test
    fun `Episodes containing ordinals`() {
        val animeTitle = "The Sister of the Woods with a Thousand Young"

        assertEpisode(animeTitle, "The 1st Night", 1.0)
        assertEpisode(animeTitle, "The 2nd Night", 2.0)
        assertEpisode(animeTitle, "The 3rd Night", 3.0)
        assertEpisode(animeTitle, "The 4th Night", 4.0)
    }

    @Test
    fun `Episodes containing tags`() {
        val animeTitle = "random"

        assertEpisode(animeTitle, "[MTBB] Shingeki no Kyojin - 03 (BD 1080p) [67CDBAC6]", 3.0)
        assertEpisode(animeTitle, "[ASC] Bleach - 012 (DVD 576p Hi10 FLAC) [0C700E28]", 12.0)
    }

    @Test
    fun `Test unwanted names in episodes`() {
        val animeTitle = "random"

        assertEpisode(animeTitle, "Kaguya-sama.Love.Is.War.S02E05.REPACK.1080p.BluRay.Opus2.0.x265-Flugel", 5.0)
        assertEpisode(
            animeTitle,
            "SHOSHIMIN.How.to.Become.Ordinary.S01E04.1080p.BluRay.10-Bit.FLAC2.0.x265-YURASUKA",
            4.0,
        )
        assertEpisode(animeTitle, "(Hi10)_Shingeki_no_Bahamut_Genesis_-_06_(BD_1080p)_(Mawaru)_(DualA)_(10910F15)", 6.0)
    }

    private fun assertEpisode(animeTitle: String, name: String, expected: Double) {
        EpisodeRecognition.parseEpisodeNumber(animeTitle, name) shouldBe expected
    }
}
