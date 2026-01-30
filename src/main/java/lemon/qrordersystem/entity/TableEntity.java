package lemon.qrordersystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "table_entity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableEntity {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Integer tableNum;
    
    @Column(nullable = false)
    private String accessKey;
}
