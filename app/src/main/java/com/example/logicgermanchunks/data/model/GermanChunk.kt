package com.example.logicgermanchunks.data.model

data class ContextSentence(
    val german: String,
    val english: String
)

data class GermanChunk(
    val id: String,
    val text: String,
    val meaning: String,
    val literalTranslation: String,
    val category: String,
    val logicalExplanation: String,
    val contextSentences: List<ContextSentence>
)
