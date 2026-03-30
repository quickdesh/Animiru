# Changelog

All notable changes to this project will be documented in this file.

The format is a modified version of [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
- `Added` - for new features.
- `Changed ` - for changes in existing functionality.
- `Improved` - for enhancement or optimization in existing functionality.
- `Removed` - for now removed features.
- `Fixed` - for any bug fixes.
- `Other` - for technical stuff.

## [Unreleased]
### Fixed
- Fix "Override ASS/SSA subtitles" option ([@Secozzi](https://github.com/Secozzi)) ([#141](https://github.com/quickdesh/Animiru/pull/141))

## [v0.19.4.2] - 2026-03-30
### Added
- Added season support for (enhanced) trackers ([@Secozzi](https://github.com/Secozzi)) ([#139](https://github.com/quickdesh/Animiru/pull/139))
- Added smart sync option for seasons ([@Secozzi](https://github.com/Secozzi)) ([#140](https://github.com/quickdesh/Animiru/pull/140))

### Improved
- Improved two-way sync for enhanced trackers ([@Secozzi](https://github.com/Secozzi)) ([#138](https://github.com/quickdesh/Animiru/pull/138))

### Fixed
- Fixed Jellyfin tracking for movies and entries with no episodes ([@Secozzi](https://github.com/Secozzi)) ([#140](https://github.com/quickdesh/Animiru/pull/140))

## [v0.19.4.1] - 2026-03-15
### Improved
- Added option to toggle subtitle rendering on black bars ([@Secozzi](https://github.com/Secozzi)) ([#134](https://github.com/quickdesh/Animiru/pull/134))
- Remove line limit for videos in quality sheet ([@Secozzi](https://github.com/Secozzi)) ([#135](https://github.com/quickdesh/Animiru/pull/135))

## [v0.19.4.0] - 2026-02-26
### Other
- Merged from Mihon ([@Secozzi](https://github.com/Secozzi)) ([#131](https://github.com/quickdesh/Animiru/pull/131))

## [v0.19.3.2] - 2026-02-23
### Added
- Added option to automatically select another video on failure to load current one ([@Secozzi](https://github.com/Secozzi)) ([#132](https://github.com/quickdesh/Animiru/pull/132))
- Added `show_seek_text` to lua bridge ([@Secozzi](https://github.com/Secozzi)) ([#132](https://github.com/quickdesh/Animiru/pull/132))

### Improved
- External subtitle tracks only load on selection ([@Secozzi](https://github.com/Secozzi)) ([#132](https://github.com/quickdesh/Animiru/pull/132))
- Chapter skipping for intro skip actually seeks by chapter([@Secozzi](https://github.com/Secozzi)) ([#132](https://github.com/quickdesh/Animiru/pull/132))

### Fixed
- Fixed start screen setting not working ([@Secozzi](https://github.com/Secozzi)) ([#128](https://github.com/quickdesh/Animiru/pull/128))

## [v0.19.3.1] - 2025-12-25
### Fixed
- Make the scrollbar on the anime screen less buggy ([@Secozzi](https://github.com/Secozzi)) ([#118](https://github.com/quickdesh/Animiru/pull/118))

## [v0.19.3.0] - 2025-12-25
### Fixed
- Fix navigation pill background disappearing on older devices ([@Secozzi](https://github.com/Secozzi)) ([#114](https://github.com/quickdesh/Animiru/pull/114))
- Fix anilist format nullability breaking search ([@Secozzi](https://github.com/Secozzi)) ([#116](https://github.com/quickdesh/Animiru/pull/116))

### Other
- Merged from Aniyomi and Mihon ([@Secozzi](https://github.com/Secozzi)) ([#115](https://github.com/quickdesh/Animiru/pull/115))

## [v0.19.0.0] - 2025-12-24
### Changed
- Remove circular edges, add background and sliding animations ([@Quickdev](https://github.com/quickdesh)) ([`8e45259`](https://github.com/quickdesh/Animiru/commit/8e45259))
- Use filter chips in recents tab ([@Quickdev](https://github.com/quickdesh)) ([`38c9c52`](https://github.com/quickdesh/Animiru/commit/38c9c52))

### Fixed
- Fix formatting of file size ([@Quickdev](https://github.com/quickdesh)) ([`958e245`](https://github.com/quickdesh/Animiru/commit/958e245))
- Don't overwrite episodes.json with anime details for localanime ([@Secozzi](https://github.com/Secozzi)) ([#96](https://github.com/quickdesh/Animiru/pull/96))
- Fix jellyfin enhanced tracker for newer versions of the extension ([@Secozzi](https://github.com/Secozzi)) ([#107](https://github.com/quickdesh/Animiru/pull/107))

### Other
- Merged from Aniyomi and Mihon ([@Secozzi](https://github.com/Secozzi)) ([#102](https://github.com/quickdesh/Animiru/pull/102) [#110](https://github.com/quickdesh/Animiru/pull/110))
- Add support for extension lib 16 ([@Secozzi](https://github.com/Secozzi)) ([#104](https://github.com/quickdesh/Animiru/pull/104))

## [v0.17.2.0] - 2024-07-27
### Fixes
- Fix extensions screen padding and loading ([@Quickdev](https://github.com/quickdesh)) ([`8e6eb30`](https://github.com/quickdesh/Animiru/commit/8e6eb30))
- Fix navigation pill tab swiping ([@Quickdev](https://github.com/quickdesh)) ([`87a246e`](https://github.com/quickdesh/Animiru/commit/87a246e))
- Fix Google drive sync ([@Quickdev](https://github.com/quickdesh)) ([`8af7c9a`](https://github.com/quickdesh/Animiru/commit/8af7c9a))
- Temporarily disable airing time sort ([@Quickdev](https://github.com/quickdesh)) ([`9637c8c`](https://github.com/quickdesh/Animiru/commit/9637c8c))

### Other
- Removed unused libraries ([@Quickdev](https://github.com/quickdesh)) ([`483dad9`](https://github.com/quickdesh/Animiru/commit/483dad9))

## [v0.17.1.0] - 2024-06-11
### Added
- Add long pressing navigation tabs ([@Quickdev](https://github.com/quickdesh)) ([`b4b1e07`](https://github.com/quickdesh/Animiru/commit/b4b1e07))

### Changed
- Remove release filter from private installer ([@Quickdev](https://github.com/quickdesh)) ([`a6a7799`](https://github.com/quickdesh/Animiru/commit/a6a7799))

### Fixed
- Fix crash when opening a new extension's settings ([@Quickdev](https://github.com/quickdesh)) ([`d90f059`](https://github.com/quickdesh/Animiru/commit/d90f059))

[unreleased]: https://github.com/quickdesh/Animiru/compare/v0.19.4.2...animiru-new-main
[v0.19.4.2]: https://github.com/quickdesh/Animiru/compare/v0.19.4.1...v0.19.4.2
[v0.19.4.1]: https://github.com/quickdesh/Animiru/compare/v0.19.4.0...v0.19.4.1
[v0.19.4.0]: https://github.com/quickdesh/Animiru/compare/v0.19.3.2...v0.19.4.0
[v0.19.3.2]: https://github.com/quickdesh/Animiru/compare/v0.19.3.1...v0.19.3.2
[v0.19.3.1]: https://github.com/quickdesh/Animiru/compare/v0.19.3.0...v0.19.3.1
[v0.19.3.0]: https://github.com/quickdesh/Animiru/compare/v0.19.0.0...v0.19.3.0
[v0.19.0.0]: https://github.com/quickdesh/Animiru/compare/v0.17.2.0...v0.19.0.0
[v0.17.2.0]: https://github.com/quickdesh/Animiru/compare/v0.17.1.0...v0.17.2.0
[v0.17.1.0]: https://github.com/quickdesh/Animiru/compare/v0.17.0.0...v0.17.1.0
