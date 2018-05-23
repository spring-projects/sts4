# Theia IDE Language Client for STS
Theia IDE language client for handling STS specific LSP extensions. Currently handled messages:
- `sts/progress`
- `sts/moveCursor`
- `sts/addClasspathListener`
- `sts/removeClasspathListener`

Handlers for `sts/progress` and `sts/moveCursor` messages are installed by default. Handlers for adding/removing classpath listeners are installed by the API client

This package also has logic for listening to preference changes and sending them over to the LS