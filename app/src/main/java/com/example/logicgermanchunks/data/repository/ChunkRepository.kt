package com.example.logicgermanchunks.data.repository

import com.example.logicgermanchunks.data.model.ContextSentence
import com.example.logicgermanchunks.data.model.GermanChunk

object ChunkRepository {
    fun getFoundationalChunks(): List<GermanChunk> {
        return listOf(
            GermanChunk(
                id = "sich_freuen_auf",
                text = "sich freuen auf (+ Accusative)",
                meaning = "to look forward to",
                literalTranslation = "to joy oneself onto/upon",
                category = "Reflexive Verb + Preposition Frame",
                logicalExplanation = "Emotion is active and turned inward in German, so you reflexively 'joy yourself' (sich freuen). The preposition 'auf' (on/upon) points forward to a future event sitting on the horizon, onto which your joy is projected.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Ich habe Tickets für das Konzert am Samstag gekauft.",
                        english = "I bought tickets for the concert on Saturday."
                    ),
                    ContextSentence(
                        german = "Ich freue mich riesig auf den Auftritt der Band.",
                        english = "I am looking forward hugely to the band's performance."
                    ),
                    ContextSentence(
                        german = "Das wird bestimmt ein unvergesslicher Abend.",
                        english = "That will certainly be an unforgettable evening."
                    )
                )
            ),
            GermanChunk(
                id = "es_kommt_darauf_an",
                text = "Es kommt darauf an",
                meaning = "It depends",
                literalTranslation = "It comes there-upon on",
                category = "Fixed Structural Frame",
                logicalExplanation = "Instead of saying 'it depends', German pictures a situation 'coming' or landing 'upon' a specific condition. 'Darauf' acts as a placeholder (there-upon) pointing to the preposition 'an' (at/on) which anchors the upcoming condition.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Wollen wir morgen am See picknicken?",
                        english = "Do we want to picnic at the lake tomorrow?"
                    ),
                    ContextSentence(
                        german = "Es kommt darauf an, ob es regnet.",
                        english = "It depends on whether it rains."
                    ),
                    ContextSentence(
                        german = "Bei Regen bleiben wir lieber gemütlich zu Hause.",
                        english = "In case of rain, we would rather stay cozy at home."
                    )
                )
            ),
            GermanChunk(
                id = "angst_haben_vor",
                text = "Angst haben vor (+ Dative)",
                meaning = "to be afraid of",
                literalTranslation = "to have fear before",
                category = "Noun + Verb + Preposition Frame",
                logicalExplanation = "In German, you don't 'be' afraid; you active 'have' fear (Angst haben). Crucially, the fear is positioned 'vor' (before/in front of) you, acting as a physical barrier blocking your path, rather than being 'of' something.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Im Keller ist es sehr dunkel und kalt.",
                        english = "It is very dark and cold in the basement."
                    ),
                    ContextSentence(
                        german = "Ich habe Angst vor der Dunkelheit dort unten.",
                        english = "I am afraid of the darkness down there (literally: I have fear before the darkness)."
                    ),
                    ContextSentence(
                        german = "Kannst du bitte mit mir hinuntergehen?",
                        english = "Can you please go down with me?"
                    )
                )
            ),
            GermanChunk(
                id = "sich_entscheiden_fuer",
                text = "sich entscheiden für (+ Accusative)",
                meaning = "to decide on / choose",
                literalTranslation = "to separate/divorce oneself for",
                category = "Reflexive Verb + Preposition Frame",
                logicalExplanation = "The verb root 'scheiden' means to separate or divorce. When making a decision, you separate yourself from all other options (sich entscheiden) in favor of or 'for' (für) one single choice.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Es gibt so viele leckere Kuchen in der Vitrine.",
                        english = "There are so many delicious cakes in the display case."
                    ),
                    ContextSentence(
                        german = "Ich entscheide mich für das Stück Erdbeertorte.",
                        english = "I decide on the piece of strawberry cake."
                    ),
                    ContextSentence(
                        german = "Dazu trinke ich einen heißen Kaffee.",
                        english = "With that, I'll drink a hot coffee."
                    )
                )
            ),
            GermanChunk(
                id = "wert_legen_auf",
                text = "Wert legen auf (+ Accusative)",
                meaning = "to value / attach importance to",
                literalTranslation = "to lay worth onto",
                category = "Collocation",
                logicalExplanation = "Instead of just holding a value abstractly, German treats value/worth (Wert) as a physical object that you actively place/lay (legen) directly onto (auf) a matter.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Unser Chef ist sehr streng bei Terminen.",
                        english = "Our boss is very strict with deadlines."
                    ),
                    ContextSentence(
                        german = "Er legt großen Wert auf absolute Pünktlichkeit.",
                        english = "He attaches great importance to absolute punctuality (literally: He lays great worth onto absolute punctuality)."
                    ),
                    ContextSentence(
                        german = "Deshalb kommen alle Mitarbeiter immer überpünktlich an.",
                        english = "Therefore, all employees always arrive early."
                    )
                )
            ),
            GermanChunk(
                id = "sich_interessieren_fuer",
                text = "sich interessieren für (+ Accusative)",
                meaning = "to be interested in",
                literalTranslation = "to interest oneself for",
                category = "Reflexive Verb + Preposition Frame",
                logicalExplanation = "In German, interest is not a passive state you 'are' in. Instead, you actively 'interest yourself' (sich interessieren). The preposition 'für' (for) denotes the target or cause of your interest, pointing to what you are supporting or seeking.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Thomas liest jeden Tag wissenschaftliche Artikel.",
                        english = "Thomas reads scientific articles every day."
                    ),
                    ContextSentence(
                        german = "Er interessiert sich brennend für Astrophysik.",
                        english = "He is burning-ly interested in astrophysics."
                    ),
                    ContextSentence(
                        german = "Sein Traum ist es, einmal die Sterne zu erforschen.",
                        english = "His dream is to one day explore the stars."
                    )
                )
            ),
            GermanChunk(
                id = "abhaengen_von",
                text = "abhängen von (+ Dative)",
                meaning = "to depend on",
                literalTranslation = "to hang off from",
                category = "Verb + Preposition Frame",
                logicalExplanation = "Similar to the Latin root of 'depend' (dependere - to hang down), German visualizes a decision literally hanging (hängen) off (von) a certain condition. If that condition moves, the decision moves too.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Unsere Pläne für das Wochenende sind noch unsicher.",
                        english = "Our plans for the weekend are still uncertain."
                    ),
                    ContextSentence(
                        german = "Unsere Wanderung wird vom Wetter abhängen.",
                        english = "Our hike will depend on the weather (literally: hang off from the weather)."
                    ),
                    ContextSentence(
                        german = "Wenn es stürmt, bleiben wir in der Hütte.",
                        english = "If it storms, we will stay in the cabin."
                    )
                )
            ),
            GermanChunk(
                id = "sich_erinnern_an",
                text = "sich erinnern an (+ Accusative)",
                meaning = "to remember",
                literalTranslation = "to inner-oneself on/at",
                category = "Reflexive Verb + Preposition Frame",
                logicalExplanation = "The verb 'erinnern' means to move something into your 'inner' mental space. You do this to yourself (sich erinnern), and you anchor that memory 'an' (at/onto) the object of remembrance, like pinning an idea onto a board.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Ich habe ein altes Foto von uns im Schrank gefunden.",
                        english = "I found an old photo of us in the closet."
                    ),
                    ContextSentence(
                        german = "Ich erinnere mich gern an unseren Urlaub in Italien.",
                        english = "I like to remember our vacation in Italy (literally: inner-myself onto our vacation in Italy)."
                    ),
                    ContextSentence(
                        german = "Damals hatten wir eine wunderbare Zeit zusammen.",
                        english = "Back then, we had a wonderful time together."
                    )
                )
            ),
            GermanChunk(
                id = "ein_Auge_werfen_auf",
                text = "ein Auge werfen auf (+ Accusative)",
                meaning = "to have one's eye on / watch",
                literalTranslation = "to throw an eye onto",
                category = "Idiomatic Expression",
                logicalExplanation = "Instead of just watching passively, German metaphorically takes one eye and 'throws' (werfen) it onto (auf) an object of desire or curiosity, indicating highly active visual focus.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Das alte Fahrrad meines Bruders ist kaputt.",
                        english = "My brother's old bicycle is broken."
                    ),
                    ContextSentence(
                        german = "Er hat bereits ein Auge auf das neue Rennrad im Laden geworfen.",
                        english = "He has already thrown an eye onto the new racing bike in the shop."
                    ),
                    ContextSentence(
                        german = "Jetzt spart er jeden Monat Geld dafür.",
                        english = "Now he is saving money for it every month."
                    )
                )
            ),
            GermanChunk(
                id = "es_handelt_sich_um",
                text = "Es handelt sich um (+ Accusative)",
                meaning = "It is about / It is a matter of",
                literalTranslation = "It deals itself around",
                category = "Fixed Structural Frame",
                logicalExplanation = "The verb 'handeln' relates to trade or negotiation. This phrase states that the situation 'deals itself' (handelt sich) specifically 'around' (um) the core topic, revolving around it as its center.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Ein mysteriöses Paket kam heute mit der Post an.",
                        english = "A mysterious package arrived in the mail today."
                    ),
                    ContextSentence(
                        german = "Es handelt sich um ein Geschenk von meiner Tante.",
                        english = "It is a gift from my aunt (literally: It deals itself around a gift)."
                    ),
                    ContextSentence(
                        german = "Ich darf es erst an meinem Geburtstag öffnen.",
                        english = "I am only allowed to open it on my birthday."
                    )
                )
            ),
            GermanChunk(
                id = "auf_dem_Laufenden_bleiben",
                text = "auf dem Laufenden bleiben",
                meaning = "to stay updated / in the loop",
                literalTranslation = "to stay on the running [one]",
                category = "Collocation",
                logicalExplanation = "Events and information flow like a running stream (der Laufende). To stay updated, you must stand directly 'on' (auf) this running current, keeping pace with the flow of information.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Die Technologie in unserer Branche verändert sich extrem schnell.",
                        english = "The technology in our industry changes extremely fast."
                    ),
                    ContextSentence(
                        german = "Wir müssen ständig auf dem Laufenden bleiben, um wettbewerbsfähig zu sein.",
                        english = "We must constantly stay in the loop (literally: stay on the running) to remain competitive."
                    ),
                    ContextSentence(
                        german = "Regelmäßige Schulungen helfen uns dabei sehr.",
                        english = "Regular training helps us a lot with this."
                    )
                )
            ),
            GermanChunk(
                id = "sich_befassen_mit",
                text = "sich befassen mit (+ Dative)",
                meaning = "to deal with / occupy oneself with",
                literalTranslation = "to grasp/touch oneself with",
                category = "Reflexive Verb + Preposition Frame",
                logicalExplanation = "Derived from 'fassen' (to grasp/seize). You physically and mentally envelope yourself (sich befassen) together 'mit' (with) a subject, handling it actively and with care.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Die neue Steuererklärung ist sehr kompliziert.",
                        english = "The new tax return is very complicated."
                    ),
                    ContextSentence(
                        german = "Ich muss mich am Wochenende gründlich mit den Formularen befassen.",
                        english = "I have to deal thoroughly with the forms on the weekend."
                    ),
                    ContextSentence(
                        german = "Hoffentlich finde ich alle Belege rechtzeitig.",
                        english = "Hopefully, I will find all receipts on time."
                    )
                )
            ),
            GermanChunk(
                id = "rechnen_mit",
                text = "rechnen mit (+ Dative)",
                meaning = "to expect / count on",
                literalTranslation = "to calculate/reckon with",
                category = "Verb + Preposition Frame",
                logicalExplanation = "When anticipating an outcome, you include it as a mathematical variable in your plans. You literally 'calculate' (rechnen) with (mit) its occurrence as a given.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Dunkle Wolken ziehen am Himmel auf.",
                        english = "Dark clouds are gathering in the sky."
                    ),
                    ContextSentence(
                        german = "Wir müssen in den nächsten Stunden mit einem schweren Sturm rechnen.",
                        english = "We have to expect a severe storm in the next few hours (literally: calculate with a severe storm)."
                    ),
                    ContextSentence(
                        german = "Bring lieber schnell die Gartenmöbel ins Haus.",
                        english = "Better bring the garden furniture into the house quickly."
                    )
                )
            ),
            GermanChunk(
                id = "sich_konzentrieren_auf",
                text = "sich konzentrieren auf (+ Accusative)",
                meaning = "to focus on",
                literalTranslation = "to center oneself onto",
                category = "Reflexive Verb + Preposition Frame",
                logicalExplanation = "You pull all your distributed energy and focus, gathering them to 'center' yourself (sich konzentrieren). This gathered beam of attention is then projected onto (auf) a target.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Der Raum ist voller lauter Gespräche.",
                        english = "The room is full of loud conversations."
                    ),
                    ContextSentence(
                        german = "Trotz des Lärms muss ich mich auf meine Arbeit konzentrieren.",
                        english = "Despite the noise, I have to focus on my work (literally: center myself onto my work)."
                    ),
                    ContextSentence(
                        german = "Ich setze mir jetzt Kopfhörer auf.",
                        english = "I'm putting on headphones now."
                    )
                )
            ),
            GermanChunk(
                id = "zweifeln_an",
                text = "zweifeln an (+ Dative)",
                meaning = "to doubt",
                literalTranslation = "to make/be in two at/on",
                category = "Verb + Preposition Frame",
                logicalExplanation = "The root of 'zweifeln' is 'zwei' (two). To doubt is to be torn between two minds or outcomes. You perform this state of 'two-ing' (zweifeln) directly 'an' (at/on) the truth value of a claim.",
                contextSentences = listOf(
                    ContextSentence(
                        german = "Seine Geschichte klingt einfach zu fantastisch.",
                        english = "His story just sounds too fantastic."
                    ),
                    ContextSentence(
                        german = "Ich zweifle stark an der Wahrheit seiner Worte.",
                        english = "I doubt strongly the truth of his words (literally: make-two at the truth of his words)."
                    ),
                    ContextSentence(
                        german = "Niemand kann so schnell um die Welt reisen.",
                        english = "No one can travel around the world that fast."
                    )
                )
            )
        )
    }
}
