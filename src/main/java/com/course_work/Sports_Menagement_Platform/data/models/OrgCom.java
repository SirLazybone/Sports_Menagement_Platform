package com.course_work.Sports_Menagement_Platform.data.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orgcom")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrgCom {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String logo;

    @OneToMany(mappedBy = "orgCom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserOrgCom> userOrgComList = new ArrayList<>();

}