package de.fsr.mariokart_backend.survey.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceAnswer;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, // Verwende den Wert des Typs zur Auswahl der Subklasse
                include = JsonTypeInfo.As.PROPERTY, // Nutze ein Attribut im JSON zur Auswahl der Klasse
                property = "answerType" // Das Feld, das den Typ definiert (MULTIPLE_CHOICE, FREE_TEXT, etc.)
)
@JsonSubTypes({
                @JsonSubTypes.Type(value = MultipleChoiceAnswer.class, name = "MULTIPLE_CHOICE"),
                @JsonSubTypes.Type(value = FreeTextAnswer.class, name = "FREE_TEXT"),
                @JsonSubTypes.Type(value = CheckboxAnswer.class, name = "CHECKBOX")
})
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "answer_type_discriminator")
public abstract class Answer {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "question_id", nullable = false)
        private Question question;

        public abstract String getAnswerDetails();
}
