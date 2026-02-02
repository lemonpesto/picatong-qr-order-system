package lemon.qrordersystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admins_id")
    private Long id;
    private String accessKey;
    private String username;
}
