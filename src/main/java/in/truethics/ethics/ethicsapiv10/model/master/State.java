package in.truethics.ethics.ethicsapiv10.model.master;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "state_tbl")
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long countryId;
    @Column(length = 2, columnDefinition = "char")
    private String countryCode;
    private String stateCode;

}