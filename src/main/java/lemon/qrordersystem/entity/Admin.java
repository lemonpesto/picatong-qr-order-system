package lemon.qrordersystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@Data
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
