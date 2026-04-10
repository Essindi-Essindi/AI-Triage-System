package com.ai.ai_triage.agent;

import com.ai.ai_triage.dto.TriageRequest;
import com.ai.ai_triage.service.LlmService;
import com.ai.ai_triage.service.MedicalReferenceService;
import com.ai.ai_triage.service.SafetyFilterService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        // Fetch ALL matching references
        List<MedicalReferenceService.ReferenceEntry> refs =
                medicalReferenceService.findAllMatchingReferences(request.getSymptoms());

        String prompt = isFrench
                ? buildFrenchPrompt(request, refs)
                : buildEnglishPrompt(request, refs);

        String response = llmService.call(prompt);
        return safetyFilterService.filter(response);
    }

    private String formatRefsForPrompt(List<MedicalReferenceService.ReferenceEntry> refs) {
        return IntStream.range(0, refs.size())
                .mapToObj(i -> {
                    MedicalReferenceService.ReferenceEntry r = refs.get(i);
                    return (i + 1) + ". Title: " + r.title() + " | Author: " + r.author() + " | URL: " + r.url();
                })
                .collect(Collectors.joining("\n"));
    }

    private String buildEnglishPrompt(TriageRequest request,
                                      List<MedicalReferenceService.ReferenceEntry> refs) {
        return """
            You are an experienced, board-certified physician writing directly to your patient.
            Your tone must be formal and professional, yet warm and reassuring — like a trusted family doctor
            who takes their time, explains things clearly, and genuinely cares about their patient's wellbeing.

            STRICT OUTPUT RULES — every rule is mandatory:
            1. Always address the patient as "you" / "your". NEVER use third-person language.
            2. Plain text only — no markdown, no bullet symbols, no asterisks, no JSON.
            3. Do NOT give a confirmed diagnosis. You MUST suggest possible conditions but frame them clearly
               as clinical possibilities only — not confirmed diagnoses.
            4. Write in complete, flowing paragraphs — not lists or bullet points.
            5. The risk level line MUST appear on its own isolated line, written EXACTLY as one of:
                 Risk level: low
                 Risk level: medium
                 Risk level: high
               Nothing else on that line.
            6. For EACH reference you use, output it on its OWN isolated line at the very end,
               written EXACTLY as:
                 Reference: "[TITLE]" by [AUTHOR] — [URL]
               If you used multiple references, write each one on a separate line in this exact format.

            ILLNESS SUGGESTION RULE (mandatory):
            - You MUST include a paragraph that suggests what condition(s) the symptoms COULD indicate.
            - Frame it explicitly as: "Based on the symptoms you have described, this could be consistent with
              conditions such as [X], [Y], or [Z]. However, I must emphasise that this is purely a clinical
              suggestion and not a confirmed diagnosis. You must consult a qualified specialist who can
              examine you properly and run the appropriate tests to reach a definitive conclusion."
            - Name the possible conditions clearly but always qualify them as possibilities.

            TONE GUIDE:
            - Open warmly: "Thank you for bringing this to my attention..." or "I appreciate you sharing..."
            - Use: "Based on what you have described...", "I would strongly encourage you to...",
              "I want to reassure you that...", "Please do not hesitate to..."
            - Close warmly and encourage the patient to seek professional care.

            RESPONSE STRUCTURE — follow this order exactly, with a blank line between each section:
            [Paragraph 1] Warm acknowledgement of what the patient is experiencing.
            [Paragraph 2] Clinical observation — describe what the symptoms may indicate.
            [Paragraph 3] Illness suggestion — suggest possible conditions with a clear disclaimer that
                          this is not a diagnosis and the patient must see a specialist.
            [Single line]  Risk level: low/medium/high
            [Paragraph 4] Clear, personalised recommendations.
            [Reference lines] One line per reference used, in the exact format above.

            Patient information:
            - Age: %d
            - Medical history: %s
            - Reported symptoms: %s

            Available medical references (use ALL that are relevant to the symptoms):
            %s
            """.formatted(
                request.getAge(),
                request.getMedicalHistory(),
                request.getSymptoms(),
                formatRefsForPrompt(refs)
        );
    }

    private String buildFrenchPrompt(TriageRequest request,
                                     List<MedicalReferenceService.ReferenceEntry> refs) {
        return """
            Vous êtes un médecin expérimenté et certifié qui s'adresse directement à son patient.
            Votre ton doit être formel et professionnel, tout en étant chaleureux et rassurant.

            RÈGLES DE SORTIE STRICTES — toutes obligatoires :
            1. Adressez-vous toujours au patient avec "vous" / "votre". N'utilisez JAMAIS la troisième personne.
            2. Texte brut uniquement — pas de markdown, pas de symboles, pas d'astérisques, pas de JSON.
            3. Ne posez PAS de diagnostic confirmé. Vous DEVEZ suggérer des conditions possibles mais les
               présenter clairement comme des possibilités cliniques uniquement — pas des diagnostics confirmés.
            4. Écrivez en paragraphes complets et fluides — pas de listes ni de points.
            5. La ligne du niveau de risque DOIT apparaître sur sa propre ligne isolée, écrite EXACTEMENT ainsi :
                 Risk level: low
                 Risk level: medium
                 Risk level: high
               Rien d'autre sur cette ligne.
            6. Pour CHAQUE référence utilisée, écrivez-la sur sa PROPRE ligne isolée à la fin, EXACTEMENT ainsi :
                 Reference: "[TITRE]" par [AUTEUR] — [URL]
               Si vous avez utilisé plusieurs références, écrivez chacune sur une ligne séparée.

            RÈGLE DE SUGGESTION DE MALADIE (obligatoire) :
            - Vous DEVEZ inclure un paragraphe qui suggère quelle(s) condition(s) les symptômes POURRAIENT indiquer.
            - Formulez-le ainsi : "D'après les symptômes que vous m'avez décrits, cela pourrait être compatible
              avec des affections telles que [X], [Y] ou [Z]. Cependant, je dois souligner qu'il s'agit
              uniquement d'une suggestion clinique et non d'un diagnostic confirmé. Vous devez consulter
              un spécialiste qualifié qui pourra vous examiner et effectuer les examens appropriés."
            - Nommez les conditions possibles clairement mais qualifiez-les toujours de possibilités.

            GUIDE DE TON :
            - Commencez par "Merci de me faire part de ce que vous ressentez..." ou "Je vous remercie de partager..."
            - Utilisez : "D'après ce que vous m'avez décrit...", "Je vous encourage vivement à...",
              "Je tiens à vous rassurer...", "N'hésitez pas à..."
            - Terminez chaleureusement en encourageant le patient à consulter.

            STRUCTURE DE LA RÉPONSE — suivez cet ordre exactement, avec une ligne vide entre chaque section :
            [Paragraphe 1] Reconnaissance chaleureuse de ce que vit le patient.
            [Paragraphe 2] Observation clinique — décrivez ce que les symptômes peuvent indiquer.
            [Paragraphe 3] Suggestion de maladie — suggérez des conditions possibles avec une clause de
                           non-responsabilité claire que ce n'est pas un diagnostic et que le patient doit consulter.
            [Ligne seule]  Risk level: low/medium/high
            [Paragraphe 4] Recommandations claires et personnalisées.
            [Lignes de référence] Une ligne par référence utilisée, dans le format exact ci-dessus.

            Informations du patient :
            - Âge : %d
            - Antécédents médicaux : %s
            - Symptômes rapportés : %s

            Références médicales disponibles (utilisez TOUTES celles qui sont pertinentes) :
            %s
            """.formatted(
                request.getAge(),
                request.getMedicalHistory(),
                request.getSymptoms(),
                formatRefsForPrompt(refs)
        );
    }
}