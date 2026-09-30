package org.entredeux.app.domain.model

data class SelectedApp(
    val packageName: String,
    val label: String,
    // The app declares itself social, video, news or a game: the kinds that
    // most often pull people in. Only used to suggest, never to decide.
    val often: Boolean = false,
)
