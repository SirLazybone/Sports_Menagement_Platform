package com.course_work.Sports_Menagement_Platform.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orgcom")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgCom {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String logo;

    @Builder.Default
    @OneToMany(mappedBy = "orgCom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserOrgCom> userOrgComList = new ArrayList<>();

}
