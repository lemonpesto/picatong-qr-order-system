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

    @Column(unique = true, nullable = false)
    private Integer tableNum;

    @Column(nullable = false)
    private String accessKey;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

}
