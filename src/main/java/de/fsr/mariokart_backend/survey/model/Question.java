package de.fsr.mariokart_backend.survey.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, // Verwende den Wert des Typs zur Auswahl der Subklasse
                include = JsonTypeInfo.As.PROPERTY, // Nutze ein Attribut im JSON zur Auswahl der Klasse
                property = "questionType" // Das Feld, das den Typ definiert (MULTIPLE_CHOICE, FREE_TEXT, etc.)
)
@JsonSubTypes({
                @JsonSubTypes.Type(value = MultipleChoiceQuestion.class, name = "MULTIPLE_CHOICE"),
                @JsonSubTypes.Type(value = FreeTextQuestion.class, name = "FREE_TEXT"),
                @JsonSubTypes.Type(value = CheckboxQuestion.class, name = "CHECKBOX")
})
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "questions")
@DiscriminatorColumn(name = "question_type_discriminator")
public abstract class Question {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String questionText;

        // @Enumerated(EnumType.STRING)
        // private QuestionType questionType;

        @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<Answer> answers;

        private Boolean active;

        private Boolean visible;

        private Boolean live;

}
