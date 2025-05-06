package de.fsr.mariokart_backend.schedule.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameInputFullDTO {
    private long id;
    private PointsInputFullDTO[] points;
}
