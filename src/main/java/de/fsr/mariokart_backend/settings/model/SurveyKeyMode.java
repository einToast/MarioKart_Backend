package de.fsr.mariokart_backend.settings.model;

public enum SurveyKeyMode {
    // Survey keys are ignored, everyone can answer
    DISABLED,
    // Everyone can answer, devices without a key receive one with their first answer
    DISTRIBUTING,
    // Only devices holding a key can answer
    REQUIRED
}
