package com.ai.ai_triage.agent;

import com.ai.ai_triage.dto.TriageRequest;
import com.ai.ai_triage.service.LlmService;
import com.ai.ai_triage.service.MedicalReferenceService;
import com.ai.ai_triage.service.SafetyFilterService;
import org.springframework.stereotype.Service;

@Service
public class MedicalAgent {

    private final LlmService llmService;
    private final SafetyFilterService safetyFilterService;
    private final MedicalReferenceService medicalReferenceService;

    public MedicalAgent(LlmService llmService,
                        SafetyFilterService safetyFilterService,
                        MedicalReferenceService medicalReferenceService) {
        this.llmService = llmService;
        this.safetyFilterService = safetyFilterService;
        this.medicalReferenceService = medicalReferenceService;
    }

    public String analyze(TriageRequest request) {
        boolean isFrench = "fr".equalsIgnoreCase(request.getLanguage());

        // Fetch the matched reference object (title + author + url)
        MedicalReferenceService.ReferenceEntry ref =
                medicalReferenceService.findReferenceEntry(request.getSymptoms());

        String prompt = isFrench
                ? buildFrenchPrompt(request, ref)
                : buildEnglishPrompt(request, ref);

        String response = llmService.call(prompt);
        return safetyFilterService.filter(response);
    }

    // -----------------------------------------------------------------------
    // English prompt — speaks DIRECTLY to the patient ("you", "your")
    // -----------------------------------------------------------------------
    private String buildEnglishPrompt(TriageRequest request,
                                      MedicalReferenceService.ReferenceEntry ref) {
        return """
            You are a compassionate medical triage assistant speaking DIRECTLY to the patient.

            RULES — follow all of them strictly:
            1. Address the patient as "you" / "your" throughout. NEVER say "the patient should..." or "advise the patient to...".
            2. Respond in plain text only — no JSON, no bullet symbols (•), no markdown, no asterisks.
            3. Do NOT give a diagnosis.
            4. At the end of your response, explicitly cite the medical reference provided below.
               Write it exactly like this (on its own line):
               "According to [AUTHOR], [TITLE]: [URL]"
            5. Structure your response in this order:
               a) A warm, empathetic opening acknowledging what you are experiencing.
               b) The symptoms you have identified.
               c) Your assessed risk level — state it clearly as: Risk level: low / medium / high
               d) Your personalised recommendations, speaking directly to the patient.
               e) The reference citation (as described in rule 4).

            Patient information:
            - Age: %d
            - Medical history: %s
            - Reported symptoms: %s

            Medical reference to cite:
            - Title: %s
            - Author: %s
            - URL: %s
            """.formatted(
                request.getAge(),
                request.getMedicalHistory(),
                request.getSymptoms(),
                ref.title(),
                ref.author(),
                ref.url()
        );
    }

    // -----------------------------------------------------------------------
    // French prompt — speaks DIRECTLY to the patient ("vous", "votre")
    // -----------------------------------------------------------------------
    private String buildFrenchPrompt(TriageRequest request,
                                     MedicalReferenceService.ReferenceEntry ref) {
        return """
            Vous êtes un assistant médical de triage bienveillant qui parle DIRECTEMENT au patient.

            RÈGLES — respectez-les toutes strictement :
            1. Adressez-vous au patient en utilisant "vous" / "votre" tout au long. NE dites JAMAIS "le patient doit..." ou "conseillez au patient de...".
            2. Répondez en texte brut uniquement — pas de JSON, pas de symboles (•), pas de markdown, pas d'astérisques.
            3. Ne posez PAS de diagnostic.
            4. À la fin de votre réponse, citez explicitement la référence médicale fournie ci-dessous.
               Écrivez-la exactement ainsi (sur sa propre ligne) :
               "Selon [AUTEUR], [TITRE] : [URL]"
            5. Structurez votre réponse dans cet ordre :
               a) Une ouverture chaleureuse et empathique reconnaissant ce que vous ressentez.
               b) Les symptômes que vous avez identifiés.
               c) Le niveau de risque évalué — indiquez-le clairement : Niveau de risque : low / medium / high
               d) Vos recommandations personnalisées, en vous adressant directement au patient.
               e) La citation de référence (comme décrit dans la règle 4).

            Informations du patient :
            - Âge : %d
            - Antécédents médicaux : %s
            - Symptômes rapportés : %s

            Référence médicale à citer :
            - Titre : %s
            - Auteur : %s
            - URL : %s
            """.formatted(
                request.getAge(),
                request.getMedicalHistory(),
                request.getSymptoms(),
                ref.title(),
                ref.author(),
                ref.url()
        );
    }
}