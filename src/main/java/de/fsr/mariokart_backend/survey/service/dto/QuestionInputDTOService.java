package de.fsr.mariokart_backend.survey.service.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationReadService;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.QuestionTranslation;
import de.fsr.mariokart_backend.survey.model.QuestionType;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamOneFreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import de.fsr.mariokart_backend.translation.Translations;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class QuestionInputDTOService {

    private final TeamRepository teamRepository;
    private final AdminRegistrationReadService adminRegistrationReadService;

    public static final int MAX_TRANSLATED_TEXT_LENGTH = 255;

    public Question questionInputDTOToQuestion(QuestionInputDTO questionInputDTO) {
        Question question;
        if (questionInputDTO.getQuestionType().equals(QuestionType.MULTIPLE_CHOICE.toString())) {
            question = new MultipleChoiceQuestion();
            ((MultipleChoiceQuestion) question).setOptions(questionInputDTO.getOptions());
        } else if (questionInputDTO.getQuestionType().equals(QuestionType.CHECKBOX.toString())) {
            question = new CheckboxQuestion();
            ((CheckboxQuestion) question).setOptions(questionInputDTO.getOptions());
        } else if (questionInputDTO.getQuestionType().equals(QuestionType.FREE_TEXT.toString())) {
            question = new FreeTextQuestion();
        } else if (questionInputDTO.getQuestionType().equals(QuestionType.TEAM.toString())) {
            question = new TeamQuestion();
            ((TeamQuestion) question).setFinalTeamsOnly(questionInputDTO.isFinalTeamsOnly());

            if (questionInputDTO.isFinalTeamsOnly()) {
                ((TeamQuestion) question).setTeams(adminRegistrationReadService.getFinalTeams());
            } else {
                ((TeamQuestion) question).setTeams(teamRepository.findAll());
            }
        } else if (questionInputDTO.getQuestionType().equals(QuestionType.TEAM_ONE_FREE_TEXT.toString())) {
            question = new TeamOneFreeTextQuestion();
        } else {
            throw new IllegalArgumentException("Invalid question type.");
        }
        question.setQuestionText(questionInputDTO.getQuestionText());
        question.setActive(questionInputDTO.isActive());
        question.setVisible(questionInputDTO.isVisible());
        question.setLive(questionInputDTO.isLive());
        question.setOneAnswerPerKey(questionInputDTO.isOneAnswerPerKey());
        if (questionInputDTO.getTranslations() != null) {
            question.setTranslations(toQuestionTranslations(questionInputDTO.getTranslations(), optionCount(question)));
        }
        return question;
    }

    private int optionCount(Question question) {
        return switch (question) {
            case MultipleChoiceQuestion choiceQuestion -> sizeOf(choiceQuestion.getOptions());
            case CheckboxQuestion checkboxQuestion -> sizeOf(checkboxQuestion.getOptions());
            default -> 0;
        };
    }

    private static int sizeOf(List<String> options) {
        return options == null ? 0 : options.size();
    }

    private Map<String, QuestionTranslation> toQuestionTranslations(Map<String, QuestionTranslation> translations,
            int optionCount) {
        return new HashMap<>(Translations.sanitize(translations, translation -> {
            String questionText = Translations.text(translation.questionText(), MAX_TRANSLATED_TEXT_LENGTH, "questions");
            List<String> options = new ArrayList<>();
            List<String> translatedOptions = translation.options() == null ? List.of() : translation.options();
            for (int index = 0; index < optionCount; index++) {
                String option = index < translatedOptions.size()
                        ? Translations.text(translatedOptions.get(index), MAX_TRANSLATED_TEXT_LENGTH, "options")
                        : null;
                options.add(option == null ? "" : option);
            }
            boolean optionsTranslated = options.stream().anyMatch(option -> !option.isEmpty());
            if (questionText == null && !optionsTranslated) {
                return null;
            }
            return new QuestionTranslation(questionText, optionsTranslated ? options : null);
        }));
    }
}
