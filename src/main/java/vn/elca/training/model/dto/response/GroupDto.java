package vn.elca.training.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private Long id;
    private Long version;
    private String leaderVisa;
    private String leaderName;
}
