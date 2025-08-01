package tachiyomi.source.local.image

import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.source.model.SManga
import java.io.InputStream

expect class LocalCoverManager {

    fun find(animeUrl: String): UniFile?

    fun update(anime: SManga, inputStream: InputStream): UniFile?
}
