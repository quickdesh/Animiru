package mihon.feature.migration.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import eu.kanade.domain.anime.model.hasCustomCover
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.presentation.anime.components.IndicatorSize
import eu.kanade.tachiyomi.animesource.model.FetchType
import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.data.download.DownloadManager
import kotlinx.coroutines.flow.update
import mihon.domain.migration.models.MigrationFlag
import mihon.domain.migration.usecases.MigrateAnimeUseCase
import mihon.feature.common.utils.getLabel
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.core.common.util.lang.withUIContext
import tachiyomi.domain.anime.model.Anime
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.components.LabeledCheckbox
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.LoadingScreen
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
internal fun Screen.MigrateAnimeDialog(
    current: Anime,
    target: Anime,
    onClickTitle: () -> Unit,
    // AY -->
    onClickSeasons: () -> Unit,
    // <-- AY
    onDismissRequest: () -> Unit,
    onComplete: () -> Unit = onDismissRequest,
) {
    val scope = rememberCoroutineScope()

    val screenModel = rememberScreenModel { MigrateDialogScreenModel(current, target) }
    val state by screenModel.state.collectAsState()
    // AY -->
    val canMigrate = remember(current.fetchType, target.fetchType) { current.fetchType == target.fetchType }
    LaunchedEffect(current, target) {
        screenModel.initialize(current, target)
    }
    // <-- AY

    if (state.isMigrating) {
        LoadingScreen(
            modifier = Modifier.background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(MR.strings.migration_dialog_what_to_include))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                if (canMigrate) {
                    state.applicableFlags.fastForEach { flag ->
                        LabeledCheckbox(
                            label = stringResource(flag.getLabel()),
                            checked = flag in state.selectedFlags,
                            onCheckedChange = { screenModel.toggleSelection(flag) },
                        )
                    }
                } else {
                    // AY -->
                    val message = if (current.fetchType == FetchType.Seasons) {
                        AYMR.strings.label_cant_migrate_season
                    } else {
                        AYMR.strings.label_cant_migrate_episode
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(IndicatorSize),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = stringResource(message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    // <-- AY
                }
            }
        },
        confirmButton = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
            ) {
                TextButton(
                    onClick = {
                        onDismissRequest()
                        onClickTitle()
                    },
                ) {
                    Text(text = stringResource(MR.strings.action_show_manga))
                }

                // AY -->
                if (target.fetchType != FetchType.Episodes) {
                    TextButton(
                        onClick = {
                            onDismissRequest()
                            onClickSeasons()
                        },
                    ) {
                        Text(text = stringResource(AYMR.strings.label_show_seasons))
                    }
                }
                // <-- AY

                Spacer(modifier = Modifier.weight(1f))

                // AY -->
                if (canMigrate) {
                    // <-- AY
                    TextButton(
                        onClick = {
                            scope.launchIO {
                                screenModel.migrateAnime(replace = false)
                                withUIContext { onComplete() }
                            }
                        },
                    ) {
                        Text(text = stringResource(MR.strings.copy))
                    }
                    TextButton(
                        onClick = {
                            scope.launchIO {
                                screenModel.migrateAnime(replace = true)
                                withUIContext { onComplete() }
                            }
                        },
                    ) {
                        Text(text = stringResource(MR.strings.migrate))
                    }
                }
            }
        },
    )
}

private class MigrateDialogScreenModel(
    private val _current: Anime,
    private val _target: Anime,
    private val sourcePreference: SourcePreferences = Injekt.get(),
    private val coverCache: CoverCache = Injekt.get(),
    private val downloadManager: DownloadManager = Injekt.get(),
    private val migrateAnime: MigrateAnimeUseCase = Injekt.get(),
) : StateScreenModel<MigrateDialogScreenModel.State>(State()) {

    // AY -->
    // Hack do get around screenmodel not being disposed
    private var current: Anime = _current
    private var target: Anime = _target
    private var hasInitialized: Boolean = false
    fun initialize(currentAnime: Anime, targetAnime: Anime) {
        if (hasInitialized && currentAnime == current && targetAnime == target) return
        current = currentAnime
        target = targetAnime

        val applicableFlags = buildList {
            MigrationFlag.entries.forEach {
                val applicable = when (it) {
                    MigrationFlag.EPISODE -> true
                    MigrationFlag.CATEGORY -> true
                    MigrationFlag.CUSTOM_COVER -> current.hasCustomCover(coverCache)
                    MigrationFlag.NOTES -> current.notes.isNotBlank()
                    MigrationFlag.REMOVE_DOWNLOAD -> downloadManager.getDownloadCount(current) > 0
                }
                if (applicable) add(it)
            }
        }
        val selectedFlags = sourcePreference.migrationFlags().get()
        mutableState.update { it.copy(applicableFlags = applicableFlags, selectedFlags = selectedFlags) }
        hasInitialized = true
    }
    // <-- AY

    init {
        initialize(_current, _target)
    }

    fun toggleSelection(flag: MigrationFlag) {
        mutableState.update {
            val selectedFlags = it.selectedFlags.toMutableSet()
                .apply { if (contains(flag)) remove(flag) else add(flag) }
                .toSet()
            it.copy(selectedFlags = selectedFlags)
        }
    }

    suspend fun migrateAnime(replace: Boolean) {
        sourcePreference.migrationFlags().set(state.value.selectedFlags)
        mutableState.update { it.copy(isMigrating = true) }
        migrateAnime(current, target, replace)
        mutableState.update { it.copy(isMigrating = false) }
    }

    data class State(
        val applicableFlags: List<MigrationFlag> = emptyList(),
        val selectedFlags: Set<MigrationFlag> = emptySet(),
        val isMigrating: Boolean = false,
    )
}
