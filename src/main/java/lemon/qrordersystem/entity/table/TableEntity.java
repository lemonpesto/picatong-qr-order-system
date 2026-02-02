package lemon.qrordersystem.entity.table;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "table_entity")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableEntity {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long id;

    @Column(unique = true)
    private Integer tableNum;
    private String accessKey;
    private Boolean isActive;
}
