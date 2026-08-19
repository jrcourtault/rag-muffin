package fr.drjeanjean.ragmuffin.user;

public enum UserRole {
    // Gestion des membres du workspace (ajout/suppression, modification des rôles) + tout EDITOR
    OWNER,
    // Upload/suppression de documents + tout VIEWER
    EDITOR,
    // Consultation : RAG (ask, search), lecture des documents
    VIEWER
}
