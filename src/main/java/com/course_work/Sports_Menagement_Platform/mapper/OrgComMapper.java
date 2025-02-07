package com.course_work.Sports_Menagement_Platform.mapper;

import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.dto.OrgComDTO;
import org.springframework.stereotype.Component;

@Component
public class OrgComMapper {
    public OrgCom DTOToEntity(OrgComDTO orgComDTO) {
        return OrgCom.builder().name(orgComDTO.getName()).build();
    }
}
