package org.entredeux.app.domain.usecase

fun toggleAppSelection(current: Set<String>, packageName: String): Set<String> =
    if (packageName in current) current - packageName else current + packageName
